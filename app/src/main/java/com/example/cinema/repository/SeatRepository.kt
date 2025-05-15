package com.example.cinema.repository

import android.util.Log
import com.example.cinema.io.database.SeatDatabase.Companion.getDatabase
import com.example.cinema.io.database.network.RetrofitInstance
import com.example.cinema.model.Seat
import com.example.cinema.model.SeatResponse

object SeatRepository {
    suspend fun fetchSeats(): SeatResponse = RetrofitInstance.api.getSeats()

    suspend fun getSeats(): List<Seat> {
        return getDatabase().getSeatDao().getSeats()
    }

    suspend fun saveSeats(seats: List<Seat>) {
        getDatabase().getSeatDao().saveSeats(seats)
    }

    suspend fun unBook(seat: Seat) {
        getDatabase().getSeatDao().unBook(seat)
        Log.d("LOG", "unbooked")
    }
}