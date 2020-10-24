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

package com.kollins.project.sofia;

import android.util.Log;

import com.kollins.project.sofia.atmega328p.DataMemory_ATmega328P;
import com.kollins.project.sofia.ucinterfaces.CPUInstructions;
import com.kollins.project.sofia.ucinterfaces.DataMemory;
import com.kollins.project.sofia.ucinterfaces.ProgramMemory;

/**
 * Created by kollins on 3/9/18.
 */

public class CPUModule implements CPUInstructions {

    public static short[] INSTRUCTION_ID = new short[(int) (Math.pow(2, 16) + 1)];

    private static int instruction;

    private static ProgramMemory programMemory;
    private static DataMemory dataMemory;
//    private static Handler uCHandler;
//    private UCModule uCModule;

    //Auxiliary Variables for Instruction Processing
    private static byte regD, regR, result;
    private static byte dataL, dataH;
    private static int offset, outData, outAddress, constValue;
    private static int stackPointer;
    private static int testJMP_CALL, testLDS_STS;
    private static int instruction_tmp;

    private static boolean needMoreClockCycles;
    private static boolean clockCycleDone;
    private boolean interruptionReturn;
    private static short clockCycleNeeded;
    private short clockCycleCount;


    public CPUModule(ProgramMemory programMemory, DataMemory dataMemory) {

        this.programMemory = programMemory;
        this.dataMemory = dataMemory;
//        this.uCHandler = uCHandler;
//        this.uCModule = uCModule;

        clockCycleDone = false;
        needMoreClockCycles = false;
        interruptionReturn = false;
        clockCycleCount = 0;
        clockCycleNeeded = 0;
    }

    public void run() {

        if (needMoreClockCycles) {

            /*****Loop for multicycle instructions*****/
            /*
            Instructions:
            clockCycleNeeded = Clock Cycle - 2 (1 clock cycle already done, last clock will execute)
             */
            while (++clockCycleCount <= clockCycleNeeded) {
                return;
            }

            clockCycleDone = true;
            needMoreClockCycles = false;
            clockCycleCount = 0;

            if (interruptionReturn) {
                interruptionReturn = false;
                interruptionRoutinePrepare();
            } else {
                decodeAndExecute();
            }
        } else {

            /******************************Fetch*****************************/
            //Load instruction and add PC;
            instruction = programMemory.loadInstruction();
//            Log.d(UCModule.MY_LOG_TAG, "Instruction loaded: " + Integer.toHexString(instruction));
            decodeAndExecute();
        }

    }

    private void decodeAndExecute() {
        /*************************Decode and execute*******************/
        Executor.values()[INSTRUCTION_ID[instruction]].executeInstruction();

        if (needMoreClockCycles){
            return;
        }

        if (UCModule.interruptionModule.haveInterruption()) {
            interruptionRoutinePrepare();
        }
    }

    private void interruptionRoutinePrepare() {
//        Log.i(UCModule.MY_LOG_TAG, "Handle Interruption");

        //Virtual CALL - 4 clock cycles
        if (!clockCycleDone) {
            needMoreClockCycles = true;
            clockCycleNeeded = 4;
            interruptionReturn = true;
            return;
        }
        clockCycleDone = false;

        //PC is already in position to go to stack (write little-endian)
        stackPointer = (dataMemory.readByte(DataMemory_ATmega328P.SPH_ADDR) << 8) |
                (0x000000FF & dataMemory.readByte(DataMemory_ATmega328P.SPL_ADDR));


        //Write PC low
        dataMemory.writeByte(stackPointer, (byte) (0x000000FF & programMemory.getPC()));
        stackPointer -= 1;
        //Write PC high
        dataMemory.writeByte(stackPointer, (byte) (0x000000FF & (programMemory.getPC() >> 8)));
        stackPointer -= 1;

        Log.d("CPU", "StackPointer_InterruptFinish: " + Integer.toHexString(stackPointer));

        //Update SPL
        dataMemory.writeByte(DataMemory_ATmega328P.SPL_ADDR, (byte) (0x000000FF & stackPointer));
        //Update SPH
        dataMemory.writeByte(DataMemory_ATmega328P.SPH_ADDR, (byte) ((0x0000FF00 & stackPointer) >> 8));

        programMemory.setPC(UCModule.interruptionModule.getPCInterruptionAddress());
        UCModule.interruptionModule.disableGlobalInterruptions();
    }

    public enum Executor {
        INSTRUCTION_ADC {
            @Override
            public void executeInstruction() {
                /*************************ADC***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction ADC");

                regD = dataMemory.readByte((0x01F0 & instruction) >> 4);
                regR = dataMemory.readByte(((0x0200 & instruction) >> 5) | (0x000F & instruction));

                result = (byte) (regD + regR);

                //If carry is set
                if (dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 0)) {
                    result += 1;
                }

                dataMemory.writeByte(((0x01F0 & instruction) >> 4), result);

                //Flag H
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 5,
                        (0x00000001 &
                                (((((0x08 & regD) & (0x08 & regR)) |
                                        ((~(0x08 & result)) & (0x08 & regR)) |
                                        ((0x08 & regD) & (~(0x08 & result))))
                                        >> 3))) != 0);

                //Flag V
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 3,
                        (0x00000001 &
                                (((0x80 & regD) & (0x80 & regR) & (~(0x80 & result)) |
                                        (~(0x80 & regD)) & (~(0x80 & regR)) & (0x80 & result))
                                        >> 7)) != 0);

                //Flag N
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 2,
                        !((result & 0x00000080) == 0));

                //Flag S
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 4,
                        dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 2) ^ dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 3));

                //Flag Z
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 1,
                        ((result & 0x000000FF) == 0));

                //Flag C
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 0,
                        (0x00000001 &
                                (((((0x80 & regD) & (0x80 & regR)) |
                                        ((~(0x80 & result)) & (0x80 & regR)) |
                                        ((0x80 & regD) & (~(0x80 & result))))
                                        >> 7))) != 0);
            }
        },
        INSTRUCTION_ADD {
            @Override
            public void executeInstruction() {
                /*************************ADD***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction ADD");

                regD = dataMemory.readByte((0x01F0 & instruction) >> 4);
                regR = dataMemory.readByte(((0x0200 & instruction) >> 5) | (0x000F & instruction));

                result = (byte) (regD + regR);

                dataMemory.writeByte((0x01F0 & instruction) >> 4, result);

                //Flag H
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 5,
                        (0x00000001 &
                                (((((0x08 & regD) & (0x08 & regR)) |
                                        ((~(0x08 & result)) & (0x08 & regR)) |
                                        ((0x08 & regD) & (~(0x08 & result))))
                                        >> 3))) != 0);

                //Flag V
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 3,
                        (0x00000001 &
                                (((0x80 & regD) & (0x80 & regR) & (~(0x80 & result)) |
                                        (~(0x80 & regD)) & (~(0x80 & regR)) & (0x80 & result))
                                        >> 7)) != 0);

                //Flag N
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 2,
                        !((result & 0x00000080) == 0));

                //Flag S
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 4,
                        dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 2) ^ dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 3));

                //Flag Z
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 1,
                        ((result & 0x000000FF) == 0));

                //Flag C
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 0,
                        (0x00000001 &
                                (((((0x80 & regD) & (0x80 & regR)) |
                                        ((~(0x80 & result)) & (0x80 & regR)) |
                                        ((0x80 & regD) & (~(0x80 & result))))
                                        >> 7))) != 0);

            }
        },
        INSTRUCTION_ADIW {
            @Override
            public void executeInstruction() {
                /*************************ADIW***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction ADIW");

                //2 clockCycles
                if (!clockCycleDone) {
                    needMoreClockCycles = true;
                    clockCycleNeeded = 0;
                    return;
                }
                clockCycleDone = false;

                offset = (((0x0030 & instruction) >> 4) * 2);
                dataL = dataMemory.readByte(0x18 + offset);
                dataH = dataMemory.readByte(0x19 + offset);

                outData = (((0x00FF & dataH) << 8) | (0x00FF & dataL)) +
                        (((0x00C0 & instruction) >> 2) | (0x000F & instruction));

                dataMemory.writeByte(0x18 + offset, (byte) (0x000000FF & outData));
                dataMemory.writeByte(0x19 + offset, (byte) ((0x0000FF00 & outData) >> 8));

                //Update status register
                //Flag C
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 0,
                        !(((dataH & 0x80) & ((~(outData & 0x00008000)) >> 8)) == 0));

                //Flag Z
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 1,
                        (outData & 0x0000FFFF) == 0);

                //Flag N
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 2,
                        !((outData & 0x00008000) == 0));

                //Flag V
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 3,
                        !(((~(dataH & 0x80)) & ((outData & 0x00008000) >> 8)) == 0));

                //Flag S
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 4,
                        dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 2) ^ dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 3));

            }
        },
        INSTRUCTION_AND {//AND - TST

            @Override
            public void executeInstruction() {
                /*************************AND/TST***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction AND/TST");

                outAddress = (0x01F0 & instruction) >> 4;
                result = (byte) (dataMemory.readByte(outAddress) &
                        dataMemory.readByte(((0x0200 & instruction) >> 5) | (0x000F & instruction)));

                dataMemory.writeByte(outAddress, result);

                //Update Status Register
                //Flag Z
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 1, result == 0);
                //Flag N
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 2, !((0x80 & result) == 0));
                //Flag V
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 3, false);
                //Flag S
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 4,
                        dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 2) ^ dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 3));

            }
        },
        INSTRUCTION_ANDI {
            @Override
            public void executeInstruction() {
                /*************************ANDI***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction ANDI");

                outAddress = (0x10 | (0x00F0 & instruction) >> 4);
                result = (byte) (dataMemory.readByte(outAddress) & (((0x0F00 & instruction) >> 4) | (0x000F & instruction)));

                dataMemory.writeByte(outAddress, result);

                //Update Flags
                //Flag Z
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 1, result == 0);
                //Flag N
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 2, !((0x80 & result) == 0));
                //Flag V
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 3, false);
                //Flag S
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 4,
                        dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 2) ^ dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 3));
            }
        },
        INSTRUCTION_ASR {
            @Override
            public void executeInstruction() {
                /*************************ASR***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction ASR");

                regD = dataMemory.readByte((0x01F0 & instruction) >> 4);
                result = (byte) (regD >> 1);
                dataMemory.writeByte((0x01F0 & instruction) >> 4, result);

                //Update Status Register
                //Flag C
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 0, !((0x01 & regD) == 0));
                //Flag Z
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 1, result == 0);
                //Flag N
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 2, !((0x80 & result) == 0));
                //Flag V
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 3,
                        dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 2) ^ dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 0));
                //Flag S
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 4,
                        dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 2) ^ dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 3));

            }
        },
        INSTRUCTION_BCLR {
            @Override
            public void executeInstruction() {
                /*********************BCLR*********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction BCLR");

                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, (0x07 & (instruction >> 4)), false);
            }
        },
        INSTRUCTION_BLD {
            @Override
            public void executeInstruction() {
                /*********************BLD*********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction BLD");

                dataMemory.writeBit((0x01F0 & instruction) >> 4, (0x07 & instruction),
                        dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 6));
            }
        },
        INSTRUCTION_BRBC {
            @Override
            public void executeInstruction() {
                /*************************BRBC***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction BRBC");

                if (clockCycleDone){
                    clockCycleDone = false;
                    return;
                }

                if (!dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, (0x0007 & instruction))) {

                    needMoreClockCycles = true;
                    clockCycleNeeded = 0;

                    programMemory.addToPC(((0x03F8 & instruction) << 22) >> 25);          //Make sign extension to get correct two complement
                }
            }
        },
        INSTRUCTION_BRBS {
            @Override
            public void executeInstruction() {
                /*************************BRBS***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction BRBS");

                if (clockCycleDone){
                    clockCycleDone = false;
                    return;
                }

                if (dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, (0x0007 & instruction))) {

                    needMoreClockCycles = true;
                    clockCycleNeeded = 0;

                    programMemory.addToPC(((0x03F8 & instruction) << 22) >> 25);          //Make sign extension to get correct two complement
                }
            }
        },
        INSTRUCTION_BREAK {
            @Override
            public void executeInstruction() {
                Log.w(UCModule.MY_LOG_TAG, "Not implemented instruction: BREAK");
            }
        },
        INSTRUCTION_BSET {
            @Override
            public void executeInstruction() {
                /*********************BSET*********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction BSET");

                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, (0x07 & (instruction >> 4)), true);
            }
        },
        INSTRUCTION_BST {
            @Override
            public void executeInstruction() {
                /*************************BST***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction BST");

                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 6,
                        dataMemory.readBit((0x01F0 & instruction) >> 4, (0x07 & instruction)));
            }
        },
        INSTRUCTION_CALL {
            @Override
            public void executeInstruction() {
                /*************************CALL***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction CALL");

                //4 clockCycles
                if (!clockCycleDone) {
                    needMoreClockCycles = true;
                    clockCycleNeeded = 2;
                    return;
                }
                clockCycleDone = false;

                outData = (0x0001 & instruction) | ((0x01F0 & instruction) >> 3);

                instruction = programMemory.loadInstruction();
                outData = (outData << 16) | instruction;

                //PC is already in position to go to stack (write little-endian)

                stackPointer = (dataMemory.readByte(DataMemory_ATmega328P.SPH_ADDR) << 8) |
                        (0x000000FF & dataMemory.readByte(DataMemory_ATmega328P.SPL_ADDR));

//                Log.d("CALL", "STACK: " + Integer.toHexString(stackPointer));

                //Write PC low
                dataMemory.writeByte(stackPointer, (byte) (0x000000FF & programMemory.getPC()));
                stackPointer -= 1;
                //Write PC high
                dataMemory.writeByte(stackPointer, (byte) ((0x000000FF & (programMemory.getPC() >> 8))));
                stackPointer -= 1;

                //Update SPL
                dataMemory.writeByte(DataMemory_ATmega328P.SPL_ADDR, (byte) (0x000000FF & stackPointer));
                //Update SPH
                dataMemory.writeByte(DataMemory_ATmega328P.SPH_ADDR, (byte) ((0x0000FF00 & stackPointer) >> 8));

                programMemory.setPC(outData);

            }
        },
        INSTRUCTION_CBI {
            @Override
            public void executeInstruction() {
                /*************************CBI***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction CBI");

                if (!clockCycleDone) {
                    needMoreClockCycles = true;
                    clockCycleNeeded = 0;
                    return;
                }
                clockCycleDone = false;

                dataMemory.writeBit(((0x00F8 & instruction) >> 3) + 0x20, (0x0007 & instruction), false);
            }
        },
        INSTRUCTION_COM {
            @Override
            public void executeInstruction() {
                /*************************COM***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction COM");

                regD = dataMemory.readByte((0x01F0 & instruction) >> 4);
                regD = (byte) (0xFF - regD);
                dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);

                //Flag C
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 0, true);
                //Flag Z
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 1, regD == 0);
                //Flag N
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 2, !((0x0080 & regD) == 0));
                //Flag V
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 3, false);
                //Flag S
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 4,
                        dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 2) ^ dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 3));

            }
        },
        INSTRUCTION_CP {
            @Override
            public void executeInstruction() {
                /*************************CP***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction CP");

                regD = dataMemory.readByte((0x01F0 & instruction) >> 4);
                regR = dataMemory.readByte(((0x0200 & instruction) >> 5) | (0x000F & instruction));

                outData = regD - regR;

                //Flag H
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 5,
                        (0x00000001 &
                                ((((~(0x08 & regD) & (0x08 & regR)) |
                                        ((0x08 & outData) & (0x08 & regR)) |
                                        (~(0x08 & regD) & (0x08 & outData)))
                                        >> 3))) != 0);

                //Flag V
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 3,
                        (0x00000001 &
                                (((0x80 & regD) & (~(0x80 & regR)) & (~(0x80 & outData)) |
                                        (~(0x80 & regD)) & (0x80 & regR) & (0x80 & outData))
                                        >> 7)) != 0);

                //Flag N
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 2,
                        !((outData & 0x00000080) == 0));

                //Flag S
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 4,
                        dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 2) ^ dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 3));

                //Flag Z
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 1,
                        (outData & 0x000000FF) == 0);

                //Flag C
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 0,
                        (0x00000001 &
                                ((((~(0x80 & regD) & (0x80 & regR)) |
                                        ((0x80 & outData) & (0x80 & regR)) |
                                        (~(0x80 & regD) & (0x80 & outData)))
                                        >> 7))) != 0);
            }
        },
        INSTRUCTION_CPC {
            @Override
            public void executeInstruction() {
                /*************************CPC***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction CPC");

                regD = dataMemory.readByte((0x01F0 & instruction) >> 4);
                regR = dataMemory.readByte(((0x0200 & instruction) >> 5) | (0x000F & instruction));

                outData = regD - regR;
                //If carry is set
                if (dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 0)) {
                    outData -= 1;
                }

                //Flag H
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 5,
                        (0x00000001 &
                                ((((~(0x08 & regD) & (0x08 & regR)) |
                                        ((0x08 & outData) & (0x08 & regR)) |
                                        (~(0x08 & regD) & (0x08 & outData)))
                                        >> 3))) != 0);

                //Flag V
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 3,
                        (0x00000001 &
                                (((0x80 & regD) & (~(0x80 & regR)) & (~(0x80 & outData)) |
                                        (~(0x80 & regD)) & (0x80 & regR) & (0x80 & outData))
                                        >> 7)) != 0);

                //Flag N
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 2,
                        !((outData & 0x00000080) == 0));

                //Flag S
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 4,
                        dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 2) ^ dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 3));

                //Flag Z
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 1,
                        ((outData & 0x000000FF) == 0) & dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 1));

                //Flag C
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 0,
                        (0x00000001 &
                                ((((~(0x80 & regD) & (0x80 & regR)) |
                                        ((0x80 & outData) & (0x80 & regR)) |
                                        (~(0x80 & regD) & (0x80 & outData)))
                                        >> 7))) != 0);
            }
        },
        INSTRUCTION_CPI {
            @Override
            public void executeInstruction() {
                /*************************CPI***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction CPI");

                constValue = (((0x00000F00 & instruction) >> 4) | (0x0000000F & instruction));
                regD = dataMemory.readByte(0x10 | (0x000000F0 & instruction) >> 4);

                outData = (regD - constValue);

                //Flag H
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 5,
                        (0x00000001 &
                                ((((~(0x08 & regD) & (0x08 & constValue)) |
                                        ((0x08 & outData) & (0x08 & constValue)) |
                                        (~(0x08 & regD) & (0x08 & outData)))
                                        >> 3))) != 0);

                //Flag V
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 3,
                        (0x00000001 &
                                (((0x80 & regD) & (~(0x80 & constValue)) & (~(0x80 & outData)) |
                                        (~(0x80 & regD)) & (0x80 & constValue) & (0x80 & outData))
                                        >> 7)) != 0);

                //Flag N
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 2,
                        !((outData & 0x00000080) == 0));

                //Flag S
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 4,
                        dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 2) ^ dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 3));

                //Flag Z
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 1,
                        (outData & 0x000000FF) == 0);

                //Flag C
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 0,
                        (0x00000001 &
                                ((((~(0x80 & regD) & (0x80 & constValue)) |
                                        ((0x80 & outData) & (0x80 & constValue)) |
                                        (~(0x80 & regD) & (0x80 & outData)))
                                        >> 7))) != 0);

            }
        },
        INSTRUCTION_CPSE {
            @Override
            public void executeInstruction() {
                /*************************CPSE***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction CPSE");


                if (clockCycleDone){
                    clockCycleDone = false;
                    return;
                }

                regD = dataMemory.readByte((0x01F0 & instruction) >> 4);
                regR = dataMemory.readByte(((0x0200 & instruction) >> 5) | (0x000F & instruction));

                if (regD == regR) {

                    needMoreClockCycles = true;
                    clockCycleNeeded = 0;

                    instruction_tmp = programMemory.loadInstruction();

                    //Test 2 word instructions
                    //JMP
                    int testJMP_CALL = (0xFE0E & instruction_tmp);
                    int testLDS_STS = (0xFE0F & instruction_tmp);

                    if (testJMP_CALL == 0x940C ||       //JMP
                            testJMP_CALL == 0x940E ||   //CALL
                            testLDS_STS == 0x9000 ||    //LDS
                            testLDS_STS == 0x9200)      //STS
                    {
                        clockCycleNeeded += 1;
                        programMemory.addToPC(1);
                    }

                }

            }
        },
        INSTRUCTION_DEC {
            @Override
            public void executeInstruction() {
                /*************************DEC***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction DEC");

                regD = dataMemory.readByte((0x01F0 & instruction) >> 4);
                result = (byte) (regD - 1);

                dataMemory.writeByte((0x01F0 & instruction) >> 4, result);


                //Flag V
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 3,
                        ((result & 0x000000FF) == 0x7F));

                //Flag N
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 2,
                        !((result & 0x00000080) == 0));

                //Flag S
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 4,
                        dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 2) ^ dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 3));

                //Flag Z
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 1,
                        ((result & 0x000000FF) == 0));

            }
        },
        INSTRUCTION_EOR {//EOR - CLR

            @Override
            public void executeInstruction() {
                /*************************EOR/CLR***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction EOR/CLR");

                outAddress = (0x01F0 & instruction) >> 4;
                result = (byte) (dataMemory.readByte(outAddress) ^
                        dataMemory.readByte(((0x0200 & instruction) >> 5) | (0x000F & instruction)));

                dataMemory.writeByte(outAddress, result);

                //Update Status Register
                //Flag Z
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 1, result == 0);
                //Flag N
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 2, !((0x80 & result) == 0));
                //Flag V
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 3, false);
                //Flag S
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 4,
                        dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 2) ^ dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 3));

            }
        },
        INSTRUCTION_FMUL {
            @Override
            public void executeInstruction() {
                /*************************FMUL***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction FMUL");

                if (!clockCycleDone) {
                    needMoreClockCycles = true;
                    clockCycleNeeded = 0;
                    return;
                }
                clockCycleDone = false;

                regD = dataMemory.readByte(0x10 + ((0x0070 & instruction) >> 4));
                regR = dataMemory.readByte(0x10 + (0x0007 & instruction));

                outData = ((0x00FF & regD) * (0x00FF & regR)) << 1;

                dataMemory.writeByte(0x01, (byte) ((0x00FF00 & outData) >> 8));
                dataMemory.writeByte(0x00, (byte) (0x00FF & outData));

                //C
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 0, !((0x8000 & outData) == 0));
                //Z
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 1, outData == 0);
            }
        },
        INSTRUCTION_FMULS {
            @Override
            public void executeInstruction() {
                /*************************FMULS***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction FMULS");

                if (!clockCycleDone) {
                    needMoreClockCycles = true;
                    clockCycleNeeded = 0;
                    return;
                }
                clockCycleDone = false;

                regD = dataMemory.readByte(0x10 + ((0x0070 & instruction) >> 4));
                regR = dataMemory.readByte(0x10 + (0x0007 & instruction));

                outData = (regD * regR) << 1;

                dataMemory.writeByte(0x01, (byte) ((0x00FF00 & outData) >> 8));
                dataMemory.writeByte(0x00, (byte) (0x00FF & outData));

                //C
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 0, !((0x8000 & outData) == 0));
                //Z
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 1, outData == 0);
            }
        },
        INSTRUCTION_FMULSU {
            @Override
            public void executeInstruction() {
                /*************************FMULSU***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction FMULSU");

                if (!clockCycleDone) {
                    needMoreClockCycles = true;
                    clockCycleNeeded = 0;
                    return;
                }
                clockCycleDone = false;

                regD = dataMemory.readByte(0x10 + ((0x0070 & instruction) >> 4));
                regR = dataMemory.readByte(0x10 + (0x0007 & instruction));

                outData = (regD * (0x00FF & regR)) << 1;

                dataMemory.writeByte(0x01, (byte) ((0x00FF00 & outData) >> 8));
                dataMemory.writeByte(0x00, (byte) (0x00FF & outData));

                //C
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 0, !((0x8000 & outData) == 0));
                //Z
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 1, outData == 0);
            }
        },
        INSTRUCTION_ICALL {
            @Override
            public void executeInstruction() {
                /*************************ICALL***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction ICALL");

                //3 clockCycles
                if (!clockCycleDone) {
                    needMoreClockCycles = true;
                    clockCycleNeeded = 1;
                    return;
                }
                clockCycleDone = false;

                dataL = dataMemory.readByte(0x1E);
                dataH = dataMemory.readByte(0x1F);
                outData = (0x0000FF00 & (dataH << 8)) | (0x000000FF & dataL);

                //PC is already in position to go to stack (write little-endian)

                stackPointer = (dataMemory.readByte(DataMemory_ATmega328P.SPH_ADDR) << 8) |
                        (0x000000FF & dataMemory.readByte(DataMemory_ATmega328P.SPL_ADDR));

//                Log.d("ICALL", "STACK: " + Integer.toHexString(stackPointer));

                //Write PC low
                dataMemory.writeByte(stackPointer, (byte) (0x000000FF & programMemory.getPC()));
                stackPointer -= 1;
                //Write PC high
                dataMemory.writeByte(stackPointer, (byte) ((0x000000FF & (programMemory.getPC() >> 8))));
                stackPointer -= 1;

                //Update SPL
                dataMemory.writeByte(DataMemory_ATmega328P.SPL_ADDR, (byte) (0x000000FF & stackPointer));
                //Update SPH
                dataMemory.writeByte(DataMemory_ATmega328P.SPH_ADDR, (byte) ((0x0000FF00 & stackPointer) >> 8));

                programMemory.setPC(outData);
            }
        },
        INSTRUCTION_IJMP {
            @Override
            public void executeInstruction() {
                /*************************IJMP***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction IJMP");

                //3 clockCycles
                if (!clockCycleDone) {
                    needMoreClockCycles = true;
                    clockCycleNeeded = 1;
                    return;
                }
                clockCycleDone = false;

                dataL = dataMemory.readByte(0x1E);
                dataH = dataMemory.readByte(0x1F);
                outData = (0x0000FF00 & (dataH << 8)) | (0x000000FF & dataL);

                programMemory.setPC(outData);
            }
        },
        INSTRUCTION_IN {
            @Override
            public void executeInstruction() {
                /*************************IN***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction IN");
                dataMemory.writeByte(((0x01F0 & instruction) >> 4),                                              //Address
                        dataMemory.readByte(((((0x0600 & instruction) >> 5) | ((0x000F & instruction))) + 0x20)));//Data
            }
        },
        INSTRUCTION_INC {
            @Override
            public void executeInstruction() {
                /*************************INC***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction INC");

                regD = dataMemory.readByte((0x01F0 & instruction) >> 4);
                result = (byte) (regD + 1);

                dataMemory.writeByte((0x01F0 & instruction) >> 4, result);


                //Flag V
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 3,
                        ((result & 0x000000FF) == 0x80));

                //Flag N
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 2,
                        !((result & 0x00000080) == 0));

                //Flag S
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 4,
                        dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 2) ^ dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 3));

                //Flag Z
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 1,
                        ((result & 0x000000FF) == 0));

            }
        },
        INSTRUCTION_JMP {
            @Override
            public void executeInstruction() {
                /*************************JMP***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction JMP");

                //3 clockCycles
                if (!clockCycleDone) {
                    needMoreClockCycles = true;
                    clockCycleNeeded = 1;
                    return;
                }
                clockCycleDone = false;

                outData = (0x0001 & instruction) | ((0x01F0 & instruction) >> 3);
                instruction = programMemory.loadInstruction();
                outData = (outData << 16) | instruction;
                programMemory.setPC(outData);
            }
        },
        INSTRUCTION_LD_X_POST_INCREMENT {
            @Override
            public void executeInstruction() {
                /*************************LD (X Post Increment)***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction LD (X Post Increment)");

                if (!clockCycleDone) {
                    needMoreClockCycles = true;
                    clockCycleNeeded = 0;
                    return;
                }
                clockCycleDone = false;

                dataL = dataMemory.readByte(0x1A);
                dataH = dataMemory.readByte(0x1B);
                outAddress = (0x0000FF00 & (dataH << 8)) | (0x000000FF & dataL);

                dataMemory.writeByte((0x01F0 & instruction) >> 4, dataMemory.readByte(outAddress));

                outAddress += 1;
                dataMemory.writeByte(0x1A, (byte) (0x000000FF & outAddress));
                dataMemory.writeByte(0x1B, (byte) ((0x0000FF00 & outAddress) >> 8));
            }
        },
        INSTRUCTION_LD_X_PRE_DECREMENT {
            @Override
            public void executeInstruction() {
                /*************************LD (X Pre Decrement)***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction LD (X Pre Decrement)");

                if (!clockCycleDone) {
                    needMoreClockCycles = true;
                    clockCycleNeeded = 0;
                    return;
                }
                clockCycleDone = false;

                dataL = dataMemory.readByte(0x1A);
                dataH = dataMemory.readByte(0x1B);
                outAddress = (0x0000FF00 & (dataH << 8)) | (0x000000FF & dataL);

                outAddress -= 1;

                dataMemory.writeByte((0x01F0 & instruction) >> 4, dataMemory.readByte(outAddress));

                dataMemory.writeByte(0x1A, (byte) (0x000000FF & outAddress));
                dataMemory.writeByte(0x1B, (byte) ((0x0000FF00 & outAddress) >> 8));
            }
        },
        INSTRUCTION_LD_X_UNCHANGED {
            @Override
            public void executeInstruction() {
                /*************************LD (X unchanged)***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction LD (X Unchanged)");

                if (!clockCycleDone) {
                    needMoreClockCycles = true;
                    clockCycleNeeded = 0;
                    return;
                }
                clockCycleDone = false;

                dataL = dataMemory.readByte(0x1A);
                dataH = dataMemory.readByte(0x1B);
                outAddress = (0x0000FF00 & (dataH << 8)) | (0x000000FF & dataL);

                dataMemory.writeByte((0x01F0 & instruction) >> 4, dataMemory.readByte(outAddress));
            }
        },
        INSTRUCTION_LD_Y_POST_INCREMENT {
            @Override
            public void executeInstruction() {
                /*************************LD (Y Post Increment)***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction LD (Y Post Increment)");

                if (!clockCycleDone) {
                    needMoreClockCycles = true;
                    clockCycleNeeded = 0;
                    return;
                }
                clockCycleDone = false;

                dataL = dataMemory.readByte(0x1C);
                dataH = dataMemory.readByte(0x1D);
                outAddress = (0x0000FF00 & (dataH << 8)) | (0x000000FF & dataL);

                dataMemory.writeByte((0x01F0 & instruction) >> 4, dataMemory.readByte(outAddress));

                outAddress += 1;
                dataMemory.writeByte(0x1C, (byte) (0x000000FF & outAddress));
                dataMemory.writeByte(0x1D, (byte) ((0x0000FF00 & outAddress) >> 8));
            }
        },
        INSTRUCTION_LD_Y_PRE_DECREMENT {
            @Override
            public void executeInstruction() {
                /*************************LD (Y Pre Decrement)***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction LD (Y Pre Decrement)");

                if (!clockCycleDone) {
                    needMoreClockCycles = true;
                    clockCycleNeeded = 0;
                    return;
                }
                clockCycleDone = false;

                dataL = dataMemory.readByte(0x1C);
                dataH = dataMemory.readByte(0x1D);
                outAddress = (0x0000FF00 & (dataH << 8)) | (0x000000FF & dataL);

                outAddress -= 1;

                dataMemory.writeByte((0x01F0 & instruction) >> 4, dataMemory.readByte(outAddress));

                dataMemory.writeByte(0x1C, (byte) (0x000000FF & outAddress));
                dataMemory.writeByte(0x1D, (byte) ((0x0000FF00 & outAddress) >> 8));
            }
        },
        INSTRUCTION_LD_Y_UNCHANGED {
            @Override
            public void executeInstruction() {
                /*************************LD (Y Unchanged)***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction LD (Y Unchanged)");

                if (!clockCycleDone) {
                    needMoreClockCycles = true;
                    clockCycleNeeded = 0;
                    return;
                }
                clockCycleDone = false;

                dataL = dataMemory.readByte(0x1C);
                dataH = dataMemory.readByte(0x1D);
                outAddress = (0x0000FF00 & (dataH << 8)) | (0x000000FF & dataL);

                dataMemory.writeByte((0x01F0 & instruction) >> 4, dataMemory.readByte(outAddress));
            }
        },
        INSTRUCTION_LD_Z_POST_INCREMENT {
            @Override
            public void executeInstruction() {
                /*************************LD (Z Post Increment)***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction LD (Z Post Increment)");

                if (!clockCycleDone) {
                    needMoreClockCycles = true;
                    clockCycleNeeded = 0;
                    return;
                }
                clockCycleDone = false;

                dataL = dataMemory.readByte(0x1E);
                dataH = dataMemory.readByte(0x1F);
                outAddress = (0x0000FF00 & (dataH << 8)) | (0x000000FF & dataL);

                dataMemory.writeByte((0x01F0 & instruction) >> 4, dataMemory.readByte(outAddress));

                outAddress += 1;
                dataMemory.writeByte(0x1E, (byte) (0x000000FF & outAddress));
                dataMemory.writeByte(0x1F, (byte) ((0x0000FF00 & outAddress) >> 8));
            }
        },
        INSTRUCTION_LD_Z_PRE_DECREMENT {
            @Override
            public void executeInstruction() {
                /*************************LD (Z Pre Decrement)***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction LD (Z Pre Decrement)");

                if (!clockCycleDone) {
                    needMoreClockCycles = true;
                    clockCycleNeeded = 0;
                    return;
                }
                clockCycleDone = false;

                dataL = dataMemory.readByte(0x1E);
                dataH = dataMemory.readByte(0x1F);
                outAddress = (0x0000FF00 & (dataH << 8)) | (0x000000FF & dataL);

                outAddress -= 1;
                dataMemory.writeByte((0x01F0 & instruction) >> 4, dataMemory.readByte(outAddress));

                dataMemory.writeByte(0x1E, (byte) (0x000000FF & outAddress));
                dataMemory.writeByte(0x1F, (byte) ((0x0000FF00 & outAddress) >> 8));
            }
        },
        INSTRUCTION_LD_Z_UNCHANGED {
            @Override
            public void executeInstruction() {
                /*************************LD (Z unchanged)***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction LD (Z unchanged)");

                if (!clockCycleDone) {
                    needMoreClockCycles = true;
                    clockCycleNeeded = 0;
                    return;
                }
                clockCycleDone = false;

                dataL = dataMemory.readByte(0x1E);
                dataH = dataMemory.readByte(0x1F);
                outAddress = (0x0000FF00 & (dataH << 8)) | (0x000000FF & dataL);

                dataMemory.writeByte((0x01F0 & instruction) >> 4, dataMemory.readByte(outAddress));
            }
        },
        INSTRUCTION_LDD_Y {
            @Override
            public void executeInstruction() {
                /*************************LDD (Y)***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction LDD (Y)");

                if (!clockCycleDone) {
                    needMoreClockCycles = true;
                    clockCycleNeeded = 0;
                    return;
                }
                clockCycleDone = false;

                dataL = dataMemory.readByte(0x1C);
                dataH = dataMemory.readByte(0x1D);
                outAddress = (0x0000FF00 & (dataH << 8)) | (0x000000FF & dataL);

                outAddress += (((0x2000 & instruction) >> 8) | ((0x0C00 & instruction) >> 7) | (0x0007 & instruction));

                dataMemory.writeByte((0x01F0 & instruction) >> 4, dataMemory.readByte(outAddress));
            }
        },
        INSTRUCTION_LDD_Z {
            @Override
            public void executeInstruction() {
                /*************************LDD (Z)***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction LDD (Z)");

                if (!clockCycleDone) {
                    needMoreClockCycles = true;
                    clockCycleNeeded = 0;
                    return;
                }
                clockCycleDone = false;

                dataL = dataMemory.readByte(0x1E);
                dataH = dataMemory.readByte(0x1F);
                outAddress = (0x0000FF00 & (dataH << 8)) | (0x000000FF & dataL);

                outAddress += (((0x2000 & instruction) >> 8) | ((0x0C00 & instruction) >> 7) | (0x0007 & instruction));

                dataMemory.writeByte((0x01F0 & instruction) >> 4, dataMemory.readByte(outAddress));
            }
        },
        INSTRUCTION_LDI {//LDI - SER

            @Override
            public void executeInstruction() {
                /*************************LDI/SER***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction LDI/SER");
                dataMemory.writeByte(
                        (0x10 | (0x00F0 & instruction) >> 4),                               //Address
                        (byte) (((0x0F00 & instruction) >> 4) | (0x000F & instruction))     //Data
                );
            }
        },
        INSTRUCTION_LDS {
            @Override
            public void executeInstruction() {
                /*************************LDS***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction LDS");

                if (!clockCycleDone) {
                    needMoreClockCycles = true;
                    clockCycleNeeded = 0;
                    return;
                }
                clockCycleDone = false;

                outAddress = (0x0000FFFF & programMemory.loadInstruction());

                dataMemory.writeByte(((0x01F0 & instruction) >> 4),
                        dataMemory.readByte(outAddress));
            }
        },
        INSTRUCTION_LPM_Z_POST_INCREMENT {
            @Override
            public void executeInstruction() {
                /*************************LPM (Z Post Increment)***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction LPM (Z Post Increment)");

                if (!clockCycleDone) {
                    needMoreClockCycles = true;
                    clockCycleNeeded = 1;
                    return;
                }
                clockCycleDone = false;

                dataL = dataMemory.readByte(0x1E);
                dataH = dataMemory.readByte(0x1F);
                outAddress = (0x0000FF00 & (dataH << 8)) | (0x000000FF & dataL);

                dataMemory.writeByte((0x01F0 & instruction) >> 4, programMemory.readByte(outAddress));

                outAddress += 1;
                dataMemory.writeByte(0x1E, (byte) (0x000000FF & outAddress));
                dataMemory.writeByte(0x1F, (byte) ((0x0000FF00 & outAddress) >> 8));
            }
        },
        INSTRUCTION_LPM_Z_UNCHANGED_DEST_R0 {
            @Override
            public void executeInstruction() {
                /*************************LPM (Z Unchanged - Dest R0)***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction LPM (Z Unchanged - Dest R0)");

                if (!clockCycleDone) {
                    needMoreClockCycles = true;
                    clockCycleNeeded = 1;
                    return;
                }
                clockCycleDone = false;

                dataL = dataMemory.readByte(0x1E);
                dataH = dataMemory.readByte(0x1F);
                outAddress = (0x0000FF00 & (dataH << 8)) | (0x000000FF & dataL);

                dataMemory.writeByte(0, programMemory.readByte(outAddress));
            }
        },
        INSTRUCTION_LPM_Z_UNCHANGED {
            @Override
            public void executeInstruction() {
                /*************************LPM (Z Unchanged)***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction LPM (Z Unchanged)");

                if (!clockCycleDone) {
                    needMoreClockCycles = true;
                    clockCycleNeeded = 1;
                    return;
                }
                clockCycleDone = false;

                dataL = dataMemory.readByte(0x1E);
                dataH = dataMemory.readByte(0x1F);
                outAddress = (0x0000FF00 & (dataH << 8)) | (0x000000FF & dataL);

                dataMemory.writeByte((0x01F0 & instruction) >> 4, programMemory.readByte(outAddress));
            }
        },
        INSTRUCTION_LSR {
            @Override
            public void executeInstruction() {
                /*************************LSR***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction LSR");

                regD = dataMemory.readByte((0x01F0 & instruction) >> 4);

                result = (byte) (0x007F & (regD >> 1));

                dataMemory.writeByte((0x01F0 & instruction) >> 4, result);

                //Flag C
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 0,
                        !((regD & 0x00000001) == 0));

                //Flag Z
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 1,
                        ((result & 0x000000FF) == 0));

                //Flag N
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 2, false);

                //Flag V
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 3,
                        dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 2) ^ dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 0));

                //Flag S
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 4,
                        dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 2) ^ dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 3));

            }
        },
        INSTRUCTION_MOV {
            @Override
            public void executeInstruction() {
                /*************************MOV***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction MOV");

                dataMemory.writeByte(((0x01F0 & instruction) >> 4),
                        dataMemory.readByte(((0x0200 & instruction) >> 5) | (0x000F & instruction)));
            }
        },
        INSTRUCTION_MOVW {
            @Override
            public void executeInstruction() {
                /*************************MOVW***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction MOVW");

                dataMemory.writeByte((((0x00F0 & instruction) >> 4) << 1), dataMemory.readByte(((0x000F & instruction) << 1)));
                dataMemory.writeByte(((((0x00F0 & instruction) >> 4) << 1) + 1), dataMemory.readByte((((0x000F & instruction) << 1) + 1)));
            }
        },
        INSTRUCTION_MUL {
            @Override
            public void executeInstruction() {
                /*************************MUL***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction MUL");

                if (!clockCycleDone) {
                    needMoreClockCycles = true;
                    clockCycleNeeded = 0;
                    return;
                }
                clockCycleDone = false;

                regD = dataMemory.readByte((0x01F0 & instruction) >> 4);
                regR = dataMemory.readByte(((0x0200 & instruction) >> 5) | (0x000F & instruction));

                outData = ((0x00FF & regD) * (0x00FF & regR));

                dataMemory.writeByte(0x01, (byte) ((0x00FF00 & outData) >> 8));
                dataMemory.writeByte(0x00, (byte) (0x00FF & outData));

                //C
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 0, !((0x8000 & outData) == 0));
                //Z
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 1, outData == 0);
            }
        },
        INSTRUCTION_MULS {
            @Override
            public void executeInstruction() {
                /*************************MULS***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction MULS");

                if (!clockCycleDone) {
                    needMoreClockCycles = true;
                    clockCycleNeeded = 0;
                    return;
                }
                clockCycleDone = false;

                regD = dataMemory.readByte(0x10 + ((0x00F0 & instruction) >> 4));
                regR = dataMemory.readByte(0x10 + ((0x000F & instruction)));

                outData = (regD * regR);

                dataMemory.writeByte(0x01, (byte) ((0x00FF00 & outData) >> 8));
                dataMemory.writeByte(0x00, (byte) (0x00FF & outData));

                //C
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 0, !((0x8000 & outData) == 0));
                //Z
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 1, outData == 0);
            }
        },
        INSTRUCTION_MULSU {
            @Override
            public void executeInstruction() {
                /*************************MULSU***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction MULSU");

                if (!clockCycleDone) {
                    needMoreClockCycles = true;
                    clockCycleNeeded = 0;
                    return;
                }
                clockCycleDone = false;

                regD = dataMemory.readByte(0x10 + ((0x0070 & instruction) >> 4));
                regR = dataMemory.readByte(0x10 + ((0x0007 & instruction)));

                outData = (regD * (0x00FF & regR));

                dataMemory.writeByte(0x01, (byte) ((0x00FF00 & outData) >> 8));
                dataMemory.writeByte(0x00, (byte) (0x00FF & outData));

                //C
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 0, !((0x8000 & outData) == 0));
                //Z
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 1, outData == 0);
            }
        },
        INSTRUCTION_NEG {
            @Override
            public void executeInstruction() {
                /*************************NEG***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction NEG");

                regD = dataMemory.readByte((0x01F0 & instruction) >> 4);
                result = (byte) (0x00 - regD);
                dataMemory.writeByte((0x01F0 & instruction) >> 4, result);

                //Flag C
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 0, result != 0);
                //Flag Z
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 1, result == 0);
                //Flag N
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 2, !((0x80 & result) == 0));
                //Flag V
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 3, result == -128);
                //Flag S
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 4,
                        dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 2) ^ dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 3));

                //Flag H
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 5, (((0x08 & result) != 0) | ((0x08 & result) != 0)));


            }
        },
        INSTRUCTION_NOP {
            @Override
            public void executeInstruction() {
                /*************************NOP***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction NOP");
            }
        },
        INSTRUCTION_OR {
            @Override
            public void executeInstruction() {
                /*************************OR***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction OR");

                outAddress = (0x01F0 & instruction) >> 4;
                result = (byte) (dataMemory.readByte(outAddress) |
                        dataMemory.readByte(((0x0200 & instruction) >> 5) | (0x000F & instruction)));

                dataMemory.writeByte(outAddress, result);

                //Update Status Register
                //Flag Z
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 1, result == 0);
                //Flag N
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 2, !((0x80 & result) == 0));
                //Flag V
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 3, false);
                //Flag S
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 4,
                        dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 2) ^ dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 3));
            }
        },
        INSTRUCTION_ORI {//ORI - SBR

            @Override
            public void executeInstruction() {
                /*************************ORI/SBR***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction ORI/SBR");

                outAddress = (0x10 | (0x00F0 & instruction) >> 4);
                result = (byte) (dataMemory.readByte(outAddress) | (((0x0F00 & instruction) >> 4) | (0x000F & instruction)));

                dataMemory.writeByte(outAddress, result);

                //Update Flags
                //Flag Z
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 1, result == 0);
                //Flag N
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 2, !((0x80 & result) == 0));
                //Flag V
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 3, false);
                //Flag S
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 4,
                        dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 2) ^ dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 3));

            }
        },
        INSTRUCTION_OUT {
            @Override
            public void executeInstruction() {
                /*************************OUT***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction OUT");
                dataMemory.writeByte(
                        ((((0x0600 & instruction) >> 5) | ((0x000F & instruction))) + 0x20), //Address
                        dataMemory.readByte(((0x01F0 & instruction) >> 4))                   //Data
                );
            }
        },
        INSTRUCTION_POP {
            @Override
            public void executeInstruction() {
                /*************************POP***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction POP");

                if (!clockCycleDone) {
                    needMoreClockCycles = true;
                    clockCycleNeeded = 0;
                    return;
                }
                clockCycleDone = false;

                stackPointer = (dataMemory.readByte(DataMemory_ATmega328P.SPH_ADDR) << 8) |
                        (0x000000FF & dataMemory.readByte(DataMemory_ATmega328P.SPL_ADDR));

//                Log.d("POP", "STACK: " + Integer.toHexString(stackPointer));

                stackPointer += 1;
                result = dataMemory.readByte(stackPointer);

                dataMemory.writeByte((0x01F0 & instruction) >> 4, result);

                //Update SPL
                dataMemory.writeByte(DataMemory_ATmega328P.SPL_ADDR, (byte) (0x000000FF & stackPointer));
                //Update SPH
                dataMemory.writeByte(DataMemory_ATmega328P.SPH_ADDR, (byte) ((0x0000FF00 & stackPointer) >> 8));
            }
        },
        INSTRUCTION_PUSH {
            @Override
            public void executeInstruction() {
                /*************************PUSH***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction PUSH");

                if (!clockCycleDone) {
                    needMoreClockCycles = true;
                    clockCycleNeeded = 0;
                    return;
                }
                clockCycleDone = false;

                result = dataMemory.readByte((0x01F0 & instruction) >> 4);

                stackPointer = (dataMemory.readByte(DataMemory_ATmega328P.SPH_ADDR) << 8) |
                        (0x000000FF & dataMemory.readByte(DataMemory_ATmega328P.SPL_ADDR));

//                Log.d("PUSH", "STACK: " + Integer.toHexString(stackPointer));

                dataMemory.writeByte(stackPointer, result);
                stackPointer -= 1;

                //Update SPL
                dataMemory.writeByte(DataMemory_ATmega328P.SPL_ADDR, (byte) (0x000000FF & stackPointer));
                //Update SPH
                dataMemory.writeByte(DataMemory_ATmega328P.SPH_ADDR, (byte) ((0x0000FF00 & stackPointer) >> 8));
            }
        },
        INSTRUCTION_RCALL {
            @Override
            public void executeInstruction() {
                /*************************RCALL***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction RCALL");

                if (!clockCycleDone) {
                    needMoreClockCycles = true;
                    clockCycleNeeded = 1;
                    return;
                }
                clockCycleDone = false;

                outData = ((0x0FFF & instruction) << 20) >> 20;

                stackPointer = (dataMemory.readByte(DataMemory_ATmega328P.SPH_ADDR) << 8) |
                        (0x000000FF & dataMemory.readByte(DataMemory_ATmega328P.SPL_ADDR));

//                Log.d("RCALL", "STACK: " + Integer.toHexString(stackPointer));

                //Write PC low
                dataMemory.writeByte(stackPointer, (byte) (0x000000FF & programMemory.getPC()));
                stackPointer -= 1;
                //Write PC high
                dataMemory.writeByte(stackPointer, (byte) ((0x000000FF & (programMemory.getPC() >> 8))));
                stackPointer -= 1;

                //Update SPL
                dataMemory.writeByte(DataMemory_ATmega328P.SPL_ADDR, (byte) (0x000000FF & stackPointer));
                //Update SPH
                dataMemory.writeByte(DataMemory_ATmega328P.SPH_ADDR, (byte) ((0x0000FF00 & stackPointer) >> 8));

                programMemory.addToPC(outData);          //Make sign extension to get correct two complement
            }
        },
        INSTRUCTION_RET {
            @Override
            public void executeInstruction() {
                /*************************RET***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction RET");

                //4 clockCycles
                if (!clockCycleDone) {
                    needMoreClockCycles = true;
                    clockCycleNeeded = 2;
                    return;
                }
                clockCycleDone = false;

                stackPointer = (dataMemory.readByte(DataMemory_ATmega328P.SPH_ADDR) << 8) |
                        (0x000000FF & dataMemory.readByte(DataMemory_ATmega328P.SPL_ADDR));

//                Log.d("RET", "STACK: " + Integer.toHexString(stackPointer));

                //PC little endian read
                stackPointer += 1;
                dataH = dataMemory.readByte(stackPointer);
                stackPointer += 1;
                dataL = dataMemory.readByte(stackPointer);

                programMemory.setPC(((0x000000FF & dataH) << 8) | (0x000000FF & dataL));

                //Update SPL
                dataMemory.writeByte(DataMemory_ATmega328P.SPL_ADDR, (byte) (0x000000FF & stackPointer));
                //Update SPH
                dataMemory.writeByte(DataMemory_ATmega328P.SPH_ADDR, (byte) ((0x0000FF00 & stackPointer) >> 8));
            }
        },
        INSTRUCTION_RETI {
            @Override
            public void executeInstruction() {
                /*************************RETI***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction RETI");

                //4 clockCycles
                if (!clockCycleDone) {
                    needMoreClockCycles = true;
                    clockCycleNeeded = 2;
                    return;
                }
                clockCycleDone = false;

                stackPointer = (dataMemory.readByte(DataMemory_ATmega328P.SPH_ADDR) << 8) |
                        (0x000000FF & dataMemory.readByte(DataMemory_ATmega328P.SPL_ADDR));

//                Log.d("RETI", "STACK: " + Integer.toHexString(stackPointer));

                //PC little endian read
                stackPointer += 1;
                dataH = dataMemory.readByte(stackPointer);
                stackPointer += 1;
                dataL = dataMemory.readByte(stackPointer);

                programMemory.setPC(((0x000000FF & dataH) << 8) | (0x000000FF & dataL));

                //Update SPL
                dataMemory.writeByte(DataMemory_ATmega328P.SPL_ADDR, (byte) (0x000000FF & stackPointer));
                //Update SPH
                dataMemory.writeByte(DataMemory_ATmega328P.SPH_ADDR, (byte) ((0x0000FF00 & stackPointer) >> 8));

                UCModule.interruptionModule.enableGlobalInterruptions();
            }
        },
        INSTRUCTION_RJMP {
            @Override
            public void executeInstruction() {
                /*************************RJMP***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction RJMP");
                if (!clockCycleDone) {
                    needMoreClockCycles = true;
                    clockCycleNeeded = 0;
                    return;
                }
                clockCycleDone = false;

                programMemory.addToPC(((0x0FFF & instruction) << 20) >> 20);          //Make sign extension to get correct two complement
            }
        },
        INSTRUCTION_ROR {
            @Override
            public void executeInstruction() {
                /*************************ROR***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction ROR");

                regD = dataMemory.readByte((0x01F0 & instruction) >> 4);

                result = (byte) (0x007F & (regD >> 1));

                if (dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 0)) {
                    result = (byte) (result | 0x0080);
                }

                dataMemory.writeByte((0x01F0 & instruction) >> 4, result);

                //Flag C
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 0,
                        !((regD & 0x00000001) == 0));

                //Flag Z
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 1,
                        ((result & 0x000000FF) == 0));

                //Flag N
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 2,
                        !((result & 0x00000080) == 0));

                //Flag V
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 3,
                        dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 2) ^ dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 0));

                //Flag S
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 4,
                        dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 2) ^ dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 3));
            }
        },
        INSTRUCTION_SBC {
            @Override
            public void executeInstruction() {
                /*************************SBC***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction SBC");

                regD = dataMemory.readByte((0x01F0 & instruction) >> 4);
                regR = dataMemory.readByte(((0x0200 & instruction) >> 5) | (0x000F & instruction));
                result = (byte) (regD - regR);

                //If carry is set
                if (dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 0)) {
                    result -= 1;
                }

                dataMemory.writeByte(((0x01F0 & instruction) >> 4), result);

                //Flag H
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 5,
                        (0x00000001 &
                                (((((~(0x08 & regD)) & (0x08 & regR)) |
                                        ((0x08 & result) & (0x08 & regR)) |
                                        ((~(0x08 & regD)) & (0x08 & result)))
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
                                (((((~(0x80 & regD)) & (0x80 & regR)) |
                                        ((0x80 & result) & (0x80 & regR)) |
                                        ((~(0x80 & regD)) & (0x80 & result)))
                                        >> 7))) != 0);
            }
        },
        INSTRUCTION_SBCI {
            @Override
            public void executeInstruction() {
                /*************************SBCI***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction SBCI");

                regD = dataMemory.readByte(0x10 | (0x00F0 & instruction) >> 4);
                constValue = ((0x0F00 & instruction) >> 4) | (0x000F & instruction);
                result = (byte) (regD - constValue);

                //If carry is set
                if (dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 0)) {
                    result -= 1;
                }

                dataMemory.writeByte((0x10 | (0x00F0 & instruction) >> 4), result);

                //Flag H
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 5,
                        (0x00000001 &
                                (((((~(0x08 & regD)) & (0x08 & constValue)) |
                                        ((0x08 & result) & (0x08 & constValue)) |
                                        ((~(0x08 & regD)) & (0x08 & result)))
                                        >> 3))) != 0);

                //Flag V
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 3,
                        (0x00000001 &
                                (((0x80 & regD) & (~(0x80 & constValue)) & (~(0x80 & result)) |
                                        (~(0x80 & regD)) & (0x80 & constValue) & (0x80 & result))
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
                                (((((~(0x80 & regD)) & (0x80 & constValue)) |
                                        ((0x80 & result) & (0x80 & constValue)) |
                                        ((~(0x80 & regD)) & (0x80 & result)))
                                        >> 7))) != 0);
            }
        },
        INSTRUCTION_SBI {
            @Override
            public void executeInstruction() {
                /*************************SBI***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction SBI");
                if (!clockCycleDone) {
                    needMoreClockCycles = true;
                    clockCycleNeeded = 0;
                    return;
                }
                clockCycleDone = false;

                dataMemory.writeBit(((0x00F8 & instruction) >> 3) + 0x20, (0x0007 & instruction), true);
            }
        },
        INSTRUCTION_SBIC {
            @Override
            public void executeInstruction() {
                /*************************SBIC***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction SBIC");

                if (clockCycleDone){
                    clockCycleDone = false;
                    return;
                }

                if (!dataMemory.readBit(((0x00F8 & instruction) >> 3) + 0x20, (0x0007 & instruction))) {

                    needMoreClockCycles = true;
                    clockCycleNeeded = 0;

                    instruction_tmp = programMemory.loadInstruction();

                    //Test 2 word instructions
                    //JMP
                    testJMP_CALL = (0xFE0E & instruction_tmp);
                    testLDS_STS = (0xFE0F & instruction_tmp);

                    if (testJMP_CALL == 0x940C ||       //JMP
                            testJMP_CALL == 0x940E ||   //CALL
                            testLDS_STS == 0x9000 ||    //LDS
                            testLDS_STS == 0x9200)      //STS
                    {
                        clockCycleNeeded += 1;
                        programMemory.addToPC(1);
                    }
                }
            }
        },
        INSTRUCTION_SBIS {
            @Override
            public void executeInstruction() {
                /*************************SBIS***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction SBIS");

                if (clockCycleDone){
                    clockCycleDone = false;
                    return;
                }

                if (dataMemory.readBit(((0x00F8 & instruction) >> 3) + 0x20, (0x0007 & instruction))) {

                    needMoreClockCycles = true;
                    clockCycleNeeded = 0;

                    instruction_tmp = programMemory.loadInstruction();

                    //Test 2 word instructions
                    //JMP
                    testJMP_CALL = (0xFE0E & instruction_tmp);
                    testLDS_STS = (0xFE0F & instruction_tmp);

                    if (testJMP_CALL == 0x940C ||       //JMP
                            testJMP_CALL == 0x940E ||   //CALL
                            testLDS_STS == 0x9000 ||    //LDS
                            testLDS_STS == 0x9200)      //STS
                    {
                        clockCycleNeeded += 1;
                        programMemory.addToPC(1);
                    }
                }
            }
        },
        INSTRUCTION_SBIW {
            @Override
            public void executeInstruction() {
                /*************************SBIW***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction SBIW");
                if (!clockCycleDone) {
                    needMoreClockCycles = true;
                    clockCycleNeeded = 0;
                    return;
                }
                clockCycleDone = false;

                offset = (((0x0030 & instruction) >> 4) * 2);
                dataL = dataMemory.readByte(0x18 + offset);
                dataH = dataMemory.readByte(0x19 + offset);

                outData = (((0x00FF & dataH) << 8) | (0x00FF & dataL)) -
                        (((0x00C0 & instruction) >> 2) | (0x000F & instruction));

                dataMemory.writeByte(0x18 + offset, (byte) (0x000000FF & outData));
                dataMemory.writeByte(0x19 + offset, (byte) ((0x0000FF00 & outData) >> 8));

                //Flag C
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 0,
                        !(((~(dataH & 0x80)) & ((outData & 0x00008000) >> 8)) == 0));

                //Flag Z
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 1,
                        (outData & 0x0000FFFF) == 0);

                //Flag N
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 2,
                        !((outData & 0x00008000) == 0));

                //Flag V
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 3,
                        !(((dataH & 0x80) & ((~(outData & 0x00008000)) >> 8)) == 0));

                //Flag S
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 4,
                        dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 2) ^ dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 3));

            }
        },
        INSTRUCTION_SBRC {
            @Override
            public void executeInstruction() {
                /*************************SBRC***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction SBRC");

                if (clockCycleDone){
                    clockCycleDone = false;
                    return;
                }


                if (!dataMemory.readBit(((0x01F0 & instruction) >> 4), (0x0007 & instruction))) {

                    needMoreClockCycles = true;
                    clockCycleNeeded = 0;

                    instruction_tmp = programMemory.loadInstruction();

                    //Test 2 word instructions
                    //JMP
                    testJMP_CALL = (0xFE0E & instruction_tmp);
                    testLDS_STS = (0xFE0F & instruction_tmp);

                    if (testJMP_CALL == 0x940C ||       //JMP
                            testJMP_CALL == 0x940E ||   //CALL
                            testLDS_STS == 0x9000 ||    //LDS
                            testLDS_STS == 0x9200)      //STS
                    {
                        clockCycleNeeded += 1;
                        programMemory.addToPC(1);
                    }
                }
            }
        },
        INSTRUCTION_SBRS {
            @Override
            public void executeInstruction() {
                /*************************SBRS***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction SBRS");
                if (clockCycleDone){
                    clockCycleDone = false;
                    return;
                }

                if (dataMemory.readBit(((0x01F0 & instruction) >> 4), (0x0007 & instruction))) {

                    needMoreClockCycles = true;
                    clockCycleNeeded = 0;

                    instruction_tmp = programMemory.loadInstruction();

                    //Test 2 word instructions
                    //JMP
                    testJMP_CALL = (0xFE0E & instruction_tmp);
                    testLDS_STS = (0xFE0F & instruction_tmp);

                    if (testJMP_CALL == 0x940C ||       //JMP
                            testJMP_CALL == 0x940E ||   //CALL
                            testLDS_STS == 0x9000 ||    //LDS
                            testLDS_STS == 0x9200)      //STS
                    {
                        clockCycleNeeded += 1;
                        programMemory.addToPC(1);
                    }
                }
            }
        },
        INSTRUCTION_SLEEP {
            @Override
            public void executeInstruction() {
                Log.w(UCModule.MY_LOG_TAG, "Not implemented instruction: SLEEP");
            }
        },
        INSTRUCTION_SPM {
            @Override
            public void executeInstruction() {
                /*************************SPM***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction SPM");

                if (!clockCycleDone) {
                    needMoreClockCycles = true;
                    clockCycleNeeded = 1;
                    return;
                }
                clockCycleDone = false;

                dataL = dataMemory.readByte(0x1E);
                dataH = dataMemory.readByte(0x1F);
                outAddress = (0x0000FF00 & (dataH << 8)) | (0x000000FF & dataL);

                regD = dataMemory.readByte(0x01);
                regR = dataMemory.readByte(0x00);
                outData = (0x0000FF00 & (regD << 8)) | (0x000000FF & regR);

                programMemory.writeWord(outAddress, outData);
            }
        },
        INSTRUCTION_ST_X_POST_INCREMENT {
            @Override
            public void executeInstruction() {
                /*************************ST (X post increment)***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction ST (X post increment)");

                if (!clockCycleDone) {
                    needMoreClockCycles = true;
                    clockCycleNeeded = 0;
                    return;
                }
                clockCycleDone = false;

                dataL = dataMemory.readByte(0x1A);
                dataH = dataMemory.readByte(0x1B);
                outAddress = (0x0000FF00 & (dataH << 8)) | (0x000000FF & dataL);

                dataMemory.writeByte(outAddress, dataMemory.readByte((0x01F0 & instruction) >> 4));

                dataL += 1;
                if (dataL == 0x00) {
                    dataH += 1;
                }

                dataMemory.writeByte(0x1A, dataL);
                dataMemory.writeByte(0x1B, dataH);
            }
        },
        INSTRUCTION_ST_X_PRE_DECREMENT {
            @Override
            public void executeInstruction() {
                /*************************ST (X pre decrement)***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction ST (X Pre Decrement)");

                if (!clockCycleDone) {
                    needMoreClockCycles = true;
                    clockCycleNeeded = 0;
                    return;
                }
                clockCycleDone = false;

                dataL = dataMemory.readByte(0x1A);
                dataH = dataMemory.readByte(0x1B);

                if (dataL == 0x00) {
                    dataH -= 1;
                }
                dataL -= 1;

                outAddress = (0x0000FF00 & (dataH << 8)) | (0x000000FF & dataL);

                dataMemory.writeByte(outAddress, dataMemory.readByte((0x01F0 & instruction) >> 4));

                dataMemory.writeByte(0x1A, dataL);
                dataMemory.writeByte(0x1B, dataH);
            }
        },
        INSTRUCTION_ST_X_UNCHANGED {
            @Override
            public void executeInstruction() {
                /*************************ST (X unchanged)***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction ST (X unchanged)");

                if (!clockCycleDone) {
                    needMoreClockCycles = true;
                    clockCycleNeeded = 0;
                    return;
                }
                clockCycleDone = false;

                dataL = dataMemory.readByte(0x1A);
                dataH = dataMemory.readByte(0x1B);
                outAddress = (0x0000FF00 & (dataH << 8)) | (0x000000FF & dataL);

                dataMemory.writeByte(outAddress, dataMemory.readByte((0x01F0 & instruction) >> 4));
            }
        },
        INSTRUCTION_ST_Y_POST_INCREMENT {
            @Override
            public void executeInstruction() {
                /*************************ST (Y post increment)***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction ST (Y post increment)");

                if (!clockCycleDone) {
                    needMoreClockCycles = true;
                    clockCycleNeeded = 0;
                    return;
                }
                clockCycleDone = false;

                dataL = dataMemory.readByte(0x1C);
                dataH = dataMemory.readByte(0x1D);
                outAddress = (0x0000FF00 & (dataH << 8)) | (0x000000FF & dataL);

                dataMemory.writeByte(outAddress, dataMemory.readByte((0x01F0 & instruction) >> 4));

                dataL += 1;
                if (dataL == 0x00) {
                    dataH += 1;
                }

                dataMemory.writeByte(0x1C, dataL);
                dataMemory.writeByte(0x1D, dataH);
            }
        },
        INSTRUCTION_ST_Y_PRE_DECREMENT {
            @Override
            public void executeInstruction() {
                /*************************ST (Y pre decrement)***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction ST (Y Pre Decrement)");

                if (!clockCycleDone) {
                    needMoreClockCycles = true;
                    clockCycleNeeded = 0;
                    return;
                }
                clockCycleDone = false;

                dataL = dataMemory.readByte(0x1C);
                dataH = dataMemory.readByte(0x1D);

                if (dataL == 0x00) {
                    dataH -= 1;
                }
                dataL -= 1;

                outAddress = (0x0000FF00 & (dataH << 8)) | (0x000000FF & dataL);

                dataMemory.writeByte(outAddress, dataMemory.readByte((0x01F0 & instruction) >> 4));

                dataMemory.writeByte(0x1C, dataL);
                dataMemory.writeByte(0x1D, dataH);
            }
        },
        INSTRUCTION_ST_Y_UNCHANGED {
            @Override
            public void executeInstruction() {
                /*************************ST (X unchanged)***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction ST (X unchanged)");

                if (!clockCycleDone) {
                    needMoreClockCycles = true;
                    clockCycleNeeded = 0;
                    return;
                }
                clockCycleDone = false;

                dataL = dataMemory.readByte(0x1C);
                dataH = dataMemory.readByte(0x1D);
                outAddress = (0x0000FF00 & (dataH << 8)) | (0x000000FF & dataL);

                dataMemory.writeByte(outAddress, dataMemory.readByte((0x01F0 & instruction) >> 4));
            }
        },
        INSTRUCTION_ST_Z_POST_INCREMENT {
            @Override
            public void executeInstruction() {
                /*************************ST (Z Post Increment)***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction ST (Z Post Increment)");

                if (!clockCycleDone) {
                    needMoreClockCycles = true;
                    clockCycleNeeded = 0;
                    return;
                }
                clockCycleDone = false;

                dataL = dataMemory.readByte(0x1E);
                dataH = dataMemory.readByte(0x1F);
                outAddress = (0x0000FF00 & (dataH << 8)) | (0x000000FF & dataL);

                dataMemory.writeByte(outAddress, dataMemory.readByte((0x01F0 & instruction) >> 4));

                outAddress += 1;
                dataMemory.writeByte(0x1E, (byte) (0x000000FF & outAddress));
                dataMemory.writeByte(0x1F, (byte) ((0x0000FF00 & outAddress) >> 8));
            }
        },
        INSTRUCTION_ST_Z_PRE_DECREMENT {
            @Override
            public void executeInstruction() {
                /*************************ST (Z Pre Increment)***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction ST (Z Pre Decrement)");

                if (!clockCycleDone) {
                    needMoreClockCycles = true;
                    clockCycleNeeded = 0;
                    return;
                }
                clockCycleDone = false;

                dataL = dataMemory.readByte(0x1E);
                dataH = dataMemory.readByte(0x1F);
                outAddress = (0x0000FF00 & (dataH << 8)) | (0x000000FF & dataL);

                outAddress -= 1;

                dataMemory.writeByte(outAddress, dataMemory.readByte((0x01F0 & instruction) >> 4));

                dataMemory.writeByte(0x1E, (byte) (0x000000FF & outAddress));
                dataMemory.writeByte(0x1F, (byte) ((0x0000FF00 & outAddress) >> 8));
            }
        },
        INSTRUCTION_ST_Z_UNCHANGED {
            @Override
            public void executeInstruction() {
                /*************************ST (Z unchanged)***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction ST (Z unchanged)");

                if (!clockCycleDone) {
                    needMoreClockCycles = true;
                    clockCycleNeeded = 0;
                    return;
                }
                clockCycleDone = false;

                dataL = dataMemory.readByte(0x1E);
                dataH = dataMemory.readByte(0x1F);
                outAddress = (0x0000FF00 & (dataH << 8)) | (0x000000FF & dataL);

                dataMemory.writeByte(outAddress, dataMemory.readByte((0x01F0 & instruction) >> 4));
            }
        },
        INSTRUCTION_STD_Y {
            @Override
            public void executeInstruction() {
                /*************************STD (Y)***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction STD (Y)");

                if (!clockCycleDone) {
                    needMoreClockCycles = true;
                    clockCycleNeeded = 0;
                    return;
                }
                clockCycleDone = false;

                dataL = dataMemory.readByte(0x1C);
                dataH = dataMemory.readByte(0x1D);
                outAddress = (0x0000FF00 & (dataH << 8)) | (0x000000FF & dataL);

                outAddress += (((0x2000 & instruction) >> 8) | ((0x0C00 & instruction) >> 7) | (0x0007 & instruction));

                dataMemory.writeByte(outAddress, dataMemory.readByte((0x01F0 & instruction) >> 4));
            }
        },
        INSTRUCTION_STD_Z {
            @Override
            public void executeInstruction() {
                /*************************STD (Z)***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction STD (Z)");

                if (!clockCycleDone) {
                    needMoreClockCycles = true;
                    clockCycleNeeded = 0;
                    return;
                }
                clockCycleDone = false;

                dataL = dataMemory.readByte(0x1E);
                dataH = dataMemory.readByte(0x1F);
                outAddress = (0x0000FF00 & (dataH << 8)) | (0x000000FF & dataL);

                outAddress += (((0x2000 & instruction) >> 8) | ((0x0C00 & instruction) >> 7) | (0x0007 & instruction));

                dataMemory.writeByte(outAddress, dataMemory.readByte((0x01F0 & instruction) >> 4));
            }
        },
        INSTRUCTION_STS {
            @Override
            public void executeInstruction() {
                /*************************STS***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction STS");

                if (!clockCycleDone) {
                    needMoreClockCycles = true;
                    clockCycleNeeded = 0;
                    return;
                }
                clockCycleDone = false;

                outAddress = (0x0000FFFF & programMemory.loadInstruction());

                dataMemory.writeByte(outAddress,
                        dataMemory.readByte(((0x01F0 & instruction) >> 4)));
            }
        },
        INSTRUCTION_SUB {
            @Override
            public void executeInstruction() {
                /*************************SUB***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction SUB");

                regD = dataMemory.readByte((0x01F0 & instruction) >> 4);
                regR = dataMemory.readByte(((0x0200 & instruction) >> 5) | (0x000F & instruction));

                result = (byte) (regD - regR);

                dataMemory.writeByte((0x01F0 & instruction) >> 4, result);

                //Flag H
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 5,
                        (0x00000001 &
                                (((((~(0x08 & regD)) & (0x08 & regR)) |
                                        ((0x08 & result) & (0x08 & regR)) |
                                        ((~(0x08 & regD)) & (0x08 & result)))
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
                        ((result & 0x000000FF) == 0));

                //Flag C
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 0,
                        (0x00000001 &
                                (((((~(0x80 & regD)) & (0x80 & regR)) |
                                        ((0x80 & result) & (0x80 & regR)) |
                                        ((~(0x80 & regD)) & (0x80 & result)))
                                        >> 7))) != 0);
            }
        },
        INSTRUCTION_SUBI {
            @Override
            public void executeInstruction() {
                /*************************SUBI***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction SUBI");

                regD = dataMemory.readByte(0x10 | (0x00F0 & instruction) >> 4);
                constValue = ((0x0F00 & instruction) >> 4) | (0x000F & instruction);
                result = (byte) (regD - constValue);

                dataMemory.writeByte((0x10 | (0x00F0 & instruction) >> 4), result);

                //Flag H
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 5,
                        (0x00000001 &
                                (((((~(0x08 & regD)) & (0x08 & constValue)) |
                                        ((0x08 & result) & (0x08 & constValue)) |
                                        ((~(0x08 & regD)) & (0x08 & result)))
                                        >> 3))) != 0);

                //Flag V
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 3,
                        (0x00000001 &
                                (((0x80 & regD) & (~(0x80 & constValue)) & (~(0x80 & result)) |
                                        (~(0x80 & regD)) & (0x80 & constValue) & (0x80 & result))
                                        >> 7)) != 0);

                //Flag N
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 2,
                        !((result & 0x00000080) == 0));

                //Flag S
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 4,
                        dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 2) ^ dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 3));

                //Flag Z
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 1,
                        ((result & 0x000000FF) == 0));

                //Flag C
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 0,
                        (0x00000001 &
                                (((((~(0x80 & regD)) & (0x80 & constValue)) |
                                        ((0x80 & result) & (0x80 & constValue)) |
                                        ((~(0x80 & regD)) & (0x80 & result)))
                                        >> 7))) != 0);

            }
        },
        INSTRUCTION_SWAP {
            @Override
            public void executeInstruction() {
                /*************************SWAP***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction SWAP");

                regD = dataMemory.readByte((0x01F0 & instruction) >> 4);

                dataMemory.writeByte((0x01F0 & instruction) >> 4, (byte) ((regD << 4) | (0x0F & (regD >> 4))));
            }
        },
        INSTRUCTION_WDR {
            @Override
            public void executeInstruction() {
                Log.w(UCModule.MY_LOG_TAG, "Not implemented instruction: WDR");
            }
        },
        UNDEFINED_INSTRUCTION {
            @Override
            public void executeInstruction() {
                Log.w(UCModule.MY_LOG_TAG, "Undefined Instruction");
            }
        };

        public abstract void executeInstruction();
    }

}

