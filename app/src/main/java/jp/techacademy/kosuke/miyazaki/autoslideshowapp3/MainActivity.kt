package jp.techacademy.kosuke.miyazaki.autoslideshowapp3

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.*
import kotlin.concurrent.timerTask

class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var buttonPrev: Button
    private lateinit var buttonPlayPause: Button
    private lateinit var buttonNext: Button

    private var imageUriList: List<Uri> = listOf()
    private var currentPosition: Int = 0
    private var isPlaying: Boolean = false
    private var timer: Timer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)
        buttonPrev = findViewById(R.id.buttonPrev)
        buttonPlayPause = findViewById(R.id.buttonPlayPause)
        buttonNext = findViewById(R.id.buttonNext)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1000)
        } else {
            getImageUris()
        }

        buttonPrev.setOnClickListener {
            showPreviousImage()
        }

        buttonNext.setOnClickListener {
            showNextImage()
        }

        buttonPlayPause.setOnClickListener {
            if (isPlaying) {
                stopSlideshow()
            } else {
                startSlideshow()
            }
        }
    }

    private fun getImageUris() {
        val uriExternal: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val cursor = contentResolver.query(uriExternal, null, null, null, null)
        cursor?.let {
            val uris = mutableListOf<Uri>()
            while (it.moveToNext()) {
                val id = it.getLong(it.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                uris.add(uri)
            }
            imageUriList = uris
            it.close()
            if (imageUriList.isNotEmpty()) {
                imageView.setImageURI(imageUriList[0])
            }
        }
    }

    private fun showPreviousImage() {
        if (imageUriList.isNotEmpty()) {
            currentPosition = if (currentPosition - 1 < 0) imageUriList.size - 1 else currentPosition - 1
            imageView.setImageURI(imageUriList[currentPosition])
        }
    }

    private fun showNextImage() {
        if (imageUriList.isNotEmpty()) {
            currentPosition = if (currentPosition + 1 >= imageUriList.size) 0 else currentPosition + 1
            imageView.setImageURI(imageUriList[currentPosition])
        }
    }

    private fun startSlideshow() {
        isPlaying = true
        buttonPlayPause.text = "停止"
        buttonPrev.isEnabled = false
        buttonNext.isEnabled = false
        timer = Timer()
        timer?.scheduleAtFixedRate(timerTask {
            runOnUiThread {
                showNextImage()
            }
        }, 2000, 2000)
    }

    private fun stopSlideshow() {
        isPlaying = false
        buttonPlayPause.text = "再生"
        buttonPrev.isEnabled = true
        buttonNext.isEnabled = true
        timer?.cancel()
        timer = null
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1000) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getImageUris()
            }
        }
    }
}
