package com.example.cinema.network.repository

import com.example.cinema.network.RetrofitInstance
import com.example.cinema.network.model.SeatResponse

class SeatRepository {
    suspend fun fetchSeats(): SeatResponse = RetrofitInstance.api.getSeats()
}