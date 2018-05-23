package com.example.kollins.sofia.atmega328p;

import android.os.Environment;
import android.os.FileObserver;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.kollins.sofia.UCModule;
import com.example.kollins.sofia.ucinterfaces.ProgramMemory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by kollins on 3/8/18.
 */

public class ProgramMemory_ATmega328P implements ProgramMemory {

    private char pcPointer;

    //32kBytes, each instruction is 16bits wide
    private final int FLASH_SIZE = 32 * ((int) Math.pow(2, 10));
    private byte[] flashMemory = null;

    private final int INTEL_DATA_SIZE = 0;
    private final int INTEL_ADDRESS = 1;
    private final int INTEL_REORD_TYPE = 3;
    private final int INTEL_DATA = 4;

    private FileObserver codeObserver;
    private Handler ucHandler;

    public ProgramMemory_ATmega328P(Handler ucHandler) {
        this.ucHandler = ucHandler;
        flashMemory = new byte[FLASH_SIZE];

        pcPointer = 0;
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

    @Override
    public int getMemorySize() {
        return FLASH_SIZE;
    }

    public boolean loadProgramMemory(String hexFileLocation) {

        String state = Environment.getExternalStorageState();

        //All set to read and write data in SDCard
        if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {

            File fileDir = null;
            if (hexFileLocation.equals(UCModule.DEFAULT_HEX_LOCATION)) {
                fileDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            } else {
                fileDir = Environment.getExternalStorageDirectory();
            }

            if (fileDir.exists()) {
                File hexFile = new File(fileDir, hexFileLocation);
                if (!hexFile.exists()) {

                    //If file was not found, wait 1s and try again
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    hexFile = new File(fileDir, hexFileLocation);
                    if (!hexFile.exists()) {
                        //Not found again? Sorry, I give up...
                        Log.e(UCModule.MY_LOG_TAG, "ERROR: File not found\n" + hexFileLocation);
                        return false;
                    }

                }

                //Watch for changes in hexFile
                codeObserver = new FileObserver(hexFile.getPath().toString()) {
                    @Override
                    public void onEvent(int event, @Nullable String path) {
                        Log.i(UCModule.MY_LOG_TAG, "File event: " + event);

                        if (event == FileObserver.CLOSE_WRITE
                                || event == FileObserver.DELETE_SELF) {
                            //Send Broadcast
                            ucHandler.sendEmptyMessage(UCModule.RESET_ACTION);
                        }
                    }
                };
                codeObserver.startWatching();

                try {
                    FileInputStream fis = new FileInputStream(hexFile);
                    loadHexFile(fis);
                } catch (FileNotFoundException e) {
                    Log.e(UCModule.MY_LOG_TAG, "ERROR: Load .hex file", e);
                    return false;
                }
            }

        } else {
            Log.e(UCModule.MY_LOG_TAG, "ERROR: Can't read SD card");
            return false;
        }

        return true;
    }

    @Override
    public int loadInstruction() {

//        Log.d(UCModule.MY_LOG_TAG, "Loading instruction -> PC: " + (int) pcPointer);

        byte instPart1 = 0x00, instPart2 = 0x00;

        try {
            //little-endian read
            instPart1 = flashMemory[pcPointer * 2];
            instPart2 = flashMemory[(pcPointer * 2) + 1];
        } catch (ArrayIndexOutOfBoundsException e) {
            ucHandler.sendEmptyMessage(UCModule.STOP_ACTION);
        }

        pcPointer += 1;

        return (((0x00FF & instPart2) << 8) | (0x00FF & instPart1));
    }

    @Override
    public void setPC(int pc) {
        pcPointer = (char) pc;
//        Log.d(UCModule.MY_LOG_TAG, "Setting PC to " + (int) pcPointer);
    }

    @Override
    public int getPC() {
        return pcPointer;
    }

    @Override
    public void addToPC(int offset) {
        pcPointer += offset;
//        Log.d(UCModule.MY_LOG_TAG, "Adding " + offset + " to PC. New value: " + (int) pcPointer);
    }

    @Override
    public void writeWord(int address, int data){
        //address is a word address

        flashMemory[address*2] = (byte) (0x0000FF & data);
        flashMemory[(address*2)+1] = (byte) ((0x00FF00 & data)>>8);
    }

    @Override
    public byte readByte(int address) {
        //address is a byte address

//        Log.d(UCModule.MY_LOG_TAG,
//                String.format("Read byte FLASH\nAddress: 0x%s, Data read: 0x%02X",
//                        Integer.toHexString((int) address), flashMemory[address]));
        return flashMemory[address];
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
                Log.v(UCModule.MY_LOG_TAG, line);

                readBytes = hexStringToByteArray(line);

                //Checksum()

                dataSize = readBytes[INTEL_DATA_SIZE];
                Log.d(UCModule.MY_LOG_TAG, "Data size: " + dataSize);

                switch (readBytes[INTEL_REORD_TYPE]) {
                    //Data
                    case 00:
                        Log.v(UCModule.MY_LOG_TAG, "Loading Data from hex");

                        //Avoid sign extention
                        memoryPosition = (((int) (0x000000FF & readBytes[INTEL_ADDRESS])) << 8)
                                | (int) (0x000000FF & readBytes[INTEL_ADDRESS + 1]);

                        if (extendedSegmentAddress) {
                            memoryPosition = memoryPosition + extendedAddress;
                        } else if (extendedLinearAddress) {
                            memoryPosition = (extendedAddress << 16) | memoryPosition;
                        }

                        Log.v(UCModule.MY_LOG_TAG, "Memory position: " + memoryPosition);

                        for (int i = 0; i < dataSize; i++) {
                            flashMemory[memoryPosition + i] = readBytes[INTEL_DATA + i];
                            Log.v(UCModule.MY_LOG_TAG, String.format("Added 0x%02X to flash", readBytes[INTEL_DATA + i]));
                        }

                        break;

                    //End of File
                    case 01:
                        Log.v(UCModule.MY_LOG_TAG, "End of File from hex");
                        break;

                    //Extended Segment Address
                    case 02:
                        Log.v(UCModule.MY_LOG_TAG, "Extended Segment Address from hex");
                        extendedSegmentAddress = true;
                        extendedLinearAddress = false;

                        extendedAddress = (((int) (0x000000FF & readBytes[INTEL_ADDRESS])) << 8)
                                | (int) (0x000000FF & readBytes[INTEL_ADDRESS + 1]);

                        Log.v(UCModule.MY_LOG_TAG, "Extended by: " + extendedAddress);
                        break;

                    //Extended Linear Address
                    case 04:
                        Log.v(UCModule.MY_LOG_TAG, "Extended Linear Address from hex");
                        extendedSegmentAddress = false;
                        extendedLinearAddress = true;

                        extendedAddress = (0x0000FFFF & ((readBytes[INTEL_DATA] << 8) | readBytes[INTEL_DATA]));
                        Log.v(UCModule.MY_LOG_TAG, "Extended by: " + extendedAddress);
                        break;

                    default:
                        Log.e(UCModule.MY_LOG_TAG, "Record Type unknown: " +
                                String.format("0x%02X", readBytes[INTEL_REORD_TYPE]));
                }

            }

            reader.close();
            fis.close();
        } catch (IOException e) {
            Log.e(UCModule.MY_LOG_TAG, "ERROR: Load .hex file", e);
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.e(UCModule.MY_LOG_TAG, "ERROR: .hex file bigger than 32kB", e);
        }
    }

    public void stopCodeObserver() {
        codeObserver.stopWatching();
    }
}
