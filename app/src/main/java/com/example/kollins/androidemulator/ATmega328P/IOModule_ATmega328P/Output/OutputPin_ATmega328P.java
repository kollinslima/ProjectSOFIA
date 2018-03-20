package com.example.kollins.androidemulator.ATmega328P.IOModule_ATmega328P.Output;

import com.example.kollins.androidemulator.UCModule;

import java.io.Serializable;

/**
 * Created by kollins on 3/14/18.
 */

public class OutputPin_ATmega328P {

    public static final int LOW_LEVEL = 0;
    public static final int HIGH_LEVEL = 1;
    public static final int TRI_STATE = 2;

    private String pin;
    private int[] pinState = new int[UCModule.getPinArray().length];
    private int pinPositionSpinner;

    public OutputPin_ATmega328P(String pin, int pinPositionSpinner){
        this.pin = pin;
        this.pinPositionSpinner = pinPositionSpinner;

        resetPinState();
    }

    public OutputPin_ATmega328P(String pin, int pinState, int pinPositionSpinner){
        this.pin = pin;
        this.pinState[pinPositionSpinner] = pinState;
        this.pinPositionSpinner = pinPositionSpinner;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public int getPinState(int index) {
        return pinState[index];
    }

    public void setPinState(int pinState, int index) {
        this.pinState[index] = pinState;
    }

    public void setPinPositionSpinner(int position){
        pinPositionSpinner = position;
    }

    public int getPinPositionSpinner(){
        return pinPositionSpinner;
    }

    public void resetPinState(){
        for (int i = 0; i < pinState.length; i++){
            pinState[i] = TRI_STATE;
        }
    }
}
