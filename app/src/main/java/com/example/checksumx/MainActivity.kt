package com.example.checksumx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.checksumx.data.preferences.PreferencesRepository
import com.example.checksumx.ui.navigation.ChecksumXNavGraph
import com.example.checksumx.ui.theme.ChecksumXTheme
import com.example.checksumx.ui.viewmodel.HomeViewModel
import com.example.checksumx.ui.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {

    private val preferencesRepository by lazy {
        PreferencesRepository(applicationContext)
    }

    private val homeViewModel: HomeViewModel by viewModels {
        HomeViewModel.Factory(preferencesRepository)
    }

    private val settingsViewModel: SettingsViewModel by viewModels {
        SettingsViewModel.Factory(preferencesRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val userPrefs by settingsViewModel.preferences.collectAsState()

            ChecksumXTheme(appTheme = userPrefs.theme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    ChecksumXNavGraph(
                        navController = navController,
                        homeViewModel = homeViewModel,
                        settingsViewModel = settingsViewModel
                    )
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        homeViewModel.onAppLeaving()
    }
}
