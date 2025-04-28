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
    private val perPage = 30
    private var isLoading = false
    private var isLastPage = false

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

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as StaggeredGridLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItems = IntArray(2)
                layoutManager.findFirstVisibleItemPositions(firstVisibleItems)
                val firstVisibleItem = firstVisibleItems.minOrNull() ?: 0

                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItem) >= totalItemCount && firstVisibleItem >= 0) {
                        // Пользователь долистал до конца - загружаем новые фото
                        val selectedTheme = getCurrentTheme() // Нужно реализовать этот метод
                        selectedTheme?.let { loadMorePhotos(it) }
                    }
                }
            }
        })


        val sortedThemes: List<StressTheme>? = intent.getSerializableExtra("SORTED_THEMES") as? List<StressTheme>

        // Настройка SeekBar
        setupSeekBar(sortedThemes)

        // Загрузка первых фото
        sortedThemes?.get(2)?.let { loadMorePhotos(it) }
    }

    private fun setupSeekBar(themes: List<StressTheme>?) {
        binding.stressSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val selectedTheme = themes?.get(progress)
                binding.currentLevelText.text = selectedTheme?.name // Отображаем текущую тему

                // При изменении уровня стресса сбрасываем пагинацию и загружаем новые фото
                resetPagination()
                if (selectedTheme != null) {
                    loadMorePhotos(selectedTheme) // Передаем выбранную тему
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    private fun getCurrentTheme(): StressTheme? {
        val progress = binding.stressSeekBar.progress
        val sortedThemes = intent.getSerializableExtra("SORTED_THEMES") as? List<StressTheme>
        return sortedThemes?.get(progress)
    }


//    private fun updateThemePreview(level: Int) {
//        val theme = StressTheme.values().first { it.level == level + 1 }
//        //binding.themePreview.setImageResource(theme.drawableRes)
//
//        binding.currentLevelText.text = when (level) {
//            0 -> "Очень низкий"
//            1 -> "Низкий"
//            2 -> "Средний"
//            3 -> "Высокий"
//            4 -> "Очень высокий"
//            else -> ""
//        }
//    }

    private fun loadMorePhotos(theme: StressTheme) {
        if (isLoading || isLastPage) return

        isLoading = true
        binding.progressBar.visibility = android.view.View.VISIBLE // Показываем ProgressBar

        lifecycleScope.launch {
            try {
                val newPhotos = fetchPhotos(theme.query, currentPage, perPage)

                if (newPhotos.isNotEmpty()) {
                    photos.addAll(newPhotos)
                    adapter.updatePhotos(photos)
                    currentPage++
                } else {
                    isLastPage = true
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error loading photos: ${e.message}", e)
            } finally {
                isLoading = false
                binding.progressBar.visibility = android.view.View.GONE // Скрываем ProgressBar
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