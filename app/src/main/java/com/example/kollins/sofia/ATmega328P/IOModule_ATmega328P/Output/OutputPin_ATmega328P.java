package com.example.kollins.sofia.ATmega328P.IOModule_ATmega328P.Output;

import com.example.kollins.sofia.UCModule;
import com.example.kollins.sofia.uCInterfaces.IOModule;

import java.io.Serializable;

/**
 * Created by kollins on 3/14/18.
 */

public class OutputPin_ATmega328P {

    private String pin;
    private int[] pinState = new int[UCModule.getPinArray().length];
    private int pinPositionSpinner;

    public OutputPin_ATmega328P(String pin, int pinPositionSpinner){
        this.pin = pin;
        this.pinPositionSpinner = pinPositionSpinner;

        resetPinState();
    }

    public OutputPin_ATmega328P(String pin, int pinPositionSpinner, int[] pinState){
        this.pin = pin;
        this.pinPositionSpinner = pinPositionSpinner;
        this.pinState = pinState;
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
            pinState[i] = IOModule.TRI_STATE;
        }
    }

    public int[] getStates() {
        return pinState;
    }
}
