package com.example.kollins.androidemulator.ATmega328P;

import android.util.Log;

import com.example.kollins.androidemulator.UCModule;
import com.example.kollins.androidemulator.uCInterfaces.CPUModule;
import com.example.kollins.androidemulator.uCInterfaces.DataMemory;
import com.example.kollins.androidemulator.uCInterfaces.ProgramMemory;

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
    private byte clockCycles;

    private ProgramMemory programMemory;
    private DataMemory dataMemory;
    private UCModule uCModule;

    private int statusRegister;

    //Auxiliar for processing
    private int offset;
    private byte inDataL;
    private byte inDataH;

    public CPUModule_ATmega328P(ProgramMemory programMemory, DataMemory dataMemory, UCModule uCModule) {
        this.programMemory = programMemory;
        this.dataMemory = dataMemory;
        this.uCModule = uCModule;

        pcPointer = 0;
        statusRegister = dataMemory.readByte(0x5F);


        clockLock = new ReentrantLock();
        clockCondition = clockLock.newCondition();
    }

    @Override
    public void run() {

        while (!uCModule.getResetFlag()) {
            try {
                /******************************Fetch*****************************/

                //Load instruction and add PC;
                instruction = programMemory.loadInstruction(pcPointer++);

                /*************************Decode and execute*******************/
                switch ((0xF000 & instruction)) {
                    case 0x2000: {
                        if ((0x0C00 & instruction) == 0x0400) {
                            /*************************CLR***********************/
//                            Log.d(UCModule.MY_LOG_TAG, "instruction CLR");

                            //Implemented as XOR, but I'll write zero manually
                            dataMemory.writeByte(
                                    ((0x01F0 & instruction) >> 4),
                                    0x00
                            );

                            //Update Status Register
                            statusRegister = (statusRegister & 0x3E);
                            statusRegister = (statusRegister | 0x02);
                            dataMemory.writeByte(
                                    0x5F,
                                    statusRegister
                            );

                            clockCycles = 1;

                        }
                        break;
                    }

                    case 0x9000: {
                        if (((0x0E00 & instruction) == 0x0400)) {
                            if (((0x000E & instruction) == 0x000C)) {
                                /*************************JMP***********************/
//                            Log.d(UCModule.MY_LOG_TAG, "instruction JMP");
                                pcPointer = programMemory.loadInstruction(pcPointer);
                                clockCycles = 3;
                            }
                        } else if (((0x0F00 & instruction) == 0x0A00)) {
                            /*************************SBI***********************/
                            dataMemory.writeBit(((0x00F8 & instruction) >> 3) + 0x20, (0x0007 & instruction), true);
                            clockCycles = 2;
                            
                        } else if (((0x0F00 & instruction) == 0x0B00)) {
                            /*************************SBIS***********************/
//                            Log.d(UCModule.MY_LOG_TAG, "instruction SBIS");

                            if (dataMemory.readBit(((0x00F8 & instruction) >> 3) + 0x20, (0x0007 & instruction))) {

                                instruction = programMemory.loadInstruction(pcPointer++);
                                clockCycles = 2;

                                //Test 2 word instructions
                                //JMP
                                int testJMP_CALL = (0xFE0E & instruction);
                                int testLDS_STS = (0xFE0F & instruction);

                                if (testJMP_CALL == 0x940C ||       //JMP
                                        testJMP_CALL == 0x940E ||   //CALL
                                        testLDS_STS == 0x9000 ||    //LDS
                                        testLDS_STS == 0x9200)      //STS
                                {
                                    clockCycles = 3;
                                    ++pcPointer;
                                }
                            } else {
                                clockCycles = 1;
                            }


                        } else if ((0x0F00 & instruction) == 0x0600) {
                            /*************************ADDIW***********************/
//                            Log.d(UCModule.MY_LOG_TAG, "instruction ADDIW");

                            offset = ((0x0030 & instruction) * 2);
                            inDataL = dataMemory.readByte(0x18 + offset);
                            inDataH = dataMemory.readByte(0x19 + offset);

                            int outData = (((0x00FF & inDataH) << 8) | (0x00FF & inDataL)) +
                                    (((0x00C0 & instruction) >> 2) | (0x000F & instruction));

                            dataMemory.writeByte(0x18 + offset, (0x00FF & outData));
                            dataMemory.writeByte(0x19 + offset, (0xFF00 & outData) >> 8);

                            //Update status register
                            //flag C -> R15 ¯ • Rdh7
                            statusRegister = statusRegister & 0xFE;
                            statusRegister = statusRegister | (((inDataH & 0x80) & ((~(outData & 0x8000)) >> 8)) >> 7);

                            //flag Z
                            if ((outData & 0x0000FFFF) == 0) {
                                statusRegister = statusRegister | 0x02;
                            } else {
                                statusRegister = statusRegister & 0xFD;
                            }

                            //flag N -> check MSB of result
                            statusRegister = statusRegister & 0xFB;
                            statusRegister = statusRegister | ((outData & 0x00008000) >> 13);

                            //flag V -> Rdh7 ¯ •R15
                            statusRegister = statusRegister & 0xF7;
                            statusRegister = statusRegister | (((~(inDataH & 0x80)) & ((outData & 0x8000) >> 8)) >> 4);

                            //flag S -> XOR(V,N)
                            statusRegister = statusRegister & 0xEF;
                            statusRegister = statusRegister | (((statusRegister & 0x08) ^ ((statusRegister & 0x04) << 1)) << 1);

                            dataMemory.writeByte(
                                    0x5F,
                                    statusRegister
                            );

                            clockCycles = 2;

                        }
                        break;
                    }

                    case 0xC000: {
                        /*************************RJMP***********************/
//                        Log.d(UCModule.MY_LOG_TAG, "instruction RJMP");
                        pcPointer += ((0x03F8 & instruction) << 20) >> 20;          //Make sign extension to get correct two complement
                        clockCycles = 2;
                    }

                    case 0xB000: {
                        if ((0x0800 & instruction) == 0x0800) {
                            /*************************OUT***********************/
//                        Log.d(UCModule.MY_LOG_TAG, "instruction OUT");
                            dataMemory.writeByte(
                                    ((((0x0600 & instruction) >> 5) | ((0x000F & instruction))) + 0x20), //Address
                                    dataMemory.readByte(((0x00F0 & instruction) >> 4))                   //Data
                            );
                            clockCycles = 1;
                        }
                        break;
                    }

                    case 0xE000: {
                        /*************************LDI***********************/
//                        Log.d(UCModule.MY_LOG_TAG, "instruction LDI");
                        dataMemory.writeByte(
                                (0x10 | (0x00F0 & instruction) >> 4),                        //Address
                                (((0x0F00 & instruction) >> 4) | (0x000F & instruction))     //Data
                        );
                        clockCycles = 1;
                        break;
                    }

                    case 0xF000: {
                        if ((0x0C00 & instruction) == 0x0400) {
                            if ((0x0007 & instruction) == 0x0001) {
                                /*************************BRNE***********************/
//                                Log.d(UCModule.MY_LOG_TAG, "Instruction BRNE");
                                clockCycles = 1;

                                if ((0x02 & statusRegister) == 0) {
                                    pcPointer += ((0x03F8 & instruction) << 22) >> 25;          //Make sign extension to get correct two complement
                                    clockCycles = 2;
                                }
                            }
                        }
                        break;
                    }

                    default: {
                        //Casos extras
                        Log.v(UCModule.MY_LOG_TAG, "Unknown instruction: " + Integer.toHexString((int) instruction));
                    }
                }

            } catch (ArrayIndexOutOfBoundsException e) {
                break;
            }

            while ((clockCycles--) > 0) {
                waitClock();
            }
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

