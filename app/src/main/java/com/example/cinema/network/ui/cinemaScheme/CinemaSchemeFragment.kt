package com.example.cinema.network.ui.cinemaScheme

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.cinema.R
import com.example.cinema.databinding.FragmentCinemaSchemeBinding
import com.example.cinema.network.model.Seat
import com.example.cinema.network.model.SeatType
import com.example.cinema.PaymentActivity
import kotlinx.coroutines.launch
import java.lang.Integer.parseInt

class CinemaSchemeFragment: Fragment() {
    private lateinit var viewModel: CinemaSchemeViewModel
    private lateinit var binding: FragmentCinemaSchemeBinding
    private lateinit var seatsInfo: List<SeatType>
    private var freeSeats = 0
    private lateinit var seatsWrapper: LinearLayout
    private lateinit var horizontalWrapper: LinearLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(CinemaSchemeViewModel::class.java)
        binding = FragmentCinemaSchemeBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.zoomLayout.isVerticalScrollBarEnabled = true
        binding.zoomLayout.isHorizontalScrollBarEnabled = true
        binding.zoomLayout.setHasClickableChildren(true)

        lifecycleScope.launch {
            viewModel.seats.collect { seats ->
                binding.gridSeats.removeView(binding.gridSeats)
                var rowChange = 0
                for (seat in seats) {
                    if (seat.object_type == "seat") {
                        if (seat.booked_seats == 0) freeSeats += 1
                        val seatView = ImageView(requireContext()).apply {
                            when (seat.seat_type) {
                                "STANDARD" -> setImageResource(R.drawable.seat_standard)
                                "VIP" -> setImageResource(R.drawable.seat_vip)
                                "COMFORT" -> setImageResource(R.drawable.seat_comfort)
                            }
                            val sizeInDp = 35
                            val scale = resources.displayMetrics.density
                            val sizeInPx = (sizeInDp * scale).toInt()
                            layoutParams = GridLayout.LayoutParams().apply {
                                width = sizeInPx
                                height = sizeInPx
                                rowSpec = GridLayout.spec(seat.row_num.toInt())
                                columnSpec = GridLayout.spec(seat.place.toInt())
                                setMargins(8, 8, 8, 8)
                                setGravity(Gravity.FILL_HORIZONTAL)
                            }
                        }
                        val seatNumber = TextView(requireContext()).apply {
                            text = seat.place
//                            scaleY = -1f
                            gravity = Gravity.TOP
                            visibility = View.GONE
                        }
                        val frameLayout = FrameLayout(requireContext()).apply {
                            setOnClickListener {
                                toggleChair(seat, seatView, seatNumber)
                            }
                        }
                        if (rowChange != parseInt(seat.row_num)) {
                            rowChange = parseInt(seat.row_num)
                            seatsWrapper = LinearLayout(requireContext()).apply {
                                layoutParams = LinearLayout.LayoutParams(
                                    0,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                ).apply {
                                    weight = 1f
                                }
                                orientation = LinearLayout.HORIZONTAL
                                gravity = Gravity.CENTER
                            }
                            horizontalWrapper = LinearLayout(requireContext()).apply {
                                orientation = LinearLayout.HORIZONTAL
                                gravity = Gravity.CENTER_VERTICAL
                            }
                            val rowNumber = TextView(requireContext()).apply {
                                text = seat.row_num
                                setPadding(0, 0, 20, 0)
                            }
                            horizontalWrapper.addView(rowNumber)
                            horizontalWrapper.addView(seatsWrapper)
                            binding.gridSeats.addView(horizontalWrapper, 0)
                        }
                        frameLayout.addView(seatView)
                        frameLayout.addView(seatNumber)
                        seatsWrapper.addView(frameLayout)
                    }
                }
                binding.freeSeats.text = "Свободные места: ${freeSeats}"
            }
            binding.hallName.text = viewModel.hall
        }

        lifecycleScope.launch {
            viewModel.seat_types.collect { seats ->
                seatsInfo = seats
                for (seat in seats) {
                    val linlayout = LinearLayout(requireContext()).apply {
                        orientation = LinearLayout.VERTICAL
                        val params = LayoutParams(0, LayoutParams.WRAP_CONTENT)
                        params.weight = 1f
                        params.marginEnd = 10
                        layoutParams = params
                    }
                    val icon = ImageView(requireContext()).apply {
                        when (seat.seat_type) {
                            "STANDARD" -> setImageResource(R.drawable.seat_standard)
                            "VIP" -> setImageResource(R.drawable.seat_vip)
                            "COMFORT" -> setImageResource(R.drawable.seat_comfort)
                        }
                        layoutParams = LayoutParams(
                            50,
                            50
                        ).apply {
                            gravity = Gravity.CENTER
                        }
                    }
                    val type = TextView(requireContext()).apply {
                        text = seat.seat_type
                        gravity = Gravity.CENTER
                        textSize = 16f
                    }
                    val price = TextView(requireContext()).apply {
                        text = "${seat.price}₽"
                        gravity = Gravity.CENTER
                    }
                    linlayout.addView(icon)
                    linlayout.addView(type)
                    linlayout.addView(price)
                    binding.seatTypesAndPrices.addView(linlayout)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.selectedSeats.observe(viewLifecycleOwner) { seats ->
                var totalCost = 0
                for (seat in seats) {
                    when (seat.seat_type) {
                        "STANDARD" -> totalCost += seatsInfo.find {it.seat_type == "STANDARD"}?.price!!
                        "VIP" -> totalCost += seatsInfo.find {it.seat_type == "VIP"}?.price!!
                        "COMFORT" -> totalCost += seatsInfo.find {it.seat_type == "COMFORT"}?.price!!
                    }
                }
                binding.totalCost.text = "${totalCost}₽"
            }
        }

        binding.zoomLayout.postDelayed( {
            binding.zoomLayout.zoomTo(1f,true)
        }, 1000)

        binding.payBtn.setOnClickListener {
            val intent = Intent(requireActivity(), PaymentActivity::class.java)
            startActivity(intent)
        }
    }

    fun toggleChair(seat: Seat, seatView: ImageView, seatNumber: TextView) {
        seat.selected = !seat.selected
        if (seat.selected) {
            val update = viewModel._selectedSeats.value?.toMutableList()?.apply {
                add(seat)
            }
            viewModel._selectedSeats.value = update
            seatNumber.visibility = View.VISIBLE
        } else {
            val update = viewModel._selectedSeats.value?.toMutableList()?.apply {
                remove(seat)
            }
            viewModel._selectedSeats.value = update
            seatNumber.visibility = View.GONE
        }
    }
}