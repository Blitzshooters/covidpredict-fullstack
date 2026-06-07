<?php

namespace App\Http\Controllers\Api\v1;

use App\Http\Controllers\Controller;
use App\Services\CovidService;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class DashboardController extends Controller
{
    protected CovidService $covidService;

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

        $summary = $this->covidService->getSummary($wilayah);

        return response()->json([
            'status'    => 'success',
            'message'   => 'Dashboard data berhasil diambil',
            'data'      => $summary,
            'timestamp' => now()->toIso8601String(),
        ]);
    }

    /**
     * Get chart data.
     */
    public function chart(Request $request): JsonResponse
    {
        $wilayah = $request->query('wilayah', 'Indonesia');
        $period  = $request->query('period', 'harian');

        if (!in_array($period, ['harian', 'mingguan', 'bulanan'])) {
            $period = 'harian';
        }

        $result = $this->covidService->getChartAnalysis($wilayah, $period);

        return response()->json([
            'status'    => 'success',
            'message'   => 'Data grafik berhasil diambil',
            'data'      => $result,
            'timestamp' => now()->toIso8601String(),
        ]);
    }
}