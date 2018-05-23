package com.example.kollins.sofia.ATmega328P.IOModule_ATmega328P.Input;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.kollins.sofia.R;

/**
 * Created by kollins on 3/21/18.
 */

public class ViewHolderInput_ATmega328P {

    Spinner pinSpinner;

    //Digital Pin
    Spinner pinModeSpinner;
    Button pushButton;
    ImageView inputPinState;

    //Analogic Pin
    SeekBar voltageLevel;
    TextView voltageDisplay;

    public ViewHolderInput_ATmega328P(Spinner pinSpinner, Spinner pinModeSpinner, Button pushButton, ImageView inputPinState) {
        this.pinSpinner = pinSpinner;
        this.pinModeSpinner = pinModeSpinner;
        this.pushButton = pushButton;
        this.inputPinState = inputPinState;
    }

    public ViewHolderInput_ATmega328P(Spinner pinSpinner, SeekBar voltageLevel, TextView voltageDisplay) {
        this.pinSpinner = pinSpinner;
        this.voltageLevel = voltageLevel;
        this.voltageDisplay = voltageDisplay;
    }
}
