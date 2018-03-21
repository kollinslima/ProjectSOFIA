package com.example.kollins.androidemulator.ATmega328P.IOModule_ATmega328P.Output;

import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.kollins.androidemulator.R;

/**
 * Created by kollins on 3/21/18.
 */

public class ViewHolderOutput_ATmega328P {
    final Spinner pinSpinner;
    final TextView led;

    public ViewHolderOutput_ATmega328P(View view){
        pinSpinner = (Spinner) view.findViewById(R.id.pinSelector);
        led = (TextView) view.findViewById(R.id.ledState);
    }
}
