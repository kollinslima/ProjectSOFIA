package com.example.kollins.androidemulator.ATmega328P;

import android.os.Handler;
import android.util.Log;

import com.example.kollins.androidemulator.ATmega328P.IOModule_ATmega328P.IOModule_ATmega328P;
import com.example.kollins.androidemulator.UCModule;
import com.example.kollins.androidemulator.uCInterfaces.DataMemory;
import com.example.kollins.androidemulator.uCInterfaces.IOModule;
import com.example.kollins.androidemulator.uCInterfaces.Timer0Module;
import com.example.kollins.androidemulator.uCInterfaces.Timer1Module;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class Timer1_ATmega328P implements Timer1Module {

    private static final char MAX = 0xFFFF;
    private static final byte BOTTOM = 0x00;

    public static boolean timerOutputControl_OC1A;
    public static boolean timerOutputControl_OC1B;

    private static DataMemory_ATmega328P dataMemory;
    private static Handler uCHandler;
    private static IOModule_ATmega328P ioModule;
    private UCModule uCModule;

    private static Lock clockLock;
    private static Condition timer1ClockCondition;

    private static boolean oldExternalT1, newExternalT1;

    private static int stateOC1A, stateOC1B;
    private static boolean nextOverflow, nextClear, phaseCorrect_UPCount;
    private static byte doubleBufferOCR1A, doubleBufferOCR1B;

    public Timer1_ATmega328P(DataMemory dataMemory, Handler uCHandler, Lock clockLock, UCModule uCModule, IOModule ioModule) {
        this.dataMemory = (DataMemory_ATmega328P) dataMemory;
        this.uCHandler = uCHandler;
        this.clockLock = clockLock;
        this.uCModule = uCModule;
        this.ioModule = (IOModule_ATmega328P) ioModule;

        timer1ClockCondition = clockLock.newCondition();
        oldExternalT1 = dataMemory.readBit(DataMemory_ATmega328P.PIND_ADDR, 5);

        timerOutputControl_OC1A = false;
        timerOutputControl_OC1B = false;
        phaseCorrect_UPCount = true;

        stateOC1A = IOModule.TRI_STATE;
        stateOC1B = IOModule.TRI_STATE;

        nextOverflow = false;
        nextClear = false;
    }

    @Override
    public void run() {
        Thread.currentThread().setName("TIMER 1");
        while (!uCModule.getResetFlag()) {
            if (ClockSource.values()[0x07 & dataMemory.readByte(DataMemory_ATmega328P.TCCR1B_ADDR)].work()) {

                if (dataMemory.readBit(DataMemory_ATmega328P.GTCCR_ADDR, 0)) {
                    continue;   //Synchronization Mode
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

    public enum TimerMode{

    }
}
