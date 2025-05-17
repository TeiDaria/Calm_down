package com.example.calmdown.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.example.calmdown.model.data.Photo
import com.example.calmdown.model.data.RetrofitInstance
import com.example.calmdown.model.enums.StressTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PhotosViewModel: ViewModel() {
    private val _photos = MutableStateFlow<List<Photo>>(emptyList())
    val photos: StateFlow<List<Photo>> = _photos

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isLastPage = MutableStateFlow(false)
    val isLastPage: StateFlow<Boolean> = _isLastPage

    private var currentPage = 1
    private val perPage = 30

    fun loadPhotos(query: String) {
        if (_isLoading.value || _isLastPage.value) return

        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val response = RetrofitInstance.api.searchPhotos(
                    query,
                    page = currentPage,
                    perPage = perPage,
                    "yy9LcujASiUad_qKT5tiK1GHQ96eGxWp3-LeEcxc7PA"
                )
                val newPhotos = response.results

                if (newPhotos.isNotEmpty()) {
                    _photos.value += newPhotos
                    currentPage++
                } else {
                    _isLastPage.value = true
                }
            } catch (e: Exception) {
                // Обработка ошибок при необходимости
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetPagination() {
        currentPage = 1
        _isLastPage.value = false
        _photos.value = emptyList()
    }
}