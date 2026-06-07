<?php

namespace App\Services;

use App\Models\CovidData;
use Illuminate\Support\Collection;
use Illuminate\Contracts\Pagination\LengthAwarePaginator;
use Carbon\Carbon;

class CovidService
{
    /**
     * Get all COVID data, optionally filtered by wilayah.
     */
    public function getAll(?string $wilayah = null): Collection
    {
        $query = CovidData::query();

        if ($wilayah) {
            $query->where('wilayah', 'like', "%{$wilayah}%");
        }

        return $query->orderBy('tanggal', 'desc')->get();
    }

    /**
     * Get latest data
     */
    public function getLatest(): ?CovidData
    {
        return CovidData::orderBy('tanggal', 'desc')->first();
    }

    /**
     * Get data by ID
     */
    public function getById(int $id): ?CovidData
    {
        return CovidData::find($id);
    }

    /**
     * Dashboard Summary
     */
    public function getSummary(?string $wilayah = 'Indonesia'): array
    {
        Carbon::setLocale('id');
        setlocale(LC_TIME, 'id_ID');

        $latest = CovidData::whereRaw('LOWER(wilayah) = ?', [strtolower($wilayah)])
            ->orderBy('tanggal', 'desc')
            ->first();

        if (!$latest) {
            return [
                'wilayah'          => $wilayah,
                'last_updated'     => '-',
                'confirmed'        => 0,
                'today_increase'   => 0,
                'recovered'        => 0,
                'recovered_rate'   => 0.0,
                'deaths'           => 0,
                'death_rate'       => 0.0,
                'trend_percent'    => 0.0,
                'trend_status'     => 'Stabil',
                'model_confidence' => 0.0,
            ];
        }

        $previous = CovidData::whereRaw('LOWER(wilayah) = ?', [strtolower($wilayah)])
            ->where('tanggal', '<', $latest->tanggal)
            ->orderBy('tanggal', 'desc')
            ->first();

        $todayIncrease = $previous
            ? ($latest->positif - $previous->positif)
            : 0;

        $recoveredRate = $latest->positif > 0
            ? round(($latest->sembuh / $latest->positif) * 100, 1)
            : 0.0;

        $deathRate = $latest->positif > 0
            ? round(($latest->meninggal / $latest->positif) * 100, 1)
            : 0.0;

        $trendStatus  = 'Penurunan';
        $trendPercent = 0.0;

        if ($previous) {
            $prevPrevious = CovidData::whereRaw('LOWER(wilayah) = ?', [strtolower($wilayah)])
                ->where('tanggal', '<', $previous->tanggal)
                ->orderBy('tanggal', 'desc')
                ->first();

            $prevIncrease = $prevPrevious
                ? ($previous->positif - $prevPrevious->positif)
                : 0;

            $trendStatus = $todayIncrease >= $prevIncrease ? 'Kenaikan' : 'Penurunan';

            if ($prevIncrease > 0) {
                $trendPercent = round((($todayIncrease - $prevIncrease) / $prevIncrease) * 100, 1);
            }
        }

        if ($trendStatus === 'Penurunan') {
            $trendPercent = -abs($trendPercent);
        }

        return [
            'wilayah'          => $wilayah,
            'last_updated'     => Carbon::parse($latest->tanggal)->translatedFormat('d M Y, h:i A'),
            'confirmed'        => $latest->positif,
            'today_increase'   => $todayIncrease,
            'recovered'        => $latest->sembuh,
            'recovered_rate'   => $recoveredRate,
            'deaths'           => $latest->meninggal,
            'death_rate'       => $deathRate,
            'trend_percent'    => $trendPercent,
            'trend_status'     => $trendStatus,
            'model_confidence' => 85.5,
        ];
    }

    /**
     * Chart data (raw)
     */
    public function getChartData(int $days = 30, ?string $wilayah = 'Indonesia'): Collection
    {
        return CovidData::whereRaw('LOWER(wilayah) = ?', [strtolower($wilayah)])
            ->orderBy('tanggal', 'desc')
            ->limit($days)
            ->get()
            ->reverse()
            ->values();
    }

    /**
     * Province paginated data
     */
    public function getByProvincePaginated(string $province, int $perPage = 15): LengthAwarePaginator
    {
        return CovidData::where('wilayah', 'like', "%{$province}%")
            ->orderBy('tanggal', 'desc')
            ->paginate($perPage);
    }

    /**
     * Get historical data for a specific wilayah.
     */
    public function getHistory(
        string $wilayah = 'Indonesia',
        int $days = 30,
        ?string $startDate = null,
        ?string $endDate = null
    ): Collection {
        $query = CovidData::whereRaw('LOWER(wilayah) = ?', [strtolower($wilayah)])
            ->orderBy('tanggal', 'desc');

        if ($startDate && $endDate) {
            $query->whereBetween('tanggal', [$startDate, $endDate]);
        } else {
            if ($days > 0) {
                $query->limit($days);
            }
        }

        return $query->get([
            'tanggal',
            'wilayah',
            'positif',
            'sembuh',
            'meninggal',
        ]);
    }

    /**
     * Get chart analysis with SES prediction model.
     * Label selalu dikirim sebagai ISO date (yyyy-MM-dd)
     * agar Android bisa format sesuai kebutuhan tiap tab.
     */
    public function getChartAnalysis(string $wilayah = 'Indonesia', string $period = 'harian'): array
    {
        $limit = match ($period) {
            'mingguan' => 49,  // 7 minggu x 7 hari
            'bulanan'  => 210, // 7 bulan x 30 hari
            default    => 14,  // 14 hari untuk ambil 7 data harian
        };

        $records = CovidData::whereRaw('LOWER(wilayah) = ?', [strtolower($wilayah)])
            ->orderBy('tanggal', 'desc')
            ->limit($limit)
            ->get()
            ->reverse()
            ->values();

        if ($records->count() < 3) {
            return [
                'avg_error'     => '0.0%',
                'insight_title' => 'Data Belum Cukup',
                'insight_text'  => 'Data historis belum cukup untuk membandingkan kasus aktual dan hasil prediksi.',
                'chart_data'    => [],
            ];
        }

        // Hitung kasus harian (selisih antar hari)
        $dailyCases = [];
        for ($i = 1; $i < $records->count(); $i++) {
            $previous = (int) $records[$i - 1]->positif;
            $current  = (int) $records[$i]->positif;
            $dailyCases[] = [
                'tanggal' => $records[$i]->tanggal, // tetap ISO: "2023-10-24"
                'actual'  => max(0, $current - $previous),
            ];
        }

        // Kelompokkan sesuai periode
        $groupedData = match ($period) {
            'mingguan' => $this->groupByWeek($dailyCases),
            'bulanan'  => $this->groupByMonth($dailyCases),
            default    => $this->groupDaily($dailyCases),
        };

        if (count($groupedData) < 2) {
            return [
                'avg_error'     => '0.0%',
                'insight_title' => 'Data Belum Cukup',
                'insight_text'  => 'Data historis belum cukup untuk membuat grafik prediksi.',
                'chart_data'    => [],
            ];
        }

        // SES (Single Exponential Smoothing) alpha = 0.7
        $alpha    = 0.7;
        $level    = $groupedData[0]['actual'];
        $errors   = [];
        $chartData = [];

        foreach ($groupedData as $index => $item) {
            if ($index === 0) {
                $prediction = $level;
            } else {
                $prediction = $level;
                $errors[]   = abs($item['actual'] - $prediction);
                $level      = ($alpha * $item['actual']) + ((1 - $alpha) * $level);
            }

            $chartData[] = [
                'label'      => $item['label'], // ISO date atau tanggal awal periode
                'actual'     => round($item['actual'], 1),
                'prediction' => round($prediction, 1),
            ];
        }

        // Hitung rata-rata error
        $mae       = count($errors) > 0 ? array_sum($errors) / count($errors) : 0.0;
        $avgActual = collect($groupedData)->avg('actual');

        $avgErrorPercent = $avgActual > 0
            ? ($mae / $avgActual) * 100
            : 0.0;

        // Insight teks
        $latestActual     = end($groupedData)['actual'];
        $latestPrediction = end($chartData)['prediction'];

        $trendText = $latestPrediction >= $latestActual
            ? 'Model memperkirakan potensi kenaikan kasus dibanding data aktual terbaru.'
            : 'Model memperkirakan potensi penurunan kasus dibanding data aktual terbaru.';

        return [
            'avg_error'     => round($avgErrorPercent, 1) . '%',
            'insight_title' => 'Wawasan Model',
            'insight_text'  => $trendText . ' Perbandingan aktual dan prediksi dihitung menggunakan metode Single Exponential Smoothing pada data kasus harian.',
            'chart_data'    => $chartData,
        ];
    }

    /**
     * Harian: ambil 7 data terakhir, label = ISO date
     */
    private function groupDaily(array $data): array
    {
        return collect($data)
            ->take(-7)
            ->values()
            ->map(fn($item) => [
                'label'  => $item['tanggal'], // "2023-10-24"
                'actual' => $item['actual'],
            ])
            ->toArray();
    }

    /**
     * Mingguan: kelompokkan per 7 hari, label = tanggal ISO awal minggu
     */
    private function groupByWeek(array $data): array
    {
        $grouped = [];
        foreach ($data as $item) {
            // Group berdasarkan tahun-minggu aktual
            $weekKey = Carbon::parse($item['tanggal'])->format('o-W'); // "2023-42"
            if (!isset($grouped[$weekKey])) {
                $grouped[$weekKey] = ['label' => $item['tanggal'], 'actual' => 0];
            }
            $grouped[$weekKey]['actual'] += $item['actual'];
        }

        return collect(array_values($grouped))
            ->take(-7)
            ->values()
            ->toArray();
    }

    private function groupByMonth(array $data): array
    {
        $grouped = [];
        foreach ($data as $item) {
            $monthKey = Carbon::parse($item['tanggal'])->format('Y-m'); // "2023-10"
            if (!isset($grouped[$monthKey])) {
                $grouped[$monthKey] = ['label' => $item['tanggal'], 'actual' => 0];
            }
            $grouped[$monthKey]['actual'] += $item['actual'];
        }

        return collect(array_values($grouped))
            ->take(-7)
            ->values()
            ->toArray();
    }
}