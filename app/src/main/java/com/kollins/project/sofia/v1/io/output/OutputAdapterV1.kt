package com.kollins.project.sofia.v1.io.output

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kollins.project.sofia.R
import com.kollins.project.sofia.interfaces.io.OutputInterface
import com.kollins.project.sofia.interfaces.io.OutputState
import com.kollins.project.sofia.v1.io.output.atmega328p.OutputPinV1ATmega328P
import kotlinx.android.synthetic.main.v1_output_pin.view.*

class OutputAdapterV1(private var outputList: MutableList<OutputInterface>) :
    RecyclerView.Adapter<OutputAdapterV1.ViewHolder>() {

    class ViewHolder(itemView: View, private val context: Context) : RecyclerView.ViewHolder(itemView) {
        private val spinner: AppCompatSpinner = itemView.v1PinSelectorOutput
        val meterLayout: LinearLayoutCompat = itemView.v1Meter
        val freqMeter: AppCompatTextView = itemView.v1Frequency
        val dcMeter: AppCompatTextView = itemView.v1DutyCycle
        private val led: AppCompatTextView = itemView.v1LedState

        init {
            spinner.adapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, OutputPinV1ATmega328P.pinNames)
        }

        fun bindView (pin : OutputInterface) {
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
//                    this@OutputAdapterV1.notifyDataSetChanged()
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }
            }

            val resources = context.resources
            when(pin.getPinState()) {
                OutputState.HIGH -> {
                    led.text = resources.getString(R.string.logic_high)
                    led.background = ContextCompat.getDrawable(context, R.drawable.v1_digital_output_on_led)
                }
                OutputState.LOW -> {
                    led.text = resources.getString(R.string.logic_low)
                    led.background = ContextCompat.getDrawable(context, R.drawable.v1_digital_output_off_led)
                }
                else -> {
                    led.text = resources.getString(R.string.logic_tri_state)
                    led.background = ContextCompat.getDrawable(context, R.drawable.v1_digital_output_tri_state_led)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        Log.d("BIND", "COUNT: " + outputList.size);
        return outputList.size
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.d("BIND", "CREATE VIEW HOLDER");
        val view = LayoutInflater.from(parent.context).inflate(R.layout.v1_output_pin, parent, false)
        return ViewHolder(view, parent.context)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d("BIND", "BIND VIEW");
        holder.bindView(outputList[position])
    }
}