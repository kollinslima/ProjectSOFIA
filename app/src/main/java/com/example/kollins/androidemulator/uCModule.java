package com.example.kollins.androidemulator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.kollins.androidemulator.uCInterfaces.IOModule;
import com.example.kollins.androidemulator.uCInterfaces.ProgramMemory;

import java.lang.reflect.InvocationTargetException;


/**
 * Created by kollins on 3/7/18.
 */

public class uCModule extends AppCompatActivity {

    public static String PACKAGE_NAME;

    //Default device
    private String device = "ATmega328P";

    //Default location
    private String hexFolderLocation = "ArduinoSimulator/";
    private String hexFileLocation = hexFolderLocation + "code.hex";

    public static final String RESET_ACTION = "RESET_ACTION";
    public static final String MY_LOG_TAG = "LOG_SIMULATOR";

    private IOModule ioModule;
    private ProgramMemory flashMemory;

    private uCBroadcastReceiver ucBroadcastReceiver;

    private boolean resetFlag;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PACKAGE_NAME = getApplicationContext().getPackageName();

        ucBroadcastReceiver = new uCBroadcastReceiver();

        setUpUc();

    }

    private void setUpUc(){
        setResetFlag(false);

        try {
            Class ioDevice = Class.forName(PACKAGE_NAME + ".IOModule_" + device);
            ioModule = (IOModule) ioDevice.getDeclaredConstructor(uCModule.class).newInstance(this);
            ioModule.loadBaseLayout();

            //Initialize RAM

            Class programMemoryDevice = Class.forName(PACKAGE_NAME + ".ProgramMemory_" + device);
            flashMemory = (ProgramMemory) programMemoryDevice.getDeclaredConstructor(Context.class).newInstance(this);
            flashMemory.loadProgramMemory(hexFileLocation);


        } catch (ClassNotFoundException|
                IllegalAccessException|
                InstantiationException|
                NoSuchMethodException|
                InvocationTargetException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filterLocal = new IntentFilter(RESET_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(ucBroadcastReceiver,filterLocal);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(ucBroadcastReceiver);
    }

    public synchronized boolean getResetFlag(){
        return resetFlag;
    }

    private synchronized void setResetFlag(boolean state){
        resetFlag = state;
    }

    private void reset(){

        Log.v(MY_LOG_TAG, "Reset");

        setResetFlag(true);

        //Join all threads

        setUpUc();
    }

    class uCBroadcastReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            switch (action){
                case RESET_ACTION:
                    reset();
                    break;
            }
        }
    }
}
