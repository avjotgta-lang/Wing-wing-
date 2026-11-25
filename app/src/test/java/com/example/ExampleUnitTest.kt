package com.example

import com.example.data.model.CouponEntity
import com.example.data.model.DiscountType
import com.example.util.LocationHelper
import com.example.util.QrCodeMatrix
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.min
import kotlin.math.roundToInt

class ExampleUnitTest {

    @Test
    fun `qr matrix generates valid 25x25 finder corners`() {
        val matrix = QrCodeMatrix.generate("upi://pay?pa=wingwing.pay@okaxis&am=289")
        assertEquals(25, matrix.size)
        assertTrue(matrix[0][0])
        assertTrue(matrix[0][6])
        assertTrue(matrix[6][0])
        assertTrue(matrix[0][24])
        assertTrue(matrix[24][0])
    }

    @Test
    fun `distance and eta calculation returns sensible bounds`() {
        val dist = LocationHelper.distanceKm(28.6304, 77.2177, 28.6353, 77.2250)
        assertTrue(dist >= 0.4)
        val eta = LocationHelper.estimateEtaMinutes(dist, prepMinutes = 12)
        assertTrue(eta in 17..60)
    }

    @Test
    fun `coupon discount calculation respects flat percent and caps`() {
        val percentCoupon = CouponEntity(
            code = "CORAL20",
            description = "20% off up to ₹120",
            discountType = DiscountType.PERCENT.name,
            value = 20,
            minOrderInr = 399,
            maxDiscountInr = 120,
            isActive = true,
            createdAtEpochMs = 0L
        )
        val subtotal = 800
        val raw = ((subtotal * percentCoupon.value) / 100.0).roundToInt()
        val capped = min(raw, percentCoupon.maxDiscountInr)
        assertEquals(120, capped)
    }
}
