package com.agastyaone.crmai

import android.app.Application
import android.content.pm.ApplicationInfo
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.initialize

/**
 * Installs Play Integrity in release builds, or DebugAppCheckProviderFactory in debug builds,
 * so calls from debug APKs don't fail Play Integrity attestation during development.
 */
class AgastyaOneApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Firebase.initialize(this)
        val isDebug = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        val providerFactory = if (isDebug) {
            DebugAppCheckProviderFactory.getInstance()
        } else {
            PlayIntegrityAppCheckProviderFactory.getInstance()
        }
        Firebase.appCheck.installAppCheckProviderFactory(providerFactory)
    }
}
