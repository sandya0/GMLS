package com.example.gmls.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Comprehensive crash prevention and error handling utility
 */
object CrashPrevention {
    
    private const val TAG = "CrashPrevention"
    private const val CRASH_PREFS = "crash_prevention_prefs"
    private const val CRASH_COUNT_KEY = "crash_count"
    private const val LAST_CRASH_TIME_KEY = "last_crash_time"
    private const val CRASH_LOGS_KEY = "crash_logs"
    private const val MAX_CRASH_LOGS = 10
    private const val CRASH_RESET_INTERVAL = 24 * 60 * 60 * 1000L // 24 hours
    
    private lateinit var context: Context
    private lateinit var preferences: SharedPreferences
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    
    /**
     * Initialize crash prevention system
     */
    fun initialize(appContext: Context) {
        context = appContext.applicationContext
        preferences = context.getSharedPreferences(CRASH_PREFS, Context.MODE_PRIVATE)
        
        // Reset crash count if enough time has passed
        resetCrashCountIfNeeded()
        
        Log.d(TAG, "Crash prevention system initialized")
    }
    
    /**
     * Create a safe coroutine exception handler
     */
    fun createSafeExceptionHandler(
        tag: String,
        onError: ((Throwable) -> Unit)? = null
    ): CoroutineExceptionHandler {
        return CoroutineExceptionHandler { _, exception ->
            Log.e(tag, "Coroutine exception caught", exception)
            recordCrash(exception, tag)
            onError?.invoke(exception)
        }
    }
    
    /**
     * Execute a block of code safely with error handling
     */
    inline fun <T> safeExecute(
        tag: String,
        defaultValue: T,
        crossinline block: () -> T
    ): T {
        return try {
            block()
        } catch (e: Exception) {
            Log.e(tag, "Safe execution failed", e)
            recordCrash(e, tag)
            defaultValue
        }
    }
    
    /**
     * Execute a suspend function safely with timeout
     */
    suspend inline fun <T> safeExecuteAsync(
        tag: String,
        timeoutMs: Long = 30000L,
        defaultValue: T,
        crossinline block: suspend () -> T
    ): T {
        return try {
            withTimeoutOrNull(timeoutMs) {
                block()
            } ?: defaultValue
        } catch (e: Exception) {
            Log.e(tag, "Safe async execution failed", e)
            recordCrash(e, tag)
            defaultValue
        }
    }
    
    /**
     * Execute a block on IO dispatcher safely
     */
    suspend inline fun <T> safeExecuteIO(
        tag: String,
        defaultValue: T,
        crossinline block: suspend () -> T
    ): T {
        return try {
            withContext(Dispatchers.IO) {
                block()
            }
        } catch (e: Exception) {
            Log.e(tag, "Safe IO execution failed", e)
            recordCrash(e, tag)
            defaultValue
        }
    }
    
    /**
     * Record a crash with details
     */
    fun recordCrash(exception: Throwable, source: String) {
        try {
            val crashCount = preferences.getInt(CRASH_COUNT_KEY, 0) + 1
            val currentTime = System.currentTimeMillis()
            
            // Create crash log entry
            val crashLog = CrashLog(
                timestamp = currentTime,
                source = source,
                exception = exception.javaClass.simpleName,
                message = exception.message ?: context.getString(com.example.gmls.R.string.no_message),
                stackTrace = getStackTraceString(exception)
            )
            
            // Save crash information
            preferences.edit()
                .putInt(CRASH_COUNT_KEY, crashCount)
                .putLong(LAST_CRASH_TIME_KEY, currentTime)
                .apply()
            
            // Save crash log
            saveCrashLog(crashLog)
            
            Log.w(TAG, "Crash recorded: $source - ${exception.javaClass.simpleName}")
            
            // If too many crashes, take action
            if (crashCount >= 5) {
                handleFrequentCrashes()
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error recording crash", e)
        }
    }
    
    /**
     * Get crash statistics
     */
    fun getCrashStats(): CrashStats {
        return CrashStats(
            crashCount = preferences.getInt(CRASH_COUNT_KEY, 0),
            lastCrashTime = preferences.getLong(LAST_CRASH_TIME_KEY, 0),
            crashLogs = getCrashLogs()
        )
    }
    
    /**
     * Clear all crash data
     */
    fun clearCrashData() {
        preferences.edit()
            .remove(CRASH_COUNT_KEY)
            .remove(LAST_CRASH_TIME_KEY)
            .remove(CRASH_LOGS_KEY)
            .apply()
        Log.d(TAG, "Crash data cleared")
    }
    
    /**
     * Check if app is in crash-prone state
     */
    fun isAppUnstable(): Boolean {
        val crashCount = preferences.getInt(CRASH_COUNT_KEY, 0)
        val lastCrashTime = preferences.getLong(LAST_CRASH_TIME_KEY, 0)
        val timeSinceLastCrash = System.currentTimeMillis() - lastCrashTime
        
        return crashCount >= 3 && timeSinceLastCrash < 60000L // 1 minute
    }
    
    /**
     * Create a safe coroutine scope
     */
    fun createSafeScope(tag: String): CoroutineScope {
        return CoroutineScope(
            SupervisorJob() + 
            Dispatchers.Main + 
            createSafeExceptionHandler(tag)
        )
    }
    
    private fun resetCrashCountIfNeeded() {
        val lastCrashTime = preferences.getLong(LAST_CRASH_TIME_KEY, 0)
        val currentTime = System.currentTimeMillis()
        
        if (currentTime - lastCrashTime > CRASH_RESET_INTERVAL) {
            preferences.edit()
                .putInt(CRASH_COUNT_KEY, 0)
                .apply()
            Log.d(TAG, "Crash count reset after 24 hours")
        }
    }
    
    private fun handleFrequentCrashes() {
        Log.w(TAG, "Frequent crashes detected - taking preventive action")
        
        // You can implement various recovery strategies here:
        // 1. Clear app cache
        // 2. Reset user preferences to defaults
        // 3. Force logout user
        // 4. Disable certain features
        // 5. Show safe mode dialog
        
        try {
            // Example: Clear some caches
            val cacheDir = context.cacheDir
            cacheDir.listFiles()?.forEach { file ->
                if (file.isFile && file.name.endsWith(".tmp")) {
                    file.delete()
                }
            }
            
            Log.d(TAG, "Cleared temporary cache files")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing cache", e)
        }
    }
    
    private fun saveCrashLog(crashLog: CrashLog) {
        try {
            val existingLogs = getCrashLogs().toMutableList()
            existingLogs.add(0, crashLog) // Add to beginning
            
            // Keep only the latest logs
            if (existingLogs.size > MAX_CRASH_LOGS) {
                existingLogs.subList(MAX_CRASH_LOGS, existingLogs.size).clear()
            }
            
            // Convert to JSON-like string for storage
            val logsString = existingLogs.joinToString("|") { log ->
                "${log.timestamp},${log.source},${log.exception},${log.message.replace(",", ";")}," +
                "${log.stackTrace.replace(",", ";").replace("\n", "\\n")}"
            }
            
            preferences.edit()
                .putString(CRASH_LOGS_KEY, logsString)
                .apply()
                
        } catch (e: Exception) {
            Log.e(TAG, "Error saving crash log", e)
        }
    }
    
    private fun getCrashLogs(): List<CrashLog> {
        return try {
            val logsString = preferences.getString(CRASH_LOGS_KEY, "") ?: ""
            if (logsString.isEmpty()) return emptyList()
            
            logsString.split("|").mapNotNull { logEntry ->
                try {
                    val parts = logEntry.split(",", limit = 5)
                    if (parts.size >= 5) {
                        CrashLog(
                            timestamp = parts[0].toLong(),
                            source = parts[1],
                            exception = parts[2],
                            message = parts[3].replace(";", ","),
                            stackTrace = parts[4].replace(";", ",").replace("\\n", "\n")
                        )
                    } else null
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting crash logs", e)
            emptyList()
        }
    }
    
    private fun getStackTraceString(throwable: Throwable): String {
        return try {
            val stringWriter = StringWriter()
            val printWriter = PrintWriter(stringWriter)
            throwable.printStackTrace(printWriter)
            stringWriter.toString()
        } catch (e: Exception) {
            "Stack trace tidak tersedia" // Indonesian for "Stack trace unavailable"
        }
    }
}

/**
 * Data class for crash log entries
 */
data class CrashLog(
    val timestamp: Long,
    val source: String,
    val exception: String,
    val message: String,
    val stackTrace: String
) {
    fun getFormattedTime(): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .format(Date(timestamp))
    }
}

/**
 * Data class for crash statistics
 */
data class CrashStats(
    val crashCount: Int,
    val lastCrashTime: Long,
    val crashLogs: List<CrashLog>
) {
    fun getLastCrashFormatted(): String {
        return if (lastCrashTime > 0) {
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(Date(lastCrashTime))
        } else {
            "Tidak pernah" // Indonesian for "Never"
        }
    }
}

/**
 * Extension functions for safe operations
 */

/**
 * Safe string operations
 */
fun String?.safeSubstring(startIndex: Int, endIndex: Int = this?.length ?: 0): String {
    return CrashPrevention.safeExecute("StringExtension", "") {
        if (this == null) "" else {
            val safeStart = startIndex.coerceAtLeast(0).coerceAtMost(length)
            val safeEnd = endIndex.coerceAtLeast(safeStart).coerceAtMost(length)
            substring(safeStart, safeEnd)
        }
    }
}

/**
 * Safe list operations
 */
fun <T> List<T>?.safeGet(index: Int): T? {
    return CrashPrevention.safeExecute("ListExtension", null) {
        if (this == null || index < 0 || index >= size) null else this[index]
    }
}

/**
 * Safe map operations
 */
fun <K, V> Map<K, V>?.safeGet(key: K): V? {
    return CrashPrevention.safeExecute("MapExtension", null) {
        this?.get(key)
    }
} 
