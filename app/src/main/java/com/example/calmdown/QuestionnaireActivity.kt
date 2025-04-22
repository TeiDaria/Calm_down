package com.example.calmdown

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.calmdown.databinding.ActivityQuestionnaireBinding

class QuestionnaireActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQuestionnaireBinding
    val questions = listOf(
        Question(
            text = "Какое фото вызывает у вас наибольшее чувство спокойствия и умиротворения?",
            imageResIds = listOf(R.drawable.cat_with_flowers, R.drawable.abstract1, R.drawable.sky_and_water)
        ),
        Question(
            text = "Какое фото вызывает у вас наибольшее чувство спокойствия и умиротворения?",
            imageResIds = listOf(R.drawable.dog_in_flowers2, R.drawable.flowers_and_mountains, R.drawable.silly_cat2)
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

    private var currentQuestionIndex = 0
    private var selectedImageIndex = -1 // Индекс выбранной картинки (-1 означает, что ничего не выбрано)

    private val selectedImageIndices = MutableList(questions.size) { -1 } // Изначально ни одна картинка не выбрана

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuestionnaireBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showQuestion(currentQuestionIndex)

        // Обработчики кликов по картинкам
        binding.image1.setOnClickListener { onImageSelected(0) }
        binding.image2.setOnClickListener { onImageSelected(1) }
        binding.image3.setOnClickListener { onImageSelected(2) }

        // Кнопка "Назад"
        binding.backButton.setOnClickListener {
            if (currentQuestionIndex > 0) {
                currentQuestionIndex--
                showQuestion(currentQuestionIndex)
                updateNavigationButtons()
            }
        }

        // Кнопка "Далее"
        binding.nextButton.setOnClickListener {
            if (currentQuestionIndex < questions.size - 1) {
                currentQuestionIndex++
                showQuestion(currentQuestionIndex)
                updateNavigationButtons()
            }
        }

        // Кнопка "Завершить"
        binding.finishButton.setOnClickListener {
            val recommendedTheme = calculateStressTheme(questions)[3]
            val sortedThemes = ArrayList(calculateStressTheme(questions))
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("RECOMMENDED_THEME", recommendedTheme)
                putExtra("SORTED_THEMES", sortedThemes)
            }
            startActivity(intent)
            finish()
        }
    }
    private fun showQuestion(index: Int) {
        val question = questions[index]

        // Установка текста вопроса
        binding.questionText.text = question.text

        // Установка изображений
        binding.image1.setImageResource(question.imageResIds[0])
        binding.image2.setImageResource(question.imageResIds[1])
        binding.image3.setImageResource(question.imageResIds[2])

        // Сброс выделения картинок
        resetImageBorders()

        // Восстановление выбранной картинки для текущего вопроса
        val selectedIndex = selectedImageIndices[index]
        if (selectedIndex != -1) {
            when (selectedIndex) {
                0 -> binding.border1.setBackgroundResource(R.drawable.border) // Рамка для image1
                1 -> binding.border2.setBackgroundResource(R.drawable.border) // Рамка для image2
                2 -> binding.border3.setBackgroundResource(R.drawable.border) // Рамка для image3
            }
        }

        // Обновление кнопок навигации
        updateNavigationButtons()
    }

    private fun onImageSelected(index: Int) {
        // Сохраняем выбранную картинку для текущего вопроса
        selectedImageIndices[currentQuestionIndex] = index

        Log.d("OhMYINDEX", "Selected image for question $currentQuestionIndex: $index")

        // Сброс выделения всех картинок
        resetImageBorders()

        // Выделение выбранной картинки
        when (index) {
            0 -> binding.border1.setBackgroundResource(R.drawable.border) // Рамка для image1
            1 -> binding.border2.setBackgroundResource(R.drawable.border) // Рамка для image2
            2 -> binding.border3.setBackgroundResource(R.drawable.border) // Рамка для image3
        }

        // Сохранение выбранной картинки
        selectedImageIndex = index

        if (currentQuestionIndex < questions.size - 1) {
            currentQuestionIndex++
            showQuestion(currentQuestionIndex)
        } else {
            // Если это последний вопрос, показываем кнопку "Завершить"
            binding.finishButton.visibility = View.VISIBLE
        }


    }

    private fun resetImageBorders() {
        // Сброс рамок у всех картинок
        binding.border1.setBackgroundResource(R.drawable.border_transparent)
        binding.border2.setBackgroundResource(R.drawable.border_transparent)
        binding.border3.setBackgroundResource(R.drawable.border_transparent)
    }

    private fun updateNavigationButtons() {
        binding.backButton.visibility = if (currentQuestionIndex > 0) View.VISIBLE else View.GONE
        binding.nextButton.visibility = if (currentQuestionIndex < questions.size - 1 && selectedImageIndices[currentQuestionIndex] != -1) View.VISIBLE else View.GONE
        binding.finishButton.visibility = if (currentQuestionIndex == questions.size - 1 && selectedImageIndices[currentQuestionIndex] != -1) View.VISIBLE else View.GONE
    }

    private fun calculateStressTheme(questions: List<Question>): List<StressTheme> {
        // Создаем карту для подсчета голосов по категориям
        val categoryVotes = mutableMapOf<StressTheme, Int>().apply {
            StressTheme.values().forEach { put(it, 0) } // Инициализируем все категории с 0 голосами
        }

        // Определяем соответствие индексов изображений и категорий
        val imageToCategoryMapping = listOf(
            listOf(StressTheme.CATS, StressTheme.ABSTRACT, StressTheme.SKY), // Вопрос 1
            listOf(StressTheme.DOGS, StressTheme.NATURE, StressTheme.CATS), // Вопрос 2
            listOf(StressTheme.ABSTRACT, StressTheme.DOGS, StressTheme.NATURE), // Вопрос 3
            listOf(StressTheme.NATURE, StressTheme.SKY, StressTheme.DOGS), // Вопрос 4
            listOf(StressTheme.SKY, StressTheme.CATS, StressTheme.ABSTRACT)  // Вопрос 5
        )

        questions.forEachIndexed { questionIndex, _ ->
            val selectedImageIndex = selectedImageIndices[questionIndex]
            if (selectedImageIndex != -1) {
                val category = imageToCategoryMapping[questionIndex][selectedImageIndex]
                categoryVotes[category] = categoryVotes[category]!! + 1
            }
        }

        // Получаем отсортированные категории по количеству голосов
        val sortedThemes = StressTheme.getSortedThemes(categoryVotes)

        return sortedThemes
    }

}
