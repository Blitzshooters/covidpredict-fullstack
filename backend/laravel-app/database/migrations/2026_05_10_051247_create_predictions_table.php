<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        Schema::create('predictions', function (Blueprint $table) {
            $table->id('id_prediksi');
            $table->date('tanggal_prediksi');
            $table->integer('periode');
            $table->integer('hasil_prediksi_positif');
            $table->integer('hasil_prediksi_sembuh');
            $table->integer('hasil_prediksi_meninggal');
            $table->timestamps();
        });
    }


    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('predictions');
    }
};
