package org.betterseqta.betterseqtateachandroid.util

import androidx.compose.ui.graphics.Color

private val teachAccentPalette = listOf(
    Color(0xFF8CC63E),
    Color(0xFF29ABE2),
    Color(0xFF662D91),
    Color(0xFFF7931E),
    Color(0xFF0071BC),
    Color(0xFF39B54A),
    Color(0xFFED1C24),
    Color(0xFF2E3192),
)

fun colorFromHex(hex: String?, fallback: Color = Color(0xFF2196F3)): Color {
    if (hex.isNullOrBlank()) return fallback
    val normalized = hex.trim().removePrefix("#")
    val argb = when (normalized.length) {
        6 -> "FF$normalized"
        8 -> normalized
        else -> return fallback
    }
    return runCatching {
        Color(argb.toLong(16))
    }.getOrDefault(fallback)
}

/** Stable accent for subject / class labels when SEQTA does not provide a colour. */
fun accentColorForSeed(seed: String, fallback: Color = Color(0xFF2196F3)): Color {
    if (seed.isBlank()) return fallback
    val index = seed.lowercase().hashCode().let { if (it == Int.MIN_VALUE) 0 else kotlin.math.abs(it) } % teachAccentPalette.size
    return teachAccentPalette[index]
}
