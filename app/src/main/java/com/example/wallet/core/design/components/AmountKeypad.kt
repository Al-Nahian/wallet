package com.example.wallet.core.design.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wallet.core.design.glass.GlassShapes
import com.example.wallet.core.design.glass.GlassStyle
import com.example.wallet.core.design.glass.GlassSurface
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
fun AmountKeypad(onKeyPress: (String) -> Unit, modifier: Modifier = Modifier, tint: Color = MaterialTheme.colorScheme.surfaceVariant) {
    val rows = listOf(
        listOf("7", "8", "9", "÷"),
        listOf("4", "5", "6", "×"),
        listOf("1", "2", "3", "-"),
        listOf(".", "0", "⌫", "+"),
    )
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
            onClick = { onKeyPress("=") },
            modifier = Modifier.fillMaxWidth().height(52.dp),
        )
    }
}

@Composable
private fun KeypadButton(label: String, tint: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    GlassSurface(
        modifier = modifier.height(52.dp).clickable(onClick = onClick),
        style = GlassStyle.Thin,
        shape = GlassShapes.medium,
        elevation = 0.dp,
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = label, fontSize = 18.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
