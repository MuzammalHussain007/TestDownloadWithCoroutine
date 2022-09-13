package com.example.testdownloadwithcoroutine

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File
import java.util.*


class MainActivity : AppCompatActivity() {
    private var  permission = 0
    private lateinit var btnDownload: Button
    private lateinit var btnShare: Button
    private lateinit var LocalAddress : String

    private val requestPermisisonLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission())  {
 permission = if(it){
     1

 }else
 {
     0

 }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        this.btnDownload = findViewById(R.id.btnDownload)
        btnShare = findViewById(R.id.btnShare)

        btnShare.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                async {
                    createInstagramIntent("image/*",LocalAddress)
                }.await()
            }
        }

        btnDownload.setOnClickListener {
            requestPermisisonLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (permission==1)
            {


            CoroutineScope(Dispatchers.IO).launch {
                val title = async {
                    startDownload()
                }.await()
                Log.d("title_____________",""+title)

                 LocalAddress = async {
                    readFileLocalAddress(title)
                }.await()

            }

            }else{
                Toast.makeText(applicationContext,"allow request",Toast.LENGTH_SHORT).show()
            }
        }


    }

    private fun createInstagramIntent(type: String, mediaPath: String) {
        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        // Create the new Intent using the 'Send' action.
        val share = Intent(Intent.ACTION_SEND)

        // Set the MIME type
        share.type = type

        // Create the URI from the media
        val media = File(mediaPath)
        val uri = Uri.fromFile(media)

        // Add the URI to the Intent.
        share.putExtra(Intent.EXTRA_STREAM, uri)

        // Broadcast the Intent.
        startActivity(Intent.createChooser(share, "Share to"))
    }

    private fun readFileLocalAddress(title: String) : String {
        val f = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                .absolutePath + File.separator + title+".jpg"
        )
        Log.d("absolutePath___",""+f.absolutePath)
        return  f.absolutePath


    }


    fun startDownload() : String  {
        val title = UUID.randomUUID()
        try {
            val download = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val imageLink =
                Uri.parse("https://cdn.pixabay.com/photo/2015/04/23/22/00/tree-736885__480.jpg")
            val request = DownloadManager.Request(imageLink)
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
                .setMimeType("image/jpeg")
                .setAllowedOverRoaming(false)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION)
                .setTitle(title.toString())
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_PICTURES,File.separator+title
                )
            download.enqueue(request)
            runOnUiThread {
                Toast.makeText(this, "Download started", Toast.LENGTH_SHORT).show()

            }



        } catch (e: Exception) {
            e.printStackTrace()
        }
        return title.toString()
    }
}