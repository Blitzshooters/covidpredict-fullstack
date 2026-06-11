# Database Schema

Dokumentasi struktur database untuk project CovidPredict.

---

## Entity Relationship Diagram (ERD) - Logika
Situs ini menggunakan 2 tabel utama untuk menyimpan data COVID dan hasil prediksi.

---

## 1. Tabel `covid_data`
Menyimpan data historis COVID-19 yang akan digunakan sebagai basis prediksi.

| Kolom | Tipe | Deskripsi |
| :--- | :--- | :--- |
| `id` | BigInt (PK) | Auto increment ID. |
| `tanggal` | Date | Tanggal pencatatan data. |
| `wilayah` | String | Nama wilayah (misal: Jawa Timur, Indonesia). |
| `positif` | Integer | Jumlah kasus positif. |
| `sembuh` | Integer | Jumlah kasus sembuh. |
| `meninggal` | Integer | Jumlah kasus meninggal. |
| `created_at` | Timestamp | Waktu data dibuat. |
| `updated_at` | Timestamp | Waktu data terakhir diupdate. |

**Index:** `['tanggal', 'wilayah']` (untuk optimasi query filter).

---

## 2. Tabel `predictions`
Menyimpan riwayat hasil prediksi yang telah dihitung oleh sistem.

| Kolom | Tipe | Deskripsi |
| :--- | :--- | :--- |
| `id_prediksi` | BigInt (PK) | Auto increment ID. |
| `tanggal_prediksi` | Date | Tanggal target yang diprediksi. |
| `periode` | Integer | Jumlah hari (periode) yang diprediksi ke depan. |
| `hasil_prediksi_positif` | Integer | Hasil prediksi kasus positif. |
| `hasil_prediksi_sembuh` | Integer | Hasil prediksi kasus sembuh. |
| `hasil_prediksi_meninggal` | Integer | Hasil prediksi kasus meninggal. |
| `created_at` | Timestamp | Waktu prediksi dihitung. |
| `updated_at` | Timestamp | Waktu data terakhir diupdate. |

---

## 3. Tabel `users`
Tabel standar Laravel untuk autentikasi (jika dikembangkan lebih lanjut).

| Kolom | Tipe | Deskripsi |
| :--- | :--- | :--- |
| `id` | BigInt (PK) | ID User. |
| `name` | String | Nama user. |
| `email` | String | Email user (unique). |
| `password` | String | Password terenkripsi. |

---

## Catatan Teknis
* Database menggunakan **MySQL**.
* Tabel dibuat menggunakan fitur **Laravel Migration**.
* Pengaturan database terdapat di file `.env` pada folder backend.
