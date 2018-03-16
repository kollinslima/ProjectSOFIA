package com.example.kollins.androidemulator.ATmega328P;

import android.os.Handler;
import android.util.Log;

import com.example.kollins.androidemulator.ATmega328P.IOModule_ATmega328P.OutputFragment;
import com.example.kollins.androidemulator.UCModule;
import com.example.kollins.androidemulator.uCInterfaces.DataMemory;


/**
 * Created by kollins on 3/9/18.
 */

public class DataMemory_ATmega328P implements DataMemory {

    public static final int SREG_ADDR = 0x5F;
    public static final int DDRB_ADDR = 0x24;
    public static final int PORTB_ADDR = 0x25;

    //2kBytes
    private final int SDRAM_SIZE = 2 * ((int) Math.pow(2, 10));
    private byte[] sdramMemory;

    private Handler outputHandler;

    public DataMemory_ATmega328P() {
        sdramMemory = new byte[SDRAM_SIZE];
        outputHandler = null;
        initDefaultContent();
    }

    private void initDefaultContent() {
        //Status Register
        sdramMemory[SREG_ADDR] = 0x00;

        //DDRB
        sdramMemory[DDRB_ADDR] = 0x00;

        //PORTB
        sdramMemory[PORTB_ADDR] = 0x00;
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

        checkOutputAddress(byteAddress);
        sdramMemory[byteAddress] = byteData;
    }

    private void checkOutputAddress(int byteAddress) {
        Log.v(UCModule.MY_LOG_TAG, String.format("Checking Address: 0x%s",
                        Integer.toHexString((int)byteAddress)));
        if (outputHandler == null){
            return;
        }
        else {
            if (byteAddress == PORTB_ADDR){
                outputHandler.sendEmptyMessage(OutputFragment.OUTPUT_EVENT_PORTB);
            }
        }
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

        checkOutputAddress(byteAddress);

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

    @Override
    public void setOuputHandler(Handler outputHandler) {
        this.outputHandler = outputHandler;
    }

}
