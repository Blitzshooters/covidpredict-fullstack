<?php

require __DIR__ . '/../backend/laravel-app/vendor/autoload.php';
$app = require_once __DIR__ . '/../backend/laravel-app/bootstrap/app.php';

use App\Services\CovidService;
use Illuminate\Support\Facades\Artisan;

$kernel = $app->make(Illuminate\Contracts\Console\Kernel::class);
$kernel->bootstrap();

$service = app(CovidService::class);
$summary = $service->getSummary('Indonesia');

echo json_encode($summary, JSON_PRETTY_PRINT);
