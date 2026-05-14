<?php

namespace App\Http\Controllers\Api\v1;

use App\Http\Controllers\Controller;
use App\Services\CovidService;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Cache;

class DashboardController extends Controller
{
    protected $covidService;

    public function __construct(CovidService $covidService)
    {
        $this->covidService = $covidService;
    }

    /**
     * Get dashboard summary.
     */
    public function index(Request $request): JsonResponse
    {
        $wilayah = $request->query('wilayah', 'Indonesia');
        $cacheKey = 'dashboard_summary_' . $wilayah;

        $summary = Cache::remember($cacheKey, 3600, function () use ($wilayah) {
            return $this->covidService->getSummary($wilayah);
        });

        return response()->json([
            'status' => 'success',
            'message' => 'Dashboard data berhasil diambil',
            'data' => $summary,
            'timestamp' => now()->toIso8601String(),
        ]);
    }

    /**
     * Get chart data.
     */
    public function chart(Request $request): JsonResponse
    {
        $days = $request->query('days', 30);
        $wilayah = $request->query('wilayah', 'Indonesia');
        $cacheKey = "dashboard_chart_{$wilayah}_{$days}";

        $data = Cache::remember($cacheKey, 3600, function () use ($days, $wilayah) {
            return $this->covidService->getChartData((int) $days, $wilayah);
        });

        return response()->json([
            'status' => 'success',
            'message' => 'Chart data berhasil diambil',
            'data' => $data,
            'timestamp' => now()->toIso8601String(),
        ]);
    }
}
