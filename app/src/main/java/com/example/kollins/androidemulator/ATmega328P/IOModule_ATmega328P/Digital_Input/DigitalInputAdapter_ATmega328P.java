package com.example.kollins.androidemulator.ATmega328P.IOModule_ATmega328P.Digital_Input;

import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;

import com.example.kollins.androidemulator.R;
import com.example.kollins.androidemulator.UCModule;
import com.example.kollins.androidemulator.uCInterfaces.DigitalInputFragment;
import com.example.kollins.androidemulator.uCInterfaces.IOModule;

import java.util.List;
import java.util.Random;

/**
 * Created by kollins on 3/21/18.
 */

public class DigitalInputAdapter_ATmega328P extends BaseAdapter {

    private String[] pinArray;
    private String[] pinModeArray;

    private DigitalInputFragment_ATmega328P digitalInputFragment;
    private List<DigitalInputPin_ATmega328P> digitalInputPins;

    private Random randomGenerator;

    public DigitalInputAdapter_ATmega328P(DigitalInputFragment_ATmega328P digitalInputFragment, List<DigitalInputPin_ATmega328P> digitalInputPins) {
        this.digitalInputFragment = digitalInputFragment;
        this.digitalInputPins = digitalInputPins;

        pinArray = UCModule.getPinArrayWithHint();
        pinModeArray = UCModule.getPinModeArray();

        randomGenerator = new Random();
    }

    @Override
    public int getCount() {
        return digitalInputPins != null ? digitalInputPins.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return digitalInputPins.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View view;
        final ViewHolderDigitalInput_ATmega328P holder;
        final DigitalInputPin_ATmega328P pin = digitalInputPins.get(position);
        final HintAdapter pinSpinnerAdapter =
                new HintAdapter(digitalInputFragment.getContext(), android.R.layout.simple_spinner_item, pinArray);
        ArrayAdapter pinSpinnerModeAdapter =
                new ArrayAdapter(digitalInputFragment.getContext(), android.R.layout.simple_spinner_item, pinModeArray);

        if (convertView == null) {
            view = LayoutInflater.from(digitalInputFragment.getContext()).inflate(R.layout.digital_input_pin,
                    parent, false);

            holder = new ViewHolderDigitalInput_ATmega328P(view);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolderDigitalInput_ATmega328P) view.getTag();
        }

        pinSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        pinSpinnerModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        holder.pinModeSpinner.setAdapter(pinSpinnerModeAdapter);
        holder.pinSpinner.setAdapter(pinSpinnerAdapter);

        if (pin.getPinSpinnerPosition() < 0) {
            holder.pinSpinner.setSelection(pinSpinnerAdapter.getCount());
        }
        else {
            holder.pinSpinner.setSelection(pin.getPinSpinnerPosition());
        }

        holder.pinSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int positionSpinner, long id) {
                if (positionSpinner == pinSpinnerAdapter.getCount()) {
                    return;
                }
                pin.setPin(pinArray[positionSpinner]);
                pin.setPinSpinnerPosition(positionSpinner);


                /************Simulate button release*************/
                int[] coordinates = new int[2];
                holder.pushButton.getLocationOnScreen(coordinates);

                // MotionEvent parameters
                long downTime = SystemClock.uptimeMillis();
                long eventTime = SystemClock.uptimeMillis();
                int action = MotionEvent.ACTION_UP;
                int x = coordinates[0];
                int y = coordinates[1];
                int metaState = 0;

                // dispatch the event
                MotionEvent event = MotionEvent.obtain(downTime, eventTime, action, x, y, metaState);
                holder.pushButton.dispatchTouchEvent(event);
                /**********************************************/

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        holder.pinModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int positionSpinner, long id) {
                pin.setPinMode(IOModule.PIN_MODES[positionSpinner]);

                switch (pin.getPinMode()){
                    case IOModule.PUSH_GND:
                    case IOModule.PUSH_VDD:
                        pin.setPinState(IOModule.TRI_STATE);
                        break;

                    case IOModule.PULL_UP:
                        pin.setPinState(IOModule.HIGH_LEVEL);
                        break;

                    case IOModule.PULL_DOWN:
                    case IOModule.TOGGLE:
                        pin.setPinState(IOModule.LOW_LEVEL);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        holder.pushButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (pin.getPinSpinnerPosition() < 0){
                    return false;
                }

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    holder.pushButton.setText(UCModule.getButtonTextOn());
                    holder.pushButton.setBackgroundColor(UCModule.getButonOnCollor());

                    DigitalInputPin_ATmega328P.buttonPressed[pin.getPinSpinnerPosition()] = true;

                    switch (pin.getPinMode()) {
                        case IOModule.PUSH_GND:
                            holder.inputPinState.setBackgroundResource(R.drawable.digital_input_off);
                            pin.setPinState(IOModule.LOW_LEVEL);
                            digitalInputFragment.inputRequest_inputChanel(IOModule.LOW_LEVEL, pin.getMemory(), pin.getBitPosition(), pin);
                            break;
                        case IOModule.PUSH_VDD:
                            holder.inputPinState.setBackgroundResource(R.drawable.digital_input_on);
                            pin.setPinState(IOModule.HIGH_LEVEL);
                            digitalInputFragment.inputRequest_inputChanel(IOModule.HIGH_LEVEL, pin.getMemory(), pin.getBitPosition(), pin);
                            break;
                        case IOModule.PULL_UP:
                            break;
                        case IOModule.PULL_DOWN:
                            break;
                        case IOModule.TOGGLE:
                            break;
                    }

                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    holder.pushButton.setText(UCModule.getButtonTextOff());
                    holder.pushButton.setBackgroundColor(UCModule.getButonOffCollor());

                    DigitalInputPin_ATmega328P.buttonPressed[pin.getPinSpinnerPosition()] = false;

                    switch (pin.getPinMode()) {
                        case IOModule.PUSH_GND:
                        case IOModule.PUSH_VDD:
                            holder.inputPinState.setBackgroundResource(R.drawable.digital_input_undefined);
                            pin.setPinState(IOModule.TRI_STATE);
                            if (digitalInputFragment.isPullUpEnabled() && digitalInputFragment.isPinPullUPEnabled(pin.getMemory(), pin.getBitPosition())) {
                                digitalInputFragment.inputRequest_inputChanel(IOModule.HIGH_LEVEL, pin.getMemory(), pin.getBitPosition(),pin);
                            } else {
                                digitalInputFragment.inputRequest_inputChanel(randomGenerator.nextInt(2), pin.getMemory(), pin.getBitPosition(),pin);
                            }
                            break;
                        case IOModule.PULL_UP:
                            break;
                        case IOModule.PULL_DOWN:
                            break;
                        case IOModule.TOGGLE:
                            break;
                    }
                }

                return true;
            }
        });

        return view;
    }
}
