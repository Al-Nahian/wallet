package com.example.wallet.feature.profile

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wallet.core.design.components.SecondaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onManageCategories: () -> Unit,
    onManageLabels: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Account") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(24.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = null,
                modifier = Modifier.padding(bottom = 16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (currentUser == null) {
                Text(
                    text = "Not signed in",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "Your data stays on this device. Sign in to back it up and use " +
                        "Wallet on more than one device.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp, top = 8.dp),
                )

                val comingSoon = {
                    Toast.makeText(context, "Sign-in arrives in a later phase.", Toast.LENGTH_SHORT).show()
                }

                SecondaryButton(
                    text = "Continue with Google",
                    onClick = comingSoon,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                )
                SecondaryButton(
                    text = "Continue with Microsoft",
                    onClick = comingSoon,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                )
                SecondaryButton(
                    text = "Continue with Email",
                    onClick = comingSoon,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                Text(text = currentUser!!.displayName, style = MaterialTheme.typography.titleLarge)
            }

            SecondaryButton(
                text = "Manage categories",
                onClick = onManageCategories,
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            )
            SecondaryButton(
                text = "Manage labels",
                onClick = onManageLabels,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            )
        }
    }
}
