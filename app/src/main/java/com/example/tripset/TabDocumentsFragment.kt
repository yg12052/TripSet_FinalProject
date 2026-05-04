package com.example.tripset

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class TabDocumentsFragment : Fragment(R.layout.fragment_tab_documents) {

    private var tripId: String? = null
    private lateinit var adapter: DocumentsAdapter
    private var listenerRegistration: ListenerRegistration? = null

    private val filePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data ?: return@registerForActivityResult

                requireContext().contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                saveDocument(uri)
            }
        }

    companion object {
        private const val ARG_TRIP_ID = "tripId"

        fun newInstance(tripId: String): TabDocumentsFragment {
            val fragment = TabDocumentsFragment()
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

        val currentTripId = tripId ?: return

        val rvDocuments = view.findViewById<RecyclerView>(R.id.rvDocuments)
        val btnAddDocument = view.findViewById<MaterialButton>(R.id.btnAddDocument)

        adapter = DocumentsAdapter(
            onLongClick = { document ->
                confirmDeleteDocument(currentTripId, document)
            }
        )

        rvDocuments.layoutManager = LinearLayoutManager(requireContext())
        rvDocuments.adapter = adapter

        startListeningForDocuments(currentTripId)

        btnAddDocument.setOnClickListener {
            openFilePicker()
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }

        filePickerLauncher.launch(intent)
    }

    private fun saveDocument(uri: Uri) {
        val currentTripId = tripId ?: return
        val fileName = getFileName(uri)

        val document = hashMapOf(
            "name" to fileName,
            "fileUrl" to uri.toString(),
            "type" to "pdf"
        )

        FirebaseFirestore.getInstance()
            .collection("trips")
            .document(currentTripId)
            .collection("documents")
            .add(document)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Document added", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Add failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun startListeningForDocuments(tripId: String) {
        listenerRegistration = FirebaseFirestore.getInstance()
            .collection("trips")
            .document(tripId)
            .collection("documents")
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    Toast.makeText(requireContext(), "Load documents failed: ${error.message}", Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }

                val documents = snapshot?.documents?.map { doc ->
                    TripDocument(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        fileUrl = doc.getString("fileUrl") ?: "",
                        storagePath = ""
                    )
                } ?: emptyList()

                adapter.submitList(documents)
            }
    }

    private fun confirmDeleteDocument(tripId: String, document: TripDocument) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete document?")
            .setMessage("Delete \"${document.name}\"?")
            .setPositiveButton("Delete") { _, _ ->
                FirebaseFirestore.getInstance()
                    .collection("trips")
                    .document(tripId)
                    .collection("documents")
                    .document(document.id)
                    .delete()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun getFileName(uri: Uri): String {
        var fileName = "document.pdf"

        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)

        cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst() && nameIndex >= 0) {
                fileName = it.getString(nameIndex)
            }
        }

        return fileName
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listenerRegistration?.remove()
    }
}