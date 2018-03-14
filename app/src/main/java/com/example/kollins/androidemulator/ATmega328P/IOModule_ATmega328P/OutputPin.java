package com.example.kollins.androidemulator.ATmega328P.IOModule_ATmega328P;


/**
 * Created by kollins on 3/14/18.
 */

public class OutputPin {
    private String[] pin;
    private int pinState;

    public OutputPin(String[] pin, int pinState){
        this.pin = pin;
        this.pinState = pinState;
    }

    public String[] getPins() {
        return pin;
    }

    public String getPin(int index) {
        return pin[index];
    }

    public void setPins(String[] pin) {
        this.pin = pin;
    }

    public int getPinState() {
        return pinState;
    }

    public void setPinState(int pinState) {
        this.pinState = pinState;
    }
}
