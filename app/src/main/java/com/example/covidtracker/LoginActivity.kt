package com.example.covidtracker;

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        db = FirebaseFirestore.getInstance()


        val buttonLogin = findViewById<Button>(R.id.buttonLogin)
        buttonLogin.setOnClickListener {
            val email = findViewById<EditText>(R.id.editTextEmail).text.toString()
            val password = findViewById<EditText>(R.id.editTextPassword).text.toString()

            val query = db.collection("users").whereEqualTo("email", email)
            query.get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        // Obține primul document din rezultatele interogării
                        val userDocument = querySnapshot.documents[0]

                        val userPassword = userDocument.getString("password")

                        if(userPassword.equals(password)) {
                            val user = FirebaseAuth.getInstance().currentUser
                            redirectToHomePage(user)
                        } else {
                            showValidationErrorDialog("The password is incorrect")
                        }
                    } else {
                        showValidationErrorDialog("Cannot find an account," +
                                " create one by pressing sign up button.")
                    }
                }
                .addOnFailureListener { e ->
                    print(e.message)
                }
        }


        val buttonSignup = findViewById<Button>(R.id.signUp)
        // Setare listener pentru butonul "Signup New Account"
        buttonSignup.setOnClickListener {
            // Redirecționare către pagina de înregistrare (SignupActivity)
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        val btSignIn = findViewById<com.google.android.gms.common.SignInButton>(R.id.bt_sign_in)
        btSignIn.setOnClickListener {
            // Assign variable
            //btSignIn = findViewById(R.id.bt_sign_in)
            println("ajunge aici")
            // Initialize sign in options the client-id is copied form google-services.json file
            val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("438431947620-ecpi41uk3dhhf4mv8g8q993k3vs49ltm.apps.googleusercontent.com")
                .requestEmail()
                .build()

            // Initialize sign in client
            googleSignInClient = GoogleSignIn.getClient(this@LoginActivity, googleSignInOptions)
            btSignIn.setOnClickListener { // Initialize sign in intent
                println("ajunge aici 2")
                val intent: Intent = googleSignInClient.signInIntent
                // Start activity for result
                startActivityForResult(intent, 100)
            }
        }
    }



    private fun redirectToHomePage(user: FirebaseUser?) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Close the signup activity to prevent going back to it by pressing the back button
    }

    private fun showValidationErrorDialog(errorMessage: String) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage(errorMessage)
            .setCancelable(false)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }

        val alert = dialogBuilder.create()
        alert.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Check condition
        if (requestCode == 100) {
            println("ajunge aici 4")
            // When request code is equal to 100 initialize task
            startActivity(
                Intent(this, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }
}
