package com.example.tripset

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DocumentsAdapter(
    private val items: MutableList<TripDocument> = mutableListOf(),
    private val onLongClick: (TripDocument) -> Unit
) : RecyclerView.Adapter<DocumentsAdapter.DocumentViewHolder>() {

    class DocumentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDocumentName: TextView = itemView.findViewById(R.id.tvDocumentName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocumentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_document, parent, false)
        return DocumentViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: DocumentViewHolder, position: Int) {
        val document = items[position]

        holder.tvDocumentName.text = "📄 ${document.name}"

        holder.itemView.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(Uri.parse(document.fileUrl), "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            holder.itemView.context.startActivity(intent)
        }

        holder.itemView.setOnLongClickListener {
            onLongClick(document)
            true
        }
    }

    fun submitList(newItems: List<TripDocument>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}