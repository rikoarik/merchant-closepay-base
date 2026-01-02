import Foundation

/**
 * Secure Configuration for iOS
 * 
 * Provides centralized access to sensitive configuration values.
 * These should be set via build configuration or Info.plist.
 */
@objc(SecureConfig)
class SecureConfig: NSObject {
    
    /**
     * Get production API base URL
     */
    @objc static func getApiProd() -> String {
        return getConfigValue(key: "API_BASE_URL_PROD", fallback: "https://api.solusiuntuknegeri.com")
    }
    
    /**
     * Get staging API base URL
     */
    @objc static func getApiStg() -> String {
        return getConfigValue(key: "API_BASE_URL_STG", fallback: "https://api.stg.solusiuntuknegeri.com")
    }
    
    /**
     * Get first certificate pin
     */
    @objc static func getPin1() -> String {
        return getConfigValue(key: "CERT_PIN_1", fallback: "sha256/cCZ/uMd/qFD4cMVMg8y5w99JpiGeT/sSTiPeB1mu/Ec=")
    }
    
    /**
     * Get second certificate pin
     */
    @objc static func getPin2() -> String {
        return getConfigValue(key: "CERT_PIN_2", fallback: "sha256/9Fk6HgfMnM7/vtnBHcUhg1b3gU2bIpSd50XmKZkMbGA=")
    }
    
    /**
     * Get security/watcher email for threat reports
     */
    @objc static func getSecurityEmail() -> String {
        return getConfigValue(key: "SECURITY_EMAIL", fallback: "security@solusiuntuknegeri.com")
    }
    
    /**
     * Get all certificate pins
     */
    @objc static func getAllPins() -> [String] {
        return [getPin1(), getPin2()]
    }
    
    /**
     * Get current API base URL based on build configuration
     * - Debug builds use staging
     * - Release builds use production
     */
    @objc static func getCurrentApiBaseUrl() -> String {
        #if DEBUG
        return getApiStg()
        #else
        return getApiProd()
        #endif
    }
    
    /**
     * Get configuration value from Info.plist or use fallback
     */
    private static func getConfigValue(key: String, fallback: String) -> String {
        // Try to get from Info.plist first
        if let value = Bundle.main.object(forInfoDictionaryKey: key) as? String {
            return value
        }
        
        // Fallback to embedded value
        return fallback
    }
}
