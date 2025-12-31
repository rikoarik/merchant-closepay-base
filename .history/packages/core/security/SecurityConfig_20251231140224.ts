import { TalsecConfig } from 'freerasp-react-native';

import Config from '../native/Config';

/**
 * Security Configuration for Talsec (FreeRASP)
 * 
 * IMPORTANT: You must update the 'certificateHashes' and 'appTeamId' 
 * with your actual production keys for this to work correctly in Release mode.
 * 
 * To get your signing certificate hash:
 * 1. Run: keytool -list -v -keystore your_keystore.keystore -alias your_key_alias
 * 2. Copy the SHA-256 fingerprint
 * 3. Remove colons and convert to Base64:
 *    echo -n "SHA256_FINGERPRINT_WITHOUT_COLONS" | xxd -r -p | base64
 */
// Get certificate hashes - required for FreeRASP
// In development, skip androidConfig if not provided
// In production, certificate hash should be provided via environment variable
const isProduction = Config.ENV === 'production';
const certificateHashes = Config.ANDROID_CERTIFICATE_HASH 
  ? [Config.ANDROID_CERTIFICATE_HASH] 
  : [];

// Warn in development if certificate hash is missing
if (!isProduction && certificateHashes.length === 0) {
  console.warn(
    '[SecurityConfig] ANDROID_CERTIFICATE_HASH not set. ' +
    'FreeRASP Android config will be skipped. ' +
    'Set ANDROID_CERTIFICATE_HASH in your environment for production builds.'
  );
}

// Build androidConfig conditionally - certificateHashes is required by FreeRASP type
// In development without certificate hash, we'll provide a placeholder to avoid type errors
// FreeRASP may still work but certificate validation will be disabled
const androidConfigBase = {
  packageName: Config.ENV === 'staging' ? 'com.solusinegeri.app.staging' : 'com.solusinegeri.app',
  // Supported app stores (Google Play and Huawei AppGallery are built-in)
  supportedAlternativeStores: [
    'com.android.vending',             // Google Play Store (for manual addition)
    'com.huawei.appmarket',            // Huawei AppGallery
    'com.sec.android.app.samsungapps', // Samsung Galaxy Store
  ],
  // Optional: Malware detection configuration
  malwareConfig: {
    // Add package names of known malware apps to blacklist
    blacklistedPackageNames: [] as string[],
    // Add SHA-256 hashes of known malware apps to blacklist
    blacklistedHashes: [] as string[],
    // Define suspicious permission combinations that may indicate malware
    suspiciousPermissions: [
      ['android.permission.CAMERA', 'android.permission.RECORD_AUDIO'],
      ['android.permission.READ_SMS', 'android.permission.SEND_SMS'],
    ] as string[][],
    // Whitelist trusted installation sources for malware scanning
    whitelistedInstallationSources: [
      'com.android.vending',
      'com.huawei.appmarket',
    ] as string[],
  },
};

// Build androidConfig with certificateHashes if available
const androidConfig = certificateHashes.length > 0
  ? { ...androidConfigBase, certificateHashes: certificateHashes }
  : { ...androidConfigBase, certificateHashes: [] }; // Empty array as fallback

// Build config - androidConfig is always required by FreeRASP
export const securityConfig: TalsecConfig = {
  androidConfig: androidConfig,
  iosConfig: {
    appBundleId: 'com.solusinegeri.app',
    appTeamId: Config.IOS_APP_TEAM_ID || '',
  },
  watcherMail: Config.TALSEC_WATCHER_MAIL || '',
  isProd: isProduction, // true for production builds, false for development
};
