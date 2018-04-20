package com.example.kollins.androidemulator.ATmega328P.IOModule_ATmega328P.Input;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.kollins.androidemulator.ATmega328P.DataMemory_ATmega328P;
import com.example.kollins.androidemulator.ATmega328P.IOModule_ATmega328P.IOModule_ATmega328P;
import com.example.kollins.androidemulator.R;
import com.example.kollins.androidemulator.UCModule;
import com.example.kollins.androidemulator.UCModule_View;
import com.example.kollins.androidemulator.uCInterfaces.DataMemory;
import com.example.kollins.androidemulator.uCInterfaces.InputFragment;
import com.example.kollins.androidemulator.uCInterfaces.IOModule;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by kollins on 3/21/18.
 */

public class InputFragment_ATmega328P extends Fragment implements InputFragment, AdapterView.OnItemLongClickListener, ActionMode.Callback, AdapterView.OnItemClickListener {

    private ListView inputPinsList;
    private InputAdapter_ATmega328P inputAdapter;
    private List<InputPin_ATmega328P> inputPins;

    private DataMemory_ATmega328P dataMemory;

    private boolean haveInput;
//    private boolean pullUpEnabled;

    private Handler screenUpdater;

    private ActionMode mActionMode;

    public InputFragment_ATmega328P() {
        inputPins = new ArrayList<InputPin_ATmega328P>();
        inputAdapter = new InputAdapter_ATmega328P(this, inputPins);
        haveInput = false;

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.frament_input, container, false);

        inputPinsList = (ListView) layout.findViewById(R.id.inputList);
        inputPinsList.setAdapter(inputAdapter);
        inputPinsList.setOnItemClickListener(this);
        inputPinsList.setOnItemLongClickListener(this);

        haveInput = true;

        return layout;
    }

    @Override
    public void addDigitalInput() {
        inputPins.add(new InputPin_ATmega328P(null, IOModule.PUSH_GND, InputPin_ATmega328P.DIGITAL_PIN));
        inputAdapter.notifyDataSetChanged();
    }

    @Override
    public void addAnalogicInput() {
        inputPins.add(new InputPin_ATmega328P(null, InputPin_ATmega328P.ANALOGIC_PIN));
        inputAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean haveInput() {
        return haveInput;
    }

    public boolean isPullUpEnabled() {
        return !dataMemory.readBit(DataMemory_ATmega328P.MCUCR_ADDR, 4);
    }

    public boolean isPinPullUPEnabled(int memory, int bitPosition) {
        return dataMemory.readBit(memory + 2, bitPosition);
    }

    @Override
    public void clearAll() {
        if (inputPins == null) {
            return;
        }
//        haveInput = false;

        for (InputPin_ATmega328P p : inputPins) {
            requestHiZ(true, p);
        }

        inputPins.clear();
        inputAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        getActivity().getMenuInflater().inflate(R.menu.menu_delete_item, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.action_delete) {
            SparseBooleanArray checked = inputPinsList.getCheckedItemPositions();

            for (int i = checked.size() - 1; i >= 0; i--) {
                if (checked.valueAt(i)) {
                    requestHiZ(true, inputPins.get(checked.keyAt(i)));
                    inputPins.remove(checked.keyAt(i));
                }
            }

            actionMode.finish();
            return true;
        }

        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        mActionMode = null;
        inputPinsList.clearChoices();
        inputAdapter.notifyDataSetChanged();
        inputPinsList.setChoiceMode(ListView.CHOICE_MODE_NONE);

        if (inputPinsList.getCount() == 0) {
            screenUpdater.sendEmptyMessage(UCModule_View.REMOVE_INPUT_FRAGMENT);
            haveInput = false;
            getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mActionMode == null) {
            //Add measure
        } else {
            int checkedCount = updateCheckedItens(inputPinsList, position);
            if (checkedCount == 0) {
                mActionMode.finish();
            }
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        boolean consumed = (mActionMode == null);

        if (consumed) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();

            mActionMode = activity.startSupportActionMode(this);
            inputPinsList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            inputPinsList.setItemChecked(position, true);
            updateCheckedItens(inputPinsList, position);
        }

        return consumed;
    }

    private int updateCheckedItens(ListView inputPinsList, int position) {
        SparseBooleanArray checked = inputPinsList.getCheckedItemPositions();

        inputPinsList.setItemChecked(position, inputPinsList.isItemChecked(position));

        int checkedCount = 0;

        for (int i = 0; i < checked.size(); i++) {
            if (checked.valueAt(i)) {
                checkedCount++;
            }
        }

        mActionMode.setTitle(UCModule.getNumberSelected(checkedCount));
        return checkedCount;
    }

    @Override
    public void setDataMemory(DataMemory dataMemory) {
        this.dataMemory = (DataMemory_ATmega328P) dataMemory;
    }

    public void inputRequest_inputChanel(int signalState, int memoryPosition, int bitPosition, InputPin_ATmega328P request) {

        if (signalState == IOModule.TRI_STATE) {
            return;
        }

        new InputRequest_InputChanel(request).execute(signalState, memoryPosition, bitPosition);


    }

    public void requestHiZ(boolean state, InputPin_ATmega328P request) {

        new RequestHiZ(request).execute(state);

    }

    public void inputRequest_outputChanel(int signalState, int memoryPosition, int bitPosition, String request) {
        new InputRequest_OutputChanel(request).execute(signalState, memoryPosition, bitPosition);
    }

    public boolean getPINState(int memoryAddress, int bitPosition) {
        if (dataMemory == null){
            //PIN not defined, so I'll use false
            return false;
        }
        return dataMemory.readBit(memoryAddress, bitPosition);
    }

    public boolean isPinHiZ(int position) {
        if (inputPins == null || inputPins.size() == 0) {
            return true;
        }
        return inputPins.get(0).getHiZ(position);
    }

    @Override
    public void setScreenUpdater(Handler screenUpdater) {
        this.screenUpdater = screenUpdater;
    }

    @Override
    public boolean checkShortCircuit() {
        return false;
    }

    @Override
    public void getPINConfig() {
        Log.i("Config", "Configuring PIN");
        for (InputPin_ATmega328P p : inputPins) {
            if (p.getPinSpinnerPosition() > 0) {
                inputRequest_inputChanel(p.getPinState(), p.getMemory(), p.getBitPosition(), p);
            }
        }
    }

    public List<InputPin_ATmega328P> getPinList() {
        return inputPins;
    }

    private class InputRequest_InputChanel extends AsyncTask<Integer, Void, Boolean> {

        private InputPin_ATmega328P request;

        public InputRequest_InputChanel(InputPin_ATmega328P request) {
            this.request = request;
        }

        //signalState - memoryPosition - bitPosition
        @Override
        protected Boolean doInBackground(Integer... memoryParams) {
            Thread.currentThread().setName("ASYNC_INPUT_REQUEST_INPUT_CHANEL");
            try {
                if (inputPins.size() == 0) {
                    //No restrictions, write requested data.
                    UCModule.interruptionModule.checkIOInterruption(memoryParams[1], memoryParams[2],dataMemory.readBit(memoryParams[1],memoryParams[2]),memoryParams[0] == IOModule.HIGH_LEVEL);
                    dataMemory.writeIOBit(memoryParams[1], memoryParams[2], memoryParams[0] == IOModule.HIGH_LEVEL);
                    return false;
                }

                //If input requested in an output pin
                if (dataMemory.readBit(memoryParams[1] + 1, memoryParams[2])) {

                    boolean boolSignalState = (memoryParams[0] == 1);

                    if (dataMemory.readBit(memoryParams[1] + 2, memoryParams[2]) ^ boolSignalState) {
                        //Output and requested input are different
                        if (!request.getHiZ(request.getPinSpinnerPosition())) {
//                        if (!InputPin_ATmega328P.hiZInput[request.getPinSpinnerPosition()]) {
                            //Input is not HiZ, so it's a short circuit!
                            return true;
                        }
                    }
                }
                //If requested in a input pin
                else {
                    //Is there another input in the same pin?
                    ArrayList<InputPin_ATmega328P> duplicatedInputs = new ArrayList<InputPin_ATmega328P>();
                    for (InputPin_ATmega328P p : inputPins) {
                        if (Objects.equals(p.getPin(), request.getPin())) {
                            duplicatedInputs.add(p);
                        }
                    }

                    //No duplicated itens
                    if (duplicatedInputs.size() == 1) {
                        Log.i("Short", "No dupliated pins - InputFragment");
                        UCModule.interruptionModule.checkIOInterruption(memoryParams[1], memoryParams[2],dataMemory.readBit(memoryParams[1],memoryParams[2]),memoryParams[0] == IOModule.HIGH_LEVEL);
                        dataMemory.writeIOBit(memoryParams[1], memoryParams[2], memoryParams[0] == IOModule.HIGH_LEVEL);
                        return false;
                    }
                    Log.i("Short", "Dupliated pins");
                    Log.i("Short", "Request input: " + memoryParams[0]);

                    for (InputPin_ATmega328P p : duplicatedInputs) {

                        Log.i("Short", "Pin: " + p.getPin() + "\nState: " + p.getPinState());

                        if (p.getHiZ(p.getPinSpinnerPosition()) ||
                                p.getPinState() == IOModule.TRI_STATE) {
                            Log.i("Short", "All good");
                            continue;
                        }
                        if (p.getPinState() != memoryParams[0]) {
                            //Short Circuit!
                            Log.i("Short", "Send short circuit - InputChanel");
                            return true;
                        }
                    }
                    UCModule.interruptionModule.checkIOInterruption(memoryParams[1], memoryParams[2],dataMemory.readBit(memoryParams[1],memoryParams[2]),memoryParams[0] == IOModule.HIGH_LEVEL);
                    dataMemory.writeIOBit(memoryParams[1], memoryParams[2], memoryParams[0] == IOModule.HIGH_LEVEL);

                }

            } catch (NullPointerException e) {
                UCModule.interruptionModule.checkIOInterruption(memoryParams[1], memoryParams[2],dataMemory.readBit(memoryParams[1],memoryParams[2]),memoryParams[0] == IOModule.HIGH_LEVEL);
                dataMemory.writeIOBit(memoryParams[1], memoryParams[2], memoryParams[0] == IOModule.HIGH_LEVEL);
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean hasShortCircuit) {
            if (hasShortCircuit) {
                IOModule_ATmega328P.sendShortCircuit();
            }
        }
    }

    private class RequestHiZ extends AsyncTask<Boolean, Void, Void> {

        private InputPin_ATmega328P request;

        public RequestHiZ(InputPin_ATmega328P request) {
            this.request = request;
        }

        @Override
        protected Void doInBackground(Boolean... params) {
            Thread.currentThread().setName("ASYNC_REQUEST_HIZ");

            //There is no restriction to remove pin from HiZ
            if (!params[0]) {
                Log.i("HiZ", "HiZ request false");
                request.setHiZ(false, request.getPinSpinnerPosition());

            } else {

                //Is there another input in the same pin?
                ArrayList<InputPin_ATmega328P> duplicatedInputs = new ArrayList<InputPin_ATmega328P>();
                for (InputPin_ATmega328P p : inputPins) {
                    if (Objects.equals(p.getPin(), request.getPin())) {
                        duplicatedInputs.add(p);
                    }
                }

                //No duplicated itens
                if (duplicatedInputs.size() == 1) {
                    Log.i("HiZ", "HiZ request true");
                    request.setHiZ(true, request.getPinSpinnerPosition());
                    return null;
                }

                for (InputPin_ATmega328P p : duplicatedInputs) {
                    if (p.getPinState() == IOModule.TRI_STATE) {
                        continue;
                    }
                    Log.i("HiZ", "HiZ request false");
                    request.setHiZ(false, request.getPinSpinnerPosition());
                    return null;
                }
                Log.i("HiZ", "HiZ request true");
                request.setHiZ(true, request.getPinSpinnerPosition());

            }

            return null;
        }
    }

    private class InputRequest_OutputChanel extends AsyncTask<Integer, Void, Boolean> {

        private String request;

        public InputRequest_OutputChanel(String request) {
            this.request = request;
        }

        //signalState - memoryPosition - bitPosition
        @Override
        protected Boolean doInBackground(Integer... memoryParams) {
            Thread.currentThread().setName("ASYNC_INPUT_REQUEST_OUTPUT_CHANEL");
            try {
                if (inputPins.size() == 0) {
                    //No restrictions, write requested data.
//                    dataMemory.writeIOBit(memoryParams[1], memoryParams[2], memoryParams[0] == IOModule.HIGH_LEVEL);
                    UCModule.interruptionModule.checkIOInterruption(memoryParams[1], memoryParams[2],dataMemory.readBit(memoryParams[1],memoryParams[2]),memoryParams[0] == IOModule.HIGH_LEVEL);
                    dataMemory.writeFeedback(memoryParams[1], memoryParams[2], memoryParams[0] == IOModule.HIGH_LEVEL);
                    return false;
                }

                //It's always an input in when this function is called from IOModule.

                //Is there another input in the same pin?
                ArrayList<InputPin_ATmega328P> duplicatedInputs = new ArrayList<InputPin_ATmega328P>();
                for (InputPin_ATmega328P p : inputPins) {
                    if (Objects.equals(p.getPin(), request)) {
                        duplicatedInputs.add(p);
                    }
                }

                //No inputs
                if (duplicatedInputs.size() == 0) {
                    UCModule.interruptionModule.checkIOInterruption(memoryParams[1], memoryParams[2],dataMemory.readBit(memoryParams[1],memoryParams[2]),memoryParams[0] == IOModule.HIGH_LEVEL);
                    dataMemory.writeFeedback(memoryParams[1], memoryParams[2], memoryParams[0] == IOModule.HIGH_LEVEL);
                    return false;
                }

                for (InputPin_ATmega328P p : duplicatedInputs) {
                    if (p.getHiZ(p.getPinSpinnerPosition()) ||
                            p.getPinState() == IOModule.TRI_STATE) {
                        continue;
                    }
                    if (p.getPinState() != memoryParams[0]) {
                        //Short Circuit!
                        Log.i("Short", "Send short circuit - OutputChanel");
                        return true;
                    }
                }
                UCModule.interruptionModule.checkIOInterruption(memoryParams[1], memoryParams[2],dataMemory.readBit(memoryParams[1],memoryParams[2]),memoryParams[0] == IOModule.HIGH_LEVEL);
                dataMemory.writeFeedback(memoryParams[1], memoryParams[2], memoryParams[0] == IOModule.HIGH_LEVEL);

            } catch (NullPointerException e) {
                UCModule.interruptionModule.checkIOInterruption(memoryParams[1], memoryParams[2],dataMemory.readBit(memoryParams[1],memoryParams[2]),memoryParams[0] == IOModule.HIGH_LEVEL);
                dataMemory.writeFeedback(memoryParams[1], memoryParams[2], memoryParams[0] == IOModule.HIGH_LEVEL);
            }
            return false;
        }
    }
}