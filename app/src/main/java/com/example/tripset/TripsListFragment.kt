package com.example.tripset

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import android.widget.Toast
class TripsListFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_trips_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvTrips = view.findViewById<RecyclerView>(R.id.rvTrips)

        rvTrips.layoutManager = LinearLayoutManager(requireContext())
        val adapter = TripsAdapter()
        rvTrips.adapter = adapter
        loadTrips(adapter)



        val fab = view.findViewById<FloatingActionButton>(R.id.fabAddTrip)
        fab.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, TripCreateFragment())
                .addToBackStack(null)
                .commit()
        }

    }

    private fun loadTrips(adapter: TripsAdapter) {

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("trips")
            .whereEqualTo("ownerUid", uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->

                val trips = snapshot.documents.map { doc ->
                    Trip(
                        id = doc.id,
                        ownerUid = doc.getString("ownerUid") ?: "",
                        destination = doc.getString("destination") ?: "",
                        startDateMillis = doc.getLong("startDateMillis") ?: 0L,
                        endDateMillis = doc.getLong("endDateMillis") ?: 0L
                    )
                }

                adapter.submitList(trips)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Load failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

}
