package com.example.calmdown

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.calmdown.databinding.ItemPhotoBinding

class PhotosAdapter(private var photos: List<Photo?>) : RecyclerView.Adapter<PhotosAdapter.PhotoViewHolder>() {
    inner class PhotoViewHolder(private val binding: ItemPhotoBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(photo: Photo) {
            Glide.with(binding.root.context)
                .load(photo.urls.small)
                .error(R.drawable.error_img)
                .into(binding.imageView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val binding = ItemPhotoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PhotoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        photos[position]?.let { holder.bind(it) }
    }

    override fun getItemCount(): Int {
        return photos.size
    }

    fun updatePhotos(newPhotos: List<Photo>) {
        val startPosition = photos.size
        photos = newPhotos
        //notifyItemRangeInserted(startPosition, newPhotos.size)
        notifyDataSetChanged()
    }

}