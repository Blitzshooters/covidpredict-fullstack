<?php

namespace App\Http\Controllers;

use App\Models\CovidData;
use Illuminate\Http\Request;

class CovidController extends Controller
{
    /**
     * Get COVID-19 data list.
     *
     * @param  \Illuminate\Http\Request  $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function index(Request $request)
    {
        $query = CovidData::query();

        // Filter by wilayah if provided
        if ($request->has('wilayah')) {
            $query->where('wilayah', 'like', '%' . $request->wilayah . '%');
        }

        // Sort by latest date descending as requested
        $data = $query->orderBy('tanggal', 'desc')->get();

        return response()->json([
            'status' => 'success',
            'message' => 'Data berhasil diambil',
            'data' => $data,
            'timestamp' => now()->toIso8601String()
        ]);
    }
}
