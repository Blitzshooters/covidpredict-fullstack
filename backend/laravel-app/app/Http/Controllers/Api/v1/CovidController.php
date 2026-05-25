<?php

namespace App\Http\Controllers\Api\v1;

use App\Http\Controllers\Controller;
use App\Services\CovidService;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Cache;

class CovidController extends Controller
{
    protected $covidService;

    public function __construct(CovidService $covidService)
    {
        $this->covidService = $covidService;
    }

    /**
     * Display a listing of the resource.
     */
    public function index(Request $request): JsonResponse
    {
        $wilayah = $request->query('wilayah', 'Indonesia');
        $days = (int) $request->query('days', 30);
        $startDate = $request->query('start_date');
        $endDate = $request->query('end_date');

        $data = $this->covidService->getHistory(
            wilayah: $wilayah,
            days: $days,
            startDate: $startDate,
            endDate: $endDate
        );

        return response()->json([
            'status' => 'success',
            'message' => 'Data berhasil diambil',
            'data' => $data,
            'timestamp' => now()->toIso8601String(),
        ]);
    }

    /**
     * Display the specified resource.
     */
    public function show(int $id): JsonResponse
    {
        $cacheKey = 'covid_detail_' . $id;

        $data = Cache::remember($cacheKey, 3600, function () use ($id) {
            return $this->covidService->getById($id);
        });

        if (!$data) {
            return response()->json([
                'status' => 'error',
                'message' => 'Data tidak ditemukan',
                'errors' => null,
                'timestamp' => now()->toIso8601String(),
            ], 404);
        }

        return response()->json([
            'status' => 'success',
            'message' => 'Detail data ditemukan',
            'data' => $data,
            'timestamp' => now()->toIso8601String(),
        ]);
    }

    /**
     * Get the latest record.
     */
    public function latest(): JsonResponse
    {
        $data = Cache::remember('covid_latest', 3600, function () {
            return $this->covidService->getLatest();
        });

        if (!$data) {
            return response()->json([
                'status' => 'error',
                'message' => 'Data belum tersedia',
                'errors' => null,
                'timestamp' => now()->toIso8601String(),
            ], 404);
        }

        return response()->json([
            'status' => 'success',
            'message' => 'Data terbaru ditemukan',
            'data' => $data,
            'timestamp' => now()->toIso8601String(),
        ]);
    }

    /**
     * Get paginated COVID data filtered by province.
     */
    public function byProvince(Request $request, string $province): JsonResponse
    {
        $perPage = $request->query('per_page', 15);
        $page = $request->query('page', 1);
        $cacheKey = "covid_province_{$province}_{$perPage}_page_{$page}";

        $paginator = Cache::remember($cacheKey, 3600, function () use ($province, $perPage) {
            return $this->covidService->getByProvincePaginated($province, $perPage);
        });

        return response()->json([
            'status' => 'success',
            'message' => 'Data provinsi berhasil diambil',
            'data' => $paginator->items(),
            'meta' => [
                'current_page' => $paginator->currentPage(),
                'last_page' => $paginator->lastPage(),
                'per_page' => $paginator->perPage(),
                'total' => $paginator->total(),
            ],
            'timestamp' => now()->toIso8601String(),
        ]);
    }
}
