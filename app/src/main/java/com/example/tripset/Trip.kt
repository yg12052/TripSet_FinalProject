package com.example.tripset

data class Trip(
    val id: String = "",
    val ownerUid: String = "",
    val destination: String = "",
    val startDateMillis: Long = 0L,
    val endDateMillis: Long = 0L
)

