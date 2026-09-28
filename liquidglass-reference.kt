package com.yourapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max

/**
 * Different wave geometries that can be assigned to individual cards.
 */
enum class LiquidWaveStyle {
    DIAGONAL,
    SWEEP,
    S_CURVE,
    RISING,
    FALLING,
    DOUBLE_WAVE
}

/**
 * Configuration for a LiquidGlassCard.
 */
data class LiquidGlassStyle(
    val colors: List<Color>,

    val waveStyle: LiquidWaveStyle = LiquidWaveStyle.SWEEP,

    // Strength of the translucent wave.
    val waveAlpha: Float = 0.14f,

    // Strength of the white glass reflection.
    val highlightAlpha: Float = 0.12f,

    // Strength of the outer colored glow.
    val glowAlpha: Float = 0.20f,

    // Border opacity.
    val borderAlpha: Float = 0.32f,

    // How much the card looks transparent.
    val glassAlpha: Float = 0.92f,

    // Where the highlight is positioned.
    val highlightPosition: LiquidHighlightPosition =
        LiquidHighlightPosition.TOP_RIGHT
)

enum class LiquidHighlightPosition {
    TOP_LEFT,
    TOP_RIGHT,
    CENTER,
    BOTTOM_LEFT,
    BOTTOM_RIGHT
}

@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,

    style: LiquidGlassStyle,

    cornerRadius: Dp = 24.dp,

    elevation: Dp = 8.dp,

    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                clip = false
            )
            .clip(shape)
            .background(Color.Transparent)
    ) {

        /*
         * All of the visual glass material is drawn underneath
         * the actual card content.
         */
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {

            val width = size.width
            val height = size.height

            if (width <= 0f || height <= 0f) return@Canvas

            /*
             * ---------------------------------------------------------
             * 1. BASE GLASS GRADIENT
             * ---------------------------------------------------------
             */

            val baseColors = when {
                style.colors.size >= 3 -> style.colors

                style.colors.size == 2 -> listOf(
                    style.colors[0],
                    style.colors[1],
                    style.colors[1].copy(alpha = 0.85f)
                )

                else -> listOf(
                    Color(0xFF176B92),
                    Color(0xFF123C68),
                    Color(0xFF101E42)
                )
            }

            drawRect(
                brush = Brush.linearGradient(
                    colors = baseColors,
                    start = Offset(0f, 0f),
                    end = Offset(width, height)
                ),
                alpha = style.glassAlpha
            )

            /*
             * ---------------------------------------------------------
             * 2. SOFT COLORED ATMOSPHERIC GLOW
             * ---------------------------------------------------------
             */

            val glowCenter = when (style.highlightPosition) {

                LiquidHighlightPosition.TOP_LEFT ->
                    Offset(width * 0.12f, height * 0.10f)

                LiquidHighlightPosition.TOP_RIGHT ->
                    Offset(width * 0.88f, height * 0.10f)

                LiquidHighlightPosition.CENTER ->
                    Offset(width * 0.50f, height * 0.50f)

                LiquidHighlightPosition.BOTTOM_LEFT ->
                    Offset(width * 0.12f, height * 0.90f)

                LiquidHighlightPosition.BOTTOM_RIGHT ->
                    Offset(width * 0.88f, height * 0.90f)
            }

            val glowColor = baseColors.firstOrNull()
                ?: Color.White

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        glowColor.copy(
                            alpha = style.glowAlpha
                        ),
                        glowColor.copy(
                            alpha = style.glowAlpha * 0.35f
                        ),
                        Color.Transparent
                    ),
                    center = glowCenter,
                    radius = max(width, height) * 0.75f
                ),
                radius = max(width, height) * 0.75f,
                center = glowCenter
            )

            /*
             * ---------------------------------------------------------
             * 3. MAIN LIQUID WAVE
             * ---------------------------------------------------------
             */

            val wave = createWavePath(
                width = width,
                height = height,
                style = style.waveStyle
            )

            drawPath(
                path = wave,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(
                            alpha = style.waveAlpha * 0.15f
                        ),
                        Color.White.copy(
                            alpha = style.waveAlpha
                        ),
                        Color.White.copy(
                            alpha = style.waveAlpha * 0.30f
                        ),
                        Color.Transparent
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(width, height)
                )
            )

            /*
             * ---------------------------------------------------------
             * 4. SECONDARY SOFT WAVE
             *
             * This gives the card the layered "liquid" appearance.
             * ---------------------------------------------------------
             */

            val secondaryWave = createSecondaryWave(
                width = width,
                height = height,
                style = style.waveStyle
            )

            drawPath(
                path = secondaryWave,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.0f),
                        Color.White.copy(
                            alpha = style.waveAlpha * 0.45f
                        ),
                        Color.White.copy(
                            alpha = style.waveAlpha * 0.15f
                        ),
                        Color.Transparent
                    ),
                    start = Offset(width, 0f),
                    end = Offset(0f, height)
                )
            )

            /*
             * ---------------------------------------------------------
             * 5. SPECULAR GLASS REFLECTION
             *
             * The large diagonal translucent reflection is one of
             * the things that makes the generated card look like glass.
             * ---------------------------------------------------------
             */

            val reflection = Path().apply {

                moveTo(
                    width * 0.52f,
                    0f
                )

                cubicTo(
                    width * 0.70f,
                    height * 0.05f,
                    width * 0.78f,
                    height * 0.18f,
                    width * 1.08f,
                    height * 0.08f
                )

                lineTo(
                    width * 1.08f,
                    height * 0.30f
                )

                cubicTo(
                    width * 0.82f,
                    height * 0.25f,
                    width * 0.70f,
                    height * 0.16f,
                    width * 0.52f,
                    0f
                )

                close()
            }

            drawPath(
                path = reflection,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(
                            alpha = style.highlightAlpha
                        ),
                        Color.White.copy(
                            alpha = style.highlightAlpha * 0.20f
                        ),
                        Color.Transparent
                    ),
                    start = Offset(width * 0.35f, 0f),
                    end = Offset(width, height * 0.25f)
                )
            )

            /*
             * ---------------------------------------------------------
             * 6. BOTTOM INNER LIGHT
             * ---------------------------------------------------------
             */

            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.08f),
                        Color.Black.copy(alpha = 0.18f)
                    ),
                    startY = height * 0.55f,
                    endY = height
                )
            )

            /*
             * ---------------------------------------------------------
             * 7. INNER GLASS BORDER
             * ---------------------------------------------------------
             */

            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(
                            alpha = style.borderAlpha * 1.35f
                        ),
                        Color.White.copy(
                            alpha = style.borderAlpha * 0.30f
                        ),
                        Color.White.copy(
                            alpha = style.borderAlpha * 0.75f
                        )
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(width, height)
                ),
                topLeft = Offset.Zero,
                size = Size(width, height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                    cornerRadius.toPx()
                ),
                style = Stroke(
                    width = 1.2.dp.toPx()
                )
            )

            /*
             * ---------------------------------------------------------
             * 8. VERY SUBTLE INNER HIGHLIGHT
             * ---------------------------------------------------------
             */

            drawRoundRect(
                color = Color.White.copy(alpha = 0.04f),
                topLeft = Offset(
                    1.dp.toPx(),
                    1.dp.toPx()
                ),
                size = Size(
                    width - 2.dp.toPx(),
                    height - 2.dp.toPx()
                ),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                    max(
                        0f,
                        cornerRadius.toPx() - 1.dp.toPx()
                    )
                ),
                style = Stroke(
                    width = 0.7.dp.toPx()
                )
            )
        }

        /*
         * -------------------------------------------------------------
         * CARD CONTENT
         * -------------------------------------------------------------
         */

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}


/**
 * Creates the primary flowing liquid shape.
 */
private fun createWavePath(
    width: Float,
    height: Float,
    style: LiquidWaveStyle
): Path {

    return Path().apply {

        when (style) {

            LiquidWaveStyle.DIAGONAL -> {

                moveTo(
                    -width * 0.05f,
                    height * 0.72f
                )

                cubicTo(
                    width * 0.22f,
                    height * 0.52f,
                    width * 0.52f,
                    height * 0.64f,
                    width * 1.05f,
                    height * 0.30f
                )

                lineTo(
                    width * 1.05f,
                    height * 1.05f
                )

                lineTo(
                    -width * 0.05f,
                    height * 1.05f
                )

                close()
            }

            LiquidWaveStyle.SWEEP -> {

                moveTo(
                    -width * 0.05f,
                    height * 0.62f
                )

                cubicTo(
                    width * 0.20f,
                    height * 0.35f,
                    width * 0.50f,
                    height * 0.42f,
                    width * 1.05f,
                    height * 0.12f
                )

                lineTo(
                    width * 1.05f,
                    height * 1.05f
                )

                lineTo(
                    -width * 0.05f,
                    height * 1.05f
                )

                close()
            }

            LiquidWaveStyle.S_CURVE -> {

                moveTo(
                    -width * 0.05f,
                    height * 0.38f
                )

                cubicTo(
                    width * 0.22f,
                    height * 0.75f,
                    width * 0.45f,
                    height * 0.05f,
                    width * 1.05f,
                    height * 0.48f
                )

                lineTo(
                    width * 1.05f,
                    height * 1.05f
                )

                lineTo(
                    -width * 0.05f,
                    height * 1.05f
                )

                close()
            }

            LiquidWaveStyle.RISING -> {

                moveTo(
                    -width * 0.05f,
                    height * 0.82f
                )

                cubicTo(
                    width * 0.30f,
                    height * 0.68f,
                    width * 0.60f,
                    height * 0.35f,
                    width * 1.05f,
                    height * 0.20f
                )

                lineTo(
                    width * 1.05f,
                    height * 1.05f
                )

                lineTo(
                    -width * 0.05f,
                    height * 1.05f
                )

                close()
            }

            LiquidWaveStyle.FALLING -> {

                moveTo(
                    -width * 0.05f,
                    height * 0.18f
                )

                cubicTo(
                    width * 0.30f,
                    height * 0.38f,
                    width * 0.70f,
                    height * 0.58f,
                    width * 1.05f,
                    height * 0.72f
                )

                lineTo(
                    width * 1.05f,
                    height * 1.05f
                )

                lineTo(
                    -width * 0.05f,
                    height * 1.05f
                )

                close()
            }

            LiquidWaveStyle.DOUBLE_WAVE -> {

                moveTo(
                    -width * 0.05f,
                    height * 0.65f
                )

                cubicTo(
                    width * 0.18f,
                    height * 0.38f,
                    width * 0.35f,
                    height * 0.82f,
                    width * 0.55f,
                    height * 0.52f
                )

                cubicTo(
                    width * 0.72f,
                    height * 0.25f,
                    width * 0.86f,
                    height * 0.40f,
                    width * 1.05f,
                    height * 0.18f
                )

                lineTo(
                    width * 1.05f,
                    height * 1.05f
                )

                lineTo(
                    -width * 0.05f,
                    height * 1.05f
                )

                close()
            }
        }
    }
}


/**
 * Secondary wave with a different geometry.
 */
private fun createSecondaryWave(
    width: Float,
    height: Float,
    style: LiquidWaveStyle
): Path {

    return Path().apply {

        when (style) {

            LiquidWaveStyle.S_CURVE,
            LiquidWaveStyle.DOUBLE_WAVE -> {

                moveTo(
                    -width * 0.05f,
                    height * 0.25f
                )

                cubicTo(
                    width * 0.28f,
                    height * 0.48f,
                    width * 0.55f,
                    height * 0.05f,
                    width * 1.05f,
                    height * 0.32f
                )

                lineTo(
                    width * 1.05f,
                    height * 0.52f
                )

                cubicTo(
                    width * 0.58f,
                    height * 0.25f,
                    width * 0.30f,
                    height * 0.62f,
                    -width * 0.05f,
                    height * 0.40f
                )

                close()
            }

            else -> {

                moveTo(
                    -width * 0.05f,
                    height * 0.78f
                )

                cubicTo(
                    width * 0.30f,
                    height * 0.58f,
                    width * 0.62f,
                    height * 0.80f,
                    width * 1.05f,
                    height * 0.48f
                )

                lineTo(
                    width * 1.05f,
                    height * 0.66f
                )

                cubicTo(
                    width * 0.60f,
                    height * 0.92f,
                    width * 0.30f,
                    height * 0.70f,
                    -width * 0.05f,
                    height * 0.92f
                )

                close()
            }
        }
    }
}