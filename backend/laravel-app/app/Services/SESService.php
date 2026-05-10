<?php

namespace App\Services;

use App\Models\CovidData;
use App\Models\Prediction;
use Carbon\Carbon;

class SESService
{
    private float $alpha = 0.9;

    /**
     * Calculate prediction using Single Exponential Smoothing
     * 
     * @param int $periode Number of days to predict into the future
     * @param string $wilayah Region to predict for
     * @return array
     */
    public function calculatePrediction(int $periode, string $wilayah = 'Indonesia'): array
    {
        // Get historical data ordered by oldest to newest for calculation
        $historicalData = CovidData::where('wilayah', $wilayah)
            ->orderBy('tanggal', 'asc')
            ->get();

        if ($historicalData->isEmpty()) {
            return [
                'positif' => 0,
                'sembuh' => 0,
                'meninggal' => 0
            ];
        }

        // Apply SES on the data series
        $predictedPositif = $this->calculateSES($historicalData->pluck('positif')->toArray());
        $predictedSembuh = $this->calculateSES($historicalData->pluck('sembuh')->toArray());
        $predictedMeninggal = $this->calculateSES($historicalData->pluck('meninggal')->toArray());

        // For SES, the forecast for future periods is flat (Ft+k = Ft+1).
        // However, if we apply it to daily changes (which is better for cumulative), 
        // we'd multiply by period. But to strictly follow simple SES on the given data:
        // We will just use the SES formula result directly.
        // Wait, if we just use the SES result on cumulative data, it will be lower than the current.
        // Let's use a slightly modified approach: apply SES to daily differences, then add to the last cumulative value.
        
        $dailyPositif = $this->getDailyDifferences($historicalData->pluck('positif')->toArray());
        $dailySembuh = $this->getDailyDifferences($historicalData->pluck('sembuh')->toArray());
        $dailyMeninggal = $this->getDailyDifferences($historicalData->pluck('meninggal')->toArray());

        $sesDailyPositif = $this->calculateSES($dailyPositif);
        $sesDailySembuh = $this->calculateSES($dailySembuh);
        $sesDailyMeninggal = $this->calculateSES($dailyMeninggal);

        $lastRecord = $historicalData->last();

        $finalPositif = $lastRecord->positif + ($sesDailyPositif * $periode);
        $finalSembuh = $lastRecord->sembuh + ($sesDailySembuh * $periode);
        $finalMeninggal = $lastRecord->meninggal + ($sesDailyMeninggal * $periode);

        $result = [
            'positif' => (int) round($finalPositif),
            'sembuh' => (int) round($finalSembuh),
            'meninggal' => (int) round($finalMeninggal),
        ];

        // Save prediction to database
        Prediction::create([
            'tanggal_prediksi' => Carbon::parse($lastRecord->tanggal)->addDays($periode)->format('Y-m-d'),
            'periode' => $periode,
            'hasil_prediksi_positif' => $result['positif'],
            'hasil_prediksi_sembuh' => $result['sembuh'],
            'hasil_prediksi_meninggal' => $result['meninggal'],
        ]);

        return $result;
    }

    /**
     * Helper to get daily differences from cumulative array
     */
    private function getDailyDifferences(array $cumulative): array
    {
        $daily = [];
        if (empty($cumulative)) return $daily;
        
        $daily[] = $cumulative[0]; // first day is itself
        for ($i = 1; $i < count($cumulative); $i++) {
            $diff = $cumulative[$i] - $cumulative[$i - 1];
            $daily[] = $diff > 0 ? $diff : 0; // ensure no negative daily cases if data is anomalous
        }
        return $daily;
    }

    /**
     * Core SES algorithm calculation
     * Formula: Ft+1 = alpha * Yt + (1 - alpha) * Ft
     */
    private function calculateSES(array $data): float
    {
        if (empty($data)) return 0.0;

        // F1 = Y1 (Initialization)
        $forecast = $data[0];

        // Calculate up to the end of the series
        for ($i = 1; $i < count($data); $i++) {
            $forecast = ($this->alpha * $data[$i]) + ((1 - $this->alpha) * $forecast);
        }

        // The forecast variable now holds F_t+1 (the prediction for the next period)
        return $forecast;
    }
}
