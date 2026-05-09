package com.axiel7.yumehyou.core.model

data class Score(
    val value: Double,
    val maxValue: Double = 100.0,
    val format: Format = Format.POINT_100,
) {
    init {
        require(value.isFinite()) {
            "value must be finite"
        }
        require(maxValue.isFinite() && maxValue > 0.0) {
            "maxValue must be finite and greater than 0"
        }
        require(value in 0.0..maxValue) {
            "value must be between 0 and maxValue"
        }
    }

    val normalized: Double
        get() = value / maxValue

    enum class Format {
        POINT_100,
        POINT_10,
        POINT_10_DECIMAL,
        POINT_5,
        POINT_3,
    }
}
