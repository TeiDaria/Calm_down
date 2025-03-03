package com.example.calmdown

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
    private val perPage = 5
    private var isLoading = false
    private var isLastPage = false

    private lateinit var adapter: PhotosAdapter
    private val photos = mutableListOf<Photo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = MainLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = PhotosAdapter(photos)
        binding.recyclerView.layoutManager = GridLayoutManager(this@MainActivity, 2)
        binding.recyclerView.adapter = adapter

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as GridLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                // Если пользователь доскроллил до конца и загрузка не идет
                if (!isLoading && !isLastPage) {
                    if (visibleItemCount + firstVisibleItemPosition >= totalItemCount
                        && firstVisibleItemPosition >= 0) {
                        loadMorePhotos(binding) // Загружаем следующую порцию
                    }
                }
            }
        })

        // Загружаем первую порцию изображений
        loadMorePhotos(binding)
    }

    // Метод для загрузки порции изображений
    private fun loadMorePhotos(binding: MainLayoutBinding) {
        if (isLoading || isLastPage) return

        isLoading = true
        lifecycleScope.launch {
            try {
                val newPhotos = fetchPhotos(currentPage, perPage)
                if (newPhotos.isNotEmpty()) {
                    val startPosition = photos.size
                    photos.addAll(newPhotos.filterNotNull()) // Добавляем новые фото в список
                    adapter.updatePhotos(photos) // Обновляем адаптер
                    currentPage++ // Увеличиваем номер страницы
                } else {
                    isLastPage = true // Если новых фото нет, останавливаем загрузку
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error loading more photos: ${e.message}", e)
            } finally {
                isLoading = false
                kotlinx.coroutines.delay(2000)
            }
        }
    }

    private suspend fun fetchPhotos(page: Int, perPage: Int): List<Photo> {
        return withContext(Dispatchers.IO) {
            try {
                RetrofitInstance.api.getPhotos(page, perPage, "yy9LcujASiUad_qKT5tiK1GHQ96eGxWp3-LeEcxc7PA")
            } catch (e: Exception) {
                Log.e("MainActivity", "Error fetching photos: ${e.message}", e)
                emptyList() // Возвращаем пустой список в случае ошибки
            }
        }
    }
}