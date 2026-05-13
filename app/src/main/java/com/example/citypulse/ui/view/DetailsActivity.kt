package com.example.citypulse.ui.view

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.citypulse.R
import com.example.citypulse.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

class DetailsActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private var isFavorite = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        // 1. Récupération des données passées par l'Intent
        val name = intent.getStringExtra("PLACE_NAME") ?: "Lieu inconnu"
        val placeId = intent.getStringExtra("PLACE_ID") ?: ""
        isFavorite = intent.getBooleanExtra("IS_FAVORITE", false)

        val detailName = findViewById<TextView>(R.id.detailName)
        val btnFavorite = findViewById<ImageButton>(R.id.btnFavorite)
        val btnSave = findViewById<Button>(R.id.btnSaveNote)
        val editNote = findViewById<EditText>(R.id.editUserNote)

        detailName.text = name

        // 2. CHARGEMENT INITIAL : Récupérer la note existante dans Room
        lifecycleScope.launch {
            // On récupère l'objet Place complet depuis la base de données locale
            val place = viewModel.getPlaceById(placeId)

            // Si une note existe déjà, on l'affiche dans le champ de texte
            place?.userNote?.let { existingNote ->
                editNote.setText(existingNote)
            }
        }

        // Initialisation de l'icône favori (étoile)
        updateFavoriteUI(btnFavorite)

        // 3. GESTION DU CLIC SUR L'ÉTOILE (Favoris)
        btnFavorite.setOnClickListener {
            isFavorite = !isFavorite
            updateFavoriteUI(btnFavorite)

            // Mise à jour permanente dans Room
            viewModel.updateFavoriteStatus(placeId, isFavorite)

            val msg = if (isFavorite) "Ajouté aux favoris" else "Retiré des favoris"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        // 4. GESTION DE LA SAUVEGARDE (Note)
        btnSave.setOnClickListener {
            val note = editNote.text.toString()

            // On sauvegarde la note, même si elle est vide (pour pouvoir l'effacer)
            viewModel.saveNote(placeId, note)

            hideKeyboard()
            Toast.makeText(this, "Note enregistrée pour $name", Toast.LENGTH_SHORT).show()

            // On ferme l'activité pour revenir à la carte à Port-au-Prince
            finish()
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