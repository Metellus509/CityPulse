package com.example.citypulse.ui.view

import android.content.Context
import android.content.Intent
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

        // Coordonnées géographiques indispensables pour la génération du partage
        val lat = intent.getDoubleExtra("PLACE_LAT", 0.0)
        val lon = intent.getDoubleExtra("PLACE_LON", 0.0)

        val detailName = findViewById<TextView>(R.id.detailName)
        val btnFavorite = findViewById<ImageButton>(R.id.btnFavorite)
        val btnSave = findViewById<Button>(R.id.btnSaveNote)
        val btnShare = findViewById<Button>(R.id.btnShare) // Changement en Button selon le XML
        val editNote = findViewById<EditText>(R.id.editUserNote)

        detailName.text = name

        // 2. CHARGEMENT INITIAL : Récupérer la note existante dans Room
        lifecycleScope.launch {
            val place = viewModel.getPlaceById(placeId)
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

            viewModel.updateFavoriteStatus(placeId, isFavorite)

            val msg = if (isFavorite) "Ajouté aux favoris" else "Retiré des favoris"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        // 4. PARTAGE PAR INTENT IMPLICITE (SMS, Email, Applications tierces)
        btnShare.setOnClickListener {
            val messageBody = """
                Découvrez ce lieu d'intérêt sur CityPulse !
                
                Nom : $name
                Coordonnées : $lat , $lon
                
                Voir sur la carte : https://maps.google.com/?q=$lat,$lon
            """.trimIndent()

            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "CityPulse - Partage de coordonnées : $name")
                putExtra(Intent.EXTRA_TEXT, messageBody)
            }

            // Forcer l'affichage du sélecteur d'applications pour laisser le choix à l'étudiant/utilisateur
            val chooser = Intent.createChooser(sendIntent, "Envoyer les coordonnées via :")
            startActivity(chooser)
        }

        // 5. GESTION DE LA SAUVEGARDE (Note)
        btnSave.setOnClickListener {
            val note = editNote.text.toString()

            viewModel.saveNote(placeId, note)

            hideKeyboard()
            Toast.makeText(this, "Note enregistrée pour $name", Toast.LENGTH_SHORT).show()

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