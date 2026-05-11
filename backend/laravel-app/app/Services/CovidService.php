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
     * 🔥 DASHBOARD SUMMARY (FIXED VERSION)
     */
    public function getSummary(?string $wilayah = 'Indonesia'): array
    {
        // 🔥 set locale Indonesia
        Carbon::setLocale('id');
        setlocale(LC_TIME, 'id_ID');

        // 🔥 query aman (tidak case sensitive)
        $latest = CovidData::whereRaw('LOWER(wilayah) = ?', [strtolower($wilayah)])
            ->orderBy('tanggal', 'desc')
            ->first();

        if (!$latest) {
            return [
                'wilayah' => $wilayah,
                'last_updated' => '-',
                'confirmed' => 0,
                'today_increase' => 0,
                'recovered' => 0,
                'recovered_rate' => 0.0,
                'deaths' => 0,
                'death_rate' => 0.0,
                'trend_percent' => 0.0,
                'trend_status' => 'Stabil',
                'model_confidence' => 0.0
            ];
        }

        // 🔥 data sebelumnya (untuk kenaikan harian)
        $previous = CovidData::whereRaw('LOWER(wilayah) = ?', [strtolower($wilayah)])
            ->where('tanggal', '<', $latest->tanggal)
            ->orderBy('tanggal', 'desc')
            ->first();

        $todayIncrease = $previous 
            ? ($latest->positif - $previous->positif) 
            : 0;

        // 🔥 rate
        $recoveredRate = $latest->positif > 0 
            ? round(($latest->sembuh / $latest->positif) * 100, 1) 
            : 0.0;

        $deathRate = $latest->positif > 0 
            ? round(($latest->meninggal / $latest->positif) * 100, 1) 
            : 0.0;

        // 🔥 TREND
        $trendStatus = 'Penurunan'; // default aman
        $trendPercent = 0.0;

        if ($previous) {
            $prevPrevious = CovidData::whereRaw('LOWER(wilayah) = ?', [strtolower($wilayah)])
                ->where('tanggal', '<', $previous->tanggal)
                ->orderBy('tanggal', 'desc')
                ->first();

            $prevIncrease = $prevPrevious 
                ? ($previous->positif - $prevPrevious->positif) 
                : 0;

            if ($todayIncrease >= $prevIncrease) {
                $trendStatus = 'Kenaikan';
            } else {
                $trendStatus = 'Penurunan';
            }

            if ($prevIncrease > 0) {
                $trendPercent = round((($todayIncrease - $prevIncrease) / $prevIncrease) * 100, 1);
            }
        }

        // 🔥 buat minus kalau penurunan
        if ($trendStatus === 'Penurunan') {
            $trendPercent = -abs($trendPercent);
        }

        return [
            'wilayah' => $wilayah,
            'last_updated' => $latest->tanggal->translatedFormat('d M Y, h:i A'),
            'confirmed' => $latest->positif,
            'today_increase' => $todayIncrease,
            'recovered' => $latest->sembuh,
            'recovered_rate' => $recoveredRate,
            'deaths' => $latest->meninggal,
            'death_rate' => $deathRate,
            'trend_percent' => $trendPercent,
            'trend_status' => $trendStatus,
            'model_confidence' => 85.5
        ];
    }

    /**
     * Chart data
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
}