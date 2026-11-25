package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.DiscountType
import com.example.data.model.PortalRole
import com.example.ui.components.AnimatedKpiCard
import com.example.ui.components.OrderStatusPill
import com.example.ui.components.PaymentQrPreview
import com.example.ui.components.StarRatingDisplay
import com.example.ui.components.formatEpochTime
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.MonoTabularStyle
import com.example.ui.theme.SkyInfo
import com.example.ui.viewmodel.WingWingUiState
import kotlin.random.Random

@Composable
fun OwnerPortalScreen(
    uiState: WingWingUiState,
    onBack: () -> Unit,
    onLockPortal: () -> Unit,
    onForgetSavedLogin: () -> Unit,
    onTrackOrder: (String) -> Unit,
    onAddKitchen: (name: String, cuisine: String, prepRange: String, address: String) -> Unit,
    onToggleKitchenActive: (String, Boolean) -> Unit,
    onCreatePortalCredential: (role: PortalRole, key: String, user: String, pass: String, label: String) -> Unit,
    onRevokeCredential: (String) -> Unit,
    onCreateCoupon: (code: String, desc: String, type: DiscountType, value: Int, minOrder: Int, maxDisc: Int) -> Unit,
    onToggleCouponActive: (String, Boolean) -> Unit,
    onDeleteCoupon: (String) -> Unit,
    onUpdateCustomQr: (Uri?, String) -> Unit,
    onResetQrDefault: (String) -> Unit,
    onShowToast: (String) -> Unit
) {
    BackHandler(onBack = onBack)

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Orders", "Kitchens", "Catalog", "Access Keys", "Coupons", "Payment QR")

    val grossVolume = uiState.orders.sumOf { it.totalInr }
    val activeKitchens = uiState.kitchens.count { it.isActive }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.statusBarsPadding()
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                            }
                            Icon(Icons.Rounded.AdminPanelSettings, contentDescription = null, tint = CoralPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Owner Console", style = MaterialTheme.typography.titleLarge)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = onForgetSavedLogin) {
                                Text("Forget", style = MaterialTheme.typography.labelSmall)
                            }
                            IconButton(onClick = onLockPortal) {
                                Icon(Icons.Rounded.Lock, contentDescription = "Lock console")
                            }
                        }
                    }

                    ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 12.dp) {
                        tabs.forEachIndexed { idx, label ->
                            Tab(
                                selected = selectedTab == idx,
                                onClick = { selectedTab = idx },
                                text = { Text(label, fontWeight = FontWeight.SemiBold) }
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 4-Card Platform KPI Strip
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AnimatedKpiCard(
                            title = "Gross Volume",
                            value = "₹$grossVolume",
                            subtitle = "Platform revenue",
                            icon = Icons.Rounded.Payments,
                            accentColor = EmeraldSuccess,
                            modifier = Modifier
                                .weight(1f)
                                .height(108.dp)
                        )
                        AnimatedKpiCard(
                            title = "Total Orders",
                            value = "${uiState.orders.size}",
                            subtitle = "All channels",
                            icon = Icons.Rounded.ReceiptLong,
                            accentColor = CoralPrimary,
                            modifier = Modifier
                                .weight(1f)
                                .height(108.dp)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AnimatedKpiCard(
                            title = "Active Kitchens",
                            value = "$activeKitchens/${uiState.kitchens.size}",
                            subtitle = "Partner hubs",
                            icon = Icons.Rounded.Storefront,
                            accentColor = SkyInfo,
                            modifier = Modifier
                                .weight(1f)
                                .height(108.dp)
                        )
                        AnimatedKpiCard(
                            title = "Portal Keys",
                            value = "${uiState.credentials.size}",
                            subtitle = "Issued access keys",
                            icon = Icons.Rounded.Key,
                            accentColor = AmberWarning,
                            modifier = Modifier
                                .weight(1f)
                                .height(108.dp)
                        )
                    }
                }
            }

            when (selectedTab) {
                0 -> {
                    item {
                        Text("Per-Shop Revenue Breakdown", style = MaterialTheme.typography.titleMedium)
                    }
                    val shopTotals = uiState.kitchens.map { kitchen ->
                        val rev = uiState.orders.sumOf { order ->
                            order.items.filter { it.shopName == kitchen.name }.sumOf { it.priceInr * it.quantity }
                        }
                        kitchen.name to rev
                    }
                    items(shopTotals) { (shop, rev) ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(shop, style = MaterialTheme.typography.titleSmall)
                                Text("₹$rev", style = MonoTabularStyle, color = EmeraldSuccess)
                            }
                        }
                    }
                }

                1 -> {
                    item {
                        OwnerOrdersSection(uiState = uiState, onTrackOrder = onTrackOrder)
                    }
                }

                2 -> {
                    item {
                        OnboardKitchenCard(onAddKitchen = onAddKitchen)
                    }
                    items(uiState.kitchens, key = { it.id }) { kitchen ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(kitchen.name, style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        text = "${kitchen.cuisineTag} • ${kitchen.prepTimeRange}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = kitchen.isActive,
                                    onCheckedChange = { onToggleKitchenActive(kitchen.id, it) }
                                )
                            }
                        }
                    }
                }

                3 -> {
                    items(uiState.dishes, key = { it.id }) { dish ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(dish.name, style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        text = "${dish.shopName} • ${dish.category}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text("₹${dish.priceInr}", style = MonoTabularStyle, color = CoralPrimary)
                            }
                        }
                    }
                }

                4 -> {
                    item {
                        PortalKeysManagerSection(
                            uiState = uiState,
                            onCreateCredential = onCreatePortalCredential,
                            onRevokeCredential = onRevokeCredential,
                            onShowToast = onShowToast
                        )
                    }
                }

                5 -> {
                    item {
                        CouponsManagerSection(
                            uiState = uiState,
                            onCreateCoupon = onCreateCoupon,
                            onToggleActive = onToggleCouponActive,
                            onDeleteCoupon = onDeleteCoupon
                        )
                    }
                }

                6 -> {
                    item {
                        OwnerQrSettingsSection(
                            uiState = uiState,
                            onUpdateCustomQr = onUpdateCustomQr,
                            onResetDefault = onResetQrDefault
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OwnerOrdersSection(
    uiState: WingWingUiState,
    onTrackOrder: (String) -> Unit
) {
    var search by remember { mutableStateOf("") }
    val filtered = remember(uiState.orders, search) {
        if (search.isBlank()) uiState.orders
        else uiState.orders.filter {
            it.id.contains(search, ignoreCase = true) ||
                it.customerName.contains(search, ignoreCase = true) ||
                it.deliveryAddress.contains(search, ignoreCase = true)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            label = { Text("Search platform orders") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        if (filtered.isEmpty()) {
            Text(
                text = "No platform orders match your filter.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            filtered.forEach { order ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${order.id} • ₹${order.totalInr}", style = MonoTabularStyle)
                            OrderStatusPill(status = order.statusEnum)
                        }
                        Text(
                            text = "${order.customerName} (${order.customerPhone}) • ${formatEpochTime(order.createdAtEpochMs)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (order.discountInr > 0) {
                            Text(
                                text = "Coupon ${order.couponCode} saved ₹${order.discountInr}",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldSuccess
                            )
                        }
                        if (order.rating != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                StarRatingDisplay(rating = order.rating)
                                if (!order.feedbackComment.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(order.feedbackComment, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                        OutlinedButton(onClick = { onTrackOrder(order.id) }) {
                            Text("Track Live")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardKitchenCard(
    onAddKitchen: (name: String, cuisine: String, prepRange: String, address: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var cuisine by remember { mutableStateOf("") }
    var prepRange by remember { mutableStateOf("20–25 min") }
    var address by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Onboard Partner Kitchen", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Kitchen Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = cuisine,
                onValueChange = { cuisine = it },
                label = { Text("Cuisine Specialty") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = prepRange,
                    onValueChange = { prepRange = it },
                    label = { Text("Prep Time") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Hub Address") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onAddKitchen(name, cuisine, prepRange, address)
                        name = ""
                        cuisine = ""
                        address = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Kitchen")
            }
        }
    }
}

@Composable
private fun PortalKeysManagerSection(
    uiState: WingWingUiState,
    onCreateCredential: (role: PortalRole, key: String, user: String, pass: String, label: String) -> Unit,
    onRevokeCredential: (String) -> Unit,
    onShowToast: (String) -> Unit
) {
    val clipboard = LocalClipboardManager.current
    var role by remember { mutableStateOf(PortalRole.RESTAURANT) }
    var portalKey by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var label by remember { mutableStateOf("") }
    var filterRole by remember { mutableStateOf<PortalRole?>(null) }
    var revealedIds by remember { mutableStateOf(setOf<String>()) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Issue New Portal Access Key", style = MaterialTheme.typography.titleMedium)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PortalRole.entries.forEach { r ->
                        FilterChip(
                            selected = role == r,
                            onClick = { role = r },
                            label = { Text(r.label) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = portalKey,
                        onValueChange = { portalKey = it.uppercase() },
                        label = { Text("Portal Key") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedButton(
                        onClick = {
                            val prefix = role.label.take(3).uppercase()
                            portalKey = "$prefix${Random.nextInt(1000, 9999)}"
                        },
                        modifier = Modifier.height(52.dp)
                    ) {
                        Icon(Icons.Rounded.Casino, contentDescription = "Generate key", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Generate")
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it.uppercase() },
                        label = { Text("Username") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Optional Label (e.g. Night Shift Rider)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        if (portalKey.isNotBlank() && username.isNotBlank() && password.isNotBlank()) {
                            onCreateCredential(role, portalKey, username, password, label)
                            portalKey = ""
                            username = ""
                            password = ""
                            label = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Create Portal Key", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Role Filter
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = filterRole == null,
                onClick = { filterRole = null },
                label = { Text("All Keys") }
            )
            PortalRole.entries.forEach { r ->
                FilterChip(
                    selected = filterRole == r,
                    onClick = { filterRole = r },
                    label = { Text(r.label) }
                )
            }
        }

        val visibleCreds = uiState.credentials.filter { filterRole == null || it.roleEnum == filterRole }
        visibleCreds.forEach { cred ->
            val revealed = revealedIds.contains(cred.id)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("${cred.roleEnum.label} • ${cred.label}", style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = "Key: ${cred.portalKey}  |  User: ${cred.username}",
                                style = MonoTabularStyle.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize),
                                color = CoralPrimary
                            )
                            Text(
                                text = "Pass: ${if (revealed) cred.password else "••••••"}",
                                style = MonoTabularStyle.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize)
                            )
                        }
                        Row {
                            IconButton(
                                onClick = {
                                    revealedIds = if (revealed) revealedIds - cred.id else revealedIds + cred.id
                                }
                            ) {
                                Icon(
                                    imageVector = if (revealed) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                    contentDescription = "Reveal password"
                                )
                            }
                            IconButton(
                                onClick = {
                                    clipboard.setText(
                                        AnnotatedString(
                                            "${cred.roleEnum.label} Portal\nKey: ${cred.portalKey}\nUser: ${cred.username}\nPass: ${cred.password}"
                                        )
                                    )
                                    onShowToast("Copied ${cred.portalKey} credentials")
                                }
                            ) {
                                Icon(Icons.Rounded.ContentCopy, contentDescription = "Copy credentials")
                            }
                            if (!cred.isDefault) {
                                IconButton(onClick = { onRevokeCredential(cred.id) }) {
                                    Icon(
                                        Icons.Rounded.DeleteOutline,
                                        contentDescription = "Revoke key",
                                        tint = MaterialTheme.colorScheme.error
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

@Composable
private fun CouponsManagerSection(
    uiState: WingWingUiState,
    onCreateCoupon: (code: String, desc: String, type: DiscountType, value: Int, minOrder: Int, maxDisc: Int) -> Unit,
    onToggleActive: (String, Boolean) -> Unit,
    onDeleteCoupon: (String) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var discountType by remember { mutableStateOf(DiscountType.FLAT) }
    var valueText by remember { mutableStateOf("") }
    var minOrderText by remember { mutableStateOf("199") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Create Promo Coupon", style = MaterialTheme.typography.titleMedium)

                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it.uppercase() },
                    label = { Text("Coupon Code (e.g. FEAST75)") },
                    leadingIcon = { Icon(Icons.Rounded.LocalOffer, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DiscountType.entries.forEach { t ->
                        FilterChip(
                            selected = discountType == t,
                            onClick = { discountType = t },
                            label = { Text(t.label) }
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = valueText,
                        onValueChange = { valueText = it.filter(Char::isDigit) },
                        label = { Text(if (discountType == DiscountType.FLAT) "Discount ₹" else "Discount %") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = minOrderText,
                        onValueChange = { minOrderText = it.filter(Char::isDigit) },
                        label = { Text("Min Order ₹") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Offer Description") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        val valNum = valueText.toIntOrNull() ?: 0
                        val minNum = minOrderText.toIntOrNull() ?: 0
                        if (code.isNotBlank() && valNum > 0) {
                            onCreateCoupon(
                                code,
                                description.ifBlank {
                                    if (discountType == DiscountType.FLAT) "Flat ₹$valNum off on ₹$minNum+"
                                    else "$valNum% off on ₹$minNum+"
                                },
                                discountType,
                                valNum,
                                minNum,
                                if (discountType == DiscountType.PERCENT) 150 else valNum
                            )
                            code = ""
                            description = ""
                            valueText = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Coupon", fontWeight = FontWeight.Bold)
                }
            }
        }

        uiState.coupons.forEach { coupon ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(coupon.code, style = MonoTabularStyle, color = CoralPrimary)
                        Text(coupon.description, style = MaterialTheme.typography.bodySmall)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = coupon.isActive,
                            onCheckedChange = { onToggleActive(coupon.code, it) }
                        )
                        IconButton(onClick = { onDeleteCoupon(coupon.code) }) {
                            Icon(
                                Icons.Rounded.DeleteOutline,
                                contentDescription = "Delete coupon",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OwnerQrSettingsSection(
    uiState: WingWingUiState,
    onUpdateCustomQr: (Uri?, String) -> Unit,
    onResetDefault: (String) -> Unit
) {
    var upiLabel by remember(uiState.preferences.paymentUpiLabel) {
        mutableStateOf(uiState.preferences.paymentUpiLabel)
    }

    val qrPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onUpdateCustomQr(uri, upiLabel)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PaymentQrPreview(
            customQrUri = uiState.preferences.customQrUri,
            upiLabel = upiLabel,
            amountInr = 0
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Custom Merchant QR Settings", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = upiLabel,
                    onValueChange = { upiLabel = it },
                    label = { Text("Merchant UPI ID / Label") },
                    leadingIcon = { Icon(Icons.Rounded.QrCode2, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = { onUpdateCustomQr(null, upiLabel) },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save UPI Label", fontWeight = FontWeight.Bold)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            qrPhotoPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Upload Custom QR")
                    }
                    OutlinedButton(
                        onClick = { onResetDefault(upiLabel) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Reset Default")
                    }
                }
            }
        }
    }
}
