# Backend - CovidPredict (Laravel API)

Backend ini dibangun menggunakan **Laravel** dan berfungsi sebagai REST API untuk aplikasi CovidPredict Mobile.

## Fungsi Utama

* Menyediakan data COVID-19
* Melakukan prediksi menggunakan metode **Single Exponential Smoothing (SES)**
* Mengirim data ke aplikasi Android dalam format JSON

---

## Tech Stack

* Laravel
* PHP
* MySQL / PostgreSQL

---

## Struktur Penting

* `app/Http/Controllers` → Mengatur request & response API
* `app/Models` → Representasi database
* `app/Services` → Logic bisnis (SES, dll)
* `routes/api.php` → Routing endpoint API
* `database/` → Migration & seeder

---

## Setup Project

1. Install dependency

```
composer install
```

2. Copy file environment

```
cp .env.example .env
```

3. Generate key

```
php artisan key:generate
```

4. Setup database di `.env`

5. Migrasi database

```
php artisan migrate
```

6. Jalankan server

```
php artisan serve
```

---

## Base URL

```
http://localhost:8000/api/v1
```

---

## Endpoint (Awal)

### GET /covid

Mengambil data COVID-19

### POST /predict

Melakukan prediksi menggunakan SES

---

## Catatan

* Semua response menggunakan format JSON
* Logic perhitungan SES harus ditempatkan di `app/Services`
* Jangan menaruh business logic di Controller

---

## Developer Notes

* Gunakan standar REST API
* Gunakan validation pada setiap request
* Gunakan API Resource untuk response

---
