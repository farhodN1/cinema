package com.example.cinema.model
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "seats")
data class Seat(
    @PrimaryKey val seat_id: String,
    val row_num: String,
    val place: String,
    var booked_seats: Int,
    val seat_type: String,
    val object_type: String,
    var selected: Boolean
) : Parcelable