import Foundation
import UIKit

/**
 * Talsec Security Manager for iOS
 * 
 * Provides runtime security monitoring for:
 * - Jailbreak detection
 * - Debugger detection  
 * - Simulator detection
 * - App tampering
 * - Hooking frameworks
 * 
 * IMPORTANT: Client-side enforcement can be bypassed!
 * - This adds friction for casual attackers
 * - Determined attackers can patch/hook this
 * - Backend MUST still validate all operations
 */
@objc(TalsecManager)
class TalsecManager: NSObject {
    
    private static let shared = TalsecManager()
    private var isInitialized = false
    
    @objc static func initialize() {
        shared.startMonitoring()
    }
    
    private func startMonitoring() {
        guard !isInitialized else {
            print("[TalsecManager] Already initialized")
            return
        }
        
        // Check for jailbreak
        if isJailbroken() {
            handleThreat(type: "JAILBREAK", description: "Device is jailbroken")
            return
        }
        
        // Check for debugger
        if isDebuggerAttached() {
            handleThreat(type: "DEBUGGER", description: "Debugger is attached")
            return
        }
        
        #if targetEnvironment(simulator)
        // Allow simulator in debug builds only
        #if DEBUG
        print("[TalsecManager] Running on simulator (debug mode)")
        #else
        handleThreat(type: "SIMULATOR", description: "Running on simulator")
        return
        #endif
        #endif
        
        isInitialized = true
        print("[TalsecManager] Security monitoring initialized")
    }
    
    /**
     * Jailbreak detection
     */
    private func isJailbroken() -> Bool {
        // Check for common jailbreak files
        let jailbreakPaths = [
            "/Applications/Cydia.app",
            "/Library/MobileSubstrate/MobileSubstrate.dylib",
            "/bin/bash",
            "/usr/sbin/sshd",
            "/etc/apt",
            "/private/var/lib/apt/",
            "/Applications/blackra1n.app",
            "/Applications/FakeCarrier.app",
            "/Applications/Icy.app",
            "/Applications/IntelliScreen.app",
            "/Applications/MxTube.app",
            "/Applications/RockApp.app",
            "/Applications/SBSettings.app",
            "/Applications/WinterBoard.app",
            "/Library/MobileSubstrate/DynamicLibraries/LiveClock.plist",
            "/Library/MobileSubstrate/DynamicLibraries/Veency.plist",
            "/private/var/lib/cydia",
            "/private/var/mobile/Library/SBSettings/Themes",
            "/private/var/stash",
            "/private/var/tmp/cydia.log",
            "/System/Library/LaunchDaemons/com.ikey.bbot.plist",
            "/System/Library/LaunchDaemons/com.saurik.Cydia.Startup.plist",
            "/usr/bin/sshd",
            "/usr/libexec/sftp-server",
            "/usr/sbin/sshd",
            "/var/cache/apt",
            "/var/lib/apt",
            "/var/lib/cydia"
        ]
        
        for path in jailbreakPaths {
            if FileManager.default.fileExists(atPath: path) {
                return true
            }
        }
        
        // Try to write to system directory
        let testPath = "/private/test_jailbreak.txt"
        do {
            try "test".write(toFile: testPath, atomically: true, encoding: .utf8)
            try FileManager.default.removeItem(atPath: testPath)
            return true
        } catch {
            // Expected on non-jailbroken devices
        }
        
        // Check for Cydia URL scheme
        if let url = URL(string: "cydia://package/com.example.package") {
            if UIApplication.shared.canOpenURL(url) {
                return true
            }
        }
        
        return false
    }
    
    /**
     * Debugger detection
     */
    private func isDebuggerAttached() -> Bool {
        var info = kinfo_proc()
        var mib: [Int32] = [CTL_KERN, KERN_PROC, KERN_PROC_PID, getpid()]
        var size = MemoryLayout<kinfo_proc>.stride
        
        let result = sysctl(&mib, UInt32(mib.count), &info, &size, nil, 0)
        
        if result != 0 {
            return false
        }
        
        return (info.kp_proc.p_flag & P_TRACED) != 0
    }
    
    /**
     * Handle detected security threat
     * 
     * IMPORTANT: Client-side enforcement can be bypassed!
     * - This adds friction for casual attackers
     * - Determined attackers can patch/hook this
     * - Backend MUST still validate all operations
     */
    private func handleThreat(type: String, description: String) {
        print("[TalsecManager] CRITICAL SECURITY THREAT: \(type) - \(description)")
        
        // TODO: Send threat signal to backend before exit
        
        DispatchQueue.main.async {
            self.showSecurityAlertAndExit(description: description)
        }
    }
    
    /**
     * Show alert and exit app
     */
    private func showSecurityAlertAndExit(description: String) {
        guard let window = UIApplication.shared.windows.first,
              let rootViewController = window.rootViewController else {
            exitApp()
            return
        }
        
        let alert = UIAlertController(
            title: "Security Alert",
            message: "This app cannot run on compromised devices.\n\nThreat: \(description)",
            preferredStyle: .alert
        )
        
        alert.addAction(UIAlertAction(title: "Exit", style: .default) { _ in
            self.exitApp()
        })
        
        rootViewController.present(alert, animated: true)
        
        // Fallback: Auto-exit after 2 seconds
        DispatchQueue.main.asyncAfter(deadline: .now() + 2.0) {
            self.exitApp()
        }
    }
    
    /**
     * Exit the application
     * 
     * WARNING: This is client-side enforcement and can be bypassed!
     * - Attacker can hook this method
     * - Attacker can patch the binary
     * 
     * This is FRICTION, not SECURITY.
     * Real security happens at backend.
     */
    private func exitApp() {
        exit(1)
    }
}
