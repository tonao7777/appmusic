package com.musicwave.app

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private var musicService: MusicService? = null
    private var serviceBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            musicService = (binder as MusicService.MusicBinder).getService()
            serviceBound = true
        }
        override fun onServiceDisconnected(name: ComponentName?) { serviceBound = false }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.statusBarColor = android.graphics.Color.parseColor("#0a0a12")
        window.navigationBarColor = android.graphics.Color.parseColor("#0a0a12")

        webView = findViewById(R.id.webview)
        setupWebView()
        webView.loadUrl("file:///android_asset/index.html")

        val intent = Intent(this, MusicService::class.java)
        startService(intent)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        requestAudioPermission()
    }

    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            mediaPlaybackRequiresUserGesture = false
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            cacheMode = WebSettings.LOAD_DEFAULT
        }
        webView.webViewClient = WebViewClient()
        webView.webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest?) {
                request?.grant(request.resources)
            }
        }
        webView.addJavascriptInterface(AndroidBridge(), "Android")
    }

    inner class AndroidBridge {

        @JavascriptInterface
        fun scanAllAudio(): String {
            return try {
                val results = JSONArray()
                val projection = arrayOf(
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media.DATA
                )
                val cursor = contentResolver.query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    "${MediaStore.Audio.Media.IS_MUSIC} != 0",
                    null,
                    "${MediaStore.Audio.Media.TITLE} ASC"
                )
                cursor?.use { c ->
                    val idCol   = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                    val titCol  = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                    val artCol  = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                    val albCol  = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                    val durCol  = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                    while (c.moveToNext()) {
                        val id  = c.getLong(idCol)
                        val uri = Uri.withAppendedPath(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id.toString()
                        ).toString()
                        val artist = c.getString(artCol) ?: ""
                        results.put(JSONObject().apply {
                            put("id",       id)
                            put("title",    c.getString(titCol) ?: "Sem título")
                            put("artist",   if (artist == "<unknown>") "" else artist)
                            put("album",    c.getString(albCol) ?: "")
                            put("duration", c.getLong(durCol))
                            put("uri",      uri)
                        })
                    }
                }
                results.toString()
            } catch (e: Exception) { "[]" }
        }

        @JavascriptInterface
        fun hasPermission(): Boolean {
            val perm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                Manifest.permission.READ_MEDIA_AUDIO
            else Manifest.permission.READ_EXTERNAL_STORAGE
            return ContextCompat.checkSelfPermission(this@MainActivity, perm) ==
                    PackageManager.PERMISSION_GRANTED
        }

        @JavascriptInterface
        fun requestPermission() = runOnUiThread { requestAudioPermission() }

        @JavascriptInterface
        fun notifyPlaying(title: String, artist: String) =
            runOnUiThread { musicService?.updateNotification(title, artist) }

        @JavascriptInterface
        fun showToast(msg: String) =
            runOnUiThread { Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show() }
    }

    private fun requestAudioPermission() {
        val perm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_AUDIO
        else Manifest.permission.READ_EXTERNAL_STORAGE

        if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(perm), 101)
        } else {
            webView.post {
                webView.evaluateJavascript("window.onPermissionGranted&&window.onPermissionGranted()", null)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            webView.evaluateJavascript("window.onPermissionGranted&&window.onPermissionGranted()", null)
        } else if (requestCode == 101) {
            Toast.makeText(this, "Permissão negada. Não é possível escanear músicas.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }

    override fun onDestroy() {
        if (serviceBound) unbindService(serviceConnection)
        super.onDestroy()
    }
}
