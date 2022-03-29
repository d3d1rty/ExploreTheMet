package com.example.explorethemet

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.example.explorethemet.databinding.ActivityArtObjectBinding
import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.URL

class ArtObjectActivity : AppCompatActivity() {
    private lateinit var binding : ActivityArtObjectBinding
    lateinit var artObject : ArtObject
    private var imageJob : Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArtObjectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = getIntent()
        artObject = intent.getSerializableExtra(
            getString(R.string.art_object_intent_key)
        ) as ArtObject

        if (artObject.accessionNumber== "") {
            binding.valueAccessionNumber.text = "UNKNOWN"
        } else {
            binding.valueAccessionNumber.text = artObject.accessionNumber
        }

        if (artObject.department == "") {
            binding.valueDepartment.text = "UNKNOWN"
        } else {
            binding.valueDepartment.text = artObject.department
        }

        if (artObject.objectName== "") {
            binding.valueObjectName.text = "UNKNOWN"
        } else {
            binding.valueObjectName.text = artObject.objectName
        }

        if (artObject.title == "") {
            binding.valueTitle.text = "UNKNOWN"
        } else {
            binding.valueTitle.text = artObject.title
        }

        if (artObject.medium == "") {
            binding.valueMedium.text = "UNKNOWN"
        } else {
            binding.valueMedium.text = artObject.medium
        }

        if (artObject.objectDate == "") {
            binding.valueObjectDate.text = "UNKNOWN"
        } else {
            binding.valueObjectDate.text = artObject.objectDate
        }

        if (artObject.artistName == "") {
            binding.valueArtistName.text = "UNKNOWN"
        } else {
            binding.valueArtistName.text = artObject.artistName
        }

        if (artObject.artistBio == "") {
            binding.valueArtistBio.text = "UNKNOWN"
        } else {
            binding.valueArtistBio.text = artObject.artistBio
        }

        if (artObject.creditLine == "") {
            binding.valueCreditLine.text = "UNKNOWN"
        } else {
            binding.valueCreditLine.text = artObject.creditLine
        }

        binding.buttonViewMore.setOnClickListener {
            viewMoreInfo(artObject.objectUrl)
        }

        downloadImage(URL(artObject.image))
    }

    private fun viewMoreInfo(objectUrl : String) {
        if (objectUrl == "") {
            val builder = AlertDialog.Builder(binding.root.context)
            builder
                .setTitle(R.string.unavailable_page_title)
                .setMessage(R.string.unavailable_page_message)
                .setPositiveButton("Ok", null)
                .show()
        } else {
            val uri = Uri.parse(objectUrl)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
    }

    private fun downloadImage(imageUrl: URL) {
        imageJob = CoroutineScope(Dispatchers.IO).launch {
            val connection : HttpURLConnection = imageUrl.openConnection() as HttpURLConnection
            var bitmap : Bitmap? = null
            try {
                connection.getInputStream().use { stream ->
                    bitmap = BitmapFactory.decodeStream(stream)
                }
            } finally {
                connection.disconnect()
            }

            withContext(Dispatchers.Main) {
                binding.imageView.setImageBitmap(bitmap)
            }
        }
    }
}