    package com.example.covidtracker

    data class StateModal (
        val state:String,
        val recovered:Int,
        val deaths: Int,
        val cases: Int
            )


    // aceasta clasa este folosita pentru depozitarea datelor pe care le folosim in recycler view