package com.example.wallet.core.design.glass

import android.os.Build
import android.view.Window
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider

/**
 * liquid_glass_implementation_plan.md §19 — real platform backdrop blur for modal dialogs/sheets,
 * via [WindowManager.LayoutParams.setBlurBehindRadius] (API 31+): unlike [GlassSurface], a dialog
 * gets its own [android.view.Window], so the OS can genuinely blur whatever is behind that window
 * (the rest of the app) rather than us having to fake it. Call once from inside a dialog's content
 * — works for both a raw `androidx.compose.ui.window.Dialog` and Material3 `AlertDialog`, since
 * both are built on the same [DialogWindowProvider]. No-op below API 31 or if the window can't be
 * resolved (e.g. Preview/tests) — never crashes (plan §37).
 */
@Composable
fun GlassWindowBlur(radiusPx: Int = 48) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
    val view = LocalView.current
    DisposableEffect(view, radiusPx) {
        val window = (view.parent as? DialogWindowProvider)?.window
        if (window != null) applyDialogBlurBehind(window, radiusPx)
        onDispose {}
    }
}

/** The non-Compose counterpart of [GlassWindowBlur] — for a raw platform dialog (e.g.
 * `DatePickerDialog`/`TimePickerDialog`, which have no Compose content to call [GlassWindowBlur]
 * from), call this directly on `dialog.window` before `dialog.show()`. No-op below API 31. */
fun applyDialogBlurBehind(window: Window, radiusPx: Int = 48) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
    applyBlurBehindApi31(window, radiusPx)
}

@RequiresApi(Build.VERSION_CODES.S)
private fun applyBlurBehindApi31(window: Window, radiusPx: Int) {
    window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
    window.attributes = window.attributes.apply { blurBehindRadius = radiusPx }
}
