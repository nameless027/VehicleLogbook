package com.fuellog.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.fuellog.app.data.local.entity.ServiceRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceRecordDao {

    @Query("SELECT * FROM service_records ORDER BY date DESC, odometer DESC")
    fun observeAll(): Flow<List<ServiceRecordEntity>>

    @Query("SELECT * FROM service_records WHERE id = :id")
    fun observeById(id: Long): Flow<ServiceRecordEntity?>

    @Query("SELECT * FROM service_records WHERE id = :id")
    suspend fun getById(id: Long): ServiceRecordEntity?

    @Query("SELECT COALESCE(SUM(total_price), 0) FROM service_records")
    fun observeTotalCost(): Flow<Double>

    @Query("SELECT MAX(odometer) FROM service_records")
    fun observeMaxOdometer(): Flow<Int?>

    @Insert
    suspend fun insert(record: ServiceRecordEntity): Long

    @Update
    suspend fun update(record: ServiceRecordEntity)

    @Delete
    suspend fun delete(record: ServiceRecordEntity)
}