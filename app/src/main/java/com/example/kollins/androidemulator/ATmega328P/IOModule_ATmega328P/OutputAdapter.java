package com.example.kollins.androidemulator.ATmega328P.IOModule_ATmega328P;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.kollins.androidemulator.R;

import java.util.List;

/**
 * Created by kollins on 3/14/18.
 */

public class OutputAdapter extends BaseAdapter {

    private String[] pinArray;

    private OutputFragment outputFragment;
    private List<OutputPin> outputPins;

    public OutputAdapter(OutputFragment outputFragment, List<OutputPin> outputPins) {
        this.outputFragment = outputFragment;
        this.outputPins = outputPins;

        pinArray = outputFragment.getPinArray();
    }

    @Override
    public int getCount() {
        return outputPins != null ? outputPins.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return outputPins.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View view = LayoutInflater.from(outputFragment.getContext()).inflate(R.layout.output_pin,
                parent, false);

        Spinner pinSpinner = (Spinner) view.findViewById(R.id.pinSelector);
        TextView led = (TextView) view.findViewById(R.id.ledState);

        led.setText(outputFragment.getResources().getStringArray(R.array.ledText)[outputPins.get(position).getPinState()]);
        led.setBackgroundResource(OutputFragment.BACKGROUND_PIN[outputPins.get(position).getPinState()]);

        ArrayAdapter<String> pinSpinnerAdapter =
                new ArrayAdapter<String>(outputFragment.getContext(), android.R.layout.simple_spinner_item, pinArray);

        pinSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        pinSpinner.setAdapter(pinSpinnerAdapter);
        pinSpinner.setSelection(outputPins.get(position).getPinPositionSpinner());

        pinSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int positionSpinner, long id) {
                outputPins.get(position).setPin(pinArray[positionSpinner]);
                outputPins.get(position).setPinPositionSpinner(positionSpinner);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return view;
    }
}
