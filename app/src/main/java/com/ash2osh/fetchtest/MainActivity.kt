package com.ash2osh.fetchtest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment


class MainActivity : AppCompatActivity()  {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        NavHostFragment.create(R.navigation.nav_graph)
        Navigation.findNavController(this, R.id.nav_host_fragment)

    }

}



