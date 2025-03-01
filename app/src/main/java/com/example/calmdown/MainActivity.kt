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
    private var currentPage = 1
    private val perPage = 20
    private var isLoading = false
    private var isLastPage = false

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
                    headers = mapOf( "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36",
                        "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
                        "Accept-Language" to "en-US,en;q=0.5")
                    url = "https://unsplash.com/s/photos/cats?license=free&orientation=portrait"
                    timeout = 60000
                }
                extractIt<ArrayList<Photo?>> {
                    htmlDocument {
                        findFirst("a.mG0SP") {
                            findAll("img.tzC2N.fbGdz.cnmNG") {
                                forEach { productHtmlElement ->
                                    val srcset = productHtmlElement.attribute("srcset")
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
}
