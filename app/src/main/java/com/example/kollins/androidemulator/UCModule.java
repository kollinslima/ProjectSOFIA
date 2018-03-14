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
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kollins.androidemulator.uCInterfaces.DataMemory;
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

    //Default device
    public static String device;

    private int oscilator = 16 * ((int) Math.pow(10, 6));
    private long clockPeriod = (long) ((1 / (double) oscilator) * Math.pow(10, 10));

    //Default location
    private String hexFolderLocation = "ArduinoSimulator/";
    private String hexFileLocation = hexFolderLocation + "code.hex";

    public static final String RESET_ACTION = "RESET_ACTION";
    public static final String CLOCK_ACTION = "CLOCK_ACTION";

    public static final String MY_LOG_TAG = "LOG_SIMULATOR";

    public static boolean[] clockVector;
    private Lock clockLock;

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
        setSupportActionBar((Toolbar) findViewById(R.id.mainToolbar));

        PACKAGE_NAME = getApplicationContext().getPackageName();
        ucBroadcastReceiver = new uCBroadcastReceiver();
        clockLock = new ReentrantLock();

        //Load device
        SharedPreferences prefDevice = getSharedPreferences("deviceConfig", MODE_PRIVATE);
        String model = prefDevice.getString("arduinoModel", "UNO");
        device = getDevice(model);
        setTitle("Arduino " + model);

        clockVector = new boolean[getDeviceModules()];
        resetClockVector();

        setUpUc();

        simulatedTime = 0;
        simulatedTimeDisplay = (TextView) findViewById(R.id.simulatedTime);

        ((Button) findViewById(R.id.manualClock)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clockLock.lock();
                try {
                    clockVector[MANUAL_CLOCK] = true;
                    for (boolean b : clockVector){
                        if (!b){
                            return;
                        }
                    }
                    UCModule.resetClockVector();

                    //Send Broadcast
                    Intent itClock = new Intent(CLOCK_ACTION);
                    LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(itClock);
                } finally {
                    clockLock.unlock();
                }
            }
        });

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_layout,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_reset:
                reset();
                break;

            case R.id.action_add:
                PopupMenu popup = new PopupMenu(this, findViewById(R.id.action_add));
                popup.setOnMenuItemClickListener(this);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.pop_up_menu, popup.getMenu());
                popup.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_output:
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

                cpuModule = new CPUModule(programMemory, dataMemory, this, clockLock);
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

    private void reset() {

        Log.i(MY_LOG_TAG, "Reset");

        setResetFlag(true);

        try {
            if (threadCPU != null) {
                Log.i(MY_LOG_TAG, "Waiting threads");
                threadCPU.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        simulatedTime = 0;
        simulatedTimeDisplay.setText(String.valueOf(simulatedTime));
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


    class uCBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

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
