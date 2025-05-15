package com.example.cinema.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cinema.model.Seat
import com.example.cinema.repository.SeatRepository.getSeats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class HistoryViewModel: ViewModel() {
    val seats: MutableStateFlow<List<Seat>> = MutableStateFlow(emptyList())
    init {
        updateSeats()
    }
    fun updateSeats() {
        viewModelScope.launch {
            seats.value = getSeats()
        }
    }

}