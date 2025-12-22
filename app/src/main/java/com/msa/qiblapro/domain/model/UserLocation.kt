package com.msa.qiblapro.domain.model

data class UserLocation(
    val lat: Double,
    val lon: Double,
    val alt: Float,
    val accuracyM: Float
)
