/*
 * Copyright 2018
 * Kollins Lima (kollins.lima@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.kollins.sofia.atmega328p.iomodule_atmega328p.input;

import com.example.kollins.sofia.UCModule;
import com.example.kollins.sofia.ucinterfaces.IOModule;

/**
 * Created by kollins on 3/21/18.
 */

public class InputPin_ATmega328P {

    private static boolean[] hiZInput = UCModule.getHiZInput();
    private static boolean[] hiZRequestDone = UCModule.getHiZInput();

    public static final boolean DIGITAL_PIN = true;
    public static final boolean ANALOGIC_PIN = !DIGITAL_PIN;

    public static final double MAX_VOLTAGE_LOW_STATE = UCModule.getMaxVoltageLowState();
    public static final double MIN_VOLTAGE_HIGH_STATE = UCModule.getMinVoltageHighState();

    private static final int[] memoryAddress = UCModule.getInputMemoryAddress();
    private static final int[] memoryBitPosition = UCModule.getInputMemoryBitPosition();

    //All pins
    private String pin;
    private int pinSpinnerPosition;
    private int pinState;
    private boolean pinDescription;

    //Digital Pins
    private int pinMode;
    private int pinModePosition;

    //Analog Pins
    //private int seekBarProgress;    //%

    //DIGITAL PINS
    public InputPin_ATmega328P(String pin, int pinMode, boolean pinDescription) {
        this.pin = pin;
        this.pinDescription = pinDescription;
        this.pinSpinnerPosition = -1;

        this.pinMode = pinMode;
        this.pinModePosition = -1;
        this.pinState = IOModule.TRI_STATE;
    }

    //ANALOGIC PINS
    public InputPin_ATmega328P(String pin, boolean pinDescription) {
        this.pin = pin;
        this.pinDescription = pinDescription;
        this.pinSpinnerPosition = -1;
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

    public int getPinStateFromAnalog(double voltage) {
        if (voltage <= MAX_VOLTAGE_LOW_STATE){
            return IOModule.LOW_LEVEL;
        } else if (voltage >= MIN_VOLTAGE_HIGH_STATE){
            return IOModule.HIGH_LEVEL;
        } else {
            return IOModule.TRI_STATE;
        }
    }

    public synchronized void setHiZDone(boolean state, int position){
        if (position >= 0) {
            InputPin_ATmega328P.hiZRequestDone[position] = state;
        }
    }

    public synchronized boolean getHiZDone(int position){
        if (position < 0) {
            return true;
        }
        return InputPin_ATmega328P.hiZRequestDone[position];
    }
}
