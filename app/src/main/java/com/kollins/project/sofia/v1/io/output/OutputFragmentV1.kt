package com.kollins.project.sofia.v1.io.output

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.kollins.project.sofia.Device
import com.kollins.project.sofia.R

class OutputFragmentV1(private val targetDevice: Device) : Fragment() {

    private lateinit var outputList : MutableList<OutputPinV1>
    private lateinit var outputAdapter : OutputAdapterV1

    private val pinNames : List<String> = getPinNames()
    //DEFAULT PIN
    //LIST OF SET WITH MEM ADDR RELATED TO EACH PIN

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        outputList = mutableListOf()
        outputAdapter = OutputAdapterV1(outputList)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.v1_output_fragment, container, false)
        view.visibility = View.GONE
        return view
    }

    fun addOutput() {
        outputList.add(OutputPinV1())
    }

    private fun getPinNames(): List<String> {
        return when(targetDevice) {
            Device.ARDUINO_UNO -> {
                resources.getStringArray(R.array.arduino_uno_pins).toList()
            }
            else -> {
                resources.getStringArray(R.array.arduino_uno_pins).toList()
            }
        }
    }
}

