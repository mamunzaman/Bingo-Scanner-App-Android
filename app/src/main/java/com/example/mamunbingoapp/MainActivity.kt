package com.example.mamunbingoapp

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import com.example.mamunbingoapp.data.DemoSeeder
import com.example.mamunbingoapp.data.db.DatabaseProvider
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mamunbingoapp.data.auth.AuthRepository
import com.example.mamunbingoapp.navigation.NavGraph
import com.example.mamunbingoapp.theme.MamunBingoTheme
import com.example.mamunbingoapp.ui.components.AppHeaderBackground
import com.example.mamunbingoapp.viewmodel.ThemeMode
import com.example.mamunbingoapp.viewmodel.ThemeViewModel
import com.example.mamunbingoapp.viewmodel.ImportTicketDeepLinkViewModel

class MainActivity : ComponentActivity() {

    private companion object {
        private const val TAG = "MainActivity"
    }

    private val importDeepLinkViewModel: ImportTicketDeepLinkViewModel by viewModels()

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        dispatchIncomingIntent(intent)
    }

    private fun dispatchIncomingIntent(intent: Intent?) {
        if (intent == null) {
            Log.d(TAG, "dispatchIncomingIntent: no intent")
            return
        }
        if (AuthRepository.handleAuthDeepLink(intent)) return
        importDeepLinkViewModel.setFromIntent(intent)
    }

    override fun onStart() {
        super.onStart()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT
    }

    override fun onStop() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        DatabaseProvider.init(applicationContext)
        com.example.mamunbingoapp.data.SettingsRepository.init(applicationContext)
        com.example.mamunbingoapp.data.AccountRepository.init(applicationContext)
        com.example.mamunbingoapp.data.projects.ProjectsRepository.init(applicationContext)
        AuthRepository.startup(applicationContext)
        dispatchIncomingIntent(intent)
        lifecycleScope.launch(Dispatchers.IO) { DemoSeeder.seedIfNeeded() }
        setContent {
            val themeViewModel: ThemeViewModel = viewModel()
            val themeMode by themeViewModel.themeMode.collectAsState(ThemeMode.SYSTEM)
            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            val context = LocalContext.current
            MamunBingoTheme(darkTheme = darkTheme) {
                @Suppress("DEPRECATION")
                SideEffect {
                    (context as? ComponentActivity)?.window?.let { w ->
                        w.statusBarColor = AndroidColor.TRANSPARENT
                        w.navigationBarColor = AndroidColor.TRANSPARENT
                    }
                }
                DisposableEffect(darkTheme) {
                    val win = (context as? ComponentActivity)?.window
                    if (win != null) {
                        val controller = WindowInsetsControllerCompat(win, win.decorView)
                        controller.isAppearanceLightStatusBars = !darkTheme
                        controller.isAppearanceLightNavigationBars = !darkTheme
                    }
                    onDispose { }
                }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(Modifier.fillMaxSize()) {
                        AppHeaderBackground(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(0.42f)
                        )
                        NavGraph(
                            themeViewModel = themeViewModel,
                            importDeepLinkViewModel = importDeepLinkViewModel
                        )
                    }
                }
            }
        }
    }
}
