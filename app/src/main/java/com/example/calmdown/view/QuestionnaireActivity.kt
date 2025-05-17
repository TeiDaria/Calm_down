package com.example.calmdown.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.calmdown.R
import com.example.calmdown.model.data.Question
import com.example.calmdown.model.enums.StressTheme
import com.example.calmdown.databinding.ActivityQuestionnaireBinding
import com.example.calmdown.viewmodel.QuestionnaireViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class QuestionnaireActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQuestionnaireBinding

    private val viewModel: QuestionnaireViewModel by viewModels()

    private lateinit var selectedImageIndices: MutableList<Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuestionnaireBinding.inflate(layoutInflater)
        setContentView(binding.root)

        selectedImageIndices = MutableList(viewModel.questions.size) { -1 }

        // Наблюдаем за текущим вопросом и выбранными ответами через корутины/collectLatest или LiveData (если используете)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentQuestionIndex.collectLatest { index ->
                    showQuestion(index)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.selectedImageIndices.collectLatest { selections ->
                    updateUI(selections)
                }
            }
        }

        // Обработчики кликов по картинкам вызывают метод ViewModel:
        binding.image1.setOnClickListener { onImageSelected(0) }
        binding.image2.setOnClickListener { onImageSelected(1) }
        binding.image3.setOnClickListener { onImageSelected(2) }

        binding.backButton.setOnClickListener {
            viewModel.goToPreviousQuestion()
        }

        binding.nextButton.setOnClickListener {
            viewModel.goToNextQuestion()
        }

        binding.finishButton.setOnClickListener {
            val resultThemes = viewModel.calculateStressTheme()
            val recommendedTheme = resultThemes.getOrNull(3) ?: resultThemes.first()

            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("RECOMMENDED_THEME", recommendedTheme)
                putExtra("SORTED_THEMES", ArrayList(resultThemes))
            }
            startActivity(intent)
            finish()
        }
    }

    private fun showQuestion(index: Int) {
        val question = viewModel.questions[index]

        // Установка текста вопроса и изображений:
        binding.questionText.text = question.text
        binding.image1.setImageResource(question.imageResIds[0])
        binding.image2.setImageResource(question.imageResIds[1])
        binding.image3.setImageResource(question.imageResIds[2])

        // Обновляем рамки выделения:
        updateBorders(viewModel.selectedImageIndices.value[index])

        // Обновляем навигацию:
        updateNavigationButtons()
    }

    private fun updateBorders(selectedIdx: Int) {
        resetBorders()
        when (selectedIdx) {
            0 -> binding.border1.setBackgroundResource(R.drawable.border)
            1 -> binding.border2.setBackgroundResource(R.drawable.border)
            2 -> binding.border3.setBackgroundResource(R.drawable.border)
        }
    }

    private fun resetBorders() {
        binding.border1.setBackgroundResource(R.drawable.border_transparent)
        binding.border2.setBackgroundResource(R.drawable.border_transparent)
        binding.border3.setBackgroundResource(R.drawable.border_transparent)
    }

    private fun updateUI(selections: List<Int>) {
        val currentIdx = viewModel.currentQuestionIndex.value
        updateBorders(selections[currentIdx])
        updateNavigationButtons()
    }

    private fun onImageSelected(index: Int) {
        val currentIdx = viewModel.currentQuestionIndex.value
        viewModel.selectImage(currentIdx, index)

        // Обновляем рамки выделения:
        updateBorders(index)

        // Если не последний вопрос — переходим к следующему автоматически:
        if (currentIdx < viewModel.questions.size - 1) {
            viewModel.goToNextQuestion()
        } else {
            // Показываем кнопку завершения:
            binding.finishButton.visibility = View.VISIBLE
        }
    }

    private fun updateNavigationButtons() {
        val currentIdx = viewModel.currentQuestionIndex.value
        val selections = viewModel.selectedImageIndices.value

        binding.backButton.visibility =
            if (currentIdx > 0) View.VISIBLE else View.GONE

        binding.nextButton.visibility =
            if (currentIdx < viewModel.questions.size - 1 && selections[currentIdx] != -1) View.VISIBLE else View.GONE

        binding.finishButton.visibility =
            if (currentIdx == viewModel.questions.size - 1 && selections[currentIdx] != -1) View.VISIBLE else View.GONE
    }
}
