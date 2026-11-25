package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.KitchenEntity
import com.example.data.model.OrderEntity
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.MonoTabularStyle

@Composable
fun OrderTrackDialog(
    order: OrderEntity,
    kitchens: List<KitchenEntity>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
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
                                imageVector = Icons.Rounded.LocalShipping,
                                contentDescription = null,
                                tint = CoralPrimary,
                                modifier = Modifier
                                    .padding(9.dp)
                                    .size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Live Order ${order.id}",
                                style = MonoTabularStyle.copy(
                                    fontSize = MaterialTheme.typography.titleLarge.fontSize
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = formatEpochTime(order.createdAtEpochMs),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close tracker")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OrderStatusPill(status = order.statusEnum)
                    Text(
                        text = order.paymentMethodEnum.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OrderProgressStepper(currentStatus = order.statusEnum)

                InteractiveDeliveryMap(
                    customerLat = order.deliveryLat,
                    customerLng = order.deliveryLng,
                    kitchens = kitchens,
                    orderStatus = order.statusEnum,
                    interactivePinSelection = false,
                    height = 200.dp
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.LocationOn,
                                contentDescription = null,
                                tint = CoralPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = order.deliveryAddress,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        order.items.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${item.quantity}x ${item.name}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "₹${item.priceInr * item.quantity}",
                                    style = MonoTabularStyle.copy(
                                        fontSize = MaterialTheme.typography.bodySmall.fontSize
                                    )
                                )
                            }
                        }
                        if (order.discountInr > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Promo (${order.couponCode ?: ""})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = EmeraldSuccess
                                )
                                Text(
                                    text = "-₹${order.discountInr}",
                                    style = MonoTabularStyle.copy(
                                        fontSize = MaterialTheme.typography.bodySmall.fontSize
                                    ),
                                    color = EmeraldSuccess
                                )
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Paid", style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = "₹${order.totalInr}",
                                style = MonoTabularStyle.copy(
                                    fontSize = MaterialTheme.typography.titleSmall.fontSize
                                ),
                                color = CoralPrimary
                            )
                        }
                    }
                }

                if (!order.riderPhone.isNullOrBlank()) {
                    Button(
                        onClick = { dialPhoneNumber(context, order.riderPhone) },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Call,
                            contentDescription = "Call rider",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Call Rider (${order.riderPhone})",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
