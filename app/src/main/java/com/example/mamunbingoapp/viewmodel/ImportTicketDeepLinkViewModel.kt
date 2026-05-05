package com.example.mamunbingoapp.viewmodel

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.mamunbingoapp.domain.qr.QrTicketCodec
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ImportTicketDeepLinkViewModel : ViewModel() {
    private val _pending = MutableStateFlow<Uri?>(null)
    val pendingImportTicket: StateFlow<Uri?> = _pending.asStateFlow()

    fun setFromIntent(intent: Intent?) {
        if (intent?.action != Intent.ACTION_VIEW) return
        val data = intent.data ?: return
        if (QrTicketCodec.isImportTicketDeepLinkUri(data)) {
            _pending.value = data
        }
    }

    fun consume() {
        _pending.value = null
    }
}
