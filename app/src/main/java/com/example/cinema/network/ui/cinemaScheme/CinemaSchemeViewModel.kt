package com.example.cinema.network.ui.cinemaScheme

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cinema.network.model.Seat
import com.example.cinema.network.model.SeatType
import com.example.cinema.network.repository.SeatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CinemaSchemeViewModel : ViewModel() {
    private val repository = SeatRepository()
    private val _seats = MutableStateFlow<List<Seat>>(emptyList())
    val seats: StateFlow<List<Seat>> = _seats

    private val _seat_types = MutableStateFlow<List<SeatType>>(emptyList())
    val seat_types: StateFlow<List<SeatType>> = _seat_types

    val _selectedSeats = MutableLiveData<List<Seat>>(emptyList())
    val selectedSeats: LiveData<List<Seat>> = _selectedSeats

    var hall: String = ""

    init {
        viewModelScope.launch {
            try {
                val response = repository.fetchSeats()
                hall = response.hall_name
                _seats.value = response.seats
                _seat_types.value = response.seats_type
            } catch (e: Exception) {
                Log.d("network", "failed")
            }

        }
    }
}
