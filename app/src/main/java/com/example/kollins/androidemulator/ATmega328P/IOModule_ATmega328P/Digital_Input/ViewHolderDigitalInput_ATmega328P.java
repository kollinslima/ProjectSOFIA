package com.example.kollins.androidemulator.ATmega328P.IOModule_ATmega328P.Digital_Input;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;

import com.example.kollins.androidemulator.R;

/**
 * Created by kollins on 3/21/18.
 */

public class ViewHolderDigitalInput_ATmega328P {

    final Spinner pinSpinner;
    final Spinner pinModeSpinner;
    final Button pushButton;
    final ImageView inputPinState;

    public ViewHolderDigitalInput_ATmega328P(View view){
        pinSpinner = (Spinner) view.findViewById(R.id.pinSelectorDigitalInput);
        pinModeSpinner = (Spinner) view.findViewById(R.id.pinModeSelector);
        pushButton = (Button) view.findViewById(R.id.digitalPushButton);
        inputPinState = (ImageView) view.findViewById(R.id.digitalInputState);
    }
}
