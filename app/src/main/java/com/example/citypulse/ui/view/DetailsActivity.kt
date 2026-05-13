package com.example.citypulse.ui.view

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.citypulse.R
import com.example.citypulse.ui.viewmodel.MainViewModel

class DetailsActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private var isFavorite = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        // Récupération des données passées par l'Intent
        val name = intent.getStringExtra("PLACE_NAME") ?: "Lieu inconnu"
        val placeId = intent.getStringExtra("PLACE_ID") ?: ""
        isFavorite = intent.getBooleanExtra("IS_FAVORITE", false)

        val detailName = findViewById<TextView>(R.id.detailName)
        val btnFavorite = findViewById<ImageButton>(R.id.btnFavorite)
        val btnSave = findViewById<Button>(R.id.btnSaveNote)
        val editNote = findViewById<EditText>(R.id.editUserNote)

        detailName.text = name

        // Initialisation de l'icône selon l'état actuel en base
        updateFavoriteUI(btnFavorite)

        // Gestion du clic sur l'étoile
        btnFavorite.setOnClickListener {
            isFavorite = !isFavorite
            updateFavoriteUI(btnFavorite)

            // Mise à jour PERMANENTE dans la base de données
            viewModel.updateFavoriteStatus(placeId, isFavorite)

            val msg = if (isFavorite) "Ajouté aux favoris" else "Retiré des favoris"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        btnSave.setOnClickListener {
            val note = editNote.text.toString()
            if (note.isNotEmpty()) {
                hideKeyboard()
                // TODO: viewModel.saveNote(placeId, note)
                Toast.makeText(this, "Note enregistrée", Toast.LENGTH_SHORT).show()
                finish() // Retour à la carte
            } else {
                Toast.makeText(this, "Veuillez écrire une note", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateFavoriteUI(btn: ImageButton) {
        val iconRes = if (isFavorite) {
            android.R.drawable.btn_star_big_on
        } else {
            android.R.drawable.btn_star_big_off
        }
        btn.setImageResource(iconRes)
    }

    private fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}