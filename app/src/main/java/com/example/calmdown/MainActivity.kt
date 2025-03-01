package com.example.calmdown

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.calmdown.databinding.MainLayoutBinding
import it.skrape.core.htmlDocument
import it.skrape.fetcher.HttpFetcher
import it.skrape.fetcher.extractIt
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.selects.html5.img
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = MainLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            try {
                val photos = fetchPhotos()
                Log.d("MainActivity", "Posts fetched: ${photos.size}")

                if (photos.isNotEmpty()) {
                    val adapter = PhotosAdapter(photos)
                    binding.recyclerView.layoutManager = GridLayoutManager(this@MainActivity, 2)
                    binding.recyclerView.adapter = adapter
                } else {
                    Log.e("MainActivity", "Posts list is empty")
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error fetching posts: ${e.message}", e)
            }
        }
    }

    private suspend fun fetchPhotos(): List<Photo?> {
        return withContext(Dispatchers.IO) {
            skrape(HttpFetcher) {
                request {
                    url = "https://yandex.ru/images/search?text=%D0%BA%D0%BE%D1%82%D0%B8%D0%BA%D0%B8&lr=213&redircnt=1740690742.1"
                    timeout = 60000
                    headers = mapOf( "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36" )

                }
                extractIt<ArrayList<Photo>> {
                    htmlDocument {
                        "a.Link.ImagesContentImage-Cover" {
                            findAll {
                                forEach { postHtmlElement ->
                                    val photo = Photo(
                                        image = postHtmlElement.img { findFirst { attribute("src") } }
                                    )
                                    it.add(photo)
                                }
                            }
                        }

                    }
                }
            }
        }
    }
}
