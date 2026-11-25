package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.MonoTabularStyle
import com.example.util.QrCodeMatrix
import java.io.File

@Composable
fun PaymentQrPreview(
    customQrUri: String,
    upiLabel: String,
    amountInr: Int,
    modifier: Modifier = Modifier
) {
    val hasCustomQr = customQrUri.isNotBlank() && File(customQrUri).exists()
    val payload = remember(upiLabel, amountInr) {
        "upi://pay?pa=$upiLabel&pn=WingWing&am=$amountInr&cu=INR"
    }
    val matrix = remember(payload) { QrCodeMatrix.generate(payload) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.QrCode2,
                    contentDescription = null,
                    tint = CoralPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Scan & Pay via UPI",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .size(176.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                if (hasCustomQr) {
                    AsyncImage(
                        model = File(customQrUri),
                        contentDescription = "Merchant payment QR code",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val dim = matrix.size
                        val cellSize = size.minDimension / dim.toFloat()
                        for (r in 0 until dim) {
                            for (c in 0 until dim) {
                                if (matrix[r][c]) {
                                    drawRect(
                                        color = Color(0xFF141416),
                                        topLeft = Offset(c * cellSize, r * cellSize),
                                        size = Size(cellSize * 0.94f, cellSize * 0.94f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = upiLabel,
                style = MonoTabularStyle.copy(
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            if (amountInr > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Payable Amount: ₹$amountInr",
                    style = MonoTabularStyle.copy(
                        fontSize = MaterialTheme.typography.labelLarge.fontSize,
                        fontWeight = FontWeight.Bold
                    ),
                    color = CoralPrimary
                )
            }
        }
    }
}
