<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class Prediction extends Model
{
    protected $table = 'predictions';
    protected $primaryKey = 'id_prediksi';

    protected $fillable = [
        'tanggal_prediksi',
        'periode',
        'hasil_prediksi_positif',
        'hasil_prediksi_sembuh',
        'hasil_prediksi_meninggal',
    ];

    protected $casts = [
        'tanggal_prediksi' => 'date',
        'periode' => 'integer',
        'hasil_prediksi_positif' => 'integer',
        'hasil_prediksi_sembuh' => 'integer',
        'hasil_prediksi_meninggal' => 'integer',
    ];
}
