package com.example.cinema.io.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.cinema.model.Seat
import kotlinx.coroutines.flow.StateFlow

@Dao
interface SeatsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSeats(seats: List<Seat>)

    @Query("SELECT * from seats")
    suspend fun getSeats(): List<Seat>

    @Update
    suspend fun unBook(seats: Seat)
}