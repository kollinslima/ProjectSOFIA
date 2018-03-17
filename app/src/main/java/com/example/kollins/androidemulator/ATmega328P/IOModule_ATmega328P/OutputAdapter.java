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
import com.example.kollins.androidemulator.UCModule;

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

        pinArray = UCModule.getPinArray();
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

        final OutputPin pin = outputPins.get(position);

        led.setText(outputFragment.getResources().getStringArray(R.array.ledText)[pin.getPinState(pin.getPinPositionSpinner())]);
        led.setBackgroundResource(OutputFragment.BACKGROUND_PIN[pin.getPinState(pin.getPinPositionSpinner())]);

        ArrayAdapter<String> pinSpinnerAdapter =
                new ArrayAdapter<String>(outputFragment.getContext(), android.R.layout.simple_spinner_item, pinArray);

        pinSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        pinSpinner.setAdapter(pinSpinnerAdapter);
        pinSpinner.setSelection(pin.getPinPositionSpinner());

        pinSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int positionSpinner, long id) {
                pin.setPin(pinArray[positionSpinner]);
                pin.setPinPositionSpinner(positionSpinner);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return view;
    }
}
