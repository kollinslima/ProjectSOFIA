/*
 * Copyright 2018
 * Kollins Lima (kollins.lima@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.kollins.sofia.atmega328p;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import java.util.Timer;
import java.util.TimerTask;

import com.example.kollins.sofia.UCModule;
import com.example.kollins.sofia.UCModule_View;
import com.example.kollins.sofia.extra.memory_map.MemoryAdapter;
import com.example.kollins.sofia.extra.memory_map.MemoryFragment;
import com.example.kollins.sofia.ucinterfaces.DataMemory;
import com.example.kollins.sofia.ucinterfaces.IOModule;


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

    private static final int RAMEND = 0x08FF;
    public static final int SDRAM_EXTERNAL_SIZE = (2 * ((int) Math.pow(2, 10)));

    //2kBytes external SDRAM + 32 Registers + 64 I/O Registers + 160 Ext I/O Registers
    public static final int SDRAM_SIZE_TOTAL = SDRAM_EXTERNAL_SIZE + 32 + 64 + 160;
    private byte[] sdramMemory;

    private static final short MEM_MAP_TIMEOUT = 800;  //ms
    private Timer timerMemMap;
    private boolean updateMemMapFlag;

    private int memoryUsageMeasure;
    private boolean[] memoryUsage;
    private int oldStackPointer;

    private Handler pinHandler;
    private IOModule ioModule;

    private Bundle ioBundle;

    private byte timer1_TEMP;
    private boolean flagOCR1AReady, stackPointerReady;
    private boolean timer1WriteEnable, adcWriteEnable;

    public DataMemory_ATmega328P(IOModule ioModule) {
        sdramMemory = new byte[SDRAM_SIZE_TOTAL];
        memoryUsageMeasure = 0;
        memoryUsage = new boolean[SDRAM_EXTERNAL_SIZE];
        oldStackPointer = RAMEND;

        this.pinHandler = (Handler) ioModule;
        this.ioModule = ioModule;

        ioBundle = new Bundle();
        flagOCR1AReady = false;
        stackPointerReady = false;
        timer1WriteEnable = false;
        adcWriteEnable = true;

        updateMemMapFlag = false;

        initDefaultContent();
    }

    public void startTimer(){
        timerMemMap = new Timer();
        timerMemMap.scheduleAtFixedRate(new TimerMemoryMap(), MEM_MAP_TIMEOUT, MEM_MAP_TIMEOUT);
    }

    public void stopTimer(){
        try{
            updateMemMapFlag = false;
            timerMemMap.cancel();
        } catch (NullPointerException e){
            Log.e(UCModule.MY_LOG_TAG, "ERROR: Timer not running", e);
        }
    }

    private void initDefaultContent() {
        Log.i("Config", "Configuring Memory");

        /*****************RESET CONDITION***************/
        /***********************************************/
        sdramMemory[DDRB_ADDR] = 0x00;
        sdramMemory[PORTB_ADDR] = 0x00;
        new Notify().execute(DDRB_ADDR);
        sdramMemory[DDRC_ADDR] = 0x00;
        sdramMemory[PORTC_ADDR] = 0x00;
        new Notify().execute(DDRC_ADDR);
        sdramMemory[DDRD_ADDR] = 0x00;
        sdramMemory[PORTD_ADDR] = 0x00;
        new Notify().execute(DDRD_ADDR);
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
        sdramMemory[UCSR0A_ADDR] = 0x20;
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
        return SDRAM_EXTERNAL_SIZE;
    }

    @Override
    public synchronized byte readByte(int byteAddress) {
//        Log.d(UCModule.MY_LOG_TAG,
//                String.format("Read byte SDRAM\nAddress: 0x%s, Data read: 0x%02X",
//                        Integer.toHexString((int) byteAddress), sdramMemory[byteAddress]));

        if (byteAddress == TCCR0B_ADDR) {
            return (byte) (0x0F & sdramMemory[byteAddress]);    //Force math always read as 0
        } else if (byteAddress == TCNT1L_ADDR) {
            timer1_TEMP = sdramMemory[TCNT1H_ADDR];
            return sdramMemory[byteAddress];
        } else if (byteAddress == OCR1AL_ADDR) {
            timer1_TEMP = sdramMemory[OCR1AH_ADDR];
            return sdramMemory[byteAddress];
        } else if (byteAddress == OCR1BL_ADDR) {
            timer1_TEMP = sdramMemory[OCR1BH_ADDR];
            return sdramMemory[byteAddress];
        } else if (byteAddress == ICR1L_ADDR) {
            timer1_TEMP = sdramMemory[ICR1H_ADDR];
            return sdramMemory[byteAddress];
        } else if (byteAddress == ADCL_ADDR) {
            adcWriteEnable = false;
            return sdramMemory[byteAddress];
        } else if (byteAddress == ADCH_ADDR) {
            adcWriteEnable = true;
            return sdramMemory[byteAddress];
        } else if (byteAddress == TCNT1H_ADDR
                || byteAddress == OCR1AH_ADDR
                || byteAddress == OCR1BH_ADDR
                || byteAddress == ICR1H_ADDR) {
            return timer1_TEMP;
        } else if (byteAddress == UDR0_ADDR) {
            UCModule.interruptionModule.receiveBufferReadedUSART();
            return USART_ATmega328P.receiver_UDR0;
        }
        return sdramMemory[byteAddress];
    }


    public synchronized byte readIOByte(int byteAddress) {
//        Log.d(UCModule.MY_LOG_TAG,
//                String.format("Read IO byte SDRAM\nAddress: 0x%s, Data read: 0x%02X",
//                        Integer.toHexString((int) byteAddress), sdramMemory[byteAddress]));
        if (byteAddress == UDR0_ADDR) {
            UCModule.interruptionModule.receiveBufferReadedUSART();
            return USART_ATmega328P.receiver_UDR0;
        }

        return sdramMemory[byteAddress];
    }

    private void updateMemoryUsage(int byteAddress) {

        int memoryUsageAddress = byteAddress - 0x0100;

        //EXTERNAL SDRAM START FOR UNO
        if (byteAddress >= 0x0100) {
            if (!memoryUsage[memoryUsageAddress]) {
                memoryUsage[memoryUsageAddress] = true;
                memoryUsageMeasure += 1;
            }
        }

        int stackPointer = (sdramMemory[SPH_ADDR] << 8) |
                (0x000000FF & sdramMemory[SPL_ADDR]);

        if (stackPointer == RAMEND) {
            stackPointerReady = true;
        }

        if (stackPointerReady) {
            //POP
            if (stackPointer > oldStackPointer) {
                memoryUsage[oldStackPointer - 0x0100] = false;
                memoryUsageMeasure -= 1;
            }
            oldStackPointer = stackPointer;
        }
    }

    @Override
    public int getMemoryUsage() {
//        return ((double) memoryUsageMeasure / SDRAM_EXTERNAL_SIZE);
        return memoryUsageMeasure;
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

        } else if (byteAddress == EIFR_ADDR
                || byteAddress == PCIFR_ADDR
                || byteAddress == TIFR0_ADDR
                || byteAddress == TIFR1_ADDR
                || byteAddress == TIFR2_ADDR) {
            //Clear Flags
            sdramMemory[byteAddress] = 0x00;
        } else if (byteAddress == ADCSRA_ADDR) {
            //Clear Flag
            byteData = (byte) (0x00EF & byteData);
            sdramMemory[byteAddress] = byteData;
        } else if (byteAddress == UCSR0A_ADDR) {
            //Clear Flag
            byteData = (byte) (0x00BF & byteData);
            sdramMemory[byteAddress] = byteData;
        } else if (byteAddress == GTCCR_ADDR) {
            //Synchronization Mode
            sdramMemory[byteAddress] = byteData;

            if (!readBit(GTCCR_ADDR, 7)) {
                writeBit(GTCCR_ADDR, 0, false);
                writeBit(GTCCR_ADDR, 1, false);
            }
        } else if (byteAddress == TCNT1H_ADDR
                || byteAddress == OCR1AH_ADDR
                || byteAddress == OCR1BH_ADDR) {
            timer1_TEMP = byteData;
        } else if (byteAddress == TCNT1L_ADDR) {
            timer1WriteEnable = false;
            sdramMemory[byteAddress] = byteData;
            sdramMemory[TCNT1H_ADDR] = timer1_TEMP;
        } else if (byteAddress == OCR1AL_ADDR) {
            sdramMemory[byteAddress] = byteData;
            sdramMemory[OCR1AH_ADDR] = timer1_TEMP;
            flagOCR1AReady = true;
        } else if (byteAddress == OCR1BL_ADDR) {
            sdramMemory[byteAddress] = byteData;
            sdramMemory[OCR1BH_ADDR] = timer1_TEMP;
        } else if (byteAddress == ICR1H_ADDR) {
//            if (Timer1_ATmega328P.enableICRWrite) {
                timer1_TEMP = byteData;
//            }
        } else if (byteAddress == ICR1L_ADDR) {
//            if (Timer1_ATmega328P.enableICRWrite) {
                sdramMemory[byteAddress] = byteData;
                sdramMemory[ICR1H_ADDR] = timer1_TEMP;
//            }
        } else if (byteAddress == ADCL_ADDR || byteAddress == ADCH_ADDR) {
            if (adcWriteEnable) {
                sdramMemory[byteAddress] = byteData;
            }
        } else if (byteAddress == UDR0_ADDR) {
            if (readBit(UCSR0A_ADDR, 5)) {
                USART_ATmega328P.transmitter_UDR0 = byteData;
                writeBit(UCSR0A_ADDR, 5, false);
            }
        } else {
            sdramMemory[byteAddress] = byteData;
            new Notify(UCModule_View.simulatedTime).execute(byteAddress);
        }

        updateMemoryUsage(byteAddress);

        if (updateMemMapFlag && !MemoryAdapter.isFiltering) {
            updateMemMapFlag = false;
            UCModule_View.screenUpdater.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        MemoryFragment.mAdapter.notifyDataSetChanged();
                    } catch (IllegalStateException | NullPointerException e) {
                        Log.e(UCModule.MY_LOG_TAG, "ERROR: update memory map", e);
                    }
                }
            });
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
                || byteAddress == TIFR1_ADDR
                || byteAddress == TIFR2_ADDR) {
            //Clear Flag
            sdramMemory[byteAddress] = (byte) (sdramMemory[byteAddress] & (0xFF7F >> (7 - bitPosition)));

        } else if (byteAddress == ADCSRA_ADDR && bitPosition == 4) {
            //Clear Flag
            sdramMemory[byteAddress] = (byte) (sdramMemory[byteAddress] & (0xFF7F >> (7 - bitPosition)));

        } else if (byteAddress == ADCSRA_ADDR && bitPosition == 6 && !bitState) {
            //Write 0 to ADSC has no effect
            return;

        } else if (byteAddress == UCSR0A_ADDR && bitPosition == 6) {
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

        } else if (byteAddress == UDR0_ADDR) {
            if (readBit(UCSR0A_ADDR, 5)) {
                USART_ATmega328P.transmitter_UDR0 = (byte) (USART_ATmega328P.transmitter_UDR0 & (0xFF7F >> (7 - bitPosition)));
                if (bitState) {
                    USART_ATmega328P.transmitter_UDR0 = (byte) (USART_ATmega328P.transmitter_UDR0 | (0x01 << bitPosition));     //Set
                }
                writeBit(UCSR0A_ADDR, 5, false);
            }
        } else {
            sdramMemory[byteAddress] = (byte) (sdramMemory[byteAddress] & (0xFF7F >> (7 - bitPosition)));   //Clear
            if (bitState) {
                sdramMemory[byteAddress] = (byte) (sdramMemory[byteAddress] | (0x01 << bitPosition));     //Set
            }
            new Notify(UCModule_View.simulatedTime).execute(byteAddress);
        }

        if (updateMemMapFlag && !MemoryAdapter.isFiltering) {
            updateMemMapFlag = false;
            UCModule_View.screenUpdater.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        MemoryFragment.mAdapter.notifyDataSetChanged();
                    } catch (IllegalStateException | NullPointerException e) {
                        Log.e(UCModule.MY_LOG_TAG, "ERROR: update memory map", e);
                    }
                }
            });
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
        new NotifyIO().execute(byteAddress);

        if (updateMemMapFlag && !MemoryAdapter.isFiltering) {
            updateMemMapFlag = false;
            UCModule_View.screenUpdater.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        MemoryFragment.mAdapter.notifyDataSetChanged();
                    } catch (IllegalStateException | NullPointerException e) {
                        Log.e(UCModule.MY_LOG_TAG, "ERROR: update memory map", e);
                    }
                }
            });
        }
    }

    @Override
    public synchronized boolean readBit(int byteAddress, int bitPosition) {
//        Log.d(UCModule.MY_LOG_TAG,
//                String.format("Read bit SDRAM\nAddress: 0x%s", Integer.toHexString((int) byteAddress))
//                        + " position: " + bitPosition + " state: " + ((0x01 & (sdramMemory[byteAddress] >> bitPosition)) != 0));

        if (byteAddress == TCCR0B_ADDR && (bitPosition == 7 || bitPosition == 6)) {
            return false;   //Force math always read as 0;
        }
        if (byteAddress == UDR0_ADDR) {
            UCModule.interruptionModule.receiveBufferReadedUSART();
            return (0x01 & (USART_ATmega328P.receiver_UDR0 >> bitPosition)) != 0;
        }
        return (0x01 & (sdramMemory[byteAddress] >> bitPosition)) != 0;

    }

    public synchronized void write16bits(int byteAddressLow, int byteAddressHigh, byte byteLow, byte byteHigh) {

        //Write TCNT1L and TCNT1H
        if (byteAddressLow == 0x84 && !timer1WriteEnable) {
            return;
        }
        sdramMemory[byteAddressLow] = byteLow;
        sdramMemory[byteAddressHigh] = byteHigh;

        if (updateMemMapFlag && !MemoryAdapter.isFiltering) {
            updateMemMapFlag = false;
            UCModule_View.screenUpdater.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        MemoryFragment.mAdapter.notifyDataSetChanged();
                    } catch (IllegalStateException | NullPointerException e) {
                        Log.e(UCModule.MY_LOG_TAG, "ERROR: update memory map", e);
                    }
                }
            });
        }
    }

    public synchronized char read16bits(int byteAddressLow, int byteAddressHigh) {
        char data = (char) (0x00FF & sdramMemory[byteAddressLow]);
        data = (char) ((sdramMemory[byteAddressHigh] << 8) | data);

        //Read TCNT1L and TCNT1H
        if (byteAddressLow == 0x84) {
            timer1WriteEnable = true;
        }

        return data;
    }

    public void setPinHandler(Handler pinHandler) {
        this.pinHandler = pinHandler;
    }

    public synchronized void writeFeedback(int byteAddress, int bitPosition, boolean bitState) {

        sdramMemory[byteAddress] = (byte) (sdramMemory[byteAddress] & (0xFF7F >> (7 - bitPosition)));   //Clear
        if (bitState) {
            sdramMemory[byteAddress] = (byte) (sdramMemory[byteAddress] | (0x01 << bitPosition));     //Set
        }

        if (updateMemMapFlag && !MemoryAdapter.isFiltering) {
            updateMemMapFlag = false;
            UCModule_View.screenUpdater.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        MemoryFragment.mAdapter.notifyDataSetChanged();
                    } catch (IllegalStateException | NullPointerException e) {
                        Log.e(UCModule.MY_LOG_TAG, "ERROR: update memory map", e);
                    }
                }
            });
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

    public boolean isOCR1AReady() {
        return flagOCR1AReady;
    }

    public boolean readForceMatchA_timer2() {
        return (0x01 & (sdramMemory[TCCR2B_ADDR] >> 7)) != 0;
    }

    public boolean readForceMatchB_timer2() {
        return (0x01 & (sdramMemory[TCCR2B_ADDR] >> 6)) != 0;
    }

    private class Notify extends AsyncTask<Integer, Void, Void> {

        private long simulatedTime;

        public Notify() {
        }

        public Notify(long simulatedTime) {
            this.simulatedTime = simulatedTime;
        }

        @Override
        protected Void doInBackground(Integer... byteAddress) {
//            Log.i(UCModule.MY_LOG_TAG, String.format("Notify Address: 0x%s",
//                    Integer.toHexString((int) byteAddress[0])));
            Message ioMessage;

            switch (byteAddress[0]) {
                case DDRB_ADDR:
                case PORTB_ADDR:

                    ioMessage = new Message();

                    ioBundle.putByte(IOModule.CONFIG_IOMESSAGE, readIOByte(DDRB_ADDR));
                    ioBundle.putByte(IOModule.VALUE_IOMESSAGE, readIOByte(PORTB_ADDR));
                    ioBundle.putLong(IOModule.TIME, simulatedTime);

                    ioMessage.what = IOModule.PORTB_EVENT;

                    ioMessage.setData(ioBundle);

                    pinHandler.dispatchMessage(ioMessage);

                    break;

                case DDRC_ADDR:
                case PORTC_ADDR:


                    ioMessage = new Message();

                    ioBundle.putByte(IOModule.CONFIG_IOMESSAGE, readIOByte(DDRC_ADDR));
                    ioBundle.putByte(IOModule.VALUE_IOMESSAGE, readIOByte(PORTC_ADDR));
                    ioBundle.putLong(IOModule.TIME, simulatedTime);

                    ioMessage.what = IOModule.PORTC_EVENT;
                    ioMessage.setData(ioBundle);

                    pinHandler.dispatchMessage(ioMessage);

                    break;


                case DDRD_ADDR:
                case PORTD_ADDR:

                    ioMessage = new Message();

                    ioBundle.putByte(IOModule.CONFIG_IOMESSAGE, readIOByte(DDRD_ADDR));
                    ioBundle.putByte(IOModule.VALUE_IOMESSAGE, readIOByte(PORTD_ADDR));
                    ioBundle.putLong(IOModule.TIME, simulatedTime);

                    ioMessage.what = IOModule.PORTD_EVENT;
                    ioMessage.setData(ioBundle);

                    pinHandler.dispatchMessage(ioMessage);

                    break;

                default:
                    //It shouldn't get here.
                    break;
            }

            return null;
        }
    }

    private class NotifyIO extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... byteAddress) {
            //        Log.i(UCModule.MY_LOG_TAG, String.format("Nority IO Address: 0x%s",
//                Integer.toHexString((int) byteAddress)));

            Message ioMessage;

            switch (byteAddress[0]) {
                case DDRB_ADDR:
//                case PORTB_ADDR:
                case PINB_ADDR:

                    ioMessage = new Message();

                    ioBundle.putByte(IOModule.CONFIG_IOMESSAGE, readIOByte(DDRB_ADDR));
                    ioBundle.putByte(IOModule.VALUE_IOMESSAGE, readIOByte(PINB_ADDR));

                    ioMessage.what = IOModule.PORTB_EVENT;
                    ioMessage.setData(ioBundle);

                    pinHandler.dispatchMessage(ioMessage);
                    break;

                case DDRC_ADDR:
//                case PORTC_ADDR:
                case PINC_ADDR:

                    ioMessage = new Message();

                    ioBundle.putByte(IOModule.CONFIG_IOMESSAGE, readIOByte(DDRC_ADDR));
                    ioBundle.putByte(IOModule.VALUE_IOMESSAGE, readIOByte(PINC_ADDR));

//                    Log.d("Analog", "Analog Notify: " + Integer.toHexString(readIOByte(PINC_ADDR)));

                    ioMessage.what = IOModule.PORTC_EVENT;
                    ioMessage.setData(ioBundle);

                    pinHandler.dispatchMessage(ioMessage);
                    break;

                case DDRD_ADDR:
//                case PORTD_ADDR:
                case PIND_ADDR:

                    ioMessage = new Message();

                    ioBundle.putByte(IOModule.CONFIG_IOMESSAGE, readIOByte(DDRD_ADDR));
                    ioBundle.putByte(IOModule.VALUE_IOMESSAGE, readIOByte(PIND_ADDR));

                    ioMessage.what = IOModule.PORTD_EVENT;
                    ioMessage.setData(ioBundle);

                    pinHandler.dispatchMessage(ioMessage);
                    break;

                default:
                    //It shouldn't get here.
                    break;
            }
            return null;
        }
    }

    private class TimerMemoryMap extends TimerTask{

        @Override
        public void run() {
            updateMemMapFlag = true;
            MemoryFragment.updateMemoryUsage(getMemoryUsage(), getMemorySize());
        }
    }
}
