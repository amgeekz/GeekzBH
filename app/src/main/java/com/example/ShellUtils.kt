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

    // Mode indicator: true = root granted, false = root not granted
    private val _isRootGranted = MutableStateFlow(false)
    val isRootGranted = _isRootGranted.asStateFlow()

    data class LogEntry(
        val timestamp: Long,
        val command: String,
        val output: String,
        val isError: Boolean,
        val isRoot: Boolean
    )

    fun initialize(context: Context) {
        checkRootPermission()
        if (_isRootGranted.value) {
            executeCmdSync("setenforce 0", useRoot = true, logToConsole = false)
        }
        logLocal("System", "LimitlessCharge Initialized. Root access: ${_isRootGranted.value}", false)
    }

    fun checkRootPermission() {
        val hasSu = checkSuBinary()
        if (hasSu) {
            val result = executeCmdSync("id", useRoot = true)
            _isRootGranted.value = !result.isError && result.output.contains("uid=0")
        } else {
            _isRootGranted.value = false
        }
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

    fun readNodeValue(nodeName: String): String {
        return readRealNode(nodeName)
    }

    fun writeNodeValue(nodeName: String, value: String): Boolean {
        logLocal("SYSFS_WRITE", "Writing node [$nodeName] -> value [$value]", false)
        return writeRealNode(nodeName, value)
    }

    private fun readRealNode(nodeName: String): String {
        val nodePath = getRealNodePath(nodeName) ?: return ""
        val cmd = "chmod 644 $nodePath 2>/dev/null; cat $nodePath"
        val useRoot = _isRootGranted.value
        val result = executeCmdSync(cmd, useRoot = useRoot, logToConsole = false)
        if (result.isError && !useRoot) {
            // Retry with root if normal read fails and we hadn't already tried it as root
            val rootResult = executeCmdSync(cmd, useRoot = true, logToConsole = false)
            return rootResult.output.trim()
        }
        return result.output.trim()
    }

    private fun writeRealNode(nodeName: String, value: String): Boolean {
        val nodePath = getRealNodePath(nodeName) ?: return false
        val cmd = "setenforce 0 2>/dev/null; chmod 666 $nodePath 2>/dev/null; echo $value > $nodePath"
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

    fun executeCmdSync(command: String, useRoot: Boolean = true, logToConsole: Boolean = true): CommandResult {
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
            } else {
                // Self-healing check: if command was executed with root and succeeded,
                // we can confirm root is granted and update the state.
                if (useRoot && !_isRootGranted.value) {
                    _isRootGranted.value = true
                }
            }
        } catch (e: Exception) {
            isError = true
            output.append("Process Execution Error: ${e.message}")
        }
        
        val result = CommandResult(command, output.toString(), isError)
        if (logToConsole) {
            logCommand(result, useRoot)
        } else {
            Log.d(TAG, "[${if (useRoot) "ROOT" else "SH"}] (NoConsoleLog) Cmd: ${result.command} -> Success: ${!result.isError}")
        }
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
