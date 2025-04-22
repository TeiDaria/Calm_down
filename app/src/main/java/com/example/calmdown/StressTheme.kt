package com.example.calmdown

import java.io.Serializable

enum class StressTheme(val query: String) : Serializable {
    ABSTRACT("calming abstract"),
    SKY("sky"),
    NATURE("beautiful nature"),
    DOGS("dog"),
    CATS("cats");

    companion object {
        // Метод для получения категорий в порядке убывания голосов
        fun getSortedThemes(votes: Map<StressTheme, Int>): List<StressTheme> {
            return votes.toList()
                .sortedByDescending { it.second } // Сортируем по количеству голосов
                .map { it.first } // Возвращаем только категории
        }
    }
}