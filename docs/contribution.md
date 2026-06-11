# Contribution Guide

Terima kasih telah tertarik untuk berkontribusi pada project CovidPredict!

---

## Cara Berkontribusi

### 1. Pelaporan Bug
Jika Anda menemukan bug, silakan buat *Issue* baru dengan format:
* Deskripsi bug yang jelas.
* Langkah-langkah untuk mereproduksi bug.
* Ekspektasi vs Realita.
* Screenshot (jika perlu).

### 2. Pengembangan Fitur
1. Fork repository ini.
2. Buat branch baru untuk fitur Anda (`git checkout -b fitur/nama-fitur`).
3. Lakukan commit perubahan Anda (`git commit -m 'Menambah fitur X'`).
4. Push ke branch tersebut (`git push origin fitur/nama-fitur`).
5. Buat *Pull Request*.

---

## Standar Kode

### Backend (Laravel)
* Gunakan **Service Layer** untuk logika bisnis (jangan di Controller).
* Ikuti standar penulisan **PSR-12**.
* Pastikan migration sudah terdokumentasi dengan baik.

### Frontend (Android)
* Terapkan arsitektur **MVVM**.
* Pisahkan *Business Logic* dalam ViewModel.
* Gunakan *String Resources* untuk teks (jangan hardcoded).
* Ikuti *naming convention* Kotlin (camelCase).

---

## Alur Kerja Tim
* Selalu tarik (*pull*) perubahan terbaru dari branch `main` sebelum mulai bekerja.
* Gunakan pesan commit yang deskriptif.
* Komunikasikan perubahan API dengan tim frontend/backend agar sinkron.
