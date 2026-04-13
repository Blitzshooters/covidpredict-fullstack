# Android App - CovidPredict (Kotlin)

Aplikasi Android untuk menampilkan data dan prediksi perkembangan COVID-19 yang terhubung dengan Laravel API.

---

## Fungsi Utama

* Menampilkan data COVID-19 terbaru
* Menampilkan hasil prediksi (SES)
* Menyajikan grafik perkembangan data
* Mengambil data dari REST API (Laravel)

---

## Tech Stack

* Kotlin
* Android Studio
* Retrofit (HTTP Client)
* MVVM Architecture
* LiveData / ViewModel

---

## Arsitektur Aplikasi

Menggunakan pola **MVVM (Model-View-ViewModel)**:

UI (Activity / Fragment)
↓
ViewModel
↓
Repository
↓
API (Retrofit)

---

## Struktur Penting

* `ui/` → Activity / Fragment (tampilan)
* `viewmodel/` → Logic untuk UI
* `repository/` → Penghubung ke API
* `network/` → Konfigurasi Retrofit
* `model/` → Data class (response API)

---

## Base URL API

```id="3c8zce"
http://10.0.2.2:8000/api/v1
```

📌 Catatan:

* `10.0.2.2` digunakan untuk emulator Android (akses localhost)
* Jika pakai HP, gunakan IP laptop

---

## Endpoint yang Digunakan

### GET /covid

Mengambil data COVID-19

### POST /predict

Mengambil hasil prediksi

---

## Setup Project

1. Clone repository
2. Buka di Android Studio
3. Sync Gradle
4. Jalankan aplikasi di emulator / device

---

## Catatan Pengembangan

* Gunakan Retrofit untuk komunikasi API
* Jangan melakukan network call di Activity
* Gunakan ViewModel untuk mengelola data
* Gunakan LiveData untuk update UI

---

## Error Umum

* Tidak bisa connect API → cek Base URL
* Emulator tidak akses localhost → gunakan `10.0.2.2`
* JSON tidak sesuai → cek API response di backend

---

## Developer Notes

* Ikuti arsitektur MVVM
* Pisahkan UI dan logic
* Gunakan naming yang konsisten

---
