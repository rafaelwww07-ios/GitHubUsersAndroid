package com.rafaelmukhametov.githubusersandroid.util

import android.app.ActivityManager
import android.content.Context
import android.os.Debug
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object PerformanceMonitor {
    private const val TAG = "PerformanceMonitor"
    
    /**
     * Измерение времени выполнения асинхронной операции
     */
    suspend fun <T> measure(operation: String, block: suspend () -> T): T {
        val startTime = System.currentTimeMillis()
        return try {
            val result = block()
            val timeElapsed = System.currentTimeMillis() - startTime
            Log.d(TAG, "⏱️ $operation: ${timeElapsed}ms")
            result
        } catch (e: Exception) {
            val timeElapsed = System.currentTimeMillis() - startTime
            Log.e(TAG, "❌ $operation failed after ${timeElapsed}ms: ${e.message}")
            throw e
        }
    }
    
    /**
     * Измерение времени выполнения синхронной операции
     */
    fun <T> measureSync(operation: String, block: () -> T): T {
        val startTime = System.currentTimeMillis()
        return try {
            val result = block()
            val timeElapsed = System.currentTimeMillis() - startTime
            Log.d(TAG, "⏱️ $operation: ${timeElapsed}ms")
            result
        } catch (e: Exception) {
            val timeElapsed = System.currentTimeMillis() - startTime
            Log.e(TAG, "❌ $operation failed after ${timeElapsed}ms: ${e.message}")
            throw e
        }
    }
    
    /**
     * Логирование использования памяти
     */
    suspend fun logMemoryUsage(context: Context) = withContext(Dispatchers.IO) {
        val runtime = Runtime.getRuntime()
        val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024
        val maxMemory = runtime.maxMemory() / 1024 / 1024
        val freeMemory = runtime.freeMemory() / 1024 / 1024
        
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager?.getMemoryInfo(memoryInfo)
        val totalMemory = memoryInfo.totalMem / 1024 / 1024
        val availableMemory = memoryInfo.availMem / 1024 / 1024
        
        Log.d(TAG, "💾 Memory usage:")
        Log.d(TAG, "   Used: ${usedMemory}MB / Max: ${maxMemory}MB")
        Log.d(TAG, "   Free: ${freeMemory}MB")
        Log.d(TAG, "   Total system: ${totalMemory}MB")
        Log.d(TAG, "   Available: ${availableMemory}MB")
    }
    
    /**
     * Получение информации об использовании памяти
     */
    fun getMemoryInfo(context: Context): MemoryInfo {
        val runtime = Runtime.getRuntime()
        val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024
        val maxMemory = runtime.maxMemory() / 1024 / 1024
        
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager?.getMemoryInfo(memoryInfo)
        val availableMemory = memoryInfo.availMem / 1024 / 1024
        
        return MemoryInfo(
            usedMemoryMB = usedMemory,
            maxMemoryMB = maxMemory,
            availableMemoryMB = availableMemory
        )
    }
    
    data class MemoryInfo(
        val usedMemoryMB: Long,
        val maxMemoryMB: Long,
        val availableMemoryMB: Long
    )
}

