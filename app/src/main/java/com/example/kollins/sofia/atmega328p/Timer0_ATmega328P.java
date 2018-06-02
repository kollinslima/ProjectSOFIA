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
import com.example.kollins.sofia.ucinterfaces.Timer0Module;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class Timer0_ATmega328P implements Timer0Module {

    private static final String TIMER0_TAG = "Timer0";

    private static final byte MAX = -1; //0xFF signed
    private static final byte BOTTOM = 0x00;

    public static boolean timerOutputControl_OC0A;
    public static boolean timerOutputControl_OC0B;

    private static DataMemory_ATmega328P dataMemory;
    private static Handler uCHandler;
    private static IOModule_ATmega328P ioModule;
    private UCModule uCModule;

    private static Lock clockLock;
    private static Condition timer0ClockCondition;

    private static boolean oldExternalT0, newExternalT0;
    private boolean buffer_WGM02;

    private static int stateOC0A, stateOC0B;
    private static boolean nextOverflow, nextClear, phaseCorrect_UPCount;
    private static byte doubleBufferOCR0A, doubleBufferOCR0B;

    public Timer0_ATmega328P(DataMemory dataMemory, Handler uCHandler, Lock clockLock, UCModule uCModule, IOModule ioModule) {
        this.dataMemory = (DataMemory_ATmega328P) dataMemory;
        this.uCHandler = uCHandler;
        this.clockLock = clockLock;
        this.uCModule = uCModule;
        this.ioModule = (IOModule_ATmega328P) ioModule;

        timer0ClockCondition = clockLock.newCondition();
        oldExternalT0 = dataMemory.readBit(DataMemory_ATmega328P.PIND_ADDR, 4);

        timerOutputControl_OC0A = false;
        timerOutputControl_OC0B = false;
        phaseCorrect_UPCount = true;

        stateOC0A = IOModule.TRI_STATE;
        stateOC0B = IOModule.TRI_STATE;

        nextOverflow = false;
        nextClear = false;

        doubleBufferOCR0A = 0;
        doubleBufferOCR0B = 0;
    }

    @Override
    public void run() {
        Thread.currentThread().setName("TIMER 0");
        while (!uCModule.getResetFlag()) {
            if (ClockSource.values()[0x07 & dataMemory.readByte(DataMemory_ATmega328P.TCCR0B_ADDR)].work()) {

                if (dataMemory.readBit(DataMemory_ATmega328P.GTCCR_ADDR, 0)) {
                    continue;   //Synchronization Mode
                }

                buffer_WGM02 = dataMemory.readBit(DataMemory_ATmega328P.TCCR0B_ADDR, 3);

                switch (0x03 & dataMemory.readByte(DataMemory_ATmega328P.TCCR0A_ADDR)) {
                    case 0x00:
                        if (!buffer_WGM02) {
                            TimerMode.NORMAL_OPERATION.count();
                        }
                        break;
                    case 0x01:
                        if (buffer_WGM02) {
                            TimerMode.PWM_PHASE_CORRECT_TOP_OCRA.count();
                        } else {
                            TimerMode.PWM_PHASE_CORRECT_TOP_0XFF.count();
                        }
                        break;
                    case 0x02:
                        if (!buffer_WGM02) {
                            TimerMode.CTC_OPERATION.count();
                        }
                        break;
                    case 0x03:
                        if (buffer_WGM02) {
                            TimerMode.FAST_PWM_TOP_OCRA.count();
                        } else {
                            TimerMode.FAST_PWM_TOP_0XFF.count();
                        }
                        break;
                    default:
                        break;
                }
            }
        }

        Log.i(UCModule.MY_LOG_TAG, "Finishing Timer 0");
    }

    private static void waitClock() {

        clockLock.lock();
        try {
            UCModule.clockVector[UCModule.TIMER0_ID] = true;

            for (int i = 0; i < UCModule.clockVector.length; i++) {
                if (!UCModule.clockVector[i]) {

                    while (UCModule.clockVector[UCModule.TIMER0_ID]) {
                        timer0ClockCondition.await();
                    }

                    return;
                }
            }

            UCModule.resetClockVector();

            //Send Broadcast
            uCHandler.sendEmptyMessage(UCModule.CLOCK_ACTION);

        } catch (InterruptedException e) {
            Log.e(UCModule.MY_LOG_TAG, "ERROR: waitClock Timer0", e);
        } finally {
            clockLock.unlock();
        }
    }

    @Override
    public void clockTimer0() {
        clockLock.lock();
        try {
            timer0ClockCondition.signal();
        } finally {
            clockLock.unlock();
        }
    }

    public enum ClockSource {
        NO_CLOCK_SOURCE {
            @Override
            public boolean work() {
                Log.i(TIMER0_TAG, "No Clock Source");
                waitClock();
                return false;
            }
        },
        CLOCK_PRESCALER_1 {
            @Override
            public boolean work() {
                Log.i(TIMER0_TAG, "Prescaler 1");
                waitClock();
                return true;
            }
        },
        CLOCK_PRESCALER_8 {
            @Override
            public boolean work() {
                Log.i(TIMER0_TAG, "Prescaler 8");
                for (int i = 0; i < 8; i++) {
                    waitClock();
                }
                return true;
            }
        },
        CLOCK_PRESCALER_64 {
            @Override
            public boolean work() {
                Log.i(TIMER0_TAG, "Prescaler 64");
                for (int i = 0; i < 64; i++) {
                    waitClock();
                }
                return false;
            }
        },
        CLOCK_PRESCALER_256 {
            @Override
            public boolean work() {
                Log.i(TIMER0_TAG, "Prescaler 256");
                for (int i = 0; i < 256; i++) {
                    waitClock();
                }
                return true;
            }
        },
        CLOCK_PRESCALER_1024 {
            @Override
            public boolean work() {
                Log.i(TIMER0_TAG, "Prescaler 1024");
                for (int i = 0; i < 1024; i++) {
                    waitClock();
                }
                return true;
            }
        },
        EXTERNAL_CLOCK_T0_FALLING_EDGE {
            @Override
            public boolean work() {
                waitClock();
                newExternalT0 = dataMemory.readBit(DataMemory_ATmega328P.PIND_ADDR, 4);
                if (oldExternalT0 & !newExternalT0) {
                    oldExternalT0 = newExternalT0;
                    return true;
                } else {
                    oldExternalT0 = newExternalT0;
                    return false;
                }
            }
        },
        EXTERNAL_CLOCK_T0_RISING_EDGE {
            @Override
            public boolean work() {
                waitClock();
                newExternalT0 = dataMemory.readBit(DataMemory_ATmega328P.PIND_ADDR, 4);
                if (!oldExternalT0 & newExternalT0) {
                    oldExternalT0 = newExternalT0;
                    return true;
                } else {
                    oldExternalT0 = newExternalT0;
                    return false;
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
                byte progress = dataMemory.readByte(DataMemory_ATmega328P.TCNT0_ADDR);
                progress = (byte) (progress + 1);

                if (progress == BOTTOM) {
                    UCModule.interruptionModule.timer0Overflow();
                }
                if (dataMemory.readForceMatchA_timer0()) {
                    match_A = true; //FORCE MATCH
                } else if (progress == dataMemory.readByte(DataMemory_ATmega328P.OCR0A_ADDR)) {
                    UCModule.interruptionModule.timer0MatchA();
                    match_A = true;
                }
                if (dataMemory.readForceMatchB_timer0()) {
                    match_B = true; //FORCE MATCH
                } else if (progress == dataMemory.readByte(DataMemory_ATmega328P.OCR0B_ADDR)) {
                    UCModule.interruptionModule.timer0MatchB();
                    match_B = true;
                }


                byte outputMode = dataMemory.readByte(DataMemory_ATmega328P.TCCR0A_ADDR);

                //CHANEL A
                switch (0xC0 & outputMode) {
                    case 0x00:
                        //OC0A disconected
                        timerOutputControl_OC0A = false;
                        break;
                    case 0x40:
                        //OC0A Toggle on Compare Match
                        timerOutputControl_OC0A = true;
                        if (match_A) {
                            stateOC0A = (stateOC0A + 1) % 2;
                            ioModule.setOC0A(stateOC0A, UCModule_View.simulatedTime);
                        }
                        break;
                    case 0x80:
                        //OC0A Clear on Compare Match
                        timerOutputControl_OC0A = true;
                        if (match_A) {
                            stateOC0A = IOModule.LOW_LEVEL;
                            ioModule.setOC0A(stateOC0A, UCModule_View.simulatedTime);
                        }
                        break;
                    case 0xC0:
                        //OC0A Set on Compare Match
                        timerOutputControl_OC0A = true;
                        if (match_A) {
                            stateOC0A = IOModule.HIGH_LEVEL;
                            ioModule.setOC0A(stateOC0A, UCModule_View.simulatedTime);
                        }
                        break;

                    default:
                        break;
                }

                //CHANEL B
                switch (0x30 & outputMode) {
                    case 0x00:
                        //OC0B disconected
                        timerOutputControl_OC0B = false;
                        break;
                    case 0x10:
                        //OC0B Toggle on Compare Match
                        timerOutputControl_OC0B = true;
                        if (match_B) {
                            stateOC0B = (stateOC0B + 1) % 2;
                            ioModule.setOC0B(stateOC0B, UCModule_View.simulatedTime);
                        }
                        break;
                    case 0x20:
                        //OC0B Clear on Compare Match
                        timerOutputControl_OC0B = true;
                        if (match_B) {
                            stateOC0B = IOModule.LOW_LEVEL;
                            ioModule.setOC0B(stateOC0B, UCModule_View.simulatedTime);
                        }
                        break;
                    case 0x30:
                        //OC0B Set on Compare Match
                        timerOutputControl_OC0B = true;
                        if (match_B) {
                            stateOC0B = IOModule.HIGH_LEVEL;
                            ioModule.setOC0B(stateOC0B, UCModule_View.simulatedTime);
                        }
                        break;
                    default:
                        break;
                }

                dataMemory.writeByte(DataMemory_ATmega328P.TCNT0_ADDR, progress);
            }
        },
        PWM_PHASE_CORRECT_TOP_0XFF {
            @Override
            public void count() {
                boolean match_A = false, match_B = false;
                byte progress = dataMemory.readByte(DataMemory_ATmega328P.TCNT0_ADDR);
                if (progress == MAX) {
                    doubleBufferOCR0A = dataMemory.readByte(DataMemory_ATmega328P.OCR0A_ADDR);
                    doubleBufferOCR0B = dataMemory.readByte(DataMemory_ATmega328P.OCR0B_ADDR);
                }

                if (phaseCorrect_UPCount) {
                    progress = (byte) (progress + 1);
                } else {
                    progress = (byte) (progress - 1);
                }

                if (progress == BOTTOM) {
                    UCModule.interruptionModule.timer0Overflow();
                    phaseCorrect_UPCount = true;
                } else if (progress == MAX) {
                    phaseCorrect_UPCount = false;
                }

                if (progress == doubleBufferOCR0A) {
                    UCModule.interruptionModule.timer0MatchA();
                    match_A = true;
                }
                if (progress == doubleBufferOCR0B) {
                    UCModule.interruptionModule.timer0MatchB();
                    match_B = true;
                }

                byte outputMode = dataMemory.readByte(DataMemory_ATmega328P.TCCR0A_ADDR);

                //CHANEL A
                switch (0xC0 & outputMode) {
                    case 0x00:
                    case 0x40:
                        //OC0A disconected
                        timerOutputControl_OC0A = false;
                        break;
                    case 0x80:
                        //OC0A Clear on Compare Match counting up, OC0A Set on Compare Match counting down
                        timerOutputControl_OC0A = true;
                        if (doubleBufferOCR0A == MAX) {
                            stateOC0A = IOModule.HIGH_LEVEL;
                            ioModule.setOC0A(stateOC0A, UCModule_View.simulatedTime);
                        } else if (doubleBufferOCR0A == BOTTOM) {
                            stateOC0A = IOModule.LOW_LEVEL;
                            ioModule.setOC0A(stateOC0A, UCModule_View.simulatedTime);
                        } else {
                            if (match_A) {
                                if (phaseCorrect_UPCount) {
                                    stateOC0A = IOModule.LOW_LEVEL;
                                } else {
                                    stateOC0A = IOModule.HIGH_LEVEL;
                                }
                                ioModule.setOC0A(stateOC0A, UCModule_View.simulatedTime);
                            }
                        }
                        break;
                    case 0xC0:
                        //OC0A Set on Compare Match counting up, OC0A Clear on Compare Match counting down
                        timerOutputControl_OC0A = true;
                        if (doubleBufferOCR0A == MAX) {
                            stateOC0A = IOModule.LOW_LEVEL;
                            ioModule.setOC0A(stateOC0A, UCModule_View.simulatedTime);
                        } else if (doubleBufferOCR0A == BOTTOM) {
                            stateOC0A = IOModule.HIGH_LEVEL;
                            ioModule.setOC0A(stateOC0A, UCModule_View.simulatedTime);
                        } else {
                            if (match_A) {
                                if (phaseCorrect_UPCount) {
                                    stateOC0A = IOModule.HIGH_LEVEL;
                                } else {
                                    stateOC0A = IOModule.LOW_LEVEL;
                                }
                                ioModule.setOC0A(stateOC0A, UCModule_View.simulatedTime);
                            }
                        }
                        break;

                    default:
                        break;
                }

                //CHANEL B
                switch (0x30 & outputMode) {
                    case 0x00:
                    case 0x10:
                        //OC0B disconected
                        timerOutputControl_OC0B = false;
                        break;
                    case 0x20:
                        //OC0B Clear on Compare Match counting up, OC0B Set on Compare Match counting down
                        timerOutputControl_OC0B = true;
                        if (match_B) {
                            if (phaseCorrect_UPCount) {
                                stateOC0B = IOModule.LOW_LEVEL;
                            } else {
                                stateOC0B = IOModule.HIGH_LEVEL;
                            }
                            ioModule.setOC0B(stateOC0B, UCModule_View.simulatedTime);
                        }
                        break;
                    case 0x30:
                        //OC0B Set on Compare Match counting up, OC0B Clear on Compare Match counting down
                        timerOutputControl_OC0B = true;
                        if (match_B) {
                            if (phaseCorrect_UPCount) {
                                stateOC0B = IOModule.HIGH_LEVEL;
                            } else {
                                stateOC0B = IOModule.LOW_LEVEL;
                            }
                            ioModule.setOC0B(stateOC0B, UCModule_View.simulatedTime);
                        }
                        break;

                    default:
                        break;
                }

                dataMemory.writeByte(DataMemory_ATmega328P.TCNT0_ADDR, progress);
            }
        },
        CTC_OPERATION {
            @Override
            public void count() {
                boolean match_A = false, match_B = false;
                byte progress = dataMemory.readByte(DataMemory_ATmega328P.TCNT0_ADDR);
                progress = (byte) (progress + 1);

                if (nextClear) {
                    nextClear = false;
                    progress = BOTTOM;
                }

                if (progress == BOTTOM && nextOverflow) {
                    nextOverflow = false;
                    UCModule.interruptionModule.timer0Overflow();
                } else if (progress == MAX) {
                    nextOverflow = true;
                }

                if (dataMemory.readForceMatchA_timer0()) {
                    match_A = true; //FORCE MATCH
                } else if (progress == dataMemory.readByte(DataMemory_ATmega328P.OCR0A_ADDR)) {
                    UCModule.interruptionModule.timer0MatchA();
                    match_A = true;
                    nextClear = true;
                }

                if (dataMemory.readForceMatchB_timer0()) {
                    match_B = true; //FORCE MATCH
                } else if (progress == dataMemory.readByte(DataMemory_ATmega328P.OCR0B_ADDR)) {
                    UCModule.interruptionModule.timer0MatchB();
                    match_B = true;
                }

                byte outputMode = dataMemory.readByte(DataMemory_ATmega328P.TCCR0A_ADDR);

                //CHANEL A
                switch (0xC0 & outputMode) {
                    case 0x00:
                        //OC0A disconected
                        timerOutputControl_OC0A = false;
                        break;
                    case 0x40:
                        //OC0A Toggle on Compare Match
                        timerOutputControl_OC0A = true;
                        if (match_A) {
                            stateOC0A = (stateOC0A + 1) % 2;
                            ioModule.setOC0A(stateOC0A, UCModule_View.simulatedTime);
                        }
                        break;
                    case 0x80:
                        //OC0A Clear on Compare Match
                        timerOutputControl_OC0A = true;
                        if (match_A) {
                            stateOC0A = IOModule.LOW_LEVEL;
                            ioModule.setOC0A(stateOC0A, UCModule_View.simulatedTime);
                        }
                        break;
                    case 0xC0:
                        //OC0A Set on Compare Match
                        timerOutputControl_OC0A = true;
                        if (match_A) {
                            stateOC0A = IOModule.HIGH_LEVEL;
                            ioModule.setOC0A(stateOC0A, UCModule_View.simulatedTime);
                        }
                        break;

                    default:
                        break;
                }

                //CHANEL B
                switch (0x30 & outputMode) {
                    case 0x00:
                        //OC0B disconected
                        timerOutputControl_OC0B = false;
                        break;
                    case 0x10:
                        //OC0B Toggle on Compare Match
                        timerOutputControl_OC0B = true;
                        if (match_B) {
                            stateOC0B = (stateOC0B + 1) % 2;
                            ioModule.setOC0B(stateOC0B, UCModule_View.simulatedTime);
                        }
                        break;
                    case 0x20:
                        //OC0B Clear on Compare Match
                        timerOutputControl_OC0B = true;
                        if (match_B) {
                            stateOC0B = IOModule.LOW_LEVEL;
                            ioModule.setOC0B(stateOC0B, UCModule_View.simulatedTime);
                        }
                        break;
                    case 0x30:
                        //OC0B Set on Compare Match
                        timerOutputControl_OC0B = true;
                        if (match_B) {
                            stateOC0B = IOModule.HIGH_LEVEL;
                            ioModule.setOC0B(stateOC0B, UCModule_View.simulatedTime);
                        }
                        break;

                    default:
                        break;
                }

                dataMemory.writeByte(DataMemory_ATmega328P.TCNT0_ADDR, progress);
            }
        },
        FAST_PWM_TOP_0XFF {
            @Override
            public void count() {
                boolean match_A = false, match_B = false;
                byte progress = dataMemory.readByte(DataMemory_ATmega328P.TCNT0_ADDR);
                if (progress == BOTTOM) {
                    doubleBufferOCR0A = dataMemory.readByte(DataMemory_ATmega328P.OCR0A_ADDR);
                    doubleBufferOCR0B = dataMemory.readByte(DataMemory_ATmega328P.OCR0B_ADDR);
                }

                progress = (byte) (progress + 1);

                if (progress == BOTTOM) {
                    UCModule.interruptionModule.timer0Overflow();
                }
                if (progress == doubleBufferOCR0A) {
                    UCModule.interruptionModule.timer0MatchA();
                    match_A = true;
                }
                if (progress == doubleBufferOCR0B) {
                    UCModule.interruptionModule.timer0MatchB();
                    match_B = true;
                }

                byte outputMode = dataMemory.readByte(DataMemory_ATmega328P.TCCR0A_ADDR);

                //CHANEL A
                switch (0xC0 & outputMode) {
                    case 0x00:
                    case 0x40:
                        //OC0A disconected
                        timerOutputControl_OC0A = false;
                        break;
                    case 0x80:
                        //OC0A Clear on Compare Match, set at BOTTOM
                        timerOutputControl_OC0A = true;
                        if (doubleBufferOCR0A == MAX) {
                            stateOC0A = IOModule.HIGH_LEVEL;
                            ioModule.setOC0A(stateOC0A, UCModule_View.simulatedTime);
                        } else {
                            if (match_A) {
                                stateOC0A = IOModule.LOW_LEVEL;
                                ioModule.setOC0A(stateOC0A, UCModule_View.simulatedTime);
                            }
                            if (progress == BOTTOM) {
                                stateOC0A = IOModule.HIGH_LEVEL;
                                ioModule.setOC0A(stateOC0A, UCModule_View.simulatedTime);
                            }
                        }
                        break;
                    case 0xC0:
                        //OC0A Set on Compare Match, clear at BOTTOM
                        timerOutputControl_OC0A = true;
                        if (doubleBufferOCR0A == MAX) {
                            stateOC0A = IOModule.LOW_LEVEL;
                            ioModule.setOC0A(stateOC0A, UCModule_View.simulatedTime);
                        } else {
                            if (match_A) {
                                stateOC0A = IOModule.HIGH_LEVEL;
                                ioModule.setOC0A(stateOC0A, UCModule_View.simulatedTime);
                            }

                            if (progress == BOTTOM) {
                                stateOC0A = IOModule.LOW_LEVEL;
                                ioModule.setOC0A(stateOC0A, UCModule_View.simulatedTime);
                            }
                        }
                        break;

                    default:
                        break;

                }

                //CHANEL B
                switch (0x30 & outputMode) {
                    case 0x00:
                    case 0x10:
                        //OC0B disconected
                        timerOutputControl_OC0B = false;
                        break;
                    case 0x20:
                        //OC0B Clear on Compare Match, set at BOTTOM
                        timerOutputControl_OC0B = true;
                        if (match_B) {
                            stateOC0B = IOModule.LOW_LEVEL;
                            ioModule.setOC0B(stateOC0B, UCModule_View.simulatedTime);
                        }
                        if (progress == BOTTOM) {
                            stateOC0B = IOModule.HIGH_LEVEL;
                            ioModule.setOC0B(stateOC0B, UCModule_View.simulatedTime);
                        }
                        break;
                    case 0x30:
                        //OC0B Set on Compare Match
                        timerOutputControl_OC0B = true;
                        if (match_B) {
                            stateOC0B = IOModule.HIGH_LEVEL;
                            ioModule.setOC0B(stateOC0B, UCModule_View.simulatedTime);
                        }
                        if (progress == BOTTOM) {
                            stateOC0B = IOModule.LOW_LEVEL;
                            ioModule.setOC0B(stateOC0B, UCModule_View.simulatedTime);
                        }
                        break;

                    default:
                        break;
                }

                dataMemory.writeByte(DataMemory_ATmega328P.TCNT0_ADDR, progress);
            }
        },
        PWM_PHASE_CORRECT_TOP_OCRA {
            @Override
            public void count() {
                boolean match_A = false, match_B = false;
                byte progress = dataMemory.readByte(DataMemory_ATmega328P.TCNT0_ADDR);
                if (progress == doubleBufferOCR0A) {
                    doubleBufferOCR0A = dataMemory.readByte(DataMemory_ATmega328P.OCR0A_ADDR);
                    doubleBufferOCR0B = dataMemory.readByte(DataMemory_ATmega328P.OCR0B_ADDR);
                }

                if (phaseCorrect_UPCount) {
                    progress = (byte) (progress + 1);
                } else {
                    progress = (byte) (progress - 1);
                }

                if (progress == BOTTOM) {
                    UCModule.interruptionModule.timer0Overflow();
                    phaseCorrect_UPCount = true;
                } else if (progress == doubleBufferOCR0A) {
                    phaseCorrect_UPCount = false;
                }

                if (progress == doubleBufferOCR0A) {
                    UCModule.interruptionModule.timer0MatchA();
                    match_A = true;
                }
                if (progress == doubleBufferOCR0B) {
                    UCModule.interruptionModule.timer0MatchB();
                    match_B = true;
                }

                byte outputMode = dataMemory.readByte(DataMemory_ATmega328P.TCCR0A_ADDR);

                //CHANEL A
                switch (0xC0 & outputMode) {
                    case 0x00:
                        //OC0A disconected
                        timerOutputControl_OC0A = false;
                        break;
                    case 0x40:
                        timerOutputControl_OC0A = true;
                        if (match_A) {
                            stateOC0A = (stateOC0A + 1) % 2;
                            ioModule.setOC0A(stateOC0A, UCModule_View.simulatedTime);
                        }
                        break;
                    case 0x80:
                        //OC0A Clear on Compare Match counting up, OC0A Set on Compare Match counting down
                        timerOutputControl_OC0A = true;
                        if (doubleBufferOCR0A == MAX) {
                            stateOC0A = IOModule.HIGH_LEVEL;
                            ioModule.setOC0A(stateOC0A, UCModule_View.simulatedTime);
                        } else if (doubleBufferOCR0A == BOTTOM) {
                            stateOC0A = IOModule.LOW_LEVEL;
                            ioModule.setOC0A(stateOC0A, UCModule_View.simulatedTime);
                        } else {
                            if (match_A) {
                                if (phaseCorrect_UPCount) {
                                    stateOC0A = IOModule.LOW_LEVEL;
                                } else {
                                    stateOC0A = IOModule.HIGH_LEVEL;
                                }
                                ioModule.setOC0A(stateOC0A, UCModule_View.simulatedTime);
                            }
                        }
                        break;
                    case 0xC0:
                        //OC0A Set on Compare Match counting up, OC0A Clear on Compare Match counting down
                        timerOutputControl_OC0A = true;
                        if (doubleBufferOCR0A == MAX) {
                            stateOC0A = IOModule.LOW_LEVEL;
                            ioModule.setOC0A(stateOC0A, UCModule_View.simulatedTime);
                        } else if (doubleBufferOCR0A == BOTTOM) {
                            stateOC0A = IOModule.HIGH_LEVEL;
                            ioModule.setOC0A(stateOC0A, UCModule_View.simulatedTime);
                        } else {
                            if (match_A) {
                                if (phaseCorrect_UPCount) {
                                    stateOC0A = IOModule.HIGH_LEVEL;
                                } else {
                                    stateOC0A = IOModule.LOW_LEVEL;
                                }
                                ioModule.setOC0A(stateOC0A, UCModule_View.simulatedTime);
                            }
                        }
                        break;

                    default:
                        break;
                }

                //CHANEL B
                switch (0x30 & outputMode) {
                    case 0x00:
                    case 0x10:
                        //OC0B disconected
                        timerOutputControl_OC0B = false;
                        break;
                    case 0x20:
                        //OC0B Clear on Compare Match counting up, OC0B Set on Compare Match counting down
                        timerOutputControl_OC0B = true;
                        if (match_B) {
                            if (phaseCorrect_UPCount) {
                                stateOC0B = IOModule.LOW_LEVEL;
                            } else {
                                stateOC0B = IOModule.HIGH_LEVEL;
                            }
                            ioModule.setOC0B(stateOC0B, UCModule_View.simulatedTime);
                        }
                        break;
                    case 0x30:
                        //OC0B Set on Compare Match counting up, OC0B Clear on Compare Match counting down
                        timerOutputControl_OC0B = true;
                        if (match_B) {
                            if (phaseCorrect_UPCount) {
                                stateOC0B = IOModule.HIGH_LEVEL;
                            } else {
                                stateOC0B = IOModule.LOW_LEVEL;
                            }
                            ioModule.setOC0B(stateOC0B, UCModule_View.simulatedTime);
                        }
                        break;

                    default:
                        break;
                }

                dataMemory.writeByte(DataMemory_ATmega328P.TCNT0_ADDR, progress);
            }
        },
        FAST_PWM_TOP_OCRA {
            @Override
            public void count() {
                boolean match_A = false, match_B = false;
                byte progress = dataMemory.readByte(DataMemory_ATmega328P.TCNT0_ADDR);
                if (progress == BOTTOM) {
                    doubleBufferOCR0A = dataMemory.readByte(DataMemory_ATmega328P.OCR0A_ADDR);
                    doubleBufferOCR0B = dataMemory.readByte(DataMemory_ATmega328P.OCR0B_ADDR);
                }

                progress = (byte) (progress + 1);

                if (nextClear) {
                    nextClear = false;
                    progress = BOTTOM;
                    UCModule.interruptionModule.timer0Overflow();
                }

                if (progress == doubleBufferOCR0A) {
                    UCModule.interruptionModule.timer0MatchA();
                    match_A = true;
                    nextClear = true;
                }
                if (progress == doubleBufferOCR0B) {
                    UCModule.interruptionModule.timer0MatchB();
                    match_B = true;
                }


                byte outputMode = dataMemory.readByte(DataMemory_ATmega328P.TCCR0A_ADDR);

                //CHANEL A
                switch (0xC0 & outputMode) {
                    case 0x00:
                        //OC0A disconected
                        timerOutputControl_OC0A = false;
                        break;
                    case 0x40:
                        timerOutputControl_OC0A = true;
                        if (match_A) {
                            stateOC0A = (stateOC0A + 1) % 2;  //Toggle
                            ioModule.setOC0A(stateOC0A, UCModule_View.simulatedTime);
                        }
                        break;
                    case 0x80:
                        //OC0A Clear on Compare Match, set at BOTTOM
                        timerOutputControl_OC0A = true;
                        if (doubleBufferOCR0A == MAX) {
                            stateOC0A = IOModule.LOW_LEVEL;
                            ioModule.setOC0A(stateOC0A, UCModule_View.simulatedTime);
                        } else {
                            if (match_A) {
                                stateOC0A = IOModule.LOW_LEVEL;
                                ioModule.setOC0A(stateOC0A, UCModule_View.simulatedTime);
                            }
                            if (progress == BOTTOM) {
                                stateOC0A = IOModule.HIGH_LEVEL;
                                ioModule.setOC0A(stateOC0A, UCModule_View.simulatedTime);
                            }
                        }
                        break;
                    case 0xC0:
                        //OC0A Set on Compare Match, clear at BOTTOM
                        timerOutputControl_OC0A = true;
                        if (doubleBufferOCR0A == MAX) {
                            stateOC0A = IOModule.HIGH_LEVEL;
                            ioModule.setOC0A(stateOC0A, UCModule_View.simulatedTime);
                        } else {
                            if (match_A) {
                                stateOC0A = IOModule.HIGH_LEVEL;
                                ioModule.setOC0A(stateOC0A, UCModule_View.simulatedTime);
                            }

                            if (progress == BOTTOM) {
                                stateOC0A = IOModule.LOW_LEVEL;
                                ioModule.setOC0A(stateOC0A, UCModule_View.simulatedTime);
                            }
                        }
                        break;

                    default:
                        break;
                }

                //CHANEL B
                switch (0x30 & outputMode) {
                    case 0x00:
                    case 0x10:
                        //OC0B disconected
                        timerOutputControl_OC0B = false;
                        break;
                    case 0x20:
                        //OC0B Clear on Compare Match, set at BOTTOM
                        timerOutputControl_OC0B = true;
                        if (match_B) {
                            stateOC0B = IOModule.LOW_LEVEL;
                            ioModule.setOC0B(stateOC0B, UCModule_View.simulatedTime);
                        }
                        if (progress == BOTTOM) {
                            stateOC0B = IOModule.HIGH_LEVEL;
                            ioModule.setOC0B(stateOC0B, UCModule_View.simulatedTime);
                        }
                        break;
                    case 0x30:
                        //OC0B Set on Compare Match
                        timerOutputControl_OC0B = true;
                        if (match_B) {
                            stateOC0B = IOModule.HIGH_LEVEL;
                            ioModule.setOC0B(stateOC0B, UCModule_View.simulatedTime);
                        }
                        if (progress == BOTTOM) {
                            stateOC0B = IOModule.LOW_LEVEL;
                            ioModule.setOC0B(stateOC0B, UCModule_View.simulatedTime);
                        }
                        break;

                    default:
                        break;
                }

                dataMemory.writeByte(DataMemory_ATmega328P.TCNT0_ADDR, progress);
            }
        };

        public abstract void count();
    }
}
