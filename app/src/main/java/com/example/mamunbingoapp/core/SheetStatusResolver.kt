package com.example.mamunbingoapp.core

import com.example.mamunbingoapp.ui.model.SheetStatus

object SheetStatusResolver {
    fun resolve(
        assignedRoomId: String?,
        calledCount: Int,
        max: Int = MAX_CALLED_NUMBERS
    ): SheetStatus {
        return if (assignedRoomId != null) {
            if (calledCount >= max) SheetStatus.COMPLETED else SheetStatus.ACTIVE
        } else {
            when {
                calledCount == 0 -> SheetStatus.IDLE
                calledCount >= max -> SheetStatus.COMPLETED
                else -> SheetStatus.IN_PROGRESS
            }
        }
    }
}
