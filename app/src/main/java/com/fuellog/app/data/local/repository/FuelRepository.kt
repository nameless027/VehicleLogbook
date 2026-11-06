package com.fuellog.app.data.repository

import com.fuellog.app.data.local.dao.FuelRecordDao
import com.fuellog.app.data.local.entity.FuelRecordEntity
import com.fuellog.app.util.ConsumptionCalculator
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

data class FuelFormInput(
    val date: LocalDate,
    val odometer: Int,
    val fuelType: String,
    val liters: Double,
    val pricePerLiter: Double,
    val fullTank: Boolean
)

class FuelRepository(private val fuelRecordDao: FuelRecordDao) {

    fun observeRecords(): Flow<List<FuelRecordEntity>> = fuelRecordDao.observeAll()

    fun observeRecord(id: Long): Flow<FuelRecordEntity?> = fuelRecordDao.observeById(id)

    fun observeTotalCost(): Flow<Double> = fuelRecordDao.observeTotalCost()

    fun observeMaxOdometer(): Flow<Int?> = fuelRecordDao.observeMaxOdometer()

    suspend fun getRecord(id: Long): FuelRecordEntity? = fuelRecordDao.getById(id)

    /**
     * Inserts or updates a fuel record.
     *
     * Total cost = liters × price per liter.
     * Consumption is calculated when the previous entry was a "Full tank" fill:
     * consumption = liters × 100 / (current odometer − previous odometer).
     */
    suspend fun save(input: FuelFormInput, existing: FuelRecordEntity?): Long {
        val totalPrice = input.liters * input.pricePerLiter
        val previous = fuelRecordDao.getPreviousRecord(input.odometer, existing?.id ?: -1L)

        val consumption = previous?.let {
            ConsumptionCalculator.calculate(
                currentLiters = input.liters,
                currentOdometer = input.odometer,
                previousFullTank = it.fullTank,
                previousOdometer = it.odometer
            )
        }

        val entity = FuelRecordEntity(
            id = existing?.id ?: 0L,
            date = input.date,
            odometer = input.odometer,
            fuelType = input.fuelType,
            liters = input.liters,
            pricePerLiter = input.pricePerLiter,
            totalPrice = totalPrice,
            fullTank = input.fullTank,
            averageConsumption = consumption,
            createdAt = existing?.createdAt ?: System.currentTimeMillis()
        )

        return if (existing == null) {
            fuelRecordDao.insert(entity)
        } else {
            fuelRecordDao.update(entity)
            entity.id
        }
    }

    suspend fun delete(record: FuelRecordEntity) = fuelRecordDao.delete(record)
}