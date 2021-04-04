package com.kollins.project.sofia.v1.io.input

import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kollins.project.sofia.R
import com.kollins.project.sofia.interfaces.io.InputInterface
import com.kollins.project.sofia.interfaces.io.InputMode
import com.kollins.project.sofia.interfaces.io.InputType
import com.kollins.project.sofia.v1.io.input.atmega328p.InputPinV1ATmega328P
import kotlinx.android.synthetic.main.v1_input_pin_analog.view.*
import kotlinx.android.synthetic.main.v1_input_pin_digital.view.*


class InputAdapterV1(private val inputList: MutableList<InputInterface>) :
    RecyclerView.Adapter<InputAdapterV1.InputViewHolder>() {

    private var boundViewHolders = mutableMapOf<InputAdapterV1.InputViewHolder, Int>()

    abstract inner class InputViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView), View.OnLongClickListener {
        abstract fun bindView(pin: InputInterface);
    }

    inner class DigitalInputViewHolder(itemView: View, private val context: Context) :
        InputViewHolder(itemView, context) {

        private val button: AppCompatButton = itemView.v1DigitalPushButton
        private val modeSelector: AppCompatSpinner = itemView.v1PinModeSelector
        private val inputSelector: AppCompatSpinner = itemView.v1PinSelectorDigitalInput
        private val inputState: AppCompatImageView = itemView.v1DigitalInputState
        private var toggle: Boolean = false

        private var spinnerPinAdapter = HideAdapter(
            context,
            android.R.layout.simple_spinner_dropdown_item,
            InputPinV1ATmega328P.inputList.map { it.boardName }
        )

        private var spinnerModeAdapter = ArrayAdapter<String>(
            context,
            android.R.layout.simple_spinner_dropdown_item,
            InputMode.values().map { it.text }
        )

        init {
            inputSelector.adapter = spinnerPinAdapter
            modeSelector.adapter = spinnerModeAdapter
            itemView.setOnLongClickListener(this)
        }

        override fun bindView(pin: InputInterface) {

            //Spinner list may change, update it!
            spinnerPinAdapter.clear()
            spinnerPinAdapter.addAll(pin.getPinNames())

            val pinIndex = pin.getPinIndex()

            //Hide inputs in use
            spinnerPinAdapter.hidePosition(pinIndex)

            inputSelector.setSelection(pinIndex)
            inputSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    spinnerPinAdapter.showPosition(pinIndex)
                    pin.setPinIndex(position)
                    toggle = false
                    setInputState(pin, false)
                    updateIO()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    inputSelector.setSelection(pin.getPinIndex())
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
                    setInputState(pin, false)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    modeSelector.setSelection(pin.getInputModeIndex())
                }
            }

            button.setOnTouchListener { v, event ->
                var buttonPressed = false
                if (event.action == MotionEvent.ACTION_DOWN) {
                    buttonPressed = true
                    button.setBackgroundColor(
                        ContextCompat.getColor(
                            context,
                            R.color.v1_on_push_button
                        )
                    )
                    toggle = !toggle
                    v.performClick()
                } else if (event.action == MotionEvent.ACTION_UP) {
                    buttonPressed = false
                    button.setBackgroundColor(
                        ContextCompat.getColor(
                            context,
                            R.color.v1_off_push_button
                        )
                    )
                }
                setInputState(pin, buttonPressed)
                true
            }
        }

        override fun onLongClick(v: View?): Boolean {
            TODO("Not yet implemented")
        }

        fun setInputState(pin: InputInterface, buttonPressed: Boolean) {
            val mode = pin.getInputModeIndex()
            val undefinedState = R.drawable.v1_digital_input_undefined
            val highState = R.drawable.v1_digital_input_on
            val lowState = R.drawable.v1_digital_input_off
            var state = undefinedState

            when (mode) {
                InputMode.PUSH_GND.ordinal -> {
                    state = if (buttonPressed) lowState else undefinedState
                }
                InputMode.PUSH_VCC.ordinal -> {
                    state = if (buttonPressed) highState else undefinedState
                }
                InputMode.PULL_UP.ordinal -> {
                    state = if (buttonPressed) lowState else highState

                }
                InputMode.PULL_DOWN.ordinal -> {
                    state = if (buttonPressed) highState else lowState

                }
                InputMode.TOGGLE.ordinal -> {
                    state = if (toggle) highState else lowState
                }
            }
            inputState.background = ContextCompat.getDrawable(context, state)

            val voltage = when(state){
                lowState -> {
                    pin.getVoltage(0)
                }
                highState -> {
                    pin.getVoltage(100)
                }
                else -> {
                    pin.getVoltage(50)
                }
            }
            pin.notifySignalInput(voltage)
        }

    }

    inner class AnalogInputViewHolder(itemView: View, private val context: Context) :
        InputViewHolder(itemView, context) {

        private val inputSelector: AppCompatSpinner = itemView.v1PinSelectorAnalogInput
        private val voltageLevel: AppCompatSeekBar = itemView.v1VoltageLevel
        private var voltageDisplay: AppCompatTextView = itemView.v1VoltageDisplay

        private var spinnerAdapter = HideAdapter(
            context,
            android.R.layout.simple_spinner_dropdown_item,
            InputPinV1ATmega328P.inputList.map { it.boardName }
        )

        init {
            inputSelector.adapter = spinnerAdapter
//            itemView.setOnLongClickListener(this)
        }

        override fun bindView(pin: InputInterface) {
            //Spinner list may change, update it!
            spinnerAdapter.clear()
            spinnerAdapter.addAll(pin.getPinNames())

            val pinIndex = pin.getPinIndex()

            //Hide inputs in use
            spinnerAdapter.hidePosition(pinIndex)

            inputSelector.setSelection(pinIndex)
            inputSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    spinnerAdapter.showPosition(pinIndex)
                    pin.setPinIndex(position)
                    updateIO()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    inputSelector.setSelection(pin.getPinIndex())
                }
            }

            voltageLevel.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    val voltage = pin.getVoltage(progress)
                    pin.notifySignalInput(voltage)
                    voltageDisplay.text = context.getString(R.string.voltage_display, voltage)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    //No need
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    //No need
                }

            })
        }

        override fun onLongClick(v: View?): Boolean {
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
        boundViewHolders[holder] = position
    }

    override fun onViewRecycled(holder: InputAdapterV1.InputViewHolder) {
        boundViewHolders.remove(holder)
    }

    fun updateIO() {
        /*
        This will update all visible inputs, just like calling notifyDataSetChanged,
        but it won't redraw the widgets
        Use notifyDataSetChanged only to add/remove new rows
         */
        for ((holder, position) in boundViewHolders) {
            holder.bindView(inputList[position])
        }
    }
}

class HideAdapter(context: Context, resource: Int, objects: List<Any?>) :
    ArrayAdapter<Any?>(context, resource, objects) {

    fun hidePosition(position: Int) {
        hidePosition.add(position)
    }

    fun showPosition(position: Int) {
        hidePosition.remove(position)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        if (position in hidePosition) {
            val text = TextView(context)
            text.visibility = View.GONE
            text.height = 0
            view = text
        } else {
            view = super.getDropDownView(position, null, parent)
        }
        return view
    }

    companion object {
        private val hidePosition: MutableSet<Int> = mutableSetOf()
    }
}