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
    private val perPage = 2
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
                    adapter.notifyItemRangeInserted(startPosition, newPhotos.size) // Обновляем адаптер
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

    private suspend fun fetchPhotos(page: Int, perPage: Int): List<Photo?> {
        return withContext(Dispatchers.IO) {
            skrape(HttpFetcher) {
                request {
                    headers = mapOf(
                        "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36",
                        "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
                        "Accept-Language" to "en-US,en;q=0.5"
                    )
                    url = "https://unsplash.com/s/photos/cats?license=free&orientation=portrait"
                    timeout = 120000
                }
                extractIt<ArrayList<Photo?>> {
                    htmlDocument {
                        findAll("img.tzC2N.fbGdz.cnmNG") {
                            forEach { productHtmlElement ->
                                val srcset = productHtmlElement.attribute("srcset")
                                Log.d("srcset", srcset)

                                val photo = srcset?.split(",")?.firstOrNull()?.trim()?.split(" ")?.first()
                                    ?.let { it1 -> Photo(it1) }
                                Log.d("photo", photo.toString())
                                it.add(photo)
                            }
                        }
                    }
                }
            }
        }
    }
}