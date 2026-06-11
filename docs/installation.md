# Installation Guide

Panduan instalasi untuk project CovidPredict (Backend & Frontend).

---

## 1. Backend (Laravel)

### Persyaratan
* PHP >= 8.3
* Composer
* MySQL / MariaDB
* Node.js & NPM

### Langkah-langkah
1. **Clone Repository** (jika belum):
   ```bash
   git clone <repository-url>
   cd covidpredict-fullstack/backend/laravel-app
   ```

2. **Install Dependencies**:
   ```bash
   composer install
   npm install
   ```

3. **Konfigurasi Environment**:
   * Salin file `.env.example` (atau buat baru) menjadi `.env`:
     ```bash
     cp .env.example .env
     ```
   * Sesuaikan konfigurasi database di `.env`:
     ```env
     DB_CONNECTION=mysql
     DB_HOST=127.0.0.1
     DB_PORT=3306
     DB_DATABASE=covidpredict
     DB_USERNAME=root
     DB_PASSWORD=
     ```

4. **Generate Key & Migrate**:
   ```bash
   php artisan key:generate
   php artisan migrate
   ```

5. **Jalankan Server**:
   ```bash
   php artisan serve
   ```
   Backend akan berjalan di `http://127.0.0.1:8000`.

---

## 2. Frontend (Android)

### Persyaratan
* Android Studio (Koala atau versi terbaru)
* JDK 17
* Android Emulator atau Real Device

### Langkah-langkah
1. **Buka Project**:
   * Buka Android Studio.
   * Pilih `Open` dan arahkan ke folder `covidpredict-fullstack/frontend/android-app`.

2. **Sync Gradle**:
   * Tunggu hingga proses Gradle Sync selesai. Pastikan koneksi internet stabil.

3. **Konfigurasi Base URL**:
   * Jika menggunakan emulator, Base URL default sudah tepat (`http://10.0.2.2:8000/api/v1`).
   * Jika menggunakan real device, ganti `10.0.2.2` dengan IP Laptop Anda di file `ApiClient.kt`.

4. **Run Application**:
   * Klik tombol `Run` (Segitiga hijau) di Android Studio.

---

## Tips & Troubleshooting
* **Database Connection Error**: Pastikan MySQL sudah menyala (misal lewat XAMPP).
* **API Connection Error**: Pastikan backend Laravel sedang berjalan (`php artisan serve`) sebelum menjalankan aplikasi Android.
* **Network Error di Emulator**: Pastikan laptop terkoneksi dengan internet (karena emulator menggunakan network laptop).
