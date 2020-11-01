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

package com.kollins.project.sofia.atmega328p.iomodule_atmega328p.output;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.fragment.app.Fragment;

import com.kollins.project.sofia.R;
import com.kollins.project.sofia.UCModule;
import com.kollins.project.sofia.UCModule_View;
import com.kollins.project.sofia.atmega328p.DataMemory_ATmega328P;
import com.kollins.project.sofia.ucinterfaces.DataMemory;
import com.kollins.project.sofia.ucinterfaces.OutputFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by kollins on 3/14/18.
 */

public class OutputFragment_ATmega328P extends Fragment implements OutputFragment, AdapterView.OnItemLongClickListener, ActionMode.Callback, AdapterView.OnItemClickListener {

    public static final int[] BACKGROUND_PIN = {R.drawable.off_led, R.drawable.on_led, R.drawable.hi_z_led};

//    public static final int FUSION_FREQ = 1000; //Above this value, the screen will blink too fast, so make it analog

    //Virtual Pin to hold states until first output
    public static int[] pinbuffer = new int[UCModule.getPinArray().length];

    public static long[] oldTime = new long[UCModule.getPinArray().length];
    public static long[] dcTimeLow = new long[UCModule.getPinArray().length];
    public static boolean[] evalFreq = new boolean[UCModule.getPinArray().length];
    public static double[] frequencyBuffer = new double[UCModule.getPinArray().length];
    public static double[] dutyCycleBuffer = new double[UCModule.getPinArray().length];

    private ListView outputPinsList;
    private OutputAdapter_ATmega328P outputAdapter;
    private List<OutputPin_ATmega328P> outputPins;

    private DataMemory_ATmega328P dataMemory;

    private boolean haveOutput;

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

        Log.i("OutputAdd", "PinBuffer: " + pinbuffer[UCModule.getDefaultPinPosition()]);

        return layout;
    }

    public boolean haveOutput() {
        return haveOutput;
    }

    public boolean isPullUpEnabled() {
        return !dataMemory.readBit(DataMemory_ATmega328P.MCUCR_ADDR, 4);
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

    public List<OutputPin_ATmega328P> getOutputPins() {
        return outputPins;
    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        getActivity().getMenuInflater().inflate(R.menu.menu_option_item_output, menu);
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
        if (menuItem.getItemId() == R.id.action_meter) {
            SparseBooleanArray checked = outputPinsList.getCheckedItemPositions();

            for (int i = checked.size() - 1; i >= 0; i--) {
                if (checked.valueAt(i)) {
                    outputPins.get(checked.keyAt(i)).showMeter();
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

        if (outputPinsList.getCount() == 0) {
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

    public synchronized void updateView(int index) {

        new UpdateScreen(index).execute();

//        if (outputPinsList == null) {
//            return;
//        }
//
//        View view = outputPinsList.getChildAt(index -
//                outputPinsList.getFirstVisiblePosition());
//
//        if (view == null) {
//            return;
//        }
//
//        final TextView led = view.findViewById(R.id.ledState);
//        final TextView freq = view.findViewById(R.id.frequency);
//        final TextView dc = view.findViewById(R.id.dutycycle);
//        final OutputPin_ATmega328P pin = outputPins.get(index);
//
//        final String ledText = UCModule.resources.getStringArray(R.array.ledText)[pin.getPinState(pin.getPinPositionSpinner())];
//        final int backgroundColor = BACKGROUND_PIN[pin.getPinState(pin.getPinPositionSpinner())];
//        final String freqText = String.format("%.0f Hz", frequencyBuffer[pin.getPinPositionSpinner()] >= 0 ? frequencyBuffer[pin.getPinPositionSpinner()] : 0);
//        final String dcText = String.format("%.0f %%", dutyCycleBuffer[pin.getPinPositionSpinner()] <= 100 ? dutyCycleBuffer[pin.getPinPositionSpinner()] : 100);

//        getActivity().runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                led.setText(ledText);
//                led.setBackgroundResource(backgroundColor);
//                freq.setText(freqText);
//                dc.setText(dcText);
//            }
//        });
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
    }

    public List<OutputPin_ATmega328P> getPinList() {
        return outputPins;
    }

    public void writeFeedback(int address, int bitPosition, boolean state) {
        UCModule.interruptionModule.checkIOInterruption(address, bitPosition, dataMemory.readBit(address, bitPosition), state);
        dataMemory.writeFeedback(address, bitPosition, state);
    }

    public boolean isMeasrureOutput(int memoryAddress, int bitPosition) {
        return !dataMemory.readBit(memoryAddress, bitPosition);
    }

    private class UpdateScreen extends AsyncTask<Void, Void, Void>{

        private TextView led, freq, dc;
        private OutputPin_ATmega328P pin;
        private String ledText, freqText, dcText;
        private int backgroundColor;
        private int r,g,b;
        private View view = null;
        private final int index;

        public UpdateScreen(int index) {
            this.index = index;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (outputPinsList != null) {
                view = outputPinsList.getChildAt(index -
                        outputPinsList.getFirstVisiblePosition());

                if (view != null) {
                    led = view.findViewById(R.id.ledState);
                    freq = view.findViewById(R.id.frequency);
                    dc = view.findViewById(R.id.dutycycle);
                }
            }
        }

        @Override
        protected Void doInBackground(Void...v) {

            if (outputPinsList == null) {
                return null;
            }

            try {
                pin = outputPins.get(index);
            } catch (IndexOutOfBoundsException e){
                Log.e(UCModule.MY_LOG_TAG, "Invalid Index", e);
                return null;
            }

            ledText = UCModule.resources.getStringArray(R.array.ledText)[pin.getPinState(pin.getPinPositionSpinner())];
            backgroundColor = BACKGROUND_PIN[pin.getPinState(pin.getPinPositionSpinner())];
            double freq = frequencyBuffer[pin.getPinPositionSpinner()];
            if (freq >= 0) {
                freqText = String.format(Locale.getDefault(), "%.0f Hz", freq);
            } else {
                freqText = "";
            }

            double dc = dutyCycleBuffer[pin.getPinPositionSpinner()];
            if (dc <= 100 && dc >= 0) {
                dcText = String.format(Locale.getDefault(), "%.0f %%", dutyCycleBuffer[pin.getPinPositionSpinner()]);
//                if (freq > FUSION_FREQ) {
//                    //TODO: Make this function dynamic with ON/OFF colors
//                    double dcPercent = (dc/100);
//                    r = (23 + (int)(dcPercent*232))&0xFF;
//                    g = (161 + (int)(dcPercent*43))&0xFF;
//                    b = (165 + (int)(dcPercent*(-165)))&0xFF;
//
//                    ledText = "";
//                } else {
//                    backgroundColor = BACKGROUND_PIN[pin.getPinState(pin.getPinPositionSpinner())];
//                }
//            } else {
//                dcText = "";
//                backgroundColor = BACKGROUND_PIN[pin.getPinState(pin.getPinPositionSpinner())];
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);

            if (view != null) {
                led.setText(ledText);
//                if (ledText.isEmpty()) {
//                    Drawable analogBackground = AppCompatResources.getDrawable(getContext(), R.drawable.off_led);
//                    DrawableCompat.setTint(analogBackground, Color.rgb(r, g, b));
//                    led.setBackground(analogBackground);
//                } else {
                    led.setBackgroundResource(backgroundColor);
//                }
                freq.setText(freqText);
                dc.setText(dcText);
            }
        }
    }
}
