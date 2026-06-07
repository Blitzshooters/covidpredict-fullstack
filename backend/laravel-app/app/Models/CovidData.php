<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class CovidData extends Model
{
    /** @use HasFactory<\Database\Factories\CovidDataFactory> */
    use HasFactory;

    protected $table = 'covid_data';

    protected $fillable = [
        'tanggal',
        'wilayah',
        'positif',
        'sembuh',
        'meninggal',
    ];

    protected $casts = [
        'tanggal' => 'date',
        'positif' => 'integer',
        'sembuh' => 'integer',
        'meninggal' => 'integer',
    ];
}
