package com.example.cinema.io.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.cinema.MainActivity.Companion.getAppContext
import com.example.cinema.model.Seat

@Database(entities = [Seat::class], version = 1)
abstract class SeatDatabase: RoomDatabase() {
    abstract fun getSeatDao(): SeatsDao

    companion object {
        private var INSTANCE: SeatDatabase? = null

        fun getDatabase(): SeatDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    getAppContext(),
                    SeatDatabase::class.java,
                    "seats_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}