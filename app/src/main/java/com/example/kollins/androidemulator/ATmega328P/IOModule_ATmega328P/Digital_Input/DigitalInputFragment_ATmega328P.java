package com.example.kollins.androidemulator.ATmega328P.IOModule_ATmega328P.Digital_Input;

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

    public void inputEvent(int signalState, int memotyPosition, int bitPosition) {
        dataMemory.writeIOBit(memotyPosition, bitPosition, signalState == IOModule.HIGH_LEVEL);
    }

    public int inputRequest(int signalState, int memotyPosition, int bitPosition, String request) {
        boolean containPin = false;
        int pinIndex = 0;
        try {
            if (digitalInputPins.size() == 0) {
                dataMemory.writeIOBit(memotyPosition, bitPosition, signalState == IOModule.HIGH_LEVEL);
                return 1;
            }

            for (DigitalInputPin_ATmega328P p : digitalInputPins){
                if (p.getPin().equals(request)){
                    containPin = true;
                    break;
                }
                pinIndex += 1;
            }
            if (containPin) {
                if (digitalInputPins.get(pinIndex).getPinState() == IOModule.TRI_STATE) {
                    dataMemory.writeIOBit(memotyPosition, bitPosition, signalState == IOModule.HIGH_LEVEL);
                    return 1;
                } else if (digitalInputPins.get(pinIndex).getPinState() != signalState) {
                    return 0;
                }
            }
            return 1;
        } catch (NullPointerException e) {
            dataMemory.writeIOBit(memotyPosition, bitPosition, signalState == IOModule.HIGH_LEVEL);
            return 1;
        }

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

}
