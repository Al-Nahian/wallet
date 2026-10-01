package com.expensetracker.wallet.feature.profile

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import com.expensetracker.wallet.core.design.components.GlassScreenScaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.expensetracker.wallet.core.design.components.GlassScreenTopBar
import com.expensetracker.wallet.core.design.components.GoogleLogoIcon
import com.expensetracker.wallet.core.design.components.MicrosoftLogoIcon
import com.expensetracker.wallet.core.design.components.SecondaryButton
import com.expensetracker.wallet.core.design.components.SocialSignInButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onManageCategories: () -> Unit,
    onManageLabels: () -> Unit,
    onManageTemplates: () -> Unit,
    onImportExport: () -> Unit,
    onAutomationSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isSigningIn by viewModel.isSigningIn.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val context = LocalContext.current
    // null = dialog closed; otherwise the mode to land on (false = sign in, true = create account).
    var emailDialogInitialSignUp by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.dismissError()
        }
    }

    emailDialogInitialSignUp?.let { initialSignUp ->
        EmailSignInDialog(
            initialSignUp = initialSignUp,
            onSignIn = { email, password ->
                viewModel.signInWithEmail(email, password)
                emailDialogInitialSignUp = null
            },
            onSignUp = { email, password ->
                viewModel.signUpWithEmail(email, password)
                emailDialogInitialSignUp = null
            },
            onDismiss = { emailDialogInitialSignUp = null },
        )
    }

    GlassScreenScaffold(
        modifier = modifier,
        topBar = {
            GlassScreenTopBar(
                title = "Account",
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
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
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

                if (isSigningIn) {
                    CircularProgressIndicator(modifier = Modifier.padding(bottom = 16.dp))
                }

                SocialSignInButton(
                    text = "Continue with Google",
                    onClick = viewModel::signInWithGoogle,
                    modifier = Modifier.padding(bottom = 10.dp),
                    icon = { GoogleLogoIcon() },
                )
                SocialSignInButton(
                    text = "Continue with Microsoft",
                    onClick = viewModel::signInWithAzure,
                    modifier = Modifier.padding(bottom = 10.dp),
                    icon = { MicrosoftLogoIcon() },
                )
                SocialSignInButton(
                    text = "Continue with Email",
                    onClick = { emailDialogInitialSignUp = false },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Email,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    },
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f))
                    Text(
                        text = "OR",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp),
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f))
                }

                SocialSignInButton(
                    text = "Create an account",
                    onClick = { emailDialogInitialSignUp = true },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.PersonAdd,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    },
                )
            } else {
                Text(text = currentUser!!.displayName, style = MaterialTheme.typography.titleLarge)
                SecondaryButton(
                    text = "Sign out",
                    onClick = viewModel::signOut,
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                )
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
            SecondaryButton(
                text = "Manage templates",
                onClick = onManageTemplates,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            )
            SecondaryButton(
                text = "Import / export data",
                onClick = onImportExport,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            )
            SecondaryButton(
                text = "Automation",
                onClick = onAutomationSettings,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            )
        }
    }
}
