# Company Assets - Member Base

This directory contains company-specific assets that will be automatically copied to the appropriate platform directories during the build process.

## Directory Structure

```
assets/
├── android/
│   ├── mipmap-mdpi/
│   │   ├── ic_launcher.png (48x48)
│   │   └── ic_launcher_round.png (48x48)
│   ├── mipmap-hdpi/
│   │   ├── ic_launcher.png (72x72)
│   │   └── ic_launcher_round.png (72x72)
│   ├── mipmap-xhdpi/
│   │   ├── ic_launcher.png (96x96)
│   │   └── ic_launcher_round.png (96x96)
│   ├── mipmap-xxhdpi/
│   │   ├── ic_launcher.png (144x144)
│   │   └── ic_launcher_round.png (144x144)
│   └── mipmap-xxxhdpi/
│       ├── ic_launcher.png (192x192)
│       └── ic_launcher_round.png (192x192)
└── ios/
    └── AppIcon.appiconset/
        ├── Contents.json
        ├── icon-20@2x.png (40x40)
        ├── icon-20@3x.png (60x60)
        ├── icon-29@2x.png (58x58)
        ├── icon-29@3x.png (87x87)
        ├── icon-40@2x.png (80x80)
        ├── icon-40@3x.png (120x120)
        ├── icon-60@2x.png (120x120)
        ├── icon-60@3x.png (180x180)
        └── icon-1024.png (1024x1024)
```

## Android Icon Sizes

| Density | Size | Files Required |
|---------|------|----------------|
| mdpi | 48x48 | `ic_launcher.png`, `ic_launcher_round.png` |
| hdpi | 72x72 | `ic_launcher.png`, `ic_launcher_round.png` |
| xhdpi | 96x96 | `ic_launcher.png`, `ic_launcher_round.png` |
| xxhdpi | 144x144 | `ic_launcher.png`, `ic_launcher_round.png` |
| xxxhdpi | 192x192 | `ic_launcher.png`, `ic_launcher_round.png` |

## iOS Icon Sizes

| Purpose | Size | Filename |
|---------|------|----------|
| iPhone Notification | 40x40 | `icon-20@2x.png` |
| iPhone Notification | 60x60 | `icon-20@3x.png` |
| iPhone Settings | 58x58 | `icon-29@2x.png` |
| iPhone Settings | 87x87 | `icon-29@3x.png` |
| iPhone Spotlight | 80x80 | `icon-40@2x.png` |
| iPhone Spotlight | 120x120 | `icon-40@3x.png` |
| iPhone App | 120x120 | `icon-60@2x.png` |
| iPhone App | 180x180 | `icon-60@3x.png` |
| App Store | 1024x1024 | `icon-1024.png` |

## How to Add Your Logo

### 1. Prepare Your Logo
- Create a square logo (1:1 aspect ratio)
- Use PNG format with transparent background
- Ensure the logo looks good at small sizes
- Start with a high-resolution version (1024x1024 recommended)

### 2. Generate Icon Sizes

You can use online tools to generate all required sizes:
- **Android**: [Android Asset Studio](https://romannurik.github.io/AndroidAssetStudio/icons-launcher.html)
- **iOS**: [App Icon Generator](https://appicon.co/)

Or use command-line tools like ImageMagick:

```bash
# Android (example for generating all densities)
convert logo.png -resize 48x48 mipmap-mdpi/ic_launcher.png
convert logo.png -resize 72x72 mipmap-hdpi/ic_launcher.png
convert logo.png -resize 96x96 mipmap-xhdpi/ic_launcher.png
convert logo.png -resize 144x144 mipmap-xxhdpi/ic_launcher.png
convert logo.png -resize 192x192 mipmap-xxxhdpi/ic_launcher.png
```

### 3. Place Icons in This Directory

Copy all generated icons to the appropriate subdirectories following the structure above.

### 4. Build Your App

The build script will automatically copy these assets to the platform directories:

```bash
# Copy assets before building
npm run copy:assets

# Or use the combined build commands
npm run build:android  # Copies assets then builds Android
npm run build:ios      # Copies assets then builds iOS
```

## Notes

- **Android Round Icons**: If you don't have a round version, you can duplicate the square icon for `ic_launcher_round.png`
- **iOS Contents.json**: Don't modify this file unless you know what you're doing. It's required by Xcode.
- **Git**: These assets should be committed to your repository for each company.

## Troubleshooting

### Icons not appearing after build

1. Make sure you've run `npm run copy:assets` before building
2. Clean and rebuild your project:
   ```bash
   # Android
   cd android && ./gradlew clean && cd ..
   
   # iOS
   cd ios && pod install && cd ..
   ```

### Wrong icons showing

1. Clear the app data on your device
2. Uninstall the app completely
3. Rebuild and reinstall

### Different logo for staging vs production

You can create multiple asset directories:
```
assets/
├── staging/
│   ├── android/
│   └── ios/
└── production/
    ├── android/
    └── ios/
```

Then modify the build script to use different directories based on the build type.
