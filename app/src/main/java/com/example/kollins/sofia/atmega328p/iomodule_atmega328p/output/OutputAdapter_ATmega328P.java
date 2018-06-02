/*
 * Copyright 2018
 * Kollins Lima (kollins.lima@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.kollins.sofia.atmega328p.iomodule_atmega328p.output;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.example.kollins.sofia.R;
import com.example.kollins.sofia.UCModule;

import java.util.List;

/**
 * Created by kollins on 3/14/18.
 */

public class OutputAdapter_ATmega328P extends BaseAdapter {

    private String[] pinArray;

    private OutputFragment_ATmega328P outputFragment;
    private List<OutputPin_ATmega328P> outputPins;

    public OutputAdapter_ATmega328P(OutputFragment_ATmega328P outputFragment, List<OutputPin_ATmega328P> outputPins) {
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

        View view;
        final ViewHolderOutput_ATmega328P holder;
        final OutputPin_ATmega328P pin = outputPins.get(position);
        ListView listView = (ListView) parent;
        ArrayAdapter<String> pinSpinnerAdapter =
                new ArrayAdapter<String>(outputFragment.getContext(), android.R.layout.simple_spinner_item, pinArray);

        if (convertView == null) {
            view = LayoutInflater.from(outputFragment.getContext()).inflate(R.layout.output_pin,
                    parent, false);

            holder = new ViewHolderOutput_ATmega328P(view);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolderOutput_ATmega328P) view.getTag();
        }

        holder.led.setText(outputFragment.getResources().getStringArray(R.array.ledText)[pin.getPinState(pin.getPinPositionSpinner())]);
        holder.led.setBackgroundResource(OutputFragment_ATmega328P.BACKGROUND_PIN[pin.getPinState(pin.getPinPositionSpinner())]);

        pinSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.pinSpinner.setAdapter(pinSpinnerAdapter);
        holder.pinSpinner.setSelection(pin.getPinPositionSpinner());

        holder.pinSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int positionSpinner, long id) {
                pin.setPin(pinArray[positionSpinner]);
                pin.setPinPositionSpinner(positionSpinner);

                //update view
                holder.led.setText(outputFragment.getResources().getStringArray(R.array.ledText)[pin.getPinState(pin.getPinPositionSpinner())]);
                holder.led.setBackgroundResource(OutputFragment_ATmega328P.BACKGROUND_PIN[pin.getPinState(pin.getPinPositionSpinner())]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Should never occur
            }
        });

        if (pin.getMeter()){
            holder.meter.setVisibility(View.VISIBLE);
        }

        view.setBackgroundColor(listView.isItemChecked(position) ?
                UCModule.getSelectedColor() :
                Color.TRANSPARENT);

        return view;
    }
}
