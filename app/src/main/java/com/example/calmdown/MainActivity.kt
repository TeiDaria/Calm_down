package com.example.calmdown

import android.os.Bundle
import android.util.Log
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
    private var lastQuery: String = ""

    private lateinit var adapter: PhotosAdapter
    private val photos = mutableListOf<Photo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = MainLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = PhotosAdapter(photos)
        binding.recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        binding.recyclerView.adapter = adapter

        // Обработка нажатия кнопки "Найти"
        binding.button.setOnClickListener {
            resetPagination() // Сбрасываем пагинацию и очищаем список
            loadMorePhotos(binding) // Загружаем данные по новому запросу
        }

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as StaggeredGridLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPositions = layoutManager.findFirstVisibleItemPositions(null)
                val firstVisibleItemPosition = firstVisibleItemPositions[0]

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
                val user_query: String = binding.editTextText.text.toString()

                if (user_query.isEmpty()) {
                    Toast.makeText(this@MainActivity, "Введите запрос", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // Если запрос изменился, сбрасываем пагинацию
                if (user_query != lastQuery) {
                    resetPagination()
                    lastQuery = user_query
                }

                val newPhotos = fetchPhotos(user_query, currentPage, perPage)
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

    private fun resetPagination() {
        currentPage = 1 // Сбрасываем пагинацию
        isLastPage = false // Сбрасываем флаг последней страницы
        photos.clear() // Очищаем список фотографий
        adapter.notifyDataSetChanged() // Уведомляем адаптер об очистке списка
    }

    private suspend fun fetchPhotos(query: String, page: Int, perPage: Int): List<Photo> {
        return withContext(Dispatchers.IO) {
            try {
                RetrofitInstance.api.searchPhotos(
                    query = query,
                    page = page,
                    perPage = perPage,
                    "yy9LcujASiUad_qKT5tiK1GHQ96eGxWp3-LeEcxc7PA").results
            } catch (e: Exception) {
                Log.e("MainActivity", "Error fetching photos: ${e.message}", e)
                emptyList() // Возвращаем пустой список в случае ошибки
            }
        }
    }
}