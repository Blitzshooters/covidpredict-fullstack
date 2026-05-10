<?php

namespace App\Services;

use App\Models\CovidData;
use Illuminate\Support\Collection;
use Illuminate\Contracts\Pagination\LengthAwarePaginator;
class CovidService
{
    /**
     * Get all COVID data, optionally filtered by wilayah.
     *
     * @param string|null $wilayah
     * @return Collection
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
     * Get the latest COVID data record.
     *
     * @return CovidData|null
     */
    public function getLatest(): ?CovidData
    {
        return CovidData::orderBy('tanggal', 'desc')->first();
    }

    /**
     * Get data by ID.
     *
     * @param int $id
     * @return CovidData|null
     */
    public function getById(int $id): ?CovidData
    {
        return CovidData::find($id);
    }

    /**
     * Get summary data for the dashboard.
     *
     * @param string|null $wilayah
     * @return array
     */
    public function getSummary(?string $wilayah = 'Indonesia'): array
    {
        $latest = CovidData::where('wilayah', $wilayah)
            ->orderBy('tanggal', 'desc')
            ->first();

        if (!$latest) {
            return [
                'last_updated' => '-',
                'confirmed' => 0,
                'today_increase' => 0,
                'recovered' => 0,
                'recovered_rate' => 0.0,
                'deaths' => 0,
                'death_rate' => 0.0,
                'trend_percent' => 0.0,
                'trend_status' => 'stable',
                'model_confidence' => 0.0
            ];
        }

        // Ambil data hari sebelumnya untuk kalkulasi kenaikan harian
        $previous = CovidData::where('wilayah', $wilayah)
            ->where('tanggal', '<', $latest->tanggal)
            ->orderBy('tanggal', 'desc')
            ->first();

        $todayIncrease = $previous ? ($latest->positif - $previous->positif) : 0;
        
        $recoveredRate = $latest->positif > 0 
            ? round(($latest->sembuh / $latest->positif) * 100, 1) 
            : 0.0;
            
        $deathRate = $latest->positif > 0 
            ? round(($latest->meninggal / $latest->positif) * 100, 1) 
            : 0.0;

        // Logika tren sederhana berdasarkan kenaikan kasus harian
        $trendStatus = 'stable';
        $trendPercent = 0.0;
        if ($previous) {
            $prevPrevious = CovidData::where('wilayah', $wilayah)
                ->where('tanggal', '<', $previous->tanggal)
                ->orderBy('tanggal', 'desc')
                ->first();
            
            $prevIncrease = $prevPrevious ? ($previous->positif - $prevPrevious->positif) : 0;
            
            if ($todayIncrease > $prevIncrease) {
                $trendStatus = 'up';
            } elseif ($todayIncrease < $prevIncrease) {
                $trendStatus = 'down';
            }
            
            if ($prevIncrease > 0) {
                $trendPercent = round((($todayIncrease - $prevIncrease) / $prevIncrease) * 100, 1);
            }
        }

        return [
            'last_updated' => $latest->tanggal->format('d M Y, h:i A'),
            'confirmed' => $latest->positif,
            'today_increase' => $todayIncrease,
            'recovered' => $latest->sembuh,
            'recovered_rate' => $recoveredRate,
            'deaths' => $latest->meninggal,
            'death_rate' => $deathRate,
            'trend_percent' => abs($trendPercent),
            'trend_status' => $trendStatus,
            'model_confidence' => 85.5 // Placeholder until AI model integrated
        ];
    }

    /**
     * Get chart data (e.g., last 30 days).
     *
     * @param int $days
     * @param string|null $wilayah
     * @return Collection
     */
    public function getChartData(int $days = 30, ?string $wilayah = 'Indonesia'): Collection
    {
        return CovidData::where('wilayah', $wilayah)
            ->orderBy('tanggal', 'desc')
            ->limit($days)
            ->get()
            ->reverse()
            ->values();
    }

    /**
     * Get paginated COVID data filtered by province.
     *
     * @param string $province
     * @param int $perPage
     * @return LengthAwarePaginator
     */
    public function getByProvincePaginated(string $province, int $perPage = 15): LengthAwarePaginator
    {
        return CovidData::where('wilayah', 'like', "%{$province}%")
            ->orderBy('tanggal', 'desc')
            ->paginate($perPage);
    }
}
