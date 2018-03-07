package com.example.kollins.androidemulator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;


/**
 * Created by kollins on 3/7/18.
 */

public class uCModule extends AppCompatActivity {

    public static final String RESET_ACTION = "RESET_ACTION";

    public static final String MY_LOG_TAG = "LOG_EMULATOR";
    private final String HEX_FOLDER_LOCATION = "ArduinoSimulator/";
    private final String HEX_FILE_LOCATION = HEX_FOLDER_LOCATION + "code.hex";

    //32kBytes, each instruction is 16bits wide
    private final int FLASH_SIZE = 32 * 1000;
    private final int WORD_SIZE = 16;
    private ArrayList<Byte> flashMemory = null;

    private final int INTEL_DATA_SIZE = 0;
    private final int INTEL_ADDRESS = 1;
    private final int INTEL_REORD_TYPE = 3;
    private final int INTEL_DATA = 4;

    private IOModule ioModule;

    private FileObserver codeObserver;
    private uCBroadcastReceiver ucBroadcastReceiver;

    private boolean resetFlag;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ucBroadcastReceiver = new uCBroadcastReceiver();

        ioModule = new IOModule(this);
        //Initialize RAM

        setUpUc();

    }

    private void setUpUc(){

        setResetFlag(false);

        if (flashMemory == null) {
            flashMemory = new ArrayList<Byte>(FLASH_SIZE);
        }
        flashMemory.clear();
        loadProgramMemory();
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


    //Thanks Dave L.
    // (https://stackoverflow.com/questions/140131/convert-a-string-representation-of-a-hex-dump-to-a-byte-array-using-java)
    private byte[] hexStringToByteArray(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }

    private void loadProgramMemory() {

        String state = Environment.getExternalStorageState();

        //All set to read and write data in SDCard
        if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {

            File dcimDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);

            if (dcimDir.exists()) {
                File hexFile = new File(dcimDir, HEX_FILE_LOCATION);

                //Watch for changes in hexFile
                codeObserver = new FileObserver(hexFile.getPath().toString()) {
                    @Override
                    public void onEvent(int event, @Nullable String path) {
                        Log.d(MY_LOG_TAG, "File event: " + event);
                        sendBroadcast(RESET_ACTION);
                    }
                };
                codeObserver.startWatching();

                if (hexFile.exists()) {
                    try {
                        FileInputStream fis = new FileInputStream(hexFile);
                        loadHexFile(fis);
                    } catch (FileNotFoundException e) {
                        Log.e(MY_LOG_TAG, "ERROR: Load .hex file", e);
                    }
                }
            }

        } else {
            Log.e(MY_LOG_TAG, "ERROR: Can't read SD card");
        }
    }

    private void loadHexFile(FileInputStream fis) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
        String line;

        byte[] readBytes;
        int dataSize;
        int memoryPosition;

        boolean extendedSegmentAddress = false;
        boolean extendedLinearAddress = false;

        int extendedAddress = 0;

        try {
            while ((line = reader.readLine()) != null) {
                //Remove colon
                line = line.substring(1);
                Log.v(MY_LOG_TAG, line);

                readBytes = hexStringToByteArray(line);

                //Checksum()

                dataSize = readBytes[INTEL_DATA_SIZE];
                Log.d(MY_LOG_TAG, "Data size: " + dataSize);

                switch (readBytes[INTEL_REORD_TYPE]) {
                    //Data
                    case 00:
                        Log.v(MY_LOG_TAG, "Loading Data from hex");

                        //Avoid sign extention
                        memoryPosition = (((int) (0x000000FF & readBytes[INTEL_ADDRESS])) << 8)
                                | (int) (0x000000FF & readBytes[INTEL_ADDRESS + 1]);

                        if (extendedSegmentAddress) {
                            memoryPosition = memoryPosition + extendedAddress;
                        } else if (extendedLinearAddress) {
                            memoryPosition = (extendedAddress << 16) | memoryPosition;
                        }

                        Log.v(MY_LOG_TAG, "Memory position: " + memoryPosition);

                        for (int i = 0; i < dataSize; i++) {
                            flashMemory.add(memoryPosition + i, readBytes[INTEL_DATA + i]);
                            Log.v(MY_LOG_TAG, String.format("Added 0x%02X to flash", readBytes[INTEL_DATA + i]));
                        }

                        break;

                    //End of File
                    case 01:
                        Log.v(MY_LOG_TAG, "End of File from hex");
                        break;

                    //Extended Segment Address
                    case 02:
                        Log.v(MY_LOG_TAG, "Extended Segment Address from hex");
                        extendedSegmentAddress = true;
                        extendedLinearAddress = false;

                        extendedAddress = (((int) (0x000000FF & readBytes[INTEL_ADDRESS])) << 8)
                                | (int) (0x000000FF & readBytes[INTEL_ADDRESS + 1]);

                        Log.v(MY_LOG_TAG, "Extended by: " + extendedAddress);
                        break;

                    //Extended Linear Address
                    case 04:
                        Log.v(MY_LOG_TAG, "Extended Linear Address from hex");
                        extendedSegmentAddress = false;
                        extendedLinearAddress = true;

                        extendedAddress = (readBytes[INTEL_DATA] << 8) | readBytes[INTEL_DATA];
                        Log.v(MY_LOG_TAG, "Extended by: " + extendedAddress);
                        break;

                    default:
                        Log.e(MY_LOG_TAG, "Record Type unknown: " +
                                String.format("0x%02X", readBytes[INTEL_REORD_TYPE]));
                }

            }

            reader.close();
            fis.close();
        } catch (IOException e) {
            Log.e(MY_LOG_TAG, "ERROR: Load .hex file", e);
        }
    }

    private void sendBroadcast(String action) {
        Intent it = new Intent(action);
        LocalBroadcastManager.getInstance(this).sendBroadcast(it);
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
