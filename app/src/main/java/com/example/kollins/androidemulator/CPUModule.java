package com.example.kollins.androidemulator;

import android.os.Handler;
import android.util.Log;

import com.example.kollins.androidemulator.ATmega328P.DataMemory_ATmega328P;
import com.example.kollins.androidemulator.uCInterfaces.CPUInstructions;
import com.example.kollins.androidemulator.uCInterfaces.DataMemory;
import com.example.kollins.androidemulator.uCInterfaces.ProgramMemory;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * Created by kollins on 3/9/18.
 */

public class CPUModule implements Runnable, CPUInstructions {

    public static int[] INSTRUCTION_ID = new int[(int) (Math.pow(2, 16) + 1)];

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
        Thread.currentThread().setName("CPU");
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

        while (!uCModule.getResetFlag()) {

            waitClock();

            /******************************Fetch*****************************/

            //Load instruction and add PC;
            instruction = programMemory.loadInstruction();
            Log.d(UCModule.MY_LOG_TAG, "Instruction loaded: " + Integer.toHexString(instruction));

            /*************************Decode and execute*******************/
            Executor.values()[INSTRUCTION_ID[instruction]].executeInstruction();

            if (UCModule.interruptionModule.haveInterruption()){

                Log.i("Interruption", "Handle Interruption");

                //Virtual CALL
                waitClock();
                waitClock();
                waitClock();
                waitClock();

                //PC is already in position to go to stack (write little-endian)
                int stackPointer = (dataMemory.readByte(DataMemory_ATmega328P.SPH_ADDR) << 8) |
                        (0x000000FF & dataMemory.readByte(DataMemory_ATmega328P.SPL_ADDR));


                //Write PC low
                stackPointer -= 1;
                dataMemory.writeByte(stackPointer, (byte) (0x000000FF & programMemory.getPC()));
                stackPointer -= 1;
                //Write PC high
                dataMemory.writeByte(stackPointer, (byte) ((0x000000FF & (programMemory.getPC() >> 8))));

                //Update SPL
                dataMemory.writeByte(DataMemory_ATmega328P.SPL_ADDR, (byte) (0x000000FF & stackPointer));
                //Update SPH
                dataMemory.writeByte(DataMemory_ATmega328P.SPH_ADDR, (byte) ((0x0000FF00 & stackPointer) >> 8));

                programMemory.setPC(UCModule.interruptionModule.getPCInterruptionAddress());
                UCModule.interruptionModule.disableGlobalInterruptions();
            }
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

    public enum Executor {
        INSTRUCTION_ADC {
            @Override
            public void executeInstruction() {
                /*************************ADC***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction ADC");

                byte regD = dataMemory.readByte((0x01F0 & instruction) >> 4);
                byte regR = dataMemory.readByte(((0x0200 & instruction) >> 5) | (0x000F & instruction));

                byte result = (byte) (regD + regR);

                //If carry is set
                if (dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 0)) {
                    result += 1;
                }

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
        INSTRUCTION_ADD {
            @Override
            public void executeInstruction() {
                /*************************ADD***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction ADD");

                byte regD = dataMemory.readByte((0x01F0 & instruction) >> 4);
                byte regR = dataMemory.readByte(((0x0200 & instruction) >> 5) | (0x000F & instruction));

                byte result = (byte) (regD + regR);

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
                waitClock();

                int offset_ADIW = (((0x0030 & instruction) >> 4) * 2);
                byte inDataL_ADIW = dataMemory.readByte(0x18 + offset_ADIW);
                byte inDataH_ADIW = dataMemory.readByte(0x19 + offset_ADIW);

                int outData_ADIW = (((0x00FF & inDataH_ADIW) << 8) | (0x00FF & inDataL_ADIW)) +
                        (((0x00C0 & instruction) >> 2) | (0x000F & instruction));

                dataMemory.writeByte(0x18 + offset_ADIW, (byte) (0x000000FF & outData_ADIW));
                dataMemory.writeByte(0x19 + offset_ADIW, (byte) ((0x0000FF00 & outData_ADIW) >> 8));

                //Update status register
                //Flag C
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 0,
                        !(((inDataH_ADIW & 0x80) & ((~(outData_ADIW & 0x00008000)) >> 8)) == 0));

                //Flag Z
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 1,
                        (outData_ADIW & 0x0000FFFF) == 0);

                //Flag N
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 2,
                        !((outData_ADIW & 0x00008000) == 0));

                //Flag V
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 3,
                        !(((~(inDataH_ADIW & 0x80)) & ((outData_ADIW & 0x00008000) >> 8)) == 0));

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

                int outAddressAND = (0x01F0 & instruction) >> 4;
                byte outDataAND = (byte) (dataMemory.readByte(outAddressAND) &
                        dataMemory.readByte(((0x0200 & instruction) >> 5) | (0x000F & instruction)));

                dataMemory.writeByte(outAddressAND, outDataAND);

                //Update Status Register
                //Flag Z
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 1, outDataAND == 0);
                //Flag N
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 2, !((0x80 & outDataAND) == 0));
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

                int Rd = (0x10 | (0x00F0 & instruction) >> 4);
                byte outData = (byte) (dataMemory.readByte(Rd) & (((0x0F00 & instruction) >> 4) | (0x000F & instruction)));

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
        INSTRUCTION_ASR {
            @Override
            public void executeInstruction() {
                Log.w(UCModule.MY_LOG_TAG, "Not implemented instruction: ASR");
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
                Log.w(UCModule.MY_LOG_TAG, "Not implemented instruction: BLD");
            }
        },
        INSTRUCTION_BRBC {
            @Override
            public void executeInstruction() {
                /*************************BRBC***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction BRBC");
                if (!dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, (0x0007 & instruction))) {
                    waitClock();
                    programMemory.addToPC(((0x03F8 & instruction) << 22) >> 25);          //Make sign extension to get correct two complement
                }
            }
        },
        INSTRUCTION_BRBS {
            @Override
            public void executeInstruction() {
                /*************************BRBS***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction BRBS");
                if (dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, (0x0007 & instruction))) {
                    waitClock();
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
                Log.w(UCModule.MY_LOG_TAG, "Not implemented instruction: BST");
            }
        },
        INSTRUCTION_CALL {
            @Override
            public void executeInstruction() {
                /*************************CALL***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction CALL");

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
                dataMemory.writeByte(stackPointer, (byte) ((0x000000FF & (programMemory.getPC() >> 8))));

                //Update SPL
                dataMemory.writeByte(DataMemory_ATmega328P.SPL_ADDR, (byte) (0x000000FF & stackPointer));
                //Update SPH
                dataMemory.writeByte(DataMemory_ATmega328P.SPH_ADDR, (byte) ((0x0000FF00 & stackPointer) >> 8));

                programMemory.setPC(callOut);

            }
        },
        INSTRUCTION_CBI {
            @Override
            public void executeInstruction() {
                /*************************CBI***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction CBI");
                waitClock();
                dataMemory.writeBit(((0x00F8 & instruction) >> 3) + 0x20, (0x0007 & instruction), false);
            }
        },
        INSTRUCTION_COM {
            @Override
            public void executeInstruction() {
                /*************************COM***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction COM");

                byte regValue = dataMemory.readByte((0x01F0 & instruction) >> 4);
                regValue = (byte) (0xFF - regValue);
                dataMemory.writeByte((0x01F0 & instruction) >> 4, regValue);

                //Flag C
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 0, true);
                //Flag Z
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 1, regValue == 0);
                //Flag N
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 2, !((0x80 & regValue) == 0));
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

                byte regD = dataMemory.readByte((0x01F0 & instruction) >> 4);
                byte regR = dataMemory.readByte(((0x0200 & instruction) >> 5) | (0x000F & instruction));

                int result = regD - regR;

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
                        (result & 0x000000FF) == 0);

                //Flag C
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 0,
                        (0x00000001 &
                                ((((~(0x80 & regD) & (0x80 & regR)) |
                                        ((0x80 & result) & (0x80 & regR)) |
                                        (~(0x80 & regD) & (0x80 & result)))
                                        >> 7))) != 0);
            }
        },
        INSTRUCTION_CPC {
            @Override
            public void executeInstruction() {
                /*************************CPC***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction CPC");

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
            }
        },
        INSTRUCTION_CPI {
            @Override
            public void executeInstruction() {
                /*************************CPI***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction CPI");

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
        INSTRUCTION_CPSE {
            @Override
            public void executeInstruction() {
                /*************************CPSE***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction CPSE");

                byte regD = dataMemory.readByte((0x01F0 & instruction) >> 4);
                byte regR = dataMemory.readByte(((0x0200 & instruction) >> 5) | (0x000F & instruction));

                if (regD == regR) {

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
            }
        },
        INSTRUCTION_DEC {
            @Override
            public void executeInstruction() {
                /*************************DEC***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction DEC");

                byte regD = dataMemory.readByte((0x01F0 & instruction) >> 4);
                byte result = (byte) (regD - 1);

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

                int outAddressEOR = (0x01F0 & instruction) >> 4;
                byte outDataEOR = (byte) (dataMemory.readByte(outAddressEOR) ^
                        dataMemory.readByte(((0x0200 & instruction) >> 5) | (0x000F & instruction)));

                dataMemory.writeByte(outAddressEOR, outDataEOR);

                //Update Status Register
                //Flag Z
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 1, outDataEOR == 0);
                //Flag N
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 2, !((0x80 & outDataEOR) == 0));
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
                Log.w(UCModule.MY_LOG_TAG, "Not implemented instruction: FMUL");
            }
        },
        INSTRUCTION_FMULS {
            @Override
            public void executeInstruction() {
                Log.w(UCModule.MY_LOG_TAG, "Not implemented instruction: FMULS");
            }
        },
        INSTRUCTION_FMULSU {
            @Override
            public void executeInstruction() {
                Log.w(UCModule.MY_LOG_TAG, "Not implemented instruction: FMULSU");
            }
        },
        INSTRUCTION_ICALL {
            @Override
            public void executeInstruction() {
                /*************************ICALL***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction ICALL");

                //3 clockCycles
                waitClock();
                waitClock();

                byte zRegL = dataMemory.readByte(0x1E);
                byte zRegH = dataMemory.readByte(0x1F);
                int callOut = (0x0000FF00 & (zRegH << 8)) | (0x000000FF & zRegL);

                //PC is already in position to go to stack (write little-endian)

                int stackPointer = (dataMemory.readByte(DataMemory_ATmega328P.SPH_ADDR) << 8) |
                        (0x000000FF & dataMemory.readByte(DataMemory_ATmega328P.SPL_ADDR));

                //Write PC low
                stackPointer -= 1;
                dataMemory.writeByte(stackPointer, (byte) (0x000000FF & programMemory.getPC()));
                stackPointer -= 1;
                //Write PC high
                dataMemory.writeByte(stackPointer, (byte) ((0x000000FF & (programMemory.getPC() >> 8))));

                //Update SPL
                dataMemory.writeByte(DataMemory_ATmega328P.SPL_ADDR, (byte) (0x000000FF & stackPointer));
                //Update SPH
                dataMemory.writeByte(DataMemory_ATmega328P.SPH_ADDR, (byte) ((0x0000FF00 & stackPointer) >> 8));

                programMemory.setPC(callOut);
            }
        },
        INSTRUCTION_IJMP {
            @Override
            public void executeInstruction() {
                Log.w(UCModule.MY_LOG_TAG, "Not implemented instruction: IJMP");
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

                byte regD = dataMemory.readByte((0x01F0 & instruction) >> 4);
                byte result = (byte) (regD + 1);

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
                waitClock();
                waitClock();

                int jumpOut = (0x0001 & instruction) | ((0x01F0 & instruction) >> 3);
                instruction = programMemory.loadInstruction();
                jumpOut = (jumpOut << 16) | instruction;

                programMemory.setPC(jumpOut);
            }
        },
        INSTRUCTION_LD_X_POST_INCREMENT {
            @Override
            public void executeInstruction() {
                /*************************LD (X Post Increment)***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction LD (X Post Increment)");

                waitClock();

                byte xRegL = dataMemory.readByte(0x1A);
                byte xRegH = dataMemory.readByte(0x1B);
                int destAddress = (0x0000FF00 & (xRegH << 8)) | (0x000000FF & xRegL);

                dataMemory.writeByte((0x01F0 & instruction) >> 4, dataMemory.readByte(destAddress));

                destAddress += 1;
                dataMemory.writeByte(0x1A, (byte) (0x000000FF & destAddress));
                dataMemory.writeByte(0x1B, (byte) ((0x0000FF00 & destAddress) >> 8));
            }
        },
        INSTRUCTION_LD_X_PRE_DECREMENT {
            @Override
            public void executeInstruction() {
                Log.w(UCModule.MY_LOG_TAG, "Not implemented instruction: LD_X_PRE_DECREMENT");
            }
        },
        INSTRUCTION_LD_X_UNCHANGED {
            @Override
            public void executeInstruction() {
                /*************************LD (X unchanged)***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction LD (X unchanged)");

                waitClock();

                byte xRegL = dataMemory.readByte(0x1A);
                byte xRegH = dataMemory.readByte(0x1B);
                int destAddress = (0x0000FF00 & (xRegH << 8)) | (0x000000FF & xRegL);

                dataMemory.writeByte((0x01F0 & instruction) >> 4, dataMemory.readByte(destAddress));
            }
        },
        INSTRUCTION_LD_Y_POST_INCREMENT {
            @Override
            public void executeInstruction() {
                Log.w(UCModule.MY_LOG_TAG, "Not implemented instruction: LD_Y_POST_INCREMENT");
            }
        },
        INSTRUCTION_LD_Y_PRE_DECREMENT {
            @Override
            public void executeInstruction() {
                Log.w(UCModule.MY_LOG_TAG, "Not implemented instruction: LD_X_PRE_DECREMENT");
            }
        },
        INSTRUCTION_LD_Y_UNCHANGED {
            @Override
            public void executeInstruction() {
                Log.w(UCModule.MY_LOG_TAG, "Not implemented instruction: LD_Y_UNCHANGED");
            }
        },
        INSTRUCTION_LD_Z_POST_INCREMENT {
            @Override
            public void executeInstruction() {
                /*************************LD (Z Post Increment)***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction LD (Z Post Increment)");

                waitClock();

                byte zRegL = dataMemory.readByte(0x1E);
                byte zRegH = dataMemory.readByte(0x1F);
                int destAddress = (0x0000FF00 & (zRegH << 8)) | (0x000000FF & zRegL);

                dataMemory.writeByte((0x01F0 & instruction) >> 4, dataMemory.readByte(destAddress));

                destAddress += 1;
                dataMemory.writeByte(0x1E, (byte) (0x000000FF & destAddress));
                dataMemory.writeByte(0x1F, (byte) ((0x0000FF00 & destAddress) >> 8));
            }
        },
        INSTRUCTION_LD_Z_PRE_DECREMENT {
            @Override
            public void executeInstruction() {
                Log.w(UCModule.MY_LOG_TAG, "Not implemented instruction: LD_Z_PRE_DECREMENT");
            }
        },
        INSTRUCTION_LD_Z_UNCHANGED {
            @Override
            public void executeInstruction() {
                /*************************LD (Z unchanged)***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction LD (Z unchanged)");

                waitClock();

                byte zRegL = dataMemory.readByte(0x1E);
                byte zRegH = dataMemory.readByte(0x1F);
                int destAddress = (0x0000FF00 & (zRegH << 8)) | (0x000000FF & zRegL);

                dataMemory.writeByte((0x01F0 & instruction) >> 4, dataMemory.readByte(destAddress));
            }
        },
        INSTRUCTION_LDD_Y {
            @Override
            public void executeInstruction() {
                Log.w(UCModule.MY_LOG_TAG, "Not implemented instruction: LDD_Y");
            }
        },
        INSTRUCTION_LDD_Z {
            @Override
            public void executeInstruction() {
                /*************************LDD (Z)***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction LDD (Z)");

                waitClock();

                byte zRegL = dataMemory.readByte(0x1E);
                byte zRegH = dataMemory.readByte(0x1F);
                int destAddress = (0x0000FF00 & (zRegH << 8)) | (0x000000FF & zRegL);

                destAddress += (((0x2000 & instruction)>>8) | ((0x0C00 & instruction)>>7) | (0x0007 & instruction));

                dataMemory.writeByte((0x01F0 & instruction) >> 4, dataMemory.readByte(destAddress));
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

                waitClock();

                int sdramDataAddress = (0x0000FFFF & programMemory.loadInstruction());

                dataMemory.writeByte(((0x01F0 & instruction) >> 4),
                        dataMemory.readByte(sdramDataAddress));
            }
        },
        INSTRUCTION_LPM_Z_POST_INCREMENT {
            @Override
            public void executeInstruction() {
                /*************************LPM (Z post-increment)***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction LPM (Z post-increment)");

                waitClock();
                waitClock();

                byte zRegL = dataMemory.readByte(0x1E);
                byte zRegH = dataMemory.readByte(0x1F);
                int destAddress = (0x0000FF00 & (zRegH << 8)) | (0x000000FF & zRegL);

                dataMemory.writeByte((0x01F0 & instruction) >> 4, programMemory.readByte(destAddress));

                destAddress += 1;
                dataMemory.writeByte(0x1E, (byte) (0x000000FF & destAddress));
                dataMemory.writeByte(0x1F, (byte) ((0x0000FF00 & destAddress) >> 8));
            }
        },
        INSTRUCTION_LPM_Z_UNCHANGED_DEST_R0 {
            @Override
            public void executeInstruction() {
                Log.w(UCModule.MY_LOG_TAG, "Not implemented instruction: LPM_Z_UNCHANGED_DEST_R0");
            }
        },
        INSTRUCTION_LPM_Z_UNCHANGED {
            @Override
            public void executeInstruction() {
                /*************************LPM (Z unchanged)***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction LPM (Z unchanged)");

                waitClock();
                waitClock();

                byte zRegL = dataMemory.readByte(0x1E);
                byte zRegH = dataMemory.readByte(0x1F);
                int destAddress = (0x0000FF00 & (zRegH << 8)) | (0x000000FF & zRegL);

                dataMemory.writeByte((0x01F0 & instruction) >> 4, programMemory.readByte(destAddress));
            }
        },
        INSTRUCTION_LSR {
            @Override
            public void executeInstruction() {
                Log.w(UCModule.MY_LOG_TAG, "Not implemented instruction: LSR");
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

                dataMemory.writeByte((((0x00F0 & instruction) >> 4)<<1), dataMemory.readByte(((0x000F & instruction)<<1)));
                dataMemory.writeByte(((((0x00F0 & instruction) >> 4)<<1) + 1), dataMemory.readByte((((0x000F & instruction)<<1) + 1)));
            }
        },
        INSTRUCTION_MUL {
            @Override
            public void executeInstruction() {
                Log.w(UCModule.MY_LOG_TAG, "Not implemented instruction: MUL");
            }
        },
        INSTRUCTION_MULS {
            @Override
            public void executeInstruction() {
                Log.w(UCModule.MY_LOG_TAG, "Not implemented instruction: MULS");
            }
        },
        INSTRUCTION_MULSU {
            @Override
            public void executeInstruction() {
                Log.w(UCModule.MY_LOG_TAG, "Not implemented instruction: MULSU");
            }
        },
        INSTRUCTION_NEG {
            @Override
            public void executeInstruction() {
                Log.w(UCModule.MY_LOG_TAG, "Not implemented instruction: NEG");
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

                int outAddressOR = (0x01F0 & instruction) >> 4;
                byte outDataOR = (byte) (dataMemory.readByte(outAddressOR) |
                        dataMemory.readByte(((0x0200 & instruction) >> 5) | (0x000F & instruction)));

                dataMemory.writeByte(outAddressOR, outDataOR);

                //Update Status Register
                //Flag Z
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 1, outDataOR == 0);
                //Flag N
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 2, !((0x80 & outDataOR) == 0));
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

                waitClock();

                int stackPointer = (dataMemory.readByte(DataMemory_ATmega328P.SPH_ADDR) << 8) |
                        (0x000000FF & dataMemory.readByte(DataMemory_ATmega328P.SPL_ADDR));

                byte value = dataMemory.readByte(stackPointer);
                stackPointer += 1;

                dataMemory.writeByte((0x01F0 & instruction) >> 4, value);

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

                waitClock();

                byte value = dataMemory.readByte((0x01F0 & instruction) >> 4);

                int stackPointer = (dataMemory.readByte(DataMemory_ATmega328P.SPH_ADDR) << 8) |
                        (0x000000FF & dataMemory.readByte(DataMemory_ATmega328P.SPL_ADDR));

                stackPointer -= 1;
                dataMemory.writeByte(stackPointer, value);

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
                waitClock();
                waitClock();

                int callOut = ((0x0FFF & instruction) << 20) >> 20;

                int stackPointer = (dataMemory.readByte(DataMemory_ATmega328P.SPH_ADDR) << 8) |
                        (0x000000FF & dataMemory.readByte(DataMemory_ATmega328P.SPL_ADDR));

                //Write PC low
                stackPointer -= 1;
                dataMemory.writeByte(stackPointer, (byte) (0x000000FF & programMemory.getPC()));
                stackPointer -= 1;
                //Write PC high
                dataMemory.writeByte(stackPointer, (byte) ((0x000000FF & (programMemory.getPC() >> 8))));

                //Update SPL
                dataMemory.writeByte(DataMemory_ATmega328P.SPL_ADDR, (byte) (0x000000FF & stackPointer));
                //Update SPH
                dataMemory.writeByte(DataMemory_ATmega328P.SPH_ADDR, (byte) ((0x0000FF00 & stackPointer) >> 8));

                programMemory.addToPC(callOut);          //Make sign extension to get correct two complement
            }
        },
        INSTRUCTION_RET {
            @Override
            public void executeInstruction() {
                /*************************RET***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction RET");

                //4 clockCycles
                waitClock();
                waitClock();
                waitClock();

                int stackPointer = (dataMemory.readByte(DataMemory_ATmega328P.SPH_ADDR) << 8) |
                        (0x000000FF & dataMemory.readByte(DataMemory_ATmega328P.SPL_ADDR));

                //PC little endian read
                byte pcHigh = dataMemory.readByte(stackPointer);
                stackPointer += 1;
                byte pcLow = dataMemory.readByte(stackPointer);
                stackPointer += 1;

                programMemory.setPC(((0x000000FF & pcHigh) << 8) | (0x000000FF & pcLow));

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
                waitClock();
                waitClock();
                waitClock();

                int stackPointer = (dataMemory.readByte(DataMemory_ATmega328P.SPH_ADDR) << 8) |
                        (0x000000FF & dataMemory.readByte(DataMemory_ATmega328P.SPL_ADDR));

                //PC little endian read
                byte pcHigh = dataMemory.readByte(stackPointer);
                stackPointer += 1;
                byte pcLow = dataMemory.readByte(stackPointer);
                stackPointer += 1;

                programMemory.setPC(((0x000000FF & pcHigh) << 8) | (0x000000FF & pcLow));

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
                waitClock();
                Log.d(UCModule.MY_LOG_TAG, "Instruction RJMP");
                programMemory.addToPC(((0x0FFF & instruction) << 20) >> 20);          //Make sign extension to get correct two complement
            }
        },
        INSTRUCTION_ROR {
            @Override
            public void executeInstruction() {
                Log.w(UCModule.MY_LOG_TAG, "Not implemented instruction: ROR");
            }
        },
        INSTRUCTION_SBC {
            @Override
            public void executeInstruction() {
                /*************************SBC***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction SBC");

                byte regD = dataMemory.readByte(0x10 | (0x00F0 & instruction) >> 4);
                byte regR = dataMemory.readByte(((0x0200 & instruction) >> 5) | (0x000F & instruction));
                byte result = (byte) (regD - regR);

                //If carry is set
                if (dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 0)) {
                    result -= 1;
                }

                dataMemory.writeByte((0x10 | (0x00F0 & instruction) >> 4), result);

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

                byte regD = dataMemory.readByte(0x10 | (0x00F0 & instruction) >> 4);
                int imediateValue = ((0x0F00 & instruction)>>4) | (0x000F & instruction);
                byte result = (byte) (regD - imediateValue);

                //If carry is set
                if (dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 0)) {
                    result -= 1;
                }

                dataMemory.writeByte((0x10 | (0x00F0 & instruction) >> 4), result);

                //Flag H
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 5,
                        (0x00000001 &
                                (((((~(0x08 & regD)) & (0x08 & imediateValue)) |
                                        ((0x08 & result) & (0x08 & imediateValue)) |
                                        ((~(0x08 & regD)) & (0x08 & result)))
                                        >> 3))) != 0);

                //Flag V
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 3,
                        (0x00000001 &
                                (((0x80 & regD) & (~(0x80 & imediateValue)) & (~(0x80 & result)) |
                                        (~(0x80 & regD)) & (0x80 & imediateValue) & (0x80 & result))
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
                                (((((~(0x80 & regD)) & (0x80 & imediateValue)) |
                                        ((0x80 & result) & (0x80 & imediateValue)) |
                                        ((~(0x80 & regD)) & (0x80 & result)))
                                        >> 7))) != 0);
            }
        },
        INSTRUCTION_SBI {
            @Override
            public void executeInstruction() {
                /*************************SBI***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction SBI");
                waitClock();
                dataMemory.writeBit(((0x00F8 & instruction) >> 3) + 0x20, (0x0007 & instruction), true);
            }
        },
        INSTRUCTION_SBIC {
            @Override
            public void executeInstruction() {
                Log.w(UCModule.MY_LOG_TAG, "Not implemented instruction: SBIC");
            }
        },
        INSTRUCTION_SBIS {
            @Override
            public void executeInstruction() {
                /*************************SBIS***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction SBIS");

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
            }
        },
        INSTRUCTION_SBIW {
            @Override
            public void executeInstruction() {
                /*************************SBIW***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction SBIW");
                waitClock();

                int offset_SBIW = (((0x0030 & instruction) >> 4) * 2);
                byte inDataL_SBIW = dataMemory.readByte(0x18 + offset_SBIW);
                byte inDataH_SBIW = dataMemory.readByte(0x19 + offset_SBIW);

                int outData_SBIW = (((0x00FF & inDataH_SBIW) << 8) | (0x00FF & inDataL_SBIW)) -
                        (((0x00C0 & instruction) >> 2) | (0x000F & instruction));

                dataMemory.writeByte(0x18 + offset_SBIW, (byte) (0x000000FF & outData_SBIW));
                dataMemory.writeByte(0x19 + offset_SBIW, (byte) ((0x0000FF00 & outData_SBIW) >> 8));

                //Flag C
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 0,
                        !(((~(inDataH_SBIW & 0x80)) & ((outData_SBIW & 0x00008000) >> 8)) == 0));

                //Flag Z
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 1,
                        (outData_SBIW & 0x0000FFFF) == 0);

                //Flag N
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 2,
                        !((outData_SBIW & 0x00008000) == 0));

                //Flag V
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 3,
                        !(((inDataH_SBIW & 0x80) & ((~(outData_SBIW & 0x00008000)) >> 8)) == 0));

                //Flag S
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 4,
                        dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 2) ^ dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 3));

            }
        },
        INSTRUCTION_SBRC {
            @Override
            public void executeInstruction() {
                Log.w(UCModule.MY_LOG_TAG, "Not implemented instruction: SBRC");
            }
        },
        INSTRUCTION_SBRS {
            @Override
            public void executeInstruction() {
                Log.w(UCModule.MY_LOG_TAG, "Not implemented instruction: SBRS");
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
                Log.w(UCModule.MY_LOG_TAG, "Not implemented instruction: SPM");
            }
        },
        INSTRUCTION_ST_X_POST_INCREMENT {
            @Override
            public void executeInstruction() {
                /*************************ST (X post increment)***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction ST (X post increment)");

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
            }
        },
        INSTRUCTION_ST_X_PRE_DECREMENT {
            @Override
            public void executeInstruction() {
                /*************************ST (X pre decrement)***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction ST (X Pre Decrement)");

                waitClock();

                byte xRegL = dataMemory.readByte(0x1A);
                byte xRegH = dataMemory.readByte(0x1B);
                int destAddress = (0x0000FF00 & (xRegH << 8)) | (0x000000FF & xRegL);

                xRegL -= 1;
                if (xRegL == 0x00) {
                    xRegH += 1;
                }

                dataMemory.writeByte(destAddress, dataMemory.readByte((0x01F0 & instruction) >> 4));

                dataMemory.writeByte(0x1A, xRegL);
                dataMemory.writeByte(0x1B, xRegH);
            }
        },
        INSTRUCTION_ST_X_UNCHANGED {
            @Override
            public void executeInstruction() {
                /*************************ST (X unchanged)***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction ST (X unchanged)");

                waitClock();

                byte xRegL = dataMemory.readByte(0x1A);
                byte xRegH = dataMemory.readByte(0x1B);
                int destAddress = (0x0000FF00 & (xRegH << 8)) | (0x000000FF & xRegL);

                dataMemory.writeByte(destAddress, dataMemory.readByte((0x01F0 & instruction) >> 4));
            }
        },
        INSTRUCTION_ST_Y_POST_INCREMENT {
            @Override
            public void executeInstruction() {
                Log.w(UCModule.MY_LOG_TAG, "Not implemented instruction: ST_Y_POST_INCREMENT");
            }
        },
        INSTRUCTION_ST_Y_PRE_DECREMENT {
            @Override
            public void executeInstruction() {
                Log.w(UCModule.MY_LOG_TAG, "Not implemented instruction: ST_Y_PRE_DECREMENT");
            }
        },
        INSTRUCTION_ST_Y_UNCHANGED {
            @Override
            public void executeInstruction() {
                Log.w(UCModule.MY_LOG_TAG, "Not implemented instruction: ST_Y_UNCHANGED");
            }
        },
        INSTRUCTION_ST_Z_POST_INCREMENT {
            @Override
            public void executeInstruction() {
                /*************************ST (Z Post Increment)***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction ST (Z Post Increment)");

                waitClock();

                byte zRegL = dataMemory.readByte(0x1E);
                byte zRegH = dataMemory.readByte(0x1F);
                int destAddress = (0x0000FF00 & (zRegH << 8)) | (0x000000FF & zRegL);

                dataMemory.writeByte(destAddress, dataMemory.readByte((0x01F0 & instruction) >> 4));

                destAddress += 1;
                dataMemory.writeByte(0x1E, (byte) (0x000000FF & destAddress));
                dataMemory.writeByte(0x1F, (byte) ((0x0000FF00 & destAddress) >> 8));
            }
        },
        INSTRUCTION_ST_Z_PRE_DECREMENT {
            @Override
            public void executeInstruction() {
                /*************************ST (Z Pre Increment)***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction ST (Z Pre Decrement)");

                waitClock();

                byte zRegL = dataMemory.readByte(0x1E);
                byte zRegH = dataMemory.readByte(0x1F);
                int destAddress = (0x0000FF00 & (zRegH << 8)) | (0x000000FF & zRegL);

                destAddress -= 1;

                dataMemory.writeByte(destAddress, dataMemory.readByte((0x01F0 & instruction) >> 4));

                dataMemory.writeByte(0x1E, (byte) (0x000000FF & destAddress));
                dataMemory.writeByte(0x1F, (byte) ((0x0000FF00 & destAddress) >> 8));
            }
        },
        INSTRUCTION_ST_Z_UNCHANGED {
            @Override
            public void executeInstruction() {
                /*************************ST (Z unchanged)***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction ST (Z unchanged)");

                waitClock();

                byte zRegL = dataMemory.readByte(0x1E);
                byte zRegH = dataMemory.readByte(0x1F);
                int destAddress = (0x0000FF00 & (zRegH << 8)) | (0x000000FF & zRegL);

                dataMemory.writeByte(destAddress, dataMemory.readByte((0x01F0 & instruction) >> 4));
            }
        },
        INSTRUCTION_STD_Y {
            @Override
            public void executeInstruction() {
                /*************************STD (Y)***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction STD (Y)");

                waitClock();

                byte yRegL = dataMemory.readByte(0x1C);
                byte yRegH = dataMemory.readByte(0x1D);
                int destAddress = (0x0000FF00 & (yRegH << 8)) | (0x000000FF & yRegL);

                destAddress += (((0x2000 & instruction)>>8) | ((0x0C00 & instruction)>>7) | (0x0007 & instruction));

                dataMemory.writeByte(destAddress, dataMemory.readByte((0x01F0 & instruction) >> 4));
            }
        },
        INSTRUCTION_STD_Z {
            @Override
            public void executeInstruction() {
                /*************************STD (Z)***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction STD (Z)");

                waitClock();

                byte zRegL = dataMemory.readByte(0x1E);
                byte zRegH = dataMemory.readByte(0x1F);
                int destAddress = (0x0000FF00 & (zRegH << 8)) | (0x000000FF & zRegL);

                destAddress += (((0x2000 & instruction)>>8) | ((0x0C00 & instruction)>>7) | (0x0007 & instruction));

                dataMemory.writeByte(destAddress, dataMemory.readByte((0x01F0 & instruction) >> 4));
            }
        },
        INSTRUCTION_STS {
            @Override
            public void executeInstruction() {
                /*************************STS***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction STS");

                waitClock();
                int sdramDataAddress = (0x0000FFFF & programMemory.loadInstruction());

                dataMemory.writeByte(sdramDataAddress,
                        dataMemory.readByte(((0x01F0 & instruction) >> 4)));
            }
        },
        INSTRUCTION_SUB {
            @Override
            public void executeInstruction() {
                /*************************SUB***********************/
                Log.d(UCModule.MY_LOG_TAG, "Instruction SUB");

                byte regD = dataMemory.readByte((0x01F0 & instruction) >> 4);
                byte regR = dataMemory.readByte(((0x0200 & instruction) >> 5) | (0x000F & instruction));

                byte result = (byte) (regD - regR);

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

                byte regD = dataMemory.readByte(0x10 | (0x00F0 & instruction) >> 4);
                int imediateValue = ((0x0F00 & instruction)>>4) | (0x000F & instruction);
                byte result = (byte) (regD - imediateValue);

                dataMemory.writeByte((0x10 | (0x00F0 & instruction) >> 4), result);

                //Flag H
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 5,
                        (0x00000001 &
                                (((((~(0x08 & regD)) & (0x08 & imediateValue)) |
                                        ((0x08 & result) & (0x08 & imediateValue)) |
                                        ((~(0x08 & regD)) & (0x08 & result)))
                                        >> 3))) != 0);

                //Flag V
                dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 3,
                        (0x00000001 &
                                (((0x80 & regD) & (~(0x80 & imediateValue)) & (~(0x80 & result)) |
                                        (~(0x80 & regD)) & (0x80 & imediateValue) & (0x80 & result))
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
                                (((((~(0x80 & regD)) & (0x80 & imediateValue)) |
                                        ((0x80 & result) & (0x80 & imediateValue)) |
                                        ((~(0x80 & regD)) & (0x80 & result)))
                                        >> 7))) != 0);

            }
        },
        INSTRUCTION_SWAP {
            @Override
            public void executeInstruction() {
                Log.w(UCModule.MY_LOG_TAG, "Not implemented instruction: SWAP");
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

