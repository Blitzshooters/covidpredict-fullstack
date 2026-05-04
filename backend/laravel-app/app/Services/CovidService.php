<?php

namespace App\Services;

use App\Models\CovidData;
use Illuminate\Support\Collection;

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
     * @return array
     */
    public function getSummary(): array
    {
        $latest = CovidData::where('wilayah', 'Indonesia')
            ->orderBy('tanggal', 'desc')
            ->first();

        if (!$latest) {
            return [
                'total_positive' => 0,
                'total_recovered' => 0,
                'total_deaths' => 0,
                'latest_data' => null
            ];
        }

        return [
            'total_positive' => $latest->positif,
            'total_recovered' => $latest->sembuh,
            'total_deaths' => $latest->meninggal,
            'latest_data' => $latest
        ];
    }

    /**
     * Get chart data (e.g., last 30 days).
     *
     * @param int $days
     * @return Collection
     */
    public function getChartData(int $days = 30): Collection
    {
        return CovidData::where('wilayah', 'Indonesia')
            ->orderBy('tanggal', 'desc')
            ->limit($days)
            ->get()
            ->reverse()
            ->values();
    }
}
