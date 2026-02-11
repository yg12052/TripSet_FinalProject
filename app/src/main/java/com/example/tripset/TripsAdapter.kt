package com.example.tripset
import android.app.Activity
import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class TripsAdapter(
    private val items: MutableList<Trip> = mutableListOf()
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

        holder.itemView.setOnLongClickListener {

            val context = holder.itemView.context

            AlertDialog.Builder(context)
                .setTitle("Delete trip?")
                .setMessage("Are you sure you want to delete \"${trip.destination}\"?")
                .setPositiveButton("Delete") { _, _ ->
                    FirebaseFirestore.getInstance()
                        .collection("trips")
                        .document(trip.id)
                        .delete().addOnSuccessListener {
                            if (context is Activity) {
                                (context as Activity).recreate()
                            }
                        }
                }
                .setNegativeButton("Cancel", null)
                .show()

            true
        }
    }

    fun submitList(newItems: List<Trip>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
