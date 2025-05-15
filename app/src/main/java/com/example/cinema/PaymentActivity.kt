package com.example.cinema

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginEnd
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.cinema.databinding.ActivityPaymentBinding
import com.example.cinema.model.Seat
import com.example.cinema.repository.SeatRepository.saveSeats
import com.example.cinema.ui.cinemaScheme.CinemaSchemeFragment
import com.example.cinema.ui.cinemaScheme.CinemaSchemeFragment.Companion.prices
import com.example.cinema.ui.history.HistoryFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.reflect.typeOf

class PaymentActivity: AppCompatActivity() {
    private var totalCost = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityPaymentBinding.inflate(layoutInflater)
        val seats = intent.getParcelableArrayListExtra<Seat>("seats") ?: return
        if (seats.isEmpty()) return
        for (seat in seats) {
            val seatPrice = prices[seat.seat_type] ?: 0
            totalCost += seatPrice
            val linearLayout = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginEnd = 20
                    marginStart = 20
                }

                orientation = LinearLayout.HORIZONTAL
            }
            val icon = ImageView(this).apply {
                setImageResource(getSeatDrawable(seat.seat_type))
                layoutParams = LinearLayout.LayoutParams(
                    50,
                    50
                )
            }
            val title = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    weight = 1f
                }
                gravity = Gravity.END
                text = seat.seat_type
            }
            val seatNumber = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    weight = 1f
                }
                text = seat.place
                gravity = Gravity.END
            }
            val price = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    weight = 1f
                }
                gravity = Gravity.END
                text = seatPrice.toString()
            }
            linearLayout.addView(icon)
            linearLayout.addView(title)
            linearLayout.addView(seatNumber)
            linearLayout.addView(price)
            binding.container.addView(linearLayout)
        }
        binding.totalCost.text = "$totalCost₽"
        binding.butBtn.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                saveSeats(seats)
                withContext(Dispatchers.Main) {
                    AlertDialog.Builder(this@PaymentActivity)
                        .setTitle("Успешно!")
                        .setMessage("Хотите вернуться к покупку билетов или посмотреть статус билетов")
                        .setPositiveButton("Покупка билетов") { dialog, _ ->
                            val intent = Intent(applicationContext, MainActivity::class.java)
                            startActivity(intent)
                        }
                        .setNegativeButton("Статус билетов") {dialog, _ ->
                            val intent = Intent(applicationContext, MainActivity::class.java)
                            intent.putExtra("isGoHistory", true)
                            startActivity(intent)
                        }
                        .create()
                        .show()
                }
            }

        }
        setContentView(binding.root)
    }

    fun getSeatDrawable(type: String): Int = when (type) {
        "STANDARD" -> R.drawable.seat_standard
        "VIP" -> R.drawable.seat_vip
        "COMFORT" -> R.drawable.seat_comfort
        else -> R.drawable.seat_unknown
    }
}