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
