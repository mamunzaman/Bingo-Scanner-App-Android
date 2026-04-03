package com.example.mamunbingoapp.engine

import com.example.mamunbingoapp.data.RoomRepository
import com.example.mamunbingoapp.core.MAX_LIVE_CALLS
import com.example.mamunbingoapp.data.RoomSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

object RoomTimerManager {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val jobs = mutableMapOf<String, Job>()

    fun startAutoCall(roomId: String) {
        stopAutoCall(roomId)
        jobs[roomId] = scope.launch {
            while (true) {
                val settings = RoomRepository.roomSettingsFlow(roomId).first() ?: break
                if (!settings.isRunning) break
                val intervalMs = (settings.intervalSeconds * 1000L).coerceAtLeast(1000L)
                delay(intervalMs)
                val called = RoomRepository.getCalledNumbersForRoom(roomId)
                if (called.size >= MAX_LIVE_CALLS) break
                val remaining = (1..75).filter { it !in called }
                if (remaining.isEmpty()) break
                val number = remaining.random()
                RoomRepository.addCalledNumberToRoom(roomId, number)
            }
            jobs.remove(roomId)
        }
    }

    fun stopAutoCall(roomId: String) {
        jobs[roomId]?.cancel()
        jobs.remove(roomId)
    }

    fun setInterval(roomId: String, seconds: Int) {
        scope.launch {
            val current = RoomRepository.roomSettingsFlow(roomId).first()
            if (current != null) {
                RoomRepository.upsertRoomSettings(current.copy(intervalSeconds = seconds.coerceIn(1, 60)))
            }
        }
    }

    fun setAutoCallRunning(roomId: String, isRunning: Boolean) {
        scope.launch {
            val current = RoomRepository.roomSettingsFlow(roomId).first()
                ?: RoomSettings(roomId, false, 5, null, false)
            RoomRepository.upsertRoomSettings(
                current.copy(
                    isRunning = isRunning,
                    lastStartedAt = if (isRunning) System.currentTimeMillis() else current.lastStartedAt
                )
            )
            if (isRunning) startAutoCall(roomId)
            else stopAutoCall(roomId)
        }
    }


    fun toggleAutoCall(roomId: String, enabled: Boolean) {
        setAutoCallRunning(roomId, enabled)
    }

    fun isRunning(roomId: String): Boolean = jobs.containsKey(roomId)
}
