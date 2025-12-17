package com.msa.qiblapro.util

data class City(val name: String, val lat: Double, val lon: Double)

object IranCities {
    val cities = listOf(
        City("تهران", 35.6892, 51.3890),
        City("مشهد", 36.2605, 59.6168),
        City("اصفهان", 32.6546, 51.6680),
        City("شیراز", 29.5926, 52.5836),
        City("تبریز", 38.0962, 46.2738),
        City("قم", 34.6416, 50.8746),
        City("کرج", 35.8400, 50.9391)
    )
}
