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

package com.example.kollins.sofia.atmega328p.iomodule_atmega328p;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.kollins.sofia.atmega328p.DataMemory_ATmega328P;
import com.example.kollins.sofia.atmega328p.USART_ATmega328P;
import com.example.kollins.sofia.atmega328p.iomodule_atmega328p.input.InputFragment_ATmega328P;
import com.example.kollins.sofia.atmega328p.iomodule_atmega328p.input.InputPin_ATmega328P;
import com.example.kollins.sofia.atmega328p.iomodule_atmega328p.output.OutputFragment_ATmega328P;
import com.example.kollins.sofia.atmega328p.iomodule_atmega328p.output.OutputPin_ATmega328P;
import com.example.kollins.sofia.atmega328p.Timer0_ATmega328P;
import com.example.kollins.sofia.atmega328p.Timer1_ATmega328P;
import com.example.kollins.sofia.atmega328p.Timer2_ATmega328P;
import com.example.kollins.sofia.UCModule;
import com.example.kollins.sofia.UCModule_View;
import com.example.kollins.sofia.ucinterfaces.InputFragment;
import com.example.kollins.sofia.ucinterfaces.IOModule;
import com.example.kollins.sofia.ucinterfaces.OutputFragment;

import java.util.List;
import java.util.Objects;

/**
 * Created by kollins on 3/23/18.
 */

public class IOModule_ATmega328P extends Handler implements IOModule {

    private final int OC0B_PIN_POSITION = 5;
    private final int OC0A_PIN_POSITION = 6;

    private final int OC1B_PIN_POSITION = 10;
    private final int OC1A_PIN_POSITION = 9;

    private final int OC2B_PIN_POSITION = 3;
    private final int OC2A_PIN_POSITION = 11;

    private final int OC0B_BIT_POSITION = 5;
    private final int OC0A_BIT_POSITION = 6;

    private final int OC1B_BIT_POSITION = 2;
    private final int OC1A_BIT_POSITION = 1;

    private final int OC2B_BIT_POSITION = 3;
    private final int OC2A_BIT_POSITION = 3;

    private byte valueRead_B;
    private byte configRead_B;
    private byte valueRead_C;
    private byte configRead_C;
    private byte valueRead_D;
    private byte configRead_D;

    private long time_B, time_C, time_D;

    private static UCModule.UCHandler uCHandler;
    private OutputFragment_ATmega328P outputFragment;
    private InputFragment_ATmega328P inputFragment;

    public IOModule_ATmega328P(OutputFragment outputFragment,
                               InputFragment inputFragment) {

        this.uCHandler = uCHandler;
        this.outputFragment = (OutputFragment_ATmega328P) outputFragment;
        this.inputFragment = (InputFragment_ATmega328P) inputFragment;
    }

    @Override
    public void handleMessage(Message msg) {

        List<OutputPin_ATmega328P> outputPins = outputFragment.getOutputPins();

        switch (msg.what) {
            case PORTB_EVENT:

                valueRead_B = msg.getData().getByte(IOModule.VALUE_IOMESSAGE);
                configRead_B = msg.getData().getByte(IOModule.CONFIG_IOMESSAGE);
                time_B = msg.getData().getLong(IOModule.TIME);
                new PortBUpdateView().execute(outputPins);

                break;

            case PORTC_EVENT:

                valueRead_C = msg.getData().getByte(IOModule.VALUE_IOMESSAGE);
                configRead_C = msg.getData().getByte(IOModule.CONFIG_IOMESSAGE);
                time_C = msg.getData().getLong(IOModule.TIME);
                new PortCUpdateView().execute(outputPins);

                break;

            case PORTD_EVENT:

                valueRead_D = msg.getData().getByte(IOModule.VALUE_IOMESSAGE);
                configRead_D = msg.getData().getByte(IOModule.CONFIG_IOMESSAGE);
                time_D = msg.getData().getLong(IOModule.TIME);
                new PortDUpdateView().execute(outputPins);


                break;

            default:
                //There are no other events
                break;
        }
    }

    public static void sendShortCircuit() {
        UCModule_View.sendShortCircuit();
    }

    @Override
    public boolean checkShortCircuit() {
        List<InputPin_ATmega328P> inputPins = inputFragment.getPinList();
        List<OutputPin_ATmega328P> outputPins = outputFragment.getPinList();

        try {
            /****************Check short circuit between inputs*****************/
            if (inputPins.size() > 0) {
                if (checkInputShortCircuit(inputPins)) {
                    return true;
                }

                /****************Check short circuit between input and output*****************/
                if (outputPins.size() > 0) {
                    if (checkInputOutputShortCircuit(inputPins, outputPins)) {

                        return true;
                    }
                }
            }
        } catch (NullPointerException e) {
            //input/output list is null
        }
        return false;
    }

    private boolean checkInputShortCircuit(List<InputPin_ATmega328P> inputPins) throws NullPointerException {
        InputPin_ATmega328P pi, pj;
        for (int i = 0; i < inputPins.size(); i++) {
            for (int j = i + 1; j < inputPins.size(); j++) {
                pi = inputPins.get(i);
                pj = inputPins.get(j);


                if (pi.getPinSpinnerPosition() == pj.getPinSpinnerPosition()) {
                    if (pi.getHiZ(pi.getPinSpinnerPosition())) {
                        continue;
                    }
                    if (pi.getPinState() == IOModule.TRI_STATE || pj.getPinState() == IOModule.TRI_STATE) {
                        continue;
                    }
                    if (pi.getPinState() != pj.getPinState()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private synchronized boolean checkInputOutputShortCircuit(List<InputPin_ATmega328P> inputPins, List<OutputPin_ATmega328P> outputPins) throws NullPointerException {
        InputPin_ATmega328P pi;
        OutputPin_ATmega328P pk;

        for (int i = 0; i < inputPins.size(); i++) {
            for (int k = 0; k < outputPins.size(); k++) {
                pi = inputPins.get(i);
                pk = outputPins.get(k);
                if (pi.getPinSpinnerPosition() == pk.getPinPositionSpinner()) {

                    if (pi.getPinState() == IOModule.TRI_STATE ||
                            pk.getPinState(pk.getPinPositionSpinner()) == IOModule.TRI_STATE) {
                        continue;
                    }
                    //No short-circuit if measuring the output
                    if (Objects.equals(pi.getPin(), pk.getPin()) &&
                            outputFragment.isMeasrureOutput(pi.getMemory() + 1, pi.getBitPosition())) {
                        continue;
                    }
                    if (pi.getPinState() != pk.getPinState(pk.getPinPositionSpinner())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public void getPINConfig() {
        inputFragment.getPINConfig();
    }

    public void setOC0A(int stateOC0A, long time) {

        //Affected only if it is an output.
        if (outputFragment.isMeasrureOutput(DataMemory_ATmega328P.DDRD_ADDR, 6)) {
            return;
        }

        if (OutputFragment_ATmega328P.pinbuffer[OC0A_PIN_POSITION] != stateOC0A) {
            OutputFragment_ATmega328P.pinbuffer[OC0A_PIN_POSITION] = stateOC0A;

            if (OutputFragment_ATmega328P.evalFreq[OC0A_PIN_POSITION]) {
//                Log.d("Freq", "Frequency: " + (Math.pow(10, 10) / (time - OutputFragment_ATmega328P.oldTime[OC0A_PIN_POSITION])));
                double period = time - OutputFragment_ATmega328P.oldTime[OC0A_PIN_POSITION];
                OutputFragment_ATmega328P.frequencyBuffer[OC0A_PIN_POSITION] = Math.pow(10, 10) / period;
                OutputFragment_ATmega328P.dutyCycleBuffer[OC0A_PIN_POSITION] = (1 - ((time - OutputFragment_ATmega328P.dcTimeLow[OC0A_PIN_POSITION]) / period))*100;

                OutputFragment_ATmega328P.oldTime[OC0A_PIN_POSITION] = time;
                OutputFragment_ATmega328P.evalFreq[OC0A_PIN_POSITION] = false;
            } else {
                OutputFragment_ATmega328P.evalFreq[OC0A_PIN_POSITION] = true;
                OutputFragment_ATmega328P.dcTimeLow[OC0A_PIN_POSITION] = time;
            }
        }

        outputFragment.writeFeedback(DataMemory_ATmega328P.PIND_ADDR, OC0A_BIT_POSITION, OutputFragment_ATmega328P.pinbuffer[OC0A_PIN_POSITION] != 0);

        try {
            int index = 0;
            for (OutputPin_ATmega328P p : outputFragment.getOutputPins()) {
                if (p.getPinPositionSpinner() == OC0A_PIN_POSITION) {
                    outputFragment.updateView(index);
                    break;
                }
                index += 1;
            }

            if (checkInputOutputShortCircuit(inputFragment.getPinList(), outputFragment.getOutputPins())) {
                sendShortCircuit();
            }
        } catch (NullPointerException e) {
            Log.e(UCModule.MY_LOG_TAG, "ERROR: setOC0A (IOModule) -> output list is null", e);
        }

    }

    public void setOC0B(int stateOC0B, long time) {

        //Affected only if it is an output.
        if (outputFragment.isMeasrureOutput(DataMemory_ATmega328P.DDRD_ADDR, 5)) {
            return;
        }

        if (OutputFragment_ATmega328P.pinbuffer[OC0B_PIN_POSITION] != stateOC0B) {
            OutputFragment_ATmega328P.pinbuffer[OC0B_PIN_POSITION] = stateOC0B;

            if (OutputFragment_ATmega328P.evalFreq[OC0B_PIN_POSITION]) {
//                Log.d("Freq", "Frequency: " + (Math.pow(10, 10) / (time - OutputFragment_ATmega328P.oldTime[OC0B_PIN_POSITION])));
                double period = time - OutputFragment_ATmega328P.oldTime[OC0B_PIN_POSITION];
                OutputFragment_ATmega328P.frequencyBuffer[OC0B_PIN_POSITION] = Math.pow(10, 10) / period;
                OutputFragment_ATmega328P.dutyCycleBuffer[OC0B_PIN_POSITION] = (1 - ((time - OutputFragment_ATmega328P.dcTimeLow[OC0B_PIN_POSITION]) / period))*100;

                OutputFragment_ATmega328P.oldTime[OC0B_PIN_POSITION] = time;
                OutputFragment_ATmega328P.evalFreq[OC0B_PIN_POSITION] = false;
            } else {
                OutputFragment_ATmega328P.evalFreq[OC0B_PIN_POSITION] = true;
                OutputFragment_ATmega328P.dcTimeLow[OC0B_PIN_POSITION] = time;
            }
        }

        outputFragment.writeFeedback(DataMemory_ATmega328P.PIND_ADDR, OC0B_BIT_POSITION, OutputFragment_ATmega328P.pinbuffer[OC0B_PIN_POSITION] != 0);

        try {
            int index = 0;
            for (OutputPin_ATmega328P p : outputFragment.getOutputPins()) {
                if (p.getPinPositionSpinner() == OC0B_PIN_POSITION) {
                    outputFragment.updateView(index);
                    break;
                }
                index += 1;
            }

            if (checkInputOutputShortCircuit(inputFragment.getPinList(), outputFragment.getOutputPins())) {
                sendShortCircuit();
            }
        } catch (NullPointerException e) {
            Log.e(UCModule.MY_LOG_TAG, "ERROR: setOC0B (IOModule) -> output list is null", e);
        }

    }

    public void setOC1A(int stateOC1A, long time) {
        //Affected only if it is an output.
        if (outputFragment.isMeasrureOutput(DataMemory_ATmega328P.DDRB_ADDR, 1)) {
            return;
        }

        if (OutputFragment_ATmega328P.pinbuffer[OC1A_PIN_POSITION] != stateOC1A) {
            OutputFragment_ATmega328P.pinbuffer[OC1A_PIN_POSITION] = stateOC1A;

            if (OutputFragment_ATmega328P.evalFreq[OC1A_PIN_POSITION]) {
//                Log.d("Freq", "Frequency: " + (Math.pow(10, 10) / (time - OutputFragment_ATmega328P.oldTime[OC1A_PIN_POSITION])));
                double period = time - OutputFragment_ATmega328P.oldTime[OC1A_PIN_POSITION];
                OutputFragment_ATmega328P.frequencyBuffer[OC1A_PIN_POSITION] = Math.pow(10, 10) / period;
                OutputFragment_ATmega328P.dutyCycleBuffer[OC1A_PIN_POSITION] = (1 - ((time - OutputFragment_ATmega328P.dcTimeLow[OC1A_PIN_POSITION]) / period))*100;

                OutputFragment_ATmega328P.oldTime[OC1A_PIN_POSITION] = time;
                OutputFragment_ATmega328P.evalFreq[OC1A_PIN_POSITION] = false;
            } else {
                OutputFragment_ATmega328P.evalFreq[OC1A_PIN_POSITION] = true;
                OutputFragment_ATmega328P.dcTimeLow[OC1A_PIN_POSITION] = time;
            }
        }

        outputFragment.writeFeedback(DataMemory_ATmega328P.PINB_ADDR, OC1A_BIT_POSITION, OutputFragment_ATmega328P.pinbuffer[OC1A_PIN_POSITION] != 0);

        try {
            int index = 0;
            for (OutputPin_ATmega328P p : outputFragment.getOutputPins()) {
                if (p.getPinPositionSpinner() == OC1A_PIN_POSITION) {
                    outputFragment.updateView(index);
                    break;
                }
                index += 1;
            }

            if (checkInputOutputShortCircuit(inputFragment.getPinList(), outputFragment.getOutputPins())) {
                sendShortCircuit();
            }
        } catch (NullPointerException e) {
            Log.e(UCModule.MY_LOG_TAG, "ERROR: setOC1A (IOModule) -> output list is null", e);
        }
    }

    public void setOC1B(int stateOC1B, long time) {
        //Affected only if it is an output.
        if (outputFragment.isMeasrureOutput(DataMemory_ATmega328P.DDRB_ADDR, 2)) {
            return;
        }

        if (OutputFragment_ATmega328P.pinbuffer[OC1B_PIN_POSITION] != stateOC1B) {
            OutputFragment_ATmega328P.pinbuffer[OC1B_PIN_POSITION] = stateOC1B;

            if (OutputFragment_ATmega328P.evalFreq[OC1B_PIN_POSITION]) {
//                Log.d("Freq", "Frequency: " + (Math.pow(10, 10) / (time - OutputFragment_ATmega328P.oldTime[OC1B_PIN_POSITION])));
                double period = time - OutputFragment_ATmega328P.oldTime[OC1B_PIN_POSITION];
                OutputFragment_ATmega328P.frequencyBuffer[OC1B_PIN_POSITION] = Math.pow(10, 10) / period;
                OutputFragment_ATmega328P.dutyCycleBuffer[OC1B_PIN_POSITION] = (1 - ((time - OutputFragment_ATmega328P.dcTimeLow[OC1B_PIN_POSITION]) / period))*100;

                OutputFragment_ATmega328P.oldTime[OC1B_PIN_POSITION] = time;
                OutputFragment_ATmega328P.evalFreq[OC1B_PIN_POSITION] = false;
            } else {
                OutputFragment_ATmega328P.evalFreq[OC1B_PIN_POSITION] = true;
                OutputFragment_ATmega328P.dcTimeLow[OC1B_PIN_POSITION] = time;
            }
        }

        outputFragment.writeFeedback(DataMemory_ATmega328P.PINB_ADDR, OC1B_BIT_POSITION, OutputFragment_ATmega328P.pinbuffer[OC1B_PIN_POSITION] != 0);

        try {
            int index = 0;
            for (OutputPin_ATmega328P p : outputFragment.getOutputPins()) {
                if (p.getPinPositionSpinner() == OC1B_PIN_POSITION) {
                    outputFragment.updateView(index);
                    break;
                }
                index += 1;
            }

            if (checkInputOutputShortCircuit(inputFragment.getPinList(), outputFragment.getOutputPins())) {
                sendShortCircuit();
            }
        } catch (NullPointerException e) {
            Log.e(UCModule.MY_LOG_TAG, "ERROR: setOC1B (IOModule) -> output list is null", e);
        }
    }

    public void setOC2A(int stateOC2A, long time) {
        //Affected only if it is an output.
        if (outputFragment.isMeasrureOutput(DataMemory_ATmega328P.DDRB_ADDR, 3)) {
            return;
        }

        if (OutputFragment_ATmega328P.pinbuffer[OC2A_PIN_POSITION] != stateOC2A) {
            OutputFragment_ATmega328P.pinbuffer[OC2A_PIN_POSITION] = stateOC2A;

            if (OutputFragment_ATmega328P.evalFreq[OC2A_PIN_POSITION]) {
//                Log.d("Freq", "Frequency: " + (Math.pow(10, 10) / (time - OutputFragment_ATmega328P.oldTime[OC2A_PIN_POSITION])));
                double period = time - OutputFragment_ATmega328P.oldTime[OC2A_PIN_POSITION];
                OutputFragment_ATmega328P.frequencyBuffer[OC2A_PIN_POSITION] = Math.pow(10, 10) / period;
                OutputFragment_ATmega328P.dutyCycleBuffer[OC2A_PIN_POSITION] = (1 - ((time - OutputFragment_ATmega328P.dcTimeLow[OC2A_PIN_POSITION]) / period))*100;

                OutputFragment_ATmega328P.oldTime[OC2A_PIN_POSITION] = time;
                OutputFragment_ATmega328P.evalFreq[OC2A_PIN_POSITION] = false;
            } else {
                OutputFragment_ATmega328P.evalFreq[OC2A_PIN_POSITION] = true;
                OutputFragment_ATmega328P.dcTimeLow[OC2A_PIN_POSITION] = time;
            }
        }

        outputFragment.writeFeedback(DataMemory_ATmega328P.PINB_ADDR, OC2A_BIT_POSITION, OutputFragment_ATmega328P.pinbuffer[OC2A_PIN_POSITION] != 0);

        try {
            int index = 0;
            for (OutputPin_ATmega328P p : outputFragment.getOutputPins()) {
                if (p.getPinPositionSpinner() == OC2A_PIN_POSITION) {
                    outputFragment.updateView(index);
                    break;
                }
                index += 1;
            }

            if (checkInputOutputShortCircuit(inputFragment.getPinList(), outputFragment.getOutputPins())) {
                sendShortCircuit();
            }
        } catch (NullPointerException e) {
            Log.e(UCModule.MY_LOG_TAG, "ERROR: setOC2A (IOModule) -> output list is null", e);
        }
    }

    public void setOC2B(int stateOC2B, long time) {
        //Affected only if it is an output.
        if (outputFragment.isMeasrureOutput(DataMemory_ATmega328P.DDRD_ADDR, 3)) {
            return;
        }

        if (OutputFragment_ATmega328P.pinbuffer[OC2B_PIN_POSITION] != stateOC2B) {
            OutputFragment_ATmega328P.pinbuffer[OC2B_PIN_POSITION] = stateOC2B;

            if (OutputFragment_ATmega328P.evalFreq[OC2B_PIN_POSITION]) {
//                Log.d("Freq", "Frequency: " + (Math.pow(10, 10) / (time - OutputFragment_ATmega328P.oldTime[OC2B_PIN_POSITION])));
                double period = time - OutputFragment_ATmega328P.oldTime[OC2B_PIN_POSITION];
                OutputFragment_ATmega328P.frequencyBuffer[OC2B_PIN_POSITION] = Math.pow(10, 10) / period;
                OutputFragment_ATmega328P.dutyCycleBuffer[OC2B_PIN_POSITION] = (1 - ((time - OutputFragment_ATmega328P.dcTimeLow[OC2B_PIN_POSITION]) / period))*100;

                OutputFragment_ATmega328P.oldTime[OC2B_PIN_POSITION] = time;
                OutputFragment_ATmega328P.evalFreq[OC2B_PIN_POSITION] = false;
            } else {
                OutputFragment_ATmega328P.evalFreq[OC2B_PIN_POSITION] = true;
                OutputFragment_ATmega328P.dcTimeLow[OC2B_PIN_POSITION] = time;
            }
        }

        outputFragment.writeFeedback(DataMemory_ATmega328P.PIND_ADDR, OC2B_BIT_POSITION, OutputFragment_ATmega328P.pinbuffer[OC2B_PIN_POSITION] != 0);

        try {
            int index = 0;
            for (OutputPin_ATmega328P p : outputFragment.getOutputPins()) {
                if (p.getPinPositionSpinner() == OC2B_PIN_POSITION) {
                    outputFragment.updateView(index);
                    break;
                }
                index += 1;
            }

            if (checkInputOutputShortCircuit(inputFragment.getPinList(), outputFragment.getOutputPins())) {
                sendShortCircuit();
            }
        } catch (NullPointerException e) {
            Log.e(UCModule.MY_LOG_TAG, "ERROR: setOC2B (IOModule) -> output list is null", e);
        }
    }

    private class PortBUpdateView extends AsyncTask<List<OutputPin_ATmega328P>, Integer, Void> {

        private int index;
        private boolean digitalPINState;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            index = 0;
        }

        @Override
        protected Void doInBackground(List<OutputPin_ATmega328P>... pins) {
            Thread.currentThread().setName("ASYNC_PORTB_UPDATE");

            //Pin8 - Pin13
            for (int i = 8, bitPosition = 0; i <= 13; i++, bitPosition++) {

                //Is input?
                if ((0x01 & (configRead_B >> bitPosition)) == 0) {

                    digitalPINState = inputFragment.getPINState(DataMemory_ATmega328P.PINB_ADDR, bitPosition);

                    if (((0x01 & (valueRead_B >> bitPosition)) == 1) && outputFragment.isPullUpEnabled()) {

                        if (!digitalPINState && inputFragment.isPinHiZ(i)) {
                            inputFragment.inputRequest_outputChanel(IOModule.HIGH_LEVEL, DataMemory_ATmega328P.PINB_ADDR, bitPosition, "Pin" + i);
//                            return null;
                        }

                        OutputFragment_ATmega328P.pinbuffer[i] = digitalPINState ? 1 : 0;

                    } else {
//                        OutputFragment_ATmega328P.pinbuffer[i] = digitalPINState ? 1 : 0;
                        if (!inputFragment.isPinHiZ(i)) {
//                            Log.i(UCModule.MY_LOG_TAG, "Button pressed");
                            OutputFragment_ATmega328P.pinbuffer[i] = digitalPINState ? 1 : 0;
                        } else {
//                            Log.i(UCModule.MY_LOG_TAG, "Button not pressed");
                            OutputFragment_ATmega328P.pinbuffer[i] = IOModule.TRI_STATE;
                        }
                    }
                }
                //Is output!
                else {
                    //Check MUX Timer1 and Timer 2
                    if ((i < 9 || i > 11)
                            || (i == 9 && !Timer1_ATmega328P.timerOutputControl_OC1A)
                            || (i == 10 && !Timer1_ATmega328P.timerOutputControl_OC1B)
                            || (i == 11 && !Timer2_ATmega328P.timerOutputControl_OC2A)) {

                        if (OutputFragment_ATmega328P.pinbuffer[i] != (0x01 & (valueRead_B >> bitPosition))) {
                            OutputFragment_ATmega328P.pinbuffer[i] = (0x01 & (valueRead_B >> bitPosition));

                            if (OutputFragment_ATmega328P.evalFreq[i]) {
//                                Log.d("Freq", "Frequency: " + (Math.pow(10, 10) / (time_B - OutputFragment_ATmega328P.oldTime[i])));

                                double period = time_B - OutputFragment_ATmega328P.oldTime[i];
                                OutputFragment_ATmega328P.frequencyBuffer[i] = Math.pow(10, 10) / period;
                                OutputFragment_ATmega328P.dutyCycleBuffer[i] = (1 - ((time_B - OutputFragment_ATmega328P.dcTimeLow[i]) / period))*100;

                                OutputFragment_ATmega328P.oldTime[i] = time_B;
                                OutputFragment_ATmega328P.evalFreq[i] = false;
                            } else {
                                OutputFragment_ATmega328P.evalFreq[i] = true;
                                OutputFragment_ATmega328P.dcTimeLow[i] = time_B;
                            }
                        }
                    }
                    outputFragment.writeFeedback(DataMemory_ATmega328P.PINB_ADDR, bitPosition, OutputFragment_ATmega328P.pinbuffer[i] != 0);
                }
            }

            if (pins[0] != null) {

                for (OutputPin_ATmega328P p : pins[0]) {
//                    publishProgress(index);
                    outputFragment.updateView(index);
                    index += 1;
                }
            }

            try {
                if (checkInputOutputShortCircuit(inputFragment.getPinList(), pins[0])) {
                    sendShortCircuit();
                }
            } catch (NullPointerException e) {
                Log.e(UCModule.MY_LOG_TAG, "ERROR: PortBUpdateView (doInBackground) -> output list is null", e);
            }

            return null;
        }

//        @Override
//        protected void onProgressUpdate(Integer... values) {
//            outputFragment.updateView(values[0]);
//        }

    }

    private class PortCUpdateView extends AsyncTask<List<OutputPin_ATmega328P>, Integer, Void> {

        private int index;
        private boolean digitalPINState;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            index = 0;
        }

        @Override
        protected Void doInBackground(List<OutputPin_ATmega328P>... pins) {
            Thread.currentThread().setName("ASYNC_PORTC_UPDATE");

            //AN0 - AN5 (Pin14 - Pin19)
            for (int i = 14, bitPosition = 0; i <= 19; i++, bitPosition++) {
                //Is input?
                if ((0x01 & (configRead_C >> bitPosition)) == 0) {

                    digitalPINState = inputFragment.getPINState(DataMemory_ATmega328P.PINC_ADDR, bitPosition);

                    if (((0x01 & (valueRead_C >> bitPosition)) == 1) && outputFragment.isPullUpEnabled()) {

                        if (!digitalPINState && inputFragment.isPinHiZ(i)) {
                            inputFragment.inputRequest_outputChanel(IOModule.HIGH_LEVEL, DataMemory_ATmega328P.PINC_ADDR, bitPosition, "AN" + bitPosition);
//                            return null;
                        }

                        OutputFragment_ATmega328P.pinbuffer[i] = digitalPINState ? 1 : 0;

                    } else {
//                        OutputFragment_ATmega328P.pinbuffer[i] = digitalPINState ? 1 : 0;
                        if (!inputFragment.isPinHiZ(i)) {
                            OutputFragment_ATmega328P.pinbuffer[i] = digitalPINState ? 1 : 0;
                        } else {
                            OutputFragment_ATmega328P.pinbuffer[i] = IOModule.TRI_STATE;
                        }
                    }
                }
                //Is output!
                else {
                    if (OutputFragment_ATmega328P.pinbuffer[i] != (0x01 & (valueRead_C >> bitPosition))) {
                        OutputFragment_ATmega328P.pinbuffer[i] = (0x01 & (valueRead_C >> bitPosition));

                        if (OutputFragment_ATmega328P.evalFreq[i]) {
//                            Log.d("Freq", "Frequency: " + (Math.pow(10, 10) / (time_C - OutputFragment_ATmega328P.oldTime[i])));

                            double period = time_C - OutputFragment_ATmega328P.oldTime[i];
                            OutputFragment_ATmega328P.frequencyBuffer[i] = Math.pow(10, 10) / period;
                            OutputFragment_ATmega328P.dutyCycleBuffer[i] = (1 - ((time_C - OutputFragment_ATmega328P.dcTimeLow[i]) / period))*100;

                            OutputFragment_ATmega328P.oldTime[i] = time_C;
                            OutputFragment_ATmega328P.evalFreq[i] = false;
                        } else {
                            OutputFragment_ATmega328P.evalFreq[i] = true;
                            OutputFragment_ATmega328P.dcTimeLow[i] = time_C;
                        }
                    }
                    outputFragment.writeFeedback(DataMemory_ATmega328P.PINC_ADDR, bitPosition, OutputFragment_ATmega328P.pinbuffer[i] != 0);
                }

            }
            if (pins[0] != null) {
                for (OutputPin_ATmega328P p : pins[0]) {
//                    publishProgress(index);
                    outputFragment.updateView(index);
                    index += 1;
                }
            }

            try {
                if (checkInputOutputShortCircuit(inputFragment.getPinList(), pins[0])) {
                    sendShortCircuit();
                }
            } catch (NullPointerException e) {
                Log.e(UCModule.MY_LOG_TAG, "ERROR: PortCUpdateView (doInBackground) -> output list is null", e);
            }

            return null;
        }
//
//        @Override
//        protected void onProgressUpdate(Integer... values) {
//            outputFragment.updateView(values[0]);
//        }
    }

    private class PortDUpdateView extends AsyncTask<List<OutputPin_ATmega328P>, Integer, Void> {

        private int index;
        private boolean digitalPINState;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            index = 0;
        }

        @Override
        protected Void doInBackground(List<OutputPin_ATmega328P>... pins) {
            Thread.currentThread().setName("ASYNC_PORTD_UPDATE");

            //Pin0 - Pin7
            for (int i = 0, bitPosition = 0; i <= 7; i++, bitPosition++) {
                //Is input?
                if ((0x01 & (configRead_D >> bitPosition)) == 0) {

                    digitalPINState = inputFragment.getPINState(DataMemory_ATmega328P.PIND_ADDR, bitPosition);

                    if (((0x01 & (valueRead_D >> bitPosition)) == 1) && outputFragment.isPullUpEnabled()) {

                        if (!digitalPINState && inputFragment.isPinHiZ(i)) {
                            inputFragment.inputRequest_outputChanel(IOModule.HIGH_LEVEL, DataMemory_ATmega328P.PIND_ADDR, bitPosition, "Pin" + i);
//                            return null;
                        }

                        OutputFragment_ATmega328P.pinbuffer[i] = digitalPINState ? 1 : 0;

                    } else {
//                        OutputFragment_ATmega328P.pinbuffer[i] = digitalPINState ? 1 : 0;
                        if (!inputFragment.isPinHiZ(i)) {
                            OutputFragment_ATmega328P.pinbuffer[i] = digitalPINState ? 1 : 0;
                        } else {
                            OutputFragment_ATmega328P.pinbuffer[i] = IOModule.TRI_STATE;
                        }
                    }
                }
                //Is output!
                else {
                    //Check MUX Timer0, Timer 2 and USART.
                    if ((i == 2 || i == 4 || i == 7)
                            || (i == 5 && !Timer0_ATmega328P.timerOutputControl_OC0B)
                            || (i == 6 && !Timer0_ATmega328P.timerOutputControl_OC0A)
                            || (i == 3 && !Timer2_ATmega328P.timerOutputControl_OC2B)
                            || (i == 0 && !USART_ATmega328P.usartOutputControl_Rx)
                            || (i == 1 && !USART_ATmega328P.usartOutputControl_Tx)) {

                        if (OutputFragment_ATmega328P.pinbuffer[i] != (0x01 & (valueRead_D >> bitPosition))) {
                            OutputFragment_ATmega328P.pinbuffer[i] = (0x01 & (valueRead_D >> bitPosition));

                            if (OutputFragment_ATmega328P.evalFreq[i]) {
//                                Log.d("Freq", "Frequency: " + (Math.pow(10, 10) / (time_D - OutputFragment_ATmega328P.oldTime[i])));
                                double period = time_D - OutputFragment_ATmega328P.oldTime[i];
                                OutputFragment_ATmega328P.frequencyBuffer[i] = Math.pow(10, 10) / period;
                                OutputFragment_ATmega328P.dutyCycleBuffer[i] = (1 - ((time_D - OutputFragment_ATmega328P.dcTimeLow[i]) / period))*100;

                                OutputFragment_ATmega328P.oldTime[i] = time_D;
                                OutputFragment_ATmega328P.evalFreq[i] = false;
                            } else {
                                OutputFragment_ATmega328P.evalFreq[i] = true;
                                OutputFragment_ATmega328P.dcTimeLow[i] = time_C;
                            }
                        }
                    }
                    outputFragment.writeFeedback(DataMemory_ATmega328P.PIND_ADDR, bitPosition, OutputFragment_ATmega328P.pinbuffer[i] != 0);
                }

            }

            if (pins[0] != null) {

                for (OutputPin_ATmega328P p : pins[0]) {
//                    publishProgress(index);
                    outputFragment.updateView(index);
                    index += 1;
                }
            }

            try {
                if (checkInputOutputShortCircuit(inputFragment.getPinList(), pins[0])) {
                    sendShortCircuit();
                }
            } catch (NullPointerException e) {
                Log.e(UCModule.MY_LOG_TAG, "ERROR: PortDUpdateView (doInBackground) -> output list is null", e);
            }

            return null;
        }

//        @Override
//        protected void onProgressUpdate(Integer... values) {
//            outputFragment.updateView(values[0]);
//        }
    }
}
