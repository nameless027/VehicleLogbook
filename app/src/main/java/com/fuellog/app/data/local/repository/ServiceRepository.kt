package com.fuellog.app.data.repository

import androidx.room.withTransaction
import com.fuellog.app.data.local.AppDatabase
import com.fuellog.app.data.local.dao.ServiceItemDao
import com.fuellog.app.data.local.dao.ServiceRecordDao
import com.fuellog.app.data.local.entity.ServiceItemEntity
import com.fuellog.app.data.local.entity.ServiceRecordEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

data class ServiceFormInput(
    val date: LocalDate,
    val odometer: Int,
    val notes: String
)

data class ServiceItemInput(
    val name: String,
    val partNumber: String,
    val quantity: Double,
    val unitPrice: Double
) {
    val totalPrice: Double get() = quantity * unitPrice
}

class ServiceRepository(
    private val database: AppDatabase,
    private val serviceRecordDao: ServiceRecordDao,
    private val serviceItemDao: ServiceItemDao
) {

    fun observeRecords(): Flow<List<ServiceRecordEntity>> = serviceRecordDao.observeAll()

    fun observeRecord(id: Long): Flow<ServiceRecordEntity?> = serviceRecordDao.observeById(id)

    fun observeItems(serviceId: Long): Flow<List<ServiceItemEntity>> =
        serviceItemDao.observeByServiceId(serviceId)

    fun observeTotalCost(): Flow<Double> = serviceRecordDao.observeTotalCost()

    fun observeMaxOdometer(): Flow<Int?> = serviceRecordDao.observeMaxOdometer()

    suspend fun getRecord(id: Long): ServiceRecordEntity? = serviceRecordDao.getById(id)

    suspend fun getItems(serviceId: Long): List<ServiceItemEntity> =
        serviceItemDao.getByServiceId(serviceId)

    /**
     * Inserts or updates a service record together with its items in a single
     * transaction. Item total = quantity × unit price; record total = Σ items.
     */
    suspend fun save(
        form: ServiceFormInput,
        items: List<ServiceItemInput>,
        existing: ServiceRecordEntity?
    ): Long = database.withTransaction {
        val totalPrice = items.sumOf { it.totalPrice }

        val recordId = if (existing != null) {
            serviceRecordDao.update(
                existing.copy(
                    date = form.date,
                    odometer = form.odometer,
                    notes = form.notes,
                    totalPrice = totalPrice
                )
            )
            existing.id
        } else {
            serviceRecordDao.insert(
                ServiceRecordEntity(
                    date = form.date,
                    odometer = form.odometer,
                    notes = form.notes,
                    totalPrice = totalPrice
                )
            )
        }

        val entities = items.map { item ->
            ServiceItemEntity(
                serviceId = recordId,
                name = item.name,
                partNumber = item.partNumber,
                quantity = item.quantity,
                unitPrice = item.unitPrice,
                totalPrice = item.totalPrice
            )
        }
        serviceItemDao.replaceForService(recordId, entities)

        recordId
    }

    /** Items are removed automatically via the CASCADE foreign key. */
    suspend fun delete(record: ServiceRecordEntity) = serviceRecordDao.delete(record)
}