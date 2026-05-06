<?php

use App\Http\Controllers\Api\v1\CovidController;
use App\Http\Controllers\Api\v1\DashboardController;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;

Route::get('/user', function (Request $request) {
    return $request->user();
})->middleware('auth:sanctum');

// COVID API v1
Route::prefix('v1')->group(function () {
    Route::get('/covid', [CovidController::class, 'index']);
    Route::get('/covid/latest', [CovidController::class, 'latest']);
    Route::get('/covid/{id}', [CovidController::class, 'show']);

    // Dashboard & Chart
    Route::get('/dashboard', [DashboardController::class, 'index']);
    Route::get('/chart', [DashboardController::class, 'chart']);
});
