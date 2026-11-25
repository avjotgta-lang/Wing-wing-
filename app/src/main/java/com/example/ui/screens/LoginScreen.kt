package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeliveryDining
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.model.DishEntity
import com.example.data.model.PortalRole
import com.example.ui.components.DishThumbnail
import com.example.ui.components.WingWingWordmark
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.MonoTabularStyle
import com.example.ui.viewmodel.WingWingUiState
import kotlin.random.Random

@Composable
fun LoginScreen(
    uiState: WingWingUiState,
    onToggleTheme: () -> Unit,
    onSignInCustomer: (email: String, password: String, rememberMe: Boolean) -> Unit,
    onSignInWithGoogleProfile: (name: String, email: String, rememberMe: Boolean) -> Unit,
    onContinueAsGuest: () -> Unit,
    onForgetSavedCustomer: () -> Unit,
    onOpenPortal: (PortalRole) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }

    var mathA by remember { mutableIntStateOf(Random.nextInt(3, 15)) }
    var mathB by remember { mutableIntStateOf(Random.nextInt(2, 12)) }
    var mathAnswer by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showGoogleDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.preferences.savedCustomerEmail, uiState.preferences.rememberCustomer) {
        if (uiState.preferences.rememberCustomer && uiState.preferences.savedCustomerEmail.isNotBlank()) {
            email = uiState.preferences.savedCustomerEmail
            password = uiState.preferences.savedCustomerPassword
            rememberMe = true
        }
    }

    val refreshCaptcha = {
        mathA = Random.nextInt(3, 18)
        mathB = Random.nextInt(2, 14)
        mathAnswer = ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
    ) {
        // Top Header with Partner Portals + Theme Toggle
        Surface(
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WingWingWordmark(subtitle = "Food, delivered your way")
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IconButton(
                            onClick = onToggleTheme,
                            modifier = Modifier.testTag("login_theme_toggle")
                        ) {
                            Icon(
                                imageVector = if (uiState.preferences.isDarkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                                contentDescription = "Toggle theme"
                            )
                        }
                    }
                }

                // Single clean Partner Portal ribbon
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(start = 16.dp, end = 16.dp, bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PortalChipButton(
                        label = "Restaurant Portal",
                        icon = Icons.Rounded.Restaurant,
                        onClick = { onOpenPortal(PortalRole.RESTAURANT) },
                        testTag = "login_restaurant_portal_btn"
                    )
                    PortalChipButton(
                        label = "Worker Portal",
                        icon = Icons.Rounded.DeliveryDining,
                        onClick = { onOpenPortal(PortalRole.WORKER) },
                        testTag = "login_worker_portal_btn"
                    )
                    PortalChipButton(
                        label = "Owner Console",
                        icon = Icons.Rounded.AdminPanelSettings,
                        onClick = { onOpenPortal(PortalRole.OWNER) },
                        testTag = "login_owner_portal_btn"
                    )
                }
            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            val isWide = maxWidth >= 760.dp

            if (isWide) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    ShowcaseHeroPanel(
                        featuredDishes = uiState.dishes.take(3),
                        onExploreStorefront = onContinueAsGuest,
                        modifier = Modifier
                            .weight(1.1f)
                            .fillMaxHeight()
                    )
                    Box(
                        modifier = Modifier
                            .weight(0.9f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                    ) {
                        SignInFormCard(
                            email = email,
                            onEmailChange = { email = it; errorMessage = null },
                            password = password,
                            onPasswordChange = { password = it; errorMessage = null },
                            showPassword = showPassword,
                            onTogglePassword = { showPassword = !showPassword },
                            mathA = mathA,
                            mathB = mathB,
                            mathAnswer = mathAnswer,
                            onMathAnswerChange = { mathAnswer = it; errorMessage = null },
                            onRefreshMath = refreshCaptcha,
                            rememberMe = rememberMe,
                            onRememberChange = { rememberMe = it },
                            hasSavedLogin = uiState.preferences.rememberCustomer,
                            onForgetSaved = {
                                email = ""
                                password = ""
                                rememberMe = false
                                onForgetSavedCustomer()
                            },
                            errorMessage = errorMessage,
                            onSubmitSignIn = {
                                val parsed = mathAnswer.trim().toIntOrNull()
                                when {
                                    !email.contains("@") || email.trim().length < 5 ->
                                        errorMessage = "Enter a valid email address."
                                    password.length < 4 ->
                                        errorMessage = "Password must be at least 4 characters."
                                    parsed != mathA + mathB -> {
                                        errorMessage = "Incorrect security check answer."
                                        refreshCaptcha()
                                    }
                                    else -> onSignInCustomer(email, password, rememberMe)
                                }
                            },
                            onGoogleSignIn = { showGoogleDialog = true },
                            onGuestExplore = onContinueAsGuest
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ShowcaseHeroPanel(
                        featuredDishes = uiState.dishes.take(3),
                        onExploreStorefront = onContinueAsGuest,
                        modifier = Modifier.fillMaxWidth()
                    )
                    SignInFormCard(
                        email = email,
                        onEmailChange = { email = it; errorMessage = null },
                        password = password,
                        onPasswordChange = { password = it; errorMessage = null },
                        showPassword = showPassword,
                        onTogglePassword = { showPassword = !showPassword },
                        mathA = mathA,
                        mathB = mathB,
                        mathAnswer = mathAnswer,
                        onMathAnswerChange = { mathAnswer = it; errorMessage = null },
                        onRefreshMath = refreshCaptcha,
                        rememberMe = rememberMe,
                        onRememberChange = { rememberMe = it },
                        hasSavedLogin = uiState.preferences.rememberCustomer,
                        onForgetSaved = {
                            email = ""
                            password = ""
                            rememberMe = false
                            onForgetSavedCustomer()
                        },
                        errorMessage = errorMessage,
                        onSubmitSignIn = {
                            val parsed = mathAnswer.trim().toIntOrNull()
                            when {
                                !email.contains("@") || email.trim().length < 5 ->
                                    errorMessage = "Enter a valid email address."
                                password.length < 4 ->
                                    errorMessage = "Password must be at least 4 characters."
                                parsed != mathA + mathB -> {
                                    errorMessage = "Incorrect security check answer."
                                    refreshCaptcha()
                                }
                                else -> onSignInCustomer(email, password, rememberMe)
                            }
                        },
                        onGoogleSignIn = { showGoogleDialog = true },
                        onGuestExplore = onContinueAsGuest
                    )
                }
            }
        }
    }

    if (showGoogleDialog) {
        GoogleAccountConnectDialog(
            initialEmail = uiState.preferences.savedCustomerEmail,
            onDismiss = { showGoogleDialog = false },
            onConfirm = { gName, gEmail ->
                showGoogleDialog = false
                onSignInWithGoogleProfile(gName, gEmail, rememberMe)
            }
        )
    }
}

@Composable
private fun PortalChipButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        modifier = Modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = CoralPrimary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ShowcaseHeroPanel(
    featuredDishes: List<DishEntity>,
    onExploreStorefront: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_spread),
                    contentDescription = "WingWing culinary spread",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Black.copy(alpha = 0.15f),
                                    Color.Black.copy(alpha = 0.82f)
                                )
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(18.dp)
                ) {
                    Text(
                        text = "Good food. Good mood.",
                        style = MaterialTheme.typography.labelMedium,
                        color = CoralPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Crispy wings, wood-fired pies & local classics in minutes.",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onExploreStorefront,
                        colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                        shape = CircleShape,
                        modifier = Modifier.testTag("hero_browse_storefront_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Storefront,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Browse Storefront", fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (featuredDishes.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Tonight's Cravings",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        featuredDishes.forEach { dish ->
                            Surface(
                                modifier = Modifier
                                    .width(170.dp)
                                    .clickable(onClick = onExploreStorefront),
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    DishThumbnail(
                                        dish = dish,
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(MaterialTheme.shapes.extraSmall)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = dish.name,
                                            style = MaterialTheme.typography.labelMedium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "₹${dish.priceInr}",
                                            style = MonoTabularStyle.copy(
                                                fontSize = MaterialTheme.typography.labelMedium.fontSize
                                            ),
                                            color = CoralPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SignInFormCard(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    showPassword: Boolean,
    onTogglePassword: () -> Unit,
    mathA: Int,
    mathB: Int,
    mathAnswer: String,
    onMathAnswerChange: (String) -> Unit,
    onRefreshMath: () -> Unit,
    rememberMe: Boolean,
    onRememberChange: (Boolean) -> Unit,
    hasSavedLogin: Boolean,
    onForgetSaved: () -> Unit,
    errorMessage: String?,
    onSubmitSignIn: () -> Unit,
    onGoogleSignIn: () -> Unit,
    onGuestExplore: () -> Unit
) {
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
            Text(
                text = "Customer Sign In",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Sign in to save delivery details, track live orders, and unlock promo offers.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("Email Address") },
                leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("customer_email_input")
            )

            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
                singleLine = true,
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = onTogglePassword) {
                        Icon(
                            imageVector = if (showPassword) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                            contentDescription = "Toggle password"
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("customer_password_input")
            )

            // Dynamic math security check
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$mathA + $mathB = ?",
                            style = MonoTabularStyle.copy(
                                fontSize = MaterialTheme.typography.titleMedium.fontSize
                            ),
                            color = CoralPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = onRefreshMath,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = "Refresh security check",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = mathAnswer,
                    onValueChange = onMathAnswerChange,
                    label = { Text("Security Check") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("customer_captcha_input")
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onRememberChange(!rememberMe) },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = rememberMe, onCheckedChange = onRememberChange)
                    Text(
                        text = "Remember me on this device",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if (hasSavedLogin) {
                    TextButton(onClick = onForgetSaved) {
                        Text("Forget", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            if (!errorMessage.isNullOrBlank()) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium
                )
            }

            Button(
                onClick = onSubmitSignIn,
                colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("customer_signin_button")
            ) {
                Text("Sign In", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text = "  OR  ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            OutlinedButton(
                onClick = onGoogleSignIn,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("continue_google_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.AccountCircle,
                    contentDescription = null,
                    tint = CoralPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Continue with Google", fontWeight = FontWeight.SemiBold)
            }

            FilledTonalButton(
                onClick = onGuestExplore,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("guest_explore_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Storefront,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Explore Menu as Guest", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun GoogleAccountConnectDialog(
    initialEmail: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, email: String) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var googleEmail by remember { mutableStateOf(initialEmail) }
    var error by remember { mutableStateOf<String?>(null) }

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
                Text(
                    text = "Continue with Google Profile",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Enter your Google account details to personalize your WingWing orders.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it; error = null },
                    label = { Text("Your Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = googleEmail,
                    onValueChange = { googleEmail = it; error = null },
                    label = { Text("Google Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (!error.isNullOrBlank()) {
                    Text(
                        text = error!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (!googleEmail.contains("@")) {
                                error = "Enter a valid email address."
                            } else {
                                onConfirm(
                                    fullName.ifBlank { googleEmail.substringBefore("@") },
                                    googleEmail
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
                    ) {
                        Text("Continue", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
