package com.example.mamunbingoapp.data.bingo

/**
 * Newest 60 Sunday results: about 14 months of weekly draws, enough for
 * archived-ticket lookup without unbounded DataStore growth.
 */
object BingoCachePolicy {
    const val MAX_CACHED_DRAWS = 60

    fun mergeStatus(cached: BingoStatus?, incoming: BingoStatus): BingoStatus {
        if (cached == null) return incoming
        return BingoStatus(
            jackpotEur = incoming.jackpotEur ?: cached.jackpotEur,
            nextDrawAt = incoming.nextDrawAt ?: cached.nextDrawAt,
            latestDrawDate = incoming.latestDrawDate ?: cached.latestDrawDate,
            checkedAt = incoming.checkedAt ?: cached.checkedAt,
        )
    }

    fun upsertDraw(existing: List<BingoDrawResult>, incoming: BingoDrawResult): List<BingoDrawResult> {
        val merged = buildList {
            add(incoming)
            existing.filter { it.drawDate != incoming.drawDate }.forEach(::add)
        }
        return retainNewest(merged)
    }

    fun retainNewest(draws: List<BingoDrawResult>): List<BingoDrawResult> =
        draws
            .distinctBy { it.drawDate }
            .sortedByDescending { it.drawDate }
            .take(MAX_CACHED_DRAWS)
}
