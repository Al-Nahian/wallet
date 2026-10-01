package com.expensetracker.wallet.core.common

/**
 * plans/12-import-export.md deliberately avoids pulling in a CSV library for this alone — a
 * hand-rolled RFC4180-ish parser/writer covers what the import/export feature actually needs:
 * comma-separated fields, double-quote-wrapped fields (needed when a field itself contains a
 * comma, quote, or newline), and `""` as an escaped quote inside a quoted field.
 */
object Csv {

    fun parse(text: String): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        val row = mutableListOf<String>()
        val field = StringBuilder()
        var inQuotes = false
        var i = 0
        var sawAnyContentInRow = false

        fun endField() {
            row.add(field.toString())
            field.clear()
        }

        fun endRow() {
            endField()
            rows.add(row.toList())
            row.clear()
            sawAnyContentInRow = false
        }

        while (i < text.length) {
            val c = text[i]
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < text.length && text[i + 1] == '"') {
                        field.append('"')
                        i++
                    } else {
                        inQuotes = false
                    }
                } else {
                    field.append(c)
                }
            } else {
                when (c) {
                    '"' -> {
                        inQuotes = true
                        sawAnyContentInRow = true
                    }
                    ',' -> {
                        endField()
                        sawAnyContentInRow = true
                    }
                    '\r' -> { /* swallow; \n (or trailing \r\n) ends the row */ }
                    '\n' -> endRow()
                    else -> {
                        field.append(c)
                        sawAnyContentInRow = true
                    }
                }
            }
            i++
        }
        if (sawAnyContentInRow || field.isNotEmpty()) {
            endRow()
        }
        return rows
    }

    fun writeRow(fields: List<String>): String =
        fields.joinToString(",") { escapeField(it) }

    fun write(rows: List<List<String>>): String =
        rows.joinToString("\r\n") { writeRow(it) } + "\r\n"

    private fun escapeField(value: String): String {
        val needsQuoting = value.any { it == ',' || it == '"' || it == '\n' || it == '\r' }
        return if (needsQuoting) "\"${value.replace("\"", "\"\"")}\"" else value
    }
}
