# Company-Specific Logo System 🎨

## Overview

Sistem otomatis untuk mengganti logo app (Android & iOS) berdasarkan company saat build.

## ✅ Yang Sudah Dibuat

1. **Build Script**: `scripts/copy-company-assets.js`
   - **Otomatis detect company dari `App.tsx` import** 🔥
   - Auto-copy logo dari `apps/{company}/assets/` ke native directories
   - Support Android (semua densities) dan iOS (semua sizes)
   - Colorful console output untuk tracking

**Auto-Detection:**
```javascript
// Script akan baca App.tsx dan cari pattern:
import MerchantbaseApp from './apps/merchant-base';

// Otomatis pakai "merchant-base" sebagai company ID
// Jadi ganti company di App.tsx = otomatis ganti logo!
```

2. **Asset Structure**: 
   ```
   apps/merchant-base/assets/
   ├── android/
   │   ├── mipmap-mdpi/
   │   ├── mipmap-hdpi/
   │   ├── mipmap-xhdpi/
   │   ├── mipmap-xxhdpi/
   │   └── mipmap-xxxhdpi/
   └── ios/
       └── AppIcon.appiconset/
           └── Contents.json (template)
   ```

3. **NPM Commands** (updated):
   - `npm run copy:assets` - Copy assets saja
   - `npm run build:apk` - Copy assets + build APK release
   - `npm run build:apk:staging` - Copy assets + build staging
   - `npm run build:apk:prod` - Copy assets + build production
   - `npm run build:aab` - Copy assets + build AAB

## 📋 Cara Pakai

### 1. Siapkan Logo

Buat logo dalam format PNG, square (1:1 ratio), high resolution (misal 1024x1024).

### 2. Generate Semua Ukuran

**Opsi A: Online Generator** (paling mudah)
- **Android**: [Android Asset Studio](https://romannurik.github.io/AndroidAssetStudio/icons-launcher.html)
- **iOS**: [App Icon Generator](https://appicon.co/)

Upload logo utama, download semua ukuran yang dihasilkan.

**Opsi B: ImageMagick** (command line)
```bash
# Android
convert logo.png -resize 48x48 mipmap-mdpi/ic_launcher.png
convert logo.png -resize 72x72 mipmap-hdpi/ic_launcher.png
convert logo.png -resize 96x96 mipmap-xhdpi/ic_launcher.png
convert logo.png -resize 144x144 mipmap-xxhdpi/ic_launcher.png
convert logo.png -resize 192x192 mipmap-xxxhdpi/ic_launcher.png

# Untuk round icons, duplicate saja
```

### 3. Taruh di Folder Company

```
apps/merchant-base/assets/
├── android/
│   ├── mipmap-mdpi/
│   │   ├── ic_launcher.png (48x48)
│   │   └── ic_launcher_round.png
│   ├── mipmap-hdpi/
│   │   ├── ic_launcher.png (72x72)
│   │   └── ic_launcher_round.png
│   └── ... (semua densities)
└── ios/
    └── AppIcon.appiconset/
        ├── Contents.json (sudah ada)
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

### 4. Build

```bash
# Otomatis detect company dari App.tsx + build
npm run build:apk

# Atau copy assets dulu (optional)
npm run copy:assets
npm run build:apk:staging
```

**Pro Tip:** Company otomatis di-detect dari import di `App.tsx`!

## 🏢 Untuk Company Baru

Misal mau bikin company baru `closepay-merchant`:

1. **Update `App.tsx` import:**
   ```typescript
   import ClosepayMerchantApp from './apps/closepay-merchant';
   export default ClosepayMerchantApp;
   ```

2. **Buat folder structure:**
   ```bash
   mkdir -p apps/closepay-merchant/assets/android/mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}
   mkdir -p apps/closepay-merchant/assets/ios/AppIcon.appiconset
   ```

3. **Copy template Contents.json:**
   ```bash
   cp apps/merchant-base/assets/ios/AppIcon.appiconset/Contents.json \
      apps/closepay-merchant/assets/ios/AppIcon.appiconset/
   ```

4. **Generate & taruh semua icon sizes**

5. **Build:**
   ```bash
   npm run build:apk
   # Otomatis pakai "closepay-merchant" dari App.tsx!
   ```

## 📝 Icon Size Reference

### Android
| Density | Size | Files |
|---------|------|-------|
| mdpi | 48x48 | `ic_launcher.png`, `ic_launcher_round.png` |
| hdpi | 72x72 | `ic_launcher.png`, `ic_launcher_round.png` |
| xhdpi | 96x96 | `ic_launcher.png`, `ic_launcher_round.png` |
| xxhdpi | 144x144 | `ic_launcher.png`, `ic_launcher_round.png` |
| xxxhdpi | 192x192 | `ic_launcher.png`, `ic_launcher_round.png` |

### iOS
| Purpose | Size | Filename |
|---------|------|----------|
| Notification 2x | 40x40 | `icon-20@2x.png` |
| Notification 3x | 60x60 | `icon-20@3x.png` |
| Settings 2x | 58x58 | `icon-29@2x.png` |
| Settings 3x | 87x87 | `icon-29@3x.png` |
| Spotlight 2x | 80x80 | `icon-40@2x.png` |
| Spotlight 3x | 120x120 | `icon-40@3x.png` |
| App 2x | 120x120 | `icon-60@2x.png` |
| App 3x | 180x180 | `icon-60@3x.png` |
| App Store | 1024x1024 | `icon-1024.png` |

## 🔧 Troubleshooting

**Logo tidak berubah setelah build?**
1. Uninstall app dari device/emulator
2. Clean build:
   ```bash
   # Android
   cd android && gradlew clean && cd ..
   
   # iOS
   cd ios && pod install && cd ..
   ```
3. Rebuild

**Script tidak jalan?**
- Pastikan Node.js terinstall
- Check apakah folder structure sudah benar
- Jalankan `node scripts/copy-company-assets.js` manual untuk debug

## 🎯 Next Steps

1. ✅ Siapkan logo company Anda
2. ✅ Generate semua ukuran yang diperlukan
3. ✅ Taruh di `apps/{company}/assets/`
4. ✅ Run `npm run build:apk`
5. ✅ Done! Logo otomatis terpakai

---

**Note**: Jangan lupa commit assets ke git untuk setiap company!
