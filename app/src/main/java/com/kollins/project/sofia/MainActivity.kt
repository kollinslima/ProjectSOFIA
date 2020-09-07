package com.kollins.project.sofia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val MAIN_UI_TAG: String = "UI MAIN"
    private var seconds: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        startCore()
    }

//////////////////// LISTENERS ///////////////////////////
    fun timeUpdate() {
        seconds += 1
        runOnUiThread {sample_text.text = seconds.toString()}

}

//////////////////// NATIVE FUNCTIONS ///////////////////////////
    external fun startCore()

    companion object {
        init {
            System.loadLibrary("sofiacore")
        }
    }
}
