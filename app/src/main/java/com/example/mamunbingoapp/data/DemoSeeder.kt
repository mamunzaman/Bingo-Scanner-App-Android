package com.example.mamunbingoapp.data

import com.example.mamunbingoapp.BuildConfig

object DemoSeeder {

    private var seeded = false

    suspend fun seedIfNeeded() {
        if (!BuildConfig.DEMO_MODE) return
        if (!seeded) {
            seeded = true
            HistoryRepository.seedDemoData()
            RoomRepository.seedDemoData()
        }
        TicketRepository.seedDemoTickets()
    }
}
