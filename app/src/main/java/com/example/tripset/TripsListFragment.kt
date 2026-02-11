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
        rvTrips.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val v = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_trip, parent, false)
                return object : RecyclerView.ViewHolder(v) {}
            }

            override fun getItemCount(): Int = 5

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val tvDestination = holder.itemView.findViewById<TextView>(R.id.tvDestination)
                val tvDates = holder.itemView.findViewById<TextView>(R.id.tvDates)

                tvDestination.text = "Trip #$position"
                tvDates.text = "01/01/2026 - 07/01/2026"
            }
        }

        val fab = view.findViewById<FloatingActionButton>(R.id.fabAddTrip)
        fab.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, TripCreateFragment())
                .addToBackStack(null)
                .commit()
        }

    }
}
