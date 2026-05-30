package com.example.mamunbingoapp.viewmodel

import androidx.annotation.StringRes
import com.example.mamunbingoapp.R

enum class TicketFilter(@StringRes val labelResId: Int) {
    All(R.string.history_filter_all),
    Today(R.string.common_today),
    ThisWeek(R.string.my_tickets_filter_week),
}
