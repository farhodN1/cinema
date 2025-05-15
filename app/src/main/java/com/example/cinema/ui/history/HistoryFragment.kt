package com.example.cinema.ui.history

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.marginBottom
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.cinema.R
import com.example.cinema.databinding.FragmentCinemaSchemeBinding
import com.example.cinema.databinding.FragmentHistoryBinding
import com.example.cinema.ui.cinemaScheme.CinemaSchemeFragment.Companion.prices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.w3c.dom.Text

class HistoryFragment: Fragment() {
    private lateinit var viewModel: HistoryViewModel
    private lateinit var binding: FragmentHistoryBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(requireActivity()).get(HistoryViewModel::class.java)
        binding = FragmentHistoryBinding.inflate(inflater)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            viewModel.seats.collect { seats ->
                for (seat in seats) {
                    val linearLayout = LinearLayout(requireContext()).apply {
                        orientation = LinearLayout.HORIZONTAL
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(20, 0, 20, 10)
                        }
                    }
                    val icon = ImageView(requireContext()).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            50,
                            50
                        )
                        setImageResource(getSeatDrawable(seat.seat_type))
                    }
                    val title = TextView(requireContext()).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            weight = 1f
                        }
                        gravity = Gravity.CENTER
                        text = seat.seat_type
                    }
                    val seatNumber = TextView(requireContext()).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            marginEnd = 50
                        }
                        gravity = Gravity.END
                        text = seat.place
                    }
                    val price = TextView(requireContext()).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            marginEnd = 50
                        }
                        gravity = Gravity.END
                        if (prices.isNotEmpty()) text = prices[seat.seat_type].toString()
                    }
                    val status = TextView(requireContext()).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            marginEnd = 10
                            marginStart = 10
                        }
                        gravity = Gravity.CENTER
                        setPadding(10)
                        setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

                        if (seat.booked_seats == 0) {
                            text = "Успешно"
                            setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.blue))
                        }
                        else if (seat.booked_seats < 0) {
                            text = "Отменён"
                            setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.red))
                        }
                    }
                    linearLayout.addView(icon)
                    linearLayout.addView(title)
                    linearLayout.addView(seatNumber)
                    linearLayout.addView(price)
                    linearLayout.addView(status)
                    binding.container.addView(linearLayout)
                }
            }
        }

        binding.swiperefreshlayout.setOnRefreshListener {
            viewModel.updateSeats()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.updateSeats()
    }

    fun getSeatDrawable(type: String): Int = when (type) {
        "STANDARD" -> R.drawable.seat_standard
        "VIP" -> R.drawable.seat_vip
        "COMFORT" -> R.drawable.seat_comfort
        else -> R.drawable.seat_unknown
    }
}