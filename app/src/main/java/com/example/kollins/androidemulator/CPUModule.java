package com.example.kollins.androidemulator;

import android.os.Handler;
import android.util.Log;

import com.example.kollins.androidemulator.ATmega328P.DataMemory_ATmega328P;
import com.example.kollins.androidemulator.uCInterfaces.DataMemory;
import com.example.kollins.androidemulator.uCInterfaces.ProgramMemory;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * Created by kollins on 3/9/18.
 */

public class CPUModule implements Runnable {

    private static final int INSTRUCTION_GROUP_0 = 0;
    private static final int INSTRUCTION_GROUP_1 = 1;
    private static final int INSTRUCTION_GROUP_2 = 2;
    private static final int INSTRUCTION_GROUP_3 = 3;
    private static final int INSTRUCTION_GROUP_4 = 4;
    private static final int INSTRUCTION_GROUP_5 = 5;
    private static final int INSTRUCTION_GROUP_6 = 6;
    private static final int INSTRUCTION_GROUP_7 = 7;
    private static final int INSTRUCTION_GROUP_8 = 8;
    private static final int INSTRUCTION_GROUP_9 = 9;
    private static final int INSTRUCTION_GROUP_10 = 10;
    private static final int INSTRUCTION_GROUP_11 = 11;
    private static final int INSTRUCTION_GROUP_12 = 12;
    private static final int INSTRUCTION_GROUP_13 = 13;
    private static final int INSTRUCTION_GROUP_14 = 14;
    private static final int INSTRUCTION_GROUP_15 = 15;

    private static int instruction;

    private static Lock clockLock;
    private static Condition cpuClockCondition;

    private static ProgramMemory programMemory;
    private static DataMemory dataMemory;
    private static Handler uCHandler;
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
            Log.d(UCModule.MY_LOG_TAG, "Instruction loaded: " + Integer.toHexString(instruction));

            /*************************Decode and execute*******************/
            Decoder.values()[(0x0000F000 & instruction) >> 12].executeInstruction();
        }

        Log.i(UCModule.MY_LOG_TAG, "Finishing CPU");

    }

    private static void waitClock() {

        clockLock.lock();
        try {
            UCModule.clockVector[UCModule.CPU_ID] = true;

            for (int i = 0; i < UCModule.clockVector.length; i++) {
                if (!UCModule.clockVector[i]) {
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

    public enum Decoder {
        /*CPC*/
        INSTRUCTION_GROUP_0 {
            @Override
            public void executeInstruction() {
                switch ((0x0C00 & instruction)) {
                    case 0x0400:
                        /*************************CPC***********************/
                        Log.d(UCModule.MY_LOG_TAG, "instruction CPC");

                        byte regD = dataMemory.readByte((0x01F0 & instruction) >> 4);
                        byte regR = dataMemory.readByte(((0x0200 & instruction) >> 5) | (0x000F & instruction));

                        int result = regD - regR;

                        //If carry is set
                        if (dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 0)) {
                            result -= 1;
                        }

                        //Flag H
                        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 5,
                                (0x00000001 &
                                        ((((~(0x08 & regD) & (0x08 & regR)) |
                                                ((0x08 & result) & (0x08 & regR)) |
                                                (~(0x08 & regD) & (0x08 & result)))
                                                >> 3))) != 0);

                        //Flag V
                        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 3,
                                (0x00000001 &
                                        (((0x80 & regD) & (~(0x80 & regR)) & (~(0x80 & result)) |
                                                (~(0x80 & regD)) & (0x80 & regR) & (0x80 & result))
                                                >> 7)) != 0);

                        //Flag N
                        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 2,
                                !((result & 0x00000080) == 0));

                        //Flag S
                        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 4,
                                dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 2) ^ dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 3));

                        //Flag Z
                        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 1,
                                ((result & 0x000000FF) == 0) & dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 1));

                        //Flag C
                        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 0,
                                (0x00000001 &
                                        ((((~(0x80 & regD) & (0x80 & regR)) |
                                                ((0x80 & result) & (0x80 & regR)) |
                                                (~(0x80 & regD) & (0x80 & result)))
                                                >> 7))) != 0);

                        break;

                    default:
                        Log.w(UCModule.MY_LOG_TAG, "Unknown instruction Group 0: " + Integer.toBinaryString(instruction));
                }
            }
        },
        INSTRUCTION_GROUP_1 {
            @Override
            public void executeInstruction() {
                Log.w(UCModule.MY_LOG_TAG, "Unknown instruction Group 1: " + Integer.toBinaryString(instruction));
            }
        },

        /*AND - CLR - EOR - MOV - OR - TST*/
        INSTRUCTION_GROUP_2 {
            @Override
            public void executeInstruction() {
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

                    default:
                        Log.w(UCModule.MY_LOG_TAG, "Unknown instruction Group 2: " + Integer.toBinaryString(instruction));
                }
            }
        },

        /*CPI*/
        INSTRUCTION_GROUP_3 {
            @Override
            public void executeInstruction() {

                int constValue = (((0x00000F00 & instruction) >> 4) | (0x0000000F & instruction));
                byte regRead = dataMemory.readByte(0x10 | (0x000000F0 & instruction) >> 4);

                int result = (regRead - constValue);

                //Flag H
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 5,
                        (0x00000001 &
                                ((((~(0x08 & regRead) & (0x08 & constValue)) |
                                        ((0x08 & result) & (0x08 & constValue)) |
                                        (~(0x08 & regRead) & (0x08 & result)))
                                        >> 3))) != 0);

                //Flag V
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 3,
                        (0x00000001 &
                                (((0x80 & regRead) & (~(0x80 & constValue)) & (~(0x80 & result)) |
                                        (~(0x80 & regRead)) & (0x80 & constValue) & (0x80 & result))
                                        >> 7)) != 0);

                //Flag N
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 2,
                        !((result & 0x00000080) == 0));

                //Flag S
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 4,
                        dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 2) ^ dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 3));

                //Flag Z
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 1,
                        (result & 0x000000FF) == 0);

                //Flag C
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 0,
                        (0x00000001 &
                                ((((~(0x80 & regRead) & (0x80 & constValue)) |
                                        ((0x80 & result) & (0x80 & constValue)) |
                                        (~(0x80 & regRead) & (0x80 & result)))
                                        >> 7))) != 0);

            }
        },
        INSTRUCTION_GROUP_4 {
            @Override
            public void executeInstruction() {
                Log.w(UCModule.MY_LOG_TAG, "Unknown instruction Group 4: " + Integer.toBinaryString(instruction));
            }
        },
        INSTRUCTION_GROUP_5 {
            @Override
            public void executeInstruction() {
                Log.w(UCModule.MY_LOG_TAG, "Unknown instruction Group 5: " + Integer.toBinaryString(instruction));
            }
        },

        /*ORI - SBR*/
        INSTRUCTION_GROUP_6 {
            @Override
            public void executeInstruction() {
                /*************************ORI/SBR***********************/
                Log.d(UCModule.MY_LOG_TAG, "instruction ORI/SBR");

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
            }
        },
        INSTRUCTION_GROUP_7 {
            @Override
            public void executeInstruction() {
                Log.w(UCModule.MY_LOG_TAG, "Unknown instruction Group 7: " + Integer.toBinaryString(instruction));
            }
        },
        INSTRUCTION_GROUP_8 {
            @Override
            public void executeInstruction() {
                Log.w(UCModule.MY_LOG_TAG, "Unknown instruction Group 8: " + Integer.toBinaryString(instruction));
            }
        },

        /*
        * ADIW - CALL - CBI - JMP - SBI - SBIS
        * BSET - SEC - SEZ - SEN - SEI - SES - SEV - SET - SEH
        * */
        INSTRUCTION_GROUP_9 {
            @Override
            public void executeInstruction() {
                switch ((0x0F00 & instruction)) {
                    case 0x0200:
                    case 0x0300:
                        switch ((0x000F & instruction)) {
                            case 0x000D:
                                /*************************ST (X post increment)***********************/
                                Log.d(UCModule.MY_LOG_TAG, "instruction ST (X post increment)");

                                waitClock();

                                byte xRegL = dataMemory.readByte(0x1A);
                                byte xRegH = dataMemory.readByte(0x1B);
                                int destAddress = (0x0000FF00 & (xRegH << 8)) | (0x000000FF & xRegL);

                                dataMemory.writeByte(destAddress, dataMemory.readByte((0x01F0 & instruction) >> 4));

                                xRegL += 1;
                                if (xRegL == 0x00) {
                                    xRegH += 1;
                                }

                                dataMemory.writeByte(0x1A, xRegL);
                                dataMemory.writeByte(0x1B, xRegH);

                                break;

                            default:
                                Log.w(UCModule.MY_LOG_TAG, "Unknown instruction Group 9: " + Integer.toBinaryString(instruction));
                        }

                        break;

                    case 0x0400:
                        switch ((0x000F & instruction)) {
                            case 0x0008:
                                switch ((0x0080 & instruction)){
                                    case 0x0000:
                                        /*********************BSET*********************/
                                        Log.d(UCModule.MY_LOG_TAG, "instruction BSET");

                                        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, (0x07 & (instruction>>4)),true);

                                        break;
                                    case 0x0080:
                                        /*********************BCLR*********************/
                                        Log.d(UCModule.MY_LOG_TAG, "instruction BCLR");

                                        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, (0x07 & (instruction>>4)),false);

                                        break;
                                    default:
                                        Log.w(UCModule.MY_LOG_TAG, "Unknown instruction Group 9: " + Integer.toBinaryString(instruction));
                                }
                                break;
//                            case 0x0009:
//                                break;

                            case 0x000C:
                            case 0x000D:
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

                            case 0x000E:
                            case 0x000F:
                                /*************************CALL***********************/
                                //4 clockCycles
                                waitClock();
                                waitClock();
                                waitClock();

                                int callOut = (0x0001 & instruction) | ((0x01F0 & instruction) >> 3);
                                instruction = programMemory.loadInstruction();
                                callOut = (callOut << 16) | instruction;

                                //PC is already in position to go to stack (write little-endian)

                                int stackPointer = (dataMemory.readByte(DataMemory_ATmega328P.SPH_ADDR) << 8) |
                                        (0x000000FF & dataMemory.readByte(DataMemory_ATmega328P.SPL_ADDR));

                                //Write PC low
                                stackPointer -= 1;
                                dataMemory.writeByte(stackPointer, (byte) (0x000000FF & programMemory.getPC()));
                                stackPointer -= 1;
                                //Write PC high
                                dataMemory.writeByte(stackPointer, (byte) ((0x000000FF & (programMemory.getPC()>>8))));

                                //Update SPL
                                dataMemory.writeByte(DataMemory_ATmega328P.SPL_ADDR, (byte) (0x000000FF & stackPointer));
                                //Update SPH
                                dataMemory.writeByte(DataMemory_ATmega328P.SPH_ADDR, (byte) ((0x0000FF00 & stackPointer)>>8));

                                programMemory.setPC(callOut);

                                break;

                            default:
                                Log.w(UCModule.MY_LOG_TAG, "Unknown instruction Group 9: " + Integer.toBinaryString(instruction));
                        }
                        break;


                    case 0x0500:
                        switch ((0x000F & instruction)) {
                            case 0x000C:
                            case 0x000D:
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

                            case 0x000E:
                            case 0x000F:
                                /*************************CALL***********************/
                                //4 clockCycles
                                waitClock();
                                waitClock();
                                waitClock();

                                int callOut = (0x0001 & instruction) | ((0x01F0 & instruction) >> 3);
                                instruction = programMemory.loadInstruction();
                                callOut = (callOut << 16) | instruction;

                                //PC is already in position to go to stack (write little-endian)

                                int stackPointer = (dataMemory.readByte(DataMemory_ATmega328P.SPH_ADDR) << 8) |
                                        (0x000000FF & dataMemory.readByte(DataMemory_ATmega328P.SPL_ADDR));

                                //Write PC low
                                dataMemory.writeByte(stackPointer, (byte) (0x000000FF & programMemory.getPC()));
                                stackPointer -= 1;
                                //Write PC high
                                dataMemory.writeByte(stackPointer, (byte) ((0x000000FF & (programMemory.getPC()>>8))));
                                stackPointer -= 1;

                                //Update SPL
                                dataMemory.writeByte(DataMemory_ATmega328P.SPL_ADDR, (byte) (0x000000FF & stackPointer));
                                //Update SPH
                                dataMemory.writeByte(DataMemory_ATmega328P.SPH_ADDR, (byte) ((0x0000FF00 & stackPointer)>>8));

                                programMemory.setPC(callOut);

                                break;

                            default:
                                Log.w(UCModule.MY_LOG_TAG, "Unknown instruction Group 9: " + Integer.toBinaryString(instruction));
                        }
                        break;


                    case 0x0600:
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
                                !(((inDataH & 0x80) & ((~(outData & 0x00008000)) >> 8)) == 0));

                        //Flag Z
                        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 1,
                                (outData & 0x0000FFFF) == 0);

                        //Flag N
                        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 2,
                                !((outData & 0x00008000) == 0));

                        //Flag V
                        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 3,
                                !(((~(inDataH & 0x80)) & ((outData & 0x00008000) >> 8)) == 0));

                        //Flag S
                        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 4,
                                dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 2) ^ dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 3));

                        break;


                    case 0x0800:
                        /*************************CBI***********************/
                        Log.d(UCModule.MY_LOG_TAG, "instruction CBI");
                        waitClock();
                        dataMemory.writeBit(((0x00F8 & instruction) >> 3) + 0x20, (0x0007 & instruction), false);
                        break;


                    case 0x0A00:
                        /*************************SBI***********************/
                        Log.d(UCModule.MY_LOG_TAG, "instruction SBI");
                        waitClock();
                        dataMemory.writeBit(((0x00F8 & instruction) >> 3) + 0x20, (0x0007 & instruction), true);
                        break;


                    case 0x0B00:
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

                    default:
                        Log.w(UCModule.MY_LOG_TAG, "Unknown instruction Group 9: " + Integer.toBinaryString(instruction));
                }
            }
        },
        INSTRUCTION_GROUP_10 {
            @Override
            public void executeInstruction() {
                Log.w(UCModule.MY_LOG_TAG, "Unknown instruction Group 10: " + Integer.toBinaryString(instruction));
            }
        },

        /*IN - OUT*/
        INSTRUCTION_GROUP_11 {
            @Override
            public void executeInstruction() {
                switch ((0x0800 & instruction)) {
                    case 0x0800:
                        /*************************OUT***********************/
                        Log.d(UCModule.MY_LOG_TAG, "instruction OUT");
                        dataMemory.writeByte(
                                ((((0x0600 & instruction) >> 5) | ((0x000F & instruction))) + 0x20), //Address
                                dataMemory.readByte(((0x01F0 & instruction) >> 4))                   //Data
                        );
                        break;

                    default:
                        Log.w(UCModule.MY_LOG_TAG, "Unknown instruction Group 11: " + Integer.toBinaryString(instruction));
                }
            }
        },

        /*RJMP*/
        INSTRUCTION_GROUP_12 {
            @Override
            public void executeInstruction() {
                /*************************RJMP***********************/
                waitClock();
                Log.d(UCModule.MY_LOG_TAG, "instruction RJMP");
                programMemory.addToPC(((0x0FFF & instruction) << 20) >> 20);          //Make sign extension to get correct two complement
            }
        },
        INSTRUCTION_GROUP_13 {
            @Override
            public void executeInstruction() {
                Log.w(UCModule.MY_LOG_TAG, "Unknown instruction Group 13: " + Integer.toBinaryString(instruction));
            }
        },

        /*LDI - SER*/
        INSTRUCTION_GROUP_14 {
            @Override
            public void executeInstruction() {
                /*************************LDI/SER***********************/
                Log.d(UCModule.MY_LOG_TAG, "instruction LDI/SER");
                dataMemory.writeByte(
                        (0x10 | (0x00F0 & instruction) >> 4),                               //Address
                        (byte) (((0x0F00 & instruction) >> 4) | (0x000F & instruction))     //Data
                );
            }
        },

        /*
         *BRBS - BRCS - BRLO - BREQ - BRMI - BRLT - BRHS - BRTS - BRVS - BRIE
         *BRBC - BRCC - BRSH - BRNE - BRPL - BRGE - BRHC - BRTC - BRVC - BRID
         *BLD - BST - SBRC - SBRS
         */
        INSTRUCTION_GROUP_15 {
            @Override
            public void executeInstruction() {
                switch ((0x0C00 & instruction)) {
                    case 0x0400:
                        /*************************BRBC***********************/
                        Log.d(UCModule.MY_LOG_TAG, "Instruction BRBC");
                        if (!dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, (0x0007 & instruction))) {
                            waitClock();
                            programMemory.addToPC(((0x03F8 & instruction) << 22) >> 25);          //Make sign extension to get correct two complement
                        }
                        break;

                    case 0x0000:
                        /*************************BRBS***********************/
                        Log.d(UCModule.MY_LOG_TAG, "Instruction BRBS");
                        if (dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, (0x0007 & instruction))) {
                            waitClock();
                            programMemory.addToPC(((0x03F8 & instruction) << 22) >> 25);          //Make sign extension to get correct two complement
                        }
                        break;

                    default:
                        Log.w(UCModule.MY_LOG_TAG, "Unknown instruction Group 15: " + Integer.toBinaryString(instruction));
                }
            }
        };

        public abstract void executeInstruction();
    }

}

