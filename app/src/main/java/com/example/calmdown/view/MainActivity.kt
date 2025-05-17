package com.example.calmdown.view

import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.calmdown.view.adapter.PhotosAdapter
import com.example.calmdown.model.data.Photo
import com.example.calmdown.model.data.RetrofitInstance
import com.example.calmdown.model.enums.StressTheme
import com.example.calmdown.databinding.MainLayoutBinding
import com.example.calmdown.viewmodel.PhotosViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: PhotosViewModel

    private lateinit var binding: MainLayoutBinding
    private lateinit var adapter: PhotosAdapter
    private val photos = mutableListOf<Photo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this).get(PhotosViewModel::class.java)

        binding = MainLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Инициализация адаптера
        adapter = PhotosAdapter(photos)
        binding.recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        binding.recyclerView.adapter = adapter

        // Подписка на состояние фотографий
        lifecycleScope.launchWhenStarted {
            viewModel.photos.collect { photos ->
                adapter.updatePhotos(photos)
            }
        }

        // Подписка на прогрессбар
        lifecycleScope.launchWhenStarted {
            viewModel.isLoading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
            }
        }

        // Обработка SeekBar
        val sortedThemes: List<StressTheme>? = intent.getSerializableExtra("SORTED_THEMES") as? List<StressTheme>
        setupSeekBar(sortedThemes)

        // Изначально загружаем фото по выбранной теме (например, по третьей)
        sortedThemes?.get(2)?.let { theme ->
            viewModel.resetPagination()
            viewModel.loadPhotos(theme.query)
        }

        // Обработка скролла для пагинации
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as StaggeredGridLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItems = IntArray(2)
                layoutManager.findFirstVisibleItemPositions(firstVisibleItems)
                val firstVisibleItem = firstVisibleItems.minOrNull() ?: 0

                if (!viewModel.isLoading.value && !viewModel.isLastPage.value) {
                    if ((visibleItemCount + firstVisibleItem) >= totalItemCount && firstVisibleItem >= 0) {
                        // Загружаем еще фото по текущей теме
                        getCurrentTheme()?.let { viewModel.loadPhotos(it.query) }
                    }
                }
            }
        })
    }



    private fun setupSeekBar(themes: List<StressTheme>?) {
        binding.stressSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val selectedTheme = themes?.get(progress)
                binding.currentLevelText.text = selectedTheme?.name ?: ""

                // При изменении темы сбрасываем пагинацию и загружаем новые фото
                viewModel.resetPagination()
                selectedTheme?.let { viewModel.loadPhotos(it.query) }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun getCurrentTheme(): StressTheme? {
        val progress = binding.stressSeekBar.progress
        val themes = intent.getSerializableExtra("SORTED_THEMES") as? List<StressTheme>
        return themes?.get(progress)
    }
}