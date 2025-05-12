package com.example.cinema.network

import com.example.cinema.network.model.SeatResponse
import retrofit2.http.GET

interface ApiService {
    @GET("seat.json")
    suspend fun getSeats(): SeatResponse
}