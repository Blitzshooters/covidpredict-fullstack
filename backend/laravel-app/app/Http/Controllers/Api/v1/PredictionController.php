<?php

namespace App\Http\Controllers\Api\v1;

use App\Http\Controllers\Controller;
use App\Services\SESService;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Cache;
use Illuminate\Support\Facades\Validator;

class PredictionController extends Controller
{
    protected SESService $sesService;

    public function __construct(SESService $sesService)
    {
        $this->sesService = $sesService;
    }

    public function predict(Request $request): JsonResponse
    {
        $validator = Validator::make($request->all(), [
            'wilayah' => 'required|string',
            'days' => 'required|integer|min:1|max:30',
            'alpha' => 'required|numeric|min:0.1|max:1',
        ], [
            'wilayah.required' => 'Wilayah wajib diisi',

            'days.required' => 'Jumlah hari prediksi wajib diisi',
            'days.integer' => 'Jumlah hari prediksi harus berupa angka',
            'days.min' => 'Jumlah hari prediksi minimal 1',
            'days.max' => 'Jumlah hari prediksi maksimal 30',

            'alpha.required' => 'Alpha wajib diisi',
            'alpha.numeric' => 'Alpha harus berupa angka',
            'alpha.min' => 'Alpha minimal 0.1',
            'alpha.max' => 'Alpha maksimal 1',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => 'error',
                'message' => 'Input prediksi tidak valid',
                'errors' => $validator->errors(),
                'timestamp' => now()->toIso8601String(),
            ], 400);
        }

        $wilayah = $request->input('wilayah');
        $days = (int) $request->input('days');
        $alpha = (float) $request->input('alpha');

        $result = $this->sesService->calculatePrediction(
            periode: $days,
            wilayah: $wilayah,
            alpha: $alpha
        );

        if (!$result['success']) {
            return response()->json([
                'status' => 'error',
                'message' => $result['message'],
                'errors' => null,
                'timestamp' => now()->toIso8601String(),
            ], 422);
        }

        return response()->json([
            'status' => 'success',
            'message' => 'Prediksi berhasil',
            'data' => $result['data'],
            'timestamp' => now()->toIso8601String(),
        ]);
    }

    public function history(): JsonResponse
    {
        $history = Cache::remember('prediction_history', 3600, function () {
            return \App\Models\Prediction::orderBy('tanggal_prediksi', 'desc')->get();
        });

        return response()->json([
            'status' => 'success',
            'message' => 'Riwayat prediksi berhasil diambil',
            'data' => $history,
            'timestamp' => now()->toIso8601String(),
        ]);
    }
}