package com.expensetracker.wallet.core.design.glass

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection

/**
 * A rounded-rect bar shape with a deep, wide "bucket" pocket cut into the top-center edge for a
 * center-docked FAB to nest into — ported from the actual `NavCustomPainter` in
 * github.com/namanh11611/curved_labeled_navigation_bar (the package behind
 * medium.com/@namanh11611/custom-curved-bottom-navigation-bar-in-flutter-db60d55124ee), fetched
 * and read directly rather than guessed at, since an earlier simple symmetric-valley attempt (one
 * cubic per side, meeting at a point) didn't match the reference's shape: that package's own
 * `paint()` uses TWO cubics per side, and each cubic's second control point already sits at full
 * notch depth back at the notch's OWN edge (not the center) — the curve dives down to full depth
 * near the edges, then travels an almost flat-bottomed stretch across to center, instead of a
 * single point-bottomed dip. That reads as a wide, rounded-bottom "bucket" a FAB nestles inside
 * (this app's reference images), not a shallow scoop.
 *
 * A later attempt replaced this with a mathematically exact circular arc (constant distance from
 * the FAB's edge everywhere) — rejected: it read as *worse*, not better, despite being more
 * "correct" geometrically. This bucket shape is back by request.
 *
 * [notchDepthFraction] is a fraction of this shape's own rendered height (like the original's
 * `bottom = 0.6`), not an absolute size — a deep pocket needs to scale with however tall the bar
 * actually renders, not a fixed dp guessed independently of it.
 */
class NotchedBottomBarShape(
    private val cornerRadius: Dp,
    private val notchWidth: Dp,
    private val notchDepthFraction: Float,
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val corner = with(density) { cornerRadius.toPx() }.coerceAtMost(size.minDimension / 2f)
        val notchSpan = with(density) { notchWidth.toPx() }
        val depth = size.height * notchDepthFraction
        val centerX = size.width / 2f
        val diameter = corner * 2f
        val leftEdge = centerX - notchSpan / 2f
        val rightEdge = centerX + notchSpan / 2f
        // The original's fixed "0.05" pre/post-curve gap is, in its own units, 0.25 of its fixed
        // s=0.2 notch-width constant — reproduced here as a fraction of our own notchSpan so it
        // scales the same way regardless of the actual notch width chosen.
        val gap = notchSpan * 0.25f

        val path = Path().apply {
            moveTo(corner, 0f)
            lineTo(leftEdge - gap, 0f)
            // The first control point sits at y=0 — exactly level with the flat edge just drawn —
            // rather than the ported source's own slight "0.05*height" dip there. That original
            // value gives this cubic a start tangent that's already sloping downward the instant
            // it leaves the flat line, which reads as a visible kink/dent right at the seam once
            // this shape's depth (0.6+ of the bar's own height) is far bigger than the small
            // source project's own notch ever was. Pinning it to y=0 makes the start tangent
            // exactly horizontal — G1-continuous with the incoming lineTo — so the curve actually
            // leaves the flat edge smoothly before diving, instead of kinking immediately.
            cubicTo(
                leftEdge + notchSpan * 0.2f, 0f,
                leftEdge, depth,
                centerX, depth,
            )
            // Mirrored climb back to the flat top edge — same fix, symmetric.
            cubicTo(
                rightEdge, depth,
                rightEdge - notchSpan * 0.2f, 0f,
                rightEdge + gap, 0f,
            )
            lineTo(size.width - corner, 0f)
            arcTo(Rect(size.width - diameter, 0f, size.width, diameter), -90f, 90f, false)
            lineTo(size.width, size.height - corner)
            arcTo(Rect(size.width - diameter, size.height - diameter, size.width, size.height), 0f, 90f, false)
            lineTo(corner, size.height)
            arcTo(Rect(0f, size.height - diameter, diameter, size.height), 90f, 90f, false)
            lineTo(0f, corner)
            arcTo(Rect(0f, 0f, diameter, diameter), 180f, 90f, false)
            close()
        }
        return Outline.Generic(path)
    }
}
