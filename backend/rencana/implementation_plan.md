# Rencana Implementasi Backend (CovidPredict)

Dokumen ini merinci langkah-langkah untuk membangun backend Laravel sesuai dokumen arsitektur dan spesifikasi API, secara bertahap fitur per fitur.

## User Review Required

> [!IMPORTANT]
> Mohon di-review apakah urutan pengerjaan apakah sudah benar?

## Proposed Changes

Kita akan mengembangkan fitur-fitur ini secara berurutan. Di setiap tahapan, kita akan membuat branch sesuai *naming convention* di `Feature.md`.

### Tahap 1: Setup & Initialization (chore/project-setup)
Tahap ini difokuskan untuk menyiapkan fondasi Laravel.
- **Install Laravel**: Install proyek pada folder `backend/laravel-app`.
- **Konfigurasi Environment**: Menyalin `.env.example` ke `.env` dan setup koneksi Database (MySQL/Laragon).
- Mengatur konfigurasi API Routes.

### Tahap 2: Database Schema & Seeder
Tahap untuk menyiapkan struktur tabel SQL dan dummy data.
- **(feature/backend-database-schema)**: Membuat migration `create_covid_data_table` (field: `tanggal`, `wilayah`, `positif`, `sembuh`, `meninggal`).
- **(feature/backend-seeder-covid)**: Membuat `CovidDataSeeder` untuk menyuntikkan data mentah historis Covid-19 agar bisa dites.

### Tahap 3: API Data COVID (GET)
Tahap pembuatan endpoint list & detail.
- **(feature/backend-covid-list)**: Membuat controller `CovidController@index` (GET `/api/v1/covid`) untuk menampilkan semua data, dengan dukungan Query string `?wilayah=...`.
- **(feature/backend-covid-detail)**: Membuat `CovidController@show` (GET `/api/v1/covid/{id}`) untuk detail 1 item.
- Memastikan response dibungkus wrapper standard (tertera di `api-specs.md`).

### Tahap 4: Prediction Service (CORE FEATURE)
Tahap logika utama (Arsitektur Service Layer).
- **(feature/backend-ses-service)**: Membuat `app/Services/SESService.php`. Di dalamnya di-coding algoritma matematika Single Exponential Smoothing (SES) untuk prediksi list positif, sembuh, dan meninggal.
- **(feature/backend-predict-endpoint)**: Membuat controller `PredictionController@predict` (POST `/api/v1/predict`) yang menerima JSON `{ "periode": n }` dan memanggil logika dari `SESService`.

### Tahap 5: Validation & Error Handling
Tahap keamanan dan standardisasi.
- **(feature/backend-validation)**: Membuat Laravel FormRequest untuk endpoint predict (validasi periode harus integer, wajib, > 0).
- **(fix/backend-error-handling)**: Mengatur file custom exception agar semua ValidationException dan HTTP Error otomatis me-return format JSON 400/404 yang seragam sesuai kesepakatan `api-specs.md`.

## Open Questions

> [!WARNING]
> 1. Database apa yang akan digunakan? Apakah MySQL (biasanya lewat Laragon bawaan) atau SQLite untuk development?
> 2. Untuk *Single Exponential Smoothing (SES)*, apakah sudah ada rujukan nilai alpha ($\alpha$) yang spesifik, atau kita akan menghitung parameter $\alpha$ yang optimal secara otomatis, atau menetapkan *default value* (misal $\alpha = 0.2$)?

## Verification Plan

### Automated Tests
- Menjalankan testing POST/GET API via Postman/CURL untuk memvalidasi apakah response persis mengikuti spesifikasi `api-specs.md`.

### Manual Verification
- Cek tabel database.
- Request POST /predict dengan `periode = 7` dan mengecek apakah hasil perhitungannya logis.
