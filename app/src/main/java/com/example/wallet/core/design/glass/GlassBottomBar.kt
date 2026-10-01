package com.example.wallet.core.design.glass

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.highlight.HighlightStyle

/** The bar's material: the reference slate pill in dark mode. Light mode draws its own
 * layered liquid-glass surface over the blur instead (see [drawLightLiquidGlass]); this
 * value only carries its no-blur fallback. Devices without blur (API < 31) get a more opaque
 * fill, since there the translucency alone has to carry the effect. */
private val LightBarColor = Color(0xFFEFEFF3)
private val DarkBarColor = Color(0xFF232E3F)

/** Dark's tint kept low over the blur: it's there to soften and unify what shows through, not
 * to hide it. Too much and the frosted look flattens into a plain panel. */
private const val BlurredBarOpacity = 0.55f

/** Without blur to diffuse the content there's nothing to soften it, so the tint has to carry
 * legibility on its own and runs much heavier. */
private const val FlatBarOpacity = 0.92f

/**
 * liquid_glass_implementation_plan.md §16 — glass backdrop for the bottom navigation bar,
 * rendered as a fully-rounded floating pill (inset from the screen edges by its caller) rather
 * than a bar flush against the bottom edge, per the liquid-glass reference design. See
 * `BottomNavigation.kt` for the nav row/FAB content this wraps.
 *
 * [liquidBackdrop] is the *same* [LayerBackdrop] capture the center FAB reads (see
 * `WalletBottomNavigation`) rather than a separate one of
 * its own — an earlier version captured the whole screen a second time just for this bar,
 * which `dumpsys gfxinfo` showed was a real, measurable chunk of the jank during scroll (every
 * scroll frame re-recorded the entire visible content tree twice, not once). Sharing the
 * capture halves that cost with no visual difference, since both consumers were always
 * blurring/refracting the exact same content anyway.
 *
 * Dark mode is the reference design's slate pill. Light mode is a liquid-glass treatment
 * (liquidglass-reference.kt): real backdrop blur under a near-white milky fill, with the
 * reference's specular sweep, bottom shade and inner border layered on top.
 *
 * Deliberately casts no shadow: the fill is translucent, so a drop shadow shows *through* the
 * bar and muddies it (and Android's shadow leaves an uneven band where it meets the outline).
 */
@Composable
fun GlassBottomBar(
    modifier: Modifier = Modifier,
    liquidBackdrop: LayerBackdrop? = null,
    shape: Shape = GlassShapes.large,
    content: @Composable BoxScope.() -> Unit,
) {
    val isDark = isSystemInDarkTheme()
    val hasBlur = liquidBackdrop != null && GlassCapabilities.supportsAdvancedBlur()
    val opacity = if (hasBlur) BlurredBarOpacity else FlatBarOpacity
    if (hasBlur) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .drawBackdrop(
                    backdrop = liquidBackdrop!!,
                    shape = { shape },
                    // Dark: a 28dp blur diffuses whatever's behind the pill into soft,
                    // barely-recognizable color (the reference's frosted pill). Light adds
                    // vibrancy so blurred pastel cards still read as color through the fill
                    // instead of washing to grey. No lens() here (unlike the FAB/blob circles):
                    // it throws UnsupportedOperationException on any shape that isn't a
                    // CornerBasedShape, and this bar's shape is [NotchedBottomBarShape] — a
                    // plain Path-based Shape for the FAB's notch — not a rounded rect.
                    effects = {
                        if (isDark) {
                            blur(28.dp.toPx())
                        } else {
                            vibrancy()
                            blur(28.dp.toPx())
                        }
                    },
                    // A faint rim so the pill reads as a distinct floating surface instead of
                    // blending into the background. Light's is a touch stronger — on a white
                    // page the edge is the main thing seating the near-white fill.
                    highlight = if (isDark) {
                        { Highlight(width = 1.dp, alpha = 0.35f, style = HighlightStyle.Default(intensity = 0.5f)) }
                    } else {
                        { Highlight(width = 1.5.dp, alpha = 0.6f, style = HighlightStyle.Default(intensity = 0.75f)) }
                    },
                    shadow = null,
                    onDrawSurface = {
                        if (isDark) {
                            drawRect(DarkBarColor.copy(alpha = opacity))
                        } else {
                            drawLightLiquidGlass()
                        }
                    },
                ),
            content = content,
        )
    } else {
        GlassSurface(
            modifier = modifier.fillMaxWidth(),
            style = GlassStyle.Regular,
            shape = shape,
            fill = (if (isDark) DarkBarColor else LightBarColor).copy(alpha = opacity),
            elevation = 0.dp,
            content = content,
        )
    }
}

/**
 * Light mode's liquid-glass material (liquidglass-reference.kt), drawn over the real blurred
 * backdrop inside the pill's shape clip:
 *
 * 1. a milky near-white base gradient — deliberately near-opaque (§1 `glassAlpha` runs 0.92 in
 *    the reference too): a sheer veil lets dark labels punch straight through and reads as
 *    clear plastic, not frosted glass;
 * 2. the diagonal specular sweep across the top-right (§5) that sells the material as glass
 *    rather than a white panel;
 * 3. the faint bottom inner shade (§6) that gives the pill weight;
 * 4. the white gradient inner border hairline (§7).
 */
private fun DrawScope.drawLightLiquidGlass() {
    val width = size.width
    val height = size.height

    // 1. Base glass gradient — near-white, cooling slightly to blue-grey at the bottom edge.
    drawRect(
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.88f),
                Color(0xFFF4F8FD).copy(alpha = 0.84f),
                Color(0xFFE4EDF8).copy(alpha = 0.87f),
            ),
            start = Offset.Zero,
            end = Offset(width, height),
        ),
    )

    // 2. Specular reflection — the reference's diagonal white sweep, offset toward the
    // top-right highlight position.
    val sweep = Path().apply {
        moveTo(width * 0.52f, 0f)
        cubicTo(
            width * 0.70f, height * 0.05f,
            width * 0.78f, height * 0.18f,
            width * 1.08f, height * 0.08f,
        )
        lineTo(width * 1.08f, height * 0.30f)
        cubicTo(
            width * 0.82f, height * 0.25f,
            width * 0.70f, height * 0.16f,
            width * 0.52f, 0f,
        )
        close()
    }
    drawPath(
        path = sweep,
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.5f),
                Color.White.copy(alpha = 0.12f),
                Color.Transparent,
            ),
            start = Offset(width * 0.35f, 0f),
            end = Offset(width, height * 0.25f),
        ),
    )

    // 3. Bottom inner light — transparent to a faint dark shade over the lower half.
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.Transparent,
                Color.Black.copy(alpha = 0.05f),
                Color.Black.copy(alpha = 0.12f),
            ),
            startY = height * 0.55f,
            endY = height,
        ),
    )

    // 4. Inner glass border — a white gradient hairline (1.2dp per the reference), inset by
    // half its stroke so the pill's shape clip doesn't eat it; the corner radius matches
    // GlassShapes.large. The clip keeps the gradient fill square edges rounded.
    val stroke = 1.2.dp.toPx()
    val inset = stroke / 2f
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.7f),
                Color.White.copy(alpha = 0.15f),
                Color.White.copy(alpha = 0.45f),
            ),
            start = Offset.Zero,
            end = Offset(width, height),
        ),
        topLeft = Offset(inset, inset),
        size = Size(width - inset * 2f, height - inset * 2f),
        cornerRadius = CornerRadius(GlassTokens.cornerLarge.toPx() - inset),
        style = Stroke(width = stroke),
    )
}
