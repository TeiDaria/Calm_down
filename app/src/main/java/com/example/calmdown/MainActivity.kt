package com.example.calmdown

import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.calmdown.databinding.MainLayoutBinding
import it.skrape.core.htmlDocument
import it.skrape.fetcher.HttpFetcher
import it.skrape.fetcher.extractIt
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private var currentPage = 1
    private val perPage = 20
    private var isLoading = false
    private var isLastPage = false
    private var currentStressLevel: Int = 3 // Средний уровень по умолчанию

    private lateinit var binding: MainLayoutBinding
    private lateinit var adapter: PhotosAdapter
    private val photos = mutableListOf<Photo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Инициализация адаптера
        adapter = PhotosAdapter(photos)
        binding.recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        binding.recyclerView.adapter = adapter

        // Настройка SeekBar
        setupSeekBar()

        // Загрузка первых фото
        loadMorePhotos()
    }

    private fun setupSeekBar() {
        binding.stressSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                currentStressLevel = progress + 1
                updateThemePreview(progress)

                // При изменении уровня стресса сбрасываем пагинацию и загружаем новые фото
                resetPagination()
                loadMorePhotos()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    private fun updateThemePreview(level: Int) {
        val theme = StressTheme.values().first { it.level == level + 1 }
        //binding.themePreview.setImageResource(theme.drawableRes)

        binding.currentLevelText.text = when (level) {
            0 -> "Очень низкий"
            1 -> "Низкий"
            2 -> "Средний"
            3 -> "Высокий"
            4 -> "Очень высокий"
            else -> ""
        }
    }

    private fun loadMorePhotos() {
        if (isLoading || isLastPage) return

        isLoading = true
        lifecycleScope.launch {
            try {
                val theme = StressTheme.fromLevel(currentStressLevel)
                val newPhotos = fetchPhotos(theme.query, currentPage, perPage)

                if (newPhotos.isNotEmpty()) {
                    photos.addAll(newPhotos.filterNotNull())
                    adapter.updatePhotos(photos)
                    currentPage++
                } else {
                    isLastPage = true
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error loading photos: ${e.message}", e)
            } finally {
                isLoading = false
            }
        }
    }

    private fun resetPagination() {
        currentPage = 1
        isLastPage = false
        photos.clear()
        adapter.notifyDataSetChanged()
    }

    private suspend fun fetchPhotos(query: String, page: Int, perPage: Int): List<Photo> {
        return withContext(Dispatchers.IO) {
            try {
                RetrofitInstance.api.searchPhotos(
                    query = query,
                    page = page,
                    perPage = perPage,
                    "yy9LcujASiUad_qKT5tiK1GHQ96eGxWp3-LeEcxc7PA"
                ).results
            } catch (e: Exception) {
                Log.e("MainActivity", "Error fetching photos: ${e.message}", e)
                emptyList()
            }
        }
    }
}