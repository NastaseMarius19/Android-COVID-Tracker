package com.example.covidtracker

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.covidtracker.R.*
import org.json.JSONException


class COVID : Fragment() {

    private lateinit var worldCasesTV: TextView
    private lateinit var worldRecoveredTV: TextView
    private lateinit var worldDeathsTV: TextView
    private lateinit var countryCasesTV: TextView
    private lateinit var countryRecoveredTV: TextView
    private lateinit var countryDeathsTV: TextView
    lateinit var stateRV:RecyclerView
    lateinit var stateRVAdapter:StateRVAdapter
    lateinit var stateList:List<StateModal>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(layout.fragment_c_o_v_i_d, container, false)

        // init RecyclerView
        worldCasesTV = view.findViewById(R.id.idTVWorldCases)
        worldRecoveredTV = view.findViewById(R.id.idTVWorldRecovered)
        worldDeathsTV = view.findViewById(R.id.idTVWorldDeaths)
        countryCasesTV = view.findViewById(R.id.idTVIndianCases)
        countryRecoveredTV = view.findViewById(R.id.idTVIndianRecovered)
        countryDeathsTV = view.findViewById(R.id.idTVIndianDeaths)
        stateRV = view.findViewById(R.id.idRVStates)
        stateList = ArrayList<StateModal>()

        stateRV.layoutManager = LinearLayoutManager(requireContext())
        stateRVAdapter = StateRVAdapter(stateList)
        stateRV.adapter = stateRVAdapter

        getStateInfo()
        getWorldInfo()

        return view
    }

    private fun getStateInfo() {
        // firstly we re defining an url and using a queue we re getting the data
        val url = "https://api.rootnet.in/covid19-in/stats/latest"
        val queue = Volley.newRequestQueue(requireContext())
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
                    stateRV.adapter = stateRVAdapter


                }catch (e:JSONException) {
                    e.printStackTrace()
                }
            }, { error ->{
                Toast.makeText(requireContext(), "Fail to get data", Toast.LENGTH_SHORT).show()
            }})
        // after we init recycler view we need to add this request queue
        queue.add(request)

    }

    private fun getWorldInfo() {
        val url = "https://corona.lmao.ninja/v3/covid-19/all"
        val queue = Volley.newRequestQueue(requireContext())
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
                Toast.makeText(requireContext(), "Fail to get data", Toast.LENGTH_SHORT).show()
            })
    }


}