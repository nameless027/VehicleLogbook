package com.fuellog.app.data.repository

import android.content.SharedPreferences
import com.fuellog.app.data.local.dao.VehicleDao
import com.fuellog.app.data.local.entity.VehicleEntity
import kotlinx.coroutines.flow.Flow

class VehicleRepository(
    private val vehicleDao: VehicleDao,
    private val prefs: SharedPreferences
) {

    fun observeVehicle(): Flow<VehicleEntity?> = vehicleDao.observe()

    suspend fun getVehicle(): VehicleEntity? = vehicleDao.get()

    /** Показывалось ли уже окно первичного заполнения (сохранить/пропустить). */
    val isSetupDone: Boolean
        get() = prefs.getBoolean(KEY_SETUP_DONE, false)

    fun markSetupDone() {
        prefs.edit().putBoolean(KEY_SETUP_DONE, true).apply()
    }

    suspend fun save(make: String, model: String, year: Int?, odometer: Int) {
        vehicleDao.upsert(
            VehicleEntity(
                id = 1L,
                make = make.trim(),
                model = model.trim(),
                year = year,
                odometer = odometer,
                updatedAt = System.currentTimeMillis()
            )
        )
        markSetupDone()
    }

    companion object {
        private const val KEY_SETUP_DONE = "vehicle_setup_done"
    }
}