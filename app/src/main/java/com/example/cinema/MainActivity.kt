package com.example.cinema

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.example.cinema.ui.history.HistoryFragment

class MainActivity : AppCompatActivity() {

    companion object {
        @Volatile
        private var appContext: Context? = null
        fun getAppContext(): Context {
            return appContext ?: throw IllegalStateException("Application context not initialized.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContext = applicationContext
        setContentView(R.layout.activity_main)
        val isGoHistory = intent.getBooleanExtra("isGoHistory", false)
        Log.d("isGoHistory", isGoHistory.toString())
        if (isGoHistory) {
            val fragment = HistoryFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .addToBackStack(null)
                .commit()

        }
    }
}