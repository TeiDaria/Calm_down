package com.example.calmdown

enum class StressTheme(val level: Int, val query: String) {

    ABSTRACT(1, "abstract"),
    SKY(2, "sky"),
    NATURE(3, "nature"),
    DOGS(4, "dogs"),
    CATS(5, "cats");

    companion object {
        fun fromLevel(level: Int): StressTheme {
            return values().first { it.level == level }
        }
    }
}