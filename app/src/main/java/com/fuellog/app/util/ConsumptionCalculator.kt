package com.fuellog.app.util

import com.fuellog.app.data.local.entity.FuelRecordEntity

object ConsumptionCalculator {

    /**
     * Consumption = (current liters × 100) / (current odometer − previous odometer).
     *
     * Calculated only when the previous fuel entry was marked "Full tank"
     * and the distance driven since that entry is positive.
     */
    fun calculate(
        currentLiters: Double,
        currentOdometer: Int,
        previousFullTank: Boolean,
        previousOdometer: Int
    ): Double? {
        if (!previousFullTank) return null
        val distance = currentOdometer - previousOdometer
        if (distance <= 0 || currentLiters <= 0.0) return null
        return currentLiters * 100.0 / distance
    }

    /**
     * Weighted all-time average: total liters of measured segments divided by
     * total distance of those segments. The distance of each segment is
     * back-derived from the stored consumption value (liters × 100 / consumption).
     */
    fun averageConsumption(records: List<FuelRecordEntity>): Double? {
        var liters = 0.0
        var distance = 0.0
        records.forEach { record ->
            val consumption = record.averageConsumption ?: return@forEach
            if (consumption <= 0.0 || record.liters <= 0.0) return@forEach
            liters += record.liters
            distance += record.liters * 100.0 / consumption
        }
        return if (distance > 0.0) liters * 100.0 / distance else null
    }
}