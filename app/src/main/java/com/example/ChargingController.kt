package com.example

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

object ChargingController {
    private const val TAG = "ChargingController"
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var daemonJob: Job? = null
    private var telemetryJob: Job? = null

    // State flows for UI observations
    private val _isServiceActive = MutableStateFlow(true)
    val isServiceActive = _isServiceActive.asStateFlow()

    private val _chargingLimit = MutableStateFlow(80) // default limit 80% to preserve lifespan
    val chargingLimit = _chargingLimit.asStateFlow()

    private val _isBypassMode = MutableStateFlow(false)
    val isBypassMode = _isBypassMode.asStateFlow()

    private val _thermalCutoffEnabled = MutableStateFlow(true)
    val thermalCutoffEnabled = _thermalCutoffEnabled.asStateFlow()

    private val _cpuPowerSaveEnabled = MutableStateFlow(false)
    val cpuPowerSaveEnabled = _cpuPowerSaveEnabled.asStateFlow()

    // Battery Live Metrics
    private val _batteryLevel = MutableStateFlow(47)
    val batteryLevel = _batteryLevel.asStateFlow()

    private val _batteryTemp = MutableStateFlow(38.5f) // °C
    val batteryTemp = _batteryTemp.asStateFlow()

    private val _batteryCurrent = MutableStateFlow(2380) // mA
    val batteryCurrent = _batteryCurrent.asStateFlow()

    private val _batteryVoltage = MutableStateFlow(4313) // mV
    val batteryVoltage = _batteryVoltage.asStateFlow()

    private val _batteryStatus = MutableStateFlow("Charging")
    val batteryStatus = _batteryStatus.asStateFlow()

    private val _powerUsageWatts = MutableStateFlow(10.26f) // Watt
    val powerUsageWatts = _powerUsageWatts.asStateFlow()

    // Battery static specs
    val health = "Good"
    val technology = "Li-poly"
    val maxCapacityMah = 5000
    val cycleCount = 2079

    fun startDaemon(context: Context) {
        daemonJob?.cancel()
        telemetryJob?.cancel()

        // Sync initial values from sysfs mock if any
        syncConfigFromNodes()

        // Start background control loop
        daemonJob = scope.launch {
            while (isActive) {
                try {
                    runControlLogic()
                } catch (e: Exception) {
                    Log.e(TAG, "Error in daemon battery check logic", e)
                }
                delay(4000) // Check every 4 seconds
            }
        }

        // Start status telemetry updates (e.g. simulating battery drain/charging)
        telemetryJob = scope.launch {
            while (isActive) {
                try {
                    updateTelemetry(context)
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating battery telemetry", e)
                }
                delay(2000) // Update metrics every 2 seconds
            }
        }
    }

    fun setServiceActive(active: Boolean) {
        _isServiceActive.value = active
        // Apply instantly
        scope.launch { runControlLogic() }
    }

    fun setChargingLimit(limit: Int) {
        _chargingLimit.value = limit
        ShellUtils.writeNodeValue("charge_control_limit_max", limit.toString())
        scope.launch { runControlLogic() }
    }

    fun setBypassMode(active: Boolean) {
        _isBypassMode.value = active
        scope.launch { runControlLogic() }
    }

    fun setThermalCutoff(enabled: Boolean) {
        _thermalCutoffEnabled.value = enabled
    }

    fun setCpuPowerSave(enabled: Boolean) {
        _cpuPowerSaveEnabled.value = enabled
        // Simulate root CPU limiting / policy changes
        if (enabled) {
            ShellUtils.executeCmdSync("echo 1200000 > /sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq", useRoot = true)
        } else {
            ShellUtils.executeCmdSync("echo 2400000 > /sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq", useRoot = true)
        }
    }

    fun calibrateBattery(): Boolean {
        // Runs battery stats reset
        val result1 = ShellUtils.executeCmdSync("dumpsys batterystats --reset", useRoot = true)
        val result2 = ShellUtils.executeCmdSync("rm /data/system/batterystats.bin", useRoot = true)
        return !result1.isError || !result2.isError
    }

    private fun syncConfigFromNodes() {
        val limitStr = ShellUtils.readNodeValue("charge_control_limit_max")
        val limit = limitStr.toIntOrNull()
        if (limit != null && limit in 50..100) {
            _chargingLimit.value = limit
        }
    }

    /**
     * core charging bypass evaluation module
     */
    private fun runControlLogic() {
        if (!_isServiceActive.value) {
            // Service INACTIVE: Restore standard charging parameters (turn on charging, disable input suspend)
            ShellUtils.writeNodeValue("charging_enabled", "1")
            ShellUtils.writeNodeValue("input_suspend", "0")
            return
        }

        val level = _batteryLevel.value
        val limit = _chargingLimit.value
        val bypass = _isBypassMode.value
        val thermalCutoff = _thermalCutoffEnabled.value
        val temp = _batteryTemp.value

        // Safety Cut-off: if temperatures exceed 48°C and thermal cutoff is enabled, forcefully cut off charging
        if (thermalCutoff && temp >= 48.0f) {
            ShellUtils.writeNodeValue("charging_enabled", "0")
            ShellUtils.writeNodeValue("input_suspend", "1")
            Log.w(TAG, "Thermal limit triggered ($temp°C). Suspending charging parameters!")
            return
        }

        // Logic check
        if (bypass) {
            // Bypass Mode IS FORCED ON: Override the max charge current to 4000mA and bypass thermal throttling limits!
            ShellUtils.writeNodeValue("charging_enabled", "1")
            ShellUtils.writeNodeValue("input_suspend", "0")
            ShellUtils.writeNodeValue("fast_charge", "1")
            ShellUtils.writeNodeValue("charge_current_max", "4000000") // 4000mA (Bypass)
        } else {
            // Bypass Mode disabled: Restore standard current limits
            ShellUtils.writeNodeValue("fast_charge", "0")
            ShellUtils.writeNodeValue("charge_current_max", "2840000") // 2840mA (Standard)

            // Evaluated limit checkpoint
            if (level >= limit) {
                // Limit reached. Suspend charging (act like bypass mode is engaged automatically at limit)
                ShellUtils.writeNodeValue("charging_enabled", "0")
                ShellUtils.writeNodeValue("input_suspend", "1")
            } else {
                // If it's below limit, make sure standard charging is active! (Fixes non-responsive OFF button bug)
                ShellUtils.writeNodeValue("charging_enabled", "1")
                ShellUtils.writeNodeValue("input_suspend", "0")
            }
        }
    }

    private fun updateTelemetry(context: Context) {
        if (ShellUtils.isSimulatedMode.value) {
            // Simulation logic: simulates gradual charging/draining behavior so it feels extremely real!
            val service = _isServiceActive.value
            val limit = _chargingLimit.value
            val bypass = _isBypassMode.value
            var level = _batteryLevel.value
            var current = _batteryCurrent.value
            var temp = _batteryTemp.value
            var voltage = _batteryVoltage.value
            var statusStr = _batteryStatus.value

            val chargingInNode = ShellUtils.readNodeValue("charging_enabled") == "1" && 
                               ShellUtils.readNodeValue("input_suspend") == "0"

            if (chargingInNode) {
                if (bypass) {
                    statusStr = "Bypass Mode Boost"
                    if (level < 100) {
                        // Increase mock battery percentage slightly faster because of fast bypass charge
                        if (Math.random() < 0.23) {
                            level += 1
                        }
                    }
                    // Current is boosted to bypassed limit (4000mA range)
                    current = (3950..4210).random()
                    voltage = 3700 + (level * 8)
                    if (temp < 46.5f) {
                        temp += 0.12f
                    }
                } else {
                    // Simulating normal charging
                    statusStr = "Charging"
                    if (level < 100) {
                        // Increase mock battery percentage over time
                        if (Math.random() < 0.15) {
                            level += 1
                        }
                    }
                    
                    // Set standard charge currents
                    current = if (level > 85) 950 else 2380
                    voltage = 3700 + (level * 8)
                    
                    // Slow temp rise during charge
                    if (temp < 43.5f) {
                        temp += 0.05f
                    }
                }
            } else {
                // Charging is suspended (either bypass active or limit reached)
                if (level >= limit) {
                    statusStr = "Limit Suspended"
                    current = 0 // charger powers mainboard, battery is idle
                    if (temp > 34.0f) {
                        temp -= 0.1f // cool down since battery is not charging
                    }
                } else {
                    // Standard discharging
                    statusStr = "Discharging"
                    if (level > 0 && Math.random() < 0.08) {
                        level -= 1
                    }
                    current = -280
                    voltage = 3700 + (level * 6)
                    
                    if (temp > 32.0f) {
                        temp -= 0.05f
                    }
                }
            }

            // Sync simulation mock files
            ShellUtils.writeNodeValue("capacity", level.toString())
            ShellUtils.writeNodeValue("temp", (temp * 10).toInt().toString())
            ShellUtils.writeNodeValue("current_now", (current * 1000).toString())
            ShellUtils.writeNodeValue("voltage_now", (voltage * 1000).toString())
            ShellUtils.writeNodeValue("status", statusStr)

            _batteryLevel.value = level
            _batteryTemp.value = ((temp * 10).toInt() / 10.0f)
            _batteryCurrent.value = current
            _batteryVoltage.value = voltage
            _batteryStatus.value = statusStr
            _powerUsageWatts.value = Math.abs(current * voltage) / 1000000.0f
        } else {
            // Read from actual real nodes
            val levelStr = ShellUtils.readNodeValue("capacity")
            val tempStr = ShellUtils.readNodeValue("temp")
            val currentStr = ShellUtils.readNodeValue("current_now")
            val voltageStr = ShellUtils.readNodeValue("voltage_now")
            val statusStr = ShellUtils.readNodeValue("status")

            val lvl = levelStr.trim().toIntOrNull() ?: readRealBatteryLevel(context)
            _batteryLevel.value = lvl

            val tmp = tempStr.trim().toFloatOrNull() ?: 380f
            _batteryTemp.value = if (tmp > 1000) tmp / 100.0f else tmp / 10.0f

            val crnt = currentStr.trim().toIntOrNull() ?: 0
            _batteryCurrent.value = if (Math.abs(crnt) > 100000) crnt / 1000 else crnt

            val vlt = voltageStr.trim().toIntOrNull() ?: 4000
            _batteryVoltage.value = if (vlt > 100000) vlt / 1000 else vlt

            _batteryStatus.value = if (statusStr.isNotEmpty()) statusStr else readRealBatteryStatus(context)

            _powerUsageWatts.value = Math.abs(_batteryCurrent.value * _batteryVoltage.value) / 1000000.0f
        }
    }

    private fun readRealBatteryLevel(context: Context): Int {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    private fun readRealBatteryStatus(context: Context): String {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatusIntent = context.registerReceiver(null, filter)
        val status = batteryStatusIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        return when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            BatteryManager.BATTERY_STATUS_FULL -> "Full"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
            else -> "Unknown"
        }
    }
}
