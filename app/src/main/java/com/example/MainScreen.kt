package com.example

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

enum class Screen {
    Home,
    Status,
    Settings
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    var currentScreen by remember { mutableStateOf(Screen.Home) }
    var isConsoleExpanded by remember { mutableStateOf(false) }
    val logs by ShellUtils.shellLogs.collectAsState()

    val batteryPercent by ChargingController.batteryLevel.collectAsState()
    val isBypassActive by ChargingController.isBypassMode.collectAsState()
    val batteryStatus by ChargingController.batteryStatus.collectAsState()

    val isDeviceCharging = batteryStatus.lowercase().contains("charging") || 
                           batteryStatus.lowercase().contains("boost") || 
                           isBypassActive

    // Infinite transition for pulsing animation when charging is active
    val infiniteTransition = rememberInfiniteTransition(label = "battery_pulse")
    val pulsingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(NeoBg)) {
                // Simulated Android Status Bar (In line with Neobrutalist high contrast styling)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "02.35", // Locked aesthetic timestamp corresponding to the user's mockup image
                        color = NeoDark,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Network Rate mockup
                        Text(
                            text = if (isBypassActive) "4,22 MB/s" else "0,14 MB/s",
                            color = NeoDark,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black
                        )
                        
                        // Wi-Fi details
                        Text(
                            text = "📶 WiFi", 
                            color = NeoDark, 
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )

                        // Battery Level glyph
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$batteryPercent%",
                                color = if (batteryPercent > 20) NeoDark else SoftRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.graphicsLayer(alpha = if (isDeviceCharging) pulsingAlpha else 1.0f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isDeviceCharging) "⚡🔋" else "🔋",
                                color = NeoDark,
                                fontSize = 11.sp,
                                modifier = Modifier.graphicsLayer(alpha = if (isDeviceCharging) pulsingAlpha else 1.0f)
                            )
                        }
                    }
                }

                // Layout toolbar header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "LIMITLESS ",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = NeoDark,
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                text = "CHARGE",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = NeoPink,
                                letterSpacing = (-0.5).sp
                            )
                        }
                        Text(
                            text = when (currentScreen) {
                                Screen.Home -> "MODUL: ROOT BYPASS"
                                Screen.Status -> "MODUL: METRIK SEL"
                                Screen.Settings -> "MODUL: KELOLA DAEMON"
                            },
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            color = NeoSubtitle,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Secure status badge
                        Box(
                            modifier = Modifier
                                .border(BorderStroke(2.dp, NeoDark), shape = RoundedCornerShape(8.dp))
                                .background(NeoWhite, shape = RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(NeoGreen)
                                        .border(BorderStroke(1.dp, NeoDark), shape = RoundedCornerShape(50))
                                )
                                Text(
                                    text = "SECURE",
                                    color = NeoDark,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        // Terminal icon console logs toggle button (Neobrutalist box format)
                        Box(
                            modifier = Modifier
                                .border(BorderStroke(2.dp, NeoDark), shape = RoundedCornerShape(8.dp))
                                .background(if (isConsoleExpanded) NeoPink else NeoWhite, shape = RoundedCornerShape(8.dp))
                                .clickable { isConsoleExpanded = !isConsoleExpanded }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "LOGS (${logs.size})",
                                color = NeoDark,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            Column {
                // Interactive kernel terminal drawer drawer
                AnimatedVisibility(visible = isConsoleExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(NeoDark)
                            .border(BorderStroke(3.dp, NeoDark))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "BASH ROOT KERNEL CONSOLE LOGS",
                                color = NeoYellow,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Hapus",
                                color = Color(0xFFFF5252),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier
                                    .clickable { ShellUtils.clearLogs() }
                                    .padding(horizontal = 8.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        val logListState = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(logListState),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (logs.isEmpty()) {
                                Text(
                                    text = "~ Idle. Menunggu perintah root trigger...",
                                    color = Color(0xFFAFAFAF),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            } else {
                                logs.forEach { logEntry ->
                                    val time = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date(logEntry.timestamp))
                                    Text(
                                        text = "[$time] ${logEntry.command} -> ${logEntry.output.trim()}",
                                        color = if (logEntry.isError) Color(0xFFFF5252) else if (logEntry.isRoot) NeoGreen else NeoWhite,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // HIGH FIDELITY NEOBRUTALIST CUSTOM TAB SELECTOR BAR
                // Styled with bold black card frames, solid shadows, and peach highlights
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NeoBg)
                        .padding(bottom = 12.dp, start = 12.dp, end = 12.dp, top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val tabs = listOf(
                        Triple(Screen.Home, "🏠", "Home"),
                        Triple(Screen.Status, "⚡", "Status"),
                        Triple(Screen.Settings, "⚙️", "Settings")
                    )
                    tabs.forEach { (scr, icon, label) ->
                        val isSelected = currentScreen == scr
                        val btnBg = if (isSelected) NeoPink else NeoWhite
                        
                        NeobrutalistCard(
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            containerColor = btnBg,
                            shadowOffset = if (isSelected) 2.dp else 4.dp,
                            borderWidth = 2.5.dp,
                            onClick = { currentScreen = scr }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(text = icon, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = label,
                                        color = NeoDark,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        containerColor = NeoBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(NeoBg)
        ) {
            when (currentScreen) {
                Screen.Home -> HomeScreen()
                Screen.Status -> StatusScreen()
                Screen.Settings -> SettingsScreen()
            }
        }
    }
}
