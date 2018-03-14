package com.example.kollins.androidemulator.ATmega328P;

import android.content.Context;
import android.util.Log;

import com.example.kollins.androidemulator.UCModule;
import com.example.kollins.androidemulator.uCInterfaces.DataMemory;


/**
 * Created by kollins on 3/9/18.
 */

public class DataMemory_ATmega328P implements DataMemory {

    public static final int SREG_ADDR = 0x5F;

    //2kBytes
    private final int SDRAM_SIZE = 2 * ((int) Math.pow(2, 10));
    private byte[] sdramMemory;

    private Context ucContext;

    public DataMemory_ATmega328P(Context ucContext) {
        this.ucContext = ucContext;
        sdramMemory = new byte[SDRAM_SIZE];

        initDefaultContent();

    }

    private void initDefaultContent() {
        //Status Register
        sdramMemory[0x5F] = 0x00;

        //DDRB
        sdramMemory[0x24] = 0x00;

        //PORTB
        sdramMemory[0x25] = 0x00;
    }


    @Override
    public int getMemorySize() {
        return SDRAM_SIZE;
    }

    @Override
    public synchronized void writeByte(int byteAddress, byte byteData) {
        Log.d(UCModule.MY_LOG_TAG,
                String.format("Write byte SDRAM\nAddress: 0x%s, Data: 0x%02X",
                        Integer.toHexString((int)byteAddress), byteData));

        sdramMemory[byteAddress] = byteData;
    }

    @Override
    public synchronized byte readByte(int byteAddress) {
        Log.d(UCModule.MY_LOG_TAG,
                String.format("Read byte SDRAM\nAddress: 0x%s, Data read: 0x%02X",
                        Integer.toHexString((int)byteAddress), sdramMemory[byteAddress]));
        return sdramMemory[byteAddress];
    }


    @Override
    public synchronized void writeBit(int byteAddress, int bitPosition, boolean bitState) {
        Log.d(UCModule.MY_LOG_TAG,
                String.format("Write bit SDRAM\nAddress: 0x%s",Integer.toHexString((int)byteAddress))
        + " position: " + bitPosition + " state: " + bitState);
        sdramMemory[byteAddress] = (byte) (sdramMemory[byteAddress] & (0xFF7F >> (7 - bitPosition)));   //Clear
        if(bitState){
            sdramMemory[byteAddress] = (byte) (sdramMemory[byteAddress] | (0x01 << bitPosition));     //Set
        }
    }

    @Override
    public synchronized boolean readBit(int byteAddress, int bitPosition) {
        Log.d(UCModule.MY_LOG_TAG,
                String.format("Read bit SDRAM\nAddress: 0x%s",Integer.toHexString((int)byteAddress))
                        + " position: " + bitPosition + " state: " + ((0x01 & (sdramMemory[byteAddress] >> bitPosition)) != 0));
        return (0x01 & (sdramMemory[byteAddress] >> bitPosition)) != 0;

    }

}
