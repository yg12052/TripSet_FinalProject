package com.example.tripset

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TripsAdapter(
    private val items: MutableList<Trip> = mutableListOf(),
    private val onTripClick: (Trip) -> Unit
) : RecyclerView.Adapter<TripsAdapter.TripViewHolder>() {

    class TripViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDestination: TextView = itemView.findViewById(R.id.tvDestination)
        val tvDates: TextView = itemView.findViewById(R.id.tvDates)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trip, parent, false)
        return TripViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        val trip = items[position]

        holder.tvDestination.text = trip.destination

        val df = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val start = df.format(Date(trip.startDateMillis))
        val end = df.format(Date(trip.endDateMillis))
        holder.tvDates.text = "$start - $end"

        holder.itemView.setOnClickListener {
            onTripClick(trip)
        }

        holder.itemView.setOnLongClickListener {

            val context = holder.itemView.context
            val currentUid = FirebaseAuth.getInstance().currentUser?.uid

            if (currentUid == null) {
                Toast.makeText(context, "User not logged in", Toast.LENGTH_LONG).show()
                return@setOnLongClickListener true
            }

            val isOwner = trip.ownerUid == currentUid

            if (isOwner) {
                AlertDialog.Builder(context)
                    .setTitle("Delete trip?")
                    .setMessage("Are you sure you want to permanently delete \"${trip.destination}\"?")
                    .setPositiveButton("Delete") { _, _ ->
                        FirebaseFirestore.getInstance()
                            .collection("trips")
                            .document(trip.id)
                            .delete()
                            .addOnSuccessListener {
                                Toast.makeText(context, "Trip deleted", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    context,
                                    "Delete failed: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            } else {
                AlertDialog.Builder(context)
                    .setTitle("Leave trip?")
                    .setMessage("You are not the owner of \"${trip.destination}\". Do you want to remove it from your trips?")
                    .setPositiveButton("Leave") { _, _ ->
                        FirebaseFirestore.getInstance()
                            .collection("trips")
                            .document(trip.id)
                            .update("members", FieldValue.arrayRemove(currentUid))
                            .addOnSuccessListener {
                                Toast.makeText(context, "Trip removed from your list", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    context,
                                    "Leave failed: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }

            true
        }
    }

    fun submitList(newItems: List<Trip>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}