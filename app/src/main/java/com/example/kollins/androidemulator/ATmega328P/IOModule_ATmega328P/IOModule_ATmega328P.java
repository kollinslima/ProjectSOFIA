package com.example.kollins.androidemulator.ATmega328P.IOModule_ATmega328P;

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

public class IOModule_ATmega328P extends Handler implements IOModule{

    private byte portRead;
    private byte configRead;
    private int index;

    private UCModule.uCHandler uCHandler;
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

//                Log.i(UCModule.MY_LOG_TAG, String.format("PORTB notified: 0x%s",
//                        Integer.toHexString((int) portRead)));

                index = 0;
                List<OutputPin_ATmega328P> outputPins = outputFragment.getOutputPins();

                if (outputPins == null){
                    break;
                }

                for (OutputPin_ATmega328P p : outputFragment.getOutputPins()) {
                    for (int i = 8; i <= 13; i++) {
                        //Is input?
                        if ((0x01 & (configRead >> (i - 8))) == 0) {
                            if ((0x01 & (portRead >> (i - 8))) == 1 && outputFragment.isPullUpEnabled()) {
                                if (digitalInputFragment.inputRequest(IOModule.HIGH_LEVEL, DataMemory_ATmega328P.PINB_ADDR, (i - 8), p.getPin()) == 0) {
                                    uCHandler.sendEmptyMessage(UCModule.SHORT_CIRCUIT_ACTION);
                                } else {
                                    p.setPinState(digitalInputFragment.getPINState(DataMemory_ATmega328P.PINB_ADDR, (i - 8)) ? 1 : 0, i);
                                }
                            } else {
                                p.setPinState(IOModule.TRI_STATE, i);
                            }
                        }
                        //Is output!
                        else {
                            Log.d(UCModule.MY_LOG_TAG, "Setting pin state: " + (0x01 & (portRead >> (8 - i))));
                            p.setPinState(0x01 & (portRead >> (i - 8)), i);
                        }
                    }
                    outputFragment.updateView(index);
                    index += 1;
                }

                break;
        }
    }
}
