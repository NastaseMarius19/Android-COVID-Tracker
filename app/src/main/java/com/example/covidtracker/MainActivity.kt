package com.example.covidtracker

import CameraFragment
import CovidCasesNotification
import VideoFragment
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.covidtracker.databinding.ActivityMainBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.Manifest
import android.os.Handler
import android.widget.Button
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider


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

class MainActivity : AppCompatActivity(), OnMapReadyCallback {


    private lateinit var mMap: GoogleMap

    private lateinit var binding: ActivityMainBinding


    private lateinit var covidCasesNotification: CovidCasesNotification
    private val handler = Handler()

    private val notificationInterval = 60 * 1000 // Interval de 1 minut (exprimat în milisecunde)
    private val initialDelay = 0L // Întârziere inițială de 0 milisecunde


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        covidCasesNotification = CovidCasesNotification(this)

        // Programați primul apel la funcția fetchCovidCasesAndShowNotification
        handler.postDelayed(notificationRunnable, initialDelay)

        // Setăm conținutul de layout pentru MainActivity
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceFragment(COVID())

        replaceFragment(COVID())
        replaceFragment(CameraFragment())

        val videoFragment = VideoFragment()

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.frame_layout, videoFragment)
            commit()
        }


        binding.bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_covid -> {
                    replaceFragment(COVID())
                    true
                }
                R.id.menu_map -> {
                    replaceFragment(Map())
                    true
                }
                R.id.menu_camera -> {
                    replaceFragment(CameraFragment())
                    true
                }
                R.id.menu_video -> {
                    supportFragmentManager.beginTransaction().apply {
                        replace(R.id.frame_layout, videoFragment)
                        commit()
                    }
                    true
                }
                else -> false
            }
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.frame_layout_map) as? SupportMapFragment
        if (mapFragment != null) {
            mapFragment.getMapAsync(this)
        } else {
            Toast.makeText(this, "", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(notificationRunnable)
    }

    private val notificationRunnable = object : Runnable {
        override fun run() {
            covidCasesNotification.fetchCovidCasesAndShowNotification()

            // Programați următorul apel la funcția fetchCovidCasesAndShowNotification
            handler.postDelayed(this, notificationInterval.toLong())
        }
    }

    private fun replaceFragment(fragment: Fragment) {

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(
            MarkerOptions()
                .position(sydney)
                .title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        // val latitude = sydney.latitude
        // val longitude = sydney.longitude
        //val apiKey = "AIzaSyCP-NQrUCfxmUFfzEES9-hhZC5XExQsk4w"
        //val mapUrl = "https://maps.googleapis.com/maps/api/staticmap?center=$latitude,$longitude&zoom=12&size=400x400&key=$apiKey"
    }


}


//AIzaSyCP-NQrUCfxmUFfzEES9-hhZC5XExQsk4w
