package com.example.covidtracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
/*
    Adapter class este folosita pentru a salava datele pentru fiecare item al recycler view-ului
 */

// ca si arg am pasat o lista care vom avea datele din StateModal
class StateRVAdapter (private val stateList:List<StateModal>) :
    RecyclerView.Adapter<StateRVAdapter.StateRVViewHolder>() {

    // prima data am creat ViewHolder class care este folosita pentru a initializa
    // toate variabilele care sunt folosite in state_rv_item
    class StateRVViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val stateNameTV:TextView = itemView.findViewById(R.id.idTVState)
        val casesTV:TextView = itemView.findViewById(R.id.idTVCases)
        val deathsTV:TextView = itemView.findViewById(R.id.idTVDeaths)
        val recoveredTV:TextView = itemView.findViewById(R.id.idTVRecovered)
        // am creat aceste variabile de tip TextView pentru variabilele din state_rv_item
    }

    // in aceasta functie am facut inflate pentru layout file-ul nostru
    // inflate = preia structura si vizualizarea def in acest fisier XML
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StateRVViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.state_rv_item, parent, false)
        return StateRVViewHolder(itemView)
    }

    // in aceasta metoda salvam datele pentru fiecare vizualizare
    override fun onBindViewHolder(holder: StateRVViewHolder, position: Int) {
        val stateData = stateList[position] // extragem datele din lista
        holder.casesTV.text = stateData.cases.toString()
        holder.stateNameTV.text = stateData.state
        holder.recoveredTV.text = stateData.recovered.toString()
        holder.deathsTV.text = stateData.deaths.toString()
    }

    // in metoda asta returnam size-ul ArrayList-ului nostru
    override fun getItemCount(): Int {
        return stateList.size
    }

}


