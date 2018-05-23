package com.example.kollins.sofia.atmega328p.iomodule_atmega328p;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.kollins.sofia.atmega328p.DataMemory_ATmega328P;
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

    private byte valueRead;
    private byte configRead;

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

        valueRead = msg.getData().getByte(IOModule.VALUE_IOMESSAGE);
        configRead = msg.getData().getByte(IOModule.CONFIG_IOMESSAGE);

        List<OutputPin_ATmega328P> outputPins = outputFragment.getOutputPins();

        switch (msg.what) {
            case PORTB_EVENT:

                new PortBUpdateView().execute(outputPins);

                break;

            case PORTC_EVENT:

                new PortCUpdateView().execute(outputPins);

                break;

            case PORTD_EVENT:

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

    public void setOC0A(int stateOC0A) {

        //Affected only if it is an output.
        if (outputFragment.isMeasrureOutput(DataMemory_ATmega328P.DDRD_ADDR, 6)) {
            return;
        }

        outputFragment.pinbuffer[OC0A_PIN_POSITION] = stateOC0A;

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

    public void setOC0B(int stateOC0B) {

        //Affected only if it is an output.
        if (outputFragment.isMeasrureOutput(DataMemory_ATmega328P.DDRD_ADDR, 5)) {
            return;
        }

        outputFragment.pinbuffer[OC0B_PIN_POSITION] = stateOC0B;

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

    public void setOC1A(int stateOC1A) {
        //Affected only if it is an output.
        if (outputFragment.isMeasrureOutput(DataMemory_ATmega328P.DDRB_ADDR, 1)) {
            return;
        }

        outputFragment.pinbuffer[OC1A_PIN_POSITION] = stateOC1A;

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

    public void setOC1B(int stateOC1B) {
        //Affected only if it is an output.
        if (outputFragment.isMeasrureOutput(DataMemory_ATmega328P.DDRB_ADDR, 2)) {
            return;
        }

        outputFragment.pinbuffer[OC1B_PIN_POSITION] = stateOC1B;

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

    public void setOC2A(int stateOC2A) {
        //Affected only if it is an output.
        if (outputFragment.isMeasrureOutput(DataMemory_ATmega328P.DDRB_ADDR, 3)) {
            return;
        }

        outputFragment.pinbuffer[OC2A_PIN_POSITION] = stateOC2A;

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

    public void setOC2B(int stateOC2B) {
        //Affected only if it is an output.
        if (outputFragment.isMeasrureOutput(DataMemory_ATmega328P.DDRD_ADDR, 3)) {
            return;
        }

        outputFragment.pinbuffer[OC2B_PIN_POSITION] = stateOC2B;

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
                if ((0x01 & (configRead >> bitPosition)) == 0) {

                    digitalPINState = inputFragment.getPINState(DataMemory_ATmega328P.PINB_ADDR, bitPosition);

                    if (((0x01 & (valueRead >> bitPosition)) == 1) && outputFragment.isPullUpEnabled()) {

                        if (!digitalPINState && inputFragment.isPinHiZ(i)) {
                            inputFragment.inputRequest_outputChanel(IOModule.HIGH_LEVEL, DataMemory_ATmega328P.PINB_ADDR, bitPosition, "Pin" + i);
//                            return null;
                        }

                        outputFragment.pinbuffer[i] = digitalPINState ? 1 : 0;

                    } else {
//                        outputFragment.pinbuffer[i] = digitalPINState ? 1 : 0;
                        if (!inputFragment.isPinHiZ(i)) {
//                            Log.i(UCModule.MY_LOG_TAG, "Button pressed");
                            outputFragment.pinbuffer[i] = digitalPINState ? 1 : 0;
                        } else {
//                            Log.i(UCModule.MY_LOG_TAG, "Button not pressed");
                            outputFragment.pinbuffer[i] = IOModule.TRI_STATE;
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
                        outputFragment.pinbuffer[i] = (0x01 & (valueRead >> bitPosition));
                    }
                    outputFragment.writeFeedback(DataMemory_ATmega328P.PINB_ADDR, bitPosition, outputFragment.pinbuffer[i] != 0);
                }
            }

            if (pins[0] != null) {

                for (OutputPin_ATmega328P p : pins[0]) {
                    publishProgress(index);
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

        @Override
        protected void onProgressUpdate(Integer... values) {
            outputFragment.updateView(values[0]);
        }

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
                if ((0x01 & (configRead >> bitPosition)) == 0) {

                    digitalPINState = inputFragment.getPINState(DataMemory_ATmega328P.PINC_ADDR, bitPosition);

                    if (((0x01 & (valueRead >> bitPosition)) == 1) && outputFragment.isPullUpEnabled()) {

                        if (!digitalPINState && inputFragment.isPinHiZ(i)) {
                            inputFragment.inputRequest_outputChanel(IOModule.HIGH_LEVEL, DataMemory_ATmega328P.PINC_ADDR, bitPosition, "AN" + bitPosition);
//                            return null;
                        }

                        outputFragment.pinbuffer[i] = digitalPINState ? 1 : 0;

                    } else {
//                        outputFragment.pinbuffer[i] = digitalPINState ? 1 : 0;
//                        Log.d("Analog", "No Pull-UP");
//                        Log.d("Analog", "HiZ: " + inputFragment.isPinHiZ(i));
                        if (!inputFragment.isPinHiZ(i)) {
//                            Log.i(UCModule.MY_LOG_TAG, "Button pressed");
                            outputFragment.pinbuffer[i] = digitalPINState ? 1 : 0;
                        } else {
//                            Log.i(UCModule.MY_LOG_TAG, "Button not pressed");
                            outputFragment.pinbuffer[i] = IOModule.TRI_STATE;
                        }
                    }
                }
                //Is output!
                else {
                    outputFragment.pinbuffer[i] = (0x01 & (valueRead >> bitPosition));
                    outputFragment.writeFeedback(DataMemory_ATmega328P.PINC_ADDR, bitPosition, outputFragment.pinbuffer[i] != 0);
                }

            }
            if (pins[0] != null) {
                for (OutputPin_ATmega328P p : pins[0]) {
                    publishProgress(index);
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

        @Override
        protected void onProgressUpdate(Integer... values) {
            outputFragment.updateView(values[0]);
        }
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
                if ((0x01 & (configRead >> bitPosition)) == 0) {

                    digitalPINState = inputFragment.getPINState(DataMemory_ATmega328P.PIND_ADDR, bitPosition);

                    if (((0x01 & (valueRead >> bitPosition)) == 1) && outputFragment.isPullUpEnabled()) {

                        if (!digitalPINState && inputFragment.isPinHiZ(i)) {
                            inputFragment.inputRequest_outputChanel(IOModule.HIGH_LEVEL, DataMemory_ATmega328P.PIND_ADDR, bitPosition, "Pin" + i);
//                            return null;
                        }

                        outputFragment.pinbuffer[i] = digitalPINState ? 1 : 0;

                    } else {
//                        outputFragment.pinbuffer[i] = digitalPINState ? 1 : 0;
                        if (!inputFragment.isPinHiZ(i)) {
//                            Log.i(UCModule.MY_LOG_TAG, "Button pressed");
                            outputFragment.pinbuffer[i] = digitalPINState ? 1 : 0;
                        } else {
//                            Log.i(UCModule.MY_LOG_TAG, "Button not pressed");
                            outputFragment.pinbuffer[i] = IOModule.TRI_STATE;
                        }
                    }
                }
                //Is output!
                else {
                    //Check MUX Timer0 and Timer 2
                    if (((i < 5 && i != 3) || i > 6)
                            || (i == 5 && !Timer0_ATmega328P.timerOutputControl_OC0B)
                            || (i == 6 && !Timer0_ATmega328P.timerOutputControl_OC0A)
                            || (i == 3 && !Timer2_ATmega328P.timerOutputControl_OC2B)) {
                        outputFragment.pinbuffer[i] = (0x01 & (valueRead >> bitPosition));
                    }
                    outputFragment.writeFeedback(DataMemory_ATmega328P.PIND_ADDR, bitPosition, outputFragment.pinbuffer[i] != 0);
                }

            }

            if (pins[0] != null) {

                for (OutputPin_ATmega328P p : pins[0]) {
                    publishProgress(index);
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

        @Override
        protected void onProgressUpdate(Integer... values) {
            outputFragment.updateView(values[0]);
        }
    }
}
