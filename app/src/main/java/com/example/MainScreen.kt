package com.example

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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

    // Format current local simulated device status bar clock
    val timeFormat = SimpleDateFormat("HH.mm", Locale.getDefault())
    val formattedTime = timeFormat.format(Date())

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(AmoledBlack)) {
                // Simulated Android Status Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "02.35", // Locked aesthetic timestamp corresponding to the user's mockup image
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Network Rate mockup
                        Text(
                            text = if (isBypassActive) "0,00 MB/s" else "0,14 MB/s",
                            color = TextPrimary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        
                        // Wi-Fi glyph emoji
                        Text(text = "📶📶📶 WiFi", color = TextPrimary, fontSize = 9.sp)

                        // Battery Level glyph
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$batteryPercent%",
                                color = if (batteryPercent > 20) NeonGreen else SoftRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "🔋", color = TextPrimary, fontSize = 11.sp)
                        }
                    }
                }

                // Layout toolbar header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "CORE-X ",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                text = "POWER",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = GlowCyan,
                                letterSpacing = (-0.5).sp
                            )
                        }
                        Text(
                            text = when (currentScreen) {
                                Screen.Home -> "MODULE: ROOT CONTROL"
                                Screen.Status -> "MODULE: CELL METRICS"
                                Screen.Settings -> "MODULE: HARDWARE PREFS"
                            },
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 1.sp
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Secure status badge
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(DarkCharcoal)
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(TechGreen)
                            )
                            Text(
                                text = "SECURE",
                                color = TextPrimary,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Terminal icon console logs toggle button
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(DarkCharcoal)
                                .clickable { isConsoleExpanded = !isConsoleExpanded }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LOGS (${logs.size})",
                                color = if (logs.isNotEmpty()) GlowCyan else TextSecondary,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            // Navigation items matching exactly the bottom navbar from the layout screenshots
            Column {
                // Interactive kernel terminal drawer drawer
                AnimatedVisibility(visible = isConsoleExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(Color(0xFF070708))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "BASH ROOT KERNEL SYSTEM CONSOLE LOGS",
                                color = NeonGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Hapus",
                                color = SoftRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
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
                                    text = "~ Idle. No root commands issued yet.",
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            } else {
                                logs.forEach { logEntry ->
                                    val time = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date(logEntry.timestamp))
                                    Text(
                                        text = "[$time] ${logEntry.command} -> ${logEntry.output.trim()}",
                                        color = if (logEntry.isError) SoftRed else if (logEntry.isRoot) NeonGreen else TextPrimary,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }

                NavigationBar(
                    containerColor = Color(0xFF0C0C0D),
                    tonalElevation = 12.dp,
                    modifier = Modifier.height(72.dp)
                ) {
                    // Item 1: Home Tab
                    val isHome = currentScreen == Screen.Home
                    NavigationBarItem(
                        selected = isHome,
                        onClick = { currentScreen = Screen.Home },
                        icon = {
                            Text(
                                text = "🏠",
                                fontSize = if (isHome) 22.sp else 18.sp
                            )
                        },
                        label = {
                            Text(
                                text = "Home",
                                fontSize = 11.sp,
                                fontWeight = if (isHome) FontWeight.Bold else FontWeight.Medium,
                                color = if (isHome) NeonGreen else TextSecondary
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = DarkCharcoal,
                            selectedIconColor = NeonGreen,
                            unselectedIconColor = TextSecondary
                        )
                    )

                    // Item 2: Status Tab
                    val isStats = currentScreen == Screen.Status
                    NavigationBarItem(
                        selected = isStats,
                        onClick = { currentScreen = Screen.Status },
                        icon = {
                            Text(
                                text = "⚡",
                                fontSize = if (isStats) 22.sp else 18.sp
                            )
                        },
                        label = {
                            Text(
                                text = "Status",
                                fontSize = 11.sp,
                                fontWeight = if (isStats) FontWeight.Bold else FontWeight.Medium,
                                color = if (isStats) NeonGreen else TextSecondary
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = DarkCharcoal,
                            selectedIconColor = NeonGreen,
                            unselectedIconColor = TextSecondary
                        )
                    )

                    // Item 3: Settings Tab
                    val isSettings = currentScreen == Screen.Settings
                    NavigationBarItem(
                        selected = isSettings,
                        onClick = { currentScreen = Screen.Settings },
                        icon = {
                            Text(
                                text = "⚙️",
                                fontSize = if (isSettings) 22.sp else 18.sp
                            )
                        },
                        label = {
                            Text(
                                text = "Settings",
                                fontSize = 11.sp,
                                fontWeight = if (isSettings) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSettings) NeonGreen else TextSecondary
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = DarkCharcoal,
                            selectedIconColor = NeonGreen,
                            unselectedIconColor = TextSecondary
                        )
                    )
                }
            }
        },
        containerColor = AmoledBlack
    ) { innerPadding ->
        // Animated transition switching screens smoothly
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(AmoledBlack)
        ) {
            when (currentScreen) {
                Screen.Home -> HomeScreen()
                Screen.Status -> StatusScreen()
                Screen.Settings -> SettingsScreen()
            }
        }
    }
}
