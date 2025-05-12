package com.example.cinema.network.model

data class Seat(
    val seat_id: String,
    val row_num: String,
    val place: String,
    val booked_seats: Int,
    val seat_type: String,
    val object_type: String,
    var selected: Boolean
)