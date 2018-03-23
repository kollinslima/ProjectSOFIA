package com.example.kollins.androidemulator.ATmega328P;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.kollins.androidemulator.ATmega328P.IOModule_ATmega328P.Output.OutputFragment_ATmega328P;
import com.example.kollins.androidemulator.UCModule;
import com.example.kollins.androidemulator.uCInterfaces.DataMemory;
import com.example.kollins.androidemulator.uCInterfaces.IOModule;


/**
 * Created by kollins on 3/9/18.
 */

public class DataMemory_ATmega328P implements DataMemory {

    public static final int SREG_ADDR = 0x5F;

    public static final int PINB_ADDR = 0x23;
    public static final int DDRB_ADDR = 0x24;
    public static final int PORTB_ADDR = 0x25;

    public static final int MCUCR_ADDR = 0x55;

    //2kBytes
    private final int SDRAM_SIZE = 2 * ((int) Math.pow(2, 10));
    private byte[] sdramMemory;

    private Handler pinHandler;

    private Bundle ioBundle;

    public DataMemory_ATmega328P(Handler pinHandler) {
        sdramMemory = new byte[SDRAM_SIZE];
        this.pinHandler = pinHandler;

        ioBundle = new Bundle();

        initDefaultContent();
    }

    private void initDefaultContent() {
        //Status Register
        sdramMemory[SREG_ADDR] = 0x00;

        //DDRB
        sdramMemory[DDRB_ADDR] = 0x00;

        //PORTB
        sdramMemory[PORTB_ADDR] = 0x00;

        //MCU Control Register
        sdramMemory[MCUCR_ADDR] = 0x00;
    }


    @Override
    public int getMemorySize() {
        return SDRAM_SIZE;
    }

    @Override
    public synchronized void writeByte(int byteAddress, byte byteData) {
        Log.d(UCModule.MY_LOG_TAG,
                String.format("Write byte SDRAM\nAddress: 0x%s, Data: 0x%02X",
                        Integer.toHexString((int) byteAddress), byteData));

        if (byteAddress == PINB_ADDR){
            //Toggle bits in PORTx
            checkOutputAddress(byteAddress+2);

            for (int i = 0; i < 8; i++) {
                if ((0x01 & (byteData >> i)) == 1) {
                    writeBit(byteAddress+2, i, !readBit(byteAddress+2, i));
                }
            }
        } else {
            checkOutputAddress(byteAddress);
            sdramMemory[byteAddress] = byteData;
        }
    }

    private void checkOutputAddress(int byteAddress) {
        Log.v(UCModule.MY_LOG_TAG, String.format("Checking Address: 0x%s",
                Integer.toHexString((int) byteAddress)));

        switch (byteAddress) {
            case DDRB_ADDR:
            case PORTB_ADDR:

                Message ioMessage = new Message();

                ioBundle.putByte(IOModule.PORT_IOMESSAGE, readByte(DataMemory_ATmega328P.PORTB_ADDR));
                ioBundle.putByte(IOModule.CONFIG_IOMESSAGE, readByte(DataMemory_ATmega328P.DDRB_ADDR));

                ioMessage.what = IOModule.PORTB_EVENT;
                ioMessage.setData(ioBundle);

                pinHandler.sendMessage(ioMessage);
                break;
        }
    }

    @Override
    public synchronized byte readByte(int byteAddress) {
        Log.d(UCModule.MY_LOG_TAG,
                String.format("Read byte SDRAM\nAddress: 0x%s, Data read: 0x%02X",
                        Integer.toHexString((int) byteAddress), sdramMemory[byteAddress]));
        return sdramMemory[byteAddress];
    }


    @Override
    public synchronized void writeBit(int byteAddress, int bitPosition, boolean bitState) {
        Log.d(UCModule.MY_LOG_TAG,
                String.format("Write bit SDRAM\nAddress: 0x%s", Integer.toHexString((int) byteAddress))
                        + " position: " + bitPosition + " state: " + bitState);

        if (byteAddress == PINB_ADDR){
            //Toggle bits in PORTx
            checkOutputAddress(byteAddress+2);
            writeBit(byteAddress+2, bitPosition,!readBit(byteAddress+2, bitPosition));
        } else {
            checkOutputAddress(byteAddress);

            sdramMemory[byteAddress] = (byte) (sdramMemory[byteAddress] & (0xFF7F >> (7 - bitPosition)));   //Clear
            if (bitState) {
                sdramMemory[byteAddress] = (byte) (sdramMemory[byteAddress] | (0x01 << bitPosition));     //Set
            }
        }

    }

    public synchronized void writeIOBit(int byteAddress, int bitPosition, boolean bitState){
        sdramMemory[byteAddress] = (byte) (sdramMemory[byteAddress] & (0xFF7F >> (7 - bitPosition)));   //Clear
        if (bitState) {
            sdramMemory[byteAddress] = (byte) (sdramMemory[byteAddress] | (0x01 << bitPosition));     //Set
        }

//        Log.i(UCModule.MY_LOG_TAG,
//                String.format("Write IO byte\nAddress: 0x%s, Data: 0x%02X",
//                        Integer.toHexString((int) byteAddress), sdramMemory[byteAddress]));

    }

    @Override
    public synchronized boolean readBit(int byteAddress, int bitPosition) {
        Log.d(UCModule.MY_LOG_TAG,
                String.format("Read bit SDRAM\nAddress: 0x%s", Integer.toHexString((int) byteAddress))
                        + " position: " + bitPosition + " state: " + ((0x01 & (sdramMemory[byteAddress] >> bitPosition)) != 0));
        return (0x01 & (sdramMemory[byteAddress] >> bitPosition)) != 0;

    }


    public void setPinHandler(Handler pinHandler) {
        this.pinHandler = pinHandler;
    }

}
