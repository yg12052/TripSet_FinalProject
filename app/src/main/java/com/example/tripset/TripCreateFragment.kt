package com.example.tripset

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TripCreateFragment : Fragment(R.layout.fragment_trip_create) {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private var startDateMillis: Long? = null
    private var endDateMillis: Long? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<Toolbar?>(R.id.toolbarCreateTrip)
        toolbar?.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val etDestination = view.findViewById<TextInputEditText>(R.id.etDestination)
        val etStartDate = view.findViewById<TextInputEditText>(R.id.etStartDate)
        val etEndDate = view.findViewById<TextInputEditText>(R.id.etEndDate)

        val tilStartDate = view.findViewById<TextInputLayout>(R.id.tilStartDate)
        val tilEndDate = view.findViewById<TextInputLayout>(R.id.tilEndDate)

        val btnSave = view.findViewById<MaterialButton>(R.id.btnSaveTrip)
        val btnCancel = view.findViewById<MaterialButton?>(R.id.btnCancelTrip)
        btnCancel?.setOnClickListener { parentFragmentManager.popBackStack() }

        fun openStartPicker() {
            openDatePicker { selectedMillis -> startDateMillis = selectedMillis
                etStartDate.setText(dateFormat.format(selectedMillis))


                val currentEnd = endDateMillis
                if (currentEnd != null && currentEnd < selectedMillis) {
                    endDateMillis = null
                    etEndDate.setText("")
                }
            }
        }

        fun openEndPicker() {
            val currentStart = startDateMillis
            if (currentStart == null) {
                Toast.makeText(requireContext(), "Choose start date first", Toast.LENGTH_SHORT).show()
                return
            }

            openDatePicker(minDateMillis = currentStart) { selectedMillis ->
                endDateMillis = selectedMillis
                etEndDate.setText(dateFormat.format(selectedMillis))
            }
        }

        etStartDate.setOnClickListener { openStartPicker() }
        etEndDate.setOnClickListener { openEndPicker() }

        tilStartDate.setEndIconOnClickListener { openStartPicker() }
        tilEndDate.setEndIconOnClickListener { openEndPicker() }

        btnSave.setOnClickListener {
            val destination = etDestination.text?.toString()?.trim().orEmpty()
            val currentStart = startDateMillis
            val currentEnd = endDateMillis

            if (destination.isBlank()) {
                Toast.makeText(requireContext(), "Please enter destination", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (currentStart == null) {
                Toast.makeText(requireContext(), "Please choose start date", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (currentEnd == null) {
                Toast.makeText(requireContext(), "Please choose end date", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(
                requireContext(),
                "Saved: $destination\n${dateFormat.format(currentStart)} - ${dateFormat.format(currentEnd)}",
                Toast.LENGTH_LONG
            ).show()

            parentFragmentManager.popBackStack()
        }
    }

    private fun openDatePicker(
        minDateMillis: Long? = null,
        onSelected: (Long) -> Unit
    ) {
        val cal = Calendar.getInstance()

        val dialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val chosen = Calendar.getInstance()
                chosen.set(Calendar.YEAR, year)
                chosen.set(Calendar.MONTH, month)
                chosen.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                chosen.set(Calendar.HOUR_OF_DAY, 0)
                chosen.set(Calendar.MINUTE, 0)
                chosen.set(Calendar.SECOND, 0)
                chosen.set(Calendar.MILLISECOND, 0)

                onSelected(chosen.timeInMillis)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )

        if (minDateMillis != null) {
            dialog.datePicker.minDate = minDateMillis
        }

        dialog.show()
    }
}
