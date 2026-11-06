package com.fuellog.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.fuellog.app.data.local.entity.FuelRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FuelRecordDao {

    @Query("SELECT * FROM fuel_records ORDER BY date DESC, odometer DESC")
    fun observeAll(): Flow<List<FuelRecordEntity>>

    @Query("SELECT * FROM fuel_records WHERE id = :id")
    fun observeById(id: Long): Flow<FuelRecordEntity?>

    @Query("SELECT * FROM fuel_records WHERE id = :id")
    suspend fun getById(id: Long): FuelRecordEntity?

    /**
     * The record used as the basis for consumption calculation:
     * the latest existing entry with an odometer reading below the current one.
     */
    @Query(
        "SELECT * FROM fuel_records " +
            "WHERE odometer < :odometer AND id != :excludeId " +
            "ORDER BY odometer DESC, date DESC LIMIT 1"
    )
    suspend fun getPreviousRecord(odometer: Int, excludeId: Long): FuelRecordEntity?

    @Query("SELECT COALESCE(SUM(total_price), 0) FROM fuel_records")
    fun observeTotalCost(): Flow<Double>

    @Query("SELECT MAX(odometer) FROM fuel_records")
    fun observeMaxOdometer(): Flow<Int?>

    @Insert
    suspend fun insert(record: FuelRecordEntity): Long

    @Update
    suspend fun update(record: FuelRecordEntity)

    @Delete
    suspend fun delete(record: FuelRecordEntity)
}