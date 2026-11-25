package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeliveryDining
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.PortalRole
import com.example.data.model.RememberedPortalCredential
import com.example.ui.theme.CoralPrimary
import kotlinx.coroutines.launch

@Composable
fun RoleGateDialog(
    role: PortalRole,
    rememberedCredential: RememberedPortalCredential?,
    onDismiss: () -> Unit,
    onAuthenticate: suspend (portalKey: String, username: String, password: String, rememberMe: Boolean) -> Boolean,
    onForgetSaved: () -> Unit,
    onUnlocked: () -> Unit
) {
    var portalKey by remember(role) { mutableStateOf(rememberedCredential?.portalKey ?: "") }
    var username by remember(role) { mutableStateOf(rememberedCredential?.username ?: "") }
    var password by remember(role) { mutableStateOf(rememberedCredential?.password ?: "") }
    var rememberMe by remember(role) { mutableStateOf(rememberedCredential != null) }
    var showPassword by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isChecking by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val roleIcon = when (role) {
        PortalRole.RESTAURANT -> Icons.Rounded.Restaurant
        PortalRole.WORKER -> Icons.Rounded.DeliveryDining
        PortalRole.OWNER -> Icons.Rounded.AdminPanelSettings
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = CoralPrimary.copy(alpha = 0.14f)
                        ) {
                            Icon(
                                imageVector = roleIcon,
                                contentDescription = role.label,
                                tint = CoralPrimary,
                                modifier = Modifier
                                    .padding(10.dp)
                                    .size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "${role.label} Portal",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Enter issued portal credentials",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close portal gate"
                        )
                    }
                }

                OutlinedTextField(
                    value = portalKey,
                    onValueChange = {
                        portalKey = it
                        errorMessage = null
                    },
                    label = { Text("Portal Key") },
                    leadingIcon = { Icon(Icons.Rounded.Key, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("portal_key_input")
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it
                        errorMessage = null
                    },
                    label = { Text("Username") },
                    leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("portal_username_input")
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = null
                    },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                contentDescription = "Toggle password visibility"
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("portal_password_input")
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { rememberMe = !rememberMe },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = { rememberMe = it }
                        )
                        Text(
                            text = "Remember me on this device",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (rememberedCredential != null) {
                        TextButton(
                            onClick = {
                                portalKey = ""
                                username = ""
                                password = ""
                                rememberMe = false
                                onForgetSaved()
                            }
                        ) {
                            Text("Forget Login", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                if (!errorMessage.isNullOrBlank()) {
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    onClick = {
                        if (portalKey.isBlank() || username.isBlank() || password.isBlank()) {
                            errorMessage = "All three credential fields are required."
                            return@Button
                        }
                        isChecking = true
                        scope.launch {
                            val ok = onAuthenticate(portalKey, username, password, rememberMe)
                            isChecking = false
                            if (ok) {
                                onUnlocked()
                            } else {
                                errorMessage = "Invalid Portal Key, Username, or Password for ${role.label}."
                            }
                        }
                    },
                    enabled = !isChecking,
                    colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("portal_unlock_button")
                ) {
                    Text(
                        text = if (isChecking) "Verifying..." else "Unlock ${role.label} Portal",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
