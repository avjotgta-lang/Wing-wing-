package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeliveryDining
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.RateReview
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.RestaurantMenu
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.DishCategory
import com.example.data.model.DishEntity
import com.example.data.model.OrderEntity
import com.example.data.model.OrderStatus
import com.example.data.model.PortalRole
import com.example.data.model.StorefrontTab
import com.example.ui.components.DishThumbnail
import com.example.ui.components.OrderProgressStepper
import com.example.ui.components.OrderStatusPill
import com.example.ui.components.StarRatingDisplay
import com.example.ui.components.WingWingWordmark
import com.example.ui.components.formatEpochTime
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.MonoTabularStyle
import com.example.ui.viewmodel.WingWingUiState

@Composable
fun StorefrontScreen(
    uiState: WingWingUiState,
    onSelectTab: (StorefrontTab) -> Unit,
    onSearchChange: (String) -> Unit,
    onCategorySelect: (DishCategory) -> Unit,
    onAddToCart: (DishEntity) -> Unit,
    onDecrementFromCart: (String) -> Unit,
    onOpenCart: () -> Unit,
    onTrackOrder: (String) -> Unit,
    onReorder: (OrderEntity) -> Unit,
    onSubmitFeedback: (orderId: String?, rating: Int, comment: String) -> Unit,
    onToggleTheme: () -> Unit,
    onSignOut: () -> Unit,
    onOpenPortal: (PortalRole) -> Unit
) {
    var showPortalMenu by remember { mutableStateOf(false) }

    val activeOrders = remember(uiState.orders) {
        uiState.orders.filter { it.statusEnum != OrderStatus.DELIVERED }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 760.dp

        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        WingWingWordmark(
                            subtitle = uiState.preferences.currentCustomerName.ifBlank { "Guest Storefront" },
                            modifier = Modifier.weight(1f)
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Bag Pill Button
                            Button(
                                onClick = onOpenCart,
                                colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                                shape = CircleShape,
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                modifier = Modifier.testTag("storefront_bag_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ShoppingBag,
                                    contentDescription = "Open bag",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Bag • ${uiState.cartCount}",
                                    style = MonoTabularStyle.copy(
                                        fontSize = MaterialTheme.typography.labelLarge.fontSize
                                    )
                                )
                            }

                            IconButton(onClick = onToggleTheme) {
                                Icon(
                                    imageVector = if (uiState.preferences.isDarkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                                    contentDescription = "Toggle theme"
                                )
                            }

                            Box {
                                IconButton(
                                    onClick = { showPortalMenu = true },
                                    modifier = Modifier.testTag("partner_portals_menu_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.MoreVert,
                                        contentDescription = "Partner portals & account"
                                    )
                                }
                                DropdownMenu(
                                    expanded = showPortalMenu,
                                    onDismissRequest = { showPortalMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Restaurant Portal") },
                                        leadingIcon = { Icon(Icons.Rounded.Restaurant, contentDescription = null) },
                                        onClick = {
                                            showPortalMenu = false
                                            onOpenPortal(PortalRole.RESTAURANT)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Worker Portal") },
                                        leadingIcon = { Icon(Icons.Rounded.DeliveryDining, contentDescription = null) },
                                        onClick = {
                                            showPortalMenu = false
                                            onOpenPortal(PortalRole.WORKER)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Owner Console") },
                                        leadingIcon = { Icon(Icons.Rounded.AdminPanelSettings, contentDescription = null) },
                                        onClick = {
                                            showPortalMenu = false
                                            onOpenPortal(PortalRole.OWNER)
                                        }
                                    )
                                    HorizontalDivider()
                                    DropdownMenuItem(
                                        text = { Text("Sign Out") },
                                        leadingIcon = { Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null) },
                                        onClick = {
                                            showPortalMenu = false
                                            onSignOut()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            bottomBar = {
                if (!useRail) {
                    NavigationBar(
                        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        StorefrontTab.entries.forEach { tab ->
                            val selected = uiState.selectedTab == tab
                            NavigationBarItem(
                                selected = selected,
                                onClick = { onSelectTab(tab) },
                                icon = {
                                    TabIconWithBadge(
                                        tab = tab,
                                        liveCount = activeOrders.size
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.label,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useRail) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))
                        StorefrontTab.entries.forEach { tab ->
                            NavigationRailItem(
                                selected = uiState.selectedTab == tab,
                                onClick = { onSelectTab(tab) },
                                icon = {
                                    TabIconWithBadge(
                                        tab = tab,
                                        liveCount = activeOrders.size
                                    )
                                },
                                label = { Text(tab.label) }
                            )
                        }
                    }
                }

                AnimatedContent(
                    targetState = uiState.selectedTab,
                    label = "storefront_tab_anim",
                    modifier = Modifier.weight(1f)
                ) { tab ->
                    when (tab) {
                        StorefrontTab.MENU -> MenuCatalogTab(
                            uiState = uiState,
                            onSearchChange = onSearchChange,
                            onCategorySelect = onCategorySelect,
                            onAddToCart = onAddToCart,
                            onDecrementFromCart = onDecrementFromCart,
                            onOpenHistory = { onSelectTab(StorefrontTab.ORDER_HISTORY) }
                        )
                        StorefrontTab.LIVE_ORDERS -> LiveOrdersTab(
                            activeOrders = activeOrders,
                            onTrackOrder = onTrackOrder,
                            onExploreMenu = { onSelectTab(StorefrontTab.MENU) }
                        )
                        StorefrontTab.ORDER_HISTORY -> OrderHistoryTab(
                            orders = uiState.orders,
                            onTrackOrder = onTrackOrder,
                            onReorder = { order ->
                                onReorder(order)
                                onOpenCart()
                            },
                            onSubmitOrderFeedback = { orderId, rating, comment ->
                                onSubmitFeedback(orderId, rating, comment)
                            }
                        )
                        StorefrontTab.FEEDBACK -> CustomerFeedbackTab(
                            uiState = uiState,
                            onSubmitFeedback = onSubmitFeedback
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TabIconWithBadge(tab: StorefrontTab, liveCount: Int) {
    val icon = when (tab) {
        StorefrontTab.MENU -> Icons.Rounded.RestaurantMenu
        StorefrontTab.LIVE_ORDERS -> Icons.Rounded.LocalShipping
        StorefrontTab.ORDER_HISTORY -> Icons.Rounded.History
        StorefrontTab.FEEDBACK -> Icons.Rounded.RateReview
    }
    if (tab == StorefrontTab.LIVE_ORDERS && liveCount > 0) {
        BadgedBox(
            badge = {
                Badge(containerColor = CoralPrimary) {
                    Text("$liveCount")
                }
            }
        ) {
            Icon(icon, contentDescription = tab.label)
        }
    } else {
        Icon(icon, contentDescription = tab.label)
    }
}

@Composable
private fun MenuCatalogTab(
    uiState: WingWingUiState,
    onSearchChange: (String) -> Unit,
    onCategorySelect: (DishCategory) -> Unit,
    onAddToCart: (DishEntity) -> Unit,
    onDecrementFromCart: (String) -> Unit,
    onOpenHistory: () -> Unit
) {
    val activeCoupons = remember(uiState.coupons) { uiState.coupons.filter { it.isActive } }
    val filteredDishes = remember(uiState.dishes, uiState.searchQuery, uiState.selectedCategory) {
        uiState.dishes.filter { dish ->
            val matchesCat = uiState.selectedCategory == DishCategory.ALL ||
                dish.category.equals(uiState.selectedCategory.label, ignoreCase = true)
            val q = uiState.searchQuery.trim()
            val matchesSearch = q.isBlank() ||
                dish.name.contains(q, ignoreCase = true) ||
                dish.shopName.contains(q, ignoreCase = true) ||
                dish.description.contains(q, ignoreCase = true)
            matchesCat && matchesSearch
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Editorial Hero Card
        item {
            Surface(
                shape = MaterialTheme.shapes.large,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(172.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_spread),
                        contentDescription = "WingWing fresh kitchens",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.Black.copy(alpha = 0.85f),
                                        Color.Black.copy(alpha = 0.35f)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Hot off the grill",
                            style = MaterialTheme.typography.labelMedium,
                            color = CoralPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Order fresh wings, smash burgers & wood-fired pies",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = onOpenHistory,
                                shape = CircleShape,
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.7f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                            ) {
                                Icon(Icons.Rounded.History, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Order History", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
        }

        // Dynamic Active Coupons Strip
        if (activeCoupons.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    activeCoupons.forEach { coupon ->
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = CoralPrimary.copy(alpha = 0.11f),
                            border = BorderStroke(1.dp, CoralPrimary.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.LocalOffer,
                                    contentDescription = null,
                                    tint = CoralPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = coupon.code,
                                        style = MonoTabularStyle.copy(
                                            fontSize = MaterialTheme.typography.labelMedium.fontSize,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = CoralPrimary
                                    )
                                    Text(
                                        text = coupon.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search dishes, kitchens, or flavours...") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("menu_search_input")
            )
        }

        // Category Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DishCategory.entries.forEach { category ->
                    val selected = uiState.selectedCategory == category
                    FilterChip(
                        selected = selected,
                        onClick = { onCategorySelect(category) },
                        label = { Text(category.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CoralPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        if (filteredDishes.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No matching dishes found", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Try clearing your search filter or picking another category.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredDishes, key = { it.id }) { dish ->
                val qty = uiState.cartQuantities[dish.id] ?: 0
                DishMenuCard(
                    dish = dish,
                    quantity = qty,
                    onAdd = { onAddToCart(dish) },
                    onDecrement = { onDecrementFromCart(dish.id) }
                )
            }
        }
    }
}

@Composable
private fun DishMenuCard(
    dish: DishEntity,
    quantity: Int,
    onAdd: () -> Unit,
    onDecrement: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(104.dp)) {
                DishThumbnail(
                    dish = dish,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(MaterialTheme.shapes.small)
                )
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp),
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.72f)
                ) {
                    Text(
                        text = dish.tag,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dish.shopName,
                    style = MaterialTheme.typography.labelSmall,
                    color = CoralPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = dish.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = dish.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "₹${dish.priceInr}",
                            style = MonoTabularStyle.copy(
                                fontSize = MaterialTheme.typography.titleMedium.fontSize
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${dish.prepMinutes} min",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (!dish.isAvailable) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "Paused",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    } else if (quantity == 0) {
                        Button(
                            onClick = onAdd,
                            colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                            shape = CircleShape,
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("add_dish_${dish.id}")
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Surface(
                            shape = CircleShape,
                            color = CoralPrimary.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, CoralPrimary)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = onDecrement,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.Remove,
                                        contentDescription = "Remove one",
                                        tint = CoralPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text(
                                    text = "$quantity",
                                    style = MonoTabularStyle,
                                    color = CoralPrimary,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                                IconButton(
                                    onClick = onAdd,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.Add,
                                        contentDescription = "Add one",
                                        tint = CoralPrimary,
                                        modifier = Modifier.size(18.dp)
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
private fun LiveOrdersTab(
    activeOrders: List<OrderEntity>,
    onTrackOrder: (String) -> Unit,
    onExploreMenu: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Active Deliveries", style = MaterialTheme.typography.headlineSmall)
        }
        if (activeOrders.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocalShipping,
                            contentDescription = null,
                            tint = CoralPrimary,
                            modifier = Modifier.size(38.dp)
                        )
                        Text("No live orders right now", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Place an order from the menu to watch live kitchen prep and rider tracking here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = onExploreMenu,
                            colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
                        ) {
                            Text("Explore Menu")
                        }
                    }
                }
            }
        } else {
            items(activeOrders, key = { it.id }) { order ->
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
                            Column {
                                Text(
                                    text = "${order.id} • ₹${order.totalInr}",
                                    style = MonoTabularStyle.copy(
                                        fontSize = MaterialTheme.typography.titleMedium.fontSize
                                    )
                                )
                                Text(
                                    text = order.deliveryAddress,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            OrderStatusPill(status = order.statusEnum)
                        }
                        OrderProgressStepper(currentStatus = order.statusEnum)
                        Button(
                            onClick = { onTrackOrder(order.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Track Live on Map", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderHistoryTab(
    orders: List<OrderEntity>,
    onTrackOrder: (String) -> Unit,
    onReorder: (OrderEntity) -> Unit,
    onSubmitOrderFeedback: (orderId: String, rating: Int, comment: String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Order History", style = MaterialTheme.typography.headlineSmall)
        }
        if (orders.isEmpty()) {
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
                        Icon(
                            imageVector = Icons.Rounded.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No past orders yet", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Your completed and active orders will appear here for one-tap re-order.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(orders, key = { it.id }) { order ->
                OrderHistoryCard(
                    order = order,
                    onTrack = { onTrackOrder(order.id) },
                    onReorder = { onReorder(order) },
                    onSubmitFeedback = { rating, comment ->
                        onSubmitOrderFeedback(order.id, rating, comment)
                    }
                )
            }
        }
    }
}

@Composable
private fun OrderHistoryCard(
    order: OrderEntity,
    onTrack: () -> Unit,
    onReorder: () -> Unit,
    onSubmitFeedback: (Int, String) -> Unit
) {
    var inlineRating by remember(order.id, order.rating) { mutableIntStateOf(order.rating ?: 5) }
    var inlineComment by remember(order.id, order.feedbackComment) { mutableStateOf(order.feedbackComment ?: "") }
    var showRateBox by remember { mutableStateOf(false) }

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
                        text = order.id,
                        style = MonoTabularStyle.copy(
                            fontSize = MaterialTheme.typography.titleMedium.fontSize
                        )
                    )
                    Text(
                        text = formatEpochTime(order.createdAtEpochMs),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OrderStatusPill(status = order.statusEnum)
            }

            order.items.forEach { item ->
                Text(
                    text = "${item.quantity}x ${item.name} • ₹${item.priceInr * item.quantity}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = order.paymentMethodEnum.label +
                        if (order.discountInr > 0) " • Saved ₹${order.discountInr}" else "",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (order.discountInr > 0) EmeraldSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Total ₹${order.totalInr}",
                    style = MonoTabularStyle,
                    color = CoralPrimary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onReorder,
                    colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Rounded.Replay, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Re-order")
                }
                OutlinedButton(
                    onClick = onTrack,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Track Status")
                }
            }

            if (order.rating != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Your Rating: ", style = MaterialTheme.typography.labelSmall)
                    StarRatingDisplay(rating = order.rating)
                    if (!order.feedbackComment.isNullOrBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "\"${order.feedbackComment}\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            } else {
                TextButton(onClick = { showRateBox = !showRateBox }) {
                    Icon(Icons.Rounded.Star, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (showRateBox) "Hide Rating" else "Rate this Order")
                }
                if (showRateBox) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (star in 1..5) {
                            IconButton(onClick = { inlineRating = star }) {
                                Icon(
                                    imageVector = Icons.Rounded.Star,
                                    contentDescription = "$star stars",
                                    tint = if (star <= inlineRating) AmberWarning else MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = inlineComment,
                        onValueChange = { inlineComment = it },
                        label = { Text("Share how the meal tasted...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            onSubmitFeedback(inlineRating, inlineComment)
                            showRateBox = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
                    ) {
                        Text("Submit Review")
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomerFeedbackTab(
    uiState: WingWingUiState,
    onSubmitFeedback: (orderId: String?, rating: Int, comment: String) -> Unit
) {
    var selectedOrderId by remember { mutableStateOf<String?>(null) }
    var rating by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Customer Feedback", style = MaterialTheme.typography.headlineSmall)
        }
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Rate your WingWing experience", style = MaterialTheme.typography.titleMedium)

                    if (uiState.orders.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedOrderId == null,
                                onClick = { selectedOrderId = null },
                                label = { Text("General") }
                            )
                            uiState.orders.take(6).forEach { order ->
                                FilterChip(
                                    selected = selectedOrderId == order.id,
                                    onClick = { selectedOrderId = order.id },
                                    label = { Text(order.id) }
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (star in 1..5) {
                            IconButton(onClick = { rating = star }) {
                                Icon(
                                    imageVector = Icons.Rounded.Star,
                                    contentDescription = "$star stars",
                                    tint = if (star <= rating) AmberWarning else MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        label = { Text("Write your review") },
                        placeholder = { Text("Crispy wings, fast delivery, packaging...") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            onSubmitFeedback(selectedOrderId, rating, comment.ifBlank { "Great experience!" })
                            comment = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Submit Feedback", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Text(
                text = "Recent Reviews (${uiState.feedbacks.size})",
                style = MaterialTheme.typography.titleMedium
            )
        }

        if (uiState.feedbacks.isEmpty()) {
            item {
                Text(
                    text = "Be the first to leave feedback on WingWing!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(uiState.feedbacks, key = { it.id }) { fb ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = fb.customerName + (fb.orderId?.let { " • $it" } ?: ""),
                                style = MaterialTheme.typography.titleSmall
                            )
                            StarRatingDisplay(rating = fb.rating)
                        }
                        Text(text = fb.comment, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = formatEpochTime(fb.createdAtEpochMs),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
