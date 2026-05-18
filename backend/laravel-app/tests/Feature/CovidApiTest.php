<?php

namespace Tests\Feature;

use App\Models\CovidData;
use App\Models\Prediction;
use Carbon\Carbon;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class CovidApiTest extends TestCase
{
    use RefreshDatabase;

    protected function setUp(): void
    {
        parent::setUp();
        
        // Atur waktu agar stabil dalam pengetesan
        Carbon::setTestNow('2026-05-18 10:00:00');
    }

    /**
     * Helper untuk membuat data COVID-19 tiruan.
     */
    private function createCovidData(string $wilayah, string $tanggal, int $positif, int $sembuh, int $meninggal): CovidData
    {
        return CovidData::create([
            'tanggal' => $tanggal,
            'wilayah' => $wilayah,
            'positif' => $positif,
            'sembuh' => $sembuh,
            'meninggal' => $meninggal,
        ]);
    }

    /**
     * Test GET /api/v1/covid (mengambil seluruh data).
     */
    public function test_get_covid_list_success(): void
    {
        // Membuat data uji
        $this->createCovidData('Jawa Timur', '2026-05-17', 100, 80, 5);
        $this->createCovidData('Jawa Barat', '2026-05-17', 120, 90, 6);

        $response = $this->getJson('/api/v1/covid');

        $response->assertStatus(200)
                 ->assertJsonStructure([
                     'status',
                     'message',
                     'data' => [
                         '*' => [
                             'id',
                             'tanggal',
                             'wilayah',
                             'positif',
                             'sembuh',
                             'meninggal',
                             'created_at',
                             'updated_at'
                         ]
                     ],
                     'timestamp'
                 ])
                 ->assertJson([
                     'status' => 'success',
                     'message' => 'Data berhasil diambil',
                 ]);

        $this->assertCount(2, $response->json('data'));
    }

    /**
     * Test GET /api/v1/covid dengan filter wilayah.
     */
    public function test_get_covid_list_with_wilayah_filter(): void
    {
        $this->createCovidData('Jawa Timur', '2026-05-17', 100, 80, 5);
        $this->createCovidData('Jawa Barat', '2026-05-17', 120, 90, 6);

        $response = $this->getJson('/api/v1/covid?wilayah=Jawa Timur');

        $response->assertStatus(200);
        $data = $response->json('data');
        $this->assertCount(1, $data);
        $this->assertEquals('Jawa Timur', $data[0]['wilayah']);
    }

    /**
     * Test GET /api/v1/covid/{id} sukses.
     */
    public function test_get_covid_detail_success(): void
    {
        $data = $this->createCovidData('Indonesia', '2026-05-17', 500, 400, 20);

        $response = $this->getJson("/api/v1/covid/{$data->id}");

        $response->assertStatus(200)
                 ->assertJson([
                     'status' => 'success',
                     'message' => 'Detail data ditemukan',
                     'data' => [
                         'id' => $data->id,
                         'wilayah' => 'Indonesia',
                         'positif' => 500,
                         'sembuh' => 400,
                         'meninggal' => 20,
                     ]
                 ]);
    }

    /**
     * Test GET /api/v1/covid/{id} gagal (404 Not Found).
     */
    public function test_get_covid_detail_not_found(): void
    {
        $response = $this->getJson('/api/v1/covid/9999');

        $response->assertStatus(404)
                 ->assertJson([
                     'status' => 'error',
                     'message' => 'Data tidak ditemukan',
                     'errors' => null
                 ]);
    }

    /**
     * Test GET /api/v1/covid/latest sukses.
     */
    public function test_get_covid_latest_success(): void
    {
        $this->createCovidData('Indonesia', '2026-05-16', 480, 390, 19);
        $latest = $this->createCovidData('Indonesia', '2026-05-17', 500, 400, 20);

        $response = $this->getJson('/api/v1/covid/latest');

        $response->assertStatus(200)
                 ->assertJson([
                     'status' => 'success',
                     'message' => 'Data terbaru ditemukan',
                     'data' => [
                         'id' => $latest->id,
                         'tanggal' => '2026-05-17T00:00:00.000000Z',
                         'positif' => 500
                     ]
                 ]);
    }

    /**
     * Test GET /api/v1/covid/latest gagal ketika data kosong (404 Not Found).
     */
    public function test_get_covid_latest_empty(): void
    {
        $response = $this->getJson('/api/v1/covid/latest');

        $response->assertStatus(404)
                 ->assertJson([
                     'status' => 'error',
                     'message' => 'Data belum tersedia'
                 ]);
    }

    /**
     * Test GET /api/v1/covid/province/{province} sukses.
     */
    public function test_get_covid_by_province_paginated_success(): void
    {
        $this->createCovidData('Jawa Timur', '2026-05-16', 90, 70, 4);
        $this->createCovidData('Jawa Timur', '2026-05-17', 100, 80, 5);

        $response = $this->getJson('/api/v1/covid/province/Jawa Timur?per_page=1');

        $response->assertStatus(200)
                 ->assertJsonStructure([
                     'status',
                     'message',
                     'data',
                     'meta' => [
                         'current_page',
                         'last_page',
                         'per_page',
                         'total'
                     ],
                     'timestamp'
                 ])
                 ->assertJson([
                     'status' => 'success',
                     'message' => 'Data provinsi berhasil diambil',
                     'meta' => [
                         'current_page' => 1,
                         'last_page' => 2,
                         'per_page' => 1,
                         'total' => 2
                     ]
                 ]);
    }

    /**
     * Test GET /api/v1/dashboard sukses.
     */
    public function test_get_dashboard_summary_success(): void
    {
        // Masukkan data untuk menghitung trend (membutuhkan 3 tanggal berurutan)
        $this->createCovidData('Indonesia', '2026-05-15', 400, 300, 15);
        $this->createCovidData('Indonesia', '2026-05-16', 450, 350, 18);
        $this->createCovidData('Indonesia', '2026-05-17', 500, 400, 20);

        $response = $this->getJson('/api/v1/dashboard?wilayah=Indonesia');

        $response->assertStatus(200)
                 ->assertJson([
                     'status' => 'success',
                     'message' => 'Dashboard data berhasil diambil',
                     'data' => [
                         'wilayah' => 'Indonesia',
                         'confirmed' => 500,
                         'today_increase' => 50, // 500 - 450
                         'recovered' => 400,
                         'deaths' => 20,
                     ]
                 ]);
    }

    /**
     * Test GET /api/v1/chart sukses.
     */
    public function test_get_dashboard_chart_success(): void
    {
        $this->createCovidData('Indonesia', '2026-05-16', 450, 350, 18);
        $this->createCovidData('Indonesia', '2026-05-17', 500, 400, 20);

        $response = $this->getJson('/api/v1/chart?wilayah=Indonesia&days=2');

        $response->assertStatus(200)
                 ->assertJson([
                     'status' => 'success',
                     'message' => 'Chart data berhasil diambil',
                 ]);

        $this->assertCount(2, $response->json('data'));
    }

    /**
     * Test POST /api/v1/predict sukses (Single Exponential Smoothing).
     */
    public function test_post_predict_success(): void
    {
        // Masukkan data historis yang memadai untuk perhitungan SES
        $this->createCovidData('Indonesia', '2026-05-13', 100, 80, 5);
        $this->createCovidData('Indonesia', '2026-05-14', 110, 85, 6);
        $this->createCovidData('Indonesia', '2026-05-15', 120, 90, 7);
        $this->createCovidData('Indonesia', '2026-05-16', 130, 95, 8);
        $this->createCovidData('Indonesia', '2026-05-17', 140, 100, 9);

        $response = $this->postJson('/api/v1/predict', [
            'periode' => 5
        ]);

        $response->assertStatus(200)
                 ->assertJsonStructure([
                     'status',
                     'message',
                     'data' => [
                         'periode',
                         'hasil_prediksi' => [
                             'positif',
                             'sembuh',
                             'meninggal'
                         ]
                     ],
                     'timestamp'
                 ])
                 ->assertJson([
                     'status' => 'success',
                     'message' => 'Prediksi berhasil',
                     'data' => [
                         'periode' => 5
                     ]
                 ]);

        // Pastikan hasil prediksi tersimpan ke database
        $this->assertDatabaseHas('predictions', [
            'periode' => 5,
        ]);
    }

    /**
     * Test POST /api/v1/predict gagal karena kesalahan validasi (400 Bad Request).
     */
    public function test_post_predict_validation_errors(): void
    {
        // Test Case 1: Periode kosong
        $response = $this->postJson('/api/v1/predict', []);
        $response->assertStatus(400)
                 ->assertJson([
                     'status' => 'error',
                     'message' => 'Periode harus lebih dari 0',
                 ])
                 ->assertJsonValidationErrors(['periode']);

        // Test Case 2: Periode bukan integer
        $response = $this->postJson('/api/v1/predict', ['periode' => 'tujuh']);
        $response->assertStatus(400)
                 ->assertJsonValidationErrors(['periode']);

        // Test Case 3: Periode kurang dari 1
        $response = $this->postJson('/api/v1/predict', ['periode' => 0]);
        $response->assertStatus(400)
                 ->assertJsonValidationErrors(['periode']);
    }

    /**
     * Test GET /api/v1/predict/history sukses.
     */
    public function test_get_predict_history_success(): void
    {
        // Masukkan data riwayat prediksi
        Prediction::create([
            'tanggal_prediksi' => '2026-05-18',
            'periode' => 7,
            'hasil_prediksi_positif' => 150,
            'hasil_prediksi_sembuh' => 110,
            'hasil_prediksi_meninggal' => 10,
        ]);

        $response = $this->getJson('/api/v1/predict/history');

        $response->assertStatus(200)
                 ->assertJson([
                     'status' => 'success',
                     'message' => 'Riwayat prediksi berhasil diambil',
                 ]);

        $this->assertCount(1, $response->json('data'));
    }
}
