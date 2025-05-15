package com.example.cinema.io.database.network

import com.example.cinema.model.SeatResponse
import retrofit2.http.GET

interface ApiService {
    @GET("seat.json")
    suspend fun getSeats(): SeatResponse
}