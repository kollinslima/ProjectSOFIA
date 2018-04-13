package com.example.kollins.androidemulator.ATmega328P;

import android.os.Handler;

import com.example.kollins.androidemulator.ATmega328P.IOModule_ATmega328P.IOModule_ATmega328P;
import com.example.kollins.androidemulator.ATmega328P.IOModule_ATmega328P.Output.OutputFragment_ATmega328P;
import com.example.kollins.androidemulator.UCModule;
import com.example.kollins.androidemulator.uCInterfaces.DataMemory;
import com.example.kollins.androidemulator.uCInterfaces.IOModule;
import com.example.kollins.androidemulator.uCInterfaces.Timer0Module;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class Timer0_ATmega328P implements Timer0Module {

    public static boolean timerOutputControl_OC0A;
    public static boolean timerOutputControl_OC0B;

    private static DataMemory_ATmega328P dataMemory;
    private static Handler uCHandler;
    private static IOModule_ATmega328P ioModule;
    private UCModule uCModule;

    private static Lock clockLock;
    private static Condition timer0ClockCondition;

    private static boolean oldExternalT0;
    private boolean buffer_WGM02;

    private static int stateOC0A;

    public Timer0_ATmega328P(DataMemory dataMemory, Handler uCHandler, Lock clockLock, UCModule uCModule, IOModule ioModule) {
        this.dataMemory = (DataMemory_ATmega328P) dataMemory;
        this.uCHandler = uCHandler;
        this.clockLock = clockLock;
        this.uCModule = uCModule;
        this.ioModule = (IOModule_ATmega328P) ioModule;

        timer0ClockCondition = clockLock.newCondition();
        oldExternalT0 = false;

        timerOutputControl_OC0A = false;
        timerOutputControl_OC0B = false;

        stateOC0A = IOModule.LOW_LEVEL;
    }

    @Override
    public void run() {
        while (!uCModule.getResetFlag()) {
            if (ClockSource.values()[0x07 & dataMemory.readByte(DataMemory_ATmega328P.TCCR0B_ADDR)].work()){

                buffer_WGM02 = dataMemory.readBit(DataMemory_ATmega328P.TCCR0B_ADDR,3);

                switch (0x03 & dataMemory.readByte(DataMemory_ATmega328P.TCCR0A_ADDR)){
                    case 0x00:
                        if (!buffer_WGM02){
                            TimerMode.NORMAL_OPERATION.count();
                        }
                        break;
                    case 0x01:
                        if (buffer_WGM02){
                            TimerMode.PWM_PHASE_CORRECT_TOP_OCRA.count();
                        } else {
                            TimerMode.PWM_PHASE_CORRECT_TOP_0XFF.count();
                        }
                        break;
                    case 0x02:
                        if (!buffer_WGM02){
                            TimerMode.CTC_OPERATION.count();
                        }
                        break;
                    case 0x03:
                        if (buffer_WGM02){
                            TimerMode.FAST_PWM_TOP_OCRA.count();
                        } else {
                            TimerMode.FAST_PWM_TOP_0XFF.count();
                        }
                        break;
                }
            }
        }
    }

    private static void waitClock() {

        clockLock.lock();
        try {
            UCModule.clockVector[UCModule.TIMER0_ID] = true;

            for (int i = 0; i < UCModule.clockVector.length; i++) {
                if (!UCModule.clockVector[i]) {
                    timer0ClockCondition.await();
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
    public void clockTimer0() {
        clockLock.lock();
        try {
            timer0ClockCondition.signal();
        } finally {
            clockLock.unlock();
        }
    }

    public enum ClockSource{
        NO_CLOCK_SOURCE{
            @Override
            public boolean work() {
                waitClock();
                return false;
            }
        },
        CLOCK_PRESCALER_1{
            @Override
            public boolean work() {
                waitClock();
                return true;
            }
        },
        CLOCK_PRESCALER_8{
            @Override
            public boolean work() {
                for (int i = 0; i < 8; i++){
                    waitClock();
                }
                return true;
            }
        },
        CLOCK_PRESCALER_64{
            @Override
            public boolean work() {
                for (int i = 0; i < 64; i++){
                    waitClock();
                }
                return true;
            }
        },
        CLOCK_PRESCALER_256{
            @Override
            public boolean work() {
                for (int i = 0; i < 256; i++){
                    waitClock();
                }
                return true;
            }
        },
        CLOCK_PRESCALER_1024{
            @Override
            public boolean work() {
                for (int i = 0; i < 1024; i++){
                    waitClock();
                }
                return true;
            }
        },
        EXTERNAL_CLOCK_T0_FALLING_EDGE{
            @Override
            public boolean work() {
                if (oldExternalT0 & !dataMemory.readBit(DataMemory_ATmega328P.PIND_ADDR, 4)){
                    return true;
                }
                return false;
            }
        },
        EXTERNAL_CLOCK_T0_RISING_EDGE{
            @Override
            public boolean work() {
                if (!oldExternalT0 & dataMemory.readBit(DataMemory_ATmega328P.PIND_ADDR, 4)){
                    return true;
                }
                return false;
            }
        };
        public abstract boolean work();
    }

    public enum TimerMode{
        NORMAL_OPERATION{
            @Override
            public void count() {
                byte progress = dataMemory.readByte(DataMemory_ATmega328P.TCNT0_ADDR);
                progress = (byte) (progress +  1);

                byte outputMode = dataMemory.readByte(DataMemory_ATmega328P.TCCR0A_ADDR);

                //CHANEL A
                switch (0xC0 & outputMode){
                    case 0x00:
                        //OC0A disconected
                        timerOutputControl_OC0A = false;
                        break;
                    case 0x40:
                        //OC0A Toggle on Compare Match
                        timerOutputControl_OC0A = true;
                        if (progress == dataMemory.readByte(DataMemory_ATmega328P.OCR0A_ADDR)){
                            //Match
                            stateOC0A = (stateOC0A + 1)%2;
                            ioModule.setOC0A(stateOC0A);
                        }
                        break;
                    case 0x80:
                        //OC0A Clear on Compare Match
                        timerOutputControl_OC0A = true;
                        if (progress == dataMemory.readByte(DataMemory_ATmega328P.OCR0A_ADDR)){
                            //Match
                            stateOC0A = IOModule.LOW_LEVEL;
                            ioModule.setOC0A(stateOC0A);
                        }
                        break;
                    case 0xC0:
                        //OC0A Set on Compare Match
                        timerOutputControl_OC0A = true;
                        if (progress == dataMemory.readByte(DataMemory_ATmega328P.OCR0A_ADDR)){
                            //Match
                            stateOC0A = IOModule.HIGH_LEVEL;
                            ioModule.setOC0A(stateOC0A);
                        }
                        break;
                }

                if (progress == 0){
                    UCModule.interruptionModule.timer0Overflow();
                }

                dataMemory.writeByte(DataMemory_ATmega328P.TCNT0_ADDR, progress);
            }
        },
        PWM_PHASE_CORRECT_TOP_0XFF{
            @Override
            public void count() {

            }
        },
        CTC_OPERATION{
            @Override
            public void count() {

            }
        },
        FAST_PWM_TOP_0XFF{
            @Override
            public void count() {

            }
        },
        PWM_PHASE_CORRECT_TOP_OCRA{
            @Override
            public void count() {

            }
        },
        FAST_PWM_TOP_OCRA{
            @Override
            public void count() {

            }
        };
        public abstract void count();
    }
}
