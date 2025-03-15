package com.example.calmdown

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class QuestionAdapter(private val questions: List<Question>, private val context: Context) : RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder>() {

    inner class QuestionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val questionText: TextView = itemView.findViewById(R.id.questionText)
        val image1: ImageView = itemView.findViewById(R.id.image1)
        val image2: ImageView = itemView.findViewById(R.id.image2)
        val image3: ImageView = itemView.findViewById(R.id.image3)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_question, parent, false)
        return QuestionViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        val question = questions[position]

        holder.questionText.text = question.text

        // Загрузите изображения из ресурсов
        holder.image1.setImageResource(question.imageResIds[0])
        holder.image2.setImageResource(question.imageResIds[1])
        holder.image3.setImageResource(question.imageResIds[2])

        // Обработка выбора изображения
        holder.image1.setOnClickListener {
            saveUserChoice(question.text, question.imageResIds[0])
        }

        holder.image2.setOnClickListener {
            saveUserChoice(question.text, question.imageResIds[1])
        }

        holder.image3.setOnClickListener {
            saveUserChoice(question.text, question.imageResIds[2])
        }
    }

    private fun saveUserChoice(question: String, imageResId: Int) {
        val sharedPreferences = context.getSharedPreferences("UserChoices", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt(question, imageResId)
        editor.apply()
    }

    override fun getItemCount(): Int {
        return questions.size
    }
}