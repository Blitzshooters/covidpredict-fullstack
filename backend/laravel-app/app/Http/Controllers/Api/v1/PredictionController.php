<?php

namespace App\Http\Controllers\Api\v1;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Cache;

class PredictionController extends Controller
{
    protected $sesService;

    public function __construct(\App\Services\SESService $sesService)
    {
        $this->sesService = $sesService;
    }

    public function predict(Request $request)
    {
        $validator = \Illuminate\Support\Facades\Validator::make($request->all(), [
            'periode' => 'required|integer|min:1',
        ], [
            'periode.required' => 'Periode wajib diisi',
            'periode.integer' => 'Periode harus berupa angka',
            'periode.min' => 'Periode harus lebih dari 0',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => 'error',
                'message' => 'Periode harus lebih dari 0',
                'errors' => $validator->errors(),
                'timestamp' => now()->toIso8601String()
            ], 400);
        }

        $periode = $request->input('periode');
        $hasilPrediksi = $this->sesService->calculatePrediction($periode);

        return response()->json([
            'status' => 'success',
            'message' => 'Prediksi berhasil',
            'data' => [
                'periode' => $periode,
                'hasil_prediksi' => $hasilPrediksi
            ],
            'timestamp' => now()->toIso8601String()
        ]);
    }

    public function history()
    {
        $history = Cache::remember('prediction_history', 3600, function () {
            return \App\Models\Prediction::orderBy('tanggal_prediksi', 'desc')->get();
        });
        
        return response()->json([
            'status' => 'success',
            'message' => 'Riwayat prediksi berhasil diambil',
            'data' => $history,
            'timestamp' => now()->toIso8601String()
        ]);
    }
}
