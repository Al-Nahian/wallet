package com.example.wallet.core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.max
import com.example.wallet.core.design.glass.GlassShapes

/** The app's general-purpose "liquid glass" card container — headline dashboard cards (account
 * tile, balance card, stat card) and every other tinted/neutral card in the app (transaction rows,
 * notification rows, account list cards, sign-in buttons).
 *
 * Dark mode renders the reference design's full multi-layer glass material — base diagonal
 * gradient, an atmospheric colored glow, two overlapping flowing "wave" highlight bands, a broad
 * diagonal specular reflection streak, a bottom inner shadow, and a layered inner border — ported
 * from a reference `LiquidGlassCard`/wave-path implementation the reference visuals were built
 * from. Light mode keeps its own separate, already-tuned pastel treatment (lighter tint, softer
 * single wave), since the two reference images this component targets are for different themes
 * and were tuned independently.
 *
 * All static `drawWithCache` draws — no real backdrop capture/blur/shader per card, so this stays
 * cheap even for the transaction list's many rows (see `lightweight`). */
@Composable
fun LiquidGlassCard(
    tint: Color,
    modifier: Modifier = Modifier,
    shape: Shape = GlassShapes.medium,
    /** The same corner rounding as [shape], as a raw value — needed because the dark-mode glass
     * layers are drawn with [androidx.compose.ui.graphics.drawscope.DrawScope.drawRoundRect] calls
     * that take an explicit [CornerRadius] rather than a [Shape]. Defaults to
     * [com.example.wallet.core.design.glass.GlassTokens.cornerMedium] to match [shape]'s own
     * default; callers passing a non-default `shape` (small/large/pill) should pass the matching
     * radius too — a large value (bigger than half the card's height) is fine for a pill shape,
     * since draw calls clamp it themselves. */
    cornerRadius: Dp = 20.dp,
    lightweight: Boolean = false,
    /** Which of the [WaveVariantCount] wave shapes to draw. Defaults to a hash of [tint], but a
     * hash over only ~7 fixed dashboard colors collides often enough that visually adjacent cards
     * (e.g. the two "Cash" account tiles) could get the identical wave — callers that know their
     * own position (a list index, a fixed call site) should pass an explicit value instead. */
    waveVariant: Int = defaultWaveVariant(tint),
    content: @Composable BoxScope.() -> Unit,
) {
    if (isSystemInDarkTheme()) {
        LiquidGlassCardDark(
            tint = tint,
            modifier = modifier,
            shape = shape,
            cornerRadius = cornerRadius,
            lightweight = lightweight,
            waveVariant = waveVariant,
            content = content,
        )
    } else {
        LiquidGlassCardLight(
            tint = tint,
            modifier = modifier,
            shape = shape,
            lightweight = lightweight,
            waveVariant = waveVariant,
            content = content,
        )
    }
}

@Composable
private fun LiquidGlassCardLight(
    tint: Color,
    modifier: Modifier,
    shape: Shape,
    lightweight: Boolean,
    waveVariant: Int,
    content: @Composable BoxScope.() -> Unit,
) {
    val elevation = if (lightweight) 0.dp else 14.dp
    val fillStart = lerp(tint, Color.White, 0.85f)
    val fillEnd = lerp(tint, Color.White, 0.62f)
    val glowNear = tint.copy(alpha = 0.16f)
    val glowFar = lerp(tint, Color.White, 0.4f).copy(alpha = 0.16f)
    Box(
        modifier = modifier
            .let { base ->
                // Modifier.shadow isn't a no-op at elevation = 0 — it's backed by its own
                // graphicsLayer regardless, which left a faint seam at the shape's edge when
                // chained straight into clip() with nothing between them (same issue GlassSurface
                // hit). Omitting the modifier entirely for lightweight rows avoids both that seam
                // and the unnecessary layer.
                if (elevation == 0.dp) {
                    base
                } else {
                    base.shadow(
                        elevation = elevation,
                        shape = shape,
                        clip = false,
                        ambientColor = tint.copy(alpha = 0.45f),
                        spotColor = tint.copy(alpha = 0.45f),
                    )
                }
            }
            .clip(shape)
            .background(Brush.linearGradient(listOf(fillStart, fillEnd)))
            .drawWithCache {
                val w = size.width
                val h = size.height
                val nearGlow = Brush.radialGradient(
                    colors = listOf(glowNear, Color.Transparent),
                    center = Offset(w * 0.12f, h * 0.1f),
                    radius = w * 0.7f,
                )
                val farGlow = Brush.radialGradient(
                    colors = listOf(glowFar, Color.Transparent),
                    center = Offset(w * 0.92f, h * 0.95f),
                    radius = w * 0.75f,
                )
                val wave = buildWaveVariant(waveVariant, w, h)
                val waveBrush = Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.35f), Color.Transparent),
                )
                onDrawBehind {
                    drawRect(nearGlow)
                    drawRect(farGlow)
                    drawPath(path = wave, brush = waveBrush)
                }
            }
            // The reference design's card border is a thin, uniform light stroke all the way
            // around — equally visible on every edge, not brighter on one corner than another
            // (a diagonal gradient version looked like a one-sided highlight instead of an
            // actual edge, and a too-faint flat one didn't read as a border at all).
            .border(width = 1.2.dp, color = Color.White.copy(alpha = 0.75f), shape = shape),
        content = content,
    )
}

/** Dark mode's rich multi-layer glass material — see the file doc for the layer list. */
@Composable
private fun LiquidGlassCardDark(
    tint: Color,
    modifier: Modifier,
    shape: Shape,
    cornerRadius: Dp,
    lightweight: Boolean,
    waveVariant: Int,
    content: @Composable BoxScope.() -> Unit,
) {
    val elevation = if (lightweight) 0.dp else 14.dp
    val shadowAlpha = 0.55f
    // Bright → mid → dark, matching the reference's own hand-picked 3-stop palettes (e.g. its
    // purple example: 0xFF8050D8 → 0xFF5B3293 → 0xFF38235F, each successive stop ~30%/55% darker
    // than the first, never brightened past the tint itself). Pre-brightening the bright stop
    // toward white (an earlier version of this) washed out already-desaturated neutral tints
    // (Cash Flow, transaction rows) into a flat gray — the glossy "pop" instead comes from the
    // reflection/wave layers drawn on top, not from lightening the base fill.
    val colorBright = tint
    val colorMid = lerp(tint, Color.Black, 0.32f)
    val colorDark = lerp(tint, Color.Black, 0.56f)
    val glowColor = lerp(tint, Color.White, 0.3f)
    Box(
        modifier = modifier
            .let { base ->
                if (elevation == 0.dp) {
                    base
                } else {
                    base.shadow(
                        elevation = elevation,
                        shape = shape,
                        clip = false,
                        ambientColor = tint.copy(alpha = shadowAlpha),
                        spotColor = tint.copy(alpha = shadowAlpha),
                    )
                }
            }
            .clip(shape)
            .drawWithCache {
                val w = size.width
                val h = size.height
                val cornerPx = cornerRadius.toPx()
                val corner = CornerRadius(cornerPx)

                val baseGradient = Brush.linearGradient(
                    colors = listOf(colorBright, colorMid, colorDark),
                    start = Offset(0f, 0f),
                    end = Offset(w, h),
                )

                val glowCenter = Offset(w * 0.88f, h * 0.10f)
                val glowRadius = max(w, h) * 0.75f
                val glow = Brush.radialGradient(
                    colors = listOf(
                        glowColor.copy(alpha = 0.20f),
                        glowColor.copy(alpha = 0.07f),
                        Color.Transparent,
                    ),
                    center = glowCenter,
                    radius = glowRadius,
                )

                val wave = buildLiquidWave(waveVariant, w, h)
                val waveBrush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.021f),
                        Color.White.copy(alpha = 0.14f),
                        Color.White.copy(alpha = 0.042f),
                        Color.Transparent,
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(w, h),
                )

                val secondaryWave = buildLiquidSecondaryWave(waveVariant, w, h)
                val secondaryWaveBrush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.063f),
                        Color.White.copy(alpha = 0.021f),
                        Color.Transparent,
                    ),
                    start = Offset(w, 0f),
                    end = Offset(0f, h),
                )

                // The broad diagonal specular streak — this is what reads as "glossy glass" in
                // the reference rather than a flat tinted panel.
                val reflection = Path().apply {
                    moveTo(w * 0.52f, 0f)
                    cubicTo(w * 0.70f, h * 0.05f, w * 0.78f, h * 0.18f, w * 1.08f, h * 0.08f)
                    lineTo(w * 1.08f, h * 0.30f)
                    cubicTo(w * 0.82f, h * 0.25f, w * 0.70f, h * 0.16f, w * 0.52f, 0f)
                    close()
                }
                val reflectionBrush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.12f),
                        Color.White.copy(alpha = 0.024f),
                        Color.Transparent,
                    ),
                    start = Offset(w * 0.35f, 0f),
                    end = Offset(w, h * 0.25f),
                )

                val bottomShadowBrush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.08f), Color.Black.copy(alpha = 0.18f)),
                    startY = h * 0.55f,
                    endY = h,
                )

                val borderBrush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.43f),
                        Color.White.copy(alpha = 0.096f),
                        Color.White.copy(alpha = 0.24f),
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(w, h),
                )

                onDrawBehind {
                    drawRect(baseGradient, alpha = 0.92f)
                    drawCircle(brush = glow, radius = glowRadius, center = glowCenter)
                    drawPath(path = wave, brush = waveBrush)
                    drawPath(path = secondaryWave, brush = secondaryWaveBrush)
                    drawPath(path = reflection, brush = reflectionBrush)
                    drawRect(brush = bottomShadowBrush)
                    drawRoundRect(
                        brush = borderBrush,
                        topLeft = Offset.Zero,
                        size = Size(w, h),
                        cornerRadius = corner,
                        style = Stroke(width = 1.2.dp.toPx()),
                    )
                }
            },
        content = content,
    )
}

private const val WaveVariantCount = 6

/** Hash-derived fallback for callers that don't know/care which wave they'll get — see
 * [LiquidGlassCard]'s `waveVariant` doc for why a caller that DOES know its position (a list
 * index, a fixed call site) should pass an explicit value instead. */
fun defaultWaveVariant(tint: Color): Int = abs(tint.toArgb()) % WaveVariantCount

/** Five distinct curved-band shapes so cards don't all show the same wave. Each is a closed
 * ribbon (two cubic edges) at a different vertical position/orientation — upper sweep, lower
 * sweep, a rising diagonal, a mid-card bulge, or two thin bands — matching how the reference
 * design varies the wave per card. Used by the light-mode card only; dark mode uses
 * [buildLiquidWave]/[buildLiquidSecondaryWave] instead. */
private fun buildWaveVariant(variant: Int, w: Float, h: Float): Path = when (variant % 5) {
    0 -> Path().apply {
        moveTo(0f, h * 0.22f)
        cubicTo(w * 0.25f, h * 0.02f, w * 0.55f, h * 0.04f, w, h * 0.28f)
        lineTo(w, h * 0.42f)
        cubicTo(w * 0.65f, h * 0.2f, w * 0.3f, h * 0.3f, 0f, h * 0.46f)
        close()
    }
    1 -> Path().apply {
        moveTo(0f, h * 0.9f)
        cubicTo(w * 0.3f, h * 1.05f, w * 0.6f, h * 1.02f, w, h * 0.78f)
        lineTo(w, h * 0.64f)
        cubicTo(w * 0.62f, h * 0.86f, w * 0.32f, h * 0.9f, 0f, h * 0.72f)
        close()
    }
    2 -> Path().apply {
        moveTo(0f, h * 0.95f)
        cubicTo(w * 0.35f, h * 0.55f, w * 0.55f, h * 0.35f, w, h * -0.1f)
        lineTo(w, h * 0.08f)
        cubicTo(w * 0.58f, h * 0.5f, w * 0.4f, h * 0.7f, 0f, h * 1.1f)
        close()
    }
    3 -> Path().apply {
        moveTo(0f, h * 0.42f)
        cubicTo(w * 0.3f, h * 0.2f, w * 0.6f, h * 0.62f, w, h * 0.4f)
        lineTo(w, h * 0.56f)
        cubicTo(w * 0.6f, h * 0.78f, w * 0.3f, h * 0.36f, 0f, h * 0.58f)
        close()
    }
    else -> Path().apply {
        moveTo(0f, h * 0.1f)
        cubicTo(w * 0.4f, h * -0.05f, w * 0.5f, h * 0.05f, w, h * 0.02f)
        lineTo(w, h * 0.16f)
        cubicTo(w * 0.5f, h * 0.19f, w * 0.4f, h * 0.09f, 0f, h * 0.24f)
        close()
        moveTo(0f, h * 0.68f)
        cubicTo(w * 0.45f, h * 0.85f, w * 0.55f, h * 0.78f, w, h * 0.9f)
        lineTo(w, h * 1.02f)
        cubicTo(w * 0.55f, h * 0.92f, w * 0.45f, h * 0.98f, 0f, h * 0.82f)
        close()
    }
}

/** Dark mode's primary flowing wave shape — 6 variants (DIAGONAL, SWEEP, S_CURVE, RISING, FALLING,
 * DOUBLE_WAVE from the reference implementation), selected by [variant] mod 6. */
private fun buildLiquidWave(variant: Int, w: Float, h: Float): Path = when (variant % 6) {
    0 -> Path().apply { // DIAGONAL
        moveTo(-w * 0.05f, h * 0.72f)
        cubicTo(w * 0.22f, h * 0.52f, w * 0.52f, h * 0.64f, w * 1.05f, h * 0.30f)
        lineTo(w * 1.05f, h * 1.05f)
        lineTo(-w * 0.05f, h * 1.05f)
        close()
    }
    1 -> Path().apply { // SWEEP
        moveTo(-w * 0.05f, h * 0.62f)
        cubicTo(w * 0.20f, h * 0.35f, w * 0.50f, h * 0.42f, w * 1.05f, h * 0.12f)
        lineTo(w * 1.05f, h * 1.05f)
        lineTo(-w * 0.05f, h * 1.05f)
        close()
    }
    2 -> Path().apply { // S_CURVE
        moveTo(-w * 0.05f, h * 0.38f)
        cubicTo(w * 0.22f, h * 0.75f, w * 0.45f, h * 0.05f, w * 1.05f, h * 0.48f)
        lineTo(w * 1.05f, h * 1.05f)
        lineTo(-w * 0.05f, h * 1.05f)
        close()
    }
    3 -> Path().apply { // RISING
        moveTo(-w * 0.05f, h * 0.82f)
        cubicTo(w * 0.30f, h * 0.68f, w * 0.60f, h * 0.35f, w * 1.05f, h * 0.20f)
        lineTo(w * 1.05f, h * 1.05f)
        lineTo(-w * 0.05f, h * 1.05f)
        close()
    }
    4 -> Path().apply { // FALLING
        moveTo(-w * 0.05f, h * 0.18f)
        cubicTo(w * 0.30f, h * 0.38f, w * 0.70f, h * 0.58f, w * 1.05f, h * 0.72f)
        lineTo(w * 1.05f, h * 1.05f)
        lineTo(-w * 0.05f, h * 1.05f)
        close()
    }
    else -> Path().apply { // DOUBLE_WAVE
        moveTo(-w * 0.05f, h * 0.65f)
        cubicTo(w * 0.18f, h * 0.38f, w * 0.35f, h * 0.82f, w * 0.55f, h * 0.52f)
        cubicTo(w * 0.72f, h * 0.25f, w * 0.86f, h * 0.40f, w * 1.05f, h * 0.18f)
        lineTo(w * 1.05f, h * 1.05f)
        lineTo(-w * 0.05f, h * 1.05f)
        close()
    }
}

/** Dark mode's secondary, thinner wave layered under [buildLiquidWave] for a "layered liquid"
 * look — S_CURVE/DOUBLE_WAVE get one geometry, every other primary style gets another. */
private fun buildLiquidSecondaryWave(variant: Int, w: Float, h: Float): Path =
    if (variant % 6 == 2 || variant % 6 == 5) {
        Path().apply {
            moveTo(-w * 0.05f, h * 0.25f)
            cubicTo(w * 0.28f, h * 0.48f, w * 0.55f, h * 0.05f, w * 1.05f, h * 0.32f)
            lineTo(w * 1.05f, h * 0.52f)
            cubicTo(w * 0.58f, h * 0.25f, w * 0.30f, h * 0.62f, -w * 0.05f, h * 0.40f)
            close()
        }
    } else {
        Path().apply {
            moveTo(-w * 0.05f, h * 0.78f)
            cubicTo(w * 0.30f, h * 0.58f, w * 0.62f, h * 0.80f, w * 1.05f, h * 0.48f)
            lineTo(w * 1.05f, h * 0.66f)
            cubicTo(w * 0.60f, h * 0.92f, w * 0.30f, h * 0.70f, -w * 0.05f, h * 0.92f)
            close()
        }
    }
