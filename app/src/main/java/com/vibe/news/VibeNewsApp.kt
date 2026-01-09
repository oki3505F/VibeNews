package com.vibe.news

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class VibeNewsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            // Log to file for debugging
            try {
                val crashFile = java.io.File(externalCacheDir, "crash_log.txt")
                crashFile.writeText("Thread: ${thread.name}\n\n${android.util.Log.getStackTraceString(throwable)}")
            } catch (e: Exception) {
                // Secondary fail? nothing we can do but let the main crash happen
            }
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
