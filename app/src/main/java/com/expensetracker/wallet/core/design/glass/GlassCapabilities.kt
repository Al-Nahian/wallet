package com.expensetracker.wallet.core.design.glass

import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

/**
 * liquid_glass_implementation_plan.md §8/§28/§36 — capability + accessibility signals the glass
 * renderer reads before deciding how much material to apply. Never invented: [supportsAdvancedBlur]
 * gates on the real Android version that ships [android.graphics.RenderEffect]-backed blur
 * (`Modifier.blur` only actually blurs on API 31+; below that it's a documented no-op, not a
 * crash), and [systemPrefersReducedMotion] reads the real "Remove animations" developer/accessibility
 * setting (`Settings.Global.ANIMATOR_DURATION_SCALE`).
 */
object GlassCapabilities {
    fun supportsAdvancedBlur(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    /** API 31+ only — [android.view.WindowManager.LayoutParams.setBlurBehindRadius] backs
     * [com.expensetracker.wallet.core.design.glass.GlassWindowBlur]. */
    fun supportsWindowBlurBehind(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
}

/** Read once per composition root (see `AppTheme`/`WalletTheme`), not per glass surface — this
 * is a ContentResolver read, not something to repeat on every recomposition. */
@Composable
fun rememberSystemPrefersReducedMotion(): Boolean {
    val context = LocalContext.current
    return try {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    } catch (_: Settings.SettingNotFoundException) {
        false
    }
}

/** liquid_glass_implementation_plan.md §28/§29. Android has no direct "reduce transparency"
 * system signal equivalent to iOS, so [reduceTransparency] defaults off and exists as a hook a
 * future in-app accessibility setting can flip; [reduceMotion] is wired to the real system
 * animator-scale signal via [rememberSystemPrefersReducedMotion]. */
data class GlassMotionPreferences(
    val reduceMotion: Boolean = false,
    val reduceTransparency: Boolean = false,
)

val LocalGlassMotionPreferences = staticCompositionLocalOf { GlassMotionPreferences() }
