package com.example.kollins.androidemulator.ATmega328P.IOModule_ATmega328P;

import java.io.Serializable;

/**
 * Created by kollins on 3/14/18.
 */

public class OutputPin {
    private String pin;
    private int pinState;
    private int pinPositionSpinner;

    public OutputPin(String pin, int pinState, int pinPositionSpinner){
        this.pin = pin;
        this.pinState = pinState;
        this.pinPositionSpinner = pinPositionSpinner;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public int getPinState() {
        return pinState;
    }

    public void setPinState(int pinState) {
        this.pinState = pinState;
    }

    public void setPinPositionSpinner(int position){
        pinPositionSpinner = position;
    }

    public int getPinPositionSpinner(){
        return pinPositionSpinner;
    }
}
