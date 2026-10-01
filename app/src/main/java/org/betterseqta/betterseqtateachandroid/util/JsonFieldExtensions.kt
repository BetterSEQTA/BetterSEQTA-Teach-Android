package org.betterseqta.betterseqtateachandroid.util

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

fun JsonObject.intField(vararg keys: String): Int? {
    for (key in keys) {
        val primitive = this[key]?.jsonPrimitive ?: continue
        primitive.intOrNull?.let { return it }
        primitive.content.toIntOrNull()?.let { return it }
    }
    return null
}

/** SEQTA often sends `read` / `starred` as `1`, `"true"`, or JSON boolean `true` (iOS parity). */
fun JsonObject.seqtaTruthyField(vararg keys: String): Boolean {
    for (key in keys) {
        val primitive = this[key]?.jsonPrimitive ?: continue
        if (primitive.booleanOrNull == true) return true
        if (primitive.intOrNull == 1) return true
        if (primitive.content.equals("true", ignoreCase = true)) return true
    }
    return false
}
