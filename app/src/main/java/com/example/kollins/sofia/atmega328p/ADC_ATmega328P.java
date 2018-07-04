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

import android.os.Handler;
import android.util.Log;

import com.example.kollins.sofia.UCModule;
import com.example.kollins.sofia.ucinterfaces.ADCModule;
import com.example.kollins.sofia.ucinterfaces.DataMemory;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ADC_ATmega328P implements ADCModule {

    //mV
    private static final short BANDGAP_REFERENCE = 1100;

    //Default AREF (mV)
    public static short AREF = 5000;

    //Input holds value in mV.
    public static short[] adcInput = new short[16];
    public static final short BANDGAP_INDEX = 14;

//    private static Handler uCHandler;

//    private UCModule uCModule;
    private static DataMemory_ATmega328P dataMemory;
    private int inputIndex;
    private boolean freeRunConversionEnable;

    private byte admuxRead, adcsraRead;
    private int vRef, prescaler;
    private int conversionADC;
    private double resolution, conversionAux;
    private boolean isFreeRun;

    public ADC_ATmega328P(DataMemory dataMemory) {
        this.dataMemory = (DataMemory_ATmega328P) dataMemory;
//        this.uCHandler = uCHandler;
//        this.uCModule = uCModule;

        adcInput[BANDGAP_INDEX] = BANDGAP_REFERENCE;

        freeRunConversionEnable = true;
    }

    public void run() {

        //Power Reduction Register
        if (dataMemory.readBit(DataMemory_ATmega328P.PRR_ADDR, 0)) {
            return;
        }

        /*
        This module is faster than the real uC.
        It takes only one clock cycle to complete a conversion
         */

        if (dataMemory.readBit(DataMemory_ATmega328P.ADCSRA_ADDR, 7)
                && dataMemory.readBit(DataMemory_ATmega328P.ADCSRA_ADDR, 6)) {

            if (!dataMemory.readBit(DataMemory_ATmega328P.ADCSRA_ADDR, 5) ||
                    (0x07 & dataMemory.readByte(DataMemory_ATmega328P.ADCSRB_ADDR)) > 0) {
                isFreeRun = false;
                freeRunConversionEnable = false;
            } else {
                isFreeRun = true;
                if (!freeRunConversionEnable) {
                    if (dataMemory.readBit(DataMemory_ATmega328P.ADCSRA_ADDR, 4)) {
                        return;
                    } else {
                        freeRunConversionEnable = true;
                    }
                }
            }
            admuxRead = dataMemory.readByte(DataMemory_ATmega328P.ADMUX_ADDR);
            adcsraRead = dataMemory.readByte(DataMemory_ATmega328P.ADCSRA_ADDR);

            prescaler = (0x0007 & adcsraRead);

            inputIndex = (0x000F & admuxRead);

            switch (0x00C0 & admuxRead) {
                case 0x0000:
                    vRef = AREF;
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
                 conversionIncrease = conversionIncrease >> 1) {

                conversionAux += conversionIncrease * resolution;
                if (conversionAux > adcInput[inputIndex]) {
                    conversionAux -= conversionIncrease * resolution;
                } else {
                    conversionADC |= conversionIncrease;
                }
            }

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

        }

    }

//    public enum ClockSource {
//        CLOCK_PRESCALER_2_1 {
//            @Override
//            public void work() {
//                Log.v("ADC", "Clock Prescaler 2");
////                waitClock();
////                for (int i = 0; i < 2; i++) {
////                    waitClock();
////                }
//            }
//        },
//        CLOCK_PRESCALER_2_2 {
//            @Override
//            public void work() {
//                Log.v("ADC", "Clock Prescaler 2");
////                waitClock();
////                for (int i = 0; i < 2; i++) {
////                    waitClock();
////                }
//            }
//        },
//        CLOCK_PRESCALER_4 {
//            @Override
//            public void work() {
//                Log.v("ADC", "Clock Prescaler 4");
////                waitClock();
////                for (int i = 0; i < 4; i++) {
////                    waitClock();
////                }
//            }
//        },
//        CLOCK_PRESCALER_8 {
//            @Override
//            public void work() {
//                Log.v("ADC", "Clock Prescaler 8");
////                waitClock();
////                for (int i = 0; i < 8; i++) {
////                    waitClock();
////                }
//            }
//        },
//        CLOCK_PRESCALER_16 {
//            @Override
//            public void work() {
//                Log.v("ADC", "Clock Prescaler 16");
////                waitClock();
////                for (int i = 0; i < 16; i++) {
////                    waitClock();
////                }
//            }
//        },
//        CLOCK_PRESCALER_32 {
//            @Override
//            public void work() {
//                Log.v("ADC", "Clock Prescaler 32");
////                waitClock();
////                for (int i = 0; i < 32; i++) {
////                    waitClock();
////                }
//            }
//        },
//        CLOCK_PRESCALER_64 {
//            @Override
//            public void work() {
//                Log.v("ADC", "Clock Prescaler 64");
////                waitClock();
////                for (int i = 0; i < 64; i++) {
////                    waitClock();
////                }
//            }
//        },
//        CLOCK_PRESCALER_128 {
//            @Override
//            public void work() {
//                Log.v("ADC", "Clock Prescaler 128");
////                waitClock();
////                for (int i = 0; i < 128; i++) {
////                    waitClock();
////                }
//            }
//        };
//
//        public abstract void work();
//    }
}
