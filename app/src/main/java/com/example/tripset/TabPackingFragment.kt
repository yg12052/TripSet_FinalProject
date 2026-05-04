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

class TabPackingFragment : Fragment(R.layout.fragment_tab_packing) {

    private var tripId: String? = null
    private lateinit var adapter: TasksAdapter
    private var listenerRegistration: ListenerRegistration? = null

    companion object {
        private const val ARG_TRIP_ID = "tripId"

        fun newInstance(tripId: String): TabPackingFragment {
            val fragment = TabPackingFragment()
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
            Toast.makeText(requireContext(), "Trip ID missing in packing", Toast.LENGTH_SHORT).show()
            return
        }

        val rvPacking = view.findViewById<RecyclerView>(R.id.rvPacking)
        val btnAddPacking = view.findViewById<MaterialButton>(R.id.btnAddPacking)

        adapter = TasksAdapter(
            onCheckedChanged = { item, isChecked ->
                updatePackingDone(currentTripId, item.id, isChecked)
            },
            onLongClick = { item ->
                confirmDeletePackingItem(currentTripId, item)
            }
        )

        rvPacking.layoutManager = LinearLayoutManager(requireContext())
        rvPacking.adapter = adapter

        startListeningForPackingItems(currentTripId)

        btnAddPacking.setOnClickListener {
            showAddPackingDialog(currentTripId)
        }
    }

    private fun startListeningForPackingItems(tripId: String) {
        listenerRegistration = FirebaseFirestore.getInstance()
            .collection("trips")
            .document(tripId)
            .collection("packingItems")
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    Toast.makeText(requireContext(), "Load packing failed: ${error.message}", Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val items = snapshot.documents.map { doc ->
                        TripTask(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            done = doc.getBoolean("done") ?: false
                        )
                    }

                    adapter.submitList(items)
                }
            }
    }

    private fun showAddPackingDialog(tripId: String) {
        val input = EditText(requireContext())
        input.hint = "Item name"

        AlertDialog.Builder(requireContext())
            .setTitle("Add packing item")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val title = input.text.toString().trim()

                if (title.isEmpty()) {
                    Toast.makeText(requireContext(), "Item cannot be empty", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                addPackingItem(tripId, title)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addPackingItem(tripId: String, title: String) {
        val item = hashMapOf(
            "title" to title,
            "done" to false
        )

        FirebaseFirestore.getInstance()
            .collection("trips")
            .document(tripId)
            .collection("packingItems")
            .add(item)
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Add failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun updatePackingDone(tripId: String, itemId: String, done: Boolean) {
        FirebaseFirestore.getInstance()
            .collection("trips")
            .document(tripId)
            .collection("packingItems")
            .document(itemId)
            .update("done", done)
    }

    private fun confirmDeletePackingItem(tripId: String, item: TripTask) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete item?")
            .setMessage("Delete \"${item.title}\"?")
            .setPositiveButton("Delete") { _, _ ->
                FirebaseFirestore.getInstance()
                    .collection("trips")
                    .document(tripId)
                    .collection("packingItems")
                    .document(item.id)
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