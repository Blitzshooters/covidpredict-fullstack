**Catatan Perbaikan Issue Laravel (Could not open input file: artisan)**

Penyebab utama aplikasi Laravel tidak bisa dijalankan (muncul error `Could not open input file: artisan`) adalah hilangnya file-file dasar ( *boilerplate* ) inti Laravel (seperti file `artisan`, `composer.json`, folder `config/`, `public/`, `resources/`, dll.) dari branch `develop` di GitHub.

Berikut adalah investigasi kronologis dan langkah perbaikan yang telah saya lakukan untuk memulihkan proyek Laravel hingga berjalan normal kembali:

---

### Penyebab Masalah (Root Cause)

1. **Ketidaksengajaan Revert** : Pada commit `7b574c7` (oleh rekan tim/ZamZam), terjadi *revert* besar yang secara tidak sengaja menghapus seluruh file dasar bawaan Laravel.
2. **Pemulihan yang Tidak Lengkap** : Pada commit `d8850db` (" *Resolve merge conflicts by restoring accidentally deleted files* "), upaya pengembalian file yang terhapus sudah dilakukan, namun hanya mengembalikan file kustom aplikasi (seperti `CovidController.php`, `CovidService.php`, `routes/api.php`). File-file inti framework Laravel terlewat dan tidak ikut dikembalikan.
3. **Dampak di Lokal** : Karena branch `develop` lokal baru saja di-pull dan disinkronkan dengan branch `develop` GitHub yang bermasalah tersebut, semua file inti Laravel di lokal ikut terhapus otomatis.

---

### Langkah Solusi yang Telah Saya Lakukan

Saya telah melakukan serangkaian perbaikan untuk merestorasi framework Laravel tanpa merusak kode kustom yang sudah ada:

1. **Restorasi File Inti** :
   Saya mengambil kembali ( *restore* ) file-file boilerplate bawaan Laravel yang hilang dari branch awal yang masih bersih (`origin/init/backend-laravel`), meliputi:

* File `artisan`, `composer.json`, `composer.lock`, `.gitignore` backend.
* Folder konfigurasi inti `config/`, folder aset `public/`, folder tampilan `resources/`, dan `tests/`.
* File controller & provider dasar (`Controller.php`, `User.php`, `AppServiceProvider.php`).
* File rute standar (`routes/web.php` dan `routes/console.php`).

1. **Pembersihan Cache & Instalasi Dependensi Bersih** :

* Menghapus folder `vendor` lama yang tidak sinkron secara bersih, lalu menjalankan `composer install --no-scripts` dari awal untuk memulihkan seluruh library PHP.
* Membersihkan cache internal Laravel (`bootstrap/cache/*.php`) yang sempat *corrupt* karena menyimpan cache library lama.

1. **Registrasi Rute API (`routes/api.php`)** :
   Karena file rute API versi kustom kita berada di `routes/api.php`, saya memperbarui file `bootstrap/app.php` dengan mendaftarkan file rute API secara eksplisit agar bisa diakses oleh aplikasi Android:
   **PHP**

```
   ->withRouting(
       web: __DIR__.'/../routes/web.php',
       api: __DIR__.'/../routes/api.php', // Saya tambahkan baris ini
       commands: __DIR__.'/../routes/console.php',
       health: '/up',
   )
```

---

### Hasil Verifikasi Akhir

Semua sistem backend Laravel lokal kini sudah berjalan dengan sempurna:

* **Artisan Berjalan Lancar** : Perintah `php artisan --version` berhasil dieksekusi (Laravel Framework 13.6.0).
* **Koneksi Database & Migrasi Aman** : Terhubung sukses ke MySQL `covidpredict` di Laragon (`php artisan migrate` berjalan tanpa  *error* ).
* **Rute API Berhasil Terdaftar** : Semua *endpoint* API kustom untuk aplikasi Android kini aktif dan terdaftar dengan baik (misal: `/api/v1/covid`, `/api/v1/dashboard`, `/api/v1/predict`, dll.).

---

### Status Saat Ini & Tindakan Selanjutnya

Server Laravel sudah bisa dijalankan dengan normal menggunakan perintah:

**Bash**

```
cd backend/laravel-app
php artisan serve
```

**Penting:** Karena file-file inti Laravel tersebut baru saja saya restorasi secara lokal, statusnya saat ini adalah *staged* (`Changes to be committed`). Saya akan segera melakukan *commit* dan *push* pemulihan ini ke GitHub agar kolaborator lain di branch `develop` tidak mengalami kendala ( *error artisan* ) yang sama saat melakukan  *pull* .
