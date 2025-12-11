package com.adit.post8

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.adit.post8.databinding.ActivityMainBinding
import com.adit.post8.databinding.DialogAddTaskBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var taskAdapter: TaskAdapter
    private val db = FirebaseFirestore.getInstance()
    private val taskList = mutableListOf<Task>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        loadTasks()

        binding.fabAdd.setOnClickListener {
            showAddTaskDialog()
        }
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            taskList,
            onCheckChanged = { task, isChecked -> updateTaskStatus(task, isChecked) },
            onDeleteClick = { task -> deleteTask(task) },
            onEditClick = { task -> showEditTaskDialog(task) }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = taskAdapter
        }
    }

    private fun loadTasks() {
        db.collection("tasks")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                taskList.clear()
                snapshots?.documents?.forEach { doc ->
                    val task = doc.toObject(Task::class.java)
                    task?.let { taskList.add(it) }
                }

                updateUI()
            }
    }

    private fun updateUI() {
        binding.emptyState.visibility = if (taskList.isEmpty()) View.VISIBLE else View.GONE
        binding.recyclerView.visibility = if (taskList.isEmpty()) View.GONE else View.VISIBLE
        taskAdapter.notifyDataSetChanged()
    }

    private fun showAddTaskDialog() {
        val dialogBinding = DialogAddTaskBinding.inflate(LayoutInflater.from(this))
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        var selectedDate = ""

        dialogBinding.etDeadline.setOnClickListener {
            showDatePicker { date ->
                selectedDate = date
                dialogBinding.etDeadline.setText(date)
            }
        }

        dialogBinding.btnSave.setOnClickListener {
            val title = dialogBinding.etTitle.text.toString().trim()
            val description = dialogBinding.etDescription.text.toString().trim()

            if (title.isEmpty()) {
                dialogBinding.etTitle.error = "Judul tidak boleh kosong"
                return@setOnClickListener
            }

            if (selectedDate.isEmpty()) {
                Toast.makeText(this, "Pilih deadline terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val task = Task(
                id = db.collection("tasks").document().id,
                title = title,
                description = description,
                deadline = selectedDate,
                isCompleted = false,
                createdAt = System.currentTimeMillis()
            )

            addTask(task)
            dialog.dismiss()
        }

        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showEditTaskDialog(task: Task) {
        val dialogBinding = DialogAddTaskBinding.inflate(LayoutInflater.from(this))
        dialogBinding.tvDialogTitle.text = "Edit Tugas"
        dialogBinding.etTitle.setText(task.title)
        dialogBinding.etDescription.setText(task.description)
        dialogBinding.etDeadline.setText(task.deadline)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        var selectedDate = task.deadline

        dialogBinding.etDeadline.setOnClickListener {
            showDatePicker { date ->
                selectedDate = date
                dialogBinding.etDeadline.setText(date)
            }
        }

        dialogBinding.btnSave.setOnClickListener {
            val title = dialogBinding.etTitle.text.toString().trim()
            val description = dialogBinding.etDescription.text.toString().trim()

            if (title.isEmpty()) {
                dialogBinding.etTitle.error = "Judul tidak boleh kosong"
                return@setOnClickListener
            }

            if (selectedDate.isEmpty()) {
                Toast.makeText(this, "Pilih deadline terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updatedTask = task.copy(
                title = title,
                description = description,
                deadline = selectedDate
            )

            updateTask(updatedTask)
            dialog.dismiss()
        }

        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                val date = String.format("%d/%d/%d", day, month + 1, year)
                onDateSelected(date)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun addTask(task: Task) {
        db.collection("tasks")
            .document(task.id)
            .set(task)
            .addOnSuccessListener {
                // Task added successfully
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal menambah tugas: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateTask(task: Task) {
        db.collection("tasks")
            .document(task.id)
            .set(task)
            .addOnSuccessListener {
                Snackbar.make(binding.root, "Tugas diperbarui", Snackbar.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal memperbarui tugas: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateTaskStatus(task: Task, isCompleted: Boolean) {
        db.collection("tasks")
            .document(task.id)
            .update("isCompleted", isCompleted)
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal memperbarui status: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteTask(task: Task) {
        db.collection("tasks")
            .document(task.id)
            .delete()
            .addOnSuccessListener {
                Snackbar.make(binding.root, "Tugas dihapus", Snackbar.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal menghapus tugas: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
