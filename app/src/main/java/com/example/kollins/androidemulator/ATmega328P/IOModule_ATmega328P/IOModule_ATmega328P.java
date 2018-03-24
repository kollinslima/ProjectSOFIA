package com.example.kollins.androidemulator.ATmega328P.IOModule_ATmega328P;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.kollins.androidemulator.ATmega328P.DataMemory_ATmega328P;
import com.example.kollins.androidemulator.ATmega328P.IOModule_ATmega328P.Digital_Input.DigitalInputFragment_ATmega328P;
import com.example.kollins.androidemulator.ATmega328P.IOModule_ATmega328P.Output.OutputFragment_ATmega328P;
import com.example.kollins.androidemulator.ATmega328P.IOModule_ATmega328P.Output.OutputPin_ATmega328P;
import com.example.kollins.androidemulator.UCModule;
import com.example.kollins.androidemulator.uCInterfaces.DigitalInputFragment;
import com.example.kollins.androidemulator.uCInterfaces.IOModule;
import com.example.kollins.androidemulator.uCInterfaces.OutputFragment;

import java.util.List;

/**
 * Created by kollins on 3/23/18.
 */

public class IOModule_ATmega328P extends Handler implements IOModule {

    private byte portRead;
    private byte configRead;

    private static UCModule.uCHandler uCHandler;
    private OutputFragment_ATmega328P outputFragment;
    private DigitalInputFragment_ATmega328P digitalInputFragment;

    public IOModule_ATmega328P(UCModule.uCHandler uCHandler,
                               OutputFragment outputFragment,
                               DigitalInputFragment digitalInputFragment) {

        this.uCHandler = uCHandler;
        this.outputFragment = (OutputFragment_ATmega328P) outputFragment;
        this.digitalInputFragment = (DigitalInputFragment_ATmega328P) digitalInputFragment;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case PORTB_EVENT:

                portRead = msg.getData().getByte(IOModule.PORT_IOMESSAGE);
                configRead = msg.getData().getByte(IOModule.CONFIG_IOMESSAGE);

                List<OutputPin_ATmega328P> outputPins = outputFragment.getOutputPins();

                if (outputPins == null) {
                    break;
                }

                new PortBUpdateView().execute(outputPins);

                break;
        }
    }

    public static void sendShortCircuit() {
        uCHandler.sendEmptyMessage(UCModule.SHORT_CIRCUIT_ACTION);
    }

    private class PortBUpdateView extends AsyncTask<List<OutputPin_ATmega328P>, Integer, Void> {

        int index;

        @Override
        protected void onPreExecute() {
            index = 0;
        }

        @Override
        protected Void doInBackground(List<OutputPin_ATmega328P>... pins) {
            for (OutputPin_ATmega328P p : pins[0]) {
                for (int i = 8; i <= 13; i++) {
                    //Is input?
                    if ((0x01 & (configRead >> (i - 8))) == 0) {
                        if ((0x01 & (portRead >> (i - 8))) == 1 && outputFragment.isPullUpEnabled()) {
                            digitalInputFragment.inputRequest_outputChanel(IOModule.HIGH_LEVEL, DataMemory_ATmega328P.PINB_ADDR, (i - 8), "Pin " + i);
                            p.setPinState(digitalInputFragment.getPINState(DataMemory_ATmega328P.PINB_ADDR, (i - 8)) ? 1 : 0, i);
                        } else {
                            p.setPinState(IOModule.TRI_STATE, i);
                        }
                    }
                    //Is output!
                    else {
                        p.setPinState(0x01 & (portRead >> (i - 8)), i);
                    }

                }
                publishProgress(index);
                index += 1;
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            outputFragment.updateView(values[0]);
        }
    }
}
