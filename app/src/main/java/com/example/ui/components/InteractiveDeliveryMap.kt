package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Directions
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.KitchenEntity
import com.example.data.model.OrderStatus
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.MonoTabularStyle
import com.example.ui.theme.SkyInfo
import com.example.util.LocationHelper
import kotlin.math.roundToInt

/**
 * Interactive vector city map rendered in Compose Canvas so pin placement, kitchen hubs,
 * live route visualization, and animated rider progress work reliably on all devices.
 */
@Composable
fun InteractiveDeliveryMap(
    customerLat: Double,
    customerLng: Double,
    kitchens: List<KitchenEntity>,
    orderStatus: OrderStatus? = null,
    interactivePinSelection: Boolean = false,
    onPinMoved: ((Double, Double) -> Unit)? = null,
    height: Dp = 210.dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = MaterialTheme.colorScheme.background.red < 0.2f

    // Map bounds centered around primary culinary district or around customer pin if far away
    val primaryKitchen = kitchens.firstOrNull()
    val centerLat = primaryKitchen?.lat ?: 28.6304
    val centerLng = primaryKitchen?.lng ?: 77.2177
    val spanLat = 0.032
    val spanLng = 0.036

    // Normalize coordinates into [0.08..0.92] canvas space
    fun toNormalized(lat: Double, lng: Double): Offset {
        val rawX = 0.5f + ((lng - centerLng) / spanLng).toFloat()
        val rawY = 0.5f - ((lat - centerLat) / spanLat).toFloat()
        return Offset(rawX.coerceIn(0.08f, 0.92f), rawY.coerceIn(0.10f, 0.90f))
    }

    fun fromNormalized(normX: Float, normY: Float): Pair<Double, Double> {
        val lng = centerLng + (normX.coerceIn(0.06f, 0.94f) - 0.5f) * spanLng
        val lat = centerLat - (normY.coerceIn(0.08f, 0.92f) - 0.5f) * spanLat
        return lat to lng
    }

    val distanceKm = LocationHelper.distanceKm(centerLat, centerLng, customerLat, customerLng)
    val etaMin = LocationHelper.estimateEtaMinutes(distanceKm)

    val riderProgress = when (orderStatus) {
        OrderStatus.NEW -> 0.05f
        OrderStatus.PREPARING -> 0.15f
        OrderStatus.READY -> 0.30f
        OrderStatus.PICKED_UP -> 0.72f
        OrderStatus.DELIVERED -> 1.0f
        null -> 0f
    }

    val pulseTransition = rememberInfiniteTransition(label = "map_pulse")
    val pulseRadius by pulseTransition.animateFloat(
        initialValue = 12f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_radius"
    )
    val riderBob by pulseTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rider_bob"
    )

    val mapBg = if (isDark) Color(0xFF151821) else Color(0xFFF3EFE6)
    val blockColor = if (isDark) Color(0xFF1E222E) else Color(0xFFE7E1D5)
    val parkColor = if (isDark) Color(0xFF142A24) else Color(0xFFD8ECDC)
    val roadColor = if (isDark) Color(0xFF2B3040) else Color(0xFFFFFFFF)
    val majorRoadColor = if (isDark) Color(0xFF394054) else Color(0xFFFDF8EE)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(mapBg)
                    .then(
                        if (interactivePinSelection && onPinMoved != null) {
                            Modifier
                                .pointerInput(centerLat, centerLng) {
                                    detectTapGestures { offset ->
                                        val nx = (offset.x / size.width).coerceIn(0.06f, 0.94f)
                                        val ny = (offset.y / size.height).coerceIn(0.08f, 0.92f)
                                        val (lat, lng) = fromNormalized(nx, ny)
                                        onPinMoved(lat, lng)
                                    }
                                }
                                .pointerInput(centerLat, centerLng) {
                                    detectDragGestures { change, _ ->
                                        change.consume()
                                        val nx = (change.position.x / size.width).coerceIn(0.06f, 0.94f)
                                        val ny = (change.position.y / size.height).coerceIn(0.08f, 0.92f)
                                        val (lat, lng) = fromNormalized(nx, ny)
                                        onPinMoved(lat, lng)
                                    }
                                }
                        } else Modifier
                    )
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // City blocks & green zones
                    drawRect(color = parkColor, topLeft = Offset(w * 0.06f, h * 0.12f), size = Size(w * 0.22f, h * 0.24f))
                    drawRect(color = parkColor, topLeft = Offset(w * 0.72f, h * 0.58f), size = Size(w * 0.20f, h * 0.26f))
                    drawRect(color = blockColor, topLeft = Offset(w * 0.34f, h * 0.14f), size = Size(w * 0.26f, h * 0.22f))
                    drawRect(color = blockColor, topLeft = Offset(w * 0.14f, h * 0.48f), size = Size(w * 0.24f, h * 0.26f))
                    drawRect(color = blockColor, topLeft = Offset(w * 0.46f, h * 0.44f), size = Size(w * 0.20f, h * 0.22f))

                    // Grid roads
                    for (i in 1..4) {
                        val y = h * (i / 5f)
                        drawLine(roadColor, Offset(0f, y), Offset(w, y), strokeWidth = 5f)
                    }
                    for (i in 1..5) {
                        val x = w * (i / 6f)
                        drawLine(roadColor, Offset(x, 0f), Offset(x, h), strokeWidth = 5f)
                    }

                    // Diagonal arterial avenue
                    drawLine(majorRoadColor, Offset(0f, h * 0.85f), Offset(w, h * 0.18f), strokeWidth = 10f)
                    drawLine(majorRoadColor, Offset(w * 0.15f, 0f), Offset(w * 0.78f, h), strokeWidth = 8f)

                    val hubNorm = toNormalized(centerLat, centerLng)
                    val hubPt = Offset(hubNorm.x * w, hubNorm.y * h)

                    val custNorm = toNormalized(customerLat, customerLng)
                    val custPt = Offset(custNorm.x * w, custNorm.y * h)

                    // Route curve between Kitchen Hub and Customer Drop Pin
                    val controlPt = Offset(
                        (hubPt.x + custPt.x) / 2f + (if (custPt.y > hubPt.y) 36f else -36f),
                        (hubPt.y + custPt.y) / 2f - 24f
                    )
                    val routePath = Path().apply {
                        moveTo(hubPt.x, hubPt.y)
                        quadraticTo(controlPt.x, controlPt.y, custPt.x, custPt.y)
                    }

                    drawPath(
                        path = routePath,
                        color = CoralPrimary.copy(alpha = 0.28f),
                        style = Stroke(width = 10f, cap = StrokeCap.Round)
                    )
                    drawPath(
                        path = routePath,
                        color = CoralPrimary,
                        style = Stroke(
                            width = 5f,
                            cap = StrokeCap.Round,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)
                        )
                    )

                    // Draw partner kitchen pins
                    kitchens.forEachIndexed { idx, kitchen ->
                        val kn = toNormalized(kitchen.lat, kitchen.lng)
                        val kp = Offset(kn.x * w, kn.y * h)
                        drawCircle(
                            color = if (idx == 0) EmeraldSuccess else Color(0xFF6366F1),
                            radius = if (idx == 0) 13f else 9f,
                            center = kp
                        )
                        drawCircle(
                            color = Color.White,
                            radius = if (idx == 0) 5f else 3.5f,
                            center = kp
                        )
                    }

                    // Customer dropoff pulse + pin
                    drawCircle(
                        color = CoralPrimary.copy(alpha = 0.22f),
                        radius = pulseRadius,
                        center = custPt
                    )
                    drawCircle(color = Color.White, radius = 15f, center = custPt)
                    drawCircle(color = CoralPrimary, radius = 11f, center = custPt)

                    // Animated rider scooter marker when active order is tracked
                    if (orderStatus != null && orderStatus != OrderStatus.NEW) {
                        val t = (riderProgress + (if (orderStatus == OrderStatus.PICKED_UP) (riderBob - 0.5f) * 0.06f else 0f))
                            .coerceIn(0f, 1f)
                        val omt = 1f - t
                        val rx = omt * omt * hubPt.x + 2 * omt * t * controlPt.x + t * t * custPt.x
                        val ry = omt * omt * hubPt.y + 2 * omt * t * controlPt.y + t * t * custPt.y
                        val riderPt = Offset(rx, ry)

                        drawCircle(color = SkyInfo.copy(alpha = 0.25f), radius = 22f, center = riderPt)
                        drawCircle(color = Color.White, radius = 13f, center = riderPt)
                        drawCircle(color = SkyInfo, radius = 9.5f, center = riderPt)
                    }
                }

                // Top-left live ETA & distance badge
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(EmeraldSuccess)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${(distanceKm * 10).roundToInt() / 10.0} km • ~$etaMin min",
                            style = MonoTabularStyle.copy(
                                fontSize = MaterialTheme.typography.labelSmall.fontSize
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                if (interactivePinSelection) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(10.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MyLocation,
                                contentDescription = null,
                                tint = CoralPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Tap or drag map to move drop pin",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Legend + External Maps button row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendDot(color = EmeraldSuccess, label = "Kitchen")
                    LegendDot(color = CoralPrimary, label = "Dropoff")
                    if (orderStatus != null) {
                        LegendDot(color = SkyInfo, label = "Rider")
                    }
                }
                FilledTonalButton(
                    onClick = {
                        openExternalMaps(context, customerLat, customerLng, "WingWing Dropoff")
                    },
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Directions,
                        contentDescription = "Open in Maps",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open Maps", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
