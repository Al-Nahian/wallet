package com.example.wallet.core.design.glass

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * A recording of the content sitting behind a glass surface, so that surface can draw it back
 * blurred — the real "backdrop blur" that makes the material read as glass rather than as a
 * flat translucent panel.
 *
 * Pair [Modifier.glassBackdropSource] (on the content to capture) with [Modifier.glassBackdrop]
 * (on the surface that blurs it). The two must not overlap in the composition tree, or the
 * surface would try to record itself.
 */
class GlassBackdrop internal constructor(internal val layer: GraphicsLayer) {
    /** Where the recording starts, so a surface can work out which slice of it sits behind it. */
    internal var sourceCoordinates: LayoutCoordinates? by mutableStateOf(null)
}

/** Fraction of full resolution the bottom nav's live blur is recorded/blurred at — see the
 * comment in [glassBackdrop] for why this is a safe, visually-invisible cost cut. */
private const val BlurDownsampleFactor = 0.5f

@Composable
fun rememberGlassBackdrop(): GlassBackdrop {
    val layer = rememberGraphicsLayer()
    return remember(layer) { GlassBackdrop(layer) }
}

/**
 * Records this subtree's drawing into [backdrop] (and still draws it normally), over an opaque
 * [background]. The background has to be part of the recording: a surface draws the blurred copy
 * on top of the original, so anything the copy leaves transparent would show through sharp.
 */
fun Modifier.glassBackdropSource(backdrop: GlassBackdrop, background: Color): Modifier = this
    .onGloballyPositioned { backdrop.sourceCoordinates = it }
    .drawWithContent {
        backdrop.layer.record {
            drawRect(background)
            this@drawWithContent.drawContent()
        }
        drawLayer(backdrop.layer)
    }

/**
 * Draws the slice of [backdrop] that sits behind this surface, blurred, beneath its own content.
 * Below API 31 there's no blur to apply, so this draws nothing and the surface falls back to its
 * translucent fill alone ([GlassColors] already runs that fill more opaque on those devices).
 *
 * Apply *after* the surface's own `clip(shape)` so the blur takes the surface's shape.
 */
fun Modifier.glassBackdrop(backdrop: GlassBackdrop?, radius: Dp = 14.dp): Modifier {
    if (backdrop == null || !GlassCapabilities.supportsAdvancedBlur()) return this
    return composed {
        val blurLayer = rememberGraphicsLayer()
        var offsetInSource by remember { mutableStateOf(Offset.Zero) }
        this
            .onGloballyPositioned { coordinates ->
                offsetInSource = backdrop.sourceCoordinates
                    ?.localPositionOf(coordinates, Offset.Zero)
                    ?: Offset.Zero
            }
            .drawBehind {
                // Blurring at full resolution every scroll frame was the dominant cost behind
                // the bottom nav bar's real backdrop blur — confirmed via `dumpsys gfxinfo
                // framestats`, which showed 100-300ms frames during scroll (vs. a 16.6ms
                // budget). A Gaussian blur's cost scales with pixel count, and a blur already
                // discards fine detail, so recording + blurring at a quarter of the pixels
                // (half width, half height) then scaling the result back up is visually
                // indistinguishable while cutting that per-frame blur work to a quarter.
                val downsample = BlurDownsampleFactor
                val radiusPx = radius.toPx() * downsample
                blurLayer.renderEffect = BlurEffect(radiusPx, radiusPx, TileMode.Clamp)
                val downSize = IntSize(
                    (size.width * downsample).roundToInt().coerceAtLeast(1),
                    (size.height * downsample).roundToInt().coerceAtLeast(1),
                )
                blurLayer.record(size = downSize) {
                    scale(downsample, pivot = Offset.Zero) {
                        translate(-offsetInSource.x, -offsetInSource.y) {
                            drawLayer(backdrop.layer)
                        }
                    }
                }
                scale(1f / downsample, pivot = Offset.Zero) {
                    drawLayer(blurLayer)
                }
            }
    }
}
