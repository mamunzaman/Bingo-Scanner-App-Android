package com.example.mamunbingoapp.ui.importgallery

import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.mamunbingoapp.R
import com.yalantis.ucrop.UCropActivity

/**
 * Gallery crop host ([UCropActivity]). [Theme.MamunBingoApp.UCrop] + [decorFitsSystemWindows] avoid
 * status-bar overlap; no toolbar/view inset hacks. uCrop toolbar handles crop/back.
 */
class MamunUcropActivity : UCropActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate start")
        WindowCompat.setDecorFitsSystemWindows(window, true)
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate after super")
        runCatching { applyUcropStatusBarAppearance() }
            .onFailure { e -> Log.w(TAG, "statusBar appearance skipped", e) }
    }

    private fun applyUcropStatusBarAppearance() {
        @Suppress("DEPRECATION")
        window.statusBarColor = ContextCompat.getColor(this, R.color.ucrop_status_bar_green)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
    }

    companion object {
        private const val TAG = "MamunUcropActivity"
    }
}
