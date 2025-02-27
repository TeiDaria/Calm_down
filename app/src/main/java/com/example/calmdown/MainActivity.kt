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
                    url = "https://unsplash.com/s/photos/cats?license=free&orientation=portrait"
                    timeout = 60000
                }
                extractIt<ArrayList<Photo?>> {
                    htmlDocument {
                        findAll("a.mG0SP") {
                            forEach { postHtmlElement ->
                                val srcset = postHtmlElement.img { findFirst { attribute("srcset") } }
                                Log.d("srcset", srcset)
                                val photo =
                                    srcset.split(",").firstOrNull()?.trim()?.split(" ")?.first()
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
