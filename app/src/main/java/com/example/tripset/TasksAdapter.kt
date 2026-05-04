package com.example.tripset

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TasksAdapter(
    private val items: MutableList<TripTask> = mutableListOf(),
    private val onCheckedChanged: (TripTask, Boolean) -> Unit,
    private val onLongClick: (TripTask) -> Unit
) : RecyclerView.Adapter<TasksAdapter.TaskViewHolder>() {

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cbDone: CheckBox = itemView.findViewById(R.id.cbDone)
        val tvTaskTitle: TextView = itemView.findViewById(R.id.tvTaskTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)

        return TaskViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = items[position]

        holder.tvTaskTitle.text = task.title

        holder.cbDone.setOnCheckedChangeListener(null)
        holder.cbDone.isChecked = task.done

        holder.cbDone.setOnCheckedChangeListener { _, isChecked ->
            onCheckedChanged(task, isChecked)
        }

        holder.itemView.setOnLongClickListener {
            onLongClick(task)
            true
        }
    }

    fun submitList(newItems: List<TripTask>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}