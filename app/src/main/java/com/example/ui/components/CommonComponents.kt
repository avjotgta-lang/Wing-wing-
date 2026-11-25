package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.DishEntity
import com.example.data.model.OrderStatus
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.CoralPrimaryDark
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.MonoTabularStyle
import com.example.ui.theme.SkyInfo
import com.example.ui.theme.SpaceGroteskFamily
import com.example.ui.theme.VioletReady
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WingWingWordmark(
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(
                    Brush.linearGradient(
                        listOf(CoralPrimary, CoralPrimaryDark)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.LocalFireDepartment,
                contentDescription = "WingWing emblem",
                tint = Color.White,
                modifier = Modifier.size(21.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = "WingWing",
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun DishThumbnail(
    dish: DishEntity,
    modifier: Modifier = Modifier
) {
    val customPath = dish.customImageUri
    if (!customPath.isNullOrBlank() && File(customPath).exists()) {
        AsyncImage(
            model = File(customPath),
            contentDescription = dish.name,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        val drawableRes = when (dish.imageKey) {
            "burger" -> R.drawable.img_dish_burger
            "pizza" -> R.drawable.img_dish_pizza
            "dosa" -> R.drawable.img_dish_dosa
            "drinks" -> R.drawable.img_dish_drinks
            else -> R.drawable.img_hero_spread
        }
        Image(
            painter = painterResource(id = drawableRes),
            contentDescription = dish.name,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    }
}

fun statusColor(status: OrderStatus): Color = when (status) {
    OrderStatus.NEW -> CoralPrimary
    OrderStatus.PREPARING -> AmberWarning
    OrderStatus.READY -> VioletReady
    OrderStatus.PICKED_UP -> SkyInfo
    OrderStatus.DELIVERED -> EmeraldSuccess
}

@Composable
fun OrderStatusPill(
    status: OrderStatus,
    modifier: Modifier = Modifier
) {
    val tint = statusColor(status)
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = tint.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, tint.copy(alpha = 0.4f))
    ) {
        Text(
            text = status.label,
            style = MaterialTheme.typography.labelSmall,
            color = tint,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun OrderProgressStepper(
    currentStatus: OrderStatus,
    modifier: Modifier = Modifier
) {
    val stages = OrderStatus.entries
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        stages.forEach { stage ->
            val reached = stage.stepIndex <= currentStatus.stepIndex
            val isCurrent = stage == currentStatus
            val pillColor by animateColorAsState(
                targetValue = when {
                    isCurrent -> statusColor(currentStatus)
                    reached -> EmeraldSuccess
                    else -> MaterialTheme.colorScheme.surfaceVariant
                },
                animationSpec = spring(),
                label = "stage_color"
            )
            val weight by animateFloatAsState(
                targetValue = if (isCurrent) 1.35f else 1f,
                animationSpec = spring(),
                label = "stage_weight"
            )
            Column(
                modifier = Modifier.weight(weight),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(pillColor)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stage.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (reached) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun AnimatedKpiCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(accentColor.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MonoTabularStyle.copy(
                    fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                    lineHeight = MaterialTheme.typography.headlineSmall.lineHeight
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun StarRatingDisplay(rating: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        for (i in 1..5) {
            Icon(
                imageVector = Icons.Rounded.Star,
                contentDescription = null,
                tint = if (i <= rating) AmberWarning else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

fun formatEpochTime(epochMs: Long): String {
    val formatter = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    return formatter.format(Date(epochMs))
}

fun dialPhoneNumber(context: Context, phone: String) {
    val clean = phone.trim()
    if (clean.isEmpty()) return
    val intent = Intent(Intent.ACTION_DIAL).apply {
        data = Uri.parse("tel:$clean")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { context.startActivity(intent) }
}

fun openExternalMaps(context: Context, lat: Double, lng: Double, label: String) {
    val encodedLabel = Uri.encode(label)
    val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng($encodedLabel)")
    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { context.startActivity(intent) }
}
