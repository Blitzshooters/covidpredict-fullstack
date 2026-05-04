<?php

namespace App\Http\Controllers\Api\v1;

use App\Http\Controllers\Controller;
use App\Services\CovidService;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

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
        $wilayah = $request->query('wilayah');
        $data = $this->covidService->getAll($wilayah);

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
        $data = $this->covidService->getById($id);

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
        $data = $this->covidService->getLatest();

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
}
