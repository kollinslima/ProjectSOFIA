package com.example.kollins.androidemulator.ATmega328P;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.kollins.androidemulator.ATmega328P.IOModule_ATmega328P.IOModule_ATmega328P;
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

    public static final int TIFR0_ADDR = 0x35;
    public static final int TIFR1_ADDR = 0x36;
    public static final int TIFR2_ADDR = 0x37;

    public static final int PCIFR_ADDR = 0x3B;
    public static final int EIFR_ADDR = 0x3C;
    public static final int EIMSK_ADDR = 0x3D;
    public static final int GPIOR0_ADDR = 0x3E;
    public static final int EECR_ADDR = 0x3F;
    public static final int EEDR_ADDR = 0x40;
    public static final int EEARL_ADDR = 0x41;
    public static final int EEARH_ADDR = 0x42;
    public static final int GTCCR_ADDR = 0x43;
    public static final int TCCR0A_ADDR = 0x44;
    public static final int TCCR0B_ADDR = 0x45;
    public static final int TCNT0_ADDR = 0x46;
    public static final int OCR0A_ADDR = 0x47;
    public static final int OCR0B_ADDR = 0x48;

    public static final int GPIOR1_ADDR = 0x4A;
    public static final int GPIOR2_ADDR = 0x4B;
    public static final int SPCR0_ADDR = 0x4C;
    public static final int SPSR0_ADDR = 0x4D;
    public static final int SPDR0_ADDR = 0x4E;

    public static final int ACSR_ADDR = 0x50;
    public static final int DWDR_ADDR = 0x51;

    public static final int SMCR_ADDR = 0x53;
    public static final int MCUSR_ADDR = 0x54;
    public static final int MCUCR_ADDR = 0x55;

    public static final int SPMCSR_ADDR = 0x57;

    public static final int SPL_ADDR = 0x5D;
    public static final int SPH_ADDR = 0x5E;
    public static final int SREG_ADDR = 0x5F;
    public static final int WDTCSR_ADDR = 0x60;
    public static final int CLKPR_ADDR = 0x61;

    public static final int PRR_ADDR = 0x64;

    public static final int OSCCAL_ADDR = 0x66;

    public static final int PCICR_ADDR = 0x68;
    public static final int EICRA_ADDR = 0x69;

    public static final int PCMSK0_ADDR = 0x6B;
    public static final int PCMSK1_ADDR = 0x6C;
    public static final int PCMSK2_ADDR = 0x6D;
    public static final int TIMSK0_ADDR = 0x6E;
    public static final int TIMSK1_ADDR = 0x6F;
    public static final int TIMSK2_ADDR = 0x70;

    public static final int ADCL_ADDR = 0x78;
    public static final int ADCH_ADDR = 0x79;
    public static final int ADCSRA_ADDR = 0x7A;
    public static final int ADCSRB_ADDR = 0x7B;
    public static final int ADMUX_ADDR = 0x7C;

    public static final int DIDR0_ADDR = 0x7E;
    public static final int DIDR1_ADDR = 0x7F;
    public static final int TCCR1A_ADDR = 0x80;
    public static final int TCCR1B_ADDR = 0x81;
    public static final int TCCR1C_ADDR = 0x82;

    public static final int TCNT1L_ADDR = 0x84;
    public static final int TCNT1H_ADDR = 0x85;
    public static final int ICR1L_ADDR = 0x86;
    public static final int ICR1H_ADDR = 0x87;
    public static final int OCR1AL_ADDR = 0x88;
    public static final int OCR1AH_ADDR = 0x89;
    public static final int OCR1BL_ADDR = 0x8A;
    public static final int OCR1BH_ADDR = 0x8B;

    public static final int TCCR2A_ADDR = 0xB0;
    public static final int TCCR2B_ADDR = 0xB1;
    public static final int TCNT2_ADDR = 0xB2;
    public static final int OCR2A_ADDR = 0xB3;
    public static final int OCR2B_ADDR = 0xB4;

    public static final int ASSR_ADDR = 0xB6;

    public static final int TWBR_ADDR = 0xB8;
    public static final int TWSR_ADDR = 0xB9;
    public static final int TWAR_ADDR = 0xBA;
    public static final int TWDR_ADDR = 0xBB;
    public static final int TWCR_ADDR = 0xBC;
    public static final int TWAMR_ADDR = 0xBD;

    public static final int UCSR0A_ADDR = 0xC0;
    public static final int UCSR0B_ADDR = 0xC1;
    public static final int UCSR0C_ADDR = 0xC2;

    public static final int UBRR0L_ADDR = 0xC4;
    public static final int UBRR0H_ADDR = 0xC5;
    public static final int UDR0_ADDR = 0xC6;

    //2kBytes SDRAM + 32 Registers + 64 I/O Registers + 160 Ext I/O Registers
    private final int SDRAM_SIZE = (2 * ((int) Math.pow(2, 10))) + 32 + 64 + 160;
    private byte[] sdramMemory;

    private Handler pinHandler;
    private IOModule ioModule;

    private Bundle ioBundle;

    private byte timer1_TEMP;

    public DataMemory_ATmega328P(IOModule ioModule) {
        sdramMemory = new byte[SDRAM_SIZE];
        this.pinHandler = (Handler) ioModule;
        this.ioModule = ioModule;

        ioBundle = new Bundle();

        initDefaultContent();
    }

    private void initDefaultContent() {
        Log.i("Config", "Configuring Memory");

        /*****************RESET CONDITION***************/
        /***********************************************/
        sdramMemory[DDRB_ADDR] = 0x00;
        sdramMemory[PORTB_ADDR] = 0x00;
        notify(DDRB_ADDR);
        sdramMemory[DDRC_ADDR] = 0x00;
        sdramMemory[PORTC_ADDR] = 0x00;
        notify(DDRC_ADDR);
        sdramMemory[DDRD_ADDR] = 0x00;
        sdramMemory[PORTD_ADDR] = 0x00;
        notify(DDRD_ADDR);
        /***********************************************/
        sdramMemory[TIFR0_ADDR] = 0x00;
        sdramMemory[TIFR1_ADDR] = 0x00;
        sdramMemory[TIFR2_ADDR] = 0x00;
        /***********************************************/
        sdramMemory[PCIFR_ADDR] = 0x00;
        sdramMemory[EIFR_ADDR] = 0x00;
        sdramMemory[EIMSK_ADDR] = 0x00;
        sdramMemory[GPIOR0_ADDR] = 0x00;
        sdramMemory[EECR_ADDR] = 0x00;
        sdramMemory[EEDR_ADDR] = 0x00;
        sdramMemory[GTCCR_ADDR] = 0x00;
        sdramMemory[TCCR0A_ADDR] = 0x00;
        sdramMemory[TCCR0B_ADDR] = 0x00;
        sdramMemory[TCNT0_ADDR] = 0x00;
        sdramMemory[OCR0A_ADDR] = 0x00;
        sdramMemory[OCR0B_ADDR] = 0x00;
        /***********************************************/
        sdramMemory[GPIOR1_ADDR] = 0x00;
        sdramMemory[GPIOR2_ADDR] = 0x00;
        sdramMemory[SPCR0_ADDR] = 0x00;
        sdramMemory[SPSR0_ADDR] = 0x00;
        /***********************************************/
        sdramMemory[ACSR_ADDR] = 0x00;
        sdramMemory[DWDR_ADDR] = 0x00;
        /***********************************************/
        sdramMemory[SMCR_ADDR] = 0x00;
        sdramMemory[MCUSR_ADDR] = 0x00;
        sdramMemory[MCUCR_ADDR] = 0x00;
        /***********************************************/
        sdramMemory[SPMCSR_ADDR] = 0x00;
        /***********************************************/
        sdramMemory[SPL_ADDR] = 0x01;
        sdramMemory[SPH_ADDR] = 0x00;
        sdramMemory[SREG_ADDR] = 0x00;
        sdramMemory[WDTCSR_ADDR] = 0x00;
        sdramMemory[CLKPR_ADDR] = 0x00;
        /***********************************************/
        sdramMemory[PRR_ADDR] = 0x00;
        /***********************************************/
        sdramMemory[PCICR_ADDR] = 0x00;
        sdramMemory[EICRA_ADDR] = 0x00;
        /***********************************************/
        sdramMemory[PCMSK0_ADDR] = 0x00;
        sdramMemory[PCMSK1_ADDR] = 0x00;
        sdramMemory[PCMSK2_ADDR] = 0x00;
        sdramMemory[TIMSK0_ADDR] = 0x00;
        sdramMemory[TIMSK1_ADDR] = 0x00;
        sdramMemory[TIMSK2_ADDR] = 0x00;
        /***********************************************/
        sdramMemory[ADCL_ADDR] = 0x00;
        sdramMemory[ADCH_ADDR] = 0x00;
        sdramMemory[ADCSRA_ADDR] = 0x00;
        sdramMemory[ADCSRB_ADDR] = 0x00;
        sdramMemory[ADMUX_ADDR] = 0x00;
        /***********************************************/
        sdramMemory[DIDR0_ADDR] = 0x00;
        sdramMemory[DIDR1_ADDR] = 0x00;
        sdramMemory[TCCR1A_ADDR] = 0x00;
        sdramMemory[TCCR1B_ADDR] = 0x00;
        sdramMemory[TCCR1C_ADDR] = 0x00;
        /***********************************************/
        sdramMemory[TCNT1L_ADDR] = 0x00;
        sdramMemory[TCNT1H_ADDR] = 0x00;
        sdramMemory[ICR1L_ADDR] = 0x00;
        sdramMemory[ICR1H_ADDR] = 0x00;
        sdramMemory[OCR1AL_ADDR] = 0x00;
        sdramMemory[OCR1AH_ADDR] = 0x00;
        sdramMemory[OCR1BL_ADDR] = 0x00;
        sdramMemory[OCR1BH_ADDR] = 0x00;
        /***********************************************/
        sdramMemory[TCCR2A_ADDR] = 0x00;
        sdramMemory[TCCR2B_ADDR] = 0x00;
        sdramMemory[TCNT2_ADDR] = 0x00;
        sdramMemory[OCR2A_ADDR] = 0x00;
        sdramMemory[OCR2B_ADDR] = 0x00;
        /***********************************************/
        sdramMemory[ASSR_ADDR] = 0x00;
        /***********************************************/
        sdramMemory[TWBR_ADDR] = 0x00;
        sdramMemory[TWSR_ADDR] = -8; //0xF8 two complement
        sdramMemory[TWAR_ADDR] = -2; //0xFE two complement
        sdramMemory[TWDR_ADDR] = -1; //0xFF two complement
        sdramMemory[TWCR_ADDR] = 0x00;
        sdramMemory[TWAMR_ADDR] = 0x00;
        /***********************************************/
        sdramMemory[UCSR0A_ADDR] = 0x00;
        sdramMemory[UCSR0B_ADDR] = 0x00;
        sdramMemory[UCSR0C_ADDR] = 0x06;
        /***********************************************/
        sdramMemory[UBRR0L_ADDR] = 0x00;
        sdramMemory[UBRR0H_ADDR] = 0x00;
        sdramMemory[UDR0_ADDR] = 0x00;
        /***********************************************/

    }

    @Override
    public int getMemorySize() {
        return SDRAM_SIZE;
    }

    private void notify(int byteAddress) {
//        Log.i(UCModule.MY_LOG_TAG, String.format("Notify Address: 0x%s",
//                Integer.toHexString((int) byteAddress)));
        Message ioMessage;

        switch (byteAddress) {
            case DDRB_ADDR:
            case PORTB_ADDR:

                ioMessage = new Message();

                ioBundle.putByte(IOModule.CONFIG_IOMESSAGE, readByte(DataMemory_ATmega328P.DDRB_ADDR));
                ioBundle.putByte(IOModule.PORT_IOMESSAGE, readByte(DataMemory_ATmega328P.PORTB_ADDR));

                ioMessage.what = IOModule.PORTB_EVENT;

                ioMessage.setData(ioBundle);

                pinHandler.dispatchMessage(ioMessage);

                break;

            case DDRC_ADDR:
            case PORTC_ADDR:


                ioMessage = new Message();

                ioBundle.putByte(IOModule.CONFIG_IOMESSAGE, readByte(DataMemory_ATmega328P.DDRC_ADDR));
                ioBundle.putByte(IOModule.PORT_IOMESSAGE, readByte(DataMemory_ATmega328P.PORTC_ADDR));

                ioMessage.what = IOModule.PORTC_EVENT;
                ioMessage.setData(ioBundle);

                pinHandler.dispatchMessage(ioMessage);

                break;


            case DDRD_ADDR:
            case PORTD_ADDR:

                ioMessage = new Message();

                ioBundle.putByte(IOModule.CONFIG_IOMESSAGE, readByte(DataMemory_ATmega328P.DDRD_ADDR));
                ioBundle.putByte(IOModule.PORT_IOMESSAGE, readByte(DataMemory_ATmega328P.PORTD_ADDR));

                ioMessage.what = IOModule.PORTD_EVENT;
                ioMessage.setData(ioBundle);

                pinHandler.dispatchMessage(ioMessage);

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

                pinHandler.dispatchMessage(ioMessage);
                break;

            case DDRC_ADDR:
            case PORTC_ADDR:
            case PINC_ADDR:

                ioMessage = new Message();

                ioBundle.putByte(IOModule.CONFIG_IOMESSAGE, readByte(DataMemory_ATmega328P.DDRC_ADDR));
                ioBundle.putByte(IOModule.PORT_IOMESSAGE, readByte(DataMemory_ATmega328P.PORTC_ADDR));

                ioMessage.what = IOModule.PORTC_EVENT;
                ioMessage.setData(ioBundle);

                pinHandler.dispatchMessage(ioMessage);
                break;

            case DDRD_ADDR:
            case PORTD_ADDR:
            case PIND_ADDR:

                ioMessage = new Message();

                ioBundle.putByte(IOModule.CONFIG_IOMESSAGE, readByte(DataMemory_ATmega328P.DDRD_ADDR));
                ioBundle.putByte(IOModule.PORT_IOMESSAGE, readByte(DataMemory_ATmega328P.PORTD_ADDR));

                ioMessage.what = IOModule.PORTD_EVENT;
                ioMessage.setData(ioBundle);

                pinHandler.dispatchMessage(ioMessage);
                break;
        }
    }

    @Override
    public synchronized byte readByte(int byteAddress) {
//        Log.d(UCModule.MY_LOG_TAG,
//                String.format("Read byte SDRAM\nAddress: 0x%s, Data read: 0x%02X",
//                        Integer.toHexString((int) byteAddress), sdramMemory[byteAddress]));

        if (byteAddress == TCCR0B_ADDR) {
            return (byte) (0x0F & sdramMemory[byteAddress]);    //Force math always read as 0
        } else if (byteAddress == TCNT1L_ADDR){
            timer1_TEMP = sdramMemory[TCNT1H_ADDR];
            return sdramMemory[byteAddress];
        } else if (byteAddress == OCR1AL_ADDR){
            timer1_TEMP = sdramMemory[OCR1AH_ADDR];
            return sdramMemory[byteAddress];
        } else if (byteAddress == OCR1BL_ADDR){
            timer1_TEMP = sdramMemory[OCR1BH_ADDR];
            return sdramMemory[byteAddress];
        } else if (byteAddress == ICR1L_ADDR){
            timer1_TEMP = sdramMemory[ICR1H_ADDR];
            return sdramMemory[byteAddress];
        } else if (byteAddress == TCNT1H_ADDR
                || byteAddress == OCR1AH_ADDR
                || byteAddress == OCR1BH_ADDR
                || byteAddress == ICR1H_ADDR){
            return timer1_TEMP;
        }
        return sdramMemory[byteAddress];
    }

    @Override
    public synchronized void writeByte(int byteAddress, byte byteData) {
//        Log.d(UCModule.MY_LOG_TAG,
//                String.format("Write byte SDRAM\nAddress: 0x%s, Data: 0x%02X",
//                        Integer.toHexString((int) byteAddress), byteData));

        if (byteAddress == PINB_ADDR || byteAddress == PINC_ADDR || byteAddress == PIND_ADDR) {
            //Toggle bits in PORTx
            boolean toggleBit;
            byte toggleByte = 0x00;

            for (int i = 0; i < 8; i++) {
                toggleBit = readBit(byteAddress + 2, i);

                if ((0x01 & byteData) == 1) {
                    toggleByte = (byte) (toggleByte | ((toggleBit ? 0 : 1) << i)); //NOT
                } else {
                    toggleByte = (byte) (toggleByte | ((toggleBit ? 1 : 0) << i));
                }

                byteData = (byte) (byteData >> 1);
            }

            writeByte(byteAddress + 2, toggleByte);
//            for (int i = 0; i < 8; i++) {
//                if ((0x01 & (byteData >> i)) == 1) {
//                    writeBit(byteAddress+2, i, !readBit(byteAddress+2, i));
//                }
//            }
        } else if ((byteAddress == EIFR_ADDR
                || byteAddress == PCIFR_ADDR
                || byteAddress == TIFR0_ADDR
                || byteAddress == TIFR1_ADDR)) {
            //Clear Flags
            sdramMemory[byteAddress] = 0x00;
        } else if (byteAddress == GTCCR_ADDR) {
            //Synchronization Mode
            sdramMemory[byteAddress] = byteData;

            if (!readBit(GTCCR_ADDR, 7)) {
                writeBit(GTCCR_ADDR, 0, false);
                writeBit(GTCCR_ADDR, 1, false);
            }
        } else if (byteAddress == TCNT1H_ADDR
                || byteAddress == OCR1AH_ADDR
                || byteAddress == OCR1BH_ADDR){
            timer1_TEMP = byteData;
        } else if (byteAddress == TCNT1L_ADDR){
            sdramMemory[byteAddress] = byteData;
            sdramMemory[TCNT1H_ADDR] = timer1_TEMP;
        } else if (byteAddress == OCR1AL_ADDR){
            sdramMemory[byteAddress] = byteData;
            sdramMemory[OCR1AH_ADDR] = timer1_TEMP;
        } else if (byteAddress == OCR1BL_ADDR){
            sdramMemory[byteAddress] = byteData;
            sdramMemory[OCR1BH_ADDR] = timer1_TEMP;
        } else if (byteAddress == ICR1H_ADDR){
            if (Timer1_ATmega328P.enableICRWrite){
                timer1_TEMP = byteData;
            }
        } else if (byteAddress == ICR1L_ADDR){
            if (Timer1_ATmega328P.enableICRWrite){
                sdramMemory[byteAddress] = byteData;
                sdramMemory[ICR1H_ADDR] = timer1_TEMP;
            }
        } else {
            sdramMemory[byteAddress] = byteData;
            notify(byteAddress);
        }
    }

    @Override
    public synchronized void writeBit(int byteAddress, int bitPosition, boolean bitState) {
//        Log.d(UCModule.MY_LOG_TAG,
//                String.format("Write bit SDRAM\nAddress: 0x%s", Integer.toHexString((int) byteAddress))
//                        + " position: " + bitPosition + " state: " + bitState);

        if (byteAddress == PINB_ADDR || byteAddress == PINC_ADDR || byteAddress == PIND_ADDR) {
            //Toggle bits in PORTx
            if (bitState) {
                writeBit(byteAddress + 2, bitPosition, !readBit(byteAddress + 2, bitPosition));
            }
        } else if (byteAddress == EIFR_ADDR
                || byteAddress == PCIFR_ADDR
                || byteAddress == TIFR0_ADDR
                || byteAddress == TIFR1_ADDR) {
            //Clear Flag
            sdramMemory[byteAddress] = (byte) (sdramMemory[byteAddress] & (0xFF7F >> (7 - bitPosition)));

        } else if (byteAddress == GTCCR_ADDR) {
            //Synchronization Mode
            sdramMemory[byteAddress] = (byte) (sdramMemory[byteAddress] & (0xFF7F >> (7 - bitPosition)));   //Clear
            if (bitState) {
                sdramMemory[byteAddress] = (byte) (sdramMemory[byteAddress] | (0x01 << bitPosition));     //Set
            }
            if (!readBit(GTCCR_ADDR, 7)) {
                sdramMemory[byteAddress] = (byte) (sdramMemory[byteAddress] & (0xFF7F >> (7 - 1)));
                sdramMemory[byteAddress] = (byte) (sdramMemory[byteAddress] & (0xFF7F >> (7 - 0)));
            }

        } else {
            sdramMemory[byteAddress] = (byte) (sdramMemory[byteAddress] & (0xFF7F >> (7 - bitPosition)));   //Clear
            if (bitState) {
                sdramMemory[byteAddress] = (byte) (sdramMemory[byteAddress] | (0x01 << bitPosition));     //Set
            }
            notify(byteAddress);
        }
    }

    public synchronized void writeIOBit(int byteAddress, int bitPosition, boolean bitState) {
//        Log.i(UCModule.MY_LOG_TAG,
//                String.format("Write IO bit SDRAM\nAddress: 0x%s", Integer.toHexString((int) byteAddress))
//                        + " position: " + bitPosition + " state: " + bitState);

        sdramMemory[byteAddress] = (byte) (sdramMemory[byteAddress] & (0xFF7F >> (7 - bitPosition)));   //Clear
        if (bitState) {
            sdramMemory[byteAddress] = (byte) (sdramMemory[byteAddress] | (0x01 << bitPosition));     //Set
        }
        notifyIO(byteAddress);

    }

    public synchronized void writeIOByte(int byteAddress, byte byteData) {
        if (byteAddress == ICR1H_ADDR){
            timer1_TEMP = byteData;
        } else if (byteAddress == ICR1L_ADDR){
            sdramMemory[byteAddress] = byteData;
            sdramMemory[ICR1H_ADDR] = timer1_TEMP;
        }
        sdramMemory[byteAddress] = byteData;
    }

    @Override
    public synchronized boolean readBit(int byteAddress, int bitPosition) {
//        Log.d(UCModule.MY_LOG_TAG,
//                String.format("Read bit SDRAM\nAddress: 0x%s", Integer.toHexString((int) byteAddress))
//                        + " position: " + bitPosition + " state: " + ((0x01 & (sdramMemory[byteAddress] >> bitPosition)) != 0));

        if (byteAddress == TCCR0B_ADDR && (bitPosition == 7 || bitPosition == 6)) {
            return false;   //Force math always read as 0;
        }
        return (0x01 & (sdramMemory[byteAddress] >> bitPosition)) != 0;

    }


    public void setPinHandler(Handler pinHandler) {
        this.pinHandler = pinHandler;
    }

    public synchronized void writeFeedback(int byteAddress, int bitPosition, boolean bitState) {

        sdramMemory[byteAddress] = (byte) (sdramMemory[byteAddress] & (0xFF7F >> (7 - bitPosition)));   //Clear
        if (bitState) {
            sdramMemory[byteAddress] = (byte) (sdramMemory[byteAddress] | (0x01 << bitPosition));     //Set
        }
    }

    public boolean readForceMatchA_timer0() {
        return (0x01 & (sdramMemory[TCCR0B_ADDR] >> 7)) != 0;
    }

    public boolean readForceMatchB_timer0() {
        return (0x01 & (sdramMemory[TCCR0B_ADDR] >> 6)) != 0;
    }

    public boolean readForceMatchA_timer1() {
        return (0x01 & (sdramMemory[TCCR1C_ADDR] >> 7)) != 0;
    }

    public boolean readForceMatchB_timer1() {
        return (0x01 & (sdramMemory[TCCR1C_ADDR] >> 6)) != 0;
    }

}
