package com.example.mamunbingoapp.domain.game

class BingoRoundManager(
    private val caller: BingoCaller = BingoCaller(),
    private val history: CalledNumbersManager = CalledNumbersManager(),
    private val controller: BingoGameController = BingoGameController()
) {

    fun startGame() {
        caller.reset()
        history.clear()
        controller.reset()
        controller.start()
    }

    fun callNextNumber(): Int? {
        if (!controller.isRunning()) return null

        val next = caller.callNext()

        if (next != null) {
            history.add(next)
            if (caller.remainingCount() == 0) {
                controller.finish()
            }
        } else {
            controller.finish()
        }

        return next
    }

    fun callSpecificNumber(number: Int): Int? {
        if (!controller.isRunning()) return null

        val called = caller.callSpecific(number)

        if (called != null) {
            history.add(called)
            if (caller.remainingCount() == 0) {
                controller.finish()
            }
        }

        return called
    }

    fun undoLastCall(): Int? {
        val last = history.removeLast()
        if (last != null) {
            caller.restore(last)
        }
        return last
    }

    fun getCalledNumbers(): List<Int> = history.getAll()

    fun getLastCalledNumber(): Int? = history.last()

    fun getRemainingCount(): Int = caller.remainingCount()

    fun getGameState(): BingoGameState = controller.getState()
}
