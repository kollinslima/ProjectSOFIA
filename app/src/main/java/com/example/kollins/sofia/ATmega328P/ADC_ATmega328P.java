package com.example.kollins.sofia.ATmega328P;

import android.os.Handler;
import android.util.Log;

import com.example.kollins.sofia.UCModule;
import com.example.kollins.sofia.uCInterfaces.ADCModule;
import com.example.kollins.sofia.uCInterfaces.DataMemory;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class ADC_ATmega328P implements ADCModule {

    //mV
    private static final short BANDGAP_REFERENCE = 1100;

    //Default AREF
    public static short AREF = 0;

    //Input holds value in mV.
    public static short[] adcInput = new short[11];
    private static final short BANDGAP_INDEX = 9;

    private static Handler uCHandler;
    private static Lock clockLock;
    private static Condition adcClockCondition;
    private static DataMemory_ATmega328P dataMemory;
    private UCModule uCModule;
    private int inputIndex;
    private boolean freeRunConversionEnable;


    public ADC_ATmega328P(DataMemory dataMemory, Handler uCHandler, Lock clockLock, UCModule uCModule) {
        this.dataMemory = (DataMemory_ATmega328P) dataMemory;
        this.uCHandler = uCHandler;
        this.clockLock = clockLock;
        this.uCModule = uCModule;

        adcClockCondition = clockLock.newCondition();

        adcInput[BANDGAP_INDEX] = BANDGAP_REFERENCE;

        freeRunConversionEnable = true;
    }

    @Override
    public void run() {
        Thread.currentThread().setName("ADC");
        byte admuxRead, adcsraRead;
        int vRef, prescaler;
        int conversionADC;
        double resolution, conversionAux;
        boolean isFreeRun;

        //13 clock cycles to finish conversion
        while (!uCModule.getResetFlag()) {
            if (dataMemory.readBit(DataMemory_ATmega328P.ADCSRA_ADDR, 7)
                    && dataMemory.readBit(DataMemory_ATmega328P.ADCSRA_ADDR, 6)) {

                if (!dataMemory.readBit(DataMemory_ATmega328P.ADCSRA_ADDR, 5) ||
                        (0x07 & dataMemory.readByte(DataMemory_ATmega328P.ADCSRB_ADDR)) > 0) {
                    isFreeRun = false;
                    freeRunConversionEnable = false;
                } else {
                    isFreeRun = true;
                    if (!freeRunConversionEnable) {
                        if (dataMemory.readBit(DataMemory_ATmega328P.ADCSRA_ADDR,4)) {
                            waitClock();
                            continue;
                        } else {
                            freeRunConversionEnable = true;
                        }
                    }
                }
                admuxRead = dataMemory.readByte(DataMemory_ATmega328P.ADMUX_ADDR);
                adcsraRead = dataMemory.readByte(DataMemory_ATmega328P.ADCSRA_ADDR);

                prescaler = (0x0007 & adcsraRead);

                ClockSource.values()[prescaler].work();
                inputIndex = (0x000F & admuxRead);

                ClockSource.values()[prescaler].work();
                switch (0x00C0 & admuxRead) {
                    case 0x0000:
                        vRef = AREF * 1000;
                        break;
                    case 0x0040:
                        vRef = UCModule.getSourcePower() * 1000;
                        break;
                    case 0x00C0:
                        vRef = BANDGAP_REFERENCE;
                        break;
                    default:
                        vRef = 0;
                        break;
                }

                resolution = vRef / 1024f;

                conversionAux = 0;
                conversionADC = 0;
                for (int conversionIncrease = 0x0200;
                     conversionIncrease > 0;
                     conversionIncrease = conversionIncrease >> 1, ClockSource.values()[prescaler].work()) {

                    conversionAux += conversionIncrease * resolution;
                    if (conversionAux > adcInput[inputIndex]) {
                        conversionAux -= conversionIncrease * resolution;
                    } else {
                        conversionADC |= conversionIncrease;
                    }
                }
                ClockSource.values()[prescaler].work();

                if (dataMemory.readBit(DataMemory_ATmega328P.ADMUX_ADDR, 5)) {
                    //Left Ajust
                    dataMemory.writeByte(DataMemory_ATmega328P.ADCH_ADDR, (byte) (0x00FF & (conversionADC >> 2)));
                    dataMemory.writeByte(DataMemory_ATmega328P.ADCL_ADDR, (byte) (0x00FF & (conversionADC << 6)));
                } else {
                    //Right Ajust
                    dataMemory.writeByte(DataMemory_ATmega328P.ADCH_ADDR, (byte) (0x00FF & (conversionADC >> 8)));
                    dataMemory.writeByte(DataMemory_ATmega328P.ADCL_ADDR, (byte) (0x00FF & conversionADC));
                }


                if (!isFreeRun) {
                    //Not Free Run mode, end of conversion, clear ADSC
                    dataMemory.writeIOBit(DataMemory_ATmega328P.ADCSRA_ADDR, 6, false);
                } else {
                    freeRunConversionEnable = false;
                }

                //Set Interruption Flag
                UCModule.interruptionModule.conversionCompleteADC();

            } else {
                waitClock();
            }
        }

        Log.i(UCModule.MY_LOG_TAG, "Finishing ADC");

    }

    private static void waitClock() {

        clockLock.lock();
        try {
            UCModule.clockVector[UCModule.ADC_ID] = true;

            //Check if this is the last module in this clock cycle.
            for (int i = 0; i < UCModule.clockVector.length; i++) {
                if (!UCModule.clockVector[i]) {

                    while(UCModule.clockVector[UCModule.ADC_ID]) {
                        adcClockCondition.await();
                    }

                    return;
                }
            }

            UCModule.resetClockVector();

            //Send Broadcast
            uCHandler.sendEmptyMessage(UCModule.CLOCK_ACTION);

        } catch (InterruptedException e) {
            Log.e(UCModule.MY_LOG_TAG, "ERROR: waitClock ADC", e);
        } finally {
            clockLock.unlock();
        }
    }

    @Override
    public void clockADC() {
        clockLock.lock();
        try {
            adcClockCondition.signal();
        } finally {
            clockLock.unlock();
        }
    }

    public enum ClockSource {
        CLOCK_PRESCALER_2_1 {
            @Override
            public void work() {
                Log.v("ADC", "Clock Prescaler 2");
                waitClock();
//                for (int i = 0; i < 2; i++) {
//                    waitClock();
//                }
            }
        },
        CLOCK_PRESCALER_2_2 {
            @Override
            public void work() {
                Log.v("ADC", "Clock Prescaler 2");
                waitClock();
//                for (int i = 0; i < 2; i++) {
//                    waitClock();
//                }
            }
        },
        CLOCK_PRESCALER_4 {
            @Override
            public void work() {
                Log.v("ADC", "Clock Prescaler 4");
                waitClock();
//                for (int i = 0; i < 4; i++) {
//                    waitClock();
//                }
            }
        },
        CLOCK_PRESCALER_8 {
            @Override
            public void work() {
                Log.v("ADC", "Clock Prescaler 8");
                waitClock();
//                for (int i = 0; i < 8; i++) {
//                    waitClock();
//                }
            }
        },
        CLOCK_PRESCALER_16 {
            @Override
            public void work() {
                Log.v("ADC", "Clock Prescaler 16");
                waitClock();
//                for (int i = 0; i < 16; i++) {
//                    waitClock();
//                }
            }
        },
        CLOCK_PRESCALER_32 {
            @Override
            public void work() {
                Log.v("ADC", "Clock Prescaler 32");
                waitClock();
//                for (int i = 0; i < 32; i++) {
//                    waitClock();
//                }
            }
        },
        CLOCK_PRESCALER_64 {
            @Override
            public void work() {
                Log.v("ADC", "Clock Prescaler 64");
                waitClock();
//                for (int i = 0; i < 64; i++) {
//                    waitClock();
//                }
            }
        },
        CLOCK_PRESCALER_128 {
            @Override
            public void work() {
                Log.v("ADC", "Clock Prescaler 128");
                waitClock();
//                for (int i = 0; i < 128; i++) {
//                    waitClock();
//                }
            }
        };

        public abstract void work();
    }
}
