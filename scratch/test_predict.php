<?php

require __DIR__ . '/../backend/laravel-app/vendor/autoload.php';
$app = require_once __DIR__ . '/../backend/laravel-app/bootstrap/app.php';

use App\Services\SESService;

$kernel = $app->make(Illuminate\Contracts\Console\Kernel::class);
$kernel->bootstrap();

$service = app(SESService::class);
echo "Memulai Prediksi untuk 7 Hari...\n";
$result = $service->calculatePrediction(7);
echo "Hasil Prediksi:\n";
echo json_encode($result, JSON_PRETTY_PRINT) . "\n";

echo "\nMengecek History Prediksi di Database:\n";
$history = \App\Models\Prediction::all()->toArray();
echo json_encode($history, JSON_PRETTY_PRINT) . "\n";
