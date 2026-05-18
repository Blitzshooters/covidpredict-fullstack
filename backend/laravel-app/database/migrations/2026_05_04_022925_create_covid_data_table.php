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
        Schema::create('covid_data', function (Blueprint $table) {
            $table->id();
            $table->date('tanggal');
            $table->string('wilayah');
            $table->integer('positif');
            $table->integer('sembuh');
            $table->integer('meninggal');
            $table->timestamps();

            $table->index(['tanggal', 'wilayah']);
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('covid_data');
    }
};
