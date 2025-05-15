package com.example.cinema.ui.cinemaScheme

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cinema.model.Seat
import com.example.cinema.model.SeatType
import com.example.cinema.repository.SeatRepository
import com.example.cinema.repository.SeatRepository.fetchSeats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch

class CinemaSchemeViewModel : ViewModel() {
    val _seats = MutableStateFlow<List<Seat>>(emptyList())
    val seats: StateFlow<List<Seat>> = _seats

    private val _seat_types = MutableStateFlow<List<SeatType>>(emptyList())
    val seat_types: StateFlow<List<SeatType>> = _seat_types


    val hall: MutableStateFlow<String> = MutableStateFlow("")

    fun fetchData(context: Context, showDialog: Boolean) {
        viewModelScope.launch {
            try {
                val response = fetchSeats()
                hall.value = response.hall_name
                _seat_types.value = response.seats_type
                _seats.value = response.seats
            } catch (e: Exception) {
                if (showDialog) {
                    AlertDialog.Builder(context)
                        .setTitle("Ошибка!")
                        .setMessage("Нет подключение к сети!")
                        .setPositiveButton("Повторить") { dialog, _ ->
                            // Perform delete action
                            fetchData(context, true)
                        }
                        .setNegativeButton("Отменить") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .create()
                        .show()
                }
            }
        }
    }
}
