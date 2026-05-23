package com.example

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    val isRootGranted by ShellUtils.isRootGranted.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NeoBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Neobrutalist Disclaimer box (Matches top disclaimer layout from user reference image)
        NeobrutalistCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = NeoYellow,
            shadowOffset = 3.dp
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(text = "⚠️", fontSize = 16.sp, modifier = Modifier.padding(top = 2.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Disclaimer & Keamanan",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeoDark
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "LimitlessCharge mengontrol langsung node kernel kelistrikan hardware. Gunakan fitur Bypass secara bijak untuk mencegah thermal overheating.",
                        fontSize = 11.sp,
                        color = NeoDark.copy(alpha = 0.85f),
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // 2. ROOT Access status card
        val rootBg = if (isRootGranted) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
        NeobrutalistCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = rootBg,
            shadowOffset = 3.dp,
            onClick = { ShellUtils.checkRootPermission() }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(50))
                            .background(if (isRootGranted) NeoGreen else SoftRed)
                            .border(BorderStroke(1.5.dp, NeoDark), shape = RoundedCornerShape(50))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isRootGranted) "ROOT ACCESS: GRANTED (ACTIVE)" else "ROOT ACCESS: DENIED / NOT FOUND",
                        color = NeoDark,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.1.sp
                    )
                }
                Text(
                    text = "RE-CHECK",
                    color = NeoPink,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
            }
        }

        // 3. Daemon Background Service card
        val serviceBgColor = if (serviceActive) Color(0xFFE2F9EE) else Color(0xFFFFF2F2)
        val serviceBorderColor = if (serviceActive) NeoGreen else SoftRed
        val serviceTextColor = if (serviceActive) NeoGreen else SoftRed

        NeobrutalistCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = serviceBgColor,
            borderColor = serviceBorderColor,
            borderWidth = 1.5.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "DAEMON BACKGROUND SERVICE",
                        color = NeoSubtitle,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (serviceActive) "SERVICE: RUNNING" else "SERVICE: STOPPED",
                        color = serviceTextColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.2.sp
                    )
                }
                
                Switch(
                    checked = serviceActive,
                    onCheckedChange = { ChargingController.setServiceActive(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NeoWhite,
                        checkedTrackColor = if (serviceActive) NeoGreen else NeoDark,
                        uncheckedThumbColor = NeoSubtitle,
                        uncheckedTrackColor = Color(0xFFE2E8F0)
                    )
                )
            }
        }

        // 4. Charging Limit Slider Card
        NeobrutalistCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = NeoWhite
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Text(
                    text = "CHARGE THRESHOLD CELL LIMIT",
                    color = NeoSubtitle,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
                
                Slider(
                    value = limit.toFloat(),
                    onValueChange = { ChargingController.setChargingLimit(it.toInt()) },
                    valueRange = 50f..100f,
                    colors = SliderDefaults.colors(
                        activeTrackColor = NeoDark,
                        inactiveTrackColor = Color(0xFFE2E8F0),
                        thumbColor = NeoPink
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                val lowerRange = (limit - 5).coerceAtLeast(40)
                Text(
                    text = "Threshold Limit: $limit% (Restores charge at <$lowerRange%)",
                    color = NeoDark,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 5. Bypass Mode Control Card
        val bypassBgColor = if (bypassMode) Color(0xFFE2F9EE) else Color(0xFFFFF2F2)
        val bypassBorderColor = if (bypassMode) NeoGreen else SoftRed

        NeobrutalistCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = bypassBgColor,
            borderColor = bypassBorderColor,
            borderWidth = 1.5.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Text(
                    text = "CHARGER LIMITS BYPASS MOD",
                    color = NeoSubtitle,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                
                val displayStatus = if (bypassMode) {
                    "ACTIVE (Bypassing restrictions. Inflow: 4000mA)"
                } else {
                    "STANDBY (Standard restrictions. Max: 2840mA)"
                }

                Text(
                    text = "STATUS: $displayStatus",
                    color = if (bypassMode) NeoGreen else SoftRed,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(18.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // ON button
                    Button(
                        onClick = { ChargingController.setBypassMode(true) },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .border(BorderStroke(2.5.dp, NeoDark), shape = RoundedCornerShape(10.dp)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (bypassMode) NeoGreen else NeoWhite,
                            contentColor = if (bypassMode) NeoWhite else NeoDark
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "FORCE ON",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.2.sp
                        )
                    }

                    // OFF button
                    Button(
                        onClick = { ChargingController.setBypassMode(false) },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .border(BorderStroke(2.5.dp, NeoDark), shape = RoundedCornerShape(10.dp)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!bypassMode) SoftRed else NeoWhite,
                            contentColor = if (!bypassMode) NeoWhite else NeoDark
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "OFF / AUTO",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.2.sp
                        )
                    }
                }
            }
        }
    }
}
