package com.example.mamunbingoapp.core

import com.example.mamunbingoapp.ui.model.RoomStatus

object RoomStatusResolver {
    fun resolve(calledCount: Int): RoomStatus =
        if (calledCount >= MAX_LIVE_CALLS) RoomStatus.FINISHED else RoomStatus.RUNNING
}
