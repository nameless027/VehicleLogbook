package com.fuellog.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.fuellog.app.data.local.entity.ServiceItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceItemDao {

    @Query("SELECT * FROM service_items WHERE service_id = :serviceId ORDER BY id ASC")
    fun observeByServiceId(serviceId: Long): Flow<List<ServiceItemEntity>>

    @Query("SELECT * FROM service_items WHERE service_id = :serviceId ORDER BY id ASC")
    suspend fun getByServiceId(serviceId: Long): List<ServiceItemEntity>

    @Insert
    suspend fun insertAll(items: List<ServiceItemEntity>)

    @Query("DELETE FROM service_items WHERE service_id = :serviceId")
    suspend fun deleteByServiceId(serviceId: Long)

    @Transaction
    suspend fun replaceForService(serviceId: Long, items: List<ServiceItemEntity>) {
        deleteByServiceId(serviceId)
        if (items.isNotEmpty()) {
            insertAll(items)
        }
    }
}