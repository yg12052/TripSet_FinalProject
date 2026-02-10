package com.example.tripset

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpFragment : Fragment(R.layout.fragment_sign_up) {

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tilEmail = view.findViewById<TextInputLayout>(R.id.tilEmail)
        val tilPassword = view.findViewById<TextInputLayout>(R.id.tilPassword)
        val tilConfirmPassword = view.findViewById<TextInputLayout>(R.id.tilConfirmPassword)

        val etEmail = view.findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = view.findViewById<TextInputEditText>(R.id.etPassword)
        val etConfirmPassword = view.findViewById<TextInputEditText>(R.id.etConfirmPassword)

        val btnSignup = view.findViewById<MaterialButton>(R.id.btnSignup)
        val tvLogin = view.findViewById<TextView>(R.id.tvLogin)

        fun clearErrors() {
            tilEmail.error = null
            tilPassword.error = null
            tilConfirmPassword.error = null
        }

        fun setLoading(isLoading: Boolean) {
            btnSignup.isEnabled = !isLoading
            tvLogin.isEnabled = !isLoading
        }

        btnSignup.setOnClickListener {
            clearErrors()

            val email = etEmail.text?.toString()?.trim().orEmpty()
            val password = etPassword.text?.toString().orEmpty()
            val confirmPassword = etConfirmPassword.text?.toString().orEmpty()

            var ok = true

            if (email.isBlank()) {
                tilEmail.error = "Email is required"
                ok = false
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                tilEmail.error = "Invalid email format"
                ok = false
            }

            if (password.isBlank()) {
                tilPassword.error = "Password is required"
                ok = false
            } else if (password.length < 6) {
                tilPassword.error = "Password must be at least 6 characters"
                ok = false
            }

            if (confirmPassword.isBlank()) {
                tilConfirmPassword.error = "Confirm password is required"
                ok = false
            } else if (password.isNotBlank() && password != confirmPassword) {
                tilConfirmPassword.error = "Passwords do not match"
                ok = false
            }

            if (!ok) return@setOnClickListener

            setLoading(true)

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val uid = result.user?.uid
                    if (uid.isNullOrBlank()) {
                        setLoading(false)
                        Toast.makeText(requireContext(), "Signup failed (uid missing)", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    // Create user doc in Firestore (recommended for TripSet)
                    val userDoc = hashMapOf(
                        "email" to email,
                        "createdAt" to Timestamp.now()
                    )

                    db.collection("users").document(uid)
                        .set(userDoc)
                        .addOnSuccessListener {
                            setLoading(false)
                            Toast.makeText(requireContext(), "Account created!", Toast.LENGTH_SHORT).show()

                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragmentContainer, TripsListFragment())
                                .commit()
                        }
                        .addOnFailureListener { e ->
                            setLoading(false)
                            Toast.makeText(requireContext(), e.message ?: "Failed to save user", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    setLoading(false)
                    Toast.makeText(requireContext(), e.message ?: "Signup failed", Toast.LENGTH_SHORT).show()
                }
        }

        tvLogin.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, LoginFragment())
                .commit()
        }
    }
}
