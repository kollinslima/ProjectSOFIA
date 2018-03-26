package com.example.kollins.androidemulator;

import android.os.Handler;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.kollins.androidemulator.uCInterfaces.DataMemory;
import com.example.kollins.androidemulator.uCInterfaces.DigitalInputFragment;
import com.example.kollins.androidemulator.uCInterfaces.IOModule;
import com.example.kollins.androidemulator.uCInterfaces.OutputFragment;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * Created by kollins on 3/23/18.
 */

public class UCModule_View extends Fragment implements Runnable {

    public enum LED_STATUS{RUNNING, SHORT_CIRCUIT};

    private int oscilator = 16 * ((int) Math.pow(10, 6));
    private long clockPeriod = (long) ((1 / (double) oscilator) * Math.pow(10, 10));

    private Lock clockLock;
    private Condition ucViewClockCondition;

    private FragmentManager mFragmentManager;
    private FragmentTransaction mFragmentTransaction;

    private FrameLayout outputFrame;
    private FrameLayout digitalInputFrame;

    private OutputFragment outputFragment;
    private DigitalInputFragment digitalInputFragment;
    private IOModule ioModule;

    private static UCModule.uCHandler uCHandler;
    private UCModule ucModule;

    private Toolbar toolbar;

    private TextView simulatedTimeDisplay, startInstructions, statusInfo;
    private long simulatedTime;
    private long microSeconds;
    private long seconds;

    private Handler screenUpdater;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        screenUpdater = new Handler();

        try {
            Class outputFragmentDevice = Class.forName(UCModule.PACKAGE_NAME + "." + UCModule.device + ".IOModule_" +
                    UCModule.device + ".Output.OutputFragment_" + UCModule.device);
            outputFragment = (OutputFragment) outputFragmentDevice.newInstance();

            Class digitalInputFragmentDevice = Class.forName(UCModule.PACKAGE_NAME + "." + UCModule.device + ".IOModule_" +
                    UCModule.device + ".Digital_Input.DigitalInputFragment_" + UCModule.device);
            digitalInputFragment = (DigitalInputFragment) digitalInputFragmentDevice.newInstance();

            Class ioModuleDevice = Class.forName(UCModule.PACKAGE_NAME + "." + UCModule.device + ".IOModule_" +
                    UCModule.device + ".IOModule_" + UCModule.device);
            ioModule = (IOModule) ioModuleDevice
                    .getDeclaredConstructor(OutputFragment.class, DigitalInputFragment.class)
                    .newInstance(outputFragment, digitalInputFragment);

        } catch (ClassNotFoundException | IllegalAccessException | java.lang.InstantiationException
                | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_io_interface, container, false);

        //Set Toolbar
        toolbar = ((Toolbar) view.findViewById(R.id.mainToolbar));
        toolbar.inflateMenu(R.menu.menu_layout);
        toolbar.setOnMenuItemClickListener(new ToolBarMenuItemClick());
        toolbar.setTitle("Arduino " + UCModule.model);
//        ((AppCompatActivity)getActivity()).setSupportActionBar((Toolbar) view.findViewById(R.id.mainToolbar));

        statusInfo = (TextView) view.findViewById(R.id.statusInfo);

        simulatedTimeDisplay = (TextView) view.findViewById(R.id.simulatedTime);
        startInstructions = (TextView) view.findViewById(R.id.startInstructions);

        outputFrame = (FrameLayout) view.findViewById(R.id.outputPins);
        digitalInputFrame = (FrameLayout) view.findViewById(R.id.digitalInputPins);

        return view;
    }

    @Override
    public void run() {
        simulatedTime = 0;
        while(!ucModule.getResetFlag()) {
            waitClock();
            simulatedTime += clockPeriod;
//            seconds = TimeUnit.NANOSECONDS.toSeconds(simulatedTime/10);
//            microSeconds = TimeUnit.NANOSECONDS.toMicros(simulatedTime/10);
            microSeconds = (simulatedTime/10)/1000;
            seconds = (microSeconds/1000)/1000;

            screenUpdater.post(new Runnable() {
                @Override
                public void run() {
                    simulatedTimeDisplay.setText(String.format("%d.%06d s",seconds, microSeconds));
                }
            });
        }

        Log.i(UCModule.MY_LOG_TAG, "Finishing UCView");
    }

    private void waitClock() {
        clockLock.lock();
        try {
            UCModule.clockVector[UCModule.SIMULATED_TIMER_ID] = true;

            for (int i = 0; i < UCModule.clockVector.length; i++) {
                if (!UCModule.clockVector[i]) {
                    ucViewClockCondition.await();
                    return;
                }
            }

            UCModule.resetClockVector();

            //Send Broadcast
            uCHandler.sendEmptyMessage(UCModule.CLOCK_ACTION);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            clockLock.unlock();
        }
    }

    public void clockUCView() {
        clockLock.lock();
        try {
            ucViewClockCondition.signal();
        } finally {
            clockLock.unlock();
        }
    }

    public void setMemoryIO(DataMemory dataMemory) {
        outputFragment.setDataMemory(dataMemory);
        digitalInputFragment.setDataMemory(dataMemory);
    }

    public IOModule getIOModule() {
        return ioModule;
    }

    public void setClockLock(Lock clockLock) {
        this.clockLock = clockLock;
        ucViewClockCondition = clockLock.newCondition();
    }

    public void resetIO(){
        outputFragment.resetOuputs();
    }

    public void setUCHandler(UCModule.uCHandler uCHandler) {
        this.uCHandler = uCHandler;
    }

    public void setStatusLed(LED_STATUS status) {
        switch (status){
            case RUNNING:
                statusInfo.setText(UCModule.getStatusRunning());
                statusInfo.setTextColor(UCModule.getStatusRunningColor());
                break;

            case SHORT_CIRCUIT:

                break;
        }
    }

    public static void sendShortCircuit() {
        uCHandler.sendEmptyMessage(UCModule.SHORT_CIRCUIT_ACTION);
    }

    public void setUCDevice(UCModule ucModule) {
        this.ucModule = ucModule;
    }

    private class ToolBarMenuItemClick implements Toolbar.OnMenuItemClickListener {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_reset:
//                    outputFragment.resetOuputs();
                    uCHandler.sendEmptyMessage(UCModule.RESET_ACTION);
                    break;

                case R.id.action_add:
                    if (!UCModule.setUpSuccessful) {
                        break;
                    }

                    PopupMenu popup = new PopupMenu(getActivity(), getView().findViewById(R.id.action_add));
                    popup.setOnMenuItemClickListener(new PopUpMenuItemClick());
                    MenuInflater inflater = popup.getMenuInflater();
                    inflater.inflate(R.menu.pop_up_menu, popup.getMenu());
                    popup.show();
                    break;

                case R.id.action_clear_io:
                    outputFrame.setVisibility(View.GONE);
                    outputFragment.clearAll();
                    digitalInputFrame.setVisibility(View.GONE);
                    digitalInputFragment.clearAll();
                    startInstructions.setVisibility(View.VISIBLE);
                    break;
            }
            return true;
        }
    }

    private class PopUpMenuItemClick implements PopupMenu.OnMenuItemClickListener {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            startInstructions.setVisibility(View.GONE);
            switch (item.getItemId()) {
                case R.id.action_output:
                    outputFrame.setVisibility(View.VISIBLE);
                    if (!outputFragment.haveOutput()) {

                        mFragmentManager = ((AppCompatActivity) getActivity()).getSupportFragmentManager();
                        mFragmentTransaction = mFragmentManager.beginTransaction();

                        mFragmentTransaction.add(R.id.outputPins, (android.support.v4.app.Fragment) outputFragment, OutputFragment.TAG_OUTPUT_FRAGMENT);
                        mFragmentTransaction.commit();
                    } else {
                        outputFragment.addOuput();
                    }
                    break;

                case R.id.action_digital_input:
                    digitalInputFrame.setVisibility(View.VISIBLE);
                    if (!digitalInputFragment.haveDigitalInput()) {

                        mFragmentManager = ((AppCompatActivity) getActivity()).getSupportFragmentManager();
                        mFragmentTransaction = mFragmentManager.beginTransaction();

                        mFragmentTransaction.add(R.id.digitalInputPins, (android.support.v4.app.Fragment) digitalInputFragment, DigitalInputFragment.TAG_DIGITAL_INPUT_FRAGMENT);
                        mFragmentTransaction.commit();
                    } else {
                        digitalInputFragment.addDigitalInput();
                    }
                    break;

                case R.id.action_analog_input:
//                toast(item.getTitle().toString());
                    break;
            }
            return true;
        }
    }
}
