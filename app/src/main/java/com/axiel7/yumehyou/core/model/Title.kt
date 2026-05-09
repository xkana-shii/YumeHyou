package com.axiel7.yumehyou.core.model

data class Title(
    val preferred: String,
    val romaji: String? = null,
    val english: String? = null,
    val native: String? = null,
    val alternatives: List<String> = emptyList(),
) {
    val all: List<String>
        get() = buildList {
            add(preferred)
            addDistinct(listOfNotNull(romaji, english, native))
            addDistinct(alternatives)
        }

    private fun MutableList<String>.addDistinct(values: List<String>) {
        values
            .filterNot { it == preferred || contains(it) }
            .forEach(::add)
    }
}
