package com.fuellog.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "service_items",
    foreignKeys = [
        ForeignKey(
            entity = ServiceRecordEntity::class,
            parentColumns = ["id"],
            childColumns = ["service_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["service_id"])]
)
data class ServiceItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "service_id")
    val serviceId: Long,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "part_number")
    val partNumber: String,
    @ColumnInfo(name = "quantity")
    val quantity: Double,
    @ColumnInfo(name = "unit_price")
    val unitPrice: Double,
    @ColumnInfo(name = "total_price")
    val totalPrice: Double
)