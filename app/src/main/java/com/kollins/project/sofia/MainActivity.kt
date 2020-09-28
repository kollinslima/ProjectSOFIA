package com.kollins.project.sofia

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import android.Manifest

private const val MAIN_UI_TAG: String = "SOFIA UI MAIN"
private const val READ_REQUEST_CODE: Int = 42

enum class Device {
    ARDUINO_UNO
}

class MainActivity : AppCompatActivity() {

    private var seconds: Int = 0

    private var n: Long = 0
    private var begin: Long = 0
    private var sum: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                0
            )
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                0
            )
        }
    }

    fun performSearch(view: View) {
        Log.d(MAIN_UI_TAG, "Perform Search")
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        startActivityForResult(intent, READ_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.also { uri ->
                val fileDesciptor = contentResolver.openFileDescriptor(uri, "r")
                if (fileDesciptor != null) {
                    loadCore(Device.ARDUINO_UNO, fileDesciptor.fd)
                    startCore()
                    begin = System.nanoTime();
                }
//                val inputStream = contentResolver.openInputStream(uri)
//                if (inputStream != null) {
//                    Log.d(MAIN_UI_TAG, "Reading file size: ${inputStream.available()}")
//                    var hexCode = ByteArray(inputStream.available())
//
//                    inputStream.bufferedReader().forEachLine {
//                        val line = it.removePrefix(":")
//                        Log.d(MAIN_UI_TAG, "LINE: $line")
//                        hexCode += line.chunked(2).map { pair -> pair.toUpperCase().toInt(16).toByte() }
//                            .toByteArray()
//                    }

//                }
            }
        }
    }

    fun getElapsed(): Long {
        n += 1;
        sum += (System.nanoTime() - begin)
        begin = System.nanoTime()
        return if (sum >= 60000000000)
            0
        else
            (sum/n)
    }

    //////////////////// LISTENERS ///////////////////////////
    fun timeUpdate() {
        seconds += 1
        runOnUiThread {sample_text.text = seconds.toString()}
//        val avg = getElapsed()
//        if (avg != 0L) {
//            runOnUiThread { sample_text.text = avg.toString() }
//        }

    }

    //////////////////// NATIVE FUNCTIONS ///////////////////////////
    external fun startCore()
    external fun loadCore(s: Device, fd: Int)

    companion object {
        init {
            System.loadLibrary("sofiacore")
        }
    }
}
