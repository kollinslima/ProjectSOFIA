package com.example.kollins.androidemulator;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kollins.androidemulator.ATmega328P.IOModule_ATmega328P.Output.OutputFragment_ATmega328P;
import com.example.kollins.androidemulator.uCInterfaces.DataMemory;
import com.example.kollins.androidemulator.uCInterfaces.OutputFragment;
import com.example.kollins.androidemulator.uCInterfaces.ProgramMemory;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Created by kollins on 3/7/18.
 */

public class UCModule extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    public static final int CPU_ID = 0;
    public static final int MANUAL_CLOCK = 1;

    public static String PACKAGE_NAME;

    private int oscilator = 16 * ((int) Math.pow(10, 6));
    private long clockPeriod = (long) ((1 / (double) oscilator) * Math.pow(10, 10));

    //Default device
    public static String device;
    public static String model;

    public static Resources resources;

    //Default location
    private String hexFolderLocation = "ArduinoSimulator/";
    private String hexFileLocation = hexFolderLocation + "code.hex";

    public static final int RESET_ACTION = 0;
    public static final int CLOCK_ACTION = 1;

    public static final String MY_LOG_TAG = "LOG_SIMULATOR";

    public static boolean[] clockVector;
    private Lock clockLock;

    private DataMemory dataMemory;
    private ProgramMemory programMemory;

    private CPUModule cpuModule;
    private Thread threadCPU;

    private uCHandler uCHandler;

    private boolean resetFlag;

    private FragmentManager mFragmentManager;
    private FragmentTransaction mFragmentTransaction;

    private OutputFragment outputFragment;

    private TextView simulatedTimeDisplay;
    private long simulatedTime;

    private FrameLayout outputFrame;

    private boolean setUpSuccessful;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.io_interface);
        setSupportActionBar((Toolbar) findViewById(R.id.mainToolbar));

//        mFragmentManager = getSupportFragmentManager();
//        mFragmentTransaction = mFragmentManager.beginTransaction();

        PACKAGE_NAME = getApplicationContext().getPackageName();
        resources = getResources();

        uCHandler = new uCHandler();
        clockLock = new ReentrantLock();

        //Load device
        SharedPreferences prefDevice = getSharedPreferences("deviceConfig", MODE_PRIVATE);
        model = prefDevice.getString("arduinoModel", "UNO");
        device = getDevice(model);
        setTitle("Arduino " + model);

        clockVector = new boolean[getDeviceModules()];
        resetClockVector();

        simulatedTimeDisplay = (TextView) findViewById(R.id.simulatedTime);

        outputFrame = (FrameLayout) findViewById(R.id.outputPins);

        try {
            Class outputFragmentDevice = Class.forName(PACKAGE_NAME + "." + device + ".IOModule_" +
                    device + ".Output.OutputFragment_" + device);
            outputFragment = (OutputFragment) outputFragmentDevice.newInstance();
        } catch (ClassNotFoundException|IllegalAccessException|InstantiationException e) {
            e.printStackTrace();
        }

        setUpSuccessful = false;

//        ((Button) findViewById(R.id.manualClock)).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                clockLock.lock();
//                try {
//                    clockVector[MANUAL_CLOCK] = true;
//                    for (boolean b : clockVector) {
//                        if (!b) {
//                            return;
//                        }
//                    }
//                    UCModule.resetClockVector();
//
//                    //Send Broadcast
//                    uCHandler.sendEmptyMessage(CLOCK_ACTION);
//                } finally {
//                    clockLock.unlock();
//                }
//            }
//        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpUc();
    }

    @Override
    protected void onPause() {
        super.onPause();
        reset();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_layout, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_reset:
                reset();
                break;

            case R.id.action_add:
                if (!setUpSuccessful){
                    break;
                }

                PopupMenu popup = new PopupMenu(this, findViewById(R.id.action_add));
                popup.setOnMenuItemClickListener(this);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.pop_up_menu, popup.getMenu());
                popup.show();
                break;

            case R.id.action_clear_io:
                outputFrame.setVisibility(View.GONE);
                outputFragment.clearAll();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_output:
                outputFrame.setVisibility(View.VISIBLE);
                if (!outputFragment.haveOutput()) {

                    mFragmentManager = getSupportFragmentManager();
                    mFragmentTransaction = mFragmentManager.beginTransaction();

                    mFragmentTransaction.add(R.id.outputPins, (Fragment) outputFragment, OutputFragment_ATmega328P.TAG_OUTPUT_FRAGMENT);
                    mFragmentTransaction.commit();
                } else {
                    outputFragment.addOuput();
                }
                break;
            case R.id.action_anal_input:
                toast(item.getTitle().toString());
                break;

            case R.id.action_input:
                toast(item.getTitle().toString());
                break;
        }
        return true;
    }

    private void setUpUc() {
        setResetFlag(false);

        simulatedTime = 0;

        try {
            //Init FLASH
            Class programMemoryDevice = Class.forName(PACKAGE_NAME + "." + device + ".ProgramMemory_" + device);
            programMemory = (ProgramMemory) programMemoryDevice
                    .getDeclaredConstructor(Handler.class)
                    .newInstance(uCHandler);

            Log.d(MY_LOG_TAG, "Flash size: " + programMemory.getMemorySize());

            if (programMemory.loadProgramMemory(hexFileLocation)) {
                //hexFile read Successfully
                ((TextView)findViewById(R.id.hexFileErrorInstructions)).setVisibility(View.GONE);

                //Init RAM
                Class dataMemoryDevice = Class.forName(PACKAGE_NAME + "." + device + ".DataMemory_" + device);
                dataMemory = (DataMemory) dataMemoryDevice.newInstance();

                Log.d(MY_LOG_TAG, "SDRAM size: " + dataMemory.getMemorySize());

                outputFragment.setDataMemory(dataMemory);

                cpuModule = new CPUModule(programMemory, dataMemory, this, uCHandler, clockLock);
                threadCPU = new Thread((Runnable) cpuModule);
                threadCPU.start();

                setUpSuccessful = true;

            } else {
                setUpSuccessful = false;
                ((TextView)findViewById(R.id.hexFileErrorInstructions)).setVisibility(View.VISIBLE);
                toast(getResources().getString(R.string.hex_file_read_fail));
            }

        } catch (ClassNotFoundException |
                IllegalAccessException |
                InstantiationException |
                NoSuchMethodException |
                InvocationTargetException e) {
            setUpSuccessful = false;
            e.printStackTrace();
        }

    }

    public static int getDeviceModules() {
        int id = resources.getIdentifier(device, "integer", PACKAGE_NAME);
//        Log.d(MY_LOG_TAG, "getDeviceModules: " + res.getInteger(id));
        return resources.getInteger(id);
    }

    public static String getDevice(String model) {
        int id = resources.getIdentifier(model, "string", PACKAGE_NAME);
        return resources.getString(id);
    }

    public static int getDefaultPinPosition() {
        int id = resources.getIdentifier(UCModule.model + "_defaultPinPosition", "integer", PACKAGE_NAME);
        return resources.getInteger(id);
    }

    public static String[] getPinArray() {
        int id = resources.getIdentifier(UCModule.model + "_pins", "array", PACKAGE_NAME);
        return resources.getStringArray(id);
    }

    public static String getNumberSelected(int number){
        return resources.getQuantityString(
                R.plurals.number_selected,
                number, number
        );
    }

    public static int getSelectedColor(){
        return resources.getColor(R.color.selectedItem);
    }

    public synchronized boolean getResetFlag() {
        return resetFlag;
    }

    private synchronized void setResetFlag(boolean state) {
        resetFlag = state;
    }

    private void reset() {

        Log.i(MY_LOG_TAG, "Reset");

        setResetFlag(true);

        outputFragment.resetOuputs();

        try {
            if (threadCPU != null) {
                Log.i(MY_LOG_TAG, "Waiting threads");
                threadCPU.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        setUpUc();
    }

    public static void resetClockVector() {
        for (int i = 0; i < clockVector.length; i++) {
            clockVector[i] = false;
        }
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    class uCHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            int action = msg.what;

            switch (action) {
                case CLOCK_ACTION:
                    cpuModule.clockCPU();
                    simulatedTime += clockPeriod;
                    simulatedTimeDisplay.setText(String.valueOf(simulatedTime / 10));
                    break;

                case RESET_ACTION:
                    reset();
                    break;
            }
        }
    }

}
