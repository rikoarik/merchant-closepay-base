package com.solusinegeri.app.config

import com.solusinegeri.app.BuildConfig

/**
 * Secure Configuration Storage
 * 
 * Provides centralized access to sensitive configuration values
 * stored in BuildConfig (compile-time constants).
 * 
 * BuildConfig values are:
 * - Set at build time via gradle
 * - Obfuscated by ProGuard/R8 in release builds
 * - Different per build variant (debug/release)
 */
object SecureConfig {
    
    /**
     * Get the production API base URL
     */
    fun getApiProd(): String = BuildConfig.API_BASE_URL_PROD
    
    /**
     * Get the staging API base URL
     */
    fun getApiStg(): String = BuildConfig.API_BASE_URL_STG
    
    /**
     * Get the first certificate pin (leaf certificate)
     */
    fun getPin1(): String = BuildConfig.CERT_PIN_1
    
    /**
     * Get the second certificate pin (intermediate certificate)
     */
    fun getPin2(): String = BuildConfig.CERT_PIN_2
    
    /**
     * Get all certificate pins
     */
    fun getAllPins(): List<String> = listOf(getPin1(), getPin2())
    
    /**
     * Get security/watcher email for threat reports
     */
    fun getSecurityEmail(): String = BuildConfig.SECURITY_EMAIL
    
    /**
     * Get current API base URL based on build variant
     * - Debug builds use staging
     * - Release builds use production
     */
    fun getCurrentApiBaseUrl(): String {
        return if (BuildConfig.DEBUG) {
            getApiStg()
        } else {
            getApiProd()
        }
    }
    
    /**
     * Get production API hostname (without protocol)
     */
    fun getApiHostname(): String = getApiProd().replace("https://", "").replace("http://", "")
    
    /**
     * Get staging API hostname (without protocol)
     */
    fun getApiStgHostname(): String = getApiStg().replace("https://", "").replace("http://", "")
    
    /**
     * Get staging API base URL
     */
    fun getApiStgBaseUrl(): String = getApiStg()
    
    /**
     * Get production API base URL
     */
    fun getApiBaseUrl(): String = getApiProd()
    
    /**
     * Get leaf certificate pin (alias for getPin1)
     */
    fun getPinLeafCert(): String = getPin1()
    
    /**
     * Get intermediate certificate pin (alias for getPin2)
     */
    fun getPinIntermediate(): String = getPin2()
    
    /**
     * Get current environment
     */
    fun getEnv(): String = if (BuildConfig.DEBUG) "staging" else "production"
}
