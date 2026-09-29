package com.agastyaone.crmai

import android.app.Application
import android.content.pm.ApplicationInfo
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import android.util.Log
import com.google.firebase.FirebaseApp

/**
 * AgastyaOne Application entry point.
 * Robust crash protection: logs uncaught exceptions and installs AppCheck safely
 * without crashing if Google Play Services or AppCheck attestation is unavailable.
 */
class AgastyaOneApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Capture any fatal crash to logcat and SharedPreferences for diagnostics
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("AgastyaOneApp", "FATAL UNCAUGHT EXCEPTION on thread ${thread.name}: ${throwable.message}", throwable)
            try {
                val prefs = getSharedPreferences("crash_reports", MODE_PRIVATE)
                val stackTrace = Log.getStackTraceString(throwable)
                prefs.edit().putString("last_crash", stackTrace).commit()
            } catch (_: Throwable) {}
            defaultHandler?.uncaughtException(thread, throwable)
        }

        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Throwable) {
            Log.w("AgastyaOneApp", "FirebaseApp init notice: ${e.message}")
        }

        try {
            val isDebug = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
            val providerFactory = if (isDebug) {
                DebugAppCheckProviderFactory.getInstance()
            } else {
                PlayIntegrityAppCheckProviderFactory.getInstance()
            }
            Firebase.appCheck.installAppCheckProviderFactory(providerFactory)
        } catch (e: Throwable) {
            Log.w("AgastyaOneApp", "AppCheck not installed or unavailable on this device: ${e.message}")
        }
    }
}
