package com.expensetracker.wallet.feature.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.expensetracker.wallet.core.design.glass.GlassStyle
import com.expensetracker.wallet.core.design.glass.GlassWindowBlur
import com.expensetracker.wallet.core.design.glass.glassDialogContainerColor

/** Email/password sign-in, with a toggle to sign up instead. See ProfileScreen's "Continue with
 * Email" and "Create an account" entry points, which set [initialSignUp] to land on the right mode. */
@Composable
fun EmailSignInDialog(
    onSignIn: (email: String, password: String) -> Unit,
    onSignUp: (email: String, password: String) -> Unit,
    onDismiss: () -> Unit,
    initialSignUp: Boolean = false,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(initialSignUp) }

    val canSubmit = email.isNotBlank() && password.length >= 6

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isSignUp) "Create account" else "Sign in") },
        text = {
            GlassWindowBlur()
            Column {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )
                TextButton(onClick = { isSignUp = !isSignUp }, modifier = Modifier.padding(top = 4.dp)) {
                    Text(if (isSignUp) "Already have an account? Sign in" else "New here? Create an account")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (isSignUp) onSignUp(email, password) else onSignIn(email, password)
                },
                enabled = canSubmit,
            ) {
                Text(if (isSignUp) "Create account" else "Sign in")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        containerColor = glassDialogContainerColor(GlassStyle.Thick),
    )
}
