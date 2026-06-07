<?php

namespace App\Services;

use App\Models\CovidData;
use App\Models\Prediction;
use Carbon\Carbon;

class SESService
{
    public function calculatePrediction(
        int $periode,
        string $wilayah = 'Indonesia',
        float $alpha = 0.7
    ): array {
        $historicalData = CovidData::whereRaw('LOWER(wilayah) = ?', [strtolower($wilayah)])
            ->orderBy('tanggal', 'desc')
            ->limit(30)
            ->get()
            ->reverse()
            ->values();

        if ($historicalData->count() < 3) {
            return [
                'success' => false,
                'message' => 'Data historis tidak cukup untuk melakukan prediksi',
                'data' => null,
            ];
        }

        $dailyPositiveCases = $this->getDailyDifferences(
            $historicalData->pluck('positif')->toArray()
        );

        $dailyRecoveredCases = $this->getDailyDifferences(
            $historicalData->pluck('sembuh')->toArray()
        );

        $dailyDeathCases = $this->getDailyDifferences(
            $historicalData->pluck('meninggal')->toArray()
        );

        if (count($dailyPositiveCases) < 2) {
            return [
                'success' => false,
                'message' => 'Data kasus harian tidak cukup untuk prediksi',
                'data' => null,
            ];
        }

        $positiveSesResult = $this->calculateSESWithErrors($dailyPositiveCases, $alpha);
        $recoveredSesResult = $this->calculateSESWithErrors($dailyRecoveredCases, $alpha);
        $deathSesResult = $this->calculateSESWithErrors($dailyDeathCases, $alpha);

        $forecastDaily = max(0, (int) round($positiveSesResult['forecast']));
        $forecastRecoveredDaily = max(0, (int) round($recoveredSesResult['forecast']));
        $forecastDeathDaily = max(0, (int) round($deathSesResult['forecast']));

        $errors = $positiveSesResult['errors'];

        $lastRecord = $historicalData->last();

        $lastCumulative = (int) $lastRecord->positif;
        $lastRecovered = (int) $lastRecord->sembuh;
        $lastDeaths = (int) $lastRecord->meninggal;

        $estimatedCases = $lastCumulative + ($forecastDaily * $periode);
        $estimatedRecovered = $lastRecovered + ($forecastRecoveredDaily * $periode);
        $estimatedDeaths = $lastDeaths + ($forecastDeathDaily * $periode);

        $mae = count($errors) > 0
            ? array_sum($errors) / count($errors)
            : 0.0;

        $avgErrorPercent = $forecastDaily > 0
            ? ($mae / $forecastDaily) * 100
            : 0.0;

        $confidenceInterval = max(0, min(100, 100 - $avgErrorPercent));

        $lastDailyCase = end($dailyPositiveCases);

        $trendPercentage = $lastDailyCase > 0
            ? (($forecastDaily - $lastDailyCase) / $lastDailyCase) * 100
            : 0.0;

        $trendStatus = $trendPercentage >= 0
            ? 'Kenaikan'
            : 'Penurunan';

        \Log::info('Debug hasil prediksi SES', [
            'wilayah' => $wilayah,
            'periode' => $periode,
            'alpha' => $alpha,

            'last_positif' => $lastCumulative,
            'forecast_daily_positif' => $forecastDaily,
            'estimated_positif' => $estimatedCases,

            'last_sembuh' => $lastRecovered,
            'forecast_daily_sembuh' => $forecastRecoveredDaily,
            'estimated_sembuh' => $estimatedRecovered,

            'last_meninggal' => $lastDeaths,
            'forecast_daily_meninggal' => $forecastDeathDaily,
            'estimated_meninggal' => $estimatedDeaths,

            'last_daily_case' => $lastDailyCase,
            'trend_percentage' => $trendPercentage,
            'avg_error_percent' => $avgErrorPercent,
            'confidence_interval' => $confidenceInterval,
        ]);

        $predictionDate = Carbon::parse($lastRecord->tanggal)
            ->addDays($periode)
            ->format('Y-m-d');

        \Log::info('Debug tanggal prediksi', [
            'last_record_tanggal' => $lastRecord->tanggal,
            'periode' => $periode,
            'prediction_date' => $predictionDate,
        ]);

        $this->savePrediction(
            predictionDate: $predictionDate,
            periode: $periode,
            estimatedCases: $estimatedCases,
            estimatedRecovered: $estimatedRecovered,
            estimatedDeaths: $estimatedDeaths
        );

        return [
            'success' => true,
            'message' => 'Prediksi berhasil',
            'data' => [
                'wilayah' => $wilayah,
                'prediction_days' => $periode,
                'alpha' => $alpha,

                'estimated_cases' => $estimatedCases,
                'forecast_daily_cases' => $forecastDaily,
                'last_daily_cases' => $lastDailyCase,

                'trend_status' => $trendStatus,
                'trend_percentage' => round($trendPercentage, 1),

                'confidence_interval' => round($confidenceInterval, 1),
                'avg_error' => round($avgErrorPercent, 2),
            ],
        ];
    }

    private function getDailyDifferences(array $cumulative): array
    {
        $daily = [];

        if (count($cumulative) < 2) {
            return $daily;
        }

        for ($i = 1; $i < count($cumulative); $i++) {
            $previous = (int) $cumulative[$i - 1];
            $current = (int) $cumulative[$i];

            $daily[] = max(0, $current - $previous);
        }

        return $daily;
    }

    private function calculateSESWithErrors(array $data, float $alpha): array
    {
        if (empty($data)) {
            return [
                'forecast' => 0.0,
                'errors' => [],
            ];
        }

        $level = $data[0];
        $errors = [];

        for ($i = 1; $i < count($data); $i++) {
            $forecast = $level;
            $actual = $data[$i];

            $errors[] = abs($actual - $forecast);

            $level = ($alpha * $actual) + ((1 - $alpha) * $level);
        }

        return [
            'forecast' => $level,
            'errors' => $errors,
        ];
    }

    private function savePrediction(
        string $predictionDate,
        int $periode,
        int $estimatedCases,
        int $estimatedRecovered,
        int $estimatedDeaths
    ): void {
        try {
            Prediction::create([
                'tanggal_prediksi' => $predictionDate,
                'periode' => $periode,
                'hasil_prediksi_positif' => $estimatedCases,
                'hasil_prediksi_sembuh' => $estimatedRecovered,
                'hasil_prediksi_meninggal' => $estimatedDeaths,
            ]);
        } catch (\Throwable $e) {
            \Log::error('Gagal simpan prediksi: ' . $e->getMessage());
        }
    }
}