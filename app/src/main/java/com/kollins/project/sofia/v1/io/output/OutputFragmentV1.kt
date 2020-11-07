package com.kollins.project.sofia.v1.io.output

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.kollins.project.sofia.Device
import com.kollins.project.sofia.R
import com.kollins.project.sofia.interfaces.io.OutputInterface
import com.kollins.project.sofia.v1.io.output.atmega328p.OutputPinV1ATmega328P
import kotlinx.android.synthetic.main.v1_output_fragment.view.*

class OutputFragmentV1 : Fragment() {

    private val outputList: MutableList<OutputInterface> = mutableListOf()
    private val outputAdapter: OutputAdapterV1 = OutputAdapterV1(outputList)
    private lateinit var outputPin: OutputInterface

    companion object{
        const val V1_OUTPUT_FRAGMENT_TAG: String = "V1_OUTPUT_FRAGMENT_TAG"
    }

    fun init(targetDevice: Device) {
        outputPin = when (targetDevice) {
            Device.ARDUINO_UNO -> {
                OutputPinV1ATmega328P()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.v1_output_fragment, container, false)

        val outputListView = view.outputList
        outputListView.adapter = outputAdapter
        outputListView.layoutManager = LinearLayoutManager(activity)
        return view
    }

    fun addOutput() {
        outputList.add(outputPin.clone())
        outputAdapter.notifyItemInserted(outputList.size-1)
    }
}

