package com.example.calmdown

data class SearchResponse(
    val total: Int,
    val total_pages: Int,
    val results: List<Photo> // Список фотографий
)
