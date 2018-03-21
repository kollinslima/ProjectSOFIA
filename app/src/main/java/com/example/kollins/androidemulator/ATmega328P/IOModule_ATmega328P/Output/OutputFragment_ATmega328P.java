package com.example.kollins.androidemulator.ATmega328P.IOModule_ATmega328P.Output;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.widget.Toast;

import com.example.kollins.androidemulator.ATmega328P.DataMemory_ATmega328P;
import com.example.kollins.androidemulator.R;
import com.example.kollins.androidemulator.UCModule;
import com.example.kollins.androidemulator.uCInterfaces.DataMemory;
import com.example.kollins.androidemulator.uCInterfaces.OutputFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kollins on 3/14/18.
 */

public class OutputFragment_ATmega328P extends Fragment implements OutputFragment, AdapterView.OnItemLongClickListener, ActionMode.Callback, AdapterView.OnItemClickListener {

    public static final int[] BACKGROUND_PIN = {R.drawable.off_led, R.drawable.on_led, R.drawable.hi_z_led};

    public static final String TAG_OUTPUT_FRAGMENT = "outputFragmentTAG";
    public static final int OUTPUT_EVENT_PORTB = 10;

    private ListView outputPinsList;
    private OutputAdapter_ATmega328P outputAdapter;
    private List<OutputPin_ATmega328P> outputPins;

    private OutputHandler outputHandler;
    private DataMemory_ATmega328P dataMemory;

    private boolean haveOutput;
    private boolean pullUpEnabled;

    private ActionMode mActionMode;


//    public static OutputFragment_ATmega328P newOutputFragment (DataMemory dataMemory){
//        Bundle param = new Bundle();
//        param.putSerializable(EXTRA_DATA_MEMORY, dataMemory);
//
//        OutputFragment_ATmega328P outputFragment = new OutputFragment_ATmega328P();
//        outputFragment.setArguments(param);
//        return outputFragment;
//    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        outputHandler = new OutputHandler();

        outputPins = new ArrayList<OutputPin_ATmega328P>();
        outputPins.add(new OutputPin_ATmega328P(null, UCModule.getDefaultPinPosition()));

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

        dataMemory.setOuputHandler(outputHandler);
        haveOutput = true;

        pullUpEnabled = !dataMemory.readBit(DataMemory_ATmega328P.MCUCR_ADDR, 4);

        return layout;
    }

    public boolean haveOutput(){
        return haveOutput;
    }

    public void addOuput() {
        outputPins.add(new OutputPin_ATmega328P(null, UCModule.getDefaultPinPosition()));
        outputAdapter.notifyDataSetChanged();
    }

    public void setDataMemory(DataMemory dataMemory) {
        this.dataMemory = (DataMemory_ATmega328P) dataMemory;
        if (haveOutput) {
            this.dataMemory.setOuputHandler(outputHandler);
        }
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

    public void clearAll(){
        outputPins.clear();
        outputAdapter.notifyDataSetChanged();
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
        if (menuItem.getItemId() == R.id.action_delete){
            SparseBooleanArray checked = outputPinsList.getCheckedItemPositions();

            for (int i = checked.size()-1; i>=0; i--){
                if (checked.valueAt(i)){
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
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mActionMode == null) {
            //Add measure
        }
        else{
            int checkedCount = updateCheckedItens(outputPinsList, position);
            if (checkedCount == 0){
                mActionMode.finish();
            }
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        boolean consumed = (mActionMode == null);

        if (consumed){
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

        for (int i = 0; i < checked.size(); i++){
            if (checked.valueAt(i)) {
                checkedCount++;
            }
        }

        mActionMode.setTitle(UCModule.getNumberSelected(checkedCount));
        return checkedCount;
    }


    class OutputHandler extends Handler {

        byte portRead;
        byte configRead;
        int index;

        TextView led;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case OUTPUT_EVENT_PORTB:
                    portRead = dataMemory.readByte(DataMemory_ATmega328P.PORTB_ADDR);
                    configRead = dataMemory.readByte(DataMemory_ATmega328P.DDRB_ADDR);

                    Log.v(UCModule.MY_LOG_TAG, String.format("PORTB notified: 0x%s",
                            Integer.toHexString((int) portRead)));

                    index = 0;
                    for (OutputPin_ATmega328P p : outputPins) {
                        for (int i = 8; i <= 13; i++) {
                            //Is input?
                            if ((0x01 & (configRead >> (i - 8))) == 0) {
                                if ((0x01 & (portRead >> (i - 8))) == 1 && pullUpEnabled){
                                    p.setPinState(OutputPin_ATmega328P.HIGH_LEVEL, i);
                                } else {
                                    p.setPinState(OutputPin_ATmega328P.TRI_STATE, i);
                                }
                            }
                            //Is output!
                            else {
                                Log.d(UCModule.MY_LOG_TAG, "Setting pin state: " + (0x01 & (portRead >> (8 - i))));
                                p.setPinState(0x01 & (portRead >> (i - 8)), i);
                            }
                        }
                        updateView(index);
                        index += 1;
                    }


                    break;
            }
        }

        private void updateView(int index) {
            View view = outputPinsList.getChildAt(index -
                    outputPinsList.getFirstVisiblePosition());

            if (view == null) {
                return;
            }

            led = (TextView) view.findViewById(R.id.ledState);

            OutputPin_ATmega328P pin = outputPins.get(index);
            led.setText(UCModule.resources.getStringArray(R.array.ledText)[pin.getPinState(pin.getPinPositionSpinner())]);
            led.setBackgroundResource(BACKGROUND_PIN[pin.getPinState(pin.getPinPositionSpinner())]);
        }
    }

}
