package com.example.calmdown

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.calmdown.databinding.ActivityQuestionnaireBinding

class QuestionnaireActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityQuestionnaireBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Создаем список вопросов с идентификаторами ресурсов
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

        val adapter = QuestionAdapter(questions, this)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        // Кнопка для перехода на главный экран
        binding.backToMainButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

    }
}