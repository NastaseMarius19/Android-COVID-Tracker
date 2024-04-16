/*
package com.example.covidtracker

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
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.android.volley.Request
import org.json.JSONException
import com.example.covidtracker.databinding.ActivityMainBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.Manifest
import android.widget.Button


// IDEEA E URMATOAREA
*/
/*
    1) Pentru partea de bluetooth putem sa luam API-ul de la GoogleMaps si sa monitorizam in timp real persoanele care au COVID
    spre ex:  daca sunt 4 persoane intr-o incapere si 3 au COVID si una nu, acea persoana care nu este infectata sa primeasca
    un mesaj de atentionare ca zona in care se afla este cu caz ridicat de periculozitat.... Deci cam asta e ideea cu app
    prin bluetooth

    2) Iar ideea cu monitorizarea in timp real ramane aceeasi, luam un API cu cazurile pozitive de COVID si trimitem catre server
    ca si raspuns un JSON,
    3) GDPR sa fie criptate sa nu uitam, pentru ca lucram cu date despre utilizator
 *//*


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    lateinit var worldCasesTV:TextView
    lateinit var worldRecoveredTV:TextView
    lateinit var worldDeathsTV:TextView
    lateinit var countryCasesTV:TextView
    lateinit var countryRecoveredTV:TextView
    lateinit var countryDeathsTV: TextView
    lateinit var stateRV:RecyclerView
    lateinit var stateRVAdapter:StateRVAdapter
    lateinit var stateList:List<StateModal>

    private lateinit var mMap: GoogleMap

    private lateinit var binding: ActivityMainBinding


    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothReceiver: BroadcastReceiver
    private val REQUEST_ENABLE_BT = 1
    private val LOCATION_PERMISSION_REQUEST_CODE = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setăm conținutul de layout pentru MainActivity
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceFragment(COVID())

        // init RecyclerView
        worldCasesTV = findViewById(R.id.idTVWorldCases)
        worldRecoveredTV = findViewById(R.id.idTVWorldRecovered)
        worldDeathsTV = findViewById(R.id.idTVWorldDeaths)
        countryCasesTV = findViewById(R.id.idTVIndianCases)
        countryDeathsTV = findViewById(R.id.idTVIndianDeaths)
        countryRecoveredTV = findViewById(R.id.idTVIndianRecovered)
        stateRV = findViewById(R.id.idRVStates)
        stateList = ArrayList<StateModal>()

        stateRV.layoutManager = LinearLayoutManager(this)
        stateRVAdapter = StateRVAdapter(stateList)
        stateRV.adapter = stateRVAdapter

        // calling both methods here
        //getStateInfo()
        //getWorldInfo()

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


        checkLocationPermission()

        // Inițializarea Bluetooth
        val bluetoothManager =
            getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        // Verificați dacă dispozitivul suportă Bluetooth
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
            Toast.makeText(this, "Dispozitivul nu suportă Bluetooth", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Verificați dacă Bluetooth este activat
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        } else {
            // Bluetooth este activat, puteți începe să căutați dispozitive
            startBluetoothDiscovery()
        }

        val btnEnableBluetooth = findViewById<Button>(R.id.btnEnableBluetooth)
        btnEnableBluetooth.setOnClickListener {
            if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            } else {
                Toast.makeText(this, "Bluetooth este deja activat", Toast.LENGTH_SHORT).show()
            }
        }

    }

    // verificare activare locatie in app deoarece vrem sa scanam dispozitivele
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun startBluetoothDiscovery() {
        // Începeți căutarea dispozitivelor Bluetooth disponibile
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        bluetoothAdapter.startDiscovery()

        // Setați un receptor pentru a asculta rezultatele descoperirii Bluetooth
        // Înregistrați receptorul pentru acțiunea ACTION_FOUND
        bluetoothReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val action = intent?.action
                if (BluetoothDevice.ACTION_FOUND == action) {
                    // Un dispozitiv Bluetooth a fost găsit
                    val device =
                        intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    // Procesați dispozitivul găsit aici
                }
            }
        }
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(bluetoothReceiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Întrerupeți descoperirea Bluetooth și eliberați resursele
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        bluetoothAdapter.cancelDiscovery()
        unregisterReceiver(bluetoothReceiver)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                // Bluetooth a fost activat cu succes
                startBluetoothDiscovery()
            } else {
                Toast.makeText(this, "Bluetooth nu a fost activat", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // inregistrare permisiunea de localizare
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permisiunea de localizare a fost acordată
                startBluetoothDiscovery()
            } else {
                Toast.makeText(
                    this,
                    "Permisiunea de localizare nu a fost acordată",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    */
/*
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_main_land)
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.activity_main)
        }
    } *//*


    private fun replaceFragment(fragment: Fragment) {

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()

    }


    // 2 methods one for STATE and another one for WORLD

    // from this method we will get data from an API
    private fun getStateInfo() {
        // firstly we re defining an url and using a queue we re getting the data
        val url = "https://api.rootnet.in/covid19-in/stats/latest"
        val queue = Volley.newRequestQueue(this@MainActivity)
        // new var for request bcs we are working with Jsons, that means we will make a JsonObjectRequest
        val request =
            JsonObjectRequest(Request.Method.GET,url,null,{ response ->
                try {
                    // here we will try to extract json data(this is an obj in json)
                    val dataObj = response.getJSONObject("data")
                    val summaryObj = dataObj.getJSONObject("summary")
                    // now we need 3 vars for our json obj properties
                    val cases:Int = summaryObj.getInt("total")
                    val recovered:Int = summaryObj.getInt("discharged")
                    val deaths:Int = summaryObj.getInt("deaths")

                    countryCasesTV.text = cases.toString()
                    countryRecoveredTV.text = recovered.toString()
                    countryDeathsTV.text = deaths.toString()

                    val regionalArray = dataObj.getJSONArray("regional")
                    for(i in 0 until regionalArray.length()) {
                        val regionalObj = regionalArray.getJSONObject(i)
                        val stateName:String = regionalObj.getString("loc")
                        val cases:Int = regionalObj.getInt("totalConfirmed")
                        val deaths:Int = regionalObj.getInt("deaths")
                        val recovered:Int = regionalObj.getInt("discharged")

                        // we want to pass our data
                        val stateModal = StateModal(stateName, recovered, deaths, cases)
                        stateList = stateList+stateModal
                    }
                    stateRVAdapter = StateRVAdapter(stateList)
                    stateRV.layoutManager = LinearLayoutManager(this)
                    stateRV.adapter = stateRVAdapter


                }catch (e:JSONException) {
                    e.printStackTrace()
                }
            }, { error ->{
                Toast.makeText(this, "Fail to get data", Toast.LENGTH_SHORT).show()
            }})
        // after we init recycler view we need to add this request queue
        queue.add(request)

    }

    private fun getWorldInfo() {
        val url = "https://corona.lmao.ninja/v3/covid-19/all"
        val queue = Volley.newRequestQueue(this@MainActivity)
        val request =
            JsonObjectRequest(Request.Method.GET, url, null, { response ->
                try {
                    val worldCases: Int = response.getInt("cases")
                    val worldRecovered: Int = response.getInt("recovered")
                    val worldDeaths: Int = response.getInt("deaths")

                    // and now we re inserting this data to our textview
                    worldRecoveredTV.text = worldRecovered.toString()
                    worldCasesTV.text = worldCases.toString()
                    worldDeathsTV.text = worldDeaths.toString()
                } catch (e:JSONException) {
                    e.printStackTrace()
                }
            }, { error ->
                Toast.makeText(this, "Fail to get data", Toast.LENGTH_SHORT).show()
            })
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
*/
