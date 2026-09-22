package com.example.wallet.core.design.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.wallet.core.design.glass.GlassBottomBar

data class WalletBottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

@Composable
fun WalletBottomNavigation(
    items: List<WalletBottomNavItem>,
    selectedRoute: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    GlassBottomBar(modifier = modifier) {
        NavigationBar(containerColor = Color.Transparent, tonalElevation = 0.dp) {
            items.forEach { item ->
                NavigationBarItem(
                    selected = item.route == selectedRoute,
                    onClick = { onItemSelected(item.route) },
                    icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                    label = { Text(item.label) },
                )
            }
        }
    }
}
