package com.example.kollins.androidemulator.ATmega328P.IOModule_ATmega328P.Digital_Input;

import com.example.kollins.androidemulator.UCModule;
import com.example.kollins.androidemulator.uCInterfaces.IOModule;

/**
 * Created by kollins on 3/21/18.
 */

public class DigitalInputPin_ATmega328P {

    public static boolean[] buttonPressed = UCModule.getButtonPressed();

    private String pin;
    private int pinState;
    private int pinMode;
    private int pinSpinnerPosition;
    private int[] memoryAddress;
    private int[] memoryBitPosition;

    public DigitalInputPin_ATmega328P(String pin, int pinMode) {
        this.pin = pin;
        this.pinMode = pinMode;
        this.pinSpinnerPosition = -1;
        this.pinState = IOModule.TRI_STATE;

        memoryAddress = UCModule.getDigitalInputMemoryAddress();
        memoryBitPosition = UCModule.getDigitalInputMemoryBitPosition();
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

    public String getPin() {
        return pin;
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

}
