package com.kollins.project.sofia.v1.io.input

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.kollins.project.sofia.Device
import com.kollins.project.sofia.R
import com.kollins.project.sofia.interfaces.io.InputInterface
import com.kollins.project.sofia.interfaces.io.InputType
import com.kollins.project.sofia.v1.io.input.atmega328p.InputPinV1ATmega328P
import kotlinx.android.synthetic.main.v1_input_fragment.view.*
import kotlinx.android.synthetic.main.v1_output_fragment.view.*

class InputFragmentV1 : Fragment() {

    private val inputList: MutableList<InputInterface> = mutableListOf()
    private val inputAdapter: InputAdapterV1 = InputAdapterV1(inputList)
    private lateinit var inputPin: InputInterface

    companion object{
        const val V1_INPUT_FRAGMENT_TAG: String = "V1_INPUT_FRAGMENT_TAG"
    }

    fun init(targetDevice: Device) {
        inputPin = when (targetDevice) {
            Device.ATMEGA328P -> {
                InputPinV1ATmega328P()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.v1_input_fragment, container, false)

        val inputListView = view.inputList
        inputListView.adapter = inputAdapter
        inputListView.layoutManager = LinearLayoutManager(activity)
        return view
    }

    fun addDigitalInput() {
        val newInput = inputPin.clone()
        newInput.setInputType(InputType.DIGITAL)
        addInput(newInput)
    }

    fun addAnalogInput() {
        val newInput = inputPin.clone()
        newInput.setInputType(InputType.ANALOG)
        addInput(newInput)
    }

    private fun addInput(input:InputInterface) {
        inputList.add(input)
        inputAdapter.notifyItemInserted(inputList.size-1)
    }

    fun clearInputs() {
        inputList.clear()
        inputAdapter.notifyDataSetChanged()
    }
}

