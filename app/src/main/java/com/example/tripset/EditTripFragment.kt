package com.example.tripset

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EditTripFragment : Fragment(R.layout.fragment_edit_trip) {

    private var tripId: String? = null

    companion object {
        private const val ARG_TRIP_ID = "tripId"

        fun newInstance(tripId: String): EditTripFragment {
            val fragment = EditTripFragment()
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
//t
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (tripId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Trip ID missing", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return
        }

        setupBackButton(view)
        setupTabs()
        loadTripData(view)
    }

    private fun loadTripData(view: View) {
        val currentTripId = tripId ?: return

        FirebaseFirestore.getInstance()
            .collection("trips")
            .document(currentTripId)
            .get()
            .addOnSuccessListener { doc ->

                if (!doc.exists()) {
                    Toast.makeText(requireContext(), "Trip not found", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                    return@addOnSuccessListener
                }

                val destination = doc.getString("destination") ?: "Trip"
                val startDateMillis = doc.getLong("startDateMillis") ?: 0L
                val endDateMillis = doc.getLong("endDateMillis") ?: 0L

                val tvTripTitle = view.findViewById<TextView>(R.id.tvTripTitle)
                val tvTripDates = view.findViewById<TextView>(R.id.tvTripDates)

                tvTripTitle.text = destination

                if (startDateMillis > 0 && endDateMillis > 0) {
                    val df = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val start = df.format(Date(startDateMillis))
                    val end = df.format(Date(endDateMillis))
                    tvTripDates.text = "$start - $end"
                } else {
                    tvTripDates.text = ""
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Load failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun setupTabs() {
        val currentTripId = tripId ?: return

        val tabLayout = requireView().findViewById<TabLayout>(R.id.tabLayout)

        tabLayout.removeAllTabs()
        tabLayout.addTab(tabLayout.newTab().setText("General Tasks"))
        tabLayout.addTab(tabLayout.newTab().setText("Packing List"))
        tabLayout.addTab(tabLayout.newTab().setText("Documents"))

        childFragmentManager.beginTransaction()
            .replace(R.id.tabContainer, TabTasksFragment.newInstance(currentTripId))
            .commit()

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {

                val fragment = when (tab?.position) {
                    0 -> TabTasksFragment.newInstance(currentTripId)
                    1 -> TabPackingFragment.newInstance(currentTripId)
                    2 -> TabDocumentsFragment()
                    else -> TabTasksFragment.newInstance(currentTripId)
                }

                childFragmentManager.beginTransaction()
                    .replace(R.id.tabContainer, fragment)
                    .commit()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupBackButton(view: View) {
        view.findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
}