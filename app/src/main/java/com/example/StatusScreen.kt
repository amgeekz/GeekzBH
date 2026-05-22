package com.example

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import java.util.Locale
import kotlin.math.sin

@Composable
fun StatusScreen() {
    val level by ChargingController.batteryLevel.collectAsState()
    val status by ChargingController.batteryStatus.collectAsState()
    val temp by ChargingController.batteryTemp.collectAsState()
    val current by ChargingController.batteryCurrent.collectAsState()
    val voltage by ChargingController.batteryVoltage.collectAsState()
    val powerWatts by ChargingController.powerUsageWatts.collectAsState()

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AmoledBlack)
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        
        // 1. Interactive Circular Battery Ring Gauge
        BatteryLiquidGauge(
            level = level,
            modifier = Modifier
                .size(240.dp)
                .padding(vertical = 12.dp)
        )

        // Status banner below battery
        val activeBypass = status == "Bypass Power"
        val displayStatus = when {
            activeBypass -> "BYPASS MODE ACTIVE"
            current > 0 -> "FAST CHARGING ACTIVE"
            current < 0 -> "DISCHARGING"
            else -> "CONNECTED (STANDBY)"
        }
        val statusColor = if (activeBypass) GlowCyan else if (current > 0) TechGreen else SoftRed

        Text(
            text = displayStatus,
            color = statusColor,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 2. Power Usage Row Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkCharcoal),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color(0xFF1E293B))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "POWER DISSIPATION FLOW",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = String.format("%.2f Watt", powerWatts),
                        color = GlowCyan,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                // Thunder Bolt Icon Custom Canvas
                Canvas(modifier = Modifier.size(36.dp)) {
                    val path = Path().apply {
                        moveTo(size.width * 0.6f, 0f)
                        lineTo(size.width * 0.15f, size.height * 0.55f)
                        lineTo(size.width * 0.5f, size.height * 0.55f)
                        lineTo(size.width * 0.4f, size.height)
                        lineTo(size.width * 0.85f, size.height * 0.45f)
                        lineTo(size.width * 0.5f, size.height * 0.45f)
                        close()
                    }
                    drawPath(path = path, color = Color(0xFF22D3EE))
                }
            }
        }

        // 3. Grid representation (2 columns x 4 rows)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Suhu (Temp)
                GridStatCard(
                    title = "TEMPERATURE",
                    value = String.format("%.1f °C", temp),
                    iconSymbol = "🌡️",
                    modifier = Modifier.weight(1f)
                )

                // Arus (Current)
                GridStatCard(
                    title = "CURRENT NOW",
                    value = "$current mA",
                    iconSymbol = "⚡",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Voltase
                GridStatCard(
                    title = "VOLTAGE LEVEL",
                    value = "$voltage mV",
                    iconSymbol = "🔋",
                    modifier = Modifier.weight(1f)
                )

                // Kesehatan
                GridStatCard(
                    title = "BATTERY HEALTH",
                    value = ChargingController.health,
                    iconSymbol = "❤️",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Teknologi
                GridStatCard(
                    title = "CELL TECH",
                    value = ChargingController.technology,
                    iconSymbol = "💾",
                    modifier = Modifier.weight(1f)
                )

                // Sumber
                GridStatCard(
                    title = "POWER SOURCE",
                    value = status,
                    iconSymbol = "🔌",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Kapasitas
                GridStatCard(
                    title = "CELL CAPACITY",
                    value = "${ChargingController.maxCapacityMah} mAh",
                    iconSymbol = "📦",
                    modifier = Modifier.weight(1f)
                )

                // Cycle Count
                GridStatCard(
                    title = "CHARGE CYCLES",
                    value = "${ChargingController.cycleCount}",
                    iconSymbol = "🔄",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun BatteryLiquidGauge(level: Int, modifier: Modifier = Modifier) {
    val status by ChargingController.batteryStatus.collectAsState()

    // Liquid percentage filling interpolator (smooth animation on update)
    val smoothLevel by animateFloatAsState(
        targetValue = level.toFloat(),
        animationSpec = tween(1200, easing = FastOutSlowInEasing)
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            val sizeMin = size.minDimension - strokeWidth
            val halfStroke = strokeWidth / 2f

            // 1. Draw background progress track (slate-800)
            drawArc(
                color = Color(0xFF1E293B),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth),
                topLeft = Offset(halfStroke, halfStroke),
                size = Size(sizeMin, sizeMin)
            )

            // Calculate active sweep angle corresponding to battery level (0-360)
            val sweepAngle = (smoothLevel / 100f) * 360f
            val activeColor = if (level > 20) GlowCyan else SoftRed

            // 2. Draw subtle glowing aura backdrop path
            drawArc(
                color = activeColor.copy(alpha = 0.15f),
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth + 6.dp.toPx(), cap = StrokeCap.Round),
                topLeft = Offset(halfStroke - 3.dp.toPx(), halfStroke - 3.dp.toPx()),
                size = Size(sizeMin + 6.dp.toPx(), sizeMin + 6.dp.toPx())
            )

            // 3. Draw active high-tech glowing progress arc
            drawArc(
                color = activeColor,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft = Offset(halfStroke, halfStroke),
                size = Size(sizeMin, sizeMin)
            )
        }

        // Concentric inner card layout for metadata displaying
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "BATTERY LEVEL",
                color = TextSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$level%",
                color = TextPrimary,
                fontSize = 52.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1.5).sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            val isChargingBypassed = status.contains("Bypass", ignoreCase = true) || status.contains("Boost", ignoreCase = true)
            Text(
                text = if (isChargingBypassed) "ACTIVE BOOST BYPASS" else "PLUGGED IN (AC)",
                color = if (isChargingBypassed) GlowCyan else TechGreen,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun GridStatCard(
    title: String,
    value: String,
    iconSymbol: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(112.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCharcoal),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Color(0xFF1E293B))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = iconSymbol,
                    fontSize = 20.sp
                )
                Text(
                    text = title,
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
            
            Text(
                text = value,
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
