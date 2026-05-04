package com.example.tripset

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class TripsListFragment : Fragment() {

    private lateinit var adapter: TripsAdapter
    private var listenerRegistration: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_trips_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvTrips = view.findViewById<RecyclerView>(R.id.rvTrips)

        adapter = TripsAdapter(mutableListOf()) { trip ->
            val editTripFragment = EditTripFragment.newInstance(trip.id)

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, editTripFragment)
                .addToBackStack(null)
                .commit()
        }

        rvTrips.layoutManager = LinearLayoutManager(requireContext())
        rvTrips.adapter = adapter

        startListeningForTrips()

        val fab = view.findViewById<FloatingActionButton>(R.id.fabAddTrip)
        fab.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, TripCreateFragment())
                .addToBackStack(null)
                .commit()
        }

        val btnLogout = view.findViewById<MaterialButton>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            parentFragmentManager.popBackStack(
                null,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, LoginFragment())
                .commit()

            Toast.makeText(requireContext(), "Logged out", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startListeningForTrips() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        listenerRegistration = FirebaseFirestore.getInstance()
            .collection("trips")
            .whereArrayContains("members", uid)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    Toast.makeText(requireContext(), "Load failed: ${error.message}", Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val trips = snapshot.documents.map { doc ->
                        Trip(
                            id = doc.id,
                            ownerUid = doc.getString("ownerUid") ?: "",
                            members = doc.get("members") as? List<String> ?: emptyList(),
                            destination = doc.getString("destination") ?: "",
                            startDateMillis = doc.getLong("startDateMillis") ?: 0L,
                            endDateMillis = doc.getLong("endDateMillis") ?: 0L
                        )
                    }

                    adapter.submitList(trips)
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listenerRegistration?.remove()
    }
}