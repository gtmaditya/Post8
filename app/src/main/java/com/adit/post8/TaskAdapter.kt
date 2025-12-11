package com.adit.post8

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.adit.post8.databinding.ItemTaskBinding

class TaskAdapter(
    private val tasks: List<com.adit.post8.Task>,
    private val onCheckChanged: (com.adit.post8.Task, Boolean) -> Unit,
    private val onDeleteClick: (com.adit.post8.Task) -> Unit,
    private val onEditClick: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]

        holder.binding.apply {
            tvTitle.text = task.title
            tvDescription.text = task.description
            tvDeadline.text = task.deadline
            checkBox.isChecked = task.isCompleted

            // Update text appearance based on completion status
            if (task.isCompleted) {
                tvTitle.alpha = 0.5f
                tvDescription.alpha = 0.5f
                tvDeadline.alpha = 0.5f
            } else {
                tvTitle.alpha = 1.0f
                tvDescription.alpha = 1.0f
                tvDeadline.alpha = 1.0f
            }

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                onCheckChanged(task, isChecked)
            }

            btnDelete.setOnClickListener {
                onDeleteClick(task)
            }

            root.setOnClickListener {
                onEditClick(task)
            }
        }
    }

    override fun getItemCount() = tasks.size
}