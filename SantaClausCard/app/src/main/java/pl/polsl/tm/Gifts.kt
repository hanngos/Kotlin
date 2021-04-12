package pl.polsl.tm

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.provider.MediaStore
import android.speech.RecognizerIntent
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.*
import java.io.File

const val CAMERA_AND_STORAGE_REQUEST_CODE = 7
const val LOCATION_REQUEST_CODE = 8

class Gifts : AppCompatActivity(), LocationListener {

    private lateinit var webAppInterface: WebAppInterface

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        val page = WebView(this)
        page.settings.javaScriptEnabled = true
        webAppInterface = WebAppInterface(this, this, savedInstanceState)
        page.addJavascriptInterface(webAppInterface, "gifts")
        page.loadUrl("file:///android_asset/gifts.html")
        setContentView(page)

        if (checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE)
        } else {
            getLocation()
        }

        val builder: StrictMode.VmPolicy.Builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
    }

    private fun getLocation(){
        val gps = getSystemService(LOCATION_SERVICE) as LocationManager

        if (checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            gps.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this)
        }
        getCoordinates(gps.getLastKnownLocation(LocationManager.GPS_PROVIDER))
    }

    private fun getCoordinates(location: Location?){
        webAppInterface.longitude = location?.longitude
        webAppInterface.latitude = location?.latitude
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode==RESULT_OK)
        {
            if(requestCode == 0) {
                val list: ArrayList<String>? = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                if (list != null) {
                    webAppInterface.listOfGifts.add("-" + list[0])
                }
            }
            if (requestCode == 1){
                val intent = Intent(this, Card::class.java).apply {
                    putExtra("listOfGifts", webAppInterface.listOfGifts.toString())
                    putExtra("longitude", (webAppInterface.longitude ?: "Lokalizacja").toString())
                    putExtra("latitude", (webAppInterface.latitude ?: "nie działa!").toString())
                    putExtra("photoPath", webAppInterface.file.toPath().toString())
                }
                this.startActivity(intent)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_AND_STORAGE_REQUEST_CODE) {
            if (checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                val photoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                photoIntent.putExtra(MediaStore.EXTRA_OUTPUT, webAppInterface.photoPath)
                try {
                    startActivityForResult(photoIntent, 1)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(this, "Sorry! Can't take photo", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Permission denied! Go to settings and change them!", Toast.LENGTH_LONG).show()
            }
        }
        else if (requestCode == LOCATION_REQUEST_CODE) {
            if (checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                getLocation()
            } else {
                Toast.makeText(this, "Permission denied! Go to settings and change them!", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onLocationChanged(location: Location?) {
        getCoordinates(location)
    }
    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {}
    override fun onProviderEnabled(p0: String?) {}
    override fun onProviderDisabled(p0: String?) {}
}

class WebAppInterface(private val context: Context, private val activity: Activity, private val bundle: Bundle?) {

    var listOfGifts = ArrayList<String>()
    var longitude: Double? = null
    var latitude: Double? = null
    val file = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.path + "/photo.jpg")
    val photoPath: Uri = Uri.fromFile(file)

    @JavascriptInterface
    fun addGift() {
        val giftIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        giftIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        giftIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Co chcesz dostać od Świętego Mikołaja?")
        try {
            startActivityForResult(activity, giftIntent, 0, bundle)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "I'm not listening!", Toast.LENGTH_SHORT).show()
        }
    }

    @JavascriptInterface
    fun takePhoto() {
        checkAndRequestPermissions()

        if (checkAndRequestPermissions()) {
            val photoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            photoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoPath)
            try {
                startActivityForResult(activity, photoIntent, 1, bundle)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, "Sorry! Can't take photo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkAndRequestPermissions(): Boolean {

        val camera: Int = checkSelfPermission(activity, Manifest.permission.CAMERA)
        val write: Int = checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val read: Int = checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
        val listPermissionsNeeded = ArrayList<String>()
        if (write != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (camera != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA)
        }
        if (read != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (read != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (listPermissionsNeeded.isNotEmpty()) {
            requestPermissions(activity, listPermissionsNeeded.toArray(arrayOfNulls<String>(listPermissionsNeeded.size)), CAMERA_AND_STORAGE_REQUEST_CODE)
            return false
        }
        return true
    }

}