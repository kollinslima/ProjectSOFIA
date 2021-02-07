package com.kollins.project.sofia.v1.io.input

import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kollins.project.sofia.R
import com.kollins.project.sofia.interfaces.io.InputInterface
import com.kollins.project.sofia.interfaces.io.InputMode
import com.kollins.project.sofia.interfaces.io.InputType
import com.kollins.project.sofia.v1.io.input.atmega328p.InputPinV1ATmega328P
import kotlinx.android.synthetic.main.v1_input_pin_digital.view.*


class InputAdapterV1(private val inputList: MutableList<InputInterface>) :
    RecyclerView.Adapter<InputAdapterV1.InputViewHolder>() {

    abstract inner class InputViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {
        abstract fun bindView(pin: InputInterface);
    }

    inner class DigitalInputViewHolder(itemView: View, private val context: Context) :
        InputViewHolder(itemView, context) {

        private val button: AppCompatButton = itemView.v1DigitalPushButton
        private val modeSelector: AppCompatSpinner = itemView.v1PinModeSelector
        private val inputSelector: AppCompatSpinner = itemView.v1PinSelectorDigitalInput
        private val inputState: AppCompatImageView = itemView.v1DigitalInputState
        private var toggle: Boolean = false

        init {
            val pinNames = InputPinV1ATmega328P.pinNames
            val pinModes = InputMode.values()
            inputSelector.adapter = HintAdapter(
                context,
                android.R.layout.simple_spinner_dropdown_item,
                List(pinNames.size) { pinNames[it].boardName }
            )
            modeSelector.adapter = ArrayAdapter<String>(
                context,
                android.R.layout.simple_spinner_dropdown_item,
                List(pinModes.size) { pinModes[it].text }
            )
        }

        override fun bindView(pin: InputInterface) {
            inputSelector.setSelection(pin.getInputIndex())
            inputSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    pin.setInputIndex(position)
                    toggle = false
                    setInputState(pin.getInputModeIndex(), false)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }
            }

            modeSelector.setSelection(pin.getInputModeIndex())
            modeSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    pin.setInputModeIndex(position)
                    toggle = false
                    setInputState(pin.getInputModeIndex(), false)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }
            }

            button.setOnTouchListener { v, event ->
                var buttonPressed = false
                if (event.action == MotionEvent.ACTION_DOWN) {
                    buttonPressed = true
                    button.setBackgroundColor(ContextCompat.getColor(context, R.color.v1_on_push_button))
                    toggle = !toggle
                    v.performClick()
                } else if (event.action == MotionEvent.ACTION_UP) {
                    buttonPressed = false
                    button.setBackgroundColor(ContextCompat.getColor(context, R.color.v1_off_push_button))
                }
                setInputState(pin.getInputModeIndex(), buttonPressed)
                true
            }
        }

        fun setInputState(mode: Int, buttonPressed: Boolean) {
            val undefinedState = R.drawable.v1_digital_input_undefined
            val highState = R.drawable.v1_digital_input_on
            val lowState = R.drawable.v1_digital_input_off

            when (mode) {
                InputMode.PUSH_GND.ordinal -> {
                    inputState.background =
                        ContextCompat.getDrawable(
                            context,
                            if (buttonPressed) lowState else undefinedState
                        )
                }
                InputMode.PUSH_VCC.ordinal -> {
                    inputState.background =
                        ContextCompat.getDrawable(
                            context,
                            if (buttonPressed) highState else undefinedState
                        )
                }
                InputMode.PULL_UP.ordinal -> {
                    inputState.background =
                        ContextCompat.getDrawable(
                            context,
                            if (buttonPressed) lowState else highState
                        )
                }
                InputMode.PULL_DOWN.ordinal -> {
                    inputState.background =
                        ContextCompat.getDrawable(
                            context,
                            if (buttonPressed) highState else lowState
                        )
                }
                InputMode.TOGGLE.ordinal -> {
                    inputState.background =
                        ContextCompat.getDrawable(
                            context,
                            if (toggle) highState else lowState
                        )
                }
            }
        }

    }

    inner class AnalogInputViewHolder(itemView: View, private val context: Context) :
        InputViewHolder(itemView, context) {
        override fun bindView(pin: InputInterface) {
            TODO("Not yet implemented")
        }

    }

    override fun getItemCount(): Int {
        return inputList.size
    }

    override fun getItemViewType(position: Int): Int {
        return inputList[position].getInputType().ordinal
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InputViewHolder {
        return when (viewType) {
            InputType.ANALOG.ordinal -> {
                AnalogInputViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.v1_input_pin_analog, parent, false),
                    parent.context
                )
            }
            else -> {
                DigitalInputViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.v1_input_pin_digital, parent, false),
                    parent.context
                )
            }
        }
    }

    override fun onBindViewHolder(holder: InputViewHolder, position: Int) {
        holder.bindView(inputList[position])
    }
}

class HintAdapter(context: Context, resource: Int, objects: List<Any?>) :
    ArrayAdapter<Any?>(context, resource, objects) {
    override fun getCount(): Int {
        //Don't show PinX
        val count = super.getCount()
        return if (count > 0) count - 1 else count
    }
}
