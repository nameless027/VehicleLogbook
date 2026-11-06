package com.fuellog.app.util

object FuelTypes {
    const val AI_92 = "АИ-92"
    const val AI_95 = "АИ-95"
    const val AI_100 = "АИ-100"
    const val DIESEL = "Дизель"
    const val GAS = "Газ (LPG/CNG)"
    const val OTHER = "Другое"

    val ALL: List<String> = listOf(AI_92, AI_95, AI_100, DIESEL, GAS, OTHER)
}