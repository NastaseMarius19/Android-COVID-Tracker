    package com.example.covidtracker

    import android.content.Intent
    import androidx.appcompat.app.AppCompatActivity
    import android.os.Bundle


    // IDEEA E URMATOAREA
    /*
        1) Pentru partea de bluetooth putem sa luam API-ul de la GoogleMaps si sa monitorizam in timp real persoanele care au COVID
        spre ex:  daca sunt 4 persoane intr-o incapere si 3 au COVID si una nu, acea persoana care nu este infectata sa primeasca
        un mesaj de atentionare ca zona in care se afla este cu caz ridicat de periculozitat.... Deci cam asta e ideea cu app
        prin bluetooth

        2) Iar ideea cu monitorizarea in timp real ramane aceeasi, luam un API cu cazurile pozitive de COVID si trimitem catre server
        ca si raspuns un JSON,
        3) GDPR sa fie criptate sa nu uitam, pentru ca lucram cu date despre utilizator
     */

    class StartupApplication : AppCompatActivity() {
        private var isFirstLaunchOnRun = true

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            val isFirstLaunch = checkIfFirstLaunch()

            if (isFirstLaunch) {
                // Redirecționăm către pagina de înregistrare
                startActivity(Intent(this, LoginActivity::class.java))
                finish()  // Închidem MainActivity pentru a preveni revenirea la ea prin apăsarea butonului Back
            }

        }

        private fun checkIfFirstLaunch(): Boolean {
            val isFirstLaunch = isFirstLaunchOnRun

            if (isFirstLaunch) {
                isFirstLaunchOnRun = false
            }

            return isFirstLaunch
        }
    }