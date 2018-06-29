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

import com.example.kollins.sofia.UCModule_View;
import com.example.kollins.sofia.atmega328p.iomodule_atmega328p.IOModule_ATmega328P;
import com.example.kollins.sofia.UCModule;
import com.example.kollins.sofia.ucinterfaces.DataMemory;
import com.example.kollins.sofia.ucinterfaces.IOModule;
import com.example.kollins.sofia.ucinterfaces.Timer2Module;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Timer2_ATmega328P implements Timer2Module {

    private static final String TIMER2_TAG = "Timer2";

    public static final byte MAX = -1; //0xFF signed
    public static final byte BOTTOM = 0x00;

    public static boolean timerOutputControl_OC2A;
    public static boolean timerOutputControl_OC2B;

    private static DataMemory_ATmega328P dataMemory;
//    private static Handler uCHandler;
    private static IOModule_ATmega328P ioModule;
//    private UCModule uCModule;

    private boolean buffer_WGM22;

    private static int stateOC2A, stateOC2B;
    private static boolean nextOverflow, nextClear;
    private static boolean phaseCorrect_UPCount,        //Tell about the next count
                           phaseCorrect_UPCount_old;    //Tell about how I get to the actual value
    private static byte doubleBufferOCR2A, doubleBufferOCR2B;

    private static short clockCount;

    public Timer2_ATmega328P(DataMemory dataMemory, IOModule ioModule) {
        this.dataMemory = (DataMemory_ATmega328P) dataMemory;
//        this.uCHandler = uCHandler;
//        this.uCModule = uCModule;
        this.ioModule = (IOModule_ATmega328P) ioModule;

        timerOutputControl_OC2A = false;
        timerOutputControl_OC2B = false;
        phaseCorrect_UPCount = true;
        phaseCorrect_UPCount_old = false;

        stateOC2A = IOModule.TRI_STATE;
        stateOC2B = IOModule.TRI_STATE;

        nextOverflow = false;
        nextClear = false;

        doubleBufferOCR2A = 0;
        doubleBufferOCR2B = 0;

        clockCount = 0;
    }

    @Override
    public void run() {
            if (ClockSource.values()[0x07 & dataMemory.readByte(DataMemory_ATmega328P.TCCR2B_ADDR)].work()) {
                if (dataMemory.readBit(DataMemory_ATmega328P.GTCCR_ADDR, 1)) {
                    return;   //Synchronization Mode
                }

                buffer_WGM22 = dataMemory.readBit(DataMemory_ATmega328P.TCCR2B_ADDR, 3);

                switch (0x03 & dataMemory.readByte(DataMemory_ATmega328P.TCCR2A_ADDR)) {
                    case 0x00:
                        if (!buffer_WGM22) {
                            TimerMode.NORMAL_OPERATION.count();
                        }
                        break;
                    case 0x01:
                        if (buffer_WGM22) {
                            TimerMode.PWM_PHASE_CORRECT_TOP_OCRA.count();
                        } else {
                            TimerMode.PWM_PHASE_CORRECT_TOP_0XFF.count();
                        }
                        break;
                    case 0x02:
                        if (!buffer_WGM22) {
                            TimerMode.CTC_OPERATION.count();
                        }
                        break;
                    case 0x03:
                        if (buffer_WGM22) {
                            TimerMode.FAST_PWM_TOP_OCRA.count();
                        } else {
                            TimerMode.FAST_PWM_TOP_0XFF.count();
                        }
                        break;
                }

            }
    }

    public enum ClockSource {
        NO_CLOCK_SOURCE {
            @Override
            public boolean work() {
                Log.i(TIMER2_TAG, "No Clock Source");
                return false;
            }
        },
        CLOCK_PRESCALER_1 {
            @Override
            public boolean work() {
                Log.i(TIMER2_TAG, "Prescaler 1");
                return true;
            }
        },
        CLOCK_PRESCALER_8 {
            @Override
            public boolean work() {
                Log.i(TIMER2_TAG, "Prescaler 8");
                if (++clockCount < 8){
                    return false;
                } else {
                    clockCount = 0;
                    return true;
                }
            }
        },
        CLOCK_PRESCALER_32 {
            @Override
            public boolean work() {
                Log.i(TIMER2_TAG, "Prescaler 32");
                if (++clockCount < 32){
                    return false;
                } else {
                    clockCount = 0;
                    return true;
                }
            }
        },
        CLOCK_PRESCALER_64 {
            @Override
            public boolean work() {
                Log.i(TIMER2_TAG, "Prescaler 64");
                if (++clockCount < 64){
                    return false;
                } else {
                    clockCount = 0;
                    return true;
                }
            }
        },
        CLOCK_PRESCALER_128 {
            @Override
            public boolean work() {
                Log.i(TIMER2_TAG, "Prescaler 128");
                if (++clockCount < 128){
                    return false;
                } else {
                    clockCount = 0;
                    return true;
                }
            }
        },
        CLOCK_PRESCALER_256 {
            @Override
            public boolean work() {
                Log.i(TIMER2_TAG, "Prescaler 256");
                if (++clockCount < 256){
                    return false;
                } else {
                    clockCount = 0;
                    return true;
                }
            }
        },
        CLOCK_PRESCALER_1024 {
            @Override
            public boolean work() {
                Log.i(TIMER2_TAG, "Prescaler 1024");
                if (++clockCount < 1024){
                    return false;
                } else {
                    clockCount = 0;
                    return true;
                }
            }
        };

        public abstract boolean work();
    }

    public enum TimerMode {
        NORMAL_OPERATION {
            @Override
            public void count() {
                boolean match_A = false, match_B = false;
                byte progress = dataMemory.readByte(DataMemory_ATmega328P.TCNT2_ADDR);
                progress = (byte) (progress + 1);

                if (progress == BOTTOM) {
                    UCModule.interruptionModule.timer2Overflow();
                }
                if (dataMemory.readForceMatchA_timer2()) {
                    match_A = true; //FORCE MATCH
                } else if (progress == dataMemory.readByte(DataMemory_ATmega328P.OCR2A_ADDR)) {
                    UCModule.interruptionModule.timer2MatchA();
                    match_A = true;
                }
                if (dataMemory.readForceMatchB_timer2()) {
                    match_B = true; //FORCE MATCH
                } else if (progress == dataMemory.readByte(DataMemory_ATmega328P.OCR2B_ADDR)) {
                    UCModule.interruptionModule.timer2MatchB();
                    match_B = true;
                }


                byte outputMode = dataMemory.readByte(DataMemory_ATmega328P.TCCR2A_ADDR);

                //CHANEL A
                switch (0xC0 & outputMode) {
                    case 0x00:
                        //OC2A disconected
                        timerOutputControl_OC2A = false;
                        break;
                    case 0x40:
                        //OC2A Toggle on Compare Match
                        timerOutputControl_OC2A = true;
                        if (match_A) {
                            stateOC2A = (stateOC2A + 1) % 2;
                            ioModule.setOC2A(stateOC2A, UCModule_View.simulatedTime);
                        }
                        break;
                    case 0x80:
                        //OC2A Clear on Compare Match
                        timerOutputControl_OC2A = true;
                        if (match_A) {
                            stateOC2A = IOModule.LOW_LEVEL;
                            ioModule.setOC2A(stateOC2A, UCModule_View.simulatedTime);
                        }
                        break;
                    case 0xC0:
                        //OC2A Set on Compare Match
                        timerOutputControl_OC2A = true;
                        if (match_A) {
                            stateOC2A = IOModule.HIGH_LEVEL;
                            ioModule.setOC2A(stateOC2A, UCModule_View.simulatedTime);
                        }
                }

                //CHANEL B
                switch (0x30 & outputMode) {
                    case 0x00:
                        //OC2B disconected
                        timerOutputControl_OC2B = false;
                        break;
                    case 0x10:
                        //OC2B Toggle on Compare Match
                        timerOutputControl_OC2B = true;
                        if (match_B) {
                            stateOC2B = (stateOC2B + 1) % 2;
                            ioModule.setOC2B(stateOC2B, UCModule_View.simulatedTime);
                        }
                        break;
                    case 0x20:
                        //OC2B Clear on Compare Match
                        timerOutputControl_OC2B = true;
                        if (match_B) {
                            stateOC2B = IOModule.LOW_LEVEL;
                            ioModule.setOC2B(stateOC2B, UCModule_View.simulatedTime);
                        }
                        break;
                    case 0x30:
                        //OC2B Set on Compare Match
                        timerOutputControl_OC2B = true;
                        if (match_B) {
                            stateOC2B = IOModule.HIGH_LEVEL;
                            ioModule.setOC2B(stateOC2B, UCModule_View.simulatedTime);
                        }
                }

                dataMemory.writeByte(DataMemory_ATmega328P.TCNT2_ADDR, progress);
            }
        },
        PWM_PHASE_CORRECT_TOP_0XFF {
            @Override
            public void count() {
                boolean match_A = false, match_B = false;
                byte progress = dataMemory.readByte(DataMemory_ATmega328P.TCNT2_ADDR);
                if (progress == MAX) {
                    doubleBufferOCR2A = dataMemory.readByte(DataMemory_ATmega328P.OCR2A_ADDR);
                    doubleBufferOCR2B = dataMemory.readByte(DataMemory_ATmega328P.OCR2B_ADDR);
                }

                if (phaseCorrect_UPCount) {
                    progress = (byte) (progress + 1);
                } else {
                    progress = (byte) (progress - 1);
                }

                phaseCorrect_UPCount_old = phaseCorrect_UPCount;

                if (progress == BOTTOM) {
                    UCModule.interruptionModule.timer2Overflow();
                    phaseCorrect_UPCount = true;
                } else if (progress == MAX) {
                    phaseCorrect_UPCount = false;
                }

                if (progress == doubleBufferOCR2A) {
                    UCModule.interruptionModule.timer2MatchA();
                    match_A = true;
                }
                if (progress == doubleBufferOCR2B) {
                    UCModule.interruptionModule.timer2MatchB();
                    match_B = true;
                }

                byte outputMode = dataMemory.readByte(DataMemory_ATmega328P.TCCR2A_ADDR);

                //CHANEL A
                switch (0xC0 & outputMode) {
                    case 0x00:
                    case 0x40:
                        //OC2A disconected
                        timerOutputControl_OC2A = false;
                        break;
                    case 0x80:
                        //OC2A Clear on Compare Match counting up, OC2A Set on Compare Match counting down
                        timerOutputControl_OC2A = true;
                        if (doubleBufferOCR2A == MAX) {
                            stateOC2A = IOModule.HIGH_LEVEL;
                            ioModule.setOC2A(stateOC2A, UCModule_View.simulatedTime);
                        } else if (doubleBufferOCR2A == BOTTOM) {
                            stateOC2A = IOModule.LOW_LEVEL;
                            ioModule.setOC2A(stateOC2A, UCModule_View.simulatedTime);
                        } else {
                            if (match_A) {
                                if (phaseCorrect_UPCount_old) {
                                    stateOC2A = IOModule.LOW_LEVEL;
                                } else {
                                    stateOC2A = IOModule.HIGH_LEVEL;
                                }
                                ioModule.setOC2A(stateOC2A, UCModule_View.simulatedTime);
                            }
                        }
                        break;
                    case 0xC0:
                        //OC2A Set on Compare Match counting up, OC2A Clear on Compare Match counting down
                        timerOutputControl_OC2A = true;
                        if (doubleBufferOCR2A == MAX) {
                            stateOC2A = IOModule.LOW_LEVEL;
                            ioModule.setOC2A(stateOC2A, UCModule_View.simulatedTime);
                        } else if (doubleBufferOCR2A == BOTTOM) {
                            stateOC2A = IOModule.HIGH_LEVEL;
                            ioModule.setOC2A(stateOC2A, UCModule_View.simulatedTime);
                        } else {
                            if (match_A) {
                                if (phaseCorrect_UPCount_old) {
                                    stateOC2A = IOModule.HIGH_LEVEL;
                                } else {
                                    stateOC2A = IOModule.LOW_LEVEL;
                                }
                                ioModule.setOC2A(stateOC2A, UCModule_View.simulatedTime);
                            }
                        }
                }

                //CHANEL B
                switch (0x30 & outputMode) {
                    case 0x00:
                    case 0x10:
                        //OC2B disconected
                        timerOutputControl_OC2B = false;
                        break;
                    case 0x20:
                        //OC2B Clear on Compare Match counting up, OC2B Set on Compare Match counting down
                        timerOutputControl_OC2B = true;
                        if (match_B) {
                            if (phaseCorrect_UPCount_old) {
                                stateOC2B = IOModule.LOW_LEVEL;
                            } else {
                                stateOC2B = IOModule.HIGH_LEVEL;
                            }
                            ioModule.setOC2B(stateOC2B, UCModule_View.simulatedTime);
                        }
                        break;
                    case 0x30:
                        //OC2B Set on Compare Match counting up, OC2B Clear on Compare Match counting down
                        timerOutputControl_OC2B = true;
                        if (match_B) {
                            if (phaseCorrect_UPCount_old) {
                                stateOC2B = IOModule.HIGH_LEVEL;
                            } else {
                                stateOC2B = IOModule.LOW_LEVEL;
                            }
                            ioModule.setOC2B(stateOC2B, UCModule_View.simulatedTime);
                        }
                }

                dataMemory.writeByte(DataMemory_ATmega328P.TCNT2_ADDR, progress);
            }
        },
        CTC_OPERATION {
            @Override
            public void count() {
                boolean match_A = false, match_B = false;
                byte progress = dataMemory.readByte(DataMemory_ATmega328P.TCNT2_ADDR);
                progress = (byte) (progress + 1);

                if (nextClear) {
                    nextClear = false;
                    progress = BOTTOM;
                }

                if (progress == BOTTOM && nextOverflow) {
                    nextOverflow = false;
                    UCModule.interruptionModule.timer2Overflow();
                } else if (progress == MAX) {
                    nextOverflow = true;
                }

                if (dataMemory.readForceMatchA_timer2()) {
                    match_A = true; //FORCE MATCH
                } else if (progress == dataMemory.readByte(DataMemory_ATmega328P.OCR2A_ADDR)) {
                    UCModule.interruptionModule.timer2MatchA();
                    match_A = true;
                    nextClear = true;
                }

                if (dataMemory.readForceMatchB_timer2()) {
                    match_B = true; //FORCE MATCH
                } else if (progress == dataMemory.readByte(DataMemory_ATmega328P.OCR2B_ADDR)) {
                    UCModule.interruptionModule.timer2MatchB();
                    match_B = true;
                }

                byte outputMode = dataMemory.readByte(DataMemory_ATmega328P.TCCR2A_ADDR);

                //CHANEL A
                switch (0xC0 & outputMode) {
                    case 0x00:
                        //OC2A disconected
                        timerOutputControl_OC2A = false;
                        break;
                    case 0x40:
                        //OC2A Toggle on Compare Match
                        timerOutputControl_OC2A = true;
                        if (match_A) {
                            stateOC2A = (stateOC2A + 1) % 2;
                            ioModule.setOC2A(stateOC2A, UCModule_View.simulatedTime);
                        }
                        break;
                    case 0x80:
                        //OC2A Clear on Compare Match
                        timerOutputControl_OC2A = true;
                        if (match_A) {
                            stateOC2A = IOModule.LOW_LEVEL;
                            ioModule.setOC2A(stateOC2A, UCModule_View.simulatedTime);
                        }
                        break;
                    case 0xC0:
                        //OC2A Set on Compare Match
                        timerOutputControl_OC2A = true;
                        if (match_A) {
                            stateOC2A = IOModule.HIGH_LEVEL;
                            ioModule.setOC2A(stateOC2A, UCModule_View.simulatedTime);
                        }
                }

                //CHANEL B
                switch (0x30 & outputMode) {
                    case 0x00:
                        //OC2B disconected
                        timerOutputControl_OC2B = false;
                        break;
                    case 0x10:
                        //OC2B Toggle on Compare Match
                        timerOutputControl_OC2B = true;
                        if (match_B) {
                            stateOC2B = (stateOC2B + 1) % 2;
                            ioModule.setOC2B(stateOC2B, UCModule_View.simulatedTime);
                        }
                        break;
                    case 0x20:
                        //OC2B Clear on Compare Match
                        timerOutputControl_OC2B = true;
                        if (match_B) {
                            stateOC2B = IOModule.LOW_LEVEL;
                            ioModule.setOC2B(stateOC2B, UCModule_View.simulatedTime);
                        }
                        break;
                    case 0x30:
                        //OC2B Set on Compare Match
                        timerOutputControl_OC2B = true;
                        if (match_B) {
                            stateOC2B = IOModule.HIGH_LEVEL;
                            ioModule.setOC2B(stateOC2B, UCModule_View.simulatedTime);
                        }
                }

                dataMemory.writeByte(DataMemory_ATmega328P.TCNT2_ADDR, progress);
            }
        },
        FAST_PWM_TOP_0XFF {
            @Override
            public void count() {
                boolean match_A = false, match_B = false;
                byte progress = dataMemory.readByte(DataMemory_ATmega328P.TCNT2_ADDR);
                if (progress == BOTTOM) {
                    doubleBufferOCR2A = dataMemory.readByte(DataMemory_ATmega328P.OCR2A_ADDR);
                    doubleBufferOCR2B = dataMemory.readByte(DataMemory_ATmega328P.OCR2B_ADDR);
                }

                progress = (byte) (progress + 1);

                if (progress == BOTTOM) {
                    UCModule.interruptionModule.timer2Overflow();
                }
                if (progress == doubleBufferOCR2A) {
                    UCModule.interruptionModule.timer2MatchA();
                    match_A = true;
                }
                if (progress == doubleBufferOCR2B) {
                    UCModule.interruptionModule.timer2MatchB();
                    match_B = true;
                }

                byte outputMode = dataMemory.readByte(DataMemory_ATmega328P.TCCR2A_ADDR);

                //CHANEL A
                switch (0xC0 & outputMode) {
                    case 0x00:
                    case 0x40:
                        //OC2A disconected
                        timerOutputControl_OC2A = false;
                        break;
                    case 0x80:
                        //OC2A Clear on Compare Match, set at BOTTOM
                        timerOutputControl_OC2A = true;
                        if (doubleBufferOCR2A == MAX) {
                            stateOC2A = IOModule.HIGH_LEVEL;
                            ioModule.setOC2A(stateOC2A, UCModule_View.simulatedTime);
                        } else {
                            if (match_A) {
                                stateOC2A = IOModule.LOW_LEVEL;
                                ioModule.setOC2A(stateOC2A, UCModule_View.simulatedTime);
                            }
                            if (progress == BOTTOM) {
                                stateOC2A = IOModule.HIGH_LEVEL;
                                ioModule.setOC2A(stateOC2A, UCModule_View.simulatedTime);
                            }
                        }
                        break;
                    case 0xC0:
                        //OC2A Set on Compare Match, clear at BOTTOM
                        timerOutputControl_OC2A = true;
                        if (doubleBufferOCR2A == MAX) {
                            stateOC2A = IOModule.LOW_LEVEL;
                            ioModule.setOC2A(stateOC2A, UCModule_View.simulatedTime);
                        } else {
                            if (match_A) {
                                stateOC2A = IOModule.HIGH_LEVEL;
                                ioModule.setOC2A(stateOC2A, UCModule_View.simulatedTime);
                            }

                            if (progress == BOTTOM) {
                                stateOC2A = IOModule.LOW_LEVEL;
                                ioModule.setOC2A(stateOC2A, UCModule_View.simulatedTime);
                            }
                        }

                }

                //CHANEL B
                switch (0x30 & outputMode) {
                    case 0x00:
                    case 0x10:
                        //OC2B disconected
                        timerOutputControl_OC2B = false;
                        break;
                    case 0x20:
                        //OC2B Clear on Compare Match, set at BOTTOM
                        timerOutputControl_OC2B = true;
                        if (match_B) {
                            stateOC2B = IOModule.LOW_LEVEL;
                            ioModule.setOC2B(stateOC2B, UCModule_View.simulatedTime);
                        }
                        if (progress == BOTTOM) {
                            stateOC2B = IOModule.HIGH_LEVEL;
                            ioModule.setOC2B(stateOC2B, UCModule_View.simulatedTime);
                        }
                        break;
                    case 0x30:
                        //OC2B Set on Compare Match
                        timerOutputControl_OC2B = true;
                        if (match_B) {
                            stateOC2B = IOModule.HIGH_LEVEL;
                            ioModule.setOC2B(stateOC2B, UCModule_View.simulatedTime);
                        }
                        if (progress == BOTTOM) {
                            stateOC2B = IOModule.LOW_LEVEL;
                            ioModule.setOC2B(stateOC2B, UCModule_View.simulatedTime);
                        }
                }

                dataMemory.writeByte(DataMemory_ATmega328P.TCNT2_ADDR, progress);
            }
        },
        PWM_PHASE_CORRECT_TOP_OCRA {
            @Override
            public void count() {
                boolean match_A = false, match_B = false;
                byte progress = dataMemory.readByte(DataMemory_ATmega328P.TCNT2_ADDR);
                if (progress == doubleBufferOCR2A) {
                    doubleBufferOCR2A = dataMemory.readByte(DataMemory_ATmega328P.OCR2A_ADDR);
                    doubleBufferOCR2B = dataMemory.readByte(DataMemory_ATmega328P.OCR2B_ADDR);
                }

                if (phaseCorrect_UPCount) {
                    progress = (byte) (progress + 1);
                } else {
                    progress = (byte) (progress - 1);
                }

                phaseCorrect_UPCount_old = phaseCorrect_UPCount;

                if (progress == BOTTOM) {
                    UCModule.interruptionModule.timer2Overflow();
                    phaseCorrect_UPCount = true;
                } else if (progress == doubleBufferOCR2A) {
                    phaseCorrect_UPCount = false;
                }

                if (progress == doubleBufferOCR2A) {
                    UCModule.interruptionModule.timer2MatchA();
                    match_A = true;
                }
                if (progress == doubleBufferOCR2B) {
                    UCModule.interruptionModule.timer2MatchB();
                    match_B = true;
                }

                byte outputMode = dataMemory.readByte(DataMemory_ATmega328P.TCCR2A_ADDR);

                //CHANEL A
                switch (0xC0 & outputMode) {
                    case 0x00:
                        //OC2A disconected
                        timerOutputControl_OC2A = false;
                        break;
                    case 0x40:
                        timerOutputControl_OC2A = true;
                        if (match_A) {
                            stateOC2A = (stateOC2A + 1) % 2;
                            ioModule.setOC2A(stateOC2A, UCModule_View.simulatedTime);
                        }
                        break;
                    case 0x80:
                        //OC2A Clear on Compare Match counting up, OC2A Set on Compare Match counting down
                        timerOutputControl_OC2A = true;
                        if (doubleBufferOCR2A == MAX) {
                            stateOC2A = IOModule.HIGH_LEVEL;
                            ioModule.setOC2A(stateOC2A, UCModule_View.simulatedTime);
                        } else if (doubleBufferOCR2A == BOTTOM) {
                            stateOC2A = IOModule.LOW_LEVEL;
                            ioModule.setOC2A(stateOC2A, UCModule_View.simulatedTime);
                        } else {
                            if (match_A) {
                                if (phaseCorrect_UPCount_old) {
                                    stateOC2A = IOModule.LOW_LEVEL;
                                } else {
                                    stateOC2A = IOModule.HIGH_LEVEL;
                                }
                                ioModule.setOC2A(stateOC2A, UCModule_View.simulatedTime);
                            }
                        }
                        break;
                    case 0xC0:
                        //OC2A Set on Compare Match counting up, OC2A Clear on Compare Match counting down
                        timerOutputControl_OC2A = true;
                        if (doubleBufferOCR2A == MAX) {
                            stateOC2A = IOModule.LOW_LEVEL;
                            ioModule.setOC2A(stateOC2A, UCModule_View.simulatedTime);
                        } else if (doubleBufferOCR2A == BOTTOM) {
                            stateOC2A = IOModule.HIGH_LEVEL;
                            ioModule.setOC2A(stateOC2A, UCModule_View.simulatedTime);
                        } else {
                            if (match_A) {
                                if (phaseCorrect_UPCount_old) {
                                    stateOC2A = IOModule.HIGH_LEVEL;
                                } else {
                                    stateOC2A = IOModule.LOW_LEVEL;
                                }
                                ioModule.setOC2A(stateOC2A, UCModule_View.simulatedTime);
                            }
                        }
                }

                //CHANEL B
                switch (0x30 & outputMode) {
                    case 0x00:
                    case 0x10:
                        //OC2B disconected
                        timerOutputControl_OC2B = false;
                        break;
                    case 0x20:
                        //OC2B Clear on Compare Match counting up, OC2B Set on Compare Match counting down
                        timerOutputControl_OC2B = true;
                        if (match_B) {
                            if (phaseCorrect_UPCount_old) {
                                stateOC2B = IOModule.LOW_LEVEL;
                            } else {
                                stateOC2B = IOModule.HIGH_LEVEL;
                            }
                            ioModule.setOC2B(stateOC2B, UCModule_View.simulatedTime);
                        }
                        break;
                    case 0x30:
                        //OC2B Set on Compare Match counting up, OC2B Clear on Compare Match counting down
                        timerOutputControl_OC2B = true;
                        if (match_B) {
                            if (phaseCorrect_UPCount_old) {
                                stateOC2B = IOModule.HIGH_LEVEL;
                            } else {
                                stateOC2B = IOModule.LOW_LEVEL;
                            }
                            ioModule.setOC2B(stateOC2B, UCModule_View.simulatedTime);
                        }
                }

                dataMemory.writeByte(DataMemory_ATmega328P.TCNT2_ADDR, progress);
            }
        },
        FAST_PWM_TOP_OCRA {
            @Override
            public void count() {
                boolean match_A = false, match_B = false;
                byte progress = dataMemory.readByte(DataMemory_ATmega328P.TCNT2_ADDR);
                if (progress == BOTTOM) {
                    doubleBufferOCR2A = dataMemory.readByte(DataMemory_ATmega328P.OCR2A_ADDR);
                    doubleBufferOCR2B = dataMemory.readByte(DataMemory_ATmega328P.OCR2B_ADDR);
                }

                progress = (byte) (progress + 1);

                if (nextClear) {
                    nextClear = false;
                    progress = BOTTOM;
                    UCModule.interruptionModule.timer2Overflow();
                }

                if (progress == doubleBufferOCR2A) {
                    UCModule.interruptionModule.timer2MatchA();
                    match_A = true;
                    nextClear = true;
                }
                if (progress == doubleBufferOCR2B) {
                    UCModule.interruptionModule.timer2MatchB();
                    match_B = true;
                }


                byte outputMode = dataMemory.readByte(DataMemory_ATmega328P.TCCR2A_ADDR);

                //CHANEL A
                switch (0xC0 & outputMode) {
                    case 0x00:
                        //OC2A disconected
                        timerOutputControl_OC2A = false;
                        break;
                    case 0x40:
                        timerOutputControl_OC2A = true;
                        if (match_A) {
                            stateOC2A = (stateOC2A + 1) % 2;  //Toggle
                            ioModule.setOC2A(stateOC2A, UCModule_View.simulatedTime);
                        }
                        break;
                    case 0x80:
                        //OC2A Clear on Compare Match, set at BOTTOM
                        timerOutputControl_OC2A = true;
                        if (doubleBufferOCR2A == MAX) {
                            stateOC2A = IOModule.LOW_LEVEL;
                            ioModule.setOC2A(stateOC2A, UCModule_View.simulatedTime);
                        } else {
                            if (match_A) {
                                stateOC2A = IOModule.LOW_LEVEL;
                                ioModule.setOC2A(stateOC2A, UCModule_View.simulatedTime);
                            }
                            if (progress == BOTTOM) {
                                stateOC2A = IOModule.HIGH_LEVEL;
                                ioModule.setOC2A(stateOC2A, UCModule_View.simulatedTime);
                            }
                        }
                        break;
                    case 0xC0:
                        //OC2A Set on Compare Match, clear at BOTTOM
                        timerOutputControl_OC2A = true;
                        if (doubleBufferOCR2A == MAX) {
                            stateOC2A = IOModule.HIGH_LEVEL;
                            ioModule.setOC2A(stateOC2A, UCModule_View.simulatedTime);
                        } else {
                            if (match_A) {
                                stateOC2A = IOModule.HIGH_LEVEL;
                                ioModule.setOC2A(stateOC2A, UCModule_View.simulatedTime);
                            }

                            if (progress == BOTTOM) {
                                stateOC2A = IOModule.LOW_LEVEL;
                                ioModule.setOC2A(stateOC2A, UCModule_View.simulatedTime);
                            }
                        }

                }

                //CHANEL B
                switch (0x30 & outputMode) {
                    case 0x00:
                    case 0x10:
                        //OC2B disconected
                        timerOutputControl_OC2B = false;
                        break;
                    case 0x20:
                        //OC2B Clear on Compare Match, set at BOTTOM
                        timerOutputControl_OC2B = true;
                        if (match_B) {
                            stateOC2B = IOModule.LOW_LEVEL;
                            ioModule.setOC2B(stateOC2B, UCModule_View.simulatedTime);
                        }
                        if (progress == BOTTOM) {
                            stateOC2B = IOModule.HIGH_LEVEL;
                            ioModule.setOC2B(stateOC2B, UCModule_View.simulatedTime);
                        }
                        break;
                    case 0x30:
                        //OC2B Set on Compare Match
                        timerOutputControl_OC2B = true;
                        if (match_B) {
                            stateOC2B = IOModule.HIGH_LEVEL;
                            ioModule.setOC2B(stateOC2B, UCModule_View.simulatedTime);
                        }
                        if (progress == BOTTOM) {
                            stateOC2B = IOModule.LOW_LEVEL;
                            ioModule.setOC2B(stateOC2B, UCModule_View.simulatedTime);
                        }
                }

                dataMemory.writeByte(DataMemory_ATmega328P.TCNT2_ADDR, progress);
            }
        };

        public abstract void count();
    }
}
