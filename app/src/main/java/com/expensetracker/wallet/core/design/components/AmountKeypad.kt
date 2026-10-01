package com.expensetracker.wallet.core.design.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expensetracker.wallet.core.design.WalletTheme
import com.expensetracker.wallet.core.design.glass.GlassColors
import com.expensetracker.wallet.core.design.glass.GlassShapes
import com.expensetracker.wallet.core.design.glass.GlassStyle
import com.expensetracker.wallet.core.design.glass.GlassSurface
import java.util.Locale

private val OPERATOR_CHARS = charArrayOf('+', '-', '×', '÷')

/** Applies one [AmountKeypad] key press to the current amount-input string — a tiny calculator
 * state machine (single binary operation, no operator precedence/parens) rather than a full
 * expression parser, since "add two numbers together" covers the realistic use case (splitting a
 * receipt, correcting a typo) without the complexity of a real calculator engine. */
fun applyAmountKeypadKey(current: String, key: String): String = when (key) {
    "⌫" -> if (current.isNotEmpty()) current.dropLast(1) else current
    "=" -> evaluateAmountExpression(current)
    "." -> {
        val lastOperandStart = current.indexOfLast { it in OPERATOR_CHARS } + 1
        val lastOperand = current.substring(lastOperandStart)
        if (lastOperand.contains('.')) current else current + "."
    }
    "+", "-", "×", "÷" -> when {
        current.isEmpty() -> current
        current.last() in OPERATOR_CHARS || current.last() == '.' -> current.dropLast(1) + key
        else -> current + key
    }
    else -> current + key
}

/** Display-only financial formatting for the amount entry: international thousands grouping,
 * with decimals shown only when the user actually typed them (`1000` → `1,000`, but `1.45` →
 * `1.45`), applied live while typing. Editing stays on the raw string ([applyAmountKeypadKey]) —
 * this never feeds back into it, so there is no cursor to manage and backspace/operators/`=`
 * all behave exactly as before.
 *
 * Partial states are preserved, not forced: a trailing dot (`50.`) stays visible as the pending-
 * decimal indicator rather than vanishing (which would hide the user's own keystroke). An
 * all-zero fraction is dropped (`50.00` → `50`, `50.10` stays `50.10`), so zeroes never trail by
 * default yet every typed decimal digit shows. Each side of an operator formats independently
 * (`120+35` → `120+35`). */
fun formatAmountEntryForDisplay(raw: String): String {
    if (raw.isEmpty()) return "0"
    val out = StringBuilder()
    var token = StringBuilder()
    for (char in raw) {
        if (char in OPERATOR_CHARS) {
            if (token.isNotEmpty()) {
                out.append(formatEntryToken(token.toString()))
                token = StringBuilder()
            }
            out.append(char)
        } else {
            token.append(char)
        }
    }
    if (token.isNotEmpty()) out.append(formatEntryToken(token.toString()))
    return out.toString().ifEmpty { "0" }
}

private fun formatEntryToken(token: String): String {
    val dot = token.indexOf('.')
    val intRaw = (if (dot >= 0) token.substring(0, dot) else token).ifEmpty { "0" }
    // A lone leading zero is just typing padding (`007` → `7`); a bare `0` stays `0`.
    val intNormalized = intRaw.trimStart('0').ifEmpty { "0" }
    val grouped = groupThousands(intNormalized)
    if (dot < 0) return grouped
    val fraction = token.substring(dot + 1)
    // Pending dot with nothing after it yet (`50.`) keeps its dot; an all-zero fraction
    // (`50.00`) is dropped so zeroes never trail by default; anything else shows as typed.
    if (fraction.isEmpty()) return "$grouped."
    return if (fraction.all { it == '0' }) grouped else "$grouped.$fraction"
}

/** International 3-digit grouping on a pure digit string (no numeric conversion, so arbitrarily
 * long inputs can't overflow). */
private fun groupThousands(digits: String): String {
    if (digits.length <= 3) return digits
    val remainder = digits.length % 3
    return buildString {
        var index = 0
        if (remainder > 0) {
            append(digits.substring(0, remainder))
            index = remainder
        }
        while (index < digits.length) {
            if (isNotEmpty()) append(',')
            append(digits.substring(index, index + 3))
            index += 3
        }
    }
}

/** Evaluates a `<number><operator><number>` expression (the only shape [applyAmountKeypadKey]
 * ever builds); returns [expr] unchanged if it isn't one (e.g. no operator yet, divide by zero,
 * or a malformed operand) rather than throwing — the field should never crash on "=" before the
 * user has finished typing. */
fun evaluateAmountExpression(expr: String): String {
    for (i in 1 until expr.length) {
        val operator = expr[i]
        if (operator in OPERATOR_CHARS) {
            val left = expr.substring(0, i).toDoubleOrNull() ?: return expr
            val right = expr.substring(i + 1).toDoubleOrNull() ?: return expr
            val result = when (operator) {
                '+' -> left + right
                '-' -> left - right
                '×' -> left * right
                '÷' -> if (right != 0.0) left / right else return expr
                else -> return expr
            }
            return String.format(Locale.US, "%.2f", result)
        }
    }
    return expr
}

/** A docked calculator-style keypad for the amount card (reference design) — used instead of the
 * system IME so entry doesn't depend on IME focus/timing at all; also supports basic arithmetic
 * (e.g. entering "120+35" and tapping "=") for combining two amounts inline. */
@Composable
fun AmountKeypad(onKeyPress: (String) -> Unit, modifier: Modifier = Modifier, tint: Color = WalletTheme.extendedColors.transfer) {
    val rows = listOf(
        listOf("7", "8", "9", "÷"),
        listOf("4", "5", "6", "×"),
        listOf("1", "2", "3", "-"),
        listOf(".", "0", "⌫", "+"),
    )
    // Reference design's "=" key is a lighter steel-blue bar, not another dark key.
    val equalsFill = if (isSystemInDarkTheme()) EqualsKeyFillDark else null
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { key ->
                    KeypadButton(label = key, tint = tint, onClick = { onKeyPress(key) }, modifier = Modifier.weight(1f))
                }
            }
        }
        KeypadButton(
            label = "=",
            tint = tint,
            fillOverride = equalsFill,
            onClick = { onKeyPress("=") },
            modifier = Modifier.fillMaxWidth().height(52.dp),
        )
    }
}

/** Steel-blue fill for the "=" key in dark mode (reference design) — light mode keeps the
 * default key treatment. */
private val EqualsKeyFillDark = Color(0xFF3A4A5E)

@Composable
private fun KeypadButton(label: String, tint: Color, onClick: () -> Unit, modifier: Modifier = Modifier, fillOverride: Color? = null) {
    GlassSurface(
        modifier = modifier.height(52.dp).clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick,
        ),
        // Same neutral-fill-blended-with-accent treatment as every other "black card" in the app,
        // rather than GlassStyle.Thin's low-alpha fill, which read as a flat black key against
        // the page background.
        style = GlassStyle.Thick,
        shape = GlassShapes.medium,
        fill = fillOverride ?: GlassColors.neutralTintedFill(tint),
        elevation = 0.dp,
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            // Reference light design uses an outline backspace glyph, not the "⌫" text char.
            // Dark mode keeps its confirmed text glyph untouched.
            if (label == "⌫" && !isSystemInDarkTheme()) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp),
                )
            } else {
                Text(text = label, fontSize = 18.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}
