<?php

namespace App\Console\Commands;

use Illuminate\Console\Attributes\Description;
use Illuminate\Console\Attributes\Signature;
use Illuminate\Console\Command;

class ImportCovidData extends Command
{
    /**
     * The name and signature of the console command.
     *
     * @var string
     */
    protected $signature = 'covid:import {--limit=100 : Number of records to import}';

    /**
     * The console command description.
     *
     * @var string
     */
    protected $description = 'Import COVID-19 data from the local CSV dataset';

    /**
     * Execute the console command.
     */
    public function handle()
    {
        $filePath = base_path('../dataset/covid_19_indonesia_time_series_all.csv');

        if (!file_exists($filePath)) {
            $this->error("Dataset file not found at: {$filePath}");
            return 1;
        }

        $file = fopen($filePath, 'r');
        $header = fgetcsv($file); // Skip header

        // Simple mapping based on known headers
        // Date,Location ISO Code,Location,New Cases,New Deaths,New Recovered,New Active Cases,Total Cases,Total Deaths,Total Recovered,Total Active Cases...
        
        $count = 0;
        $limit = $this->option('limit');

        $this->info("Importing data...");

        while (($row = fgetcsv($file)) !== false && $count < $limit) {
            // Mapping (adjust indices based on CSV structure)
            // 0: Date
            // 2: Location
            // 3: New Cases
            // 4: New Deaths
            // 5: New Recovered
            // 7: Total Cases
            // 8: Total Deaths
            // 9: Total Recovered

            \App\Models\CovidData::updateOrCreate(
                [
                    'tanggal' => \Carbon\Carbon::parse($row[0])->format('Y-m-d'),
                    'wilayah' => $row[2],
                ],
                [
                    'positif' => (int) $row[7],  // Total Cases
                    'meninggal' => (int) $row[8], // Total Deaths
                    'sembuh' => (int) $row[9],    // Total Recovered
                ]
            );

            $count++;
            if ($count % 10 === 0) {
                $this->line("Imported {$count} records...");
            }
        }

        fclose($file);
        $this->info("Successfully imported {$count} records.");
        return 0;
    }
}
