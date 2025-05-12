package com.example.cinema.network.model

data class SeatType(
    val ticket_id: Int,
    val ticket_type: String,
    val name: String,
    val price: Int,
    val seat_type: String
)
