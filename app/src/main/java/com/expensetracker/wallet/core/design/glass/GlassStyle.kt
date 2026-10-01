package com.expensetracker.wallet.core.design.glass

/** liquid_glass_implementation_plan.md §5. */
enum class GlassStyle {
    /** Default functional glass for navigation and floating controls. */
    Regular,

    /** More transparent; only use when background contrast remains safe. */
    Clear,

    /** Higher opacity for sheets, dialogs and important controls. */
    Thick,

    /** Subtle glass for limited use. */
    Thin,

    /** Regular glass with interaction response (press scale/tint). */
    Interactive,

    /** Saturated, strongly tinted glass for headline content cards (account/balance/stat tiles) —
     * the material still reads as glass via highlight/border/glow, but the [tint] color dominates
     * the fill rather than being a barely-there accent. Always pair with a [tint]. */
    Vivid,
}
