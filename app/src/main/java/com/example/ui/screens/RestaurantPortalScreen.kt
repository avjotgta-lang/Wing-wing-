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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Fastfood
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.SoupKitchen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.DishCategory
import com.example.data.model.KitchenEntity
import com.example.data.model.OrderStatus
import com.example.ui.components.AnimatedKpiCard
import com.example.ui.components.DishThumbnail
import com.example.ui.components.InteractiveDeliveryMap
import com.example.ui.components.OrderStatusPill
import com.example.ui.components.formatEpochTime
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.MonoTabularStyle
import com.example.ui.theme.VioletReady
import com.example.ui.viewmodel.WingWingUiState

@Composable
fun RestaurantPortalScreen(
    uiState: WingWingUiState,
    onBack: () -> Unit,
    onLockPortal: () -> Unit,
    onForgetSavedLogin: () -> Unit,
    onUpdateOrderStatus: (String, OrderStatus) -> Unit,
    onTrackOrder: (String) -> Unit,
    onToggleDishAvailability: (String, Boolean) -> Unit,
    onAddDish: (
        name: String,
        kitchen: KitchenEntity,
        category: DishCategory,
        priceInr: Int,
        prepMinutes: Int,
        description: String,
        tag: String,
        pickedImageUri: Uri?
    ) -> Unit
) {
    BackHandler(onBack = onBack)

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var queueFilter by remember { mutableStateOf<OrderStatus?>(null) }

    val newOrdersCount = uiState.orders.count { it.statusEnum == OrderStatus.NEW }
    val prepOrdersCount = uiState.orders.count { it.statusEnum == OrderStatus.PREPARING }
    val readyOrdersCount = uiState.orders.count { it.statusEnum == OrderStatus.READY }
    val activeDishesCount = uiState.dishes.count { it.isAvailable }

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
                            Icon(Icons.Rounded.Restaurant, contentDescription = null, tint = CoralPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Restaurant Portal", style = MaterialTheme.typography.titleLarge)
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
                        listOf("Kitchen Queue", "Hub Map", "Menu Manager").forEachIndexed { idx, title ->
                            Tab(
                                selected = selectedTabIndex == idx,
                                onClick = { selectedTabIndex = idx },
                                text = { Text(title, fontWeight = FontWeight.SemiBold) }
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
            // 4-Column KPI Strip (2x2 clean grid)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AnimatedKpiCard(
                            title = "New Queue",
                            value = "$newOrdersCount",
                            subtitle = "Awaiting accept",
                            icon = Icons.Rounded.NotificationsActive,
                            accentColor = CoralPrimary,
                            modifier = Modifier
                                .weight(1f)
                                .height(110.dp)
                        )
                        AnimatedKpiCard(
                            title = "Preparing",
                            value = "$prepOrdersCount",
                            subtitle = "On the grill",
                            icon = Icons.Rounded.SoupKitchen,
                            accentColor = AmberWarning,
                            modifier = Modifier
                                .weight(1f)
                                .height(110.dp)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AnimatedKpiCard(
                            title = "Ready Pickup",
                            value = "$readyOrdersCount",
                            subtitle = "Waiting rider",
                            icon = Icons.Rounded.CheckCircle,
                            accentColor = VioletReady,
                            modifier = Modifier
                                .weight(1f)
                                .height(110.dp)
                        )
                        AnimatedKpiCard(
                            title = "Active Menu",
                            value = "$activeDishesCount/${uiState.dishes.size}",
                            subtitle = "Dishes live",
                            icon = Icons.Rounded.Fastfood,
                            accentColor = EmeraldSuccess,
                            modifier = Modifier
                                .weight(1f)
                                .height(110.dp)
                        )
                    }
                }
            }

            when (selectedTabIndex) {
                0 -> {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = queueFilter == null,
                                onClick = { queueFilter = null },
                                label = { Text("All (${uiState.orders.size})") }
                            )
                            listOf(OrderStatus.NEW, OrderStatus.PREPARING, OrderStatus.READY).forEach { st ->
                                val count = uiState.orders.count { it.statusEnum == st }
                                FilterChip(
                                    selected = queueFilter == st,
                                    onClick = { queueFilter = st },
                                    label = { Text("${st.label} ($count)") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CoralPrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    val filteredOrders = uiState.orders.filter {
                        queueFilter == null || it.statusEnum == queueFilter
                    }

                    if (filteredOrders.isEmpty()) {
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Rounded.LocalFireDepartment, contentDescription = null, tint = CoralPrimary, modifier = Modifier.size(34.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Kitchen queue is clear", style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        "Incoming customer orders will appear here in real time.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        items(filteredOrders, key = { it.id }) { order ->
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
                                        Column {
                                            Text(
                                                text = "${order.id} • ₹${order.totalInr}",
                                                style = MonoTabularStyle.copy(
                                                    fontSize = MaterialTheme.typography.titleMedium.fontSize
                                                )
                                            )
                                            Text(
                                                text = "${order.customerName} • ${formatEpochTime(order.createdAtEpochMs)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        OrderStatusPill(status = order.statusEnum)
                                    }

                                    order.items.forEach { item ->
                                        Text(
                                            text = "${item.quantity}x ${item.name} (${item.shopName})",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (order.statusEnum == OrderStatus.NEW) {
                                            Button(
                                                onClick = { onUpdateOrderStatus(order.id, OrderStatus.PREPARING) },
                                                colors = ButtonDefaults.buttonColors(containerColor = AmberWarning),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Start Preparing", fontWeight = FontWeight.Bold)
                                            }
                                        } else if (order.statusEnum == OrderStatus.PREPARING) {
                                            Button(
                                                onClick = { onUpdateOrderStatus(order.id, OrderStatus.READY) },
                                                colors = ButtonDefaults.buttonColors(containerColor = VioletReady),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Mark Ready", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        OutlinedButton(onClick = { onTrackOrder(order.id) }) {
                                            Text("Live Map")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    item {
                        val latestOrder = uiState.orders.firstOrNull { it.statusEnum != OrderStatus.DELIVERED }
                        InteractiveDeliveryMap(
                            customerLat = latestOrder?.deliveryLat ?: uiState.preferences.savedDeliveryLat,
                            customerLng = latestOrder?.deliveryLng ?: uiState.preferences.savedDeliveryLng,
                            kitchens = uiState.kitchens,
                            orderStatus = latestOrder?.statusEnum,
                            interactivePinSelection = false,
                            height = 280.dp
                        )
                    }
                }

                2 -> {
                    item {
                        AddDishCard(
                            kitchens = uiState.kitchens,
                            onAddDish = onAddDish
                        )
                    }
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
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                DishThumbnail(
                                    dish = dish,
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(MaterialTheme.shapes.extraSmall)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(dish.name, style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        text = "${dish.shopName} • ₹${dish.priceInr}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Switch(
                                        checked = dish.isAvailable,
                                        onCheckedChange = { onToggleDishAvailability(dish.id, it) }
                                    )
                                    Text(
                                        text = if (dish.isAvailable) "In Stock" else "Paused",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (dish.isAvailable) EmeraldSuccess else MaterialTheme.colorScheme.onSurfaceVariant
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
private fun AddDishCard(
    kitchens: List<KitchenEntity>,
    onAddDish: (
        name: String,
        kitchen: KitchenEntity,
        category: DishCategory,
        priceInr: Int,
        prepMinutes: Int,
        description: String,
        tag: String,
        pickedImageUri: Uri?
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var prepText by remember { mutableStateOf("18") }
    var description by remember { mutableStateOf("") }
    var tag by remember { mutableStateOf("Chef Special") }
    var selectedKitchenIndex by remember { mutableIntStateOf(0) }
    var selectedCategory by remember { mutableStateOf(DishCategory.BURGERS_SIDES) }
    var pickedUri by remember { mutableStateOf<Uri?>(null) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> pickedUri = uri }

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
            Text("Add New Dish", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Dish Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                kitchens.forEachIndexed { idx, kitchen ->
                    FilterChip(
                        selected = selectedKitchenIndex == idx,
                        onClick = { selectedKitchenIndex = idx },
                        label = { Text(kitchen.name) }
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DishCategory.entries.filter { it != DishCategory.ALL }.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat.label) }
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it.filter(Char::isDigit) },
                    label = { Text("Price (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = prepText,
                    onValueChange = { prepText = it.filter(Char::isDigit) },
                    label = { Text("Prep (min)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Rounded.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (pickedUri != null) "Photo Attached" else "Pick Dish Photo")
                }

                Button(
                    onClick = {
                        val kitchen = kitchens.getOrNull(selectedKitchenIndex) ?: return@Button
                        val price = priceText.toIntOrNull() ?: 199
                        val prep = prepText.toIntOrNull() ?: 18
                        if (name.isNotBlank()) {
                            onAddDish(
                                name,
                                kitchen,
                                selectedCategory,
                                price,
                                prep,
                                description.ifBlank { "Freshly prepared at ${kitchen.name}." },
                                tag,
                                pickedUri
                            )
                            name = ""
                            priceText = ""
                            description = ""
                            pickedUri = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("add_dish_submit_btn")
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Publish Dish")
                }
            }
        }
    }
}
