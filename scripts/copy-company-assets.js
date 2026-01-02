#!/usr/bin/env node

/**
 * Copy Company Assets Script
 * 
 * Automatically copies company-specific assets (logo, icons, etc.) 
 * from apps/{company}/assets/ to the appropriate Android and iOS directories.
 * 
 * Company is auto-detected from App.tsx import pattern.
 * 
 * Usage:
 *   node scripts/copy-company-assets.js           # Auto-detect from App.tsx
 *   node scripts/copy-company-assets.js [company] # Manual override
 */

const fs = require('fs');
const path = require('path');

// ANSI color codes for console output
const colors = {
  reset: '\x1b[0m',
  bright: '\x1b[1m',
  green: '\x1b[32m',
  yellow: '\x1b[33m',
  blue: '\x1b[34m',
  red: '\x1b[31m',
};

function log(message, color = colors.reset) {
  console.log(`${color}${message}${colors.reset}`);
}

function logSuccess(message) {
  log(`✓ ${message}`, colors.green);
}

function logWarning(message) {
  log(`⚠ ${message}`, colors.yellow);
}

function logError(message) {
  log(`✗ ${message}`, colors.red);
}

function logInfo(message) {
  log(`ℹ ${message}`, colors.blue);
}

// Configuration
const ROOT_DIR = path.resolve(__dirname, '..');
const APPS_DIR = path.join(ROOT_DIR, 'apps');
const ANDROID_RES_DIR = path.join(ROOT_DIR, 'android', 'app', 'src', 'main', 'res');
const IOS_ASSETS_DIR = path.join(ROOT_DIR, 'ios', 'MerchantBaseApp', 'Images.xcassets', 'AppIcon.appiconset');

/**
 * Copy a file from source to destination
 */
function copyFile(src, dest) {
  try {
    // Create destination directory if it doesn't exist
    const destDir = path.dirname(dest);
    if (!fs.existsSync(destDir)) {
      fs.mkdirSync(destDir, { recursive: true });
    }

    // Copy file
    fs.copyFileSync(src, dest);
    return true;
  } catch (error) {
    logError(`Failed to copy ${src} to ${dest}: ${error.message}`);
    return false;
  }
}

/**
 * Copy Android icons from company assets to Android res directories
 */
function copyAndroidIcons(companyId) {
  logInfo(`Copying Android icons for ${companyId}...`);
  
  const androidAssetsDir = path.join(APPS_DIR, companyId, 'assets', 'android');
  
  if (!fs.existsSync(androidAssetsDir)) {
    logWarning(`Android assets directory not found: ${androidAssetsDir}`);
    logWarning('Creating placeholder structure...');
    return false;
  }

  const densities = ['mdpi', 'hdpi', 'xhdpi', 'xxhdpi', 'xxxhdpi'];
  let successCount = 0;
  let totalCount = 0;

  densities.forEach(density => {
    const mipmapDir = `mipmap-${density}`;
    const sourceDir = path.join(androidAssetsDir, mipmapDir);
    const destDir = path.join(ANDROID_RES_DIR, mipmapDir);

    if (!fs.existsSync(sourceDir)) {
      logWarning(`Source directory not found: ${sourceDir}`);
      return;
    }

    // Copy ic_launcher.png and ic_launcher_round.png
    ['ic_launcher.png', 'ic_launcher_round.png'].forEach(filename => {
      const src = path.join(sourceDir, filename);
      const dest = path.join(destDir, filename);
      
      totalCount++;
      if (fs.existsSync(src)) {
        if (copyFile(src, dest)) {
          successCount++;
        }
      } else {
        logWarning(`Icon not found: ${src}`);
      }
    });
  });

  if (successCount > 0) {
    logSuccess(`Copied ${successCount}/${totalCount} Android icons`);
    return true;
  } else {
    logWarning('No Android icons were copied');
    return false;
  }
}

/**
 * Copy iOS icons from company assets to iOS Assets directory
 */
function copyiOSIcons(companyId) {
  logInfo(`Copying iOS icons for ${companyId}...`);
  
  const iosAssetsDir = path.join(APPS_DIR, companyId, 'assets', 'ios', 'AppIcon.appiconset');
  
  if (!fs.existsSync(iosAssetsDir)) {
    logWarning(`iOS assets directory not found: ${iosAssetsDir}`);
    return false;
  }

  if (!fs.existsSync(IOS_ASSETS_DIR)) {
    logWarning(`iOS destination directory not found: ${IOS_ASSETS_DIR}`);
    fs.mkdirSync(IOS_ASSETS_DIR, { recursive: true });
  }

  let successCount = 0;
  let totalCount = 0;

  // Read all files from iOS assets directory
  const files = fs.readdirSync(iosAssetsDir);
  
  files.forEach(filename => {
    // Skip Contents.json, it will be handled separately
    if (filename === 'Contents.json') {
      return;
    }

    const src = path.join(iosAssetsDir, filename);
    const dest = path.join(IOS_ASSETS_DIR, filename);
    
    // Only copy image files
    if (filename.match(/\.(png|jpg|jpeg)$/i)) {
      totalCount++;
      if (copyFile(src, dest)) {
        successCount++;
      }
    }
  });

  // Copy Contents.json
  const contentsJsonSrc = path.join(iosAssetsDir, 'Contents.json');
  const contentsJsonDest = path.join(IOS_ASSETS_DIR, 'Contents.json');
  
  if (fs.existsSync(contentsJsonSrc)) {
    totalCount++;
    if (copyFile(contentsJsonSrc, contentsJsonDest)) {
      successCount++;
      logSuccess('Copied Contents.json');
    }
  }

  if (successCount > 0) {
    logSuccess(`Copied ${successCount}/${totalCount} iOS icons`);
    return true;
  } else {
    logWarning('No iOS icons were copied');
    return false;
  }
}

/**
 * Auto-detect company from App.tsx import
 */
function detectCompany() {
  const appTsxPath = path.join(ROOT_DIR, 'App.tsx');
  
  if (!fs.existsSync(appTsxPath)) {
    logError('App.tsx not found!');
    return null;
  }

  try {
    const appContent = fs.readFileSync(appTsxPath, 'utf8');
    
    // Look for import pattern: from './apps/{company-id}'
    const importMatch = appContent.match(/from\s+['"]\.\/(apps\/([^\/'"]+))/);
    
    if (importMatch && importMatch[2]) {
      return importMatch[2];
    }
    
    logError('Could not detect company from App.tsx import');
    logInfo('Expected pattern: from \'./apps/{company-id}\'');
    return null;
  } catch (error) {
    logError(`Failed to read App.tsx: ${error.message}`);
    return null;
  }
}

/**
 * Main function
 */
function main() {
  log('\n' + '='.repeat(60), colors.bright);
  log('  Company Assets Copy Script', colors.bright);
  log('='.repeat(60) + '\n', colors.bright);

  // Auto-detect company from App.tsx or use argument
  let companyId = process.argv[2];
  
  if (!companyId) {
    logInfo('No company specified, auto-detecting from App.tsx...');
    companyId = detectCompany();
    
    if (!companyId) {
      logError('Failed to detect company. Please specify manually.');
      logInfo('Usage: node scripts/copy-company-assets.js [company-id]');
      process.exit(1);
    }
  }
  
  logInfo(`Company ID: ${companyId}`);
  logInfo(`Apps directory: ${APPS_DIR}`);
  
  const companyDir = path.join(APPS_DIR, companyId);
  
  // Check if company directory exists
  if (!fs.existsSync(companyDir)) {
    logError(`Company directory not found: ${companyDir}`);
    logInfo('Available companies:');
    
    if (fs.existsSync(APPS_DIR)) {
      const companies = fs.readdirSync(APPS_DIR).filter(name => {
        return fs.statSync(path.join(APPS_DIR, name)).isDirectory();
      });
      companies.forEach(company => {
        console.log(`  - ${company}`);
      });
    }
    
    process.exit(1);
  }

  logSuccess(`Found company directory: ${companyDir}\n`);

  // Copy assets
  const androidSuccess = copyAndroidIcons(companyId);
  const iosSuccess = copyiOSIcons(companyId);

  // Summary
  log('\n' + '='.repeat(60), colors.bright);
  if (androidSuccess || iosSuccess) {
    logSuccess('✓ Assets copied successfully!');
  } else {
    logWarning('⚠ No assets were copied. Please check your assets directory structure.');
  }
  log('='.repeat(60) + '\n', colors.bright);
}

// Run the script
main();
