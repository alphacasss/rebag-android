# Rebag Harian (Android)

Aplikasi Android untuk input hasil rebag harian: tanggal, jumlah koli per hari,
lokasi tumpukan (A01.1.1 sampai D04.1.1), total hasil rebag akumulatif, dan
kuantum. Data disinkronkan ke Google Sheet melalui Apps Script Web App.

## Build

```bat
set JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.21.21-hotspot
gradlew assembleDebug
```

APK hasil build: `app/build/outputs/apk/debug/RebagHarian-v<versi>.apk`
(nama mengikuti aplikasi dan versi, diatur di `app/build.gradle.kts`).

Contoh versi 2.0: `RebagHarian-v2.0.apk`.

## Rilis & OTA

Rilis APK terbaru ada di halaman Releases. Aplikasi mengecek update lewat
Apps Script Web App dengan parameter `?update=1`, yang mengembalikan:

```json
{"ok": true, "versi": 2, "url": "https://github.com/alphacasss/rebag-android/releases/download/v2.0/app-debug.apk", "catatan": "..."}
```

Untuk rilis baru:
1. Naikkan `versionCode` dan `versionName` di `app/build.gradle.kts`.
2. Build APK, lalu unggah sebagai aset Release di GitHub.
3. Di Apps Script, perbarui `APP_VERSION`, `APK_URL`, dan `CATATAN_UPDATE`.
4. Deploy ulang Web App (New version).

## Pengaturan

Tombol **Pengaturan** di aplikasi berisi URL Web App, URL Update (opsional),
dan opsi cek update otomatis saat aplikasi dibuka.

Detail pemakaian ada di `PANDUAN.txt`.
