package com.example.kollins.androidemulator;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kollins.androidemulator.uCInterfaces.DataMemory;
import com.example.kollins.androidemulator.uCInterfaces.IOModule;
import com.example.kollins.androidemulator.uCInterfaces.OutputFragment;
import com.example.kollins.androidemulator.uCInterfaces.ProgramMemory;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Created by kollins on 3/7/18.
 */

public class UCModule extends AppCompatActivity {

    public static final int CPU_ID = 0;
    public static final int SIMULATED_TIMER_ID = 1;
    public static final int MANUAL_CLOCK = 2;

    public static String PACKAGE_NAME;

    //Default device
    public static String device;
    public static String model;

    public static Resources resources;

    //Default location
    private String hexFolderLocation = "ArduinoSimulator/";
    private String hexFileLocation = hexFolderLocation + "code.hex";

    public static final int RESET_ACTION = 0;
    public static final int CLOCK_ACTION = 1;
    public static final int SHORT_CIRCUIT_ACTION = 2;

    public static final String MY_LOG_TAG = "LOG_SIMULATOR";

    public static boolean[] clockVector;
    private Lock clockLock;

    private DataMemory dataMemory;
    private ProgramMemory programMemory;

    private CPUModule cpuModule;
    private Thread threadCPU;

    private uCHandler uCHandler;

    private boolean resetFlag;
    private static int resetManager;

    public static boolean setUpSuccessful;

    private FrameLayout frameIO;
    private UCModule_View ucView;
    private Thread threadUCView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_view);

//        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

        PACKAGE_NAME = getApplicationContext().getPackageName();
        resources = getResources();

        uCHandler = new uCHandler();
        clockLock = new ReentrantLock();

        //Load device
        SharedPreferences prefDevice = getSharedPreferences("deviceConfig", MODE_PRIVATE);
        model = prefDevice.getString("arduinoModel", "UNO");
        device = getDevice(model);
        setTitle("Arduino " + model);

        clockVector = new boolean[getDeviceModules()+1];    //+1 for SIMULATED_TIMER;
        resetClockVector();

        setUpSuccessful = false;

        ucView = new UCModule_View();

        frameIO = (FrameLayout) findViewById(R.id.fragmentIO);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        ft.add(R.id.fragmentIO, ucView, OutputFragment.TAG_OUTPUT_FRAGMENT);
        ft.commit();

        ucView.setClockLock(clockLock);
        ucView.setUCHandler(uCHandler);
        ucView.setUCDevice(this);
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

    private void setUpUc() {
        Log.i(MY_LOG_TAG, "SetUp");
        setResetFlag(false);

        try {
            //Init FLASH
            Class programMemoryDevice = Class.forName(PACKAGE_NAME + "." + device + ".ProgramMemory_" + device);
            programMemory = (ProgramMemory) programMemoryDevice
                    .getDeclaredConstructor(Handler.class)
                    .newInstance(uCHandler);

            Log.d(MY_LOG_TAG, "Flash size: " + programMemory.getMemorySize());

            if (programMemory.loadProgramMemory(hexFileLocation)) {
                //hexFile read Successfully

                programMemory.setPC(0);
                ((TextView) findViewById(R.id.hexFileErrorInstructions)).setVisibility(View.GONE);

                //Init RAM
                Class dataMemoryDevice = Class.forName(PACKAGE_NAME + "." + device + ".DataMemory_" + device);
                dataMemory = (DataMemory) dataMemoryDevice.getDeclaredConstructor(IOModule.class)
                        .newInstance(ucView.getIOModule());

                Log.d(MY_LOG_TAG, "SDRAM size: " + dataMemory.getMemorySize());

                ucView.setMemoryIO(dataMemory);

                threadUCView = new Thread(ucView);
                threadUCView.start();

                cpuModule = new CPUModule(programMemory, dataMemory, this, uCHandler, clockLock);
                threadCPU = new Thread(cpuModule);
                threadCPU.start();

                resetManager = 0;
                setUpSuccessful = true;
                ucView.setStatus(UCModule_View.LED_STATUS.RUNNING);

            } else {
                setUpSuccessful = false;
                ucView.setStatus(UCModule_View.LED_STATUS.HEX_FILE_ERROR);
//                toast(getResources().getString(R.string.hex_file_read_fail));
            }

        } catch (ClassNotFoundException |
                IllegalAccessException |
                InstantiationException |
                NoSuchMethodException |
                InvocationTargetException e) {
            setUpSuccessful = false;
            ucView.setStatus(UCModule_View.LED_STATUS.HEX_FILE_ERROR);
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

    public static boolean[] getHiZInput() {
        boolean[] hiZInput = new boolean[getPinArray().length];
        for (int i = 0; i < hiZInput.length; i++){
            hiZInput[i] = true;
        }
        return hiZInput;
    }

    public static String[] getPinModeArray() {
        return resources.getStringArray(R.array.inputModes);
    }

    public static int getSourcePower() {
        return resources.getInteger(R.integer.defaultSourcePower);
    }

    public static String[] getPinArrayWithHint() {
        int id = resources.getIdentifier(UCModule.model + "_pins", "array", PACKAGE_NAME);
        String[] pinArrayWithHint = resources.getStringArray(id);
        pinArrayWithHint = Arrays.copyOf(pinArrayWithHint, pinArrayWithHint.length + 1);

        pinArrayWithHint[pinArrayWithHint.length - 1] = resources.getString(R.string.inputHint);
        return pinArrayWithHint;
    }

    public static String getNumberSelected(int number) {
        return resources.getQuantityString(
                R.plurals.number_selected,
                number, number
        );
    }

    public static int[] getDigitalInputMemoryAddress() {
        int id = resources.getIdentifier(UCModule.model + "_digitalInputMemoryAddress", "array", PACKAGE_NAME);
        return resources.getIntArray(id);
    }

    public static int[] getDigitalInputMemoryBitPosition() {
        int id = resources.getIdentifier(UCModule.model + "_digitalInputMemoryBitPosition", "array", PACKAGE_NAME);
        return resources.getIntArray(id);
    }

    public static int getSelectedColor() {
        return resources.getColor(R.color.selectedItem);
    }

    public static int getButonOnCollor() {
        return resources.getColor(R.color.on_button);
    }

    public static int getButonOffCollor() {
        return resources.getColor(R.color.off_button);
    }

    public static String getButtonTextOn() {
        return resources.getString(R.string.buttonOn);
    }

    public static String getButtonTextOff() {
        return resources.getString(R.string.buttonOff);
    }

    public static String getStatusRunning() {
        return resources.getString(R.string.running);
    }

    public static int getStatusRunningColor() {
        return resources.getColor(R.color.running);
    }

    public static String getStatusShortCircuit() {
        return resources.getString(R.string.short_circuit);
    }

    public static int getStatusShortCircuitColor() {
        return resources.getColor(R.color.short_circuit);
    }

    public static String getStatusHexFileError() {
        return resources.getString(R.string.hex_file_read_fail);
    }

    public static int getStatusHexFileErrorColor() {
        return resources.getColor(R.color.hex_file_error);
    }

    public synchronized boolean getResetFlag() {
        return resetFlag;
    }

    private synchronized void setResetFlag(boolean state) {
        resetFlag = state;
    }

    private void reset() {

        Log.i(MY_LOG_TAG, "Reset");

        stopSystem();
        ucView.resetIO();
        setUpUc();
    }

    private void stopSystem(){
        Log.i(MY_LOG_TAG, "Stopping system");

        setResetFlag(true);

        try {
            if (threadCPU != null) {
                Log.i(MY_LOG_TAG, "Waiting CPU thread");
                while (threadCPU.isAlive()){
                    cpuModule.clockCPU();
                }
                threadCPU.join();

                clockLock.lock();
                clockVector[CPU_ID] = true;
                clockLock.unlock();

                resetManager = 1;
            }
            if (threadUCView != null) {
                Log.i(MY_LOG_TAG, "Waiting UCView Thread");
                while (threadUCView.isAlive()){
                    ucView.clockUCView();
                }
                threadUCView.join();

                clockLock.lock();
                clockVector[SIMULATED_TIMER_ID] = true;
                clockLock.unlock();

                resetManager = 2;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (setUpSuccessful){
            programMemory.stopCodeObserver();
        }
    }

    private void shortCircuit() {

        Log.i(MY_LOG_TAG, "Short Circuit");
        ucView.setStatus(UCModule_View.LED_STATUS.SHORT_CIRCUIT);
        stopSystem();
    }

    public static void resetClockVector() {
        for (int i = resetManager; i < clockVector.length; i++) {
            clockVector[i] = false;
        }
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public class uCHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            int action = msg.what;

            switch (action) {
                case CLOCK_ACTION:
                    ucView.clockUCView();
                    cpuModule.clockCPU();
                    break;

                case RESET_ACTION:
                    reset();
                    break;

                case SHORT_CIRCUIT_ACTION:
//                    Toast.makeText(UCModule.this, "SHORT CIRCUIT!!!", Toast.LENGTH_SHORT).show();
                    shortCircuit();
                    break;
            }
        }
    }

}
