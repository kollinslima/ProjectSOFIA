package com.example.kollins.androidemulator.ATmega328P;

import android.content.Context;
import android.util.Log;

import com.example.kollins.androidemulator.UCModule_ATmega328P;
import com.example.kollins.androidemulator.uCInterfaces.DataMemory;


/**
 * Created by kollins on 3/9/18.
 */

public class DataMemory_ATmega328P implements DataMemory{

    //2kBytes
    private final int SDRAM_SIZE = 2 * ((int)Math.pow(2,10));
    private byte[] sdramMemory;

    private Context ucContext;

    public DataMemory_ATmega328P(Context ucContext){
        this.ucContext = ucContext;
        sdramMemory = new byte[SDRAM_SIZE];
    }

    @Override
    public int getMemorySize() {
        return SDRAM_SIZE;
    }

    @Override
    public synchronized void writeByte(char byteAddress, byte byteData) {
        Log.d(UCModule_ATmega328P.MY_LOG_TAG,
                String.format("Write byte SDRAM\nAddress: 0x%s, Data: 0x%02X",
                        Integer.toHexString((int)byteAddress), byteData));
    }

    @Override
    public synchronized byte readByte(char byteAddress) {
        Log.d(UCModule_ATmega328P.MY_LOG_TAG,
                String.format("Read byte SDRAM\nAddress: 0x%s, Data read: 0x%02X",
                        Integer.toHexString((int)byteAddress), sdramMemory[byteAddress]));
        return sdramMemory[byteAddress];
    }


    @Override
    public synchronized void writeBit(char byteAddress, byte bitPosition, boolean bitState) {

    }

    @Override
    public synchronized boolean readBit(char byteAddress, byte bitPosition) {
        return false;
    }
}
