package com.vibe.news

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class VibeNewsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            // Try to log it to a file so we can maybe find it later, 
            // but for now, just let the default handler take over after we've had a look.
            // In a real debug scenario, we might show a custom Activity here.
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
