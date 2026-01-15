package com.example.snorly.feature.challenges.memory

import kotlin.random.Random

private const val DEFAULT_PAIRS = 6

fun defaultEmojiSet(): List<String> = listOf(
    "⭐", "❤️", "⚡", "✨", "🔥", "🌙",
    "🍀", "🍎", "🎯", "🎵", "👾", "🧠",
    "🐱", "🐶", "🦊", "🐼", "🐸", "🦄"
)

fun createNewCards(
    pairCount: Int = DEFAULT_PAIRS,
    contents: List<String> = defaultEmojiSet()
): List<MemoryCard> {
    val chosen = contents.shuffled(Random(System.currentTimeMillis())).take(pairCount)
    val doubled = (chosen + chosen).shuffled(Random(System.currentTimeMillis()))
    return doubled.mapIndexed { idx, c -> MemoryCard(id = idx, content = c) }
}

fun revealAll(cards: List<MemoryCard>): List<MemoryCard> =
    cards.map { it.copy(isFaceUp = true) }

fun hideAllUnmatched(cards: List<MemoryCard>): List<MemoryCard> =
    cards.map { c -> if (c.isMatched) c else c.copy(isFaceUp = false) }

fun onCardTapped(
    cards: List<MemoryCard>,
    totalPairs: Int,
    pairsFound: Int,
    moves: Int,
    isMemorizePhase: Boolean,
    inputLocked: Boolean,
    cardId: Int
): FlipResult {
    if (isMemorizePhase || inputLocked) return FlipResult.Ignored

    val tapped = cards.getOrNull(cardId) ?: return FlipResult.Ignored
    if (tapped.isMatched || tapped.isFaceUp) return FlipResult.Ignored

    val faceUpUnmatched = cards.filter { it.isFaceUp && !it.isMatched }
    if (faceUpUnmatched.size >= 2) return FlipResult.Ignored

    var newCards = cards.map { if (it.id == cardId) it.copy(isFaceUp = true) else it }

    val nowUp = newCards.filter { it.isFaceUp && !it.isMatched }
    var newMoves = moves
    var newPairs = pairsFound
    var message = "Go!"
    var mismatchFlipBack = false

    if (nowUp.size == 2) {
        newMoves += 1
        val (a, b) = nowUp
        if (a.content == b.content) {
            newCards = newCards.map {
                if (it.id == a.id || it.id == b.id) it.copy(isMatched = true) else it
            }
            newPairs += 1
            message = if (newPairs == totalPairs) "Nice! All pairs found 🎉" else "Match!"
        } else {
            message = "Try again!"
            mismatchFlipBack = true
        }
    }

    return FlipResult.Updated(
        cards = newCards,
        pairsFound = newPairs,
        moves = newMoves,
        message = message,
        needsMismatchFlipBack = mismatchFlipBack
    )
}

fun flipBackMismatched(cards: List<MemoryCard>): List<MemoryCard> {
    val up = cards.filter { it.isFaceUp && !it.isMatched }
    if (up.size != 2) return cards
    val (a, b) = up
    return cards.map { c ->
        if (c.id == a.id || c.id == b.id) c.copy(isFaceUp = false) else c
    }
}