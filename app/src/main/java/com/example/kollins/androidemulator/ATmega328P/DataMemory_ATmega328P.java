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

    public static final int PINB_ADDR = 0x23;
    public static final int DDRB_ADDR = 0x24;
    public static final int PORTB_ADDR = 0x25;

    public static final int PINC_ADDR = 0x26;
    public static final int DDRC_ADDR = 0x27;
    public static final int PORTC_ADDR = 0x28;

    public static final int PIND_ADDR = 0x29;
    public static final int DDRD_ADDR = 0x2A;
    public static final int PORTD_ADDR = 0x2B;

    public static final int MCUCR_ADDR = 0x55;

    public static final int SPL_ADDR = 0x5D;
    public static final int SPH_ADDR = 0x5E;
    public static final int SREG_ADDR = 0x5F;

    //2kBytes
    private final int SDRAM_SIZE = 2 * ((int) Math.pow(2, 10));
    private byte[] sdramMemory;

    private Handler pinHandler;
    private IOModule ioModule;

    private Bundle ioBundle;

    public DataMemory_ATmega328P(IOModule ioModule) {
        sdramMemory = new byte[SDRAM_SIZE];
        this.pinHandler = (Handler) ioModule;
        this.ioModule = ioModule;

        ioBundle = new Bundle();

        initDefaultContent();
    }

    private void initDefaultContent() {
        Log.i("Config", "Configuring Memory");
        //RESET CONDITION

        //Status Register
        sdramMemory[SREG_ADDR] = 0x00;

        //DDRB
        sdramMemory[DDRB_ADDR] = 0x00;
        //PORTB
        sdramMemory[PORTB_ADDR] = 0x00;
        notify(DDRB_ADDR);

        //DDRC
        sdramMemory[DDRC_ADDR] = 0x00;
        //PORTC
        sdramMemory[PORTC_ADDR] = 0x00;
        notify(DDRC_ADDR);

        //DDRD
        sdramMemory[DDRD_ADDR] = 0x00;
        //PORTD
        sdramMemory[PORTD_ADDR] = 0x00;
        notify(DDRD_ADDR);

        //STACK (SPL and SPH)
        sdramMemory[SPL_ADDR] = 0x01;
        sdramMemory[SPH_ADDR] = 0x00;

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

        if (byteAddress == PINB_ADDR || byteAddress==PINC_ADDR || byteAddress==PIND_ADDR){
            //Toggle bits in PORTx
            boolean toggleBit;
            byte toggleByte = 0x00;

            for (int i = 0; i < 8; i++){
                toggleBit = readBit(byteAddress+2, i);

                if ((0x01 & byteData) == 1) {
                    toggleByte = (byte) (toggleByte | ((toggleBit?0:1)<<i)); //NOT
                } else {
                    toggleByte = (byte) (toggleByte | ((toggleBit?1:0)<<i));
                }

                byteData = (byte) (byteData>>1);
            }

            writeByte(byteAddress+2, toggleByte);
//            for (int i = 0; i < 8; i++) {
//                if ((0x01 & (byteData >> i)) == 1) {
//                    writeBit(byteAddress+2, i, !readBit(byteAddress+2, i));
//                }
//            }
        } else {
            sdramMemory[byteAddress] = byteData;
            notify(byteAddress);
        }
    }

    private void notify(int byteAddress) {
//        Log.i(UCModule.MY_LOG_TAG, String.format("Notify Address: 0x%s",
//                Integer.toHexString((int) byteAddress)));
        Message ioMessage;

        switch (byteAddress) {
            case DDRB_ADDR:
            case PORTB_ADDR:

                //Wait IO update
                while (ioModule.isUpdatingIO());

                ioMessage = new Message();

                ioBundle.putByte(IOModule.CONFIG_IOMESSAGE, readByte(DataMemory_ATmega328P.DDRB_ADDR));
                ioBundle.putByte(IOModule.PORT_IOMESSAGE, readByte(DataMemory_ATmega328P.PORTB_ADDR));

                ioMessage.what = IOModule.PORTB_EVENT;
                ioMessage.setData(ioBundle);

                pinHandler.sendMessage(ioMessage);
                break;

            case DDRC_ADDR:
            case PORTC_ADDR:

                //Wait IO update
                while (ioModule.isUpdatingIO());

                ioMessage = new Message();

                ioBundle.putByte(IOModule.CONFIG_IOMESSAGE, readByte(DataMemory_ATmega328P.DDRC_ADDR));
                ioBundle.putByte(IOModule.PORT_IOMESSAGE, readByte(DataMemory_ATmega328P.PORTC_ADDR));

                ioMessage.what = IOModule.PORTC_EVENT;
                ioMessage.setData(ioBundle);

                pinHandler.sendMessage(ioMessage);
                break;


            case DDRD_ADDR:
            case PORTD_ADDR:

                //Wait IO update
                while (ioModule.isUpdatingIO());

                ioMessage = new Message();

                ioBundle.putByte(IOModule.CONFIG_IOMESSAGE, readByte(DataMemory_ATmega328P.DDRD_ADDR));
                ioBundle.putByte(IOModule.PORT_IOMESSAGE, readByte(DataMemory_ATmega328P.PORTD_ADDR));

                ioMessage.what = IOModule.PORTD_EVENT;
                ioMessage.setData(ioBundle);

                pinHandler.sendMessage(ioMessage);
                break;
        }
    }

    private void notifyIO(int byteAddress) {
//        Log.i(UCModule.MY_LOG_TAG, String.format("Nority IO Address: 0x%s",
//                Integer.toHexString((int) byteAddress)));

        Message ioMessage;

        switch (byteAddress) {
            case DDRB_ADDR:
            case PORTB_ADDR:
            case PINB_ADDR:

                ioMessage = new Message();

                ioBundle.putByte(IOModule.CONFIG_IOMESSAGE, readByte(DataMemory_ATmega328P.DDRB_ADDR));
                ioBundle.putByte(IOModule.PORT_IOMESSAGE, readByte(DataMemory_ATmega328P.PORTB_ADDR));

                ioMessage.what = IOModule.PORTB_EVENT;
                ioMessage.setData(ioBundle);

                pinHandler.sendMessage(ioMessage);
                break;

            case DDRC_ADDR:
            case PORTC_ADDR:
            case PINC_ADDR:

                ioMessage = new Message();

                ioBundle.putByte(IOModule.CONFIG_IOMESSAGE, readByte(DataMemory_ATmega328P.DDRC_ADDR));
                ioBundle.putByte(IOModule.PORT_IOMESSAGE, readByte(DataMemory_ATmega328P.PORTC_ADDR));

                ioMessage.what = IOModule.PORTC_EVENT;
                ioMessage.setData(ioBundle);

                pinHandler.sendMessage(ioMessage);
                break;

            case DDRD_ADDR:
            case PORTD_ADDR:
            case PIND_ADDR:

                ioMessage = new Message();

                ioBundle.putByte(IOModule.CONFIG_IOMESSAGE, readByte(DataMemory_ATmega328P.DDRD_ADDR));
                ioBundle.putByte(IOModule.PORT_IOMESSAGE, readByte(DataMemory_ATmega328P.PORTD_ADDR));

                ioMessage.what = IOModule.PORTD_EVENT;
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

        if (byteAddress == PINB_ADDR || byteAddress==PINC_ADDR || byteAddress==PIND_ADDR){
            //Toggle bits in PORTx
            if (bitState) {
                writeBit(byteAddress + 2, bitPosition, !readBit(byteAddress + 2, bitPosition));
            }
        } else {
            sdramMemory[byteAddress] = (byte) (sdramMemory[byteAddress] & (0xFF7F >> (7 - bitPosition)));   //Clear
            if (bitState) {
                sdramMemory[byteAddress] = (byte) (sdramMemory[byteAddress] | (0x01 << bitPosition));     //Set
            }
            notify(byteAddress);
        }
    }

    public synchronized void writeIOBit(int byteAddress, int bitPosition, boolean bitState){

        Log.i(UCModule.MY_LOG_TAG,
                String.format("Write IO bit SDRAM\nAddress: 0x%s", Integer.toHexString((int) byteAddress))
                        + " position: " + bitPosition + " state: " + bitState);

        sdramMemory[byteAddress] = (byte) (sdramMemory[byteAddress] & (0xFF7F >> (7 - bitPosition)));   //Clear
        if (bitState) {
            sdramMemory[byteAddress] = (byte) (sdramMemory[byteAddress] | (0x01 << bitPosition));     //Set
        }
        notifyIO(byteAddress);

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
