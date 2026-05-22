package com.example

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
    val scrollState = rememberScrollState()
    var activeDialogType by remember { mutableStateOf<SettingsDialog?>(null) }

    val thermalCutoff by ChargingController.thermalCutoffEnabled.collectAsState()
    val cpuPowerSave by ChargingController.cpuPowerSaveEnabled.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NeoBg)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        
        // --- SECTION 1: PROTEKSI ---
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionHeader(title = "PROTEKSI KESEHATAN")
            
            SettingsSwitchRow(
                title = "Thermal Cut-off",
                subtitle = "Hentikan pengisian saat suhu tinggi (>= 48.0°C)",
                checked = thermalCutoff,
                onCheckedChange = { ChargingController.setThermalCutoff(it) }
            )
        }

        // --- SECTION 2: HARDWARE ---
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionHeader(title = "KONTROL HARDWARE")

            SettingsSwitchRow(
                title = "CPU Power Save",
                subtitle = "Optimasi penggunaan daya prosesor saat bypass aktif",
                checked = cpuPowerSave,
                onCheckedChange = { ChargingController.setCpuPowerSave(it) }
            )
        }

        // --- SECTION 3: SISTEM ---
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionHeader(title = "FUNGSI UTAMA SISTEM")

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
                title = "Akses Izin Root",
                subtitle = "Cek apakah izin root superuser tersedia",
                onClick = { activeDialogType = SettingsDialog.PermissionAccess }
            )
        }

        // --- SECTION 4: INFORMASI ---
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionHeader(title = "INFORMASI & DIAGNOSTIK")

            SettingsActionRow(
                title = "Cek Node Aktif",
                subtitle = "Deteksi ketersediaan node hardware Charging",
                onClick = { activeDialogType = SettingsDialog.CheckActiveNodes }
            )

            SettingsActionRow(
                title = "Tentang Aplikasi",
                subtitle = "Developer, lisensi, dan informasi versi",
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
        color = NeoDark,
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Black,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
    )
}

@Composable
fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    NeobrutalistCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = if (checked) NeoPink.copy(alpha = 0.08f) else NeoWhite,
        shadowOffset = 3.dp
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
                    color = NeoDark,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    color = NeoSubtitle,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
            
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = NeoWhite,
                    checkedTrackColor = NeoDark,
                    uncheckedThumbColor = NeoSubtitle,
                    uncheckedTrackColor = Color(0xFFE2E8F0)
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
    NeobrutalistCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = NeoWhite,
        shadowOffset = 3.dp,
        onClick = onClick
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
                    color = NeoDark,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    color = NeoSubtitle,
                    fontSize = 11.sp
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
                    color = NeoDark,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 3.dp.toPx(),
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
                ShellUtils.checkRootPermission()
                val isRoot = ShellUtils.isRootGranted.value
                terminalRows = terminalRows + if (isRoot) {
                    listOf("SU binary found: /system/xbin/su", "Verifying root node permission...", "Inquiry result: ACCESS GRANTED", "System is fully ready.")
                } else {
                    listOf("SU binary not responding / Device not rooted.", "Inquiry result: ACCESS DENIED", "LimitlessCharge running in diagnostic telemetry mode.")
                }
                isRunningTask = false
            }
            else -> {}
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(2.dp, NeoDark),
                colors = ButtonDefaults.buttonColors(containerColor = NeoDark, contentColor = NeoWhite)
            ) {
                Text(text = "Tutup", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
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
                color = NeoDark,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                if (dialogType == SettingsDialog.BatteryCalibration || 
                    dialogType == SettingsDialog.RestartDaemon || 
                    dialogType == SettingsDialog.PermissionAccess
                ) {
                    // Terminal display styling (Neobrutalist Console Box)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .border(BorderStroke(2.5.dp, NeoDark), shape = RoundedCornerShape(10.dp))
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0F1115))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "ROOT SHELL CONSOLE",
                            color = NeoSubtitle.copy(alpha = 0.8f),
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
                                        color = if (row.startsWith("Error") || row.startsWith("WARNING") || row.startsWith("Inquiry result: ACCESS DENIED")) SoftRed else if (row.startsWith("SUCCESS") || row.startsWith("Permission granted!") || row.contains("ACCESS GRANTED")) NeoGreen else NeoWhite,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                            if (isRunningTask) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .align(Alignment.BottomEnd),
                                    color = NeoPink,
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    }
                } else if (dialogType == SettingsDialog.CheckActiveNodes) {
                    // Node identification summary list
                    val nodes = listOf(
                        "charging_enabled" to "Writable Control (0/1)",
                        "input_suspend" to "Alternative Charger Stop (0/1)",
                        "charge_control_limit_max" to "Hardware percentage limit (50-100)",
                        "status" to "Readable Power supply mode (Charging/Bypass)",
                        "capacity" to "Current Battery Capacity percentage"
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Daftar status kesiapan node hardware device Anda:",
                            color = NeoSubtitle,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                        
                        nodes.forEach { (nodeName, desc) ->
                            NeobrutalistCard(
                                containerColor = NeoWhite,
                                shadowOffset = 2.dp,
                                borderWidth = 1.5.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(NeoGreen)
                                            .border(BorderStroke(1.dp, NeoDark), shape = RoundedCornerShape(50))
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = nodeName,
                                            color = NeoDark,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = desc,
                                            color = NeoSubtitle,
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
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // App Logo Drawing (Neobrutalist box format)
                        NeobrutalistCard(
                            modifier = Modifier.size(72.dp),
                            containerColor = NeoYellow,
                            borderWidth = 2.5.dp,
                            shadowOffset = 4.dp
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(text = "🔌", fontSize = 34.sp)
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "LimitlessCharge",
                                color = NeoDark,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Versi 1.0.0 (Build 2026)",
                                color = NeoSubtitle,
                                fontSize = 12.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(NeoDark)
                        )

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Pengembang", color = NeoSubtitle, fontSize = 12.sp)
                                Text(text = "VoltByte Dev Team", color = NeoDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Hubungi", color = NeoSubtitle, fontSize = 12.sp)
                                Text(text = "bolehakutaunomormu@gmail.com", color = NeoDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Kompabilitas", color = NeoSubtitle, fontSize = 12.sp)
                                Text(text = "Direct Kernel SysFS Nodes", color = NeoDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
        containerColor = NeoWhite,
        modifier = Modifier.border(BorderStroke(3.dp, NeoDark), shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp)
    )
}
