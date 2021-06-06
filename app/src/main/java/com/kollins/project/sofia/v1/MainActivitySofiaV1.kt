package com.kollins.project.sofia.v1

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.kollins.project.sofia.*
import com.kollins.project.sofia.exception.SofiaException
import com.kollins.project.sofia.interfaces.ui.UiInterface
import com.kollins.project.sofia.v1.io.input.InputFragmentV1
import com.kollins.project.sofia.v1.io.output.OutputFragmentV1
import kotlinx.android.synthetic.main.v1_main_activity.*


private const val MAIN_V1_UI_TAG: String = "SOFIA UI MAIN V1"

class MainActivitySofiaV1 : AppCompatActivity(), UiInterface {

    private lateinit var targetDevice: Device

    private lateinit var suc: SofiaUiController
    private lateinit var scn: SofiaCoreNotifier
    private lateinit var simulatedTimeText: TextView
    private lateinit var outputFragment: OutputFragmentV1
    private lateinit var inputFragment: InputFragmentV1

    private lateinit var fileDescriptor: ParcelFileDescriptor

    //////////////////// ACTIVITY CALLBACKS ///////////////////////////
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.v1_main_activity)

        suc = SofiaUiController(this)
        scn = SofiaCoreNotifier(suc)

        targetDevice = intent.getSerializableExtra(SofiaUiController.TARGET_DEVICE_EXTRA) as Device

        simulatedTimeText = v1SimulatedTime
        setSupportActionBar(v1MainToolBar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        outputFragment = OutputFragmentV1()
        outputFragment.init(targetDevice)
        inputFragment = InputFragmentV1(scn)
        inputFragment.init(targetDevice)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.v1_main_layout, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.v1ActionAddOutput -> {
                val fragManager = supportFragmentManager
                if (fragManager.findFragmentByTag(OutputFragmentV1.V1_OUTPUT_FRAGMENT_TAG) == null) {
                    val fragTransaction = fragManager.beginTransaction()
                    fragTransaction.add(
                        R.id.v1OutputFragmentFrame,
                        outputFragment,
                        OutputFragmentV1.V1_OUTPUT_FRAGMENT_TAG
                    )
                    fragTransaction.commit()
                    v1OutputContainer.visibility = View.VISIBLE
                }
                outputFragment.addOutput()
                true
            }

            R.id.v1ActionAddDigitalInput -> {
                val fragManager = supportFragmentManager
                if (fragManager.findFragmentByTag(InputFragmentV1.V1_INPUT_FRAGMENT_TAG) == null) {
                    val fragTransaction = fragManager.beginTransaction()
                    fragTransaction.add(
                        R.id.v1InputFragmentFrame,
                        inputFragment,
                        InputFragmentV1.V1_INPUT_FRAGMENT_TAG
                    )
                    fragTransaction.commit()
                    v1InputContainer.visibility = View.VISIBLE
                }
                inputFragment.addDigitalInput()
                true
            }

            R.id.v1ActionAddAnalogInput -> {
                val fragManager = supportFragmentManager
                if (fragManager.findFragmentByTag(InputFragmentV1.V1_INPUT_FRAGMENT_TAG) == null) {
                    val fragTransaction = fragManager.beginTransaction()
                    fragTransaction.add(
                        R.id.v1InputFragmentFrame,
                        inputFragment,
                        InputFragmentV1.V1_INPUT_FRAGMENT_TAG
                    )
                    fragTransaction.commit()
                    v1InputContainer.visibility = View.VISIBLE
                }
                inputFragment.addAnalogInput()
                true
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
                true
            }

            R.id.v1ActionClearIO -> {
                outputFragment.clearOutputs()
                inputFragment.clearInputs()

                val fragManager = supportFragmentManager
                val outputFragment =
                    fragManager.findFragmentByTag(OutputFragmentV1.V1_OUTPUT_FRAGMENT_TAG)
                val inputFragment =
                    fragManager.findFragmentByTag(InputFragmentV1.V1_INPUT_FRAGMENT_TAG)

                if (outputFragment != null) {
                    val fragTransaction = fragManager.beginTransaction()
                    fragTransaction.remove(outputFragment)
                    fragTransaction.commit()
                    v1OutputContainer.visibility = View.GONE
                }

                if (inputFragment != null) {
                    val fragTransaction = fragManager.beginTransaction()
                    fragTransaction.remove(inputFragment)
                    fragTransaction.commit()
                    v1InputContainer.visibility = View.GONE
                }

                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    //////////////////// UI FUNCTIONS ///////////////////////////
    private fun importFile() {
        Log.d(MAIN_V1_UI_TAG, "Importing file...")
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        startActivityForResult(intent, RequestCodes.IMPORT_FILE_REQUEST_CODE.ordinal)
    }

    override fun timeUpdate(time:String) {
        if (simulatedTimeText.text != time) {
            runOnUiThread { simulatedTimeText.text = time }
        }
    }

    override fun loadSuccess() {
        this.fileDescriptor.close()
        runOnUiThread {
            simulatedTimeText.text = "0";
        }
    }

    override fun loadCoreChecksumError() {
        this.fileDescriptor.close()
        Toast.makeText(this, R.string.import_fail_checksum_error, Toast.LENGTH_SHORT).show()
        throw SofiaException(getString(R.string.exception_checksum_error))
    }

    override fun loadCoreFileOpenFail() {
        this.fileDescriptor.close()
        Toast.makeText(this, R.string.import_fail_open_fail, Toast.LENGTH_SHORT).show()
        throw SofiaException(getString(R.string.exception_open_fail))
    }

    override fun loadCoreInvalidFile() {
        this.fileDescriptor.close()
        Toast.makeText(this, R.string.import_fail_invalid_file, Toast.LENGTH_SHORT).show()
        throw SofiaException(getString(R.string.exception_invalid_file))
    }

    override fun outputChange(change: String) {
        outputFragment.outputChange(change)
    }

    override fun outputConfig(config: String) {
        outputFragment.outputConfig(config)
    }

    override fun inputConfig(config: String) {
        inputFragment.inputConfig(config)
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
            if (data == null) {
                Toast.makeText(this, R.string.import_fail_open_fail, Toast.LENGTH_SHORT).show()
                throw SofiaException(getString(R.string.exception_import_fail_data_null))
            }

            data.data.also { uri ->
                if (uri == null) {
                    Toast.makeText(this, R.string.import_fail_open_fail, Toast.LENGTH_SHORT).show()
                    throw SofiaException(getString(R.string.exception_import_fail_uri_null))
                }

                val descriptor = contentResolver.openFileDescriptor(uri, "r")
                if (descriptor == null) {
                    val message = R.string.import_fail_open_fail
                    Toast.makeText(this, R.string.import_fail_open_fail, Toast.LENGTH_SHORT).show()
                    throw SofiaException(getString(message))
                }

                this.fileDescriptor = descriptor
                suc.loadCore(Device.ATMEGA328P, this.fileDescriptor.fd)
            }
        }
    }
}
