package com.expensetracker.wallet.core.design.components

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.expensetracker.wallet.core.design.glass.GlassCircleIconButton
import com.expensetracker.wallet.core.design.glass.GlassStyle
import com.expensetracker.wallet.core.design.glass.GlassSurface
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.highlight.HighlightStyle
import com.kyant.backdrop.shadow.Shadow

/** A small circular glass badge for a card's leading icon — every headline dashboard/report card
 * (account tile, balance card, stat card, cash flow card) frames its icon this way instead of a
 * bare [Icon], matching the liquid-glass reference's "glossy bubble" icon treatment.
 *
 * The bubble derives from the card's own [tint] rather than a theme glass blend: dark mode
 * deepens it toward black so the well stays dark (and the glyph legible) on vivid fills, while
 * light mode keeps it bright — a darkened well read as a muddy blob on the pastel cards, where
 * the reference carries bright coral/blue/purple beads with white glyphs. */
@Composable
fun GlassIconBubble(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    tint: Color? = null,
    size: Dp = 32.dp,
    iconTint: Color = Color.White,
) {
    val isDark = isSystemInDarkTheme()
    val bubbleFill = if (isDark) {
        lerp(tint ?: Color.Black, Color.Black, 0.45f).copy(alpha = 0.85f)
    } else {
        (tint?.let { lerp(it, Color.Black, 0.12f) } ?: Color.Gray).copy(alpha = 0.95f)
    }
    GlassSurface(
        modifier = modifier.size(size),
        style = GlassStyle.Thick,
        shape = CircleShape,
        fill = bubbleFill,
        elevation = 2.dp,
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(size * 0.5f))
        }
    }
}

/** [GlassIconBubble] with real liquid-glass refraction (io.github.kyant0:backdrop) instead of a
 * flat translucent fill — the bubble genuinely bends a decorative gradient behind it, not just
 * a static color.
 *
 * PROTOTYPE, deliberately scoped to single-card call sites (not list rows): the backdrop it reads
 * is a small gradient [Box] positioned as its own *sibling* right here, not an ancestor containing
 * it — capturing a scrolling list's content and reading it from an icon nested inside that same
 * list would make the icon try to record itself mid-draw, which crashes the render thread natively
 * (confirmed the hard way on an earlier attempt). This sidesteps that entirely by giving the
 * bubble its own tiny, self-contained thing to refract, independent of whatever screen it's on.
 * Falls back to plain [GlassIconBubble] below API 31. */
@Composable
fun LiquidIconBubble(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    tint: Color? = null,
    size: Dp = 32.dp,
    iconTint: Color = Color.White,
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        GlassIconBubble(icon = icon, modifier = modifier, tint = tint, size = size, iconTint = iconTint)
        return
    }
    val backdrop = rememberLayerBackdrop()
    val bubbleTint = tint ?: Color.Black
    val isDark = isSystemInDarkTheme()
    val bubbleShadow: (() -> Shadow)? =
        if (isDark) null else ({ Shadow(radius = 5.dp, color = bubbleTint.copy(alpha = 0.5f)) })
    Box(modifier = modifier.size(size)) {
        // The decorative source: a small radial gradient, captured but never itself reading the
        // capture — a sibling of the surface below, not its ancestor. Dark mode keeps the deep,
        // near-black-edged glossy sphere (confirmed working against its vivid card fills). Light
        // mode's cards are now a pastel wave texture, and that same near-black edge read as a
        // dark, mismatched blob against them — its edge stays within the tint's own color instead
        // of crossing into black, so it reads as a lighter, brighter bubble.
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(CircleShape)
                .layerBackdrop(backdrop)
                .background(
                    Brush.radialGradient(
                        if (isDark) {
                            listOf(
                                lerp(bubbleTint, Color.White, 0.55f),
                                bubbleTint,
                                lerp(bubbleTint, Color.Black, 0.5f),
                            )
                        } else {
                            listOf(
                                lerp(bubbleTint, Color.White, 0.75f),
                                lerp(bubbleTint, Color.White, 0.35f),
                                lerp(bubbleTint, Color.White, 0.05f),
                            )
                        },
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { CircleShape },
                    effects = {
                        vibrancy()
                        blur(1.dp.toPx())
                        lens(size.value / 4f, size.value / 2f, depthEffect = true)
                    },
                    highlight = { Highlight(width = 1.dp, alpha = 0.9f, style = HighlightStyle.Default(intensity = 0.85f)) },
                    // Reference design's pastel light-mode cards give each icon bubble its own
                    // small drop shadow, distinguishing it from the flat card behind it — dark
                    // mode's vivid card fill already gives it enough separation without one.
                    shadow = bubbleShadow,
                    onDrawSurface = {
                        drawRect(bubbleTint, blendMode = BlendMode.Hue)
                        drawRect(Color.Black.copy(alpha = if (isDark) 0.15f else 0f))
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(size * 0.5f))
        }
    }
}

/** [GlassCircleIconButton] with the same real liquid-glass refraction as [LiquidIconBubble],
 * for the top bar's standalone profile/notification controls — arbitrary [content] (e.g. a
 * [androidx.compose.material3.BadgedBox]-wrapped bell) instead of a single icon, since the
 * notification bell needs its unread badge drawn on top. Falls back to [GlassCircleIconButton]
 * below API 31. */
@Composable
fun LiquidCircleIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    size: Dp = 40.dp,
    content: @Composable () -> Unit,
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        GlassCircleIconButton(onClick = onClick, modifier = modifier, contentDescription = contentDescription, content = content)
        return
    }
    val interactionSource = remember { MutableInteractionSource() }
    val backdrop = rememberLayerBackdrop()
    // The reference design's top-bar bubbles are theme-dependent: near-clear beads in light
    // mode, but a muted steel-blue glass in dark mode (dark-mode-reference-image.png) — the
    // old code rendered the light treatment in both themes, which read as washed-out white
    // discs on the dark page background.
    val isDark = isSystemInDarkTheme()
    val baseGradient = if (isDark) {
        listOf(
            Color(0xFF9DB4D0).copy(alpha = 0.95f),
            Color(0xFF5B7A9C).copy(alpha = 0.75f),
            Color(0xFF2E425C).copy(alpha = 0.65f),
        )
    } else {
        // Light mode: bright white glass bead (reference dashboard) — essentially opaque white
        // with only a breath of cool tint at the very edge, floating on a wide soft shadow and
        // ringed by a crisp bright rim. Glyph contrast comes from the slate-navy icon
        // (see topBarGlyphTint), not the fill.
        listOf(
            Color.White.copy(alpha = 1f),
            Color(0xFFFAFCFE).copy(alpha = 0.97f),
            Color(0xFFE9EFF6).copy(alpha = 0.92f),
        )
    }
    val surfaceTint = if (isDark) Color(0xFF5B7A9C).copy(alpha = 0.18f) else Color(0xFF5B7A9C).copy(alpha = 0.08f)
    val glowShadow: (() -> Shadow)? =
        if (isDark) {
            ({ Shadow(radius = 8.dp, color = Color(0xFF4E7FB5).copy(alpha = 0.35f)) })
        } else {
            // No outer shadow in light mode — the bright bead separates from the page via
            // its rim highlight alone; the soft dark halo read as a dirty black edge.
            null
        }
    Box(
        modifier = modifier
            .size(size)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .semantics {
                role = Role.Button
                contentDescription?.let { this.contentDescription = it }
            },
    ) {
        // Unlike LiquidIconBubble's vividly-colored card bubbles, the top bar's chrome icons
        // aren't tied to any card color — per the reference design this reads as an almost-clear
        // glass bead in light mode and a steel-blue bead in dark mode, with a bright rim
        // [Highlight] and soft [Shadow] doing the work of separating it from the page rather
        // than a saturated fill. A floating chrome control like the FAB, not something embedded
        // in a clipped container, so the shadow doesn't bleed into a layout bug.
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(CircleShape)
                .layerBackdrop(backdrop)
                .background(
                    if (isDark) {
                        Brush.radialGradient(baseGradient)
                    } else {
                        // White must dominate the small bead (reference reads near-pure white):
                        // an evenly-spread gradient leaves most of the area mid-tone blue-grey.
                        Brush.radialGradient(
                            0.0f to Color.White,
                            0.65f to Color.White,
                            1.0f to Color(0xFFE8EEF6),
                        )
                    },
                ),
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { CircleShape },
                    effects = {
                        vibrancy()
                        blur(1.dp.toPx())
                        lens(size.value / 4f, size.value / 2f, depthEffect = true)
                    },
                    highlight = {
                        if (isDark) {
                            Highlight(width = 1.5.dp, alpha = 1f, style = HighlightStyle.Default(intensity = 0.9f))
                        } else {
                            // Narrower rim than the earlier 2dp version — that read as a thick
                            // white ring rather than a glass edge.
                            Highlight(width = 0.75.dp, alpha = 1f, style = HighlightStyle.Default(intensity = 1f))
                        }
                    },
                    shadow = glowShadow,
                    onDrawSurface = {
                        drawRect(surfaceTint)
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}
