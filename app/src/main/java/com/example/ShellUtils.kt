package com.example

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object ShellUtils {
    private const val TAG = "ShellUtils"
    
    // Flow of shell logs to display in the UI console
    private val _shellLogs = MutableStateFlow<List<LogEntry>>(emptyList())
    val shellLogs = _shellLogs.asStateFlow()

    // Mode indicator: true = simulated (mock nodes), false = attempts real root
    private val _isSimulatedMode = MutableStateFlow(true)
    val isSimulatedMode = _isSimulatedMode.asStateFlow()

    // Real vs Simulated paths
    private var simulatedDir: File? = null

    data class LogEntry(
        val timestamp: Long,
        val command: String,
        val output: String,
        val isError: Boolean,
        val isRoot: Boolean
    )

    fun initialize(context: Context) {
        simulatedDir = File(context.filesDir, "sys_mock").apply {
            if (!exists()) {
                mkdirs()
            }
        }
        // Initialize simulated sysfs files if they don't exist
        setupSimulatedFiles()
        
        // Detect if root helper actually exists (su binary)
        val hasSu = checkSuBinary()
        _isSimulatedMode.value = !hasSu
        
        logLocal("System", "Service Initialized. Root found: $hasSu. Simulated Mode default: ${_isSimulatedMode.value}", false)
    }

    fun setSimulatedMode(enabled: Boolean) {
        _isSimulatedMode.value = enabled
        logLocal("System", "Operational mode toggled: " + if (enabled) "SIMULATOR (Sandbox)" else "REAL SYSTEM ROOT", false)
    }

    private fun checkSuBinary(): Boolean {
        listOf("/system/bin/su", "/system/xbin/su", "/sbin/su", "/system/sd/xbin/su", "/system/bin/failsafe/su", "/data/local/xbin/su", "/data/local/bin/su", "/data/local/su").forEach { path ->
            if (File(path).exists()) return true
        }
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("which", "su"))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            reader.readLine() != null
        } catch (e: Exception) {
            false
        }
    }

    private fun setupSimulatedFiles() {
        val dir = simulatedDir ?: return
        
        // Setup battery nodes structure
        writeSimulatedFile("charging_enabled", "1")
        writeSimulatedFile("input_suspend", "0")
        writeSimulatedFile("charge_control_limit_max", "100")
        
        // Read-only battery info nodes
        writeSimulatedFile("capacity", "47")
        writeSimulatedFile("temp", "479") // 47.9°C
        writeSimulatedFile("current_now", "2380000") // 2380mA in uA
        writeSimulatedFile("voltage_now", "4313000") // 4313mV in uV
        writeSimulatedFile("health", "Good")
        writeSimulatedFile("technology", "Li-poly")
        writeSimulatedFile("status", "Charging")
        writeSimulatedFile("cycle_count", "2079")
    }

    fun readNodeValue(nodeName: String): String {
        return if (_isSimulatedMode.value) {
            readSimulatedFile(nodeName)
        } else {
            readRealNode(nodeName)
        }
    }

    fun writeNodeValue(nodeName: String, value: String): Boolean {
        logLocal("SYSFS_WRITE", "Writing node [$nodeName] -> value [$value]", false)
        return if (_isSimulatedMode.value) {
            writeSimulatedFile(nodeName, value)
            true
        } else {
            writeRealNode(nodeName, value)
        }
    }

    private fun readSimulatedFile(fileName: String): String {
        val file = File(simulatedDir, fileName)
        if (!file.exists()) return ""
        return try {
            file.readText().trim()
        } catch (e: Exception) {
            ""
        }
    }

    private fun writeSimulatedFile(fileName: String, content: String) {
        val file = File(simulatedDir, fileName)
        try {
            file.writeText(content)
        } catch (e: Exception) {
            Log.e(TAG, "Fail to write simulated file $fileName", e)
        }
    }

    private fun readRealNode(nodeName: String): String {
        val nodePath = getRealNodePath(nodeName) ?: return ""
        val cmd = "cat $nodePath"
        val result = executeCmdSync(cmd, useRoot = false)
        if (result.isError) {
            // Retry with root if normal read fails
            val rootResult = executeCmdSync(cmd, useRoot = true)
            return rootResult.output.trim()
        }
        return result.output.trim()
    }

    private fun writeRealNode(nodeName: String, value: String): Boolean {
        val nodePath = getRealNodePath(nodeName) ?: return false
        val cmd = "echo $value > $nodePath"
        val result = executeCmdSync(cmd, useRoot = true)
        return !result.isError
    }

    private fun getRealNodePath(nodeName: String): String? {
        val baseBattery = "/sys/class/power_supply/battery"
        val possiblePaths = when (nodeName) {
            "charging_enabled" -> listOf("$baseBattery/charging_enabled", "$baseBattery/battery_charging_enabled", "$baseBattery/charge_enabled")
            "input_suspend" -> listOf("$baseBattery/input_suspend", "$baseBattery/charging_suspend")
            "charge_control_limit_max" -> listOf("$baseBattery/charge_control_limit_max", "$baseBattery/charge_control_limit")
            "capacity" -> listOf("$baseBattery/capacity")
            "temp" -> listOf("$baseBattery/temp", "$baseBattery/batt_temp")
            "current_now" -> listOf("$baseBattery/current_now", "$baseBattery/batt_current")
            "voltage_now" -> listOf("$baseBattery/voltage_now", "$baseBattery/batt_vol")
            "health" -> listOf("$baseBattery/health")
            "technology" -> listOf("$baseBattery/technology")
            "status" -> listOf("$baseBattery/status")
            "cycle_count" -> listOf("$baseBattery/cycle_count")
            else -> listOf("$baseBattery/$nodeName")
        }
        
        for (path in possiblePaths) {
            if (File(path).exists()) return path
        }
        return possiblePaths.firstOrNull()
    }

    fun executeCmdSync(command: String, useRoot: Boolean = true): CommandResult {
        val output = StringBuilder()
        var isError = false
        try {
            val process = if (useRoot) {
                Runtime.getRuntime().exec("su")
            } else {
                Runtime.getRuntime().exec("sh")
            }
            
            val writer = OutputStreamWriter(process.outputStream)
            writer.write(command + "\n")
            writer.write("exit\n")
            writer.flush()
            
            val exitCode = process.waitFor()
            
            val successReader = BufferedReader(InputStreamReader(process.inputStream))
            val errorReader = BufferedReader(InputStreamReader(process.errorStream))
            
            var line: String?
            while (successReader.readLine().also { line = it } != null) {
                output.appendLine(line)
            }
            
            val errorOutput = StringBuilder()
            while (errorReader.readLine().also { line = it } != null) {
                errorOutput.appendLine(line)
            }
            
            if (exitCode != 0 || errorOutput.isNotEmpty()) {
                isError = true
                if (errorOutput.isNotEmpty()) {
                    output.appendLine("Error Output: $errorOutput")
                }
            }
        } catch (e: Exception) {
            isError = true
            output.append("Process Execution Error: ${e.message}")
        }
        
        val result = CommandResult(command, output.toString(), isError)
        logCommand(result, useRoot)
        return result
    }

    private fun logCommand(result: CommandResult, isRoot: Boolean) {
        val entry = LogEntry(
            timestamp = System.currentTimeMillis(),
            command = result.command,
            output = result.output,
            isError = result.isError,
            isRoot = isRoot
        )
        val current = _shellLogs.value.toMutableList()
        current.add(0, entry) // Add at top (most recent first)
        if (current.size > 100) {
            current.removeAt(current.size - 1)
        }
        _shellLogs.value = current
        Log.d(TAG, "[${if (isRoot) "ROOT" else "SH"}] Cmd: ${result.command} -> Success: ${!result.isError}")
    }

    private fun logLocal(tag: String, text: String, isError: Boolean) {
        val entry = LogEntry(
            timestamp = System.currentTimeMillis(),
            command = "[$tag]",
            output = text,
            isError = isError,
            isRoot = false
        )
        val current = _shellLogs.value.toMutableList()
        current.add(0, entry)
        if (current.size > 100) {
            current.removeAt(current.size - 1)
        }
        _shellLogs.value = current
    }

    fun clearLogs() {
        _shellLogs.value = emptyList()
    }

    data class CommandResult(
        val command: String,
        val output: String,
        val isError: Boolean
    )
}
