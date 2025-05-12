package com.example.cinema.network.model

data class SeatResponse (
    val session_date: String,
    val session_time: String,
    val hall_name: String,
    val seats: List<Seat>,
    val seats_type: List<SeatType>
)