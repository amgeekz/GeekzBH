package com.example

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
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
            .background(NeoBg)
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        
        // 1. High-Fidelity Animated Circular Battery Ring Gauge (Neobrutalist Outer Border)
        NeobrutalistCard(
            modifier = Modifier
                .size(230.dp)
                .padding(vertical = 4.dp),
            containerColor = NeoWhite,
            borderColor = NeoDark,
            borderWidth = 3.dp,
            shadowOffset = 5.dp
        ) {
            BatteryLiquidGauge(
                level = level,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Status banner below battery
        val activeBypass = ChargingController.isBypassMode.collectAsState().value
        val isCharging = current >= 0
        val displayStatus = when {
            activeBypass -> "BYPASS MODE ACTIVE"
            isCharging -> "FAST CHARGING ACTIVE"
            else -> "DISCHARGING"
        }
        // Use fully opaque colors to prevent the solid shadow from shining through!
        val cardBgColor = if (activeBypass) Color(0xFFFFECEF) else if (isCharging) Color(0xFFE2F9EE) else Color(0xFFFFF2F2)
        val cardTextColor = if (activeBypass) Color(0xFFD81B60) else if (isCharging) NeoGreen else Color(0xFFDC2626)
        val cardBorderColor = if (activeBypass) NeoPink else if (isCharging) NeoGreen else SoftRed

        NeobrutalistCard(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            containerColor = cardBgColor,
            borderColor = cardBorderColor,
            shadowOffset = 2.dp,
            borderWidth = 1.5.dp
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = displayStatus,
                    color = cardTextColor,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 2. Power Usage Row Banner
        NeobrutalistCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = NeoWhite
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
                        color = NeoSubtitle,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = String.format("%.2f Watt", powerWatts),
                        color = NeoDark,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                // Thunder Bolt Icon Custom Canvas (Follows neobrutalist yellow)
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
                    drawPath(path = path, color = NeoYellow)
                    drawPath(
                        path = path,
                        color = NeoDark,
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
        }

        // 3. Grid representation (2 columns x 4 rows)
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
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
                    iconSymbol = "🔌",
                    valueColor = if (current >= 0) NeoGreen else SoftRed,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                // Voltase
                GridStatCard(
                    title = "VOLTAGE LEVEL",
                    value = "$voltage mV",
                    iconSymbol = "⚡",
                    modifier = Modifier.weight(1f)
                )

                // Kesehatan
                GridStatCard(
                    title = "BATTERY HEALTH",
                    value = ChargingController.health,
                    iconSymbol = "❤️",
                    valueColor = if (ChargingController.health.lowercase().contains("good")) NeoGreen else SoftRed,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
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
                    value = if (current >= 0) "Charging (AC)" else "Discharging",
                    iconSymbol = "🔋",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
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

        // 4. Live Current Trend Line Chart Card (Neobrutalist Shadow Box at bottom as requested!)
        val currentHistory by ChargingController.currentHistory.collectAsState()
        val currentNow by ChargingController.batteryCurrent.collectAsState()

        val maxHistoric = if (currentHistory.isNotEmpty()) currentHistory.maxOrNull() ?: currentNow else currentNow
        val minHistoric = if (currentHistory.isNotEmpty()) currentHistory.minOrNull() ?: currentNow else currentNow

        NeobrutalistCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "LIVE STATISTIK TELEMETRI",
                            color = NeoSubtitle,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$currentNow mA",
                            color = if (currentNow >= 0) NeoGreen else SoftRed,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Max: $maxHistoric mA",
                            color = NeoGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Min: $minHistoric mA",
                            color = SoftRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val lineColor = if (currentNow >= 0) NeoGreen else SoftRed
                
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    if (currentHistory.size >= 2) {
                        val path = Path()
                        val fillPath = Path()
                        
                        val pointsCount = currentHistory.size
                        val stepX = size.width / (pointsCount - 1)
                        
                        val maxRaw = currentHistory.maxOrNull() ?: 1000
                        val minRaw = currentHistory.minOrNull() ?: -1000
                        val delta = (maxRaw - minRaw).coerceAtLeast(100)
                        
                        currentHistory.forEachIndexed { idx, valItem ->
                            val normalizedY = 1.0f - ((valItem - minRaw).toFloat() / delta.toFloat())
                            val y = 10f + (normalizedY * (size.height - 20f))
                            val x = idx * stepX
                            
                            if (idx == 0) {
                                path.moveTo(x, y)
                                fillPath.moveTo(x, size.height)
                                fillPath.lineTo(x, y)
                            } else {
                                path.lineTo(x, y)
                                fillPath.lineTo(x, y)
                            }
                            
                            if (idx == pointsCount - 1) {
                                fillPath.lineTo(x, size.height)
                                fillPath.close()
                            }
                        }
                        
                        drawPath(
                            path = fillPath,
                            color = lineColor.copy(alpha = 0.12f)
                        )
                        
                        drawPath(
                            path = path,
                            color = lineColor,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BatteryLiquidGauge(level: Int, modifier: Modifier = Modifier) {
    val status by ChargingController.batteryStatus.collectAsState()

    // Smooth percentage transition
    val smoothLevel by animateFloatAsState(
        targetValue = level.toFloat(),
        animationSpec = tween(1200, easing = FastOutSlowInEasing)
    )

    val statusLower = status.lowercase()
    val isCharging = (statusLower.contains("charging") && 
                      !statusLower.contains("discharging") && 
                      !statusLower.contains("not charging")) || 
                     statusLower.contains("boost") || 
                     statusLower.contains("full")

    // Infinite wave shift transition to create liquid wave animation ("Battery Level tidak ada animasi nya")
    val infiniteTransition = rememberInfiniteTransition(label = "liquid_wave")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_offset"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 3.dp.toPx()
            
            // Draw an internal sloshing fluid inside the card.
            // Create a Circular clipping path
            val clipPath = Path().apply {
                addOval(Rect(Offset.Zero, Size(size.width, size.height)))
            }
            
            clipPath(clipPath) {
                // Fill lower background of circle based on battery level
                val fillY = size.height * (1f - (smoothLevel / 100f))
                val wavePath = Path()
                
                wavePath.moveTo(0f, size.height)
                wavePath.lineTo(0f, fillY)
                
                // Draw wave sine path
                val steps = 100
                val stepWidth = size.width / steps.toFloat()
                for (i in 0..steps) {
                    val x = i * stepWidth
                    // Amplitude changes based on charging state
                    val amplitude = if (isCharging) 12.dp.toPx() else 6.dp.toPx()
                    val y = fillY + sin((x / size.width) * (2f * Math.PI.toFloat()) + waveOffset) * amplitude
                    wavePath.lineTo(x, y)
                }
                wavePath.lineTo(size.width, size.height)
                wavePath.close()

                // Color the wave fluid: pink/peach if active bypass, else green or red
                val liquidColor = if (level <= 20) SoftRed else (if (isCharging) NeoGreen else NeoPink)
                drawPath(
                    path = wavePath,
                    color = liquidColor.copy(alpha = 0.25f)
                )
                
                // Draw a secondary slightly offset wave for an extremely premium layered fluid depth effect!
                val wavePath2 = Path()
                wavePath2.moveTo(0f, size.height)
                wavePath2.lineTo(0f, fillY)
                for (i in 0..steps) {
                    val x = i * stepWidth
                    // Opposite wave phase for depth
                    val amplitude = if (isCharging) 10.dp.toPx() else 5.dp.toPx()
                    val y = fillY + sin((x / size.width) * (2.2f * Math.PI.toFloat()) - waveOffset + 1f) * amplitude
                    wavePath2.lineTo(x, y)
                }
                wavePath2.lineTo(size.width, size.height)
                wavePath2.close()
                
                drawPath(
                    path = wavePath2,
                    color = liquidColor.copy(alpha = 0.45f)
                )
            }

            // Draw a subtle thin internal black border inside the crop circle to give it neobrutalist outline depth!
            drawArc(
                color = NeoDark,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth),
                topLeft = Offset(strokeWidth/2, strokeWidth/2),
                size = Size(size.width - strokeWidth, size.height - strokeWidth)
            )
        }

        // Concentric inner card layout for metadata displaying
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "LEVEL BATERAI",
                color = NeoSubtitle,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$level%",
                color = NeoDark,
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            val activeBypassed = ChargingController.isBypassMode.collectAsState().value
            Text(
                text = if (activeBypassed) "ACTIVE BYPASS BOOST⚡" else (if (isCharging) "CHARGING⚡" else "DISCHARGING🔋"),
                color = if (activeBypassed) NeoPink else if (isCharging) NeoGreen else NeoDark,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.2.sp
            )
        }
    }
}

@Composable
fun GridStatCard(
    title: String,
    value: String,
    iconSymbol: String,
    valueColor: Color = NeoDark,
    modifier: Modifier = Modifier
) {
    NeobrutalistCard(
        modifier = modifier.height(108.dp),
        containerColor = NeoWhite,
        shadowOffset = 3.dp
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
                    text = iconSymbol,
                    fontSize = 18.sp
                )
                Text(
                    text = title,
                    color = NeoSubtitle,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
            
            Text(
                text = value,
                color = valueColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}
