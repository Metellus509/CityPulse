package com.example.citypulse.ui.view

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.citypulse.R
import com.example.citypulse.ui.viewmodel.MainViewModel
import com.example.citypulse.model.Place // <--- VÉRIFIE CET IMPORT

class MainActivity : AppCompatActivity() {

    // On lie le ViewModel à l'activité
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // On commence à observer les données
        setupObservers()
    }

    private fun setupObservers() {
        // On observe le LiveData du ViewModel
        viewModel.placesByLiveData.observe(this) { listDeLieux: List<Place>? ->
            // L'ajout de ": List<Place>?" ci-dessus aide le compilateur à "inférer" le type
            if (listDeLieux != null && listDeLieux.isNotEmpty()) {
                Toast.makeText(
                    this,
                    "Nombre de lieux chargés : ${listDeLieux.size}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}