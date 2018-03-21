package com.example.kollins.androidemulator.ATmega328P.IOModule_ATmega328P.Digital_Input;

import com.example.kollins.androidemulator.UCModule;

/**
 * Created by kollins on 3/21/18.
 */

public class DigitalInputPin_ATmega328P {

    private String pin;
    private int pinMode;
    private int pinSpinnerPosition;
    private int[] memoryAddress;
    private int[] memoryBitPosition;

    public DigitalInputPin_ATmega328P(String pin, int pinMode) {
        this.pin = pin;
        this.pinMode = pinMode;
        this.pinSpinnerPosition = -1;

        memoryAddress = UCModule.getDigitalInputMemoryAddress();
        memoryBitPosition = UCModule.getDigitalInputMemoryBitPosition();
    }

    public int getPinMode(){
        return pinMode;
    }

    public void setPinMode(int pinMode){
        this.pinMode = pinMode;
    }

    public int getPinSpinnerPosition() {
        return pinSpinnerPosition;
    }

    public void setPinSpinnerPosition(int pinSpinnerPosition) {
        this.pinSpinnerPosition = pinSpinnerPosition;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public int getMemory(){
        return memoryAddress[pinSpinnerPosition];
    }

    public int getBitPosition(){
        return memoryBitPosition[pinSpinnerPosition];
    }
}
