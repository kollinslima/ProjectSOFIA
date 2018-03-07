package com.example.kollins.androidemulator;

import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Created by kollins on 3/7/18.
 */

public class uCModule implements Runnable {

    private static final String MY_LOG_TAG = "LOG_EMULATOR";
    private final String HEX_FILE_LOCATION = "ArduinoSimulator/code.hex";

    //32kBytes, each instruction is 16bits wide
    private final int FLASH_SIZE = 32*1000;
    private final int WORD_SIZE = 16;
    private ArrayList<Byte> flashMemory;

    private final int INTEL_DATA_SIZE = 0;
    private final int INTEL_ADDRESS = 1;
    private final int INTEL_REORD_TYPE = 3;
    private final int INTEL_DATA = 4;

    private IOModule ioModule;

    private Lock outputLock;
    private Condition outputCondition;

    public uCModule(IOModule ioModule) {
        this.ioModule = ioModule;

        flashMemory = new ArrayList<Byte>(FLASH_SIZE);
        loadProgramMemory();

        outputLock = new ReentrantLock();
        outputCondition = outputLock.newCondition();
    }

    @Override
    public void run() {
        outputLock.lock();
        try {

            //Wait for output from RAM
            outputCondition.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        finally {
            outputLock.unlock();
        }
    }

    //Thanks Dave L.
    // (https://stackoverflow.com/questions/140131/convert-a-string-representation-of-a-hex-dump-to-a-byte-array-using-java)
    private byte[] hexStringToByteArray(String hexString){
        int len = hexString.length();
        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i+1), 16));
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

                switch (readBytes[INTEL_REORD_TYPE]){
                    //Data
                    case 00:
                        Log.v(MY_LOG_TAG, "Loading Data from hex");

                        memoryPosition = (readBytes[INTEL_ADDRESS]<<8)|readBytes[INTEL_ADDRESS+1];

                        if (extendedSegmentAddress){
                            memoryPosition = memoryPosition + extendedAddress;
                        }
                        else if (extendedLinearAddress){
                            memoryPosition = (extendedAddress<<16)|memoryPosition;
                        }

                        Log.v(MY_LOG_TAG, "Memory position: " + memoryPosition);

                        for (int i = 0; i < dataSize; i++){
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

                        extendedAddress = (readBytes[INTEL_DATA]<<8)|readBytes[INTEL_DATA];
                        Log.v(MY_LOG_TAG, "Extended by: " + extendedAddress);
                        break;

                    //Extended Linear Address
                    case 04:
                        Log.v(MY_LOG_TAG, "Extended Linear Address from hex");
                        extendedSegmentAddress = false;
                        extendedLinearAddress = true;

                        extendedAddress = (readBytes[INTEL_DATA]<<8)|readBytes[INTEL_DATA];
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

}
