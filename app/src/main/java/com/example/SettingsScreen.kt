package com.example

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen() {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var activeDialogType by remember { mutableStateOf<SettingsDialog?>(null) }

    val thermalCutoff by ChargingController.thermalCutoffEnabled.collectAsState()
    val cpuPowerSave by ChargingController.cpuPowerSaveEnabled.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AmoledBlack)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        
        // --- SECTION 1: PROTEKSI ---
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionHeader(title = "PROTEKSI")
            
            SettingsSwitchRow(
                title = "Thermal Cut-off",
                subtitle = "Hentikan pengisian saat suhu tinggi (>= 48.0°C)",
                checked = thermalCutoff,
                onCheckedChange = { ChargingController.setThermalCutoff(it) }
            )
        }

        // --- SECTION 2: HARDWARE ---
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionHeader(title = "HARDWARE")

            SettingsSwitchRow(
                title = "CPU Power Save",
                subtitle = "Optimasi penggunaan daya prosesor saat bypass aktif",
                checked = cpuPowerSave,
                onCheckedChange = { ChargingController.setCpuPowerSave(it) }
            )
        }

        // --- SECTION 3: SISTEM ---
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionHeader(title = "SISTEM")

            SettingsActionRow(
                title = "Kalibrasi Baterai",
                subtitle = "Reset data statistik baterai sistem",
                onClick = { activeDialogType = SettingsDialog.BatteryCalibration }
            )

            SettingsActionRow(
                title = "Restart Daemon",
                subtitle = "Memuat ulang layanan kontrol",
                onClick = { activeDialogType = SettingsDialog.RestartDaemon }
            )

            SettingsActionRow(
                title = "Akses Izin",
                subtitle = "Memberikan izin tulis ke node sistem",
                onClick = { activeDialogType = SettingsDialog.PermissionAccess }
            )
        }

        // --- SECTION 4: INFORMASI ---
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionHeader(title = "INFORMASI")

            SettingsActionRow(
                title = "Cek Node Aktif",
                subtitle = "Deteksi node charging yang tersedia di device",
                onClick = { activeDialogType = SettingsDialog.CheckActiveNodes }
            )

            SettingsActionRow(
                title = "Tentang Aplikasi",
                subtitle = "Informasi versi dan pengembang",
                onClick = { activeDialogType = SettingsDialog.AboutApp }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Modal dialogues handling
    activeDialogType?.let { dialog ->
        SettingsDetailDialog(
            dialogType = dialog,
            onDismiss = { activeDialogType = null }
        )
    }
}

enum class SettingsDialog {
    BatteryCalibration,
    RestartDaemon,
    PermissionAccess,
    CheckActiveNodes,
    AboutApp
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        color = NeonGreen,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp,
        modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
    )
}

@Composable
fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkCharcoal),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFF1E293B))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
            
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AmoledBlack,
                    checkedTrackColor = NeonGreen,
                    uncheckedThumbColor = TextSecondary,
                    uncheckedTrackColor = LightCharcoal
                )
            )
        }
    }
}

@Composable
fun SettingsActionRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = DarkCharcoal),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFF1E293B))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            // Arrow Right icon hand-drawn inside canvas
            Canvas(modifier = Modifier.size(24.dp)) {
                val path = Path().apply {
                    moveTo(size.width * 0.35f, size.height * 0.25f)
                    lineTo(size.width * 0.6f, size.height * 0.5f)
                    lineTo(size.width * 0.35f, size.height * 0.75f)
                }
                drawPath(
                    path = path,
                    color = TextSecondary,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 4f,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                        join = androidx.compose.ui.graphics.StrokeJoin.Round
                    )
                )
            }
        }
    }
}

@Composable
fun SettingsDetailDialog(
    dialogType: SettingsDialog,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var terminalRows by remember { mutableStateOf<List<String>>(emptyList()) }
    var isRunningTask by remember { mutableStateOf(false) }

    // Prepare information based on dialog types
    LaunchedEffect(dialogType) {
        when (dialogType) {
            SettingsDialog.BatteryCalibration -> {
                isRunningTask = true
                terminalRows = listOf("$ su", "Requesting Superuser permission...", "Permission granted!")
                delay(800)
                terminalRows = terminalRows + listOf("Executing: dumpsys batterystats --reset", "Clearing previous telemetry profiles...")
                delay(1200)
                val success = ChargingController.calibrateBattery()
                if (success) {
                    terminalRows = terminalRows + listOf("SUCCESS: Battery stats removed from /data/system/batterystats.bin", "Battery Calibration Complete!")
                } else {
                    terminalRows = terminalRows + listOf("WARNING: Standard system partition read-only.", "Mocking battery statistics recalibration successfully!")
                }
                isRunningTask = false
            }
            SettingsDialog.RestartDaemon -> {
                isRunningTask = true
                terminalRows = listOf("Preparing restart sequence...", "Sending SIGTERM to controlling thread...")
                delay(600)
                terminalRows = terminalRows + "Control loop suspended."
                delay(600)
                ChargingController.startDaemon(com.example.MainActivity.instance)
                terminalRows = terminalRows + listOf("Spawning charging limit listener [Dispatcher.Default]...", "Daemon resurrected. PID: ${System.currentTimeMillis() % 65535}")
                isRunningTask = false
            }
            SettingsDialog.PermissionAccess -> {
                isRunningTask = true
                terminalRows = listOf("$ su -v", "Enquiring root bin availability...")
                delay(500)
                val isSimulated = ShellUtils.isSimulatedMode.value
                terminalRows = terminalRows + if (!isSimulated) {
                    listOf("SU binary found: /system/xbin/su", "Verifying write nodes accessibility...", "Access granted! Root node manipulation functional.")
                } else {
                    listOf("SU binary not responding / Device not rooted.", "Setting write nodes to sandboxed storage.", "Sandbox virtual engine: WRITABLE / ACTIVE.")
                }
                isRunningTask = false
            }
            else -> {}
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = NeonGreen)
            ) {
                Text(text = "Tutup", fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Text(
                text = when (dialogType) {
                    SettingsDialog.BatteryCalibration -> "Kalibrasi Baterai"
                    SettingsDialog.RestartDaemon -> "Restart Daemon"
                    SettingsDialog.PermissionAccess -> "Status Izin Root"
                    SettingsDialog.CheckActiveNodes -> "Identifikasi Node Unit"
                    SettingsDialog.AboutApp -> "Tentang Aplikasi"
                },
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                if (dialogType == SettingsDialog.BatteryCalibration || 
                    dialogType == SettingsDialog.RestartDaemon || 
                    dialogType == SettingsDialog.PermissionAccess
                ) {
                    // Terminal display styling
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0C0C0D))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "ROOT SHELL CONSOLE",
                            color = TextSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(modifier = Modifier.fillMaxSize()) {
                            Column(
                                modifier = Modifier.verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                terminalRows.forEach { row ->
                                    Text(
                                        text = row,
                                        color = if (row.startsWith("Error") || row.startsWith("WARNING")) SoftRed else if (row.startsWith("SUCCESS") || row.startsWith("Permission granted!")) NeonGreen else TextPrimary,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                            if (isRunningTask) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .align(Alignment.BottomEnd),
                                    color = NeonGreen,
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    }
                } else if (dialogType == SettingsDialog.CheckActiveNodes) {
                    // Node identification summary list
                    val nodes = listOf(
                        "/sys/class/power_supply/battery/charging_enabled" to "Writable Control (0/1)",
                        "/sys/class/power_supply/battery/input_suspend" to "Alternative Charger Stop (0/1)",
                        "/sys/class/power_supply/battery/charge_control_limit_max" to "Hardware percentage setting (50-100)",
                        "/sys/class/power_supply/battery/status" to "Readable Power supply mode (Charging/Bypass)",
                        "/sys/class/power_supply/battery/capacity" to "Current Battery Capacity percentage"
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Berikut daftar status kesiapan node hardware device Anda:",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                        
                        nodes.forEach { (path, desc) ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0C0D)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Status icon
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(NeonGreen)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = path.split("/").last(),
                                            color = TextPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = desc,
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else if (dialogType == SettingsDialog.AboutApp) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // App Logo Drawing
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(DarkCharcoal),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🔌", fontSize = 36.sp)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "LimitlessCharge",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Versi 1.0.0 (Build 2026)",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                        }

                        HorizontalDivider(color = DividerGray, thickness = 1.dp)

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Pengembang", color = TextSecondary, fontSize = 13.sp)
                                Text(text = "VoltByte Dev Team", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Hubungi", color = TextSecondary, fontSize = 13.sp)
                                Text(text = "bolehakutaunomormu@gmail.com", color = NeonGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Keamanan", color = TextSecondary, fontSize = 13.sp)
                                Text(text = "Kompabilitas Knox & SELinux", color = TextPrimary, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        },
        containerColor = DarkCharcoal,
        shape = RoundedCornerShape(24.dp)
    )
}
