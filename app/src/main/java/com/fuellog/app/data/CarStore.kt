package com.fuellog.app.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class CarProfile(
    val make: String = "",
    val model: String = "",
    val year: String = "",
    val mileage: String = ""
)

/** Хранение профиля автомобиля (одна машина) в SharedPreferences. */
class CarStore(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("car_prefs", Context.MODE_PRIVATE)

    private val _profile = MutableStateFlow(load())
    val profile: StateFlow<CarProfile> = _profile.asStateFlow()

    private fun load(): CarProfile = CarProfile(
        make = prefs.getString(KEY_MAKE, "").orEmpty(),
        model = prefs.getString(KEY_MODEL, "").orEmpty(),
        year = prefs.getString(KEY_YEAR, "").orEmpty(),
        mileage = prefs.getString(KEY_MILEAGE, "").orEmpty()
    )

    fun save(profile: CarProfile) {
        prefs.edit()
            .putString(KEY_MAKE, profile.make)
            .putString(KEY_MODEL, profile.model)
            .putString(KEY_YEAR, profile.year)
            .putString(KEY_MILEAGE, profile.mileage)
            .apply()
        _profile.value = profile
    }

    private companion object {
        const val KEY_MAKE = "make"
        const val KEY_MODEL = "model"
        const val KEY_YEAR = "year"
        const val KEY_MILEAGE = "mileage"
    }
}