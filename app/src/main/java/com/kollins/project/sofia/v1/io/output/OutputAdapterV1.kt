package com.kollins.project.sofia.v1.io.output

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kollins.project.sofia.R
import com.kollins.project.sofia.interfaces.io.OutputInterface
import com.kollins.project.sofia.interfaces.io.OutputState
import com.kollins.project.sofia.v1.io.output.atmega328p.OutputPinV1ATmega328P
import kotlinx.android.synthetic.main.v1_output_pin.view.*

class OutputAdapterV1(private val outputList: MutableList<OutputInterface>) :
    RecyclerView.Adapter<OutputAdapterV1.ViewHolder>() {

    private var boundViewHolders = mutableMapOf<ViewHolder, Int>()

    inner class ViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {
        private val spinner: AppCompatSpinner = itemView.v1PinSelectorOutput
//        val meterLayout: LinearLayoutCompat = itemView.v1Meter
//        val freqMeter: AppCompatTextView = itemView.v1Frequency
//        val dcMeter: AppCompatTextView = itemView.v1DutyCycle
        private val led: AppCompatTextView = itemView.v1LedState

        init {
            spinner.adapter = ArrayAdapter<String>(
                context,
                android.R.layout.simple_spinner_dropdown_item,
                OutputPinV1ATmega328P.pinNames
            )
        }

        fun bindView(pin: OutputInterface) {
            pin.updatePinState()

            spinner.setSelection(pin.getOutputIndex())
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    pin.setOutputIndex(position)
                    this@OutputAdapterV1.notifyDataSetChanged()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }
            }

            when (pin.getPinState()) {
                OutputState.HIGH -> {
                    led.background =
                        ContextCompat.getDrawable(context, R.drawable.v1_digital_output_on_led)
                }
                OutputState.LOW -> {
                    led.background =
                        ContextCompat.getDrawable(context, R.drawable.v1_digital_output_off_led)
                }
                else -> {
                    led.background = ContextCompat.getDrawable(
                        context,
                        R.drawable.v1_digital_output_tri_state_led
                    )
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return outputList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.v1_output_pin, parent, false)
        return ViewHolder(view, parent.context)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindView(outputList[position])
        boundViewHolders[holder] = position
    }

    override fun onViewRecycled(holder: ViewHolder) {
        boundViewHolders.remove(holder)
    }

    fun updateIO() {
        /*
        This will update all visible outputs, just like calling notifyDataSetChanged,
        but it won't redraw the widgets, so a opened spinner will remain opened while
        the LED is blinking.
        Use notifyDataSetChanged only to add/remove new rows
         */
        for ((holder, position) in boundViewHolders) {
            holder.bindView(outputList[position])
        }
    }
}