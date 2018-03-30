package com.example.kollins.androidemulator.ATmega328P.IOModule_ATmega328P.Input;

import com.example.kollins.androidemulator.UCModule;
import com.example.kollins.androidemulator.uCInterfaces.IOModule;

/**
 * Created by kollins on 3/21/18.
 */

public class InputPin_ATmega328P {

    private static boolean[] hiZInput = UCModule.getHiZInput();

    public static final boolean DIGITAL_PIN = true;
    public static final boolean ANALOGIC_PIN = !DIGITAL_PIN;

    //All pins
    private String pin;
    private int pinSpinnerPosition;
    private int pinState;
    private boolean pinDescription;

    //Digital Pins
    private int pinMode;
    private int pinModePosition;
    private int[] memoryAddress;
    private int[] memoryBitPosition;

    //Analog Pins
    private int seekBarProgress;    //%

    //DIGITAL PINS
    public InputPin_ATmega328P(String pin, int pinMode, boolean pinDescription) {
        this.pin = pin;
        this.pinDescription = pinDescription;
        this.pinSpinnerPosition = -1;

        this.pinMode = pinMode;
        this.pinModePosition = -1;
        this.pinState = IOModule.TRI_STATE;

        memoryAddress = UCModule.getDigitalInputMemoryAddress();
        memoryBitPosition = UCModule.getDigitalInputMemoryBitPosition();
    }

    //ANALOGIC PINS
    public InputPin_ATmega328P(String pin, boolean pinDescription) {
        this.pin = pin;
        this.pinDescription = pinDescription;
        this.pinSpinnerPosition = -1;

        this.seekBarProgress = 0;
    }

    public int getPinMode() {
        return pinMode;
    }

    public void setPinMode(int pinMode) {
        this.pinMode = pinMode;
    }

    public int getPinSpinnerPosition() {
        return pinSpinnerPosition;
    }

    public void setPinSpinnerPosition(int pinSpinnerPosition) {
        this.pinSpinnerPosition = pinSpinnerPosition;
    }

    public int getPinModePosition() {
        return pinModePosition;
    }

    public void setPinModePosition(int pinModePosition) {
        this.pinModePosition = pinModePosition;
    }

    public String getPin() {
        return pin;
    }

    public boolean getDescription(){
        return pinDescription;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public void setPinState(int pinState) {
        this.pinState = pinState;
    }

    public int getPinState() {
        return pinState;
    }

    public int getMemory() {
        return memoryAddress[pinSpinnerPosition];
    }

    public int getBitPosition() {
        return memoryBitPosition[pinSpinnerPosition];
    }

    public synchronized void setHiZ(boolean state, int position){
        if (position >= 0) {
            InputPin_ATmega328P.hiZInput[position] = state;
        }
    }

    public synchronized boolean getHiZ(int position){
        if (position < 0) {
            return true;
        }
        return InputPin_ATmega328P.hiZInput[position];
    }

}
