package com.cllarity.app

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.cllarity.app.data.CllarityRepository
import com.cllarity.app.ui.screens.HomeScreen
import com.cllarity.app.ui.theme.CllarityTheme
import com.cllarity.app.ui.theme.MidnightBackground

class MainActivity : FragmentActivity() {
    private lateinit var repository: CllarityRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        repository = CllarityRepository(applicationContext)

        setContent {
            CllarityTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MidnightBackground
                ) {
                    HomeScreen(repository = repository)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::repository.isInitialized) {
            repository.checkDayRollover()
        }
    }
}
