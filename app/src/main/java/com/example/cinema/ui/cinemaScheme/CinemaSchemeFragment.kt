package com.example.cinema.ui.cinemaScheme

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.cinema.R
import com.example.cinema.databinding.FragmentCinemaSchemeBinding
import com.example.cinema.model.Seat
import com.example.cinema.model.SeatType
import com.example.cinema.PaymentActivity
import com.example.cinema.io.database.SeatDatabase.Companion.getDatabase
import com.example.cinema.repository.SeatRepository.fetchSeats
import com.example.cinema.repository.SeatRepository.getSeats
import com.example.cinema.repository.SeatRepository.saveSeats
import com.example.cinema.repository.SeatRepository.unBook
import com.otaliastudios.zoom.ZoomEngine
import com.otaliastudios.zoom.ZoomLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import java.lang.Integer.parseInt
import kotlin.coroutines.coroutineContext

class CinemaSchemeFragment: Fragment() {
    private lateinit var viewModel: CinemaSchemeViewModel
    private lateinit var binding: FragmentCinemaSchemeBinding
    private var freeSeats = 0
    private lateinit var seatsWrapper: LinearLayout
    private lateinit var horizontalWrapper: LinearLayout
    private var isNotFirstAttempt = false
    val seatQuantityViews = mutableMapOf<String, TextView>()
    private var standard = 0
    private var vip = 0
    private var comfort = 0
    private var totalCost = 0
    private var bookedSeats: List<Seat> = emptyList()

    companion object {
        val prices = mutableMapOf<String, Int>()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(CinemaSchemeViewModel::class.java)
        viewModel.fetchData(requireContext(), true)
        binding = FragmentCinemaSchemeBinding.inflate(inflater, container, false)
        lifecycleScope.launch(Dispatchers.IO) {
            getBookedSeats()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.zoomLayout.isVerticalScrollBarEnabled = true
        binding.zoomLayout.isHorizontalScrollBarEnabled = true

        binding.zoomLayout.setHasClickableChildren(true)
        binding.goToHistory.setOnClickListener {
            findNavController().navigate(R.id.fragment_history)
        }

        lifecycleScope.launch {
            viewModel.hall.collect { value ->
                binding.hallName.text = value
            }
        }

        lifecycleScope.launch {
            viewModel.seat_types.collect { seats ->
                binding.seatTypesAndPrices.removeAllViews()
                for (seat in seats) {
                    prices[seat.seat_type] = seat.price
                    val frameLayout = FrameLayout(requireContext()).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            gravity = Gravity.CENTER
                        }

                    }
                    val linlayout = LinearLayout(requireContext()).apply {
                        orientation = LinearLayout.VERTICAL
                        val params = LayoutParams(0, LayoutParams.WRAP_CONTENT)
                        params.weight = 1f
                        params.marginEnd = 10
                        layoutParams = params
                    }
                    val icon = ImageView(requireContext()).apply {
                        setImageResource(getSeatDrawable(seat.seat_type))
                        layoutParams = LayoutParams(
                            80,
                            80
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
                    val quantity = TextView(requireContext()).apply {
                        visibility = View.VISIBLE
                        gravity = Gravity.CENTER
                        maxHeight = 15
                        maxWidth = 15

                        layoutParams = FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.WRAP_CONTENT,
                            FrameLayout.LayoutParams.WRAP_CONTENT,
                            Gravity.END
                        ).apply{
                            maxWidth = 15
                            maxHeight = 15
                            gravity = Gravity.TOP or Gravity.END
                        }
                        background = ContextCompat.getDrawable(requireContext(), R.drawable.border_radius)
                    }
                    seatQuantityViews[seat.seat_type] = quantity
                    frameLayout.addView(icon)
                    frameLayout.addView(quantity)
                    linlayout.addView(frameLayout)
                    linlayout.addView(type)
                    linlayout.addView(price)
                    binding.seatTypesAndPrices.addView(linlayout)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.seats.collect { seats ->
                if (prices.isEmpty()) return@collect
                if (seats.isNotEmpty()) {
                    binding.progressBar.visibility = View.GONE
                    isNotFirstAttempt = true
                } else {
                    lifecycleScope.launch(Dispatchers.IO) {
                        repeat(5) {
                            viewModel.fetchData(requireContext(), false)
                            delay(5000)
                        }
                    }
                    binding.progressBar.apply {
                        postDelayed({
                            visibility = View.GONE
                        }, 30000)
                    }
                }
                binding.gridSeats.removeAllViews()
                var rowChange = 0
                freeSeats = 0
                standard = 0
                vip = 0
                comfort = 0
                totalCost = 0
                for (seat in seats) {
                    if (seat.selected) {
                        totalCost += prices[seat.seat_type]!!
                        if (seat.seat_type == "STANDARD") {
                            standard += 1
                        } else if (seat.seat_type == "VIP") {
                            vip += 1
                        } else if (seat.seat_type == "COMFORT") {
                            comfort += 1
                        }
                    }
                    if (seat.object_type == "seat") {
                        val isBooked = bookedSeats.any { it.seat_id == seat.seat_id }
                        if (seat.booked_seats == 0) freeSeats += 1
                        val seatView = ImageView(requireContext()).apply {
                            if (isBooked) setImageResource(R.drawable.seat_unknown)
                            else setImageResource(getSeatDrawable(seat.seat_type))
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
                            gravity = Gravity.CENTER
                            maxHeight = 15
                            maxWidth = 15

                            layoutParams = FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.WRAP_CONTENT,
                                FrameLayout.LayoutParams.WRAP_CONTENT,
                                Gravity.END
                            ).apply{
                                gravity = Gravity.TOP or Gravity.END
                            }
                            background = ContextCompat.getDrawable(requireContext(), R.drawable.border_radius)
                            if (seat.selected) visibility = View.VISIBLE
                            else visibility = View.GONE
                        }
                        val frameLayout = FrameLayout(requireContext()).apply {
                            setOnClickListener {
                                if (isBooked) {
                                    AlertDialog.Builder(requireContext())
                                        .setTitle("Это место уже выбрано.")
                                        .setMessage("Вы уверены, что хотите отменить выбор?")
                                        .setPositiveButton("Да") { dialog, _ ->
                                            lifecycleScope.launch(Dispatchers.IO) {
                                                seat.booked_seats = -1
                                                unBook(seat)
                                                getBookedSeats()
                                                viewModel.fetchData(requireContext(), false)
                                            }
                                        }
                                        .setNegativeButton("Не") { dialog, _ ->
                                            dialog.dismiss()
                                        }
                                        .create()
                                        .show()
                                } else {
                                    toggleChair(seat, seatView, seatNumber)
                                }
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
                    if (totalCost > 0) {
                        binding.costAndBtn.visibility = View.VISIBLE
                    }

                    if (standard > 0) {
                        seatQuantityViews["STANDARD"]?.visibility = View.VISIBLE
                        seatQuantityViews["STANDARD"]?.text = standard.toString()
                    } else {
                        seatQuantityViews["STANDARD"]?.visibility = View.GONE
                    }

                    if (vip > 0) {
                        seatQuantityViews["VIP"]?.visibility = View.VISIBLE
                        seatQuantityViews["VIP"]?.text = vip.toString()
                    } else {
                        seatQuantityViews["VIP"]?.visibility = View.GONE
                    }

                    if (comfort > 0) {
                        seatQuantityViews["COMFORT"]?.visibility = View.VISIBLE
                        seatQuantityViews["COMFORT"]?.text = comfort.toString()
                    } else {
                        seatQuantityViews["COMFORT"]?.visibility = View.GONE
                    }
                    binding.totalCost.text = "${totalCost}₽"
                }
                binding.freeSeats.text = "Свободные места: ${freeSeats}"
            }
        }

        zoomOut(binding.zoomLayout)

        binding.swiperefreshlayout.setOnRefreshListener {
            viewModel.fetchData(requireContext(), true)
            binding.swiperefreshlayout.isRefreshing = false
        }

        binding.payBtn.setOnClickListener {
            val seats = viewModel._seats.value.filter { it.selected }
            val intent = Intent(requireContext(), PaymentActivity::class.java)
            intent.putParcelableArrayListExtra("seats", ArrayList(seats))
            startActivity(intent)
        }
    }

    fun getSeatDrawable(type: String): Int = when (type) {
        "STANDARD" -> R.drawable.seat_standard
        "VIP" -> R.drawable.seat_vip
        "COMFORT" -> R.drawable.seat_comfort
        else -> R.drawable.seat_unknown
    }

    fun toggleChair(seat: Seat, seatView: ImageView, seatNumber: TextView) {
        lifecycleScope.launch {
            val updatedList = viewModel._seats.value.map {
                if (it.seat_id == seat.seat_id) it.copy(selected = !it.selected) else it
            }
            viewModel._seats.value = updatedList
        }
    }

    suspend fun getBookedSeats() {
        bookedSeats = getSeats().filter {it.booked_seats >= 0}
    }

    fun zoomOut(view: ZoomLayout) {
        var count = 0
        view.postDelayed( {
            count += 1
            val childCount = binding.gridSeats.childCount
            if (childCount < 1 && count < 200) zoomOut(view)
            else {
                view.zoomTo(1f,true)
            }
        }, 200)
    }
}