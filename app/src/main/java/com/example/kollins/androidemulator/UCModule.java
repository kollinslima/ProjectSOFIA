package com.example.kollins.androidemulator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kollins.androidemulator.uCInterfaces.CPUModule;
import com.example.kollins.androidemulator.uCInterfaces.DataMemory;
import com.example.kollins.androidemulator.uCInterfaces.ProgramMemory;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Created by kollins on 3/7/18.
 */

public class UCModule extends AppCompatActivity {

    public static String PACKAGE_NAME;

    //Default device
    private String device;

    private int oscilator = 16 * ((int) Math.pow(10, 6));
    private long clockPeriod = (long) ((1 / (double) oscilator) * Math.pow(10, 10));

    //Default location
    private String hexFolderLocation = "ArduinoSimulator/";
    private String hexFileLocation = hexFolderLocation + "code.hex";

    public static final String RESET_ACTION = "RESET_ACTION";
    public static final String CLOCK_ACTION = "CLOCK_ACTION";

    public static final String MY_LOG_TAG = "LOG_SIMULATOR";

    private boolean[] clockVector;
    private Lock clockVectorLock;

    private DataMemory dataMemory;
    private ProgramMemory programMemory;

    private CPUModule cpuModule;
    private Thread threadCPU;

    private uCBroadcastReceiver ucBroadcastReceiver;

    private boolean resetFlag;

    private TextView simulatedTimeDisplay;
    private long simulatedTime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.io_interface);

        PACKAGE_NAME = getApplicationContext().getPackageName();
        ucBroadcastReceiver = new uCBroadcastReceiver();
        clockVectorLock = new ReentrantLock();

        //Load device
        SharedPreferences prefDevice = getSharedPreferences("deviceConfig", MODE_PRIVATE);
        String model = prefDevice.getString("arduinoModel", "UNO");
        device = getDevice(model);
        setTitle("Arduino " + model);

        setUpUc();

        simulatedTime = 0;
        simulatedTimeDisplay = (TextView) findViewById(R.id.simulatedTime);

    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filterReset = new IntentFilter(RESET_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(ucBroadcastReceiver, filterReset);

        IntentFilter filterClock = new IntentFilter(CLOCK_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(ucBroadcastReceiver, filterClock);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(ucBroadcastReceiver);
    }


    private void setUpUc() {
        setResetFlag(false);

        clockVector = new boolean[getDeviceModules()];
        resetClockVector();

        try {
            //Init RAM
            Class dataMemoryDevice = Class.forName(PACKAGE_NAME + "." + device + ".DataMemory_" + device);
            dataMemory = (DataMemory) dataMemoryDevice
                    .getDeclaredConstructor(Context.class)
                    .newInstance(this);

            Log.d(MY_LOG_TAG, "SDRAM size: " + dataMemory.getMemorySize());

            //Init FLASH
            Class programMemoryDevice = Class.forName(PACKAGE_NAME + "." + device + ".ProgramMemory_" + device);
            programMemory = (ProgramMemory) programMemoryDevice
                    .getDeclaredConstructor(Context.class)
                    .newInstance(this);

            Log.d(MY_LOG_TAG, "Flash size: " + programMemory.getMemorySize());

            if (programMemory.loadProgramMemory(hexFileLocation)) {
                //hexFile read Successfully

                Class cpuDevice = Class.forName(PACKAGE_NAME + "." + device + ".CPUModule_" + device);
                cpuModule = (CPUModule) cpuDevice
                        .getDeclaredConstructor(ProgramMemory.class, DataMemory.class, UCModule.class)
                        .newInstance(programMemory, dataMemory, this);

                threadCPU = new Thread((Runnable) cpuModule);
                threadCPU.start();

            } else {
                toast(getResources().getString(R.string.hex_file_read_fail));
            }

        } catch (ClassNotFoundException |
                IllegalAccessException |
                InstantiationException |
                NoSuchMethodException |
                InvocationTargetException e) {
            e.printStackTrace();
        }

    }

    private int getDeviceModules() {
        Resources res = getResources();
        int id = res.getIdentifier(device, "integer", getPackageName());
//        Log.d(MY_LOG_TAG, "getDeviceModules: " + res.getInteger(id));
        return res.getInteger(id);
    }

    private String getDevice(String model) {
        Resources res = getResources();
        int id = res.getIdentifier(model, "string", getPackageName());
        return res.getString(id);
    }

    public synchronized boolean getResetFlag() {
        return resetFlag;
    }

    private synchronized void setResetFlag(boolean state) {
        resetFlag = state;
    }

    public void setClockVector(int moduleID) {
        clockVectorLock.lock();
        try {
            clockVector[moduleID] = true;
            for (int i = 0; i < clockVector.length; i++) {
                if (!clockVector[i]) {
                    return;
                }
            }

            Intent it = new Intent(CLOCK_ACTION);
            LocalBroadcastManager.getInstance(this).sendBroadcast(it);
            resetClockVector();

        } finally {
            clockVectorLock.unlock();
        }
    }

    public boolean getClockVector(int moduleID) {
        boolean val;
        clockVectorLock.lock();
        try {
            return clockVector[moduleID];
        } finally {
            clockVectorLock.unlock();
        }
    }

    private void reset() {

        Log.v(MY_LOG_TAG, "Reset");

        setResetFlag(true);

        try {
            if (threadCPU != null) {
                threadCPU.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        setUpUc();
    }

    private void resetClockVector() {
        for (int i = 0; i < clockVector.length; i++) {
            clockVector[i] = false;
        }
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void wakeUpModules() {
        cpuModule.clockCPU();
    }


    class uCBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            switch (action) {
                case CLOCK_ACTION:
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
