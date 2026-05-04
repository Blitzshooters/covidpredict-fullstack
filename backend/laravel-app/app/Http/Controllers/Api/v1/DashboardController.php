<?php

namespace App\Http\Controllers\Api\v1;

use App\Http\Controllers\Controller;
use App\Services\CovidService;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

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
    public function index(): JsonResponse
    {
        $summary = $this->covidService->getSummary();

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
        $data = $this->covidService->getChartData((int) $days);

        return response()->json([
            'status' => 'success',
            'message' => 'Chart data berhasil diambil',
            'data' => $data,
            'timestamp' => now()->toIso8601String(),
        ]);
    }
}
