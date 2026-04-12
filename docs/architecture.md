# System Architecture - CovidPredict

## Overview

CovidPredict menggunakan arsitektur **Client-Server** berbasis REST API, di mana aplikasi Android berkomunikasi dengan backend Laravel untuk mengambil data dan melakukan prediksi.

---

## Arsitektur Utama

Android App (Client)
↓ (HTTP Request - JSON)
Laravel API (Backend)
↓
Database

---

## Komponen Sistem

### 1. Client (Android App)

Dibangun menggunakan Kotlin dengan arsitektur **MVVM**.

Fungsi:

* Menampilkan data COVID-19
* Mengirim request prediksi
* Menampilkan hasil prediksi & grafik

Teknologi:

* Kotlin
* Retrofit (HTTP Client)
* ViewModel & LiveData

---

### 2. Backend (Laravel API)

Berfungsi sebagai pusat logika sistem.

Fungsi:

* Menyediakan REST API
* Mengelola data COVID-19
* Menjalankan algoritma prediksi (SES)

Struktur:

* Controller → menangani request
* Service → logic bisnis (SES)
* Model → akses database

---

### 3. Prediction Engine (SES)

Bagian dari backend yang bertugas menghitung prediksi menggunakan metode:

Single Exponential Smoothing (SES)

Fungsi:

* Mengolah data historis
* Menghasilkan prediksi periode berikutnya

Lokasi:
app/Services/SESService.php

---

### 4. Database

Digunakan untuk menyimpan data COVID-19 dan hasil prediksi.

Contoh tabel:

* covid_data
* prediction

---

## Alur Sistem (Data Flow)

### Ambil Data COVID-19

1. Android → GET /covid
2. Laravel → ambil data dari database
3. Laravel → kirim response JSON
4. Android → tampilkan ke UI

---

### Proses Prediksi

1. Android → POST /predict
2. Laravel → kirim data ke SES Service
3. SES Service → hitung prediksi
4. Laravel → kirim hasil ke Android
5. Android → tampilkan hasil

---

## Komunikasi Data

Format komunikasi menggunakan:

* Protocol: HTTP
* Format: JSON

Contoh:
{
"status": "success",
"data": {}
}

---

## Layered Architecture (Backend)

Controller
↓
Service (Business Logic)
↓
Model
↓
Database

Catatan:

* Tidak boleh menaruh logic di Controller
* Semua perhitungan SES harus di Service

---

## Arsitektur Android (MVVM)

UI (Activity / Fragment)
↓
ViewModel
↓
Repository
↓
API (Retrofit)

---

## Keamanan (Basic)

* Validasi request di backend
* Gunakan .env untuk konfigurasi
* Tidak menyimpan data sensitif di client

---

## Deployment (Sederhana)

Backend:

* Jalankan dengan Laravel (`php artisan serve`)

Android:

* Jalankan di emulator / device

---

## Developer Agreement

* Semua komunikasi data melalui API
* Tidak boleh akses database langsung dari Android
* Struktur arsitektur harus dipatuhi
* Perubahan arsitektur harus disepakati tim

---
