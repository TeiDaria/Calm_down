// в пакете viewmodel
package com.example.calmdown.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calmdown.R
import com.example.calmdown.model.data.Question
import com.example.calmdown.model.enums.StressTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class QuestionnaireViewModel : ViewModel() {

    val questions = listOf(
        Question(
            text = "Какое фото вызывает у вас наибольшее чувство спокойствия и умиротворения?",
            imageResIds = listOf(
                R.drawable.cat_with_flowers,
                R.drawable.abstract1,
                R.drawable.sky_and_water
            )
        ),
        Question(
            text = "Какое фото вызывает у вас наибольшее чувство спокойствия и умиротворения?",
            imageResIds = listOf(
                R.drawable.dog_in_flowers2,
                R.drawable.flowers_and_mountains,
                R.drawable.silly_cat2
            )
        ),
        Question(
            text = "Какое фото вызывает у вас наибольшее чувство спокойствия и умиротворения?",
            imageResIds = listOf(R.drawable.abstract2, R.drawable.puppy, R.drawable.forest)
        ),
        Question(
            text = "Какое фото вызывает у вас наибольшее чувство спокойствия и умиротворения?",
            imageResIds = listOf(R.drawable.tiger2, R.drawable.clouds, R.drawable.doggy)
        ),
        Question(
            text = "Какое фото вызывает у вас наибольшее чувство спокойствия и умиротворения?",
            imageResIds = listOf(R.drawable.cloudy, R.drawable.kitty, R.drawable.abstract3)
        )
    )

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex

    private val _selectedImageIndices = MutableStateFlow<List<Int>>(List(questions.size) { -1 })
    val selectedImageIndices: StateFlow<List<Int>> = _selectedImageIndices

    // Можно добавить состояние для видимости кнопок, если нужно

    fun selectImage(questionIndex: Int, imageIndex: Int) {
        val newSelections = _selectedImageIndices.value.toMutableList()
        newSelections[questionIndex] = imageIndex
        _selectedImageIndices.value = newSelections
    }

    fun goToNextQuestion() {
        if (_currentQuestionIndex.value < questions.size - 1) {
            _currentQuestionIndex.value += 1
        }
    }

    fun goToPreviousQuestion() {
        if (_currentQuestionIndex.value > 0) {
            _currentQuestionIndex.value -= 1
        }
    }

    fun calculateStressTheme(): List<StressTheme> {
        val categoryVotes = mutableMapOf<StressTheme, Int>().apply {
            StressTheme.values().forEach { put(it, 0) }
        }

        val questionsList = questions
        val selectedIndices = _selectedImageIndices.value

        // Маппинг изображений к категориям по вопросам (тот же, что у вас)
        val imageToCategoryMapping = listOf(
            listOf(StressTheme.CATS, StressTheme.ABSTRACT, StressTheme.SKY),
            listOf(StressTheme.DOGS, StressTheme.NATURE, StressTheme.CATS),
            listOf(StressTheme.ABSTRACT, StressTheme.DOGS, StressTheme.NATURE),
            listOf(StressTheme.NATURE, StressTheme.SKY, StressTheme.DOGS),
            listOf(StressTheme.SKY, StressTheme.CATS, StressTheme.ABSTRACT)
        )

        questionsList.forEachIndexed { index, _ ->
            val selectedIdx = selectedIndices[index]
            if (selectedIdx != -1) {
                val category = imageToCategoryMapping[index][selectedIdx]
                categoryVotes[category] = categoryVotes[category]!! + 1
            }
        }

        return StressTheme.getSortedThemes(categoryVotes).reversed()
    }
}