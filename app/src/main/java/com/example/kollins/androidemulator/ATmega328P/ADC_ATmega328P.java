package com.example.kollins.androidemulator.ATmega328P;

import android.os.Handler;

import com.example.kollins.androidemulator.UCModule;
import com.example.kollins.androidemulator.uCInterfaces.ADCModule;
import com.example.kollins.androidemulator.uCInterfaces.DataMemory;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class ADC_ATmega328P implements ADCModule{

    private static Handler uCHandler;
    private static Lock clockLock;
    private static Condition adcClockCondition;
    private static DataMemory_ATmega328P dataMemory;


    public ADC_ATmega328P(DataMemory dataMemory, Handler uCHandler, Lock clockLock) {
        this.dataMemory = (DataMemory_ATmega328P) dataMemory;
        this.uCHandler = uCHandler;
        this.clockLock = clockLock;

        adcClockCondition = clockLock.newCondition();
    }

    @Override
    public void run() {
        Thread.currentThread().setName("ADC");

        while (true){
            waitClock();
        }

    }
    private static void waitClock() {

        clockLock.lock();
        try {
            UCModule.clockVector[UCModule.ADC_ID] = true;

            for (int i = 0; i < UCModule.clockVector.length; i++) {
                if (!UCModule.clockVector[i]) {
                    adcClockCondition.await();
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
    public void clockADC() {
        clockLock.lock();
        try {
            adcClockCondition.signal();
        } finally {
            clockLock.unlock();
        }
    }
}
