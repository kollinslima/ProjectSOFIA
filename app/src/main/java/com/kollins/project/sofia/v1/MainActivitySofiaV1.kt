package com.kollins.project.sofia.v1

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.kollins.project.sofia.Device
import com.kollins.project.sofia.R
import com.kollins.project.sofia.RequestCodes
import com.kollins.project.sofia.SofiaUiController
import com.kollins.project.sofia.interfaces.ui.UiInterface
import com.kollins.project.sofia.v1.io.output.OutputFragmentV1
import kotlinx.android.synthetic.main.v1_main_activity.*

private const val MAIN_V1_UI_TAG: String = "SOFIA UI MAIN V1"

class MainActivitySofiaV1 : AppCompatActivity(), UiInterface {

    private var simulatedTime: Long = 0
    private lateinit var targetDevice: Device

    private lateinit var suc: SofiaUiController
    private lateinit var simulatedTimeText: TextView
    private lateinit var outputFragment: OutputFragmentV1

    //////////////////// ACTIVITY CALLBACKS ///////////////////////////
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.v1_main_activity)

        targetDevice = intent.getSerializableExtra(SofiaUiController.TARGET_DEVICE_EXTRA) as Device

        simulatedTimeText = v1SimulatedTime
        v1MainToolBar.inflateMenu(R.menu.v1_main_layout)
        v1MainToolBar.setOnMenuItemClickListener(toolBarMenuItemClick)

        outputFragment = OutputFragmentV1()
        outputFragment.init(targetDevice)

        suc = SofiaUiController(this)
    }

    //////////////////// UI FUNCTIONS ///////////////////////////
    fun importFile() {
        Log.d(MAIN_V1_UI_TAG, "Importing file...")
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        startActivityForResult(intent, RequestCodes.IMPORT_FILE_REQUEST_CODE.ordinal)
    }

    override fun timeUpdate() {
        simulatedTime++
        runOnUiThread { simulatedTimeText.text = simulatedTime.toString() }
    }

    override fun loadCoreChecksumError() {
        Toast.makeText(this, R.string.import_fail_checksum_error, Toast.LENGTH_SHORT)
    }

    override fun loadCoreFileOpenFail() {
        Toast.makeText(this, R.string.import_fail_open_fail, Toast.LENGTH_SHORT)
    }

    override fun loadCoreInvalidFile() {
        Toast.makeText(this, R.string.import_fail_invalid_file, Toast.LENGTH_SHORT)
    }

    override fun ioUpdate(change:String) {
        runOnUiThread { outputFragment.ioUpdate(change) }
    }

    //////////////////// LISTENERS ///////////////////////////
    private val toolBarMenuItemClick: Toolbar.OnMenuItemClickListener =
        Toolbar.OnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.v1ActionAddIO -> {
                    val fragManager = supportFragmentManager
                    if(fragManager.findFragmentByTag(OutputFragmentV1.V1_OUTPUT_FRAGMENT_TAG) == null) {
                        val fragTransaction = fragManager.beginTransaction()
                        fragTransaction.add(R.id.v1OutputFragmentFrame, outputFragment, OutputFragmentV1.V1_OUTPUT_FRAGMENT_TAG)
                        fragTransaction.commit()
                        v1OutputContainer.visibility = View.VISIBLE
                    }
                    outputFragment.addOutput()
                }
                R.id.v1ActionImport -> {
                    when (PackageManager.PERMISSION_GRANTED) {
                        ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ) -> {
                            importFile()
                        }
                        else -> {
                            ActivityCompat.requestPermissions(
                                this,
                                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                                RequestCodes.READ_EXTERNAL_REQUEST_CODE.ordinal
                            )
                        }
                    }
                }
            }
            true //Return true to consume this event
        }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            RequestCodes.READ_EXTERNAL_REQUEST_CODE.ordinal -> {
                importFile()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RequestCodes.IMPORT_FILE_REQUEST_CODE.ordinal && resultCode == Activity.RESULT_OK) {
            data?.data?.also { uri ->
                val fileDesciptor = contentResolver.openFileDescriptor(uri, "r")
                if (fileDesciptor != null) {
                    suc.loadCore(Device.ARDUINO_UNO, fileDesciptor.fd)
                } else {
                    Toast.makeText(this, R.string.import_fail_open_fail, Toast.LENGTH_SHORT)
                }
            }
        }
    }
}
