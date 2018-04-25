package com.example.kollins.androidemulator.ATmega328P;

import android.os.Handler;
import android.util.Log;

import com.example.kollins.androidemulator.ATmega328P.IOModule_ATmega328P.IOModule_ATmega328P;
import com.example.kollins.androidemulator.UCModule;
import com.example.kollins.androidemulator.uCInterfaces.DataMemory;
import com.example.kollins.androidemulator.uCInterfaces.IOModule;
import com.example.kollins.androidemulator.uCInterfaces.Timer1Module;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class Timer1_ATmega328P implements Timer1Module {

    private static final char MAX = 0xFFFF;
    private static final char MAX_8B = 0x00FF;
    private static final char MAX_9B = 0x01FF;
    private static final char MAX_10B = 0x03FF;
    private static final char BOTTOM = 0x0000;

    public static boolean timerOutputControl_OC1A;
    public static boolean timerOutputControl_OC1B;

    private static DataMemory_ATmega328P dataMemory;
    private static Handler uCHandler;
    private static IOModule_ATmega328P ioModule;
    private UCModule uCModule;

    private static Lock clockLock;
    private static Condition timer1ClockCondition;

    private static boolean oldExternalT1, newExternalT1;
    private static boolean oldICP1, newICP1;

    private static int stateOC1A, stateOC1B;
    private static boolean nextOverflow, nextClear, phaseCorrect_UPCount;
    private static char doubleBufferOCR1A, doubleBufferOCR1B;

    private byte modeSelector;
    public static boolean enableICRWrite;

    public Timer1_ATmega328P(DataMemory dataMemory, Handler uCHandler, Lock clockLock, UCModule uCModule, IOModule ioModule) {
        this.dataMemory = (DataMemory_ATmega328P) dataMemory;
        this.uCHandler = uCHandler;
        this.clockLock = clockLock;
        this.uCModule = uCModule;
        this.ioModule = (IOModule_ATmega328P) ioModule;

        timer1ClockCondition = clockLock.newCondition();
        oldExternalT1 = dataMemory.readBit(DataMemory_ATmega328P.PIND_ADDR, 5);
        oldICP1 = dataMemory.readBit(DataMemory_ATmega328P.PINB_ADDR, 0);

        timerOutputControl_OC1A = false;
        timerOutputControl_OC1B = false;
        phaseCorrect_UPCount = true;

        stateOC1A = IOModule.TRI_STATE;
        stateOC1B = IOModule.TRI_STATE;

        nextOverflow = false;
        nextClear = false;

        enableICRWrite = false;
    }

    @Override
    public void run() {
        Thread.currentThread().setName("TIMER 1");
        while (!uCModule.getResetFlag()) {
            if (ClockSource.values()[0x07 & dataMemory.readByte(DataMemory_ATmega328P.TCCR1B_ADDR)].work()) {

                if (dataMemory.readBit(DataMemory_ATmega328P.GTCCR_ADDR, 0)) {
                    continue;   //Synchronization Mode
                }

                modeSelector = (byte) (0x03 & dataMemory.readByte(DataMemory_ATmega328P.TCCR1A_ADDR));
                modeSelector = (byte) (((0x18 & dataMemory.readByte(DataMemory_ATmega328P.TCCR1B_ADDR)) >> 1) | modeSelector);

                switch (modeSelector) {
                    case 0x00:
                        TimerMode.NORMAL_OPERATION.count();
                        break;
                    case 0x01:
                        break;
                    case 0x02:
                        break;
                    case 0x03:
                        break;
                    case 0x04:
                        TimerMode.CTC_OPERATION_TOP_OCR1A.count();
                        break;
                    case 0x05:
                        TimerMode.FAST_PWM_8B.count();
                        break;
                    case 0x06:
                        TimerMode.FAST_PWM_9B.count();
                        break;
                    case 0x07:
                        TimerMode.FAST_PWM_10B.count();
                        break;
                    case 0x08:
                        break;
                    case 0x09:
                        break;
                    case 0x0A:
                        break;
                    case 0x0B:
                        break;
                    case 0x0C:
                        TimerMode.CTC_OPERATION_TOP_ICR1.count();
                        break;
                    case 0x0E:
                        TimerMode.FAST_PWM_TOP_ICR1.count();
                        break;
                    case 0x0F:
                        TimerMode.FAST_PWM_TOP_OCRA.count();
                        break;
                }

            }
        }

        Log.i(UCModule.MY_LOG_TAG, "Finishing Timer 1");
    }

    private static void waitClock() {

        clockLock.lock();
        try {
            UCModule.clockVector[UCModule.TIMER1_ID] = true;

            for (int i = 0; i < UCModule.clockVector.length; i++) {
                if (!UCModule.clockVector[i]) {
                    timer1ClockCondition.await();
                    return;
                }
            }

            UCModule.resetClockVector();

            //Send Broadcast
            uCHandler.sendEmptyMessage(UCModule.CLOCK_ACTION);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            clockLock.unlock();
        }
    }

    @Override
    public void clockTimer1() {
        clockLock.lock();
        try {
            timer1ClockCondition.signal();
        } finally {
            clockLock.unlock();
        }
    }

    public enum ClockSource {
        NO_CLOCK_SOURCE {
            @Override
            public boolean work() {
                Log.i("Timer1", "No Clock Source");
                waitClock();
                return false;
            }
        },
        CLOCK_PRESCALER_1 {
            @Override
            public boolean work() {
                Log.i("Timer1", "Prescaler 1");
                waitClock();
                return true;
            }
        },
        CLOCK_PRESCALER_8 {
            @Override
            public boolean work() {
                Log.i("Timer1", "Prescaler 8");
                for (int i = 0; i < 8; i++) {
                    waitClock();
                }
                return true;
            }
        },
        CLOCK_PRESCALER_64 {
            @Override
            public boolean work() {
                Log.i("Timer1", "Prescaler 64");
                for (int i = 0; i < 64; i++) {
                    waitClock();
                }
                return false;
            }
        },
        CLOCK_PRESCALER_256 {
            @Override
            public boolean work() {
                Log.i("Timer1", "Prescaler 256");
                for (int i = 0; i < 256; i++) {
                    waitClock();
                }
                return true;
            }
        },
        CLOCK_PRESCALER_1024 {
            @Override
            public boolean work() {
                Log.i("Timer1", "Prescaler 1024");
                for (int i = 0; i < 1024; i++) {
                    waitClock();
                }
                return true;
            }
        },
        EXTERNAL_CLOCK_T1_FALLING_EDGE {
            @Override
            public boolean work() {
                waitClock();
                newExternalT1 = dataMemory.readBit(DataMemory_ATmega328P.PIND_ADDR, 5);
                if (oldExternalT1 & !newExternalT1) {
                    oldExternalT1 = newExternalT1;
                    return true;
                } else {
                    oldExternalT1 = newExternalT1;
                    return false;
                }
            }
        },
        EXTERNAL_CLOCK_T1_RISING_EDGE {
            @Override
            public boolean work() {
                waitClock();
                newExternalT1 = dataMemory.readBit(DataMemory_ATmega328P.PIND_ADDR, 5);
                if (!oldExternalT1 & newExternalT1) {
                    oldExternalT1 = newExternalT1;
                    return true;
                } else {
                    oldExternalT1 = newExternalT1;
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
                enableICRWrite = false;
                boolean match_A = false, match_B = false;
                char progress = (char) (0x00FF & dataMemory.readByte(DataMemory_ATmega328P.TCNT1L_ADDR));
                progress = (char) ((dataMemory.readByte(DataMemory_ATmega328P.TCNT1H_ADDR) << 8) | progress);
                progress += 1;

                if (progress == BOTTOM) {
                    UCModule.interruptionModule.timer1Overflow();
                }
                if (dataMemory.readForceMatchA_timer1()) {
                    match_A = true; //FORCE MATCH
                } else {
                    char ocr1a = (char) (0x00FF & dataMemory.readByte(DataMemory_ATmega328P.OCR1AL_ADDR));
                    ocr1a = (char) ((dataMemory.readByte(DataMemory_ATmega328P.OCR1AH_ADDR) << 8) | ocr1a);

                    if (progress == ocr1a) {
                        UCModule.interruptionModule.timer1MatchA();
                        match_A = true;
                    }
                }
                if (dataMemory.readForceMatchB_timer1()) {
                    match_B = true; //FORCE MATCH
                } else {
                    char ocr1b = (char) (0x00FF & dataMemory.readByte(DataMemory_ATmega328P.OCR1BL_ADDR));
                    ocr1b = (char) ((dataMemory.readByte(DataMemory_ATmega328P.OCR1BH_ADDR) << 8) | ocr1b);

                    if (progress == ocr1b) {
                        UCModule.interruptionModule.timer1MatchB();
                        match_B = true;
                    }
                }

                byte outputMode = dataMemory.readByte(DataMemory_ATmega328P.TCCR1A_ADDR);

                //CHANEL A
                switch (0xC0 & outputMode) {
                    case 0x00:
                        //OC1A disconected
                        timerOutputControl_OC1A = false;
                        break;
                    case 0x40:
                        //OC1A Toggle on Compare Match
                        timerOutputControl_OC1A = true;
                        if (match_A) {
                            stateOC1A = (stateOC1A + 1) % 2;
                            ioModule.setOC1A(stateOC1A);
                        }
                        break;
                    case 0x80:
                        //OC1A Clear on Compare Match
                        timerOutputControl_OC1A = true;
                        if (match_A) {
                            stateOC1A = IOModule.LOW_LEVEL;
                            ioModule.setOC1A(stateOC1A);
                        }
                        break;
                    case 0xC0:
                        //OC1A Set on Compare Match
                        timerOutputControl_OC1A = true;
                        if (match_A) {
                            stateOC1A = IOModule.HIGH_LEVEL;
                            ioModule.setOC1A(stateOC1A);
                        }
                }

                //CHANEL B
                switch (0x30 & outputMode) {
                    case 0x00:
                        //OC1B disconected
                        timerOutputControl_OC1B = false;
                        break;
                    case 0x10:
                        //OC0B Toggle on Compare Match
                        timerOutputControl_OC1B = true;
                        if (match_B) {
                            stateOC1B = (stateOC1B + 1) % 2;
                            ioModule.setOC1B(stateOC1B);
                        }
                        break;
                    case 0x20:
                        //OC0B Clear on Compare Match
                        timerOutputControl_OC1B = true;
                        if (match_B) {
                            stateOC1B = IOModule.LOW_LEVEL;
                            ioModule.setOC1B(stateOC1B);
                        }
                        break;
                    case 0x30:
                        //OC0B Set on Compare Match
                        timerOutputControl_OC1B = true;
                        if (match_B) {
                            stateOC1B = IOModule.HIGH_LEVEL;
                            ioModule.setOC1B(stateOC1B);
                        }
                }

                //Input Capture Unit
                newICP1 = dataMemory.readBit(DataMemory_ATmega328P.PINB_ADDR, 0);
                if (dataMemory.readBit(DataMemory_ATmega328P.TCCR1B_ADDR, 6)) {
                    //Rising Edge detect
                    if (!oldICP1 && newICP1) {
                        UCModule.interruptionModule.timer1InputCapture();
                        dataMemory.writeIOByte(DataMemory_ATmega328P.ICR1H_ADDR, (byte) (0x00FF & (progress >> 8)));
                        dataMemory.writeIOByte(DataMemory_ATmega328P.ICR1L_ADDR, (byte) (0x00FF & progress));
                    }
                } else {
                    //Falling Edge detect
                    if (oldICP1 && !newICP1) {
                        UCModule.interruptionModule.timer1InputCapture();
                        dataMemory.writeIOByte(DataMemory_ATmega328P.ICR1H_ADDR, (byte) (0x00FF & (progress >> 8)));
                        dataMemory.writeIOByte(DataMemory_ATmega328P.ICR1L_ADDR, (byte) (0x00FF & progress));
                    }
                }
                oldICP1 = newICP1;

                dataMemory.writeByte(DataMemory_ATmega328P.TCNT1H_ADDR, (byte) (0x00FF & (progress >> 8)));
                dataMemory.writeByte(DataMemory_ATmega328P.TCNT1L_ADDR, (byte) (0x00FF & progress));

            }
        },
        CTC_OPERATION_TOP_OCR1A {
            @Override
            public void count() {
                enableICRWrite = false;
                boolean match_A = false, match_B = false;
                char progress = (char) (0x00FF & dataMemory.readByte(DataMemory_ATmega328P.TCNT1L_ADDR));
                progress = (char) ((dataMemory.readByte(DataMemory_ATmega328P.TCNT1H_ADDR) << 8) | progress);
                progress += 1;

                if (nextClear) {
                    nextClear = false;
                    progress = BOTTOM;
                }

                if (progress == BOTTOM && nextOverflow) {
                    nextOverflow = false;
                    UCModule.interruptionModule.timer1Overflow();
                } else if (progress == MAX) {
                    nextOverflow = true;
                }

                if (dataMemory.readForceMatchA_timer1()) {
                    match_A = true; //FORCE MATCH
                } else {
                    char ocr1a = (char) (0x00FF & dataMemory.readByte(DataMemory_ATmega328P.OCR1AL_ADDR));
                    ocr1a = (char) ((dataMemory.readByte(DataMemory_ATmega328P.OCR1AH_ADDR) << 8) | ocr1a);

                    if (progress == ocr1a) {
                        UCModule.interruptionModule.timer1MatchA();
                        match_A = true;
                        nextClear = true;
                    }
                }

                if (dataMemory.readForceMatchB_timer1()) {
                    match_B = true; //FORCE MATCH
                } else {
                    char ocr1b = (char) (0x00FF & dataMemory.readByte(DataMemory_ATmega328P.OCR1BL_ADDR));
                    ocr1b = (char) ((dataMemory.readByte(DataMemory_ATmega328P.OCR1BH_ADDR) << 8) | ocr1b);

                    if (progress == ocr1b) {
                        UCModule.interruptionModule.timer1MatchB();
                        match_B = true;
                    }
                }

                byte outputMode = dataMemory.readByte(DataMemory_ATmega328P.TCCR1A_ADDR);

                //CHANEL A
                switch (0xC0 & outputMode) {
                    case 0x00:
                        //OC1A disconected
                        timerOutputControl_OC1A = false;
                        break;
                    case 0x40:
                        //OC1A Toggle on Compare Match
                        timerOutputControl_OC1A = true;
                        if (match_A) {
                            stateOC1A = (stateOC1A + 1) % 2;
                            ioModule.setOC1A(stateOC1A);
                        }
                        break;
                    case 0x80:
                        //OC1A Clear on Compare Match
                        timerOutputControl_OC1A = true;
                        if (match_A) {
                            stateOC1A = IOModule.LOW_LEVEL;
                            ioModule.setOC1A(stateOC1A);
                        }
                        break;
                    case 0xC0:
                        //OC1A Set on Compare Match
                        timerOutputControl_OC1A = true;
                        if (match_A) {
                            stateOC1A = IOModule.HIGH_LEVEL;
                            ioModule.setOC1A(stateOC1A);
                        }
                }

                //CHANEL B
                switch (0x30 & outputMode) {
                    case 0x00:
                        //OC1B disconected
                        timerOutputControl_OC1B = false;
                        break;
                    case 0x10:
                        //OC0B Toggle on Compare Match
                        timerOutputControl_OC1B = true;
                        if (match_B) {
                            stateOC1B = (stateOC1B + 1) % 2;
                            ioModule.setOC1B(stateOC1B);
                        }
                        break;
                    case 0x20:
                        //OC0B Clear on Compare Match
                        timerOutputControl_OC1B = true;
                        if (match_B) {
                            stateOC1B = IOModule.LOW_LEVEL;
                            ioModule.setOC1B(stateOC1B);
                        }
                        break;
                    case 0x30:
                        //OC0B Set on Compare Match
                        timerOutputControl_OC1B = true;
                        if (match_B) {
                            stateOC1B = IOModule.HIGH_LEVEL;
                            ioModule.setOC1B(stateOC1B);
                        }
                }

                //Input Capture Unit
                newICP1 = dataMemory.readBit(DataMemory_ATmega328P.PINB_ADDR, 0);
                if (dataMemory.readBit(DataMemory_ATmega328P.TCCR1B_ADDR, 6)) {
                    //Rising Edge detect
                    if (!oldICP1 && newICP1) {
                        UCModule.interruptionModule.timer1InputCapture();
                        dataMemory.writeIOByte(DataMemory_ATmega328P.ICR1H_ADDR, (byte) (0x00FF & (progress >> 8)));
                        dataMemory.writeIOByte(DataMemory_ATmega328P.ICR1L_ADDR, (byte) (0x00FF & progress));
                    }
                } else {
                    //Falling Edge detect
                    if (oldICP1 && !newICP1) {
                        UCModule.interruptionModule.timer1InputCapture();
                        dataMemory.writeIOByte(DataMemory_ATmega328P.ICR1H_ADDR, (byte) (0x00FF & (progress >> 8)));
                        dataMemory.writeIOByte(DataMemory_ATmega328P.ICR1L_ADDR, (byte) (0x00FF & progress));
                    }
                }
                oldICP1 = newICP1;

                dataMemory.writeByte(DataMemory_ATmega328P.TCNT1H_ADDR, (byte) (0x00FF & (progress >> 8)));
                dataMemory.writeByte(DataMemory_ATmega328P.TCNT1L_ADDR, (byte) (0x00FF & progress));
            }
        },
        CTC_OPERATION_TOP_ICR1 {
            @Override
            public void count() {
                enableICRWrite = true;
                boolean match_A = false, match_B = false;
                char progress = (char) (0x00FF & dataMemory.readByte(DataMemory_ATmega328P.TCNT1L_ADDR));
                progress = (char) ((dataMemory.readByte(DataMemory_ATmega328P.TCNT1H_ADDR) << 8) | progress);
                progress += 1;

                if (nextClear) {
                    nextClear = false;
                    progress = BOTTOM;
                }

                char icr1 = (char) (0x00FF & dataMemory.readByte(DataMemory_ATmega328P.ICR1L_ADDR));
                icr1 = (char) ((dataMemory.readByte(DataMemory_ATmega328P.ICR1H_ADDR) << 8) | icr1);
                if (progress == icr1) {
                    UCModule.interruptionModule.timer1InputCapture();
                    nextClear = true;
                }

                if (progress == BOTTOM && nextOverflow) {
                    nextOverflow = false;
                    UCModule.interruptionModule.timer1Overflow();
                } else if (progress == MAX) {
                    nextOverflow = true;
                }

                if (dataMemory.readForceMatchA_timer1()) {
                    match_A = true; //FORCE MATCH
                } else {
                    char ocr1a = (char) (0x00FF & dataMemory.readByte(DataMemory_ATmega328P.OCR1AL_ADDR));
                    ocr1a = (char) ((dataMemory.readByte(DataMemory_ATmega328P.OCR1AH_ADDR) << 8) | ocr1a);

                    if (progress == ocr1a) {
                        UCModule.interruptionModule.timer1MatchA();
                        match_A = true;
                    }
                }

                if (dataMemory.readForceMatchB_timer1()) {
                    match_B = true; //FORCE MATCH
                } else {
                    char ocr1b = (char) (0x00FF & dataMemory.readByte(DataMemory_ATmega328P.OCR1BL_ADDR));
                    ocr1b = (char) ((dataMemory.readByte(DataMemory_ATmega328P.OCR1BH_ADDR) << 8) | ocr1b);

                    if (progress == ocr1b) {
                        UCModule.interruptionModule.timer1MatchB();
                        match_B = true;
                    }
                }

                byte outputMode = dataMemory.readByte(DataMemory_ATmega328P.TCCR1A_ADDR);

                //CHANEL A
                switch (0xC0 & outputMode) {
                    case 0x00:
                        //OC1A disconected
                        timerOutputControl_OC1A = false;
                        break;
                    case 0x40:
                        //OC1A Toggle on Compare Match
                        timerOutputControl_OC1A = true;
                        if (match_A) {
                            stateOC1A = (stateOC1A + 1) % 2;
                            ioModule.setOC1A(stateOC1A);
                        }
                        break;
                    case 0x80:
                        //OC1A Clear on Compare Match
                        timerOutputControl_OC1A = true;
                        if (match_A) {
                            stateOC1A = IOModule.LOW_LEVEL;
                            ioModule.setOC1A(stateOC1A);
                        }
                        break;
                    case 0xC0:
                        //OC1A Set on Compare Match
                        timerOutputControl_OC1A = true;
                        if (match_A) {
                            stateOC1A = IOModule.HIGH_LEVEL;
                            ioModule.setOC1A(stateOC1A);
                        }
                }

                //CHANEL B
                switch (0x30 & outputMode) {
                    case 0x00:
                        //OC1B disconected
                        timerOutputControl_OC1B = false;
                        break;
                    case 0x10:
                        //OC0B Toggle on Compare Match
                        timerOutputControl_OC1B = true;
                        if (match_B) {
                            stateOC1B = (stateOC1B + 1) % 2;
                            ioModule.setOC1B(stateOC1B);
                        }
                        break;
                    case 0x20:
                        //OC0B Clear on Compare Match
                        timerOutputControl_OC1B = true;
                        if (match_B) {
                            stateOC1B = IOModule.LOW_LEVEL;
                            ioModule.setOC1B(stateOC1B);
                        }
                        break;
                    case 0x30:
                        //OC0B Set on Compare Match
                        timerOutputControl_OC1B = true;
                        if (match_B) {
                            stateOC1B = IOModule.HIGH_LEVEL;
                            ioModule.setOC1B(stateOC1B);
                        }
                }

                dataMemory.writeByte(DataMemory_ATmega328P.TCNT1H_ADDR, (byte) (0x00FF & (progress >> 8)));
                dataMemory.writeByte(DataMemory_ATmega328P.TCNT1L_ADDR, (byte) (0x00FF & progress));
            }
        },
        FAST_PWM_8B {
            @Override
            public void count() {
                enableICRWrite = false;
                if (!dataMemory.isOCR1AReady()){
                    return;
                }

                boolean match_A = false, match_B = false;
                char progress = (char) (0x00FF & dataMemory.readByte(DataMemory_ATmega328P.TCNT1L_ADDR));
                progress = (char) ((dataMemory.readByte(DataMemory_ATmega328P.TCNT1H_ADDR) << 8) | progress);

                if (progress == BOTTOM) {
                    doubleBufferOCR1A = (char) (0x00FF & dataMemory.readByte(DataMemory_ATmega328P.OCR1AL_ADDR));
                    doubleBufferOCR1A = (char) ((dataMemory.readByte(DataMemory_ATmega328P.OCR1AH_ADDR) << 8) | doubleBufferOCR1A);

                    doubleBufferOCR1B = (char) (0x00FF & dataMemory.readByte(DataMemory_ATmega328P.OCR1BL_ADDR));
                    doubleBufferOCR1B = (char) ((dataMemory.readByte(DataMemory_ATmega328P.OCR1BH_ADDR) << 8) | doubleBufferOCR1B);
                }

                progress += 1;

                if (nextClear) {
                    nextClear = false;
                    progress = BOTTOM;
                    UCModule.interruptionModule.timer1Overflow();
                }

                if (progress == MAX_8B) {
                    nextClear = true;
                }

                if (progress == doubleBufferOCR1A) {
                    UCModule.interruptionModule.timer1MatchA();
                    match_A = true;
                }

                if (progress == doubleBufferOCR1B) {
                    UCModule.interruptionModule.timer1MatchB();
                    match_B = true;
                }


                byte outputMode = dataMemory.readByte(DataMemory_ATmega328P.TCCR1A_ADDR);

                //CHANEL A
                switch (0xC0 & outputMode) {
                    case 0x00:
                    case 0x40:
                        //OC1A disconected
                        timerOutputControl_OC1A = false;
                        break;
                    case 0x80:
                        //OC1A Clear on Compare Match, set at BOTTOM
                        timerOutputControl_OC1A = true;
                        if (doubleBufferOCR1A == MAX_8B) {
                            stateOC1A = IOModule.HIGH_LEVEL;
                            ioModule.setOC1A(stateOC1A);
                        } else {
                            if (match_A) {
                                stateOC1A = IOModule.LOW_LEVEL;
                                ioModule.setOC1A(stateOC1A);
                            }
                            if (progress == BOTTOM) {
                                stateOC1A = IOModule.HIGH_LEVEL;
                                ioModule.setOC1A(stateOC1A);
                            }
                        }
                        break;
                    case 0xC0:
                        //OC1A Set on Compare Match, clear at BOTTOM
                        timerOutputControl_OC1A = true;
                        if (doubleBufferOCR1A == MAX_8B) {
                            stateOC1A = IOModule.LOW_LEVEL;
                            ioModule.setOC1A(stateOC1A);
                        } else {
                            if (match_A) {
                                stateOC1A = IOModule.HIGH_LEVEL;
                                ioModule.setOC1A(stateOC1A);
                            }

                            if (progress == BOTTOM) {
                                stateOC1A = IOModule.LOW_LEVEL;
                                ioModule.setOC1A(stateOC1A);
                            }
                        }

                }

                //CHANEL B
                switch (0x30 & outputMode) {
                    case 0x00:
                    case 0x10:
                        //OC1B disconected
                        timerOutputControl_OC1B = false;
                        break;
                    case 0x20:
                        //OC1B Clear on Compare Match, set at BOTTOM
                        timerOutputControl_OC1B = true;
                        if (doubleBufferOCR1B == MAX_8B) {
                            stateOC1B = IOModule.HIGH_LEVEL;
                            ioModule.setOC1B(stateOC1B);
                        } else {
                            if (match_B) {
                                stateOC1B = IOModule.LOW_LEVEL;
                                ioModule.setOC1B(stateOC1B);
                            }
                            if (progress == BOTTOM) {
                                stateOC1B = IOModule.HIGH_LEVEL;
                                ioModule.setOC1B(stateOC1B);
                            }
                        }
                        break;
                    case 0x30:
                        //OC1B Set on Compare Match, clear at BOTTOM
                        timerOutputControl_OC1B = true;
                        if (doubleBufferOCR1B == MAX_8B) {
                            stateOC1B = IOModule.LOW_LEVEL;
                            ioModule.setOC1B(stateOC1B);
                        } else {
                            if (match_B) {
                                stateOC1B = IOModule.HIGH_LEVEL;
                                ioModule.setOC1B(stateOC1B);
                            }
                            if (progress == BOTTOM) {
                                stateOC1B = IOModule.LOW_LEVEL;
                                ioModule.setOC1B(stateOC1B);
                            }
                        }
                }

                //Input Capture Unit
                newICP1 = dataMemory.readBit(DataMemory_ATmega328P.PINB_ADDR, 0);
                if (dataMemory.readBit(DataMemory_ATmega328P.TCCR1B_ADDR, 6)) {
                    //Rising Edge detect
                    if (!oldICP1 && newICP1) {
                        UCModule.interruptionModule.timer1InputCapture();
                        dataMemory.writeIOByte(DataMemory_ATmega328P.ICR1H_ADDR, (byte) (0x00FF & (progress >> 8)));
                        dataMemory.writeIOByte(DataMemory_ATmega328P.ICR1L_ADDR, (byte) (0x00FF & progress));
                    }
                } else {
                    //Falling Edge detect
                    if (oldICP1 && !newICP1) {
                        UCModule.interruptionModule.timer1InputCapture();
                        dataMemory.writeIOByte(DataMemory_ATmega328P.ICR1H_ADDR, (byte) (0x00FF & (progress >> 8)));
                        dataMemory.writeIOByte(DataMemory_ATmega328P.ICR1L_ADDR, (byte) (0x00FF & progress));
                    }
                }
                oldICP1 = newICP1;

                dataMemory.writeByte(DataMemory_ATmega328P.TCNT1H_ADDR, (byte) (0x00FF & (progress >> 8)));
                dataMemory.writeByte(DataMemory_ATmega328P.TCNT1L_ADDR, (byte) (0x00FF & progress));
            }
        },
        FAST_PWM_9B {
            @Override
            public void count() {
                enableICRWrite = false;
                if (!dataMemory.isOCR1AReady()){
                    return;
                }

                boolean match_A = false, match_B = false;
                char progress = (char) (0x00FF & dataMemory.readByte(DataMemory_ATmega328P.TCNT1L_ADDR));
                progress = (char) ((dataMemory.readByte(DataMemory_ATmega328P.TCNT1H_ADDR) << 8) | progress);

                if (progress == BOTTOM) {
                    doubleBufferOCR1A = (char) (0x00FF & dataMemory.readByte(DataMemory_ATmega328P.OCR1AL_ADDR));
                    doubleBufferOCR1A = (char) ((dataMemory.readByte(DataMemory_ATmega328P.OCR1AH_ADDR) << 8) | doubleBufferOCR1A);

                    doubleBufferOCR1B = (char) (0x00FF & dataMemory.readByte(DataMemory_ATmega328P.OCR1BL_ADDR));
                    doubleBufferOCR1B = (char) ((dataMemory.readByte(DataMemory_ATmega328P.OCR1BH_ADDR) << 8) | doubleBufferOCR1B);
                }

                progress += 1;

                if (nextClear) {
                    nextClear = false;
                    progress = BOTTOM;
                    UCModule.interruptionModule.timer1Overflow();
                }

                if (progress == MAX_9B) {
                    nextClear = true;
                }

                if (progress == doubleBufferOCR1A) {
                    UCModule.interruptionModule.timer1MatchA();
                    match_A = true;
                }

                if (progress == doubleBufferOCR1B) {
                    UCModule.interruptionModule.timer1MatchB();
                    match_B = true;
                }


                byte outputMode = dataMemory.readByte(DataMemory_ATmega328P.TCCR1A_ADDR);

                //CHANEL A
                switch (0xC0 & outputMode) {
                    case 0x00:
                    case 0x40:
                        //OC1A disconected
                        timerOutputControl_OC1A = false;
                        break;
                    case 0x80:
                        //OC1A Clear on Compare Match, set at BOTTOM
                        timerOutputControl_OC1A = true;
                        if (doubleBufferOCR1A == MAX_9B) {
                            stateOC1A = IOModule.HIGH_LEVEL;
                            ioModule.setOC1A(stateOC1A);
                        } else {
                            if (match_A) {
                                stateOC1A = IOModule.LOW_LEVEL;
                                ioModule.setOC1A(stateOC1A);
                            }
                            if (progress == BOTTOM) {
                                stateOC1A = IOModule.HIGH_LEVEL;
                                ioModule.setOC1A(stateOC1A);
                            }
                        }
                        break;
                    case 0xC0:
                        //OC1A Set on Compare Match, clear at BOTTOM
                        timerOutputControl_OC1A = true;
                        if (doubleBufferOCR1A == MAX_9B) {
                            stateOC1A = IOModule.LOW_LEVEL;
                            ioModule.setOC1A(stateOC1A);
                        } else {
                            if (match_A) {
                                stateOC1A = IOModule.HIGH_LEVEL;
                                ioModule.setOC1A(stateOC1A);
                            }

                            if (progress == BOTTOM) {
                                stateOC1A = IOModule.LOW_LEVEL;
                                ioModule.setOC1A(stateOC1A);
                            }
                        }

                }

                //CHANEL B
                switch (0x30 & outputMode) {
                    case 0x00:
                    case 0x10:
                        //OC1B disconected
                        timerOutputControl_OC1B = false;
                        break;
                    case 0x20:
                        //OC1B Clear on Compare Match, set at BOTTOM
                        timerOutputControl_OC1B = true;
                        if (doubleBufferOCR1B == MAX_9B) {
                            stateOC1B = IOModule.HIGH_LEVEL;
                            ioModule.setOC1B(stateOC1B);
                        } else {
                            if (match_B) {
                                stateOC1B = IOModule.LOW_LEVEL;
                                ioModule.setOC1B(stateOC1B);
                            }
                            if (progress == BOTTOM) {
                                stateOC1B = IOModule.HIGH_LEVEL;
                                ioModule.setOC1B(stateOC1B);
                            }
                        }
                        break;
                    case 0x30:
                        //OC1B Set on Compare Match, clear at BOTTOM
                        timerOutputControl_OC1B = true;
                        if (doubleBufferOCR1B == MAX_9B) {
                            stateOC1B = IOModule.LOW_LEVEL;
                            ioModule.setOC1B(stateOC1B);
                        } else {
                            if (match_B) {
                                stateOC1B = IOModule.HIGH_LEVEL;
                                ioModule.setOC1B(stateOC1B);
                            }
                            if (progress == BOTTOM) {
                                stateOC1B = IOModule.LOW_LEVEL;
                                ioModule.setOC1B(stateOC1B);
                            }
                        }
                }

                //Input Capture Unit
                newICP1 = dataMemory.readBit(DataMemory_ATmega328P.PINB_ADDR, 0);
                if (dataMemory.readBit(DataMemory_ATmega328P.TCCR1B_ADDR, 6)) {
                    //Rising Edge detect
                    if (!oldICP1 && newICP1) {
                        UCModule.interruptionModule.timer1InputCapture();
                        dataMemory.writeIOByte(DataMemory_ATmega328P.ICR1H_ADDR, (byte) (0x00FF & (progress >> 8)));
                        dataMemory.writeIOByte(DataMemory_ATmega328P.ICR1L_ADDR, (byte) (0x00FF & progress));
                    }
                } else {
                    //Falling Edge detect
                    if (oldICP1 && !newICP1) {
                        UCModule.interruptionModule.timer1InputCapture();
                        dataMemory.writeIOByte(DataMemory_ATmega328P.ICR1H_ADDR, (byte) (0x00FF & (progress >> 8)));
                        dataMemory.writeIOByte(DataMemory_ATmega328P.ICR1L_ADDR, (byte) (0x00FF & progress));
                    }
                }
                oldICP1 = newICP1;

                dataMemory.writeByte(DataMemory_ATmega328P.TCNT1H_ADDR, (byte) (0x00FF & (progress >> 8)));
                dataMemory.writeByte(DataMemory_ATmega328P.TCNT1L_ADDR, (byte) (0x00FF & progress));
            }
        },
        FAST_PWM_10B {
            @Override
            public void count() {
                enableICRWrite = false;
                if (!dataMemory.isOCR1AReady()){
                    return;
                }

                boolean match_A = false, match_B = false;
                char progress = (char) (0x00FF & dataMemory.readByte(DataMemory_ATmega328P.TCNT1L_ADDR));
                progress = (char) ((dataMemory.readByte(DataMemory_ATmega328P.TCNT1H_ADDR) << 8) | progress);

                if (progress == BOTTOM) {
                    doubleBufferOCR1A = (char) (0x00FF & dataMemory.readByte(DataMemory_ATmega328P.OCR1AL_ADDR));
                    doubleBufferOCR1A = (char) ((dataMemory.readByte(DataMemory_ATmega328P.OCR1AH_ADDR) << 8) | doubleBufferOCR1A);

                    doubleBufferOCR1B = (char) (0x00FF & dataMemory.readByte(DataMemory_ATmega328P.OCR1BL_ADDR));
                    doubleBufferOCR1B = (char) ((dataMemory.readByte(DataMemory_ATmega328P.OCR1BH_ADDR) << 8) | doubleBufferOCR1B);
                }

                progress += 1;

                if (nextClear) {
                    nextClear = false;
                    progress = BOTTOM;
                    UCModule.interruptionModule.timer1Overflow();
                }

                if (progress == MAX_10B) {
                    nextClear = true;
                }

                if (progress == doubleBufferOCR1A) {
                    UCModule.interruptionModule.timer1MatchA();
                    match_A = true;
                }

                if (progress == doubleBufferOCR1B) {
                    UCModule.interruptionModule.timer1MatchB();
                    match_B = true;
                }


                byte outputMode = dataMemory.readByte(DataMemory_ATmega328P.TCCR1A_ADDR);

                //CHANEL A
                switch (0xC0 & outputMode) {
                    case 0x00:
                    case 0x40:
                        //OC1A disconected
                        timerOutputControl_OC1A = false;
                        break;
                    case 0x80:
                        //OC1A Clear on Compare Match, set at BOTTOM
                        timerOutputControl_OC1A = true;
                        if (doubleBufferOCR1A == MAX_10B) {
                            stateOC1A = IOModule.HIGH_LEVEL;
                            ioModule.setOC1A(stateOC1A);
                        } else {
                            if (match_A) {
                                stateOC1A = IOModule.LOW_LEVEL;
                                ioModule.setOC1A(stateOC1A);
                            }
                            if (progress == BOTTOM) {
                                stateOC1A = IOModule.HIGH_LEVEL;
                                ioModule.setOC1A(stateOC1A);
                            }
                        }
                        break;
                    case 0xC0:
                        //OC1A Set on Compare Match, clear at BOTTOM
                        timerOutputControl_OC1A = true;
                        if (doubleBufferOCR1A == MAX_10B) {
                            stateOC1A = IOModule.LOW_LEVEL;
                            ioModule.setOC1A(stateOC1A);
                        } else {
                            if (match_A) {
                                stateOC1A = IOModule.HIGH_LEVEL;
                                ioModule.setOC1A(stateOC1A);
                            }

                            if (progress == BOTTOM) {
                                stateOC1A = IOModule.LOW_LEVEL;
                                ioModule.setOC1A(stateOC1A);
                            }
                        }

                }

                //CHANEL B
                switch (0x30 & outputMode) {
                    case 0x00:
                    case 0x10:
                        //OC1B disconected
                        timerOutputControl_OC1B = false;
                        break;
                    case 0x20:
                        //OC1B Clear on Compare Match, set at BOTTOM
                        timerOutputControl_OC1B = true;
                        if (doubleBufferOCR1B == MAX_10B) {
                            stateOC1B = IOModule.HIGH_LEVEL;
                            ioModule.setOC1B(stateOC1B);
                        } else {
                            if (match_B) {
                                stateOC1B = IOModule.LOW_LEVEL;
                                ioModule.setOC1B(stateOC1B);
                            }
                            if (progress == BOTTOM) {
                                stateOC1B = IOModule.HIGH_LEVEL;
                                ioModule.setOC1B(stateOC1B);
                            }
                        }
                        break;
                    case 0x30:
                        //OC1B Set on Compare Match, clear at BOTTOM
                        timerOutputControl_OC1B = true;
                        if (doubleBufferOCR1B == MAX_10B) {
                            stateOC1B = IOModule.LOW_LEVEL;
                            ioModule.setOC1B(stateOC1B);
                        } else {
                            if (match_B) {
                                stateOC1B = IOModule.HIGH_LEVEL;
                                ioModule.setOC1B(stateOC1B);
                            }
                            if (progress == BOTTOM) {
                                stateOC1B = IOModule.LOW_LEVEL;
                                ioModule.setOC1B(stateOC1B);
                            }
                        }
                }

                //Input Capture Unit
                newICP1 = dataMemory.readBit(DataMemory_ATmega328P.PINB_ADDR, 0);
                if (dataMemory.readBit(DataMemory_ATmega328P.TCCR1B_ADDR, 6)) {
                    //Rising Edge detect
                    if (!oldICP1 && newICP1) {
                        UCModule.interruptionModule.timer1InputCapture();
                        dataMemory.writeIOByte(DataMemory_ATmega328P.ICR1H_ADDR, (byte) (0x00FF & (progress >> 8)));
                        dataMemory.writeIOByte(DataMemory_ATmega328P.ICR1L_ADDR, (byte) (0x00FF & progress));
                    }
                } else {
                    //Falling Edge detect
                    if (oldICP1 && !newICP1) {
                        UCModule.interruptionModule.timer1InputCapture();
                        dataMemory.writeIOByte(DataMemory_ATmega328P.ICR1H_ADDR, (byte) (0x00FF & (progress >> 8)));
                        dataMemory.writeIOByte(DataMemory_ATmega328P.ICR1L_ADDR, (byte) (0x00FF & progress));
                    }
                }
                oldICP1 = newICP1;

                dataMemory.writeByte(DataMemory_ATmega328P.TCNT1H_ADDR, (byte) (0x00FF & (progress >> 8)));
                dataMemory.writeByte(DataMemory_ATmega328P.TCNT1L_ADDR, (byte) (0x00FF & progress));
            }
        },
        FAST_PWM_TOP_ICR1 {
            @Override
            public void count() {
                enableICRWrite = true;
                if (!dataMemory.isOCR1AReady()){
                    return;
                }

                boolean match_A = false, match_B = false;
                char progress = (char) (0x00FF & dataMemory.readByte(DataMemory_ATmega328P.TCNT1L_ADDR));
                progress = (char) ((dataMemory.readByte(DataMemory_ATmega328P.TCNT1H_ADDR) << 8) | progress);

                if (progress == BOTTOM) {
                    doubleBufferOCR1A = (char) (0x00FF & dataMemory.readByte(DataMemory_ATmega328P.OCR1AL_ADDR));
                    doubleBufferOCR1A = (char) ((dataMemory.readByte(DataMemory_ATmega328P.OCR1AH_ADDR) << 8) | doubleBufferOCR1A);

                    doubleBufferOCR1B = (char) (0x00FF & dataMemory.readByte(DataMemory_ATmega328P.OCR1BL_ADDR));
                    doubleBufferOCR1B = (char) ((dataMemory.readByte(DataMemory_ATmega328P.OCR1BH_ADDR) << 8) | doubleBufferOCR1B);
                }

                progress += 1;

                if (nextClear) {
                    nextClear = false;
                    progress = BOTTOM;
                    UCModule.interruptionModule.timer1Overflow();
                }

                char icr1 = (char) (0x00FF & dataMemory.readByte(DataMemory_ATmega328P.ICR1L_ADDR));
                Log.i("Timer1A", "ICRL: " + Integer.toHexString(icr1));
                icr1 = (char) ((dataMemory.readByte(DataMemory_ATmega328P.ICR1H_ADDR) << 8) | icr1);
                Log.i("Timer1A", "ICRH: " + Integer.toHexString(icr1));

                Log.i("Timer1A", "ICR: " + Integer.toHexString(icr1));
                if (progress == icr1) {
                    nextClear = true;
                }

                if (progress == doubleBufferOCR1A) {
                    UCModule.interruptionModule.timer1MatchA();
                    match_A = true;
                }

                if (progress == doubleBufferOCR1B) {
                    UCModule.interruptionModule.timer1MatchB();
                    match_B = true;
                }


                byte outputMode = dataMemory.readByte(DataMemory_ATmega328P.TCCR1A_ADDR);

                //CHANEL A
                switch (0xC0 & outputMode) {
                    case 0x00:
                        //OC1A disconected
                        timerOutputControl_OC1A = false;
                        break;
                    case 0x40:
                        timerOutputControl_OC1A = true;
                        if (match_A) {
                            stateOC1A = (stateOC1A + 1) % 2;  //Toggle
                            ioModule.setOC1A(stateOC1A);
                        }
                        break;
                    case 0x80:
                        //OC1A Clear on Compare Match, set at BOTTOM
                        timerOutputControl_OC1A = true;
                        if (doubleBufferOCR1A == icr1) {
                            stateOC1A = IOModule.HIGH_LEVEL;
                            ioModule.setOC1A(stateOC1A);
                        } else {
                            if (match_A) {
                                stateOC1A = IOModule.LOW_LEVEL;
                                ioModule.setOC1A(stateOC1A);
                            }
                            if (progress == BOTTOM) {
                                stateOC1A = IOModule.HIGH_LEVEL;
                                ioModule.setOC1A(stateOC1A);
                            }
                        }
                        break;
                    case 0xC0:
                        //OC1A Set on Compare Match, clear at BOTTOM
                        timerOutputControl_OC1A = true;
                        if (doubleBufferOCR1A == icr1) {
                            stateOC1A = IOModule.LOW_LEVEL;
                            ioModule.setOC1A(stateOC1A);
                        } else {
                            if (match_A) {
                                stateOC1A = IOModule.HIGH_LEVEL;
                                ioModule.setOC1A(stateOC1A);
                            }

                            if (progress == BOTTOM) {
                                stateOC1A = IOModule.LOW_LEVEL;
                                ioModule.setOC1A(stateOC1A);
                            }
                        }

                }

                //CHANEL B
                switch (0x30 & outputMode) {
                    case 0x00:
                    case 0x10:
                        //OC1B disconected
                        timerOutputControl_OC1B = false;
                        break;
                    case 0x20:
                        //OC1B Clear on Compare Match, set at BOTTOM
                        timerOutputControl_OC1B = true;
                        if (doubleBufferOCR1B == icr1) {
                            stateOC1B = IOModule.HIGH_LEVEL;
                            ioModule.setOC1B(stateOC1B);
                        } else {
                            if (match_B) {
                                stateOC1B = IOModule.LOW_LEVEL;
                                ioModule.setOC1B(stateOC1B);
                            }
                            if (progress == BOTTOM) {
                                stateOC1B = IOModule.HIGH_LEVEL;
                                ioModule.setOC1B(stateOC1B);
                            }
                        }
                        break;
                    case 0x30:
                        //OC1B Set on Compare Match, clear at BOTTOM
                        timerOutputControl_OC1B = true;
                        if (doubleBufferOCR1B == icr1) {
                            stateOC1B = IOModule.LOW_LEVEL;
                            ioModule.setOC1B(stateOC1B);
                        } else {
                            if (match_B) {
                                stateOC1B = IOModule.HIGH_LEVEL;
                                ioModule.setOC1B(stateOC1B);
                            }
                            if (progress == BOTTOM) {
                                stateOC1B = IOModule.LOW_LEVEL;
                                ioModule.setOC1B(stateOC1B);
                            }
                        }
                }

                dataMemory.writeByte(DataMemory_ATmega328P.TCNT1H_ADDR, (byte) (0x00FF & (progress >> 8)));
                dataMemory.writeByte(DataMemory_ATmega328P.TCNT1L_ADDR, (byte) (0x00FF & progress));
            }
        },
        FAST_PWM_TOP_OCRA {
            @Override
            public void count() {
                enableICRWrite = false;
                if (!dataMemory.isOCR1AReady()){
                    return;
                }

                boolean match_A = false, match_B = false;
                char progress = (char) (0x00FF & dataMemory.readByte(DataMemory_ATmega328P.TCNT1L_ADDR));
                progress = (char) ((dataMemory.readByte(DataMemory_ATmega328P.TCNT1H_ADDR) << 8) | progress);

                if (progress == BOTTOM) {
                    doubleBufferOCR1A = (char) (0x00FF & dataMemory.readByte(DataMemory_ATmega328P.OCR1AL_ADDR));
                    doubleBufferOCR1A = (char) ((dataMemory.readByte(DataMemory_ATmega328P.OCR1AH_ADDR) << 8) | doubleBufferOCR1A);

                    doubleBufferOCR1B = (char) (0x00FF & dataMemory.readByte(DataMemory_ATmega328P.OCR1BL_ADDR));
                    doubleBufferOCR1B = (char) ((dataMemory.readByte(DataMemory_ATmega328P.OCR1BH_ADDR) << 8) | doubleBufferOCR1B);
                }

                progress += 1;

                if (nextClear) {
                    nextClear = false;
                    progress = BOTTOM;
                    UCModule.interruptionModule.timer1Overflow();
                }

                if (progress == doubleBufferOCR1A) {
                    UCModule.interruptionModule.timer1MatchA();
                    match_A = true;
                    nextClear = true;
                }

                if (progress == doubleBufferOCR1B) {
                    UCModule.interruptionModule.timer1MatchB();
                    match_B = true;
                }


                byte outputMode = dataMemory.readByte(DataMemory_ATmega328P.TCCR1A_ADDR);

                //CHANEL A
                switch (0xC0 & outputMode) {
                    case 0x00:
                        //OC1A disconected
                        timerOutputControl_OC1A = false;
                        break;
                    case 0x40:
                        timerOutputControl_OC1A = true;
                        if (match_A) {
                            stateOC1A = (stateOC1A + 1) % 2;  //Toggle
                            ioModule.setOC1A(stateOC1A);
                        }
                        break;
                    case 0x80:
                        //OC1A Clear on Compare Match, set at BOTTOM
                        timerOutputControl_OC1A = true;
                        if (doubleBufferOCR1A == MAX) {
                            stateOC1A = IOModule.HIGH_LEVEL;
                            ioModule.setOC1A(stateOC1A);
                        } else {
                            if (match_A) {
                                stateOC1A = IOModule.LOW_LEVEL;
                                ioModule.setOC1A(stateOC1A);
                            }
                            if (progress == BOTTOM) {
                                stateOC1A = IOModule.HIGH_LEVEL;
                                ioModule.setOC1A(stateOC1A);
                            }
                        }
                        break;
                    case 0xC0:
                        //OC1A Set on Compare Match, clear at BOTTOM
                        timerOutputControl_OC1A = true;
                        if (doubleBufferOCR1A == MAX) {
                            stateOC1A = IOModule.LOW_LEVEL;
                            ioModule.setOC1A(stateOC1A);
                        } else {
                            if (match_A) {
                                stateOC1A = IOModule.HIGH_LEVEL;
                                ioModule.setOC1A(stateOC1A);
                            }

                            if (progress == BOTTOM) {
                                stateOC1A = IOModule.LOW_LEVEL;
                                ioModule.setOC1A(stateOC1A);
                            }
                        }

                }

                //CHANEL B
                switch (0x30 & outputMode) {
                    case 0x00:
                    case 0x10:
                        //OC1B disconected
                        timerOutputControl_OC1B = false;
                        break;
                    case 0x20:
                        //OC1B Clear on Compare Match, set at BOTTOM
                        timerOutputControl_OC1B = true;
                        if (doubleBufferOCR1B == doubleBufferOCR1A) {
                            stateOC1B = IOModule.HIGH_LEVEL;
                            ioModule.setOC1B(stateOC1B);
                        } else {
                            if (match_B) {
                                stateOC1B = IOModule.LOW_LEVEL;
                                ioModule.setOC1B(stateOC1B);
                            }
                            if (progress == BOTTOM) {
                                stateOC1B = IOModule.HIGH_LEVEL;
                                ioModule.setOC1B(stateOC1B);
                            }
                        }
                        break;
                    case 0x30:
                        //OC1B Set on Compare Match, clear at BOTTOM
                        timerOutputControl_OC1B = true;
                        if (doubleBufferOCR1B == doubleBufferOCR1A) {
                            stateOC1B = IOModule.LOW_LEVEL;
                            ioModule.setOC1B(stateOC1B);
                        } else {
                            if (match_B) {
                                stateOC1B = IOModule.HIGH_LEVEL;
                                ioModule.setOC1B(stateOC1B);
                            }
                            if (progress == BOTTOM) {
                                stateOC1B = IOModule.LOW_LEVEL;
                                ioModule.setOC1B(stateOC1B);
                            }
                        }
                }

                //Input Capture Unit
                newICP1 = dataMemory.readBit(DataMemory_ATmega328P.PINB_ADDR, 0);
                if (dataMemory.readBit(DataMemory_ATmega328P.TCCR1B_ADDR, 6)) {
                    //Rising Edge detect
                    if (!oldICP1 && newICP1) {
                        UCModule.interruptionModule.timer1InputCapture();
                        dataMemory.writeIOByte(DataMemory_ATmega328P.ICR1H_ADDR, (byte) (0x00FF & (progress >> 8)));
                        dataMemory.writeIOByte(DataMemory_ATmega328P.ICR1L_ADDR, (byte) (0x00FF & progress));
                    }
                } else {
                    //Falling Edge detect
                    if (oldICP1 && !newICP1) {
                        UCModule.interruptionModule.timer1InputCapture();
                        dataMemory.writeIOByte(DataMemory_ATmega328P.ICR1H_ADDR, (byte) (0x00FF & (progress >> 8)));
                        dataMemory.writeIOByte(DataMemory_ATmega328P.ICR1L_ADDR, (byte) (0x00FF & progress));
                    }
                }
                oldICP1 = newICP1;

                dataMemory.writeByte(DataMemory_ATmega328P.TCNT1H_ADDR, (byte) (0x00FF & (progress >> 8)));
                dataMemory.writeByte(DataMemory_ATmega328P.TCNT1L_ADDR, (byte) (0x00FF & progress));
            }
        };

        public abstract void count();
    }
}
