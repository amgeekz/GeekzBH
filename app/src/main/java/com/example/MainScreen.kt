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
    Settings,
    Logs
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    var currentScreen by remember { mutableStateOf(Screen.Home) }
    val logs by ShellUtils.shellLogs.collectAsState()

    Scaffold(
        topBar = {
            if (currentScreen != Screen.Logs) {
                Column(modifier = Modifier.statusBarsPadding().background(NeoBg)) {
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
                                    Screen.Logs -> "MODUL: KERNEL LOGS"
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
                                    .background(if (currentScreen == Screen.Logs) NeoPink else NeoWhite, shape = RoundedCornerShape(8.dp))
                                    .clickable { currentScreen = Screen.Logs }
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
            }
        },
        bottomBar = {
            if (currentScreen != Screen.Logs) {
                Column {
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
                Screen.Logs -> LogsScreen(onBackToHome = { currentScreen = Screen.Home })
            }
        }
    }
}

@Composable
fun LogsScreen(onBackToHome: () -> Unit) {
    val logs by ShellUtils.shellLogs.collectAsState()
    val logListState = rememberScrollState()

    // Smooth scroll logs to bottom when entries are added
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            logListState.animateScrollTo(logListState.maxValue)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NeoBg)
            .padding(16.dp)
    ) {
        // High visibility Neobrutalist Headers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NeobrutalistCard(
                modifier = Modifier.height(40.dp),
                containerColor = NeoWhite,
                shadowOffset = 2.dp,
                borderWidth = 2.dp,
                onClick = onBackToHome
            ) {
                Row(
                    modifier = Modifier.fillMaxHeight().padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "⬅ KEMBALI",
                        color = NeoDark,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Text(
                text = "KERNEL SYSTEM LOGS",
                color = NeoDark,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )

            NeobrutalistCard(
                modifier = Modifier.height(40.dp),
                containerColor = Color(0xFFFFECEF),
                borderColor = SoftRed,
                shadowOffset = 2.dp,
                borderWidth = 2.dp,
                onClick = { ShellUtils.clearLogs() }
            ) {
                Row(
                    modifier = Modifier.fillMaxHeight().padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "HAPUS",
                        color = Color(0xFFDC2626),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Full terminal interface container with solid bounds
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .border(BorderStroke(2.5.dp, NeoDark), shape = RoundedCornerShape(12.dp))
                .background(Color(0xFF0F172A), shape = RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(logListState),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (logs.isEmpty()) {
                    Text(
                        text = "~ Terminal idle. Menunggu aktivitas root daemon...\n\n~ Semua telemetry sensor battery, kontrol bypass, serta pembacaan kernel node sysfs akan tercatat secara real-time di console terminal sistem ini.",
                        color = Color(0xFFAFAFAF),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 16.sp
                    )
                } else {
                    logs.forEach { logEntry ->
                        val time = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date(logEntry.timestamp))
                        Text(
                            text = "[$time] ${logEntry.command}\n└─> ${logEntry.output.trim()}",
                            color = if (logEntry.isError) Color(0xFFFF5252) else if (logEntry.isRoot) NeoGreen else NeoWhite,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Large visual confirmation back button
        NeobrutalistCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            containerColor = NeoGreen,
            shadowOffset = 4.dp,
            borderWidth = 2.5.dp,
            onClick = onBackToHome
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "KEMBALI KE HOME SCREEN 🏠",
                    color = NeoWhite,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
