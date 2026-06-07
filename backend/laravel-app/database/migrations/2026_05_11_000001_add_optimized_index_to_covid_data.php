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
        Schema::table('covid_data', function (Blueprint $table) {
            // Index optimized for queries filtering by wilayah and sorting by tanggal
            $table->index(['wilayah', 'tanggal'], 'covid_data_wilayah_tanggal_index');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::table('covid_data', function (Blueprint $table) {
            $table->dropIndex('covid_data_wilayah_tanggal_index');
        });
    }
};
