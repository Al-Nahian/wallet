package com.example.wallet.core.design.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** The standard four-color Google "G" mark, for the "Continue with Google" sign-in row —
 * required by Google's sign-in branding guidelines, not an arbitrary generic icon. */
private val GoogleLogoVector: ImageVector by lazy {
    Builder(
        name = "GoogleLogo",
        defaultWidth = 18.dp,
        defaultHeight = 18.dp,
        viewportWidth = 18f,
        viewportHeight = 18f,
    ).apply {
        path(fill = SolidColor(Color(0xFF4285F4)), pathFillType = PathFillType.NonZero) {
            moveTo(17.64f, 9.2045f)
            curveTo(17.64f, 8.5664f, 17.5827f, 7.9527f, 17.4764f, 7.3636f)
            lineTo(9f, 7.3636f)
            lineTo(9f, 10.845f)
            lineTo(13.8436f, 10.845f)
            curveTo(13.635f, 11.97f, 13.0009f, 12.9232f, 12.0477f, 13.5614f)
            lineTo(12.0477f, 15.8195f)
            lineTo(14.9564f, 15.8195f)
            curveTo(16.6582f, 14.2527f, 17.64f, 11.9455f, 17.64f, 9.2045f)
            close()
        }
        path(fill = SolidColor(Color(0xFF34A853)), pathFillType = PathFillType.NonZero) {
            moveTo(9f, 18f)
            curveTo(11.43f, 18f, 13.4673f, 17.1941f, 14.9564f, 15.8195f)
            lineTo(12.0477f, 13.5614f)
            curveTo(11.2418f, 14.1014f, 10.2109f, 14.4205f, 9f, 14.4205f)
            curveTo(6.6564f, 14.4205f, 4.6718f, 12.8386f, 3.9641f, 10.71f)
            lineTo(0.9573f, 10.71f)
            lineTo(0.9573f, 13.0418f)
            curveTo(2.4382f, 15.9832f, 5.4818f, 18f, 9f, 18f)
            close()
        }
        path(fill = SolidColor(Color(0xFFFBBC05)), pathFillType = PathFillType.NonZero) {
            moveTo(3.9641f, 10.71f)
            curveTo(3.7841f, 10.17f, 3.6818f, 9.5932f, 3.6818f, 9f)
            curveTo(3.6818f, 8.4068f, 3.7841f, 7.83f, 3.9641f, 7.29f)
            lineTo(3.9641f, 4.9582f)
            lineTo(0.9573f, 4.9582f)
            curveTo(0.3477f, 6.1732f, 0f, 7.5477f, 0f, 9f)
            curveTo(0f, 10.4523f, 0.3477f, 11.8268f, 0.9573f, 13.0418f)
            lineTo(3.9641f, 10.71f)
            close()
        }
        path(fill = SolidColor(Color(0xFFEA4335)), pathFillType = PathFillType.NonZero) {
            moveTo(9f, 3.5795f)
            curveTo(10.3214f, 3.5795f, 11.5077f, 4.0336f, 12.4405f, 4.9255f)
            lineTo(15.0219f, 2.3441f)
            curveTo(13.4632f, 0.8918f, 11.4259f, 0f, 9f, 0f)
            curveTo(5.4818f, 0f, 2.4382f, 2.0168f, 0.9573f, 4.9582f)
            lineTo(3.9641f, 7.29f)
            curveTo(4.6718f, 5.1614f, 6.6564f, 3.5795f, 9f, 3.5795f)
            close()
        }
    }.build()
}

@Composable
fun GoogleLogoIcon(modifier: Modifier = Modifier, size: Dp = 20.dp) {
    Image(imageVector = GoogleLogoVector, contentDescription = null, modifier = modifier.size(size))
}
