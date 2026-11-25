package com.example.ui.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.GpsFixed
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.OrderEntity
import com.example.data.model.PaymentMethod
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.MonoTabularStyle
import com.example.ui.viewmodel.WingWingUiState
import com.example.util.LocationHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartCheckoutSheet(
    uiState: WingWingUiState,
    discountInr: Int,
    onDismiss: () -> Unit,
    onIncrement: (String) -> Unit,
    onDecrement: (String) -> Unit,
    onRemove: (String) -> Unit,
    onApplyCoupon: (String) -> Unit,
    onRemoveCoupon: () -> Unit,
    onDetectGps: ((String, Double, Double) -> Unit) -> Unit,
    onReverseGeocode: (Double, Double, (String) -> Unit) -> Unit,
    onPlaceOrder: (phone: String, address: String, lat: Double, lng: Double, payment: PaymentMethod) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    var phone by remember { mutableStateOf(uiState.preferences.savedCustomerPhone) }
    var address by remember { mutableStateOf(uiState.preferences.savedDeliveryAddress) }
    var lat by remember { mutableDoubleStateOf(uiState.preferences.savedDeliveryLat) }
    var lng by remember { mutableDoubleStateOf(uiState.preferences.savedDeliveryLng) }
    var liveGpsEnabled by remember { mutableStateOf(false) }
    var couponInput by remember { mutableStateOf(uiState.appliedCouponCode ?: "") }
    var paymentMethod by remember { mutableStateOf(PaymentMethod.QR_UPI) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val granted = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            liveGpsEnabled = true
            onDetectGps { resolvedAddr, newLat, newLng ->
                address = resolvedAddr
                lat = newLat
                lng = newLng
            }
        } else {
            validationError = "Location permission denied. Tap the map or type your address manually."
            liveGpsEnabled = false
        }
    }

    val subtotal = uiState.cartSubtotal
    val finalTotal = (subtotal - discountInr).coerceAtLeast(0)
    val activeCoupons = uiState.coupons.filter { it.isActive }

    val matchingPresets = remember(address) {
        if (address.isBlank()) LocationHelper.presetLandmarks
        else LocationHelper.presetLandmarks.filter {
            it.formattedAddress.contains(address, ignoreCase = true) && !it.formattedAddress.equals(address, ignoreCase = true)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.ShoppingBag,
                        contentDescription = null,
                        tint = CoralPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Your Bag (${uiState.cartCount})",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close bag")
                }
            }

            if (uiState.cartItems.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ShoppingBag,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Your bag is empty",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Add burgers, wings, pizzas, dosas, or drinks from the menu.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Bag items
                uiState.cartItems.forEach { cartItem ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DishThumbnail(
                                dish = cartItem.dish,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(MaterialTheme.shapes.extraSmall)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = cartItem.dish.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = cartItem.dish.shopName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "₹${cartItem.dish.priceInr * cartItem.quantity}",
                                    style = MonoTabularStyle.copy(
                                        fontSize = MaterialTheme.typography.labelLarge.fontSize
                                    ),
                                    color = CoralPrimary
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { onDecrement(cartItem.dish.id) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Rounded.Remove, contentDescription = "Decrease quantity", modifier = Modifier.size(18.dp))
                                }
                                Text(
                                    text = "${cartItem.quantity}",
                                    style = MonoTabularStyle,
                                    modifier = Modifier.padding(horizontal = 6.dp)
                                )
                                IconButton(
                                    onClick = { onIncrement(cartItem.dish.id) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Rounded.Add, contentDescription = "Increase quantity", modifier = Modifier.size(18.dp))
                                }
                                IconButton(
                                    onClick = { onRemove(cartItem.dish.id) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.DeleteOutline,
                                        contentDescription = "Remove item",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider()

                // Contact Phone & Delivery Location
                Text(
                    text = "Delivery Details",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        phone = it
                        validationError = null
                    },
                    label = { Text("Mobile Number (for rider calls)") },
                    placeholder = { Text("+91 98XXXXXXXX") },
                    leadingIcon = { Icon(Icons.Rounded.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("checkout_phone_input")
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = {
                        address = it
                        validationError = null
                    },
                    label = { Text("Delivery Address") },
                    placeholder = { Text("House / Flat, Street, Landmark") },
                    leadingIcon = { Icon(Icons.Rounded.LocationOn, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("checkout_address_input")
                )

                // Landmark quick-suggestions
                if (matchingPresets.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        matchingPresets.take(4).forEach { preset ->
                            FilterChip(
                                selected = address == preset.formattedAddress,
                                onClick = {
                                    address = preset.formattedAddress
                                    lat = preset.lat
                                    lng = preset.lng
                                    validationError = null
                                },
                                label = {
                                    Text(
                                        text = preset.formattedAddress.substringBefore(","),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            )
                        }
                    }
                }

                // GPS Auto-Detect & Live Location Toggle
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.GpsFixed,
                                contentDescription = null,
                                tint = CoralPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Live GPS Location",
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Text(
                                    text = uiState.locationMessage ?: "Auto-fill coordinates & street",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        if (uiState.isDetectingLocation) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp,
                                color = CoralPrimary
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TextButton(
                                    onClick = {
                                        if (LocationHelper.hasLocationPermission(context)) {
                                            liveGpsEnabled = true
                                            onDetectGps { resolvedAddr, newLat, newLng ->
                                                address = resolvedAddr
                                                lat = newLat
                                                lng = newLng
                                            }
                                        } else {
                                            locationPermissionLauncher.launch(
                                                arrayOf(
                                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                                )
                                            )
                                        }
                                    }
                                ) {
                                    Text("Detect", fontWeight = FontWeight.Bold)
                                }
                                Switch(
                                    checked = liveGpsEnabled,
                                    onCheckedChange = { checked ->
                                        if (checked) {
                                            if (LocationHelper.hasLocationPermission(context)) {
                                                liveGpsEnabled = true
                                                onDetectGps { resolvedAddr, newLat, newLng ->
                                                    address = resolvedAddr
                                                    lat = newLat
                                                    lng = newLng
                                                }
                                            } else {
                                                locationPermissionLauncher.launch(
                                                    arrayOf(
                                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                                    )
                                                )
                                            }
                                        } else {
                                            liveGpsEnabled = false
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                InteractiveDeliveryMap(
                    customerLat = lat,
                    customerLng = lng,
                    kitchens = uiState.kitchens,
                    interactivePinSelection = true,
                    onPinMoved = { newLat, newLng ->
                        lat = newLat
                        lng = newLng
                        onReverseGeocode(newLat, newLng) { resolved ->
                            address = resolved
                        }
                    },
                    height = 175.dp
                )

                HorizontalDivider()

                // Promo Coupons
                Text(
                    text = "Promo Coupon",
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = couponInput,
                        onValueChange = { couponInput = it.uppercase() },
                        label = { Text("Enter Coupon Code") },
                        leadingIcon = { Icon(Icons.Rounded.LocalOffer, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("coupon_code_input")
                    )
                    if (uiState.appliedCouponCode != null) {
                        OutlinedButton(
                            onClick = {
                                couponInput = ""
                                onRemoveCoupon()
                            },
                            modifier = Modifier.height(52.dp)
                        ) {
                            Text("Remove")
                        }
                    } else {
                        Button(
                            onClick = { onApplyCoupon(couponInput) },
                            colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                            modifier = Modifier
                                .height(52.dp)
                                .testTag("apply_coupon_button")
                        ) {
                            Text("Apply")
                        }
                    }
                }

                if (!uiState.couponError.isNullOrBlank()) {
                    Text(
                        text = uiState.couponError,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (activeCoupons.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        activeCoupons.forEach { coupon ->
                            val isSelected = uiState.appliedCouponCode.equals(coupon.code, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    couponInput = coupon.code
                                    onApplyCoupon(coupon.code)
                                },
                                label = {
                                    Text("${coupon.code} • ${coupon.description}", style = MaterialTheme.typography.labelSmall)
                                }
                            )
                        }
                    }
                }

                HorizontalDivider()

                // Payment Method
                Text(
                    text = "Payment Method",
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PaymentMethod.entries.forEach { method ->
                        val selected = paymentMethod == method
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(MaterialTheme.shapes.small)
                                .clickable { paymentMethod = method },
                            shape = MaterialTheme.shapes.small,
                            color = if (selected) CoralPrimary.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            border = BorderStroke(
                                width = if (selected) 1.5.dp else 1.dp,
                                color = if (selected) CoralPrimary else MaterialTheme.colorScheme.outline
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (method == PaymentMethod.QR_UPI) Icons.Rounded.QrCode2 else Icons.Rounded.Payments,
                                    contentDescription = null,
                                    tint = if (selected) CoralPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = method.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                if (paymentMethod == PaymentMethod.QR_UPI) {
                    PaymentQrPreview(
                        customQrUri = uiState.preferences.customQrUri,
                        upiLabel = uiState.preferences.paymentUpiLabel,
                        amountInr = finalTotal
                    )
                }

                // Bill Summary
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Item Subtotal", style = MaterialTheme.typography.bodyMedium)
                            Text("₹$subtotal", style = MonoTabularStyle)
                        }
                        if (discountInr > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Coupon (${uiState.appliedCouponCode})",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = EmeraldSuccess
                                )
                                Text(
                                    text = "-₹$discountInr",
                                    style = MonoTabularStyle,
                                    color = EmeraldSuccess
                                )
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total Payable",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "₹$finalTotal",
                                style = MonoTabularStyle.copy(
                                    fontSize = MaterialTheme.typography.titleMedium.fontSize,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = CoralPrimary
                            )
                        }
                    }
                }

                if (!validationError.isNullOrBlank()) {
                    Text(
                        text = validationError!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    onClick = {
                        val digits = phone.filter { it.isDigit() }
                        if (digits.length < 7) {
                            validationError = "Please enter a valid mobile number so the rider can reach you."
                            return@Button
                        }
                        if (address.trim().length < 5) {
                            validationError = "Please enter or detect your delivery address."
                            return@Button
                        }
                        onPlaceOrder(phone, address, lat, lng, paymentMethod)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("place_order_button")
                ) {
                    Text(
                        text = "Place Order • ₹$finalTotal",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun OrderConfirmedDialog(
    order: OrderEntity,
    onTrackOrder: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = EmeraldSuccess.copy(alpha = 0.14f)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = "Order Confirmed",
                        tint = EmeraldSuccess,
                        modifier = Modifier
                            .padding(14.dp)
                            .size(40.dp)
                    )
                }
                Text(
                    text = "Order Placed!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${order.id} • ₹${order.totalInr}",
                    style = MonoTabularStyle.copy(
                        fontSize = MaterialTheme.typography.titleMedium.fontSize
                    ),
                    color = CoralPrimary
                )
                Text(
                    text = "Kitchen & riders have been notified. Delivering to ${order.deliveryAddress}.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = onTrackOrder,
                    colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("track_confirmed_order_button")
                ) {
                    Text("Track Live Order on Map", fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = onDismiss) {
                    Text("Back to Storefront")
                }
            }
        }
    }
}
