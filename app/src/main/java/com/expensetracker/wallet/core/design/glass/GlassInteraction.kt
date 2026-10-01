package com.expensetracker.wallet.core.design.glass

/** liquid_glass_implementation_plan.md §14 — whether a [GlassSurface] should visually respond to
 * press (subtle scale + tint boost) or stay static. The actual press signal comes from an
 * `interactionSource` the caller passes in (e.g. [GlassButton]'s `clickable`); this enum just
 * says whether [GlassSurface] should react to it. */
enum class GlassInteraction {
    None,
    Pressable,
}
