package com.example.calmdown

import android.content.Intent
import android.os.Bundle
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
            imageResIds = listOf(R.drawable.cat_with_flowers, R.drawable.flowers_and_mountains, R.drawable.sky_and_water)
        ),
        Question(
            text = "Какое фото вызывает у вас наибольшее чувство спокойствия и умиротворения?",
            imageResIds = listOf(R.drawable.tiger, R.drawable.flowers_in_snow, R.drawable.silly_cat)
        ),
        Question(
            text = "Какое фото вызывает у вас наибольшее чувство спокойствия и умиротворения?",
            imageResIds = listOf(R.drawable.dog_in_flowers, R.drawable.clouds, R.drawable.mountains_and_water)
        )
    )

    private var currentQuestionIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuestionnaireBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showQuestion(currentQuestionIndex)
        binding.backButton.setOnClickListener {
            if (currentQuestionIndex > 0) {
                currentQuestionIndex--
                showQuestion(currentQuestionIndex)
            }
        }

        binding.nextButton.setOnClickListener {
            if (currentQuestionIndex < questions.size - 1) {
                currentQuestionIndex++
                showQuestion(currentQuestionIndex)
            } else {
                // Завершение анкеты
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
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

        // Управление видимостью кнопок
        binding.backButton.visibility = if (index > 0) View.VISIBLE else View.GONE
        binding.nextButton.setImageResource(
            if (index == questions.size - 1) R.drawable.baseline_check_24 else R.drawable.baseline_arrow_forward_24
        )
    }

}
