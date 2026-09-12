# Jamaah-Absensi

Aplikasi pencatatan absensi jamaah shalat 5 waktu. Dibuat khusus untuk pengujian offline tanpa database online.

## Fitur
- Manajemen Data Jamaah (Tambah, Hapus, Lihat).
- Pencatatan Absensi 5 Waktu Shalat (Mengikuti, Ijin, Tidak Mengikuti, Telat).
- Pencegahan Absensi Ganda.
- Dashboard Statistik Harian.
- Pengaturan (Reset Data).

## Teknologi
- Kotlin
- Jetpack Compose
- Room Database (Local Storage)
- GitHub Actions (CI/CD)

## Cara Build APK via GitHub (Lewat HP)
1. Buka repositori ini di browser HP Anda.
2. Klik tab **Actions**.
3. Pilih workflow **Build Android APK** di sebelah kiri.
4. Klik tombol **Run workflow** -> Pilih branch `main` -> Klik **Run workflow**.
5. Tunggu sekitar 2-3 menit hingga proses build selesai (muncul centang hijau).
6. Klik pada hasil build tersebut, gulir ke bawah ke bagian **Artifacts**.
7. Klik file `app-debug.apk` untuk mendownloadnya.
8. Buka file `.zip` yang terdownload, ekstrak, lalu install APK di HP Android Anda.
9. 
