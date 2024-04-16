package com.example.covidtracker

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class SignupActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        db = FirebaseFirestore.getInstance()

        val buttonSignUp = findViewById<Button>(R.id.buttonSignUp)
        buttonSignUp.setOnClickListener {
            val username = findViewById<EditText>(R.id.editTextUsername).text.toString()
            val email = findViewById<EditText>(R.id.editTextEmail).text.toString()
            val password = findViewById<EditText>(R.id.editTextPassword).text.toString()

            if (validateFields(username, email, password)) {
                val user = HashMap<String, Any>()
                user["username"] = username
                user["email"] = email
                user["password"] = password
                if(mailValidation(email)){
                db.collection("users")
                    .add(user)
                    .addOnSuccessListener { documentReference ->
                        showToast("User registered successfully")
                        redirectToLogin()
                    }
                    .addOnFailureListener { e ->
                        showToast("Error registering user")
                        // Handle failure, display error message, etc.
                    }
                } else {
                    showValidationErrorDialog("Mail format incorrect or already exists!")
                }
            } else {

                showValidationErrorDialog("Complete all fields!")
            }
        }
    }

    private fun mailValidation(mail: String): Boolean{
        var uniqMail = true
        val query = db.collection("users").whereEqualTo("email", mail)
        query.get()
            .addOnSuccessListener { querySnapshot ->

                if (!querySnapshot.isEmpty) {
                    uniqMail = false
                }
            }
            .addOnFailureListener { e ->
                // Tratarea erorilor în timpul accesului la baza de date
                uniqMail = false
                showValidationErrorDialog("An error occurred while checking the email. Please try again.")
            }

        if (!Patterns.EMAIL_ADDRESS.matcher(mail).matches()) {
            uniqMail = false
        }
        return uniqMail
    }

    private fun validateFields(username: String, email: String, password: String): Boolean {
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            return false
        }
        return true
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish() // Close the signup activity to prevent going back to it by pressing the back button
    }

    private fun showValidationErrorDialog(errorMessage: String) {
        if (!isFinishing) {
            val dialogBuilder = AlertDialog.Builder(this)
            dialogBuilder.setMessage(errorMessage)
                .setCancelable(false)
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }

            val alert = dialogBuilder.create()
            alert.show()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
