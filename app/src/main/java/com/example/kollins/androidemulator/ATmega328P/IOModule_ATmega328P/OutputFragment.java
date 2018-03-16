package com.example.kollins.androidemulator.ATmega328P.IOModule_ATmega328P;

import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.example.kollins.androidemulator.ATmega328P.DataMemory_ATmega328P;
import com.example.kollins.androidemulator.R;
import com.example.kollins.androidemulator.UCModule;
import com.example.kollins.androidemulator.uCInterfaces.DataMemory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by kollins on 3/14/18.
 */

public class OutputFragment extends Fragment {

    public static final int[] BACKGROUND_PIN = {R.drawable.off_led, R.drawable.on_led};

    public static final String TAG_OUTPUT_FRAGMENT = "outputFragmentTAG";
    public static final int OUTPUT_EVENT_PORTB = 10;

    private ListView outputPinsList;
    private OutputAdapter outputAdapter;
    private List<OutputPin> outputPins;

    private OutputHandler outputHandler;
    private DataMemory dataMemory;

    private Resources res;

    public boolean haveOutput;

//    public static OutputFragment newOutputFragment (DataMemory dataMemory){
//        Bundle param = new Bundle();
//        param.putSerializable(EXTRA_DATA_MEMORY, dataMemory);
//
//        OutputFragment outputFragment = new OutputFragment();
//        outputFragment.setArguments(param);
//        return outputFragment;
//    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        res = getResources();
        outputHandler = new OutputHandler();

        outputPins = new ArrayList<OutputPin>();
        outputPins.add(new OutputPin(null, 0, getDefaultPinPosition()));

        outputAdapter = new OutputAdapter(this, outputPins);

        haveOutput = false;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View layout = inflater.inflate(R.layout.frament_output, container, false);

        outputPinsList = (ListView) layout.findViewById(R.id.outputList);

        outputPinsList.setAdapter(outputAdapter);

        dataMemory.setOuputHandler(outputHandler);
        haveOutput = true;

        return layout;
    }


    public int getDefaultPinPosition() {
        int id = res.getIdentifier(UCModule.model + "_defaultPinPosition", "integer", UCModule.PACKAGE_NAME);
        return res.getInteger(id);
    }

    public String[] getPinArray() {
        int id = res.getIdentifier(UCModule.model + "_pins", "array", UCModule.PACKAGE_NAME);
        return res.getStringArray(id);
    }

    public void addOuput(){
        outputPins.add(new OutputPin(null, 0, getDefaultPinPosition()));
        outputAdapter.notifyDataSetChanged();
    }

    public void setDataMemory(DataMemory dataMemory) {
        this.dataMemory = dataMemory;
        if (haveOutput) {
            dataMemory.setOuputHandler(outputHandler);
        }
    }

    class OutputHandler extends Handler{

        byte portRead;
        int index;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case OUTPUT_EVENT_PORTB:
                    portRead = dataMemory.readByte(DataMemory_ATmega328P.PORTB_ADDR);

                    Log.v(UCModule.MY_LOG_TAG, String.format("PORTB notified: 0x%s",
                            Integer.toHexString((int)portRead)));

                    index = 0;
                    for (OutputPin p : outputPins){
                        for (int i = 8; i <= 13; i++) {
                            Log.d(UCModule.MY_LOG_TAG, "Looking for: Pin " + i);
                            Log.d(UCModule.MY_LOG_TAG, "Found: " + p.getPin());
                            if (Objects.equals(p.getPin(),"Pin " + i)) {
                                Log.d(UCModule.MY_LOG_TAG, "Setting pin state: " + (0x01 & (portRead >> (8 - i))));
                                p.setPinState(0x01 & (portRead >> (i - 8)));
                                updateView(index);
//                                outputAdapter.notifyDataSetChanged();
                            }
                        }
                        index += 1;
                    }

                    break;
            }
        }

        private void updateView(int index){
            View view = outputPinsList.getChildAt(index -
                    outputPinsList.getFirstVisiblePosition());

            if (view == null){
                return;
            }

            TextView led = (TextView) view.findViewById(R.id.ledState);
            led.setText(getResources().getStringArray(R.array.ledText)[outputPins.get(index).getPinState()]);
            led.setBackgroundResource(BACKGROUND_PIN[outputPins.get(index).getPinState()]);

        }
    }

}
