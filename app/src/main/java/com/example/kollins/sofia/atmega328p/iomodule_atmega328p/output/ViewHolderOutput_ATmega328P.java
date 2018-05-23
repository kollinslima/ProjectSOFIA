package com.example.kollins.sofia.atmega328p.iomodule_atmega328p.output;

import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.kollins.sofia.R;

/**
 * Created by kollins on 3/21/18.
 */

public class ViewHolderOutput_ATmega328P {
    final Spinner pinSpinner;
    final TextView led;

    public ViewHolderOutput_ATmega328P(View view){
        pinSpinner = (Spinner) view.findViewById(R.id.pinSelectorOutput);
        led = (TextView) view.findViewById(R.id.ledState);
    }
}
