<?php

namespace Database\Seeders;

use App\Models\CovidData;
use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\DB;
use Carbon\Carbon;

class CovidDataSeeder extends Seeder
{
    /**
     * Run the database seeds.
     */
    public function run(): void
    {
        // Path to the dataset
        $csvFile = base_path('../dataset/covid_19_indonesia_time_series_all.csv');

        if (!file_exists($csvFile)) {
            $this->command->error("CSV file not found at: {$csvFile}");
            return;
        }

        $file = fopen($csvFile, 'r');
        $header = fgetcsv($file);

        // Handle BOM (Byte Order Mark) if present
        if (isset($header[0])) {
            $header[0] = preg_replace('/^\xEF\xBB\xBF/', '', $header[0]);
        }

        // Trim headers to be safe
        $header = array_map('trim', $header);

        // Column mapping based on CSV header
        $columns = array_flip($header);

        if (!isset($columns['Date']) || !isset($columns['Location'])) {
            $this->command->error("CSV columns 'Date' or 'Location' not found. Available columns: " . implode(', ', $header));
            return;
        }

        $batchSize = 1000;
        $dataBatch = [];
        $count = 0;

        // Truncate existing data
        DB::table('covid_data')->truncate();

        while (($row = fgetcsv($file)) !== FALSE) {
            try {
                // Parse Date (M/D/YYYY to YYYY-MM-DD)
                $dateStr = $row[$columns['Date']];
                $tanggal = Carbon::createFromFormat('n/j/Y', $dateStr)->format('Y-m-d');

                $dataBatch[] = [
                    'tanggal' => $tanggal,
                    'wilayah' => $row[$columns['Location']],
                    'positif' => (int) $row[$columns['Total Cases']],
                    'sembuh' => (int) $row[$columns['Total Recovered']],
                    'meninggal' => (int) $row[$columns['Total Deaths']],
                    'created_at' => now(),
                    'updated_at' => now(),
                ];

                $count++;

                if (count($dataBatch) >= $batchSize) {
                    DB::table('covid_data')->insert($dataBatch);
                    $dataBatch = [];
                    $this->command->info("Imported {$count} records...");
                }
            } catch (\Exception $e) {
                $this->command->warn("Skipping row: " . implode(',', $row) . " - Error: " . $e->getMessage());
            }
        }

        if (count($dataBatch) > 0) {
            DB::table('covid_data')->insert($dataBatch);
        }

        fclose($file);

        $this->command->info("Finished! Imported total {$count} records.");
    }
}
