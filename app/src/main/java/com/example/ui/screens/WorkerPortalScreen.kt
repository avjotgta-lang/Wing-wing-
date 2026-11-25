package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeliveryDining
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.OrderStatus
import com.example.ui.components.AnimatedKpiCard
import com.example.ui.components.InteractiveDeliveryMap
import com.example.ui.components.OrderStatusPill
import com.example.ui.components.dialPhoneNumber
import com.example.ui.components.formatEpochTime
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.MonoTabularStyle
import com.example.ui.theme.SkyInfo
import com.example.ui.viewmodel.WingWingUiState
import com.example.util.AlertNotifier

@Composable
fun WorkerPortalScreen(
    uiState: WingWingUiState,
    onBack: () -> Unit,
    onLockPortal: () -> Unit,
    onForgetSavedLogin: () -> Unit,
    onSaveWorkerPhone: (String) -> Unit,
    onSetNotificationsEnabled: (Boolean) -> Unit,
    onRingTestAlert: () -> Unit,
    onMarkNotificationRead: (String) -> Unit,
    onMarkAllNotificationsRead: () -> Unit,
    onClearNotifications: () -> Unit,
    onUpdateOrderStatus: (String, OrderStatus) -> Unit,
    onTrackOrder: (String) -> Unit
) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var workerPhoneInput by remember(uiState.preferences.workerPhone) {
        mutableStateOf(uiState.preferences.workerPhone)
    }
    var hasNotifPerm by remember { mutableStateOf(AlertNotifier.hasNotificationPermission(context)) }

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotifPerm = granted
        if (granted) onSetNotificationsEnabled(true)
    }

    val activeDispatchOrders = remember(uiState.orders) {
        uiState.orders.filter { it.statusEnum != OrderStatus.DELIVERED }
    }
    val completedOrders = remember(uiState.orders) {
        uiState.orders.filter { it.statusEnum == OrderStatus.DELIVERED }
    }
    val payoutTotal = completedOrders.size * 45

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
                            Icon(Icons.Rounded.DeliveryDining, contentDescription = null, tint = CoralPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Worker Portal", style = MaterialTheme.typography.titleLarge)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = onForgetSavedLogin) {
                                Text("Forget", style = MaterialTheme.typography.labelSmall)
                            }
                            IconButton(onClick = onLockPortal) {
                                Icon(Icons.Rounded.Lock, contentDescription = "Lock portal")
                            }
                        }
                    }

                    ScrollableTabRow(selectedTabIndex = selectedTabIndex, edgePadding = 12.dp) {
                        Tab(
                            selected = selectedTabIndex == 0,
                            onClick = { selectedTabIndex = 0 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (uiState.unreadWorkerNotificationsCount > 0) {
                                        BadgedBox(
                                            badge = {
                                                Badge(containerColor = CoralPrimary) {
                                                    Text("${uiState.unreadWorkerNotificationsCount}")
                                                }
                                            }
                                        ) {
                                            Text("Notifications  ", fontWeight = FontWeight.SemiBold)
                                        }
                                    } else {
                                        Text("Notifications", fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        )
                        Tab(
                            selected = selectedTabIndex == 1,
                            onClick = { selectedTabIndex = 1 },
                            text = { Text("Dispatch & Map (${activeDispatchOrders.size})", fontWeight = FontWeight.SemiBold) }
                        )
                        Tab(
                            selected = selectedTabIndex == 2,
                            onClick = { selectedTabIndex = 2 },
                            text = { Text("Completed (${completedOrders.size})", fontWeight = FontWeight.SemiBold) }
                        )
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
            // Rider Mobile Number & Alert Controls
            item {
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Rider Profile & Mobile Alerts", style = MaterialTheme.typography.titleMedium)
                            Switch(
                                checked = uiState.preferences.workerNotificationsEnabled,
                                onCheckedChange = onSetNotificationsEnabled
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = workerPhoneInput,
                                onValueChange = { workerPhoneInput = it },
                                label = { Text("Rider Mobile Number") },
                                leadingIcon = { Icon(Icons.Rounded.Phone, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("worker_phone_input")
                            )
                            Button(
                                onClick = { onSaveWorkerPhone(workerPhoneInput) },
                                colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                                modifier = Modifier.height(52.dp)
                            ) {
                                Text("Save")
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (!hasNotifPerm && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                OutlinedButton(
                                    onClick = {
                                        notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Rounded.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Enable Alerts")
                                }
                            }
                            OutlinedButton(
                                onClick = onRingTestAlert,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("worker_ring_alert_btn")
                            ) {
                                Icon(Icons.Rounded.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Ring Alert")
                            }
                        }
                    }
                }
            }

            // 2-Card Summary KPI
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AnimatedKpiCard(
                        title = "Active Queue",
                        value = "${activeDispatchOrders.size}",
                        subtitle = "Orders to deliver",
                        icon = Icons.Rounded.DeliveryDining,
                        accentColor = SkyInfo,
                        modifier = Modifier
                            .weight(1f)
                            .height(106.dp)
                    )
                    AnimatedKpiCard(
                        title = "Trip Payouts",
                        value = "₹$payoutTotal",
                        subtitle = "${completedOrders.size} trips @ ₹45",
                        icon = Icons.Rounded.Payments,
                        accentColor = EmeraldSuccess,
                        modifier = Modifier
                            .weight(1f)
                            .height(106.dp)
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Dispatch Alerts (${uiState.workerNotifications.size})",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Row {
                                TextButton(onClick = onMarkAllNotificationsRead) {
                                    Icon(Icons.Rounded.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Read All")
                                }
                                TextButton(onClick = onClearNotifications) {
                                    Text("Clear")
                                }
                            }
                        }
                    }

                    if (uiState.workerNotifications.isEmpty()) {
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Rounded.NotificationsActive, contentDescription = null, tint = CoralPrimary, modifier = Modifier.size(34.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("No dispatch notifications yet", style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        "Whenever a customer places an order, you'll receive an instant sound and mobile dispatch alert here.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        items(uiState.workerNotifications, key = { it.id }) { notif ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium,
                                color = if (!notif.isRead) CoralPrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
                                border = BorderStroke(
                                    1.dp,
                                    if (!notif.isRead) CoralPrimary else MaterialTheme.colorScheme.outline
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = notif.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = CoralPrimary
                                        )
                                        Text(
                                            text = formatEpochTime(notif.createdAtEpochMs),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(notif.message, style = MaterialTheme.typography.bodyMedium)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                onMarkNotificationRead(notif.id)
                                                onUpdateOrderStatus(notif.orderId, OrderStatus.PICKED_UP)
                                                onTrackOrder(notif.orderId)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Accept & Track", fontWeight = FontWeight.Bold)
                                        }
                                        if (notif.customerPhone.isNotBlank()) {
                                            OutlinedButton(
                                                onClick = { dialPhoneNumber(context, notif.customerPhone) }
                                            ) {
                                                Icon(Icons.Rounded.Call, contentDescription = "Call customer", modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Call")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    val featuredOrder = activeDispatchOrders.firstOrNull()
                    item {
                        InteractiveDeliveryMap(
                            customerLat = featuredOrder?.deliveryLat ?: uiState.preferences.savedDeliveryLat,
                            customerLng = featuredOrder?.deliveryLng ?: uiState.preferences.savedDeliveryLng,
                            kitchens = uiState.kitchens,
                            orderStatus = featuredOrder?.statusEnum,
                            interactivePinSelection = false,
                            height = 210.dp
                        )
                    }

                    if (activeDispatchOrders.isEmpty()) {
                        item {
                            Text(
                                text = "All assigned pickups are completed.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(activeDispatchOrders, key = { it.id }) { order ->
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
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${order.id} • ₹${order.totalInr}",
                                            style = MonoTabularStyle.copy(
                                                fontSize = MaterialTheme.typography.titleMedium.fontSize
                                            )
                                        )
                                        OrderStatusPill(status = order.statusEnum)
                                    }
                                    Text(
                                        text = "Dropoff: ${order.deliveryAddress}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "Customer: ${order.customerName} (${order.customerPhone})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (order.statusEnum != OrderStatus.PICKED_UP) {
                                            Button(
                                                onClick = { onUpdateOrderStatus(order.id, OrderStatus.PICKED_UP) },
                                                colors = ButtonDefaults.buttonColors(containerColor = SkyInfo),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Mark Picked Up", fontWeight = FontWeight.Bold)
                                            }
                                        } else {
                                            Button(
                                                onClick = { onUpdateOrderStatus(order.id, OrderStatus.DELIVERED) },
                                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Mark Delivered", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        OutlinedButton(
                                            onClick = { dialPhoneNumber(context, order.customerPhone) }
                                        ) {
                                            Icon(Icons.Rounded.Call, contentDescription = "Call customer", modifier = Modifier.size(16.dp))
                                        }
                                        OutlinedButton(onClick = { onTrackOrder(order.id) }) {
                                            Text("Route")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    if (completedOrders.isEmpty()) {
                        item {
                            Text(
                                text = "No completed trips yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(completedOrders, key = { it.id }) { order ->
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
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(order.id, style = MonoTabularStyle)
                                        }
                                        Text(
                                            text = order.deliveryAddress,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "+₹45",
                                        style = MonoTabularStyle,
                                        color = EmeraldSuccess
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
