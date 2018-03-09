package com.example.kollins.androidemulator.ATmega328P;

import android.util.Log;

import com.example.kollins.androidemulator.uCInterfaces.CPUModule;
import com.example.kollins.androidemulator.uCInterfaces.DataMemory;
import com.example.kollins.androidemulator.uCInterfaces.ProgramMemory;
import com.example.kollins.androidemulator.UCModule_ATmega328P;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by kollins on 3/9/18.
 */

public class CPUModule_ATmega328P implements CPUModule, Runnable {

    private char pcPointer;
    private char instruction;

    private Lock clockLock;
    private Condition clockCondition;

    private ProgramMemory programMemory;
    private DataMemory dataMemory;
    private UCModule_ATmega328P uCModule;

    public CPUModule_ATmega328P(ProgramMemory programMemory, DataMemory dataMemory, UCModule_ATmega328P uCModule) {
        this.programMemory = programMemory;
        this.dataMemory = dataMemory;
        this.uCModule = uCModule;

        pcPointer = 0;

        clockLock = new ReentrantLock();
        clockCondition = clockLock.newCondition();
    }

    @Override
    public void run() {
        while (!uCModule.getResetFlag()) {

            //Load instruction and add PC;
            try {
                //Fetch
                instruction = programMemory.loadInstruction(pcPointer++);

                //Decode and execute
                switch ((0xF000 & instruction)) {
                    case 0x2000:
                        if ((0x0C00 & instruction) == 0x0400){
//                            Log.d(UCModule_ATmega328P.MY_LOG_TAG, "instruction CLR");

                            //Implemented as XOR, but I'll write zero manually
                           dataMemory.writeByte(
                                   (char)((0x01F0 & instruction)>>4),
                                   (byte) 0x00
                           );
                        }
                        break;

                    case 0x9000:
                        if (((0x0E00 & instruction) == 0x0400)) {
                            if (((0x000E & instruction) == 0x000C)) {
//                            Log.d(UCModule_ATmega328P.MY_LOG_TAG, "instruction JUMP");
                                pcPointer = programMemory.loadInstruction(pcPointer);
                            }
                        }
                        break;

                    case 0xB000:
                        if ((0x0800 & instruction) == 0x0800) {
//                        Log.d(UCModule_ATmega328P.MY_LOG_TAG, "instruction OUT");
                            dataMemory.writeByte(
                                    (char) ((((0x0600 & instruction) >> 5) | ((0x000F & instruction))) + 0x20), //Address
                                    dataMemory.readByte((char) ((0x00F0 & instruction) >> 4))                   //Data
                            );
                        }
                        break;

                    case 0xE000:
//                        Log.d(UCModule_ATmega328P.MY_LOG_TAG, "instruction LDI");
                        dataMemory.writeByte(
                                (char) (0x10 | (0x00F0 & instruction) >> 4),                        //Address
                                (byte) (((0x0F00 & instruction) >> 4) | (0x000F & instruction))     //Data
                        );
                        break;

                    default:
                        //Casos extras
                        Log.d(UCModule_ATmega328P.MY_LOG_TAG, "Unknown instruction: " + Integer.toHexString((int) instruction));
                }

            } catch (ArrayIndexOutOfBoundsException e) {
                break;
            }
            waitClock();
        }
    }

    private void waitClock() {
        clockLock.lock();
        try {
            uCModule.setClockVector(CPU_ID);
            if (uCModule.getClockVector(CPU_ID)) {
                //I'm not the last module, so I'll wait
                clockCondition.await();
            } else {
                //I'm the last module, so I'll wake the others
                uCModule.wakeUpModules();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();

        } finally {
            clockLock.unlock();
        }
    }

    @Override
    public void clockCPU() {
        clockLock.lock();
        try {
            clockCondition.signal();
        } finally {
            clockLock.unlock();
        }

    }

}

