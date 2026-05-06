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
