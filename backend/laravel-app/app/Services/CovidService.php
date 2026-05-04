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
}
