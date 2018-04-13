package com.example.kollins.androidemulator.ATmega328P.IOModule_ATmega328P.Output;

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
import android.widget.TextView;

import com.example.kollins.androidemulator.ATmega328P.DataMemory_ATmega328P;
import com.example.kollins.androidemulator.R;
import com.example.kollins.androidemulator.UCModule;
import com.example.kollins.androidemulator.UCModule_View;
import com.example.kollins.androidemulator.uCInterfaces.DataMemory;
import com.example.kollins.androidemulator.uCInterfaces.OutputFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kollins on 3/14/18.
 */

public class OutputFragment_ATmega328P extends Fragment implements OutputFragment, AdapterView.OnItemLongClickListener, ActionMode.Callback, AdapterView.OnItemClickListener {

    public static final int[] BACKGROUND_PIN = {R.drawable.off_led, R.drawable.on_led, R.drawable.hi_z_led};

    //Virtual Pin to hold states until first output
    public static int[] pinbuffer = new int[UCModule.getPinArray().length];

    private ListView outputPinsList;
    private OutputAdapter_ATmega328P outputAdapter;
    private List<OutputPin_ATmega328P> outputPins;

    private DataMemory_ATmega328P dataMemory;

    private boolean haveOutput;
    private boolean pullUpEnabled;

    private Handler screenUpdater;

    private ActionMode mActionMode;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        outputPins = new ArrayList<OutputPin_ATmega328P>();
        outputPins.add(new OutputPin_ATmega328P(null, UCModule.getDefaultPinPosition(), pinbuffer));

        outputAdapter = new OutputAdapter_ATmega328P(this, outputPins);

        haveOutput = false;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View layout = inflater.inflate(R.layout.frament_output, container, false);

        outputPinsList = (ListView) layout.findViewById(R.id.outputList);
        outputPinsList.setAdapter(outputAdapter);
        outputPinsList.setOnItemClickListener(this);
        outputPinsList.setOnItemLongClickListener(this);

        haveOutput = true;

        pullUpEnabled = !dataMemory.readBit(DataMemory_ATmega328P.MCUCR_ADDR, 4);

        Log.i("OutputAdd", "PinBuffer: " + pinbuffer[UCModule.getDefaultPinPosition()]);

        return layout;
    }

    public boolean haveOutput() {
        return haveOutput;
    }

    public boolean isPullUpEnabled(){
        return pullUpEnabled;
    }

    public void addOuput() {
        Log.i("OutputAdd", "PinBuffer: " + pinbuffer[UCModule.getDefaultPinPosition()]);
        outputPins.add(new OutputPin_ATmega328P(null, UCModule.getDefaultPinPosition(), pinbuffer));
        outputAdapter.notifyDataSetChanged();
    }

    public void setDataMemory(DataMemory dataMemory) {
        this.dataMemory = (DataMemory_ATmega328P) dataMemory;
    }

    public void resetOuputs() {

        if (outputPins == null) {
            return;
        }

        for (OutputPin_ATmega328P pin : outputPins) {
            pin.resetPinState();
        }
        outputAdapter.notifyDataSetChanged();

    }

    public void clearAll() {
        if (outputPins == null) {
            return;
        }
        outputPins.clear();
        outputAdapter.notifyDataSetChanged();
    }

    public List<OutputPin_ATmega328P> getOutputPins(){
        return outputPins;
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
            SparseBooleanArray checked = outputPinsList.getCheckedItemPositions();

            for (int i = checked.size() - 1; i >= 0; i--) {
                if (checked.valueAt(i)) {
                    outputPins.remove(checked.keyAt(i));
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
        outputPinsList.clearChoices();
        outputAdapter.notifyDataSetChanged();
        outputPinsList.setChoiceMode(ListView.CHOICE_MODE_NONE);

        if (outputPinsList.getCount() == 0){
            screenUpdater.sendEmptyMessage(UCModule_View.REMOVE_OUTPUT_FRAGMENT);
            haveOutput = false;
            getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mActionMode == null) {
            //Add measure
        } else {
            int checkedCount = updateCheckedItens(outputPinsList, position);
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
            outputPinsList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            outputPinsList.setItemChecked(position, true);
            updateCheckedItens(outputPinsList, position);
        }

        return consumed;
    }

    private int updateCheckedItens(ListView outputPinsList, int position) {
        SparseBooleanArray checked = outputPinsList.getCheckedItemPositions();

        outputPinsList.setItemChecked(position, outputPinsList.isItemChecked(position));

        int checkedCount = 0;

        for (int i = 0; i < checked.size(); i++) {
            if (checked.valueAt(i)) {
                checkedCount++;
            }
        }

        mActionMode.setTitle(UCModule.getNumberSelected(checkedCount));
        return checkedCount;
    }

    public synchronized void updateView(final int index) {

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                View view = outputPinsList.getChildAt(index -
                        outputPinsList.getFirstVisiblePosition());

                if (view == null) {
                    return;
                }

                TextView led = (TextView) view.findViewById(R.id.ledState);

                try {
                    OutputPin_ATmega328P pin = outputPins.get(index);
                    led.setText(UCModule.resources.getStringArray(R.array.ledText)[pin.getPinState(pin.getPinPositionSpinner())]);
                    led.setBackgroundResource(BACKGROUND_PIN[pin.getPinState(pin.getPinPositionSpinner())]);
                } catch (IndexOutOfBoundsException e){
                    //AsyncTask may be still running and cause this error.
                }
            }
        });
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
        return;
    }

    public List<OutputPin_ATmega328P> getPinList(){
        return outputPins;
    }

    public void writeFeedback(int address, int bitPosition, boolean state) {
        UCModule.interruptionModule.checkIOInterruption(address, bitPosition,dataMemory.readBit(address,bitPosition),state);
        dataMemory.writeFeedback(address,bitPosition,state);
    }

    public boolean isMeasrureOutput(int memoryAddress, int bitPosition) {
        return !dataMemory.readBit(memoryAddress, bitPosition);
    }
}
