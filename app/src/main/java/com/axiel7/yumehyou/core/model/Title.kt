package com.axiel7.yumehyou.core.model

import java.util.Locale

data class Title(
    val preferred: String,
    val romaji: String? = null,
    val english: String? = null,
    val native: String? = null,
    val alternatives: List<String> = emptyList(),
) {
    val all: List<String>
        get() = buildList {
            val seen = mutableSetOf<String>()

            appendDistinct(preferred, seen)
            listOfNotNull(romaji, english, native).forEach { appendDistinct(it, seen) }
            alternatives.forEach { appendDistinct(it, seen) }
        }

    private fun MutableList<String>.appendDistinct(
        value: String,
        seen: MutableSet<String>,
    ) {
        if (seen.add(value.lowercase(Locale.ROOT))) {
            add(value)
        }
    }
}
