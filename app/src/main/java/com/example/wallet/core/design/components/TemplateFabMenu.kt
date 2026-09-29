package com.example.wallet.core.design.components

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.wallet.core.design.DarkPrimary
import com.example.wallet.core.design.WalletTheme
import com.example.wallet.core.design.glass.GlassShapes
import com.example.wallet.core.design.glass.GlassTokens
import com.example.wallet.core.design.glass.GlassWindowBlur
import com.example.wallet.domain.model.Template
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur as backdropBlur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.highlight.HighlightStyle
import com.kyant.backdrop.shadow.Shadow as BackdropShadow
import kotlin.math.cos
import kotlin.math.sin

/** The fixed actions [TemplateFabMenu] fans out to. */
enum class TemplateFabAction {
    ADD_NEW_TRANSACTION,
    SELECT_TEMPLATE,
    ADD_ACCOUNT,
}

/** One fanned-out circle: which action it triggers, its icon, and the angle (degrees, measured
 * counterclockwise from the positive x-axis, so 90° is straight up) it sits at around the FAB. */
private data class FabMenuSlot(
    val action: TemplateFabAction,
    val icon: ImageVector,
    val contentDescription: String,
    val angleDegrees: Float,
)

private val fabMenuSlots = listOf(
    FabMenuSlot(TemplateFabAction.ADD_NEW_TRANSACTION, Icons.Filled.Add, "Add new transaction", 150f),
    FabMenuSlot(TemplateFabAction.SELECT_TEMPLATE, Icons.Filled.Bookmark, "Select template", 90f),
    FabMenuSlot(TemplateFabAction.ADD_ACCOUNT, Icons.Filled.AccountBalance, "Add account", 30f),
)

/** github.com/jurajkusnier/fluid-bottom-navigation's blur-then-threshold "goo": a strong blur
 * smears solid shapes into each other at the seams, then a steep alpha cutoff (the `50f, -5000f`
 * row) snaps every softened edge back to fully opaque or fully transparent — nothing stays
 * partially blurred, so overlapping/adjacent circles read as one melted blob instead of two
 * translucent smudges. */
@RequiresApi(Build.VERSION_CODES.S)
private fun gooRenderEffect(): RenderEffect {
    // DECAL (not MIRROR): the blob box is a tight fit around the FAB + two circles, so a blob
    // sitting flush against the box's own edge is common (the anchor circle is bottom-aligned
    // exactly at the FAB). MIRROR reflects that edge-touching shape back into the layer, which
    // reads as the goo "spreading" far outside its circles (into the nav bar below); DECAL pads
    // with transparent instead, so the effect never produces content beyond what was actually
    // drawn.
    val blur = RenderEffect.createBlurEffect(36f, 36f, Shader.TileMode.DECAL)
    val threshold = RenderEffect.createColorFilterEffect(
        ColorMatrixColorFilter(
            ColorMatrix(
                floatArrayOf(
                    1f, 0f, 0f, 0f, 0f,
                    0f, 1f, 0f, 0f, 0f,
                    0f, 0f, 1f, 0f, 0f,
                    0f, 0f, 0f, 50f, -5000f,
                ),
            ),
        ),
    )
    return RenderEffect.createChainEffect(threshold, blur)
}

/** Remaps [progress] from the `[from, to]` sub-range to `[0, 1]` before easing it — the same
 * trick the reference repo's `Utils.kt` uses to stagger several items off one shared animation
 * value instead of running a separate `Animatable` per item. */
private fun Easing.transform(from: Float, to: Float, progress: Float): Float =
    transform(((progress - from) * (1f / (to - from))).coerceIn(0f, 1f))

private val ItemCircleSize = 50.dp
private val FanRadius = 96.dp
private val MenuBoxWidth = 300.dp
private val MenuBoxHeight = 220.dp

/** A three-icon "speed dial" fanning up from the transaction FAB — deliberately matched to
 * github.com/jurajkusnier/fluid-bottom-navigation's own reference screenshot (a handful of
 * fixed, icon-only circles arcing out from the button, not an open-ended list): one circle each
 * for "Add New Transaction", "Select Template" (which opens [TemplateShortcutPickerDialog] to
 * actually pick from the saved templates) and "Add Account". An earlier version fanned one circle
 * per saved template straight up in a vertical stack, which not only broke the reference's
 * clean-arc look but had no fixed count to size the blur layer or item spacing against.
 *
 * Uses the reference's own gooey-blob technique — a blurred layer of solid color circles run
 * through a steep alpha threshold ([gooRenderEffect]) so the circles melt into each other and
 * into the FAB beneath while they're still close together early in the motion, then separate
 * cleanly once [FanRadius] pulls them far enough apart. A second, un-blurred layer draws the
 * actual icons on top, at identical offsets, using [liquidFabBackdrop] (when supplied, API 31+)
 * to render the exact same real backdrop-blur glass [CenterFabItem] itself uses — not an
 * approximation of it — so every circle looks like the FAB's own material, not a differently
 * rendered badge sitting next to it. Below API 31, or with no backdrop captured, falls back to a
 * gradient-and-border approximation. */
@Composable
fun TemplateFabMenu(
    expanded: Boolean,
    onAction: (TemplateFabAction) -> Unit,
    fabSize: Dp,
    modifier: Modifier = Modifier,
    liquidFabBackdrop: LayerBackdrop? = null,
) {
    val progress by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = tween(durationMillis = 650, easing = LinearEasing),
        label = "templateFabMenuProgress",
    )
    // Nothing to draw once fully collapsed — avoids paying for the blur graphicsLayer at rest.
    if (progress <= 0f) return

    // Same fixed mint the FAB itself always uses (CenterFabItem's own `tint`), not the Template
    // feature's usual orange — the fanned-out circles should read as liquid budding off the "+"
    // button itself, not as a differently-colored menu layered on top of it.
    val tint = DarkPrimary
    val renderEffect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        remember { gooRenderEffect().asComposeRenderEffect() }
    } else {
        null
    }

    val slotCount = fabMenuSlots.size
    val slotMotion = fabMenuSlots.mapIndexed { index, slot ->
        val start = index * (0.12f / slotCount)
        val posT = FastOutSlowInEasing.transform(start, (start + 0.75f).coerceAtMost(1f), progress)
        val angleRad = Math.toRadians(slot.angleDegrees.toDouble())
        val offsetX = FanRadius * cos(angleRad).toFloat() * posT
        val offsetY = -FanRadius * sin(angleRad).toFloat() * posT
        SlotMotion(slot, offsetX, offsetY, 0.4f + 0.6f * posT)
    }

    // Fixed size (not fillMaxWidth/wrap-content): this becomes the Popup's own measured content
    // size in BottomNavigation.kt, and Popup centers ITS bounds over the FAB — a lopsided
    // measurement would pull that center, and so the whole fan, sideways off the FAB.
    Box(modifier = modifier.width(MenuBoxWidth).height(MenuBoxHeight), contentAlignment = Alignment.BottomCenter) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                // Forces this whole subtree into its OWN offscreen buffer, isolated from
                // whatever's drawn behind it (the dashboard content) — without this, the
                // drawWithContent below's SrcAtop blend composites against the shared root
                // canvas (i.e. the page behind the FAB), not just this box's own blob shapes,
                // painting a large hazy rectangle over the dashboard instead of tinting the goo.
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                // A glassy diagonal sheen, SrcAtop so — now that the layer above isolates it —
                // it only tints pixels the goo shape actually painted, leaving the transparent
                // surroundings untouched. Without this the melted "neck" between the FAB and
                // each circle is flat opaque color, unlike every other glass surface in this app
                // (matches FabMenuIcon's own sheen on the resting circles).
                .drawWithContent {
                    drawContent()
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(Color.White.copy(alpha = 0.32f), Color.Transparent),
                        ),
                        blendMode = BlendMode.SrcAtop,
                    )
                },
            contentAlignment = Alignment.BottomCenter,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { this.renderEffect = renderEffect },
                contentAlignment = Alignment.BottomCenter,
            ) {
                // Anchors the goo to the FAB itself — without this, blobs appear to spawn out of
                // thin air instead of pinching off the button that opened them. Always full
                // opacity, same as the FAB and every circle: this whole effect went through an
                // earlier version that faded this anchor's alpha out as the blobs separated, but
                // the goo threshold ColorMatrix (see gooRenderEffect) snaps every alpha above its
                // cutoff to fully OPAQUE — there's no such thing as a "40% faded" shape inside
                // this render effect, so that fade only ever produced an abrupt solid-to-gone
                // flip, not a smooth cross-fade, and looked like a flash of solid color instead
                // of a drain. Once the blobs move far enough apart the anchor is simply hidden
                // behind the real FAB drawn on top of it (same position, same fixed look).
                Box(Modifier.size(fabSize).clip(CircleShape).background(tint))
                slotMotion.forEach { motion ->
                    Box(
                        Modifier
                            .offset(x = motion.offsetX, y = motion.offsetY)
                            .size(ItemCircleSize)
                            .scale(motion.scale)
                            .clip(CircleShape)
                            .background(tint),
                    )
                }
            }
        }
        slotMotion.forEach { motion ->
            FabMenuIcon(
                icon = motion.slot.icon,
                contentDescription = motion.slot.contentDescription,
                tint = tint,
                liquidBackdrop = liquidFabBackdrop,
                offsetX = motion.offsetX,
                offsetY = motion.offsetY,
                scale = motion.scale,
                onClick = { onAction(motion.slot.action) },
            )
        }
    }
}

private class SlotMotion(
    val slot: FabMenuSlot,
    val offsetX: Dp,
    val offsetY: Dp,
    val scale: Float,
)

/** The un-blurred layer drawn over each blob, rendered with the FAB's own fixed, never-animated
 * material — same colors, same alphas, same everything [CenterFabItem] always shows at rest —
 * so every circle looks like a piece of the FAB itself budding off, not a differently-styled menu
 * item. Only position and [scale] animate; a growing-then-settling circle reads as fluid motion
 * on its own, and fading the glass material itself on top of that turned out to double-render
 * badly (see the anchor blob's own comment in [TemplateFabMenu] for why). With [liquidBackdrop]
 * captured (API 31+), this renders through the exact same [drawBackdrop] call [CenterFabItem]
 * itself uses — real background blur/refraction, not an approximation. Without a backdrop, falls
 * back to a gradient-and-border "glass bead" approximation. */
@Composable
private fun FabMenuIcon(
    icon: ImageVector,
    contentDescription: String,
    tint: Color,
    liquidBackdrop: LayerBackdrop?,
    offsetX: Dp,
    offsetY: Dp,
    scale: Float,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    if (liquidBackdrop != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Box(
            modifier = Modifier
                .offset(x = offsetX, y = offsetY)
                .size(ItemCircleSize)
                .scale(scale)
                .drawBackdrop(
                    backdrop = liquidBackdrop,
                    shape = { CircleShape },
                    effects = {
                        vibrancy()
                        backdropBlur(2.dp.toPx())
                        lens(12.dp.toPx(), 24.dp.toPx(), depthEffect = true)
                    },
                    highlight = { Highlight(width = 3.dp, alpha = 1f, style = HighlightStyle.Default(intensity = 1f)) },
                    shadow = { BackdropShadow(radius = 16.dp, color = tint.copy(alpha = 0.55f)) },
                    onDrawSurface = {
                        drawRect(tint, blendMode = BlendMode.Hue)
                        drawRect(tint.copy(alpha = 0.75f))
                    },
                )
                .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
                .semantics {
                    role = Role.Button
                    this.contentDescription = contentDescription
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = Color.White)
        }
    } else {
        Box(
            modifier = Modifier
                .offset(x = offsetX, y = offsetY)
                .size(ItemCircleSize)
                .scale(scale)
                .drawBehind {
                    val glowRadius = size.maxDimension / 2f + 8.dp.toPx()
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(tint.copy(alpha = 0.55f), Color.Transparent),
                            radius = glowRadius,
                            center = center,
                        ),
                        radius = glowRadius,
                        center = center,
                    )
                }
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.4f),
                            tint.copy(alpha = 0.22f),
                        ),
                    ),
                )
                .border(1.5.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
                .semantics {
                    role = Role.Button
                    this.contentDescription = contentDescription
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = Color.White)
        }
    }
}

/** Opened by [TemplateFabMenu]'s "Select Template" circle — a compact list of every saved
 * template, styled like this app's other centered glass pickers (AccountPickerDialog,
 * CategoryPickerDialog) so it reads as the same component family. Picking one hands its id back
 * to the caller, which navigates to Add Transaction pre-filled via
 * `TransactionRoutes.createFromTemplate`. */
@Composable
fun TemplateShortcutPickerDialog(
    templates: List<Template>,
    onSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val tint = WalletTheme.extendedColors.warning
    val isDark = isSystemInDarkTheme()

    Dialog(onDismissRequest = onDismiss) {
        GlassWindowBlur()
        LiquidGlassCard(
            tint = tint,
            modifier = Modifier.fillMaxWidth().heightIn(max = 480.dp),
            shape = GlassShapes.large,
            cornerRadius = GlassTokens.cornerLarge,
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                ) {
                    GlassIconBubble(icon = Icons.Filled.Bookmark, tint = tint, size = 32.dp)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Select a template",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = if (isDark) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                if (templates.isEmpty()) {
                    Text(
                        text = "No templates yet — add one from a transaction's Template card first.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDark) Color.White.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    )
                } else {
                    LazyColumn {
                        items(templates, key = { it.id }) { template ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelected(template.id) }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                            ) {
                                GlassIconBubble(icon = Icons.Filled.Bookmark, tint = tint, size = 40.dp)
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = template.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
