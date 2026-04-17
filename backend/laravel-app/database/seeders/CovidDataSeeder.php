<?php

namespace Database\Seeders;

use Illuminate\Database\Console\Seeds\WithoutModelEvents;
use Illuminate\Database\Seeder;

class CovidDataSeeder extends Seeder
{
    /**
     * Run the database seeds.
     */
    public function run(): void
    {
        $data = [
            ['tanggal' => '2026-01-01', 'wilayah' => 'Jawa Timur', 'positif' => 100, 'sembuh' => 80, 'meninggal' => 5],
            ['tanggal' => '2026-01-02', 'wilayah' => 'Jawa Timur', 'positif' => 120, 'sembuh' => 90, 'meninggal' => 6],
            ['tanggal' => '2026-01-03', 'wilayah' => 'Jawa Timur', 'positif' => 110, 'sembuh' => 95, 'meninggal' => 4],
            ['tanggal' => '2026-01-04', 'wilayah' => 'Jawa Timur', 'positif' => 130, 'sembuh' => 100, 'meninggal' => 7],
            ['tanggal' => '2026-01-05', 'wilayah' => 'Jawa Timur', 'positif' => 140, 'sembuh' => 110, 'meninggal' => 5],
            ['tanggal' => '2026-01-06', 'wilayah' => 'Jawa Timur', 'positif' => 150, 'sembuh' => 120, 'meninggal' => 8],
            ['tanggal' => '2026-01-07', 'wilayah' => 'Jawa Timur', 'positif' => 160, 'sembuh' => 130, 'meninggal' => 6],
            ['tanggal' => '2026-01-08', 'wilayah' => 'Jawa Timur', 'positif' => 170, 'sembuh' => 140, 'meninggal' => 9],
            ['tanggal' => '2026-01-09', 'wilayah' => 'Jawa Timur', 'positif' => 180, 'sembuh' => 150, 'meninggal' => 7],
            ['tanggal' => '2026-01-10', 'wilayah' => 'Jawa Timur', 'positif' => 190, 'sembuh' => 160, 'meninggal' => 10],
        ];

        foreach ($data as $item) {
            \App\Models\CovidData::create($item);
        }
    }
}
