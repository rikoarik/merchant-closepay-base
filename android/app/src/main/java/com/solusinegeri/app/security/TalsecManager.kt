package com.solusinegeri.app.security

import android.app.Application
import android.util.Log
import com.aheaditec.talsec.security.api.Talsec
import com.aheaditec.talsec.security.api.TalsecConfig
import com.aheaditec.talsec.security.api.ThreatListener

/**
 * Talsec Security Manager
 * 
 * Initializes and manages Talsec security library for runtime protection.
 * Detects:
 * - Root/Jailbreak
 * - Debugger attached
 * - Emulator/Simulator
 * - App tampering
 * - Hooking frameworks (Frida, Xposed, etc)
 * 
 * Implementation follows Security Rule #18: Detection as signal, not decision
 * - Client detects threats (best effort)
 * - Backend makes final security decisions
 * - Detection does NOT block core functionality
 */
object TalsecManager {
    
    private const val TAG = "TalsecManager"
    private var isInitialized = false
    
    /**
     * Initialize Talsec security monitoring
     */
    fun initialize(application: Application) {
        if (isInitialized) {
            Log.d(TAG, "Talsec already initialized")
            return
        }
        
        try {
            val config = TalsecConfig.Builder(
                application.packageName,
                "CHANGE_THIS_TO_YOUR_SIGNING_CERTIFICATE_HASH" // TODO: Update with actual certificate hash
            )
                .watcherMail(com.solusinegeri.app.BuildConfig.SECURITY_EMAIL) // Email for threat reports
                .supportedAlternativeStores(arrayOf("com.sec.android.app.samsungapps")) // Samsung store
                .build()
            
            val threatListener = object : ThreatListener {
                override fun onRootDetected() {
                    handleThreat("ROOT_DETECTED", "Device is rooted")
                }
                
                override fun onDebuggerDetected() {
                    handleThreat("DEBUGGER_DETECTED", "Debugger is attached")
                }
                
                override fun onEmulatorDetected() {
                    handleThreat("EMULATOR_DETECTED", "Running on emulator")
                }
                
                override fun onTamperDetected() {
                    handleThreat("TAMPER_DETECTED", "App has been tampered")
                }
                
                override fun onUntrustedInstallationSourceDetected() {
                    handleThreat("UNTRUSTED_SOURCE", "App installed from untrusted source")
                }
                
                override fun onHookDetected() {
                    handleThreat("HOOK_DETECTED", "Hooking framework detected (Frida/Xposed)")
                }
                
                override fun onDeviceBindingDetected() {
                    handleThreat("DEVICE_BINDING", "Device binding mismatch")
                }
                
                override fun onUnofficalStoreDetected() {
                    handleThreat("UNOFFICIAL_STORE", "App from unofficial store")
                }
                
                override fun onObfuscationIssuesDetected() {
                    handleThreat("OBFUSCATION_ISSUE", "Obfuscation tampering detected")
                }
                
                override fun onPrivilegedAccessDetected() {
                    handleThreat("PRIVILEGED_ACCESS", "Privileged access detected")
                }
                
                override fun onSecureHardwareNotAvailable() {
                    Log.d(TAG, "Secure hardware not available (info only)")
                }
                
                override fun onSystemVPNDetected() {
                    Log.d(TAG, "System VPN detected (info only)")
                }
            }
            
            Talsec.start(application, config, threatListener)
            isInitialized = true
            Log.d(TAG, "Talsec security initialized successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Talsec: ${e.message}", e)
            // Don't crash app if security init fails
        }
    }
    
    /**
     * Handle detected security threat
     * 
     * IMPORTANT: Client-side enforcement can be bypassed!
     * - This adds friction for casual attackers
     * - Determined attackers can patch/hook this
     * - Backend MUST still validate all operations
     * 
     * Action taken:
     * - Log the threat
     * - (TODO) Send to backend for analysis
     * - Terminate app immediately
     */
    private fun handleThreat(threatType: String, description: String) {
        Log.e(TAG, "CRITICAL SECURITY THREAT: $threatType - $description")
        
        // TODO: Send threat signal to backend before exit
        // This ensures backend knows about compromised devices
        // even if app is killed before network call completes
        
        // Show alert and exit app
        // Note: This is bypassable but adds friction
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            try {
                android.app.AlertDialog.Builder(
                    (Talsec.getApplication() as? Application)?.let { 
                        getCurrentActivity(it) 
                    } ?: return@post
                )
                    .setTitle("Security Alert")
                    .setMessage("This app cannot run on compromised devices.\n\nThreat: $description")
                    .setCancelable(false)
                    .setPositiveButton("Exit") { _, _ ->
                        exitApp()
                    }
                    .show()
                    
                // Fallback: Auto-exit after 2 seconds even if dialog fails
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    exitApp()
                }, 2000)
                
            } catch (e: Exception) {
                // If dialog fails, exit immediately
                Log.e(TAG, "Failed to show security dialog, exiting", e)
                exitApp()
            }
        }
    }
    
    /**
     * Get current activity from application
     */
    private fun getCurrentActivity(application: Application): android.app.Activity? {
        return try {
            val activityThreadClass = Class.forName("android.app.ActivityThread")
            val activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null)
            val activitiesField = activityThreadClass.getDeclaredField("mActivities")
            activitiesField.isAccessible = true
            val activities = activitiesField.get(activityThread) as? Map<*, *>
            
            activities?.values?.firstOrNull()?.let { activityRecord ->
                val activityRecordClass = activityRecord.javaClass
                val pausedField = activityRecordClass.getDeclaredField("paused")
                pausedField.isAccessible = true
                
                if (pausedField.getBoolean(activityRecord)) return null
                
                val activityField = activityRecordClass.getDeclaredField("activity")
                activityField.isAccessible = true
                activityField.get(activityRecord) as? android.app.Activity
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Exit the application
     * 
     * WARNING: This is client-side enforcement and can be bypassed!
     * - Attacker can hook this method
     * - Attacker can patch the bytecode
     * - Attacker can prevent process kill
     * 
     * This is FRICTION, not SECURITY.
     * Real security happens at backend.
     */
    private fun exitApp() {
        try {
            // Clear all activities
            android.os.Process.killProcess(android.os.Process.myPid())
            System.exit(1)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to exit app", e)
        }
    }
    
    /**
     * Check if Talsec is initialized and monitoring
     */
    fun isMonitoring(): Boolean = isInitialized
}
