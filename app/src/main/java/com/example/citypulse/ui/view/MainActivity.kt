
package com.example.citypulse.ui.view
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.citypulse.R
import com.example.citypulse.ui.viewmodel.MainViewModel

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
        viewModel.places.observe(this) { listDeLieux ->
            // À chaque fois que la liste change, ce code s'exécute
            if (listDeLieux.isNotEmpty()) {
                Toast.makeText(this, "Nombre de lieux chargés : ${listDeLieux.size}", Toast.LENGTH_LONG).show()
            }
        }
    }
}