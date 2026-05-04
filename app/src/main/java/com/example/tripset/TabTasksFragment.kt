package com.example.tripset

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class TabTasksFragment : Fragment(R.layout.fragment_tab_tasks) {

    private var tripId: String? = null
    private lateinit var adapter: TasksAdapter
    private var listenerRegistration: ListenerRegistration? = null

    companion object {
        private const val ARG_TRIP_ID = "tripId"

        fun newInstance(tripId: String): TabTasksFragment {
            val fragment = TabTasksFragment()
            val bundle = Bundle()
            bundle.putString(ARG_TRIP_ID, tripId)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tripId = arguments?.getString(ARG_TRIP_ID)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentTripId = tripId
        if (currentTripId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Trip ID missing in tasks", Toast.LENGTH_SHORT).show()
            return
        }

        val rvTasks = view.findViewById<RecyclerView>(R.id.rvTasks)
        val btnAddTask = view.findViewById<MaterialButton>(R.id.btnAddTask)

        adapter = TasksAdapter(
            onCheckedChanged = { task, isChecked ->
                updateTaskDone(currentTripId, task.id, isChecked)
            },
            onLongClick = { task ->
                confirmDeleteTask(currentTripId, task)
            }
        )

        rvTasks.layoutManager = LinearLayoutManager(requireContext())
        rvTasks.adapter = adapter

        startListeningForTasks(currentTripId)

        btnAddTask.setOnClickListener {
            showAddTaskDialog(currentTripId)
        }
    }

    private fun startListeningForTasks(tripId: String) {
        listenerRegistration = FirebaseFirestore.getInstance()
            .collection("trips")
            .document(tripId)
            .collection("generalTasks")
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    Toast.makeText(requireContext(), "Load tasks failed: ${error.message}", Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val tasks = snapshot.documents.map { doc ->
                        TripTask(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            done = doc.getBoolean("done") ?: false
                        )
                    }

                    adapter.submitList(tasks)
                }
            }
    }

    private fun showAddTaskDialog(tripId: String) {
        val input = EditText(requireContext())
        input.hint = "Task name"

        AlertDialog.Builder(requireContext())
            .setTitle("Add task")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val title = input.text.toString().trim()

                if (title.isEmpty()) {
                    Toast.makeText(requireContext(), "Task cannot be empty", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                addTask(tripId, title)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addTask(tripId: String, title: String) {
        val task = hashMapOf(
            "title" to title,
            "done" to false
        )

        FirebaseFirestore.getInstance()
            .collection("trips")
            .document(tripId)
            .collection("generalTasks")
            .add(task)
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Add failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun updateTaskDone(tripId: String, taskId: String, done: Boolean) {
        FirebaseFirestore.getInstance()
            .collection("trips")
            .document(tripId)
            .collection("generalTasks")
            .document(taskId)
            .update("done", done)
    }

    private fun confirmDeleteTask(tripId: String, task: TripTask) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete task?")
            .setMessage("Delete \"${task.title}\"?")
            .setPositiveButton("Delete") { _, _ ->
                FirebaseFirestore.getInstance()
                    .collection("trips")
                    .document(tripId)
                    .collection("generalTasks")
                    .document(task.id)
                    .delete()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listenerRegistration?.remove()
    }
}