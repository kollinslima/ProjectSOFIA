package com.example.kollins.androidemulator.ATmega328P.IOModule_ATmega328P.Digital_Input;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.kollins.androidemulator.ATmega328P.DataMemory_ATmega328P;
import com.example.kollins.androidemulator.ATmega328P.IOModule_ATmega328P.IOModule_ATmega328P;
import com.example.kollins.androidemulator.R;
import com.example.kollins.androidemulator.UCModule;
import com.example.kollins.androidemulator.uCInterfaces.DataMemory;
import com.example.kollins.androidemulator.uCInterfaces.DigitalInputFragment;
import com.example.kollins.androidemulator.uCInterfaces.IOModule;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kollins on 3/21/18.
 */

public class DigitalInputFragment_ATmega328P extends Fragment implements DigitalInputFragment, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private ListView digitalInputPinsList;
    private DigitalInputAdapter_ATmega328P digitalInputAdapter;
    private List<DigitalInputPin_ATmega328P> digitalInputPins;

    private DataMemory_ATmega328P dataMemory;

    private boolean haveDigitalInput;
    private boolean pullUpEnabled;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        digitalInputPins = new ArrayList<DigitalInputPin_ATmega328P>();
        digitalInputPins.add(new DigitalInputPin_ATmega328P(null, IOModule.PUSH_GND));

        digitalInputAdapter = new DigitalInputAdapter_ATmega328P(this, digitalInputPins);

        haveDigitalInput = false;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.frament_digital_input, container, false);

        digitalInputPinsList = (ListView) layout.findViewById(R.id.digitalInputList);
        digitalInputPinsList.setAdapter(digitalInputAdapter);
        digitalInputPinsList.setOnItemClickListener(this);
        digitalInputPinsList.setOnItemLongClickListener(this);

        haveDigitalInput = true;

        pullUpEnabled = !dataMemory.readBit(DataMemory_ATmega328P.MCUCR_ADDR, 4);

        return layout;
    }

    @Override
    public void addDigitalInput() {
        digitalInputPins.add(new DigitalInputPin_ATmega328P(null, IOModule.PUSH_GND));
        digitalInputAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean haveDigitalInput() {
        return haveDigitalInput;
    }

    public boolean isPullUpEnabled() {
        return pullUpEnabled;
    }

    public boolean isPinPullUPEnabled(int memory, int bitPosition) {
        return dataMemory.readBit(memory + 2, bitPosition);
    }

    @Override
    public void clearAll() {
        if (digitalInputPins == null) {
            return;
        }
        digitalInputPins.clear();
        digitalInputAdapter.notifyDataSetChanged();
    }

    @Override
    public void setDataMemory(DataMemory dataMemory) {
        this.dataMemory = (DataMemory_ATmega328P) dataMemory;
    }

//    public void inputEvent(int signalState, int memoryPosition, int bitPosition) {
//        dataMemory.writeIOBit(memoryPosition, bitPosition, signalState == IOModule.HIGH_LEVEL);
//    }

    public void inputRequest_inputChanel(int signalState, int memoryPosition, int bitPosition, DigitalInputPin_ATmega328P request) {

        new InputRequest_InputChanel(request).execute(signalState,memoryPosition,bitPosition);

    }

    public void inputRequest_outputChanel(int signalState, int memoryPosition, int bitPosition, String request) {

        new InputRequest_OutputChanel(request).execute(signalState,memoryPosition,bitPosition);
    }

    public boolean getPINState(int memoryAddress, int bitPosition) {
        return dataMemory.readBit(memoryAddress, bitPosition);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        return false;
    }

    private class InputRequest_InputChanel extends AsyncTask<Integer, Void, Boolean>{

        private DigitalInputPin_ATmega328P request;

        public InputRequest_InputChanel(DigitalInputPin_ATmega328P request) {
            this.request = request;
        }

        //signalState - memoryPosition - bitPosition
        @Override
        protected Boolean doInBackground(Integer... memoryParams) {
            try {
                if (digitalInputPins.size() == 0) {
                    //No restrictions, write requested data.
                    dataMemory.writeIOBit(memoryParams[1], memoryParams[2], memoryParams[0] == IOModule.HIGH_LEVEL);
                    return false;
                }

                //If input requested in a output pin
                if (dataMemory.readBit(memoryParams[1] + 1, memoryParams[2])) {

                    boolean boolSignalState = (memoryParams[0] == 1);

                    if (dataMemory.readBit(memoryParams[1] + 2, memoryParams[2]) ^ boolSignalState) {
                        //Output and requested input are different
                        return true;
                    }
                }
                //If requested in a input pin
                else {
                    //Is there another input in the same pin?
                    ArrayList<DigitalInputPin_ATmega328P> duplicatedInputs = new ArrayList<DigitalInputPin_ATmega328P>();
                    for (DigitalInputPin_ATmega328P p : digitalInputPins) {
                        if (p.equals(request)) {
                            duplicatedInputs.add(p);
                        }
                    }

                    //No duplicated itens
                    if (duplicatedInputs.size() == 1) {
                        dataMemory.writeIOBit(memoryParams[1], memoryParams[2], memoryParams[0] == IOModule.HIGH_LEVEL);
                        return false;
                    }

                    for (DigitalInputPin_ATmega328P p : duplicatedInputs) {
                        if (p.getPinState() == IOModule.TRI_STATE) {
                            continue;
                        }
                        if (p.getPinState() != memoryParams[0]) {
                            //Short Circuit!
                            return true;
                        }
                    }
                    dataMemory.writeIOBit(memoryParams[1], memoryParams[2], memoryParams[0] == IOModule.HIGH_LEVEL);

                }

            } catch (NullPointerException e) {
                dataMemory.writeIOBit(memoryParams[1], memoryParams[2], memoryParams[0] == IOModule.HIGH_LEVEL);
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean hasShortCircuit) {
            if (hasShortCircuit){
                IOModule_ATmega328P.sendShortCircuit();
            }
        }
    }

    private class InputRequest_OutputChanel extends AsyncTask<Integer, Void, Boolean>{

        private String request;

        public InputRequest_OutputChanel(String request) {
            this.request = request;
        }

        //signalState - memoryPosition - bitPosition
        @Override
        protected Boolean doInBackground(Integer... memoryParams) {
            try {
                if (digitalInputPins.size() == 0) {
                    //No restrictions, write requested data.
                    dataMemory.writeIOBit(memoryParams[1], memoryParams[2], memoryParams[0] == IOModule.HIGH_LEVEL);
                    return false;
                }

                //It's always an input in when this function is called from IOModule.

                //Is there another input in the same pin?
                ArrayList<DigitalInputPin_ATmega328P> duplicatedInputs = new ArrayList<DigitalInputPin_ATmega328P>();
                for (DigitalInputPin_ATmega328P p : digitalInputPins) {
                    if (p.getPin().equals(request)) {
                        duplicatedInputs.add(p);
                    }
                }

                //No inputs
                if (duplicatedInputs.size() == 0) {
                    dataMemory.writeIOBit(memoryParams[1], memoryParams[2], memoryParams[0] == IOModule.HIGH_LEVEL);
                    return false;
                }

                for (DigitalInputPin_ATmega328P p : duplicatedInputs) {
                    if (p.getPinState() == IOModule.TRI_STATE) {
                        continue;
                    }
                    if (p.getPinState() != memoryParams[0]) {
                        //Short Circuit!
                        return true;
                    }
                }
                dataMemory.writeIOBit(memoryParams[1], memoryParams[2], memoryParams[0] == IOModule.HIGH_LEVEL);

            } catch (NullPointerException e) {
                dataMemory.writeIOBit(memoryParams[1], memoryParams[2], memoryParams[0] == IOModule.HIGH_LEVEL);
            }
            return false;
        }
    }
}
