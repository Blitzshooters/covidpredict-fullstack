# User Guide

Panduan penggunaan aplikasi CovidPredict untuk pengguna.

---

## 1. Halaman Utama (Dashboard)
Saat pertama kali membuka aplikasi, Anda akan melihat ringkasan data COVID-19 terbaru.
* **Filter Wilayah**: Anda dapat memilih wilayah tertentu untuk melihat data spesifik.
* **Statistik**: Menampilkan jumlah Positif, Sembuh, dan Meninggal.

---

## 2. Fitur Prediksi
Aplikasi ini memungkinkan Anda untuk melihat prediksi perkembangan COVID-19 dalam beberapa hari ke depan menggunakan metode *Single Exponential Smoothing* (SES).

### Cara Melakukan Prediksi:
1. Klik menu atau tombol **Prediksi**.
2. Masukkan **Periode** (jumlah hari ke depan yang ingin diprediksi, misal: 7 hari).
3. Klik tombol **Hitung**.
4. Hasil prediksi akan muncul di layar, mencakup estimasi angka positif, sembuh, dan meninggal.

---

## 3. Visualisasi Grafik
Gunakan fitur grafik untuk melihat tren perkembangan data secara visual.
* Pilih jenis data (misal: "Positif") untuk melihat pergerakan angka dari waktu ke waktu.
* Grafik akan menyesuaikan dengan wilayah yang dipilih.

---

## 4. FAQ (Frequently Asked Questions)

### Apa itu metode SES?
*Single Exponential Smoothing* adalah metode peramalan yang memberikan bobot lebih besar pada data terbaru dibandingkan data yang lebih lama untuk memprediksi nilai masa depan.

### Mengapa data prediksi tidak muncul?
* Pastikan HP Anda terkoneksi ke internet.
* Pastikan server backend sedang aktif.

### Bagaimana cara update data?
Data akan terupdate secara otomatis saat Anda membuka aplikasi (jika backend melakukan update data dari sumbernya).
