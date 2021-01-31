package com.kollins.project.sofia

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.kollins.project.sofia.v1.MainActivitySofiaV1

private const val SPLASH_SCREEN_UI_TAG: String = "SOFIA UI SPLASH SCREEN"
//private const val SPLASH_SCREEN_DELAY: Long = 1000 //ms

class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)

//        CoroutineScope(Dispatchers.IO).launch {
//            delay(SPLASH_SCREEN_DELAY)
//            withContext(Dispatchers.Main){
//                startActivity(Intent(this@SplashScreenActivity, MainActivitySofiaV1::class.java))
//            }
//        }
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(this, MainActivitySofiaV1::class.java).apply {
            putExtra(SofiaUiController.TARGET_DEVICE_EXTRA, Device.ATMEGA328P)
        }
        startActivity(intent)
        finish()
    }
}
