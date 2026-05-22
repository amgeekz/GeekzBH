package com.example

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun HomeScreen() {
    val serviceActive by ChargingController.isServiceActive.collectAsState()
    val limit by ChargingController.chargingLimit.collectAsState()
    val bypassMode by ChargingController.isBypassMode.collectAsState()
    val isSimulated by ShellUtils.isSimulatedMode.collectAsState()
    val currentBattery by ChargingController.batteryLevel.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AmoledBlack)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mode Header Indicator (Aesthetic indicator of operational backend mode)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(if (isSimulated) DarkCharcoal else Color(0x1F06B6D4))
                .clickable { ShellUtils.setSimulatedMode(!isSimulated) }
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (isSimulated) GlowCyan else TechGreen)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isSimulated) "SANDBOX SIMULATOR" else "HARDWARE SU ROOT",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
            Text(
                text = "CHANGE",
                color = GlowCyan,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }

        // 1. Service Status Card
        val serviceBorder = if (serviceActive) {
            BorderStroke(1.5.dp, GlowCyan.copy(alpha = 0.6f))
        } else {
            BorderStroke(1.dp, Color(0xFF1E293B))
        }
        val serviceBg = if (serviceActive) {
            Color(0xFF0F172A)
        } else {
            Color(0xFF0F172A).copy(alpha = 0.5f)
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = serviceBg),
            shape = RoundedCornerShape(20.dp),
            border = serviceBorder
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "DAEMON BACKGROUND SERVICE",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (serviceActive) "ACTIVE" else "DISABLED",
                        color = if (serviceActive) GlowCyan else TextSecondary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
                
                Switch(
                    checked = serviceActive,
                    onCheckedChange = { ChargingController.setServiceActive(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AmoledBlack,
                        checkedTrackColor = GlowCyan,
                        uncheckedThumbColor = TextSecondary,
                        uncheckedTrackColor = Color(0xFF1E293B)
                    )
                )
            }
        }

        // 2. Charging Limit Slider Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkCharcoal),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color(0xFF1E293B))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "CHARGE THRESHOLD CELL LIMIT",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Slider(
                    value = limit.toFloat(),
                    onValueChange = { ChargingController.setChargingLimit(it.toInt()) },
                    valueRange = 50f..100f,
                    colors = SliderDefaults.colors(
                        activeTrackColor = GlowCyan,
                        inactiveTrackColor = Color(0xFF1E293B),
                        thumbColor = GlowCyan
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                val lowerRange = (limit - 5).coerceAtLeast(40)
                Text(
                    text = "Threshold: $limit% (Evaluator: Restores inflow below $lowerRange%)",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // 3. Bypass Mode Control Card
        val bypassBorder = if (bypassMode) {
            BorderStroke(1.5.dp, GlowCyan.copy(alpha = 0.8f))
        } else {
            BorderStroke(1.dp, Color(0xFF1E293B))
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (bypassMode) TechBypassBg.copy(alpha = 0.35f) else DarkCharcoal
            ),
            shape = RoundedCornerShape(20.dp),
            border = bypassBorder
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "CHARGER LIMITS BYPASS MOD",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                val displayStatus = if (bypassMode) {
                    "ACTIVE (Bypassing charging throttle limit. Target: 4000mA)"
                } else {
                    "STANDBY (Standard charging throttle active: Max 2840mA)"
                }

                Text(
                    text = "STATUS: $displayStatus",
                    color = if (bypassMode) TechBypassBorder else TextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(20.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ON button
                    Button(
                        onClick = { ChargingController.setBypassMode(true) },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (bypassMode) GlowCyan else Color(0xFF1E293B),
                            contentColor = if (bypassMode) AmoledBlack else TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "FORCE ON",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            color = if (bypassMode) AmoledBlack else GlowCyan,
                            letterSpacing = 0.5.sp
                        )
                    }

                    // OFF button
                    Button(
                        onClick = { ChargingController.setBypassMode(false) },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E293B),
                            contentColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "OFF / AUTO",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            color = if (!bypassMode) SoftRed else TextSecondary,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}
