package com.example.tripset

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment(R.layout.fragment_login) {

    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val tilEmail = view.findViewById<TextInputLayout>(R.id.tilEmail)
        val tilPassword = view.findViewById<TextInputLayout>(R.id.tilPassword)

        val etEmail = view.findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = view.findViewById<TextInputEditText>(R.id.etPassword)

        val btnLogin = view.findViewById<MaterialButton>(R.id.btnLogin)
        val tvSignUp = view.findViewById<TextView>(R.id.tvSignUp)

        fun clearErrors() {
            tilEmail.error = null
            tilPassword.error = null
        }
        fun setLoading(isLoading: Boolean) {
            btnLogin.isEnabled = !isLoading
            tvSignUp.isEnabled = !isLoading
        }

        btnLogin.setOnClickListener {
            clearErrors()

            val email = etEmail.text?.toString()?.trim().orEmpty()
            val password = etPassword.text?.toString().orEmpty()

            var ok = true
            if (email.isBlank()) {
                tilEmail.error = "Email is required"
                ok = false
            }
            if (password.isBlank()) {
                tilPassword.error = "Password is required"
                ok = false
            }
            if (!ok) return@setOnClickListener

            setLoading(true)

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    setLoading(false)
                    Toast.makeText(requireContext(), "Logged in!", Toast.LENGTH_SHORT).show()

                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, TripsListFragment())
                        .commit()
                }
                .addOnFailureListener { e ->
                    setLoading(false)
                    Toast.makeText(
                        requireContext(),
                        e.message ?: "Login failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }


        tvSignUp.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, SignUpFragment())
                .commit()
        }
    }
}
