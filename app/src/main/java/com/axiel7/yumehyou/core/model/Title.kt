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
            listOfNotNull(romaji, english, native)
                .filterNot { it == preferred }
                .forEach(::add)
            alternatives
                .filterNot { it == preferred || contains(it) }
                .forEach(::add)
        }
}
