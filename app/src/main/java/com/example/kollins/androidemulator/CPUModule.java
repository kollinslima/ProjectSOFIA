package com.example.kollins.androidemulator;

import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.TextView;

import com.example.kollins.androidemulator.ATmega328P.DataMemory_ATmega328P;
import com.example.kollins.androidemulator.uCInterfaces.DataMemory;
import com.example.kollins.androidemulator.uCInterfaces.ProgramMemory;

import java.io.Serializable;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * Created by kollins on 3/9/18.
 */

public class CPUModule implements Runnable{

    private int instruction;

    private Lock clockLock;
    private Condition cpuClockCondition;

    private ProgramMemory programMemory;
    private DataMemory dataMemory;
    private Handler uCHandler;
    private UCModule uCModule;


    public CPUModule(ProgramMemory programMemory, DataMemory dataMemory, UCModule uCModule,
                     Handler uCHandler, Lock clockLock) {
        this.programMemory = programMemory;
        this.dataMemory = dataMemory;
        this.uCHandler = uCHandler;
        this.uCModule = uCModule;

        this.clockLock = clockLock;
        cpuClockCondition = clockLock.newCondition();
    }

    @Override
    public void run() {

        while (!uCModule.getResetFlag()) {

            waitClock();

            /******************************Fetch*****************************/

            //Load instruction and add PC;
            instruction = programMemory.loadInstruction();

            /*************************Decode and execute*******************/
            switch ((0xF000 & instruction)) {
                case 0x2000: {
                    switch ((0x0C00 & instruction)) {
                        case 0x0400:
                            /*************************EOR/CLR***********************/
                            Log.d(UCModule.MY_LOG_TAG, "instruction CLR/EOR");

                            int outAddress = (0x01F0 & instruction) >> 4;
                            byte outData = (byte) (dataMemory.readByte(outAddress) ^
                                    dataMemory.readByte(((0x0200 & instruction) >> 5) | (0x000F & instruction)));

                            dataMemory.writeByte(outAddress, outData);

                            //Update Status Register
                            //Flag Z
                            dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 1, outData == 0);
                            //Flag N
                            dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 2, !((0x80 & outData) == 0));
                            //Flag V
                            dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 3, false);
                            //Flag S
                            dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 4,
                                    dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 2) ^ dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 3));

                            break;
                    }
                    break;
                }

                case 0x6000: {
                    /*************************SBR***********************/
                    Log.d(UCModule.MY_LOG_TAG, "instruction SBR");

                    int Rd = (0x10 | (0x00F0 & instruction) >> 4);
                    byte outData = (byte) (dataMemory.readByte(Rd) | (((0x0F00 & instruction) >> 4) | (0x000F & instruction)));

                    dataMemory.writeByte(Rd, outData);

                    //Update Flags
                    //Flag Z
                    dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 1, outData == 0);
                    //Flag N
                    dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 2, !((0x80 & outData) == 0));
                    //Flag V
                    dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 3, false);
                    //Flag S
                    dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 4,
                            dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 2) ^ dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 3));

                    break;
                }

                case 0x9000: {
                    switch ((0x0F00 & instruction)) {
                        case 0x0400: {
                            switch ((0x000F & instruction)) {
                                case 0x0008:
                                    break;
                                case 0x0009:
                                    break;

                                case 0x000C:
                                case 0x000D: {
                                    /*************************JMP***********************/
                                    Log.d(UCModule.MY_LOG_TAG, "instruction JMP");

                                    //3 clockCycles
                                    waitClock();
                                    waitClock();

                                    int jumpOut = (0x0001 & instruction) | ((0x01F0 & instruction) >> 3);
                                    instruction = programMemory.loadInstruction();
                                    jumpOut = (jumpOut << 16) | instruction;

                                    programMemory.setPC(jumpOut);

                                    break;
                                }

                            }
                            break;
                        }

                        case 0x0500: {
                            switch ((0x000F & instruction)) {
                                case 0x000C:
                                case 0x000D: {
                                    /*************************JMP***********************/
                                    Log.d(UCModule.MY_LOG_TAG, "instruction JMP");

                                    //3 clockCycles
                                    waitClock();
                                    waitClock();

                                    int jumpOut = (0x0001 & instruction) | ((0x01F0 & instruction) >> 3);
                                    instruction = programMemory.loadInstruction();
                                    jumpOut = (jumpOut << 16) | instruction;

                                    programMemory.setPC(jumpOut);
                                    break;
                                }
                            }
                            break;
                        }

                        case 0x0600: {
                            /*************************ADIW***********************/
                            Log.d(UCModule.MY_LOG_TAG, "instruction ADIW");

                            //2 clockCycles
                            waitClock();

                            int offset = ((0x0030 & instruction) * 2);
                            byte inDataL = dataMemory.readByte(0x18 + offset);
                            byte inDataH = dataMemory.readByte(0x19 + offset);

                            int outData = (((0x00FF & inDataH) << 8) | (0x00FF & inDataL)) +
                                    (((0x00C0 & instruction) >> 2) | (0x000F & instruction));

                            dataMemory.writeByte(0x18 + offset, (byte) (0x00FF & outData));
                            dataMemory.writeByte(0x19 + offset, (byte) ((0xFF00 & outData) >> 8));

                            //Update status register
                            //Flag C
                            dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 0,
                                    !(((inDataH & 0x80) & ((~(outData & 0x8000)) >> 8)) == 0));

                            //Flag Z
                            dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 1,
                                    (outData & 0x0000FFFF) == 0);

                            //Flag N
                            dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 2,
                                    !((outData & 0x00008000) == 0));

                            //Flag V
                            dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 3,
                                    !(((~(inDataH & 0x80)) & ((outData & 0x8000) >> 8)) == 0));

                            //Flag S
                            dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 4,
                                    dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 2) ^ dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 3));

                            break;
                        }

                        case 0x0800: {
                            /*************************CBI***********************/
                            Log.d(UCModule.MY_LOG_TAG, "instruction CBI");
                            waitClock();
                            dataMemory.writeBit(((0x00F8 & instruction) >> 3) + 0x20, (0x0007 & instruction), false);
                            break;
                        }

                        case 0x0A00: {
                            /*************************SBI***********************/
                            Log.d(UCModule.MY_LOG_TAG, "instruction SBI");
                            waitClock();
                            dataMemory.writeBit(((0x00F8 & instruction) >> 3) + 0x20, (0x0007 & instruction), true);
                            break;
                        }

                        case 0x0B00: {
                            /*************************SBIS***********************/
                            Log.d(UCModule.MY_LOG_TAG, "instruction SBIS");

                            if (dataMemory.readBit(((0x00F8 & instruction) >> 3) + 0x20, (0x0007 & instruction))) {

                                waitClock();
                                instruction = programMemory.loadInstruction();

                                //Test 2 word instructions
                                //JMP
                                int testJMP_CALL = (0xFE0E & instruction);
                                int testLDS_STS = (0xFE0F & instruction);

                                if (testJMP_CALL == 0x940C ||       //JMP
                                        testJMP_CALL == 0x940E ||   //CALL
                                        testLDS_STS == 0x9000 ||    //LDS
                                        testLDS_STS == 0x9200)      //STS
                                {
                                    waitClock();
                                    programMemory.addToPC(1);
                                }
                            }
                            break;
                        }
                    }

                    break;
                }

                case 0xC000: {
                    /*************************RJMP***********************/
                    waitClock();
                    Log.d(UCModule.MY_LOG_TAG, "instruction RJMP");
                    programMemory.addToPC(((0x0FFF & instruction) << 20) >> 20);          //Make sign extension to get correct two complement
                    break;
                }

                case 0xB000: {
                    switch ((0x0800 & instruction)) {
                        case 0x0800: {
                            /*************************OUT***********************/
                            Log.d(UCModule.MY_LOG_TAG, "instruction OUT");
                            dataMemory.writeByte(
                                    ((((0x0600 & instruction) >> 5) | ((0x000F & instruction))) + 0x20), //Address
                                    dataMemory.readByte(((0x01F0 & instruction) >> 4))                   //Data
                            );
                            break;
                        }
                    }
                    break;
                }

                case 0xE000: {
                    /*************************LDI/SER***********************/
                    Log.d(UCModule.MY_LOG_TAG, "instruction LDI/SER");
                    dataMemory.writeByte(
                            (0x10 | (0x00F0 & instruction) >> 4),                               //Address
                            (byte) (((0x0F00 & instruction) >> 4) | (0x000F & instruction))     //Data
                    );
                    break;
                }

                case 0xF000: {
                    switch ((0x0C00 & instruction)) {
                        case 0x0400: {
                            /*************************BRBC***********************/
                            Log.d(UCModule.MY_LOG_TAG, "Instruction BRBC");
                            if (!dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, (0x0007 & instruction))) {
                                waitClock();
                                programMemory.addToPC(((0x03F8 & instruction) << 22) >> 25);          //Make sign extension to get correct two complement
                            }
                            break;
                        }
                        case 0x0000: {
                            /*************************BRBS***********************/
                            Log.d(UCModule.MY_LOG_TAG, "Instruction BRBS");
                            if (dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, (0x0007 & instruction))) {
                                waitClock();
                                programMemory.addToPC(((0x03F8 & instruction) << 22) >> 25);          //Make sign extension to get correct two complement
                            }
                            break;
                        }
                    }
                    break;
                }

                default: {
                    //Casos extras
                    Log.v(UCModule.MY_LOG_TAG, "Unknown instruction: " + Integer.toHexString((int) instruction));
                }
            }

        }

        Log.i(UCModule.MY_LOG_TAG, "Finishing CPU");

    }

    private void waitClock() {

        clockLock.lock();
        try {
            UCModule.clockVector[UCModule.CPU_ID] = true;

            for (boolean b : UCModule.clockVector){
                if (!b){
                    cpuClockCondition.await();
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

    public void clockCPU() {
        clockLock.lock();
        try {
            cpuClockCondition.signal();
        } finally {
            clockLock.unlock();
        }
    }

}

