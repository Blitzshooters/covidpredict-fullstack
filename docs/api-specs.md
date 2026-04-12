# API Specification - CovidPredict (Best Practice)

## 🔗 Base URL

http://localhost:8000/api/v1

---

# Standard Response Format

## Success

{
"status": "success",
"message": "Request berhasil",
"data": {},
"timestamp": "2026-04-13T21:00:00Z"
}

## Error

{
"status": "error",
"message": "Pesan error",
"errors": null,
"timestamp": "2026-04-13T21:00:00Z"
}

---

# Data Structure

## Covid Data

{
"id": "integer",
"tanggal": "string (YYYY-MM-DD)",
"wilayah": "string",
"positif": "integer",
"sembuh": "integer",
"meninggal": "integer"
}

---

# 1. GET /covid

## Deskripsi

Mengambil data COVID-19 (list)

## Request

Method: GET
Endpoint: /covid

Query (opsional):

* wilayah (string)

Contoh:
GET /covid?wilayah=Jawa Timur

---

## Response (200 OK)

{
"status": "success",
"message": "Data berhasil diambil",
"data": [
{
"id": 1,
"tanggal": "2026-01-01",
"wilayah": "Jawa Timur",
"positif": 100,
"sembuh": 80,
"meninggal": 5
}
],
"timestamp": "2026-04-13T21:00:00Z"
}

---

# 2. GET /covid/{id}

## Deskripsi

Mengambil detail data COVID-19 berdasarkan ID

## Request

Method: GET
Endpoint: /covid/{id}

---

## Response (200 OK)

{
"status": "success",
"message": "Detail data ditemukan",
"data": {
"id": 1,
"tanggal": "2026-01-01",
"wilayah": "Indonesia",
"positif": 100,
"sembuh": 80,
"meninggal": 5
},
"timestamp": "2026-04-13T21:00:00Z"
}

---

## Response (404 Not Found)

{
"status": "error",
"message": "Data tidak ditemukan",
"errors": null,
"timestamp": "2026-04-13T21:00:00Z"
}

---

# 3. POST /predict

## Deskripsi

Melakukan prediksi menggunakan metode Single Exponential Smoothing (SES)

## Request

Method: POST
Endpoint: /predict

Body (JSON):
{
"periode": 7
}

---

## Validasi

* periode: integer
* wajib diisi
* harus lebih dari 0

---

## Response (200 OK)

{
"status": "success",
"message": "Prediksi berhasil",
"data": {
"periode": 7,
"hasil_prediksi": {
"positif": 120,
"sembuh": 90,
"meninggal": 6
}
},
"timestamp": "2026-04-13T21:00:00Z"
}

---

## Response (400 Bad Request)

{
"status": "error",
"message": "Periode harus lebih dari 0",
"errors": {
"periode": ["Periode wajib diisi dan > 0"]
},
"timestamp": "2026-04-13T21:00:00Z"
}

---

# HTTP Status Code

* 200 → Request berhasil
* 400 → Request tidak valid
* 404 → Data tidak ditemukan
* 500 → Kesalahan server

---

# 🔄 Flow Integrasi

## Ambil Data

1. Android → GET /covid
2. Laravel → ambil dari database
3. Laravel → kirim JSON
4. Android → tampilkan

## Prediksi

1. Android → POST /predict
2. Laravel → proses SES (Service Layer)
3. Laravel → kirim hasil
4. Android → tampilkan

---

# Versi API

Semua endpoint menggunakan prefix:

/api/v1/

Contoh:
http://localhost:8000/api/v1/covid

---

# Developer Agreement

* Semua response wajib mengikuti format standar
* Tidak boleh mengubah struktur JSON tanpa koordinasi
* Backend wajib validasi request
* Frontend wajib mengikuti struktur response

---
