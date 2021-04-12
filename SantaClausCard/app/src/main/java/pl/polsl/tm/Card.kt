package pl.polsl.tm

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity


class Card : AppCompatActivity() {

    private val webAppInterface2 = WebAppInterface2()

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        supportActionBar?.hide()
        window.decorView.apply {
            systemUiVisibility =
                    View.SYSTEM_UI_FLAG_FULLSCREEN
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }

        val page = WebView(this)
        page.settings.javaScriptEnabled=true
        page.addJavascriptInterface(webAppInterface2, "card")
        page.loadUrl("file:///android_asset/card.html")
        setContentView(page)

        webAppInterface2.latitude = intent.getStringExtra("latitude")
        webAppInterface2.longitude = intent.getStringExtra("longitude")
        webAppInterface2.listOfGifts = intent.getStringExtra("listOfGifts")
        webAppInterface2.photoPath = intent.getStringExtra("photoPath")
    }

}

class WebAppInterface2 {
    var listOfGifts : String? = ""
    var longitude: String? = ""
    var latitude: String? = ""
    var photoPath: String? = ""

    @JavascriptInterface
    fun getGifts(): String? {
        listOfGifts = listOfGifts?.replace(",", "<br>")
        val i = listOfGifts?.length
        return if (i != null && i > 2) {
            listOfGifts?.substring(1, i - 1)
        } else "Wszystko, czego potrzebuję do szczęścia to zaliczenie TM!"
    }

    @JavascriptInterface
    fun getPath(): String? {
        return photoPath
    }

    @JavascriptInterface
    fun getLocalization(): String {
        return "dł: $longitude<br>sz: $latitude"
    }

}

