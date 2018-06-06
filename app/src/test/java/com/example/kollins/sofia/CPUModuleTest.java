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

package com.example.kollins.sofia;

import android.os.Handler;

import com.example.kollins.sofia.atmega328p.DataMemory_ATmega328P;
import com.example.kollins.sofia.atmega328p.ProgramMemory_ATmega328P;
import com.example.kollins.sofia.ucinterfaces.DataMemory;
import com.example.kollins.sofia.ucinterfaces.IOModule;
import com.example.kollins.sofia.ucinterfaces.ProgramMemory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.lang.reflect.Field;
import java.util.concurrent.locks.Lock;

import static org.mockito.Mockito.mock;
import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CPUModule.class, ProgramMemory.class, DataMemory.class})
public class CPUModuleTest {

    private Field cpuField;

    private ProgramMemory programMemory;
    private DataMemory dataMemory;
    private CPUModule cpuModule;

    private int instruction;
    private byte regD,regR;

    @Mock
    private static IOModule ioModule;

    @Before
    public void prepareForTest() throws Exception {
        PowerMockito.spy(CPUModule.class);
        PowerMockito.doNothing().when(CPUModule.class, "waitClock");

        dataMemory = new DataMemory_ATmega328P(ioModule);
        dataMemory.stopTimer();

        UCModule uCModule = mock(UCModule.class);
        Handler uCHandler = mock(Handler.class);
        Lock clockLock = PowerMockito.mock(Lock.class);

        programMemory = new ProgramMemory_ATmega328P(uCHandler);
        cpuModule = new CPUModule(programMemory,dataMemory,uCModule,uCHandler,clockLock);

        cpuField = CPUModule.class.getDeclaredField("instruction");
        cpuField.setAccessible(true);

        Whitebox.setInternalState(dataMemory,"updateMemMapFlag", false);
    }

    @Test
    public void ADC_SumZeroNoCarry_Return0() throws IllegalAccessException {

        instruction = 0x00FE;
        cpuField.set(null, instruction);

        regD = 0;
        regR = 0;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);
        dataMemory.writeByte(((0x0200 & instruction) >> 5) | (0x000F & instruction), regR);
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 0, false);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_ADC.executeInstruction();

        //Read Result
        assertEquals(0, dataMemory.readByte((0x01F0 & instruction) >> 4));

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,5));//H
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1)); //Z
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void ADC_SumZeroAndCarry_Return1() throws IllegalAccessException {

        instruction = 0x00FE;
        cpuField.set(null, instruction);

        regD = 0;
        regR = 0;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);
        dataMemory.writeByte(((0x0200 & instruction) >> 5) | (0x000F & instruction), regR);
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 0, true);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_ADC.executeInstruction();

        //Read Result
        assertEquals(1, dataMemory.readByte((0x01F0 & instruction) >> 4));

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,5));//H
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void ADC_SumOverflowNoCarry_Return0() throws IllegalAccessException {

        instruction = 0x00FE;
        cpuField.set(null, instruction);

        regD = -1;
        regR = 1;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);
        dataMemory.writeByte(((0x0200 & instruction) >> 5) | (0x000F & instruction), regR);
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 0, false);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_ADC.executeInstruction();

        //Read Result
        assertEquals(0, dataMemory.readByte((0x01F0 & instruction) >> 4));

        //Read Flags
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,5));//H
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void ADC_SumTwoComplementOverflowWithCarry_ReturnNeg128() throws IllegalAccessException {

        instruction = 0x00FE;
        cpuField.set(null, instruction);

        regD = 0x7F;
        regR = 0;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);
        dataMemory.writeByte(((0x0200 & instruction) >> 5) | (0x000F & instruction), regR);
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 0, true);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_ADC.executeInstruction();

        //Read Result
        assertEquals(-128, dataMemory.readByte((0x01F0 & instruction) >> 4));

        //Read Flags
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,5));//H
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void ADC_SumTwoComplementWithoutOverflowNoCarry_ReturnNeg127() throws IllegalAccessException {

        instruction = 0x00FE;
        cpuField.set(null, instruction);

        regD = -128;
        regR = 1;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);
        dataMemory.writeByte(((0x0200 & instruction) >> 5) | (0x000F & instruction), regR);
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 0, false);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_ADC.executeInstruction();

        //Read Result
        assertEquals(-127, dataMemory.readByte((0x01F0 & instruction) >> 4));

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,5));//H
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void ADD_SumZeroAndOne_Return1() throws IllegalAccessException {

        instruction = 0x00FE;
        cpuField.set(null, instruction);

        regD = 0;
        regR = 1;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);
        dataMemory.writeByte(((0x0200 & instruction) >> 5) | (0x000F & instruction), regR);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_ADD.executeInstruction();

        //Read Result
        assertEquals(1, dataMemory.readByte((0x01F0 & instruction) >> 4));

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,5));//H
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void ADD_ShiftLeft_Return2() throws IllegalAccessException {

        instruction = 0x00FF;
        cpuField.set(null, instruction);

        regD = 1;
        regR = 1;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);
        dataMemory.writeByte(((0x0200 & instruction) >> 5) | (0x000F & instruction), regR);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_ADD.executeInstruction();

        //Read Result
        assertEquals(2, dataMemory.readByte((0x01F0 & instruction) >> 4));

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,5));//H
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void ADD_SumOverflow_Return0() throws IllegalAccessException {

        instruction = 0x00FE;
        cpuField.set(null, instruction);

        regD = -1;
        regR = 1;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);
        dataMemory.writeByte(((0x0200 & instruction) >> 5) | (0x000F & instruction), regR);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_ADD.executeInstruction();

        //Read Result
        assertEquals(0, dataMemory.readByte((0x01F0 & instruction) >> 4));

        //Read Flags
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,5));//H
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void ADD_SumTwoComplementOverflow_ReturnNeg128() throws IllegalAccessException {

        instruction = 0x00FE;
        cpuField.set(null, instruction);

        regD = 0x7F;
        regR = 1;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);
        dataMemory.writeByte(((0x0200 & instruction) >> 5) | (0x000F & instruction), regR);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_ADD.executeInstruction();

        //Read Result
        assertEquals(-128, dataMemory.readByte((0x01F0 & instruction) >> 4));

        //Read Flags
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,5));//H
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void ADD_SumTwoComplementWithoutOverflow_ReturnNeg127() throws IllegalAccessException {

        instruction = 0x00FE;
        cpuField.set(null, instruction);

        regD = -128;
        regR = 1;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);
        dataMemory.writeByte(((0x0200 & instruction) >> 5) | (0x000F & instruction), regR);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_ADD.executeInstruction();

        //Read Result
        assertEquals(-127, dataMemory.readByte((0x01F0 & instruction) >> 4));

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,5));//H
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void ADIW_SumZero24And25_Return0And0() throws Exception {

        instruction = 0x0000;
        cpuField.set(null, instruction);

        //Run Instruction
//        PowerMockito.spy(CPUModule.class);
//        PowerMockito.doNothing().when(CPUModule.class, "waitClock");
        CPUModule.Executor.INSTRUCTION_ADIW.executeInstruction();

        //Read Result
        assertEquals(0, dataMemory.readByte(24));
        assertEquals(0, dataMemory.readByte(25));

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void ADIW_SumOneTwoComplementOverflow26And27_ReturnNeg128And0() throws Exception {

        instruction = 0x0011;
        cpuField.set(null, instruction);

        //Fill memory
        dataMemory.writeByte(26, (byte) -1);
        dataMemory.writeByte(27, (byte) 127);

        //Run Instruction
//        PowerMockito.spy(CPUModule.class);
//        PowerMockito.doNothing().when(CPUModule.class, "waitClock");
        CPUModule.Executor.INSTRUCTION_ADIW.executeInstruction();

        //Read Result
        assertEquals(0, dataMemory.readByte(26));
        assertEquals(-128, dataMemory.readByte(27));

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void ADIW_SumOneOverflow28And29_Return0And0() throws Exception {

        instruction = 0x0021;
        cpuField.set(null, instruction);

        //Fill memory
        dataMemory.writeByte(28, (byte) -1);
        dataMemory.writeByte(29, (byte) -1);

        //Run Instruction
//        PowerMockito.spy(CPUModule.class);
//        PowerMockito.doNothing().when(CPUModule.class, "waitClock");
        CPUModule.Executor.INSTRUCTION_ADIW.executeInstruction();

        //Read Result
        assertEquals(0, dataMemory.readByte(28));
        assertEquals(0, dataMemory.readByte(29));

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void ADIW_SumTen30And31_ReturnNeg128And10() throws Exception {

        instruction = 0x003A;
        cpuField.set(null, instruction);

        //Fill memory
        dataMemory.writeByte(30, (byte) 0);
        dataMemory.writeByte(31, (byte) -1);

        //Run Instruction
//        PowerMockito.spy(CPUModule.class);
//        PowerMockito.doNothing().when(CPUModule.class, "waitClock");
        CPUModule.Executor.INSTRUCTION_ADIW.executeInstruction();

        //Read Result
        assertEquals(10, dataMemory.readByte(30));
        assertEquals(-1, dataMemory.readByte(31));

        //Read Flags
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void AND_ZeroAndZero_Return0() throws IllegalAccessException {

        instruction = 0x00FE;
        cpuField.set(null, instruction);

        regD = 0;
        regR = 0;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);
        dataMemory.writeByte(((0x0200 & instruction) >> 5) | (0x000F & instruction), regR);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_AND.executeInstruction();

        //Read Result
        assertEquals(0, dataMemory.readByte((0x01F0 & instruction) >> 4));

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z

    }

    @Test
    public void AND_ZeroAndFF_Return0() throws IllegalAccessException {

        instruction = 0x00FE;
        cpuField.set(null, instruction);

        regD = 0;
        regR = -1;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);
        dataMemory.writeByte(((0x0200 & instruction) >> 5) | (0x000F & instruction), regR);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_AND.executeInstruction();

        //Read Result
        assertEquals(0, dataMemory.readByte((0x01F0 & instruction) >> 4));

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z

    }

    @Test
    public void AND_FFAndF0_ReturnF0() throws IllegalAccessException {

        instruction = 0x00FE;
        cpuField.set(null, instruction);

        regD = -16;//0xF0
        regR = -1;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);
        dataMemory.writeByte(((0x0200 & instruction) >> 5) | (0x000F & instruction), regR);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_AND.executeInstruction();

        //Read Result
        assertEquals(-16, dataMemory.readByte((0x01F0 & instruction) >> 4));

        //Read Flags
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z

    }

    @Test
    public void ANDI_ZeroAndZero_Return0() throws IllegalAccessException {

        instruction = 0x00F0;
        cpuField.set(null, instruction);

        regD = 0;

        //Fill Memory
        dataMemory.writeByte((0x10 | (0x00F0 & instruction) >> 4), regD);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_ANDI.executeInstruction();

        //Read Result
        assertEquals(0, dataMemory.readByte((0x10 | (0x00F0 & instruction) >> 4)));

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z

    }

    @Test
    public void ANDI_ZeroAndFF_Return0() throws IllegalAccessException {

        instruction = 0x0FFF;
        cpuField.set(null, instruction);

        regD = 0;

        //Fill Memory
        dataMemory.writeByte((0x10 | (0x00F0 & instruction) >> 4), regD);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_ANDI.executeInstruction();

        //Read Result
        assertEquals(0, dataMemory.readByte((0x10 | (0x00F0 & instruction) >> 4)));

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z

    }

    @Test
    public void ANDI_FFAndF0_ReturnF0() throws IllegalAccessException {

        instruction = 0x0FF0;
        cpuField.set(null, instruction);

        regD = -1;

        //Fill Memory
        dataMemory.writeByte((0x10 | (0x00F0 & instruction) >> 4), regD);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_ANDI.executeInstruction();

        //Read Result
        assertEquals(-16, dataMemory.readByte((0x10 | (0x00F0 & instruction) >> 4)));

        //Read Flags
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z

    }

    @Test
    public void ASR_ShiftTwo_Return1() throws IllegalAccessException {

        instruction = 0x00F0;
        cpuField.set(null, instruction);

        regD = 2;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_ASR.executeInstruction();

        //Read Result
        assertEquals(1, dataMemory.readByte((0x01F0 & instruction) >> 4));

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void ASR_ShiftOne_Return0() throws IllegalAccessException {

        instruction = 0x00F0;
        cpuField.set(null, instruction);

        regD = 1;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_ASR.executeInstruction();

        //Read Result
        assertEquals(0, dataMemory.readByte((0x01F0 & instruction) >> 4));

        //Read Flags
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void ASR_SignExtention_Return0() throws IllegalAccessException {

        instruction = 0x00F0;
        cpuField.set(null, instruction);

        regD = -128;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_ASR.executeInstruction();

        //Read Result
        assertEquals(-64, dataMemory.readByte((0x01F0 & instruction) >> 4));

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void BCLR_ClearI() throws IllegalAccessException {

        instruction = 0x00F0;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 7, true);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_BCLR.executeInstruction();

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,7));//I
    }

    @Test
    public void BCLR_ClearT() throws IllegalAccessException {

        instruction = 0x00E0;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 6, true);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_BCLR.executeInstruction();

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,6));//T
    }

    @Test
    public void BCLR_ClearH() throws IllegalAccessException {

        instruction = 0x00D0;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 5, true);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_BCLR.executeInstruction();

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,5));//H
    }

    @Test
    public void BCLR_ClearS() throws IllegalAccessException {

        instruction = 0x00C0;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 4, true);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_BCLR.executeInstruction();

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
    }

    @Test
    public void BCLR_ClearV() throws IllegalAccessException {

        instruction = 0x00B0;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 3, true);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_BCLR.executeInstruction();

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
    }

    @Test
    public void BCLR_ClearN() throws IllegalAccessException {

        instruction = 0x00A0;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 2, true);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_BCLR.executeInstruction();

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
    }

    @Test
    public void BCLR_ClearZ() throws IllegalAccessException {

        instruction = 0x0090;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 1, true);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_BCLR.executeInstruction();

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
    }

    @Test
    public void BCLR_ClearC() throws IllegalAccessException {

        instruction = 0x0080;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 0, true);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_BCLR.executeInstruction();

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C
    }

    @Test
    public void BLD_MoveTrueToPositionOne_Return2() throws IllegalAccessException {

        instruction = 0x00F1;
        cpuField.set(null, instruction);

        regD = 0;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 6, true);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_BLD.executeInstruction();

        //Read Result
        assertEquals(2, dataMemory.readByte((0x01F0 & instruction) >> 4));

    }

    @Test
    public void BLD_MoveFalseToPositionZero_Return6() throws IllegalAccessException {

        instruction = 0x00F0;
        cpuField.set(null, instruction);

        regD = 0x07;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 6, false);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_BLD.executeInstruction();

        //Read Result
        assertEquals(6, dataMemory.readByte((0x01F0 & instruction) >> 4));

    }

    @Test
    public void BRBC_BranchWithI_ReturnPCOne() throws IllegalAccessException {
        instruction = 0x000F;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 7, false);
        programMemory.setPC(0);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_BRBC.executeInstruction();

        //Read Result
        assertEquals(1, programMemory.getPC());
    }

    @Test
    public void BRBC_NotBranchWithI_ReturnPCZero() throws IllegalAccessException {
        instruction = 0x000F;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 7, true);
        programMemory.setPC(0);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_BRBC.executeInstruction();

        //Read Result
        assertEquals(0, programMemory.getPC());
    }

    @Test
    public void BRBC_BranchWithT_ReturnPCTwo() throws IllegalAccessException {
        instruction = 0x0016;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 6, false);
        programMemory.setPC(0);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_BRBC.executeInstruction();

        //Read Result
        assertEquals(2, programMemory.getPC());
    }

    @Test
    public void BRBC_NotBranchWithT_ReturnPCZero() throws IllegalAccessException {
        instruction = 0x0016;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 6, true);
        programMemory.setPC(0);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_BRBC.executeInstruction();

        //Read Result
        assertEquals(0, programMemory.getPC());
    }

    @Test
    public void BRBC_BranchWithH_ReturnPCThree() throws IllegalAccessException {
        instruction = 0x001D;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 5, false);
        programMemory.setPC(0);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_BRBC.executeInstruction();

        //Read Result
        assertEquals(3, programMemory.getPC());
    }

    @Test
    public void BRBC_NotBranchWithH_ReturnPCZero() throws IllegalAccessException {
        instruction = 0x001D;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 5, true);
        programMemory.setPC(0);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_BRBC.executeInstruction();

        //Read Result
        assertEquals(0, programMemory.getPC());
    }

    @Test
    public void BRBC_BranchWithS_ReturnPCFour() throws IllegalAccessException {
        instruction = 0x0024;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 4, false);
        programMemory.setPC(0);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_BRBC.executeInstruction();

        //Read Result
        assertEquals(4, programMemory.getPC());
    }

    @Test
    public void BRBC_NotBranchWithS_ReturnPCZero() throws IllegalAccessException {
        instruction = 0x0024;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 4, true);
        programMemory.setPC(0);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_BRBC.executeInstruction();

        //Read Result
        assertEquals(0, programMemory.getPC());
    }

    @Test
    public void BRBC_BranchWithV_ReturnPCFive() throws IllegalAccessException {
        instruction = 0x002B;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 3, false);
        programMemory.setPC(0);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_BRBC.executeInstruction();

        //Read Result
        assertEquals(5, programMemory.getPC());
    }

    @Test
    public void BRBC_NotBranchWithV_ReturnPCZero() throws IllegalAccessException {
        instruction = 0x002B;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 3, true);
        programMemory.setPC(0);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_BRBC.executeInstruction();

        //Read Result
        assertEquals(0, programMemory.getPC());
    }

    @Test
    public void BRBC_BranchWithN_ReturnPCSix() throws IllegalAccessException {
        instruction = 0x0032;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 2, false);
        programMemory.setPC(0);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_BRBC.executeInstruction();

        //Read Result
        assertEquals(6, programMemory.getPC());
    }

    @Test
    public void BRBC_NotBranchWithN_ReturnPCZero() throws IllegalAccessException {
        instruction = 0x0032;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 2, true);
        programMemory.setPC(0);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_BRBC.executeInstruction();

        //Read Result
        assertEquals(0, programMemory.getPC());
    }

    @Test
    public void BRBC_BranchWithZ_ReturnPCSeven() throws IllegalAccessException {
        instruction = 0x0039;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 1, false);
        programMemory.setPC(0);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_BRBC.executeInstruction();

        //Read Result
        assertEquals(7, programMemory.getPC());
    }

    @Test
    public void BRBC_NotBranchWithZ_ReturnPCZero() throws IllegalAccessException {
        instruction = 0x0039;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 1, true);
        programMemory.setPC(0);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_BRBC.executeInstruction();

        //Read Result
        assertEquals(0, programMemory.getPC());
    }

    @Test
    public void BRBC_BranchWithC_ReturnPCEight() throws IllegalAccessException {
        instruction = 0x0040;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 0, false);
        programMemory.setPC(0);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_BRBC.executeInstruction();

        //Read Result
        assertEquals(8, programMemory.getPC());
    }

    @Test
    public void BRBC_NotBranchWithC_ReturnPCZero() throws IllegalAccessException {
        instruction = 0x0040;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 0, true);
        programMemory.setPC(0);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_BRBC.executeInstruction();

        //Read Result
        assertEquals(0, programMemory.getPC());
    }

    @Test
    public void BRBS_BranchWithI_ReturnPCOne() throws IllegalAccessException {
        instruction = 0x000F;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 7, true);
        programMemory.setPC(0);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_BRBS.executeInstruction();

        //Read Result
        assertEquals(1, programMemory.getPC());
    }

    @Test
    public void BRBS_NotBranchWithI_ReturnPCZero() throws IllegalAccessException {
        instruction = 0x000F;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 7, false);
        programMemory.setPC(0);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_BRBS.executeInstruction();

        //Read Result
        assertEquals(0, programMemory.getPC());
    }

    @Test
    public void BRBS_BranchWithT_ReturnPCTwo() throws IllegalAccessException {
        instruction = 0x0016;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 6, true);
        programMemory.setPC(0);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_BRBS.executeInstruction();

        //Read Result
        assertEquals(2, programMemory.getPC());
    }

    @Test
    public void BRBS_NotBranchWithT_ReturnPCZero() throws IllegalAccessException {
        instruction = 0x0016;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 6, false);
        programMemory.setPC(0);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_BRBS.executeInstruction();

        //Read Result
        assertEquals(0, programMemory.getPC());
    }

    @Test
    public void BRBS_BranchWithH_ReturnPCThree() throws IllegalAccessException {
        instruction = 0x001D;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 5, true);
        programMemory.setPC(0);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_BRBS.executeInstruction();

        //Read Result
        assertEquals(3, programMemory.getPC());
    }

    @Test
    public void BRBS_NotBranchWithH_ReturnPCZero() throws IllegalAccessException {
        instruction = 0x001D;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 5, false);
        programMemory.setPC(0);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_BRBS.executeInstruction();

        //Read Result
        assertEquals(0, programMemory.getPC());
    }

    @Test
    public void BRBS_BranchWithS_ReturnPCFour() throws IllegalAccessException {
        instruction = 0x0024;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 4, true);
        programMemory.setPC(0);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_BRBS.executeInstruction();

        //Read Result
        assertEquals(4, programMemory.getPC());
    }

    @Test
    public void BRBS_NotBranchWithS_ReturnPCZero() throws IllegalAccessException {
        instruction = 0x0024;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 4, false);
        programMemory.setPC(0);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_BRBS.executeInstruction();

        //Read Result
        assertEquals(0, programMemory.getPC());
    }

    @Test
    public void BRBS_BranchWithV_ReturnPCFive() throws IllegalAccessException {
        instruction = 0x002B;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 3, true);
        programMemory.setPC(0);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_BRBS.executeInstruction();

        //Read Result
        assertEquals(5, programMemory.getPC());
    }

    @Test
    public void BRBS_NotBranchWithV_ReturnPCZero() throws IllegalAccessException {
        instruction = 0x002B;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 3, false);
        programMemory.setPC(0);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_BRBS.executeInstruction();

        //Read Result
        assertEquals(0, programMemory.getPC());
    }

    @Test
    public void BRBS_BranchWithN_ReturnPCSix() throws IllegalAccessException {
        instruction = 0x0032;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 2, true);
        programMemory.setPC(0);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_BRBS.executeInstruction();

        //Read Result
        assertEquals(6, programMemory.getPC());
    }

    @Test
    public void BRBS_NotBranchWithN_ReturnPCZero() throws IllegalAccessException {
        instruction = 0x0032;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 2, false);
        programMemory.setPC(0);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_BRBS.executeInstruction();

        //Read Result
        assertEquals(0, programMemory.getPC());
    }

    @Test
    public void BRBS_BranchWithZ_ReturnPCSeven() throws IllegalAccessException {
        instruction = 0x0039;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 1, true);
        programMemory.setPC(0);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_BRBS.executeInstruction();

        //Read Result
        assertEquals(7, programMemory.getPC());
    }

    @Test
    public void BRBS_NotBranchWithZ_ReturnPCZero() throws IllegalAccessException {
        instruction = 0x0039;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 1, false);
        programMemory.setPC(0);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_BRBS.executeInstruction();

        //Read Result
        assertEquals(0, programMemory.getPC());
    }

    @Test
    public void BRBS_BranchWithC_ReturnPCEight() throws IllegalAccessException {
        instruction = 0x0040;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 0, true);
        programMemory.setPC(0);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_BRBS.executeInstruction();

        //Read Result
        assertEquals(8, programMemory.getPC());
    }

    @Test
    public void BRBS_NotBranchWithC_ReturnPCZero() throws IllegalAccessException {
        instruction = 0x0040;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 0, false);
        programMemory.setPC(0);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_BRBS.executeInstruction();

        //Read Result
        assertEquals(0, programMemory.getPC());
    }

    @Test
    public void BSET_ClearI() throws IllegalAccessException {

        instruction = 0x00F0;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 7, false);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_BSET.executeInstruction();

        //Read Flags
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,7));//I
    }

    @Test
    public void BSET_ClearT() throws IllegalAccessException {

        instruction = 0x00E0;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 6, false);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_BSET.executeInstruction();

        //Read Flags
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,6));//T
    }

    @Test
    public void BSET_ClearH() throws IllegalAccessException {

        instruction = 0x00D0;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 5, false);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_BSET.executeInstruction();

        //Read Flags
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,5));//H
    }

    @Test
    public void BSET_ClearS() throws IllegalAccessException {

        instruction = 0x00C0;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 4, false);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_BSET.executeInstruction();

        //Read Flags
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
    }

    @Test
    public void BSET_ClearV() throws IllegalAccessException {

        instruction = 0x00B0;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 3, false);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_BSET.executeInstruction();

        //Read Flags
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
    }

    @Test
    public void BSET_ClearN() throws IllegalAccessException {

        instruction = 0x00A0;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 2, false);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_BSET.executeInstruction();

        //Read Flags
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
    }

    @Test
    public void BSET_ClearZ() throws IllegalAccessException {

        instruction = 0x0090;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 1, false);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_BSET.executeInstruction();

        //Read Flags
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
    }

    @Test
    public void BSET_ClearC() throws IllegalAccessException {

        instruction = 0x0080;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 0, false);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_BSET.executeInstruction();

        //Read Flags
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C
    }

    @Test
    public void BST_MoveTrueFromPositionFour() throws IllegalAccessException {

        instruction = 0x0084;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeBit((0x01F0 & instruction) >> 4, 4, true);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_BST.executeInstruction();

        //Read Flags
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,6));
    }

    @Test
    public void BST_MoveFalseFromPositionFive() throws IllegalAccessException {

        instruction = 0x0085;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeBit((0x01F0 & instruction) >> 4, 5, false);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_BST.executeInstruction();

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,6));
    }

    @Test
    public void CALL_JumpFromFiveToTen() throws Exception {

        instruction = 0x0000;
        cpuField.set(null, instruction);

        //Fill Memory
        programMemory.setPC(5);
        programMemory.writeWord(5,10);
        dataMemory.writeByte(DataMemory_ATmega328P.SPH_ADDR, (byte) 0x08);
        dataMemory.writeByte(DataMemory_ATmega328P.SPL_ADDR, (byte) -1);//0xFF

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_CALL.executeInstruction();

        //Read Result

        //Stack in correct position?
        assertEquals(0x08, dataMemory.readByte(DataMemory_ATmega328P.SPH_ADDR));
        assertEquals(-3, dataMemory.readByte(DataMemory_ATmega328P.SPL_ADDR));

        int stackPointer = (dataMemory.readByte(DataMemory_ATmega328P.SPH_ADDR) << 8) |
                (0x000000FF & dataMemory.readByte(DataMemory_ATmega328P.SPL_ADDR));

        byte pcHigh = dataMemory.readByte(stackPointer);
        stackPointer += 1;
        byte pcLow = dataMemory.readByte(stackPointer);

        //PC Correct in stack?
        assertEquals(0, pcHigh);
        assertEquals(6, pcLow);

        //New PC Value correct?
        assertEquals(10, programMemory.getPC());
    }

    @Test
    public void CALL_JumpFromFirstToLast() throws Exception {

        instruction = 0x01F1;
        cpuField.set(null, instruction);

        //Fill Memory
        programMemory.setPC(0);
        programMemory.writeWord(0,0xFFFF);
        dataMemory.writeByte(DataMemory_ATmega328P.SPH_ADDR, (byte) 0x08);
        dataMemory.writeByte(DataMemory_ATmega328P.SPL_ADDR, (byte) -1);//0xFF

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_CALL.executeInstruction();

        //Read Result

        //Stack in correct position?
        assertEquals(0x08, dataMemory.readByte(DataMemory_ATmega328P.SPH_ADDR));
        assertEquals(-3, dataMemory.readByte(DataMemory_ATmega328P.SPL_ADDR));

        int stackPointer = (dataMemory.readByte(DataMemory_ATmega328P.SPH_ADDR) << 8) |
                (0x000000FF & dataMemory.readByte(DataMemory_ATmega328P.SPL_ADDR));

        byte pcHigh = dataMemory.readByte(stackPointer);
        stackPointer += 1;
        byte pcLow = dataMemory.readByte(stackPointer);

        //PC Correct in stack?
        assertEquals(0, pcHigh);
        assertEquals(1, pcLow);

        //New PC Value correct? (16-bits PC)
        assertEquals(0xFFFF, programMemory.getPC());
    }

    @Test
    public void CALL_JumpFromLastToFirst() throws Exception {

        instruction = 0x0000;
        cpuField.set(null, instruction);

        //Fill Memory
        programMemory.setPC(0x3FFF);
        programMemory.writeWord(0x3FFF,0);
        dataMemory.writeByte(DataMemory_ATmega328P.SPH_ADDR, (byte) 0x08);
        dataMemory.writeByte(DataMemory_ATmega328P.SPL_ADDR, (byte) -1);//0xFF

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_CALL.executeInstruction();

        //Read Result

        //Stack in correct position?
        assertEquals(0x08, dataMemory.readByte(DataMemory_ATmega328P.SPH_ADDR));
        assertEquals(-3, dataMemory.readByte(DataMemory_ATmega328P.SPL_ADDR));

        int stackPointer = (dataMemory.readByte(DataMemory_ATmega328P.SPH_ADDR) << 8) |
                (0x000000FF & dataMemory.readByte(DataMemory_ATmega328P.SPL_ADDR));

        byte pcHigh = dataMemory.readByte(stackPointer);
        stackPointer += 1;
        byte pcLow = dataMemory.readByte(stackPointer);

        //PC Correct in stack?
        assertEquals(0x40, pcHigh);
        assertEquals(0x00, pcLow);

        //New PC Value correct?
        assertEquals(0, programMemory.getPC());
    }

    @Test
    public void CBI_ClearFourthBitInPORTB() throws IllegalAccessException {
        instruction = 0x002C;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeBit(DataMemory_ATmega328P.PORTB_ADDR, 4, true);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_CBI.executeInstruction();

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.PORTB_ADDR,4));
    }

    @Test
    public void CBI_ClearSecondBitInDDRD() throws IllegalAccessException {
        instruction = 0x0052;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeBit(DataMemory_ATmega328P.DDRD_ADDR, 2, true);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_CBI.executeInstruction();

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.DDRD_ADDR,2));
    }

    @Test
    public void COM_ComplementAllZero_ReturnNeg1() throws IllegalAccessException {

        instruction = 0x00F0;
        cpuField.set(null, instruction);

        regD = 0;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_COM.executeInstruction();

        //Read Result
        assertEquals(-1, dataMemory.readByte((0x01F0 & instruction) >> 4));

        //Read Flags
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void COM_ComplementAllOne_Return0() throws IllegalAccessException {

        instruction = 0x00F0;
        cpuField.set(null, instruction);

        regD = -1;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_COM.executeInstruction();

        //Read Result
        assertEquals(0, dataMemory.readByte((0x01F0 & instruction) >> 4));

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void COM_ComplementHalf_Return15() throws IllegalAccessException {

        instruction = 0x00F0;
        cpuField.set(null, instruction);

        regD = -16;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_COM.executeInstruction();

        //Read Result
        assertEquals(15, dataMemory.readByte((0x01F0 & instruction) >> 4));

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void CP_CompareEqual() throws IllegalAccessException {

        instruction = 0x00FE;
        cpuField.set(null, instruction);

        regD = 1;
        regR = 1;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);
        dataMemory.writeByte(((0x0200 & instruction) >> 5) | (0x000F & instruction), regR);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_CP.executeInstruction();

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,5));//H
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void CP_CompareGreather() throws IllegalAccessException {

        instruction = 0x00FE;
        cpuField.set(null, instruction);

        regD = 2;
        regR = 1;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);
        dataMemory.writeByte(((0x0200 & instruction) >> 5) | (0x000F & instruction), regR);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_CP.executeInstruction();

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,5));//H
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void CP_CompareLower() throws IllegalAccessException {

        instruction = 0x00FE;
        cpuField.set(null, instruction);

        regD = 1;
        regR = 2;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);
        dataMemory.writeByte(((0x0200 & instruction) >> 5) | (0x000F & instruction), regR);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_CP.executeInstruction();

        //Read Flags
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,5));//H
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void CP_CompareTwoComplementOverflow() throws IllegalAccessException {

        instruction = 0x00FE;
        cpuField.set(null, instruction);

        regD = 1;
        regR = -127;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);
        dataMemory.writeByte(((0x0200 & instruction) >> 5) | (0x000F & instruction), regR);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_CP.executeInstruction();

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,5));//H
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void CPC_CompareEqualWithoutCarry() throws IllegalAccessException {

        instruction = 0x00FE;
        cpuField.set(null, instruction);

        regD = 1;
        regR = 1;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);
        dataMemory.writeByte(((0x0200 & instruction) >> 5) | (0x000F & instruction), regR);
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 0, false);
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 1, true);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_CPC.executeInstruction();

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,5));//H
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void CPC_CompareGreatherWithCarry() throws IllegalAccessException {

        instruction = 0x00FE;
        cpuField.set(null, instruction);

        regD = 2;
        regR = 1;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);
        dataMemory.writeByte(((0x0200 & instruction) >> 5) | (0x000F & instruction), regR);
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 0, true);
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 1, true);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_CPC.executeInstruction();

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,5));//H
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void CPC_CompareLowerWithoutCarry() throws IllegalAccessException {

        instruction = 0x00FE;
        cpuField.set(null, instruction);

        regD = 1;
        regR = 2;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);
        dataMemory.writeByte(((0x0200 & instruction) >> 5) | (0x000F & instruction), regR);
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 0, false);
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 1, true);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_CPC.executeInstruction();

        //Read Flags
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,5));//H
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void CPC_CompareTwoComplementOverflowWithCarry() throws IllegalAccessException {

        instruction = 0x00FE;
        cpuField.set(null, instruction);

        regD = 127;
        regR = -128;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);
        dataMemory.writeByte(((0x0200 & instruction) >> 5) | (0x000F & instruction), regR);
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 0, true);
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 1, true);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_CPC.executeInstruction();

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,5));//H
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void CPI_CompareEqual() throws IllegalAccessException {

        instruction = 0x00F1;
        cpuField.set(null, instruction);

        regD = 1;

        //Fill Memory
        dataMemory.writeByte((0x10 | (0x000000F0 & instruction) >> 4), regD);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_CPI.executeInstruction();

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,5));//H
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void CPI_CompareGreather() throws IllegalAccessException {

        instruction = 0x00F1;
        cpuField.set(null, instruction);

        regD = 2;

        //Fill Memory
        dataMemory.writeByte((0x10 | (0x000000F0 & instruction) >> 4), regD);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_CPI.executeInstruction();

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,5));//H
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void CPI_CompareLower() throws IllegalAccessException {

        instruction = 0x00F2;
        cpuField.set(null, instruction);

        regD = 1;

        //Fill Memory
        dataMemory.writeByte((0x10 | (0x000000F0 & instruction) >> 4), regD);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_CPI.executeInstruction();

        //Read Flags
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,5));//H
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void CPI_CompareTwoComplementOverflow() throws IllegalAccessException {

        instruction = 0x08F1;
        cpuField.set(null, instruction);

        regD = 1;

        //Fill Memory
        dataMemory.writeByte((0x10 | (0x000000F0 & instruction) >> 4), regD);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_CPI.executeInstruction();

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,5));//H
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void CPSE_JumpOneWordInstruction() throws IllegalAccessException {

        instruction = 0x00FE;
        cpuField.set(null, instruction);

        regD = 1;
        regR = 1;

        //Fill Memory
        programMemory.setPC(0);
        programMemory.writeWord(0, 0x0000);
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);
        dataMemory.writeByte(((0x0200 & instruction) >> 5) | (0x000F & instruction), regR);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_CPSE.executeInstruction();

        assertEquals(1, programMemory.getPC());
    }

    @Test
    public void CPSE_JumpTwoWordInstructionSTS() throws IllegalAccessException {

        instruction = 0x00FE;
        cpuField.set(null, instruction);

        regD = 1;
        regR = 1;

        //Fill Memory
        programMemory.setPC(0);
        programMemory.writeWord(0, 0x9200);
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);
        dataMemory.writeByte(((0x0200 & instruction) >> 5) | (0x000F & instruction), regR);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_CPSE.executeInstruction();

        assertEquals(2, programMemory.getPC());
    }

    @Test
    public void CPSE_JumpTwoWordInstructionLDS() throws IllegalAccessException {

        instruction = 0x00FE;
        cpuField.set(null, instruction);

        regD = 1;
        regR = 1;

        //Fill Memory
        programMemory.setPC(0);
        programMemory.writeWord(0, 0x90F0);
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);
        dataMemory.writeByte(((0x0200 & instruction) >> 5) | (0x000F & instruction), regR);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_CPSE.executeInstruction();

        assertEquals(2, programMemory.getPC());
    }

    @Test
    public void CPSE_JumpTwoWordInstructionCALL() throws IllegalAccessException {

        instruction = 0x00FE;
        cpuField.set(null, instruction);

        regD = 1;
        regR = 1;

        //Fill Memory
        programMemory.setPC(0);
        programMemory.writeWord(0, 0x940E);
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);
        dataMemory.writeByte(((0x0200 & instruction) >> 5) | (0x000F & instruction), regR);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_CPSE.executeInstruction();

        assertEquals(2, programMemory.getPC());
    }

    @Test
    public void CPSE_JumpTwoWordInstructionJMP() throws IllegalAccessException {

        instruction = 0x00FE;
        cpuField.set(null, instruction);

        regD = 1;
        regR = 1;

        //Fill Memory
        programMemory.setPC(0);
        programMemory.writeWord(0, 0x940C);
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);
        dataMemory.writeByte(((0x0200 & instruction) >> 5) | (0x000F & instruction), regR);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_CPSE.executeInstruction();

        assertEquals(2, programMemory.getPC());
    }

    @Test
    public void CPSE_NoJump() throws IllegalAccessException {

        instruction = 0x00FE;
        cpuField.set(null, instruction);

        regD = 2;
        regR = 1;

        //Fill Memory
        programMemory.setPC(0);
        programMemory.writeWord(0, 0x940C);
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);
        dataMemory.writeByte(((0x0200 & instruction) >> 5) | (0x000F & instruction), regR);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_CPSE.executeInstruction();

        assertEquals(0, programMemory.getPC());
    }

    @Test
    public void DEC_FromOne_Return0() throws IllegalAccessException {

        instruction = 0x00FE;
        cpuField.set(null, instruction);

        regD = 1;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_DEC.executeInstruction();

        //Read Result
        assertEquals(0, dataMemory.readByte((0x01F0 & instruction) >> 4));

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
    }

    @Test
    public void DEC_FromZero_ReturnNeg1() throws IllegalAccessException {

        instruction = 0x00FE;
        cpuField.set(null, instruction);

        regD = 0;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_DEC.executeInstruction();

        //Read Result
        assertEquals(-1, dataMemory.readByte((0x01F0 & instruction) >> 4));

        //Read Flags
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
    }

    @Test
    public void DEC_TwoComplementOverflow_Return127() throws IllegalAccessException {

        instruction = 0x00FE;
        cpuField.set(null, instruction);

        regD = -128;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_DEC.executeInstruction();

        //Read Result
        assertEquals(127, dataMemory.readByte((0x01F0 & instruction) >> 4));

        //Read Flags
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
    }

    @Test
    public void EOR_ClearRegister() throws IllegalAccessException {

        instruction = 0x00FF;
        cpuField.set(null, instruction);

        regD = 0x0F;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_EOR.executeInstruction();

        //Read Result
        assertEquals(0, dataMemory.readByte((0x01F0 & instruction) >> 4));

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z

    }

    @Test
    public void EOR_ZeroXor85_Result85() throws IllegalAccessException {

        instruction = 0x00FE;
        cpuField.set(null, instruction);

        regD = 85;
        regR = 0;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);
        dataMemory.writeByte(((0x0200 & instruction) >> 5) | (0x000F & instruction), regR);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_EOR.executeInstruction();

        //Read Result
        assertEquals(85, dataMemory.readByte((0x01F0 & instruction) >> 4));

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z

    }

    @Test
    public void EOR_FFXor85_ResultNeg86() throws IllegalAccessException {

        instruction = 0x00FE;
        cpuField.set(null, instruction);

        regD = 85;
        regR = -1;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);
        dataMemory.writeByte(((0x0200 & instruction) >> 5) | (0x000F & instruction), regR);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_EOR.executeInstruction();

        //Read Result
        assertEquals(-86, dataMemory.readByte((0x01F0 & instruction) >> 4));

        //Read Flags
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z

    }

    @Test
    public void FMUL_SquareHalf_Result20And00() throws IllegalAccessException {

        instruction = 0x0076;
        cpuField.set(null, instruction);

        regD = 0b01000000; //2^-1
        regR = 0b01000000; //2^-1

        //Fill Memory
        dataMemory.writeByte((0x10 + ((0x0070 & instruction)>>4)), regD);
        dataMemory.writeByte((0x10 + (0x0007 & instruction)), regR);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_FMUL.executeInstruction();

        //Read Result
        //0x2000 = 0b0010000000000000 = 2^-2
        assertEquals(0x20, dataMemory.readByte(0x01));
        assertEquals(0x00, dataMemory.readByte(0x00));

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void FMUL_HalfTimesZero_Result00And00() throws IllegalAccessException {

        instruction = 0x0076;
        cpuField.set(null, instruction);

        regD = 0b01000000; //2^-1
        regR = 0b00000000;

        //Fill Memory
        dataMemory.writeByte((0x10 + ((0x0070 & instruction)>>4)), regD);
        dataMemory.writeByte((0x10 + (0x0007 & instruction)), regR);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_FMUL.executeInstruction();

        //Read Result
        assertEquals(0x00, dataMemory.readByte(0x01));
        assertEquals(0x00, dataMemory.readByte(0x00));

        //Read Flags
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void FMUL_OneTimesOneAndThreeQuarters_ResultE0And00() throws IllegalAccessException {

        instruction = 0x0076;
        cpuField.set(null, instruction);

        regD = -128; //0b10000000 = 1
        regR = -32; //0b11100000 = 1,75

        //Fill Memory
        dataMemory.writeByte((0x10 + ((0x0070 & instruction)>>4)), regD);
        dataMemory.writeByte((0x10 + (0x0007 & instruction)), regR);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_FMUL.executeInstruction();

        //Read Result
        //0xE000 = 1,75
        assertEquals((byte)0xE0, dataMemory.readByte(0x01));
        assertEquals((byte)0x00, dataMemory.readByte(0x00));

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void FMUL_0xABTimes0x55_Result0x718E() throws IllegalAccessException {

        instruction = 0x0076;
        cpuField.set(null, instruction);

        regD = -85; //0xAB
        regR = 0x55;

        //Fill Memory
        dataMemory.writeByte((0x10 + ((0x0070 & instruction)>>4)), regD);
        dataMemory.writeByte((0x10 + (0x0007 & instruction)), regR);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_FMUL.executeInstruction();

        //Read Result
        assertEquals((byte)0x71, dataMemory.readByte(0x01));
        assertEquals((byte)0x8E, dataMemory.readByte(0x00));

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void FMULS_0xABTimes0x55_ResultC78E() throws IllegalAccessException {

        instruction = 0x0076;
        cpuField.set(null, instruction);

        regD = -85; //0xAB
        regR = 0x55;

        //Fill Memory
        dataMemory.writeByte((0x10 + ((0x0070 & instruction)>>4)), regD);
        dataMemory.writeByte((0x10 + (0x0007 & instruction)), regR);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_FMULS.executeInstruction();

        //Read Result
        assertEquals((byte)0xC7, dataMemory.readByte(0x01));
        assertEquals((byte)0x8E, dataMemory.readByte(0x00));

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void FMULS_SquareHalf_Result20And00() throws IllegalAccessException {

        instruction = 0x0076;
        cpuField.set(null, instruction);

        regD = 0b01000000; //2^-1
        regR = 0b01000000; //2^-1

        //Fill Memory
        dataMemory.writeByte((0x10 + ((0x0070 & instruction)>>4)), regD);
        dataMemory.writeByte((0x10 + (0x0007 & instruction)), regR);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_FMULS.executeInstruction();

        //Read Result
        //0x2000 = 0b0010000000000000 = 2^-2
        assertEquals(0x20, dataMemory.readByte(0x01));
        assertEquals(0x00, dataMemory.readByte(0x00));

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void FMULS_HalfTimesZero_Result00And00() throws IllegalAccessException {

        instruction = 0x0076;
        cpuField.set(null, instruction);

        regD = 0b01000000; //2^-1
        regR = 0b00000000;

        //Fill Memory
        dataMemory.writeByte((0x10 + ((0x0070 & instruction)>>4)), regD);
        dataMemory.writeByte((0x10 + (0x0007 & instruction)), regR);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_FMULS.executeInstruction();

        //Read Result
        assertEquals(0x00, dataMemory.readByte(0x01));
        assertEquals(0x00, dataMemory.readByte(0x00));

        //Read Flags
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void FMULS_MinusOneTimesOneAndThreeQuarters_Result00And40() throws IllegalAccessException {

        instruction = 0x0076;
        cpuField.set(null, instruction);

        regD = -1; //0b11111111 = -1
        regR = -32; //0b11100000 = 1,75

        //Fill Memory
        dataMemory.writeByte((0x10 + ((0x0070 & instruction)>>4)), regD);
        dataMemory.writeByte((0x10 + (0x0007 & instruction)), regR);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_FMULS.executeInstruction();

        //Read Result
        assertEquals((byte)0x00, dataMemory.readByte(0x01));
        assertEquals((byte)0x40, dataMemory.readByte(0x00));

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void FMULSU_0xABTimes0x55_ResultC78E() throws IllegalAccessException {

        instruction = 0x0076;
        cpuField.set(null, instruction);

        regD = -85; //0xAB
        regR = 0x55;

        //Fill Memory
        dataMemory.writeByte((0x10 + ((0x0070 & instruction)>>4)), regD);
        dataMemory.writeByte((0x10 + (0x0007 & instruction)), regR);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_FMULSU.executeInstruction();

        //Read Result
        assertEquals((byte)0xC7, dataMemory.readByte(0x01));
        assertEquals((byte)0x8E, dataMemory.readByte(0x00));

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void FMULSU_0x55Times0xAB_Result718E() throws IllegalAccessException {

        instruction = 0x0076;
        cpuField.set(null, instruction);

        regD = 0x55;
        regR = -85; //0xAB

        //Fill Memory
        dataMemory.writeByte((0x10 + ((0x0070 & instruction)>>4)), regD);
        dataMemory.writeByte((0x10 + (0x0007 & instruction)), regR);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_FMULSU.executeInstruction();

        //Read Result
        assertEquals((byte)0x71, dataMemory.readByte(0x01));
        assertEquals((byte)0x8E, dataMemory.readByte(0x00));

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void FMULSU_HalfTimesZero_Result00And00() throws IllegalAccessException {

        instruction = 0x0076;
        cpuField.set(null, instruction);

        regD = 0b01000000; //2^-1
        regR = 0b00000000;

        //Fill Memory
        dataMemory.writeByte((0x10 + ((0x0070 & instruction)>>4)), regD);
        dataMemory.writeByte((0x10 + (0x0007 & instruction)), regR);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_FMULSU.executeInstruction();

        //Read Result
        assertEquals(0x00, dataMemory.readByte(0x01));
        assertEquals(0x00, dataMemory.readByte(0x00));

        //Read Flags
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void ICALL_JumpFromTenToTwenty() throws Exception {

        instruction = 0x0000;
        cpuField.set(null, instruction);

        //Fill Memory
        programMemory.setPC(10);
        dataMemory.writeByte(31, (byte) 0);
        dataMemory.writeByte(30, (byte) 20);
        dataMemory.writeByte(DataMemory_ATmega328P.SPH_ADDR, (byte) 0x08);
        dataMemory.writeByte(DataMemory_ATmega328P.SPL_ADDR, (byte) -1);//0xFF

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_ICALL.executeInstruction();

        //Read Result

        //Stack in correct position?
        assertEquals(0x08, dataMemory.readByte(DataMemory_ATmega328P.SPH_ADDR));
        assertEquals(-3, dataMemory.readByte(DataMemory_ATmega328P.SPL_ADDR));

        int stackPointer = (dataMemory.readByte(DataMemory_ATmega328P.SPH_ADDR) << 8) |
                (0x000000FF & dataMemory.readByte(DataMemory_ATmega328P.SPL_ADDR));

        byte pcHigh = dataMemory.readByte(stackPointer);
        stackPointer += 1;
        byte pcLow = dataMemory.readByte(stackPointer);

        //PC Correct in stack?
        assertEquals(0, pcHigh);
        assertEquals(10, pcLow);

        //New PC Value correct?
        assertEquals(20, programMemory.getPC());
    }

    @Test
    public void ICALL_JumpFromFirstToLast() throws Exception {

        instruction = 0x0000;
        cpuField.set(null, instruction);

        //Fill Memory
        programMemory.setPC(0);
        dataMemory.writeByte(31, (byte) -1); //0xFF
        dataMemory.writeByte(30, (byte) -1); //0xFF
        dataMemory.writeByte(DataMemory_ATmega328P.SPH_ADDR, (byte) 0x08);
        dataMemory.writeByte(DataMemory_ATmega328P.SPL_ADDR, (byte) -1);//0xFF

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_ICALL.executeInstruction();

        //Read Result

        //Stack in correct position?
        assertEquals(0x08, dataMemory.readByte(DataMemory_ATmega328P.SPH_ADDR));
        assertEquals(-3, dataMemory.readByte(DataMemory_ATmega328P.SPL_ADDR));

        int stackPointer = (dataMemory.readByte(DataMemory_ATmega328P.SPH_ADDR) << 8) |
                (0x000000FF & dataMemory.readByte(DataMemory_ATmega328P.SPL_ADDR));

        byte pcHigh = dataMemory.readByte(stackPointer);
        stackPointer += 1;
        byte pcLow = dataMemory.readByte(stackPointer);

        //PC Correct in stack?
        assertEquals(0, pcHigh);
        assertEquals(0, pcLow);

        //New PC Value correct?
        assertEquals(0xFFFF, programMemory.getPC());
    }

    @Test
    public void ICALL_JumpFromLastToFirst() throws Exception {

        instruction = 0x0000;
        cpuField.set(null, instruction);

        //Fill Memory
        programMemory.setPC(0xFFFF);
        dataMemory.writeByte(31, (byte) 0); //0xFF
        dataMemory.writeByte(30, (byte) 0); //0xFF
        dataMemory.writeByte(DataMemory_ATmega328P.SPH_ADDR, (byte) 0x08);
        dataMemory.writeByte(DataMemory_ATmega328P.SPL_ADDR, (byte) -1);//0xFF

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_ICALL.executeInstruction();

        //Read Result

        //Stack in correct position?
        assertEquals(0x08, dataMemory.readByte(DataMemory_ATmega328P.SPH_ADDR));
        assertEquals(-3, dataMemory.readByte(DataMemory_ATmega328P.SPL_ADDR));

        int stackPointer = (dataMemory.readByte(DataMemory_ATmega328P.SPH_ADDR) << 8) |
                (0x000000FF & dataMemory.readByte(DataMemory_ATmega328P.SPL_ADDR));

        byte pcHigh = dataMemory.readByte(stackPointer);
        stackPointer += 1;
        byte pcLow = dataMemory.readByte(stackPointer);

        //PC Correct in stack?
        assertEquals(-1, pcHigh);
        assertEquals(-1, pcLow);

        //New PC Value correct?
        assertEquals(0, programMemory.getPC());
    }

    @Test
    public void IJMP_JumpFromTwentyTo256() throws Exception {

        instruction = 0x0000;
        cpuField.set(null, instruction);

        //Fill Memory
        programMemory.setPC(20);
        dataMemory.writeByte(31, (byte) 1);
        dataMemory.writeByte(30, (byte) 0);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_IJMP.executeInstruction();

        //Read Result
        assertEquals(256, programMemory.getPC());
    }

    @Test
    public void IJMP_JumpFromFirstToLast() throws Exception {

        instruction = 0x0000;
        cpuField.set(null, instruction);

        //Fill Memory
        programMemory.setPC(0);
        dataMemory.writeByte(31, (byte) -1); //0xFF
        dataMemory.writeByte(30, (byte) -1); //0xFF

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_IJMP.executeInstruction();

        //Read Result
        assertEquals(0xFFFF, programMemory.getPC());
    }

    @Test
    public void IJMP_JumpFromLastToFirst() throws Exception {

        instruction = 0x0000;
        cpuField.set(null, instruction);

        //Fill Memory
        programMemory.setPC(0xFFFF);
        dataMemory.writeByte(31, (byte) 0); //0xFF
        dataMemory.writeByte(30, (byte) 0); //0xFF

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_IJMP.executeInstruction();

        //Read Result
        assertEquals(0, programMemory.getPC());
    }

    @Test
    public void IN_Load0x77ToR0() throws Exception {

        instruction = 0x060F;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(((((0x0600 & instruction) >> 5) | ((0x000F & instruction))) + 0x20), (byte) 0x77);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_IN.executeInstruction();

        //Read Result
        assertEquals(0x77, dataMemory.readByte((0x01F0 & instruction) >> 4));
    }

    @Test
    public void IN_Load0x76ToR31() throws Exception {

        instruction = 0x07FF;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(((((0x0600 & instruction) >> 5) | ((0x000F & instruction))) + 0x20), (byte) 0x76);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_IN.executeInstruction();

        //Read Result
        assertEquals(0x76, dataMemory.readByte((0x01F0 & instruction) >> 4));
    }

    @Test
    public void INC_FromZero_Return1() throws IllegalAccessException {

        instruction = 0x00FE;
        cpuField.set(null, instruction);

        regD = 0;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_INC.executeInstruction();

        //Read Result
        assertEquals(1, dataMemory.readByte((0x01F0 & instruction) >> 4));

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
    }

    @Test
    public void INC_FromNegOne_Return0() throws IllegalAccessException {

        instruction = 0x00FE;
        cpuField.set(null, instruction);

        regD = -1;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_INC.executeInstruction();

        //Read Result
        assertEquals(0, dataMemory.readByte((0x01F0 & instruction) >> 4));

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
    }

    @Test
    public void INC_FromNeg2_ReturnNeg1() throws IllegalAccessException {

        instruction = 0x00FE;
        cpuField.set(null, instruction);

        regD = -2;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_INC.executeInstruction();

        //Read Result
        assertEquals(-1, dataMemory.readByte((0x01F0 & instruction) >> 4));

        //Read Flags
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
    }

    @Test
    public void INC_TwoComplementOverflow_ReturnNeg128() throws IllegalAccessException {

        instruction = 0x00FE;
        cpuField.set(null, instruction);

        regD = 0x7F;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_INC.executeInstruction();

        //Read Result
        assertEquals(-128, dataMemory.readByte((0x01F0 & instruction) >> 4));

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
    }

    @Test
    public void JMP_JumpFrom256To300() throws Exception {

        instruction = 0x0000;
        cpuField.set(null, instruction);

        //Fill Memory
        programMemory.setPC(256);
        programMemory.writeWord(256,0x12C);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_JMP.executeInstruction();

        //Read Result
        assertEquals(300, programMemory.getPC());
    }

    @Test
    public void JMP_JumpFromFirstToLast() throws Exception {

        instruction = 0x01F1;
        cpuField.set(null, instruction);

        //Fill Memory
        programMemory.setPC(0);
        programMemory.writeWord(0,0xFFFF);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_JMP.executeInstruction();

        //Read Result
        assertEquals(0xFFFF, programMemory.getPC());
    }

    @Test
    public void JMP_JumpFromLastToFirst() throws Exception {

        instruction = 0x0000;
        cpuField.set(null, instruction);

        //Fill Memory
        programMemory.setPC(0x3FFE);
        programMemory.writeWord(0x3FFE,0);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_JMP.executeInstruction();

        //Read Result
        assertEquals(0, programMemory.getPC());
    }

    @Test
    public void LD_X_POST_INCREMENT_Move0x7EToRegister14() throws Exception {

        instruction = 0x00E0;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(0x200, (byte) 0x7E);
        dataMemory.writeByte(26, (byte) 0x00);
        dataMemory.writeByte(27, (byte) 0x02);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_LD_X_POST_INCREMENT.executeInstruction();

        //Read Result
        assertEquals(0x7E, dataMemory.readByte(14));
        assertEquals(0x01, dataMemory.readByte(26));
        assertEquals(0x02, dataMemory.readByte(27));
    }

    @Test
    public void LD_X_POST_INCREMENT_Move0x7DToRegister15OverflowLow() throws Exception {

        instruction = 0x00F0;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(0x2FF, (byte) 0x7D);
        dataMemory.writeByte(26, (byte) 0xFF);
        dataMemory.writeByte(27, (byte) 0x02);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_LD_X_POST_INCREMENT.executeInstruction();

        //Read Result
        assertEquals(0x7D, dataMemory.readByte(15));
        assertEquals(0x00, dataMemory.readByte(26));
        assertEquals(0x03, dataMemory.readByte(27));
    }

    @Test
    public void LD_X_POST_INCREMENT_Move0x7CToRegister16LastAddress() throws Exception {

        instruction = 0x0100;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(0x8FF, (byte) 0x7C);
        dataMemory.writeByte(26, (byte) 0xFF);
        dataMemory.writeByte(27, (byte) 0x08);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_LD_X_POST_INCREMENT.executeInstruction();

        //Read Result
        assertEquals(0x7C, dataMemory.readByte(16));
        assertEquals(0x00, dataMemory.readByte(26));
        assertEquals(0x09, dataMemory.readByte(27));
    }

    @Test
    public void LD_X_PRE_DECREMENT_Move0x7BToRegister17() throws Exception {

        instruction = 0x0110;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(0x200, (byte) 0x7B);
        dataMemory.writeByte(26, (byte) 0x01);
        dataMemory.writeByte(27, (byte) 0x02);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_LD_X_PRE_DECREMENT.executeInstruction();

        //Read Result
        assertEquals(0x7B, dataMemory.readByte(17));
        assertEquals(0x00, dataMemory.readByte(26));
        assertEquals(0x02, dataMemory.readByte(27));
    }

    @Test
    public void LD_X_PRE_DECREMENT_Move0x7AToRegister18UnderflowLow() throws Exception {

        instruction = 0x0120;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(0x1FF, (byte) 0x7A);
        dataMemory.writeByte(26, (byte) 0x00);
        dataMemory.writeByte(27, (byte) 0x02);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_LD_X_PRE_DECREMENT.executeInstruction();

        //Read Result
        assertEquals(0x7A, dataMemory.readByte(18));
        assertEquals((byte) 0xFF, dataMemory.readByte(26));
        assertEquals(0x01, dataMemory.readByte(27));
    }

    @Test
    public void LD_X_PRE_DECREMENT_Move0x70ToRegister19FirstAddress() throws Exception {

        instruction = 0x0130;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(0x0000, (byte) 0x70);
        dataMemory.writeByte(26, (byte) 0x01);
        dataMemory.writeByte(27, (byte) 0x00);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_LD_X_PRE_DECREMENT.executeInstruction();

        //Read Result
        assertEquals(0x70, dataMemory.readByte(19));
        assertEquals(0x00, dataMemory.readByte(26));
        assertEquals(0x00, dataMemory.readByte(27));
    }

    @Test
    public void LD_X_UNCHANGED_Move0x6FToRegister20() throws Exception {

        instruction = 0x0140;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(0x200, (byte) 0x6F);
        dataMemory.writeByte(26, (byte) 0x00);
        dataMemory.writeByte(27, (byte) 0x02);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_LD_X_UNCHANGED.executeInstruction();

        //Read Result
        assertEquals(0x6F, dataMemory.readByte(20));
        assertEquals(0x00, dataMemory.readByte(26));
        assertEquals(0x02, dataMemory.readByte(27));
    }

    @Test
    public void LD_X_UNCHANGED_Move0x80ToRegister21FirstAddress() throws Exception {

        instruction = 0x0150;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(0x00, (byte) 0x80);
        dataMemory.writeByte(26, (byte) 0x00);
        dataMemory.writeByte(27, (byte) 0x00);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_LD_X_UNCHANGED.executeInstruction();

        //Read Result
        assertEquals((byte) 0x80, dataMemory.readByte(21));
        assertEquals(0x00, dataMemory.readByte(26));
        assertEquals(0x00, dataMemory.readByte(27));
    }

    @Test
    public void LD_X_UNCHANGED_Move0x81ToRegister22LastAddress() throws Exception {

        instruction = 0x0160;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(0x8FF, (byte) 0x81);
        dataMemory.writeByte(26, (byte) 0xFF);
        dataMemory.writeByte(27, (byte) 0x08);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_LD_X_UNCHANGED.executeInstruction();

        //Read Result
        assertEquals((byte) 0x81, dataMemory.readByte(22));
        assertEquals((byte) 0xFF, dataMemory.readByte(26));
        assertEquals(0x08, dataMemory.readByte(27));
    }

    @Test
    public void LD_Y_POST_INCREMENT_Move0x7EToRegister14() throws Exception {

        instruction = 0x00E0;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(0x200, (byte) 0x7E);
        dataMemory.writeByte(28, (byte) 0x00);
        dataMemory.writeByte(29, (byte) 0x02);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_LD_Y_POST_INCREMENT.executeInstruction();

        //Read Result
        assertEquals(0x7E, dataMemory.readByte(14));
        assertEquals(0x01, dataMemory.readByte(28));
        assertEquals(0x02, dataMemory.readByte(29));
    }

    @Test
    public void LD_Y_POST_INCREMENT_Move0x7DToRegister15OverflowLow() throws Exception {

        instruction = 0x00F0;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(0x2FF, (byte) 0x7D);
        dataMemory.writeByte(28, (byte) 0xFF);
        dataMemory.writeByte(29, (byte) 0x02);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_LD_Y_POST_INCREMENT.executeInstruction();

        //Read Result
        assertEquals(0x7D, dataMemory.readByte(15));
        assertEquals(0x00, dataMemory.readByte(28));
        assertEquals(0x03, dataMemory.readByte(29));
    }

    @Test
    public void LD_Y_POST_INCREMENT_Move0x7CToRegister16LastAddress() throws Exception {

        instruction = 0x0100;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(0x8FF, (byte) 0x7C);
        dataMemory.writeByte(28, (byte) 0xFF);
        dataMemory.writeByte(29, (byte) 0x08);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_LD_Y_POST_INCREMENT.executeInstruction();

        //Read Result
        assertEquals(0x7C, dataMemory.readByte(16));
        assertEquals(0x00, dataMemory.readByte(28));
        assertEquals(0x09, dataMemory.readByte(29));
    }

    @Test
    public void LD_Y_PRE_DECREMENT_Move0x7BToRegister17() throws Exception {

        instruction = 0x0110;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(0x200, (byte) 0x7B);
        dataMemory.writeByte(28, (byte) 0x01);
        dataMemory.writeByte(29, (byte) 0x02);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_LD_Y_PRE_DECREMENT.executeInstruction();

        //Read Result
        assertEquals(0x7B, dataMemory.readByte(17));
        assertEquals(0x00, dataMemory.readByte(28));
        assertEquals(0x02, dataMemory.readByte(29));
    }

    @Test
    public void LD_Y_PRE_DECREMENT_Move0x7AToRegister18UnderflowLow() throws Exception {

        instruction = 0x0120;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(0x1FF, (byte) 0x7A);
        dataMemory.writeByte(28, (byte) 0x00);
        dataMemory.writeByte(29, (byte) 0x02);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_LD_Y_PRE_DECREMENT.executeInstruction();

        //Read Result
        assertEquals(0x7A, dataMemory.readByte(18));
        assertEquals((byte) 0xFF, dataMemory.readByte(28));
        assertEquals(0x01, dataMemory.readByte(29));
    }

    @Test
    public void LD_Y_PRE_DECREMENT_Move0x70ToRegister19FirstAddress() throws Exception {

        instruction = 0x0130;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(0x0000, (byte) 0x70);
        dataMemory.writeByte(28, (byte) 0x01);
        dataMemory.writeByte(29, (byte) 0x00);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_LD_Y_PRE_DECREMENT.executeInstruction();

        //Read Result
        assertEquals(0x70, dataMemory.readByte(19));
        assertEquals(0x00, dataMemory.readByte(28));
        assertEquals(0x00, dataMemory.readByte(29));
    }

    @Test
    public void LD_Y_UNCHANGED_Move0x6FToRegister20() throws Exception {

        instruction = 0x0140;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(0x200, (byte) 0x6F);
        dataMemory.writeByte(28, (byte) 0x00);
        dataMemory.writeByte(29, (byte) 0x02);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_LD_Y_UNCHANGED.executeInstruction();

        //Read Result
        assertEquals(0x6F, dataMemory.readByte(20));
        assertEquals(0x00, dataMemory.readByte(28));
        assertEquals(0x02, dataMemory.readByte(29));
    }

    @Test
    public void LD_Y_UNCHANGED_Move0x80ToRegister21FirstAddress() throws Exception {

        instruction = 0x0150;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(0x00, (byte) 0x80);
        dataMemory.writeByte(28, (byte) 0x00);
        dataMemory.writeByte(29, (byte) 0x00);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_LD_Y_UNCHANGED.executeInstruction();

        //Read Result
        assertEquals((byte) 0x80, dataMemory.readByte(21));
        assertEquals(0x00, dataMemory.readByte(28));
        assertEquals(0x00, dataMemory.readByte(29));
    }

    @Test
    public void LD_Y_UNCHANGED_Move0x81ToRegister22LastAddress() throws Exception {

        instruction = 0x0160;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(0x8FF, (byte) 0x81);
        dataMemory.writeByte(28, (byte) 0xFF);
        dataMemory.writeByte(29, (byte) 0x08);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_LD_Y_UNCHANGED.executeInstruction();

        //Read Result
        assertEquals((byte) 0x81, dataMemory.readByte(22));
        assertEquals((byte) 0xFF, dataMemory.readByte(28));
        assertEquals(0x08, dataMemory.readByte(29));
    }

    @Test
    public void LD_Z_POST_INCREMENT_Move0x7EToRegister14() throws Exception {

        instruction = 0x00E0;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(0x200, (byte) 0x7E);
        dataMemory.writeByte(30, (byte) 0x00);
        dataMemory.writeByte(31, (byte) 0x02);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_LD_Z_POST_INCREMENT.executeInstruction();

        //Read Result
        assertEquals(0x7E, dataMemory.readByte(14));
        assertEquals(0x01, dataMemory.readByte(30));
        assertEquals(0x02, dataMemory.readByte(31));
    }

    @Test
    public void LD_Z_POST_INCREMENT_Move0x7DToRegister15OverflowLow() throws Exception {

        instruction = 0x00F0;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(0x2FF, (byte) 0x7D);
        dataMemory.writeByte(30, (byte) 0xFF);
        dataMemory.writeByte(31, (byte) 0x02);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_LD_Z_POST_INCREMENT.executeInstruction();

        //Read Result
        assertEquals(0x7D, dataMemory.readByte(15));
        assertEquals(0x00, dataMemory.readByte(30));
        assertEquals(0x03, dataMemory.readByte(31));
    }

    @Test
    public void LD_Z_POST_INCREMENT_Move0x7CToRegister16LastAddress() throws Exception {

        instruction = 0x0100;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(0x8FF, (byte) 0x7C);
        dataMemory.writeByte(30, (byte) 0xFF);
        dataMemory.writeByte(31, (byte) 0x08);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_LD_Z_POST_INCREMENT.executeInstruction();

        //Read Result
        assertEquals(0x7C, dataMemory.readByte(16));
        assertEquals(0x00, dataMemory.readByte(30));
        assertEquals(0x09, dataMemory.readByte(31));
    }

    @Test
    public void LD_Z_PRE_DECREMENT_Move0x7BToRegister17() throws Exception {

        instruction = 0x0110;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(0x200, (byte) 0x7B);
        dataMemory.writeByte(30, (byte) 0x01);
        dataMemory.writeByte(31, (byte) 0x02);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_LD_Z_PRE_DECREMENT.executeInstruction();

        //Read Result
        assertEquals(0x7B, dataMemory.readByte(17));
        assertEquals(0x00, dataMemory.readByte(30));
        assertEquals(0x02, dataMemory.readByte(31));
    }

    @Test
    public void LD_Z_PRE_DECREMENT_Move0x7AToRegister18UnderflowLow() throws Exception {

        instruction = 0x0120;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(0x1FF, (byte) 0x7A);
        dataMemory.writeByte(30, (byte) 0x00);
        dataMemory.writeByte(31, (byte) 0x02);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_LD_Z_PRE_DECREMENT.executeInstruction();

        //Read Result
        assertEquals(0x7A, dataMemory.readByte(18));
        assertEquals((byte) 0xFF, dataMemory.readByte(30));
        assertEquals(0x01, dataMemory.readByte(31));
    }

    @Test
    public void LD_Z_PRE_DECREMENT_Move0x70ToRegister19FirstAddress() throws Exception {

        instruction = 0x0130;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(0x0000, (byte) 0x70);
        dataMemory.writeByte(30, (byte) 0x01);
        dataMemory.writeByte(31, (byte) 0x00);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_LD_Z_PRE_DECREMENT.executeInstruction();

        //Read Result
        assertEquals(0x70, dataMemory.readByte(19));
        assertEquals(0x00, dataMemory.readByte(30));
        assertEquals(0x00, dataMemory.readByte(31));
    }

    @Test
    public void LD_Z_UNCHANGED_Move0x6FToRegister20() throws Exception {

        instruction = 0x0140;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(0x200, (byte) 0x6F);
        dataMemory.writeByte(30, (byte) 0x00);
        dataMemory.writeByte(31, (byte) 0x02);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_LD_Z_UNCHANGED.executeInstruction();

        //Read Result
        assertEquals(0x6F, dataMemory.readByte(20));
        assertEquals(0x00, dataMemory.readByte(30));
        assertEquals(0x02, dataMemory.readByte(31));
    }

    @Test
    public void LD_Z_UNCHANGED_Move0x80ToRegister21FirstAddress() throws Exception {

        instruction = 0x0150;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(0x00, (byte) 0x80);
        dataMemory.writeByte(30, (byte) 0x00);
        dataMemory.writeByte(31, (byte) 0x00);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_LD_Z_UNCHANGED.executeInstruction();

        //Read Result
        assertEquals((byte) 0x80, dataMemory.readByte(21));
        assertEquals(0x00, dataMemory.readByte(30));
        assertEquals(0x00, dataMemory.readByte(31));
    }

    @Test
    public void LD_Z_UNCHANGED_Move0x81ToRegister22LastAddress() throws Exception {

        instruction = 0x0160;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(0x8FF, (byte) 0x81);
        dataMemory.writeByte(30, (byte) 0xFF);
        dataMemory.writeByte(31, (byte) 0x08);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_LD_Z_UNCHANGED.executeInstruction();

        //Read Result
        assertEquals((byte) 0x81, dataMemory.readByte(22));
        assertEquals((byte) 0xFF, dataMemory.readByte(30));
        assertEquals(0x08, dataMemory.readByte(31));
    }

    @Test
    public void LDD_Y_Move0x01ToRegister01MinDisplacement() throws Exception {

        instruction = 0x0010;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(0x200, (byte) 0x01);
        dataMemory.writeByte(28, (byte) 0x00);
        dataMemory.writeByte(29, (byte) 0x02);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_LDD_Y.executeInstruction();

        //Read Result
        assertEquals((byte) 0x01, dataMemory.readByte(1));
        assertEquals((byte) 0x00, dataMemory.readByte(28));
        assertEquals(0x02, dataMemory.readByte(29));
    }

    @Test
    public void LDD_Y_Move0x02ToRegister02MaxDisplacement() throws Exception {

        instruction = 0x2C27;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(0x23F, (byte) 0x02);
        dataMemory.writeByte(28, (byte) 0x00);
        dataMemory.writeByte(29, (byte) 0x02);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_LDD_Y.executeInstruction();

        //Read Result
        assertEquals((byte) 0x02, dataMemory.readByte(2));
        assertEquals((byte) 0x00, dataMemory.readByte(28));
        assertEquals(0x02, dataMemory.readByte(29));
    }

    @Test
    public void LDD_Z_Move0x01ToRegister01MinDisplacement() throws Exception {

        instruction = 0x0010;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(0x200, (byte) 0x01);
        dataMemory.writeByte(30, (byte) 0x00);
        dataMemory.writeByte(31, (byte) 0x02);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_LDD_Z.executeInstruction();

        //Read Result
        assertEquals((byte) 0x01, dataMemory.readByte(1));
        assertEquals((byte) 0x00, dataMemory.readByte(30));
        assertEquals(0x02, dataMemory.readByte(31));
    }

    @Test
    public void LDD_Z_Move0x02ToRegister02MaxDisplacement() throws Exception {

        instruction = 0x2C27;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(0x23F, (byte) 0x02);
        dataMemory.writeByte(30, (byte) 0x00);
        dataMemory.writeByte(31, (byte) 0x02);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_LDD_Z.executeInstruction();

        //Read Result
        assertEquals((byte) 0x02, dataMemory.readByte(2));
        assertEquals((byte) 0x00, dataMemory.readByte(30));
        assertEquals(0x02, dataMemory.readByte(31));
    }

    @Test
    public void LDI_Move0x83ToRegister017() throws Exception {

        instruction = 0x0813;
        cpuField.set(null, instruction);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_LDI.executeInstruction();

        //Read Result
        assertEquals((byte) 0x83, dataMemory.readByte(17));
    }

    @Test
    public void LDS_Move0xFFFrom0x0345ToRegister0() throws Exception {

        instruction = 0x0000;
        cpuField.set(null, instruction);

        //Fill Memory
        programMemory.setPC(0);
        programMemory.writeWord(0,0x0345);
        dataMemory.writeByte(0x0345, (byte) 0xFF);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_LDS.executeInstruction();

        //Read Result
        assertEquals((byte) 0xFF, dataMemory.readByte(0));
    }

    @Test
    public void LPM_Z_POST_INCREMENT_Move0x1234FromProgramMemoryToRegister1_LowByte() throws Exception {

        instruction = 0x0010;
        cpuField.set(null, instruction);

        //Fill Memory
        programMemory.writeWord(100,0x1234);    //Word Address
        dataMemory.writeByte(30, (byte) 0xC8);    //Byte Address
        dataMemory.writeByte(31, (byte) 0x00);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_LPM_Z_POST_INCREMENT.executeInstruction();

        //Read Result
        assertEquals((byte) 0x34, dataMemory.readByte(1));
        assertEquals((byte) 0xC9, dataMemory.readByte(30));
        assertEquals((byte) 0x00, dataMemory.readByte(31));
    }

    @Test
    public void LPM_Z_POST_INCREMENT_Move0x1234FromProgramMemoryToRegister1_HighByte() throws Exception {

        instruction = 0x0010;
        cpuField.set(null, instruction);

        //Fill Memory
        programMemory.writeWord(100,0x1234);    //Word Address
        dataMemory.writeByte(30, (byte) 0xC9);    //Byte Address
        dataMemory.writeByte(31, (byte) 0x00);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_LPM_Z_POST_INCREMENT.executeInstruction();

        //Read Result
        assertEquals((byte) 0x12, dataMemory.readByte(1));
        assertEquals((byte) 0xCA, dataMemory.readByte(30));
        assertEquals((byte) 0x00, dataMemory.readByte(31));
    }

    @Test
    public void LPM_Z_LPM_Z_UNCHANGED_DEST_R0_Move0x1234FromProgramMemory_LowByte() throws Exception {

        instruction = 0x0010;
        cpuField.set(null, instruction);

        //Fill Memory
        programMemory.writeWord(100,0x1234);    //Word Address
        dataMemory.writeByte(30, (byte) 0xC8);    //Byte Address
        dataMemory.writeByte(31, (byte) 0x00);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_LPM_Z_UNCHANGED_DEST_R0.executeInstruction();

        //Read Result
        assertEquals((byte) 0x34, dataMemory.readByte(0));
        assertEquals((byte) 0xC8, dataMemory.readByte(30));
        assertEquals((byte) 0x00, dataMemory.readByte(31));
    }

    @Test
    public void LPM_Z_LPM_Z_UNCHANGED_DEST_R0_Move0x1234FromProgramMemory_HighByte() throws Exception {

        instruction = 0x0010;
        cpuField.set(null, instruction);

        //Fill Memory
        programMemory.writeWord(100,0x1234);    //Word Address
        dataMemory.writeByte(30, (byte) 0xC9);    //Byte Address
        dataMemory.writeByte(31, (byte) 0x00);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_LPM_Z_UNCHANGED_DEST_R0.executeInstruction();

        //Read Result
        assertEquals((byte) 0x12, dataMemory.readByte(0));
        assertEquals((byte) 0xC9, dataMemory.readByte(30));
        assertEquals((byte) 0x00, dataMemory.readByte(31));
    }

    @Test
    public void LPM_Z_LPM_Z_UNCHANGED_Move0x1234FromProgramMemoryToRegister1_LowByte() throws Exception {

        instruction = 0x0010;
        cpuField.set(null, instruction);

        //Fill Memory
        programMemory.writeWord(100,0x1234);    //Word Address
        dataMemory.writeByte(30, (byte) 0xC8);    //Byte Address
        dataMemory.writeByte(31, (byte) 0x00);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_LPM_Z_UNCHANGED.executeInstruction();

        //Read Result
        assertEquals((byte) 0x34, dataMemory.readByte(1));
        assertEquals((byte) 0xC8, dataMemory.readByte(30));
        assertEquals((byte) 0x00, dataMemory.readByte(31));
    }

    @Test
    public void LPM_Z_LPM_Z_UNCHANGED_Move0x1234FromProgramMemoryToRegister1_HighByte() throws Exception {

        instruction = 0x0010;
        cpuField.set(null, instruction);

        //Fill Memory
        programMemory.writeWord(100,0x1234);    //Word Address
        dataMemory.writeByte(30, (byte) 0xC9);    //Byte Address
        dataMemory.writeByte(31, (byte) 0x00);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_LPM_Z_UNCHANGED.executeInstruction();

        //Read Result
        assertEquals((byte) 0x12, dataMemory.readByte(1));
        assertEquals((byte) 0xC9, dataMemory.readByte(30));
        assertEquals((byte) 0x00, dataMemory.readByte(31));
    }

    @Test
    public void LSR_ShiftRight_Return2() throws IllegalAccessException {

        instruction = 0x00F0;
        cpuField.set(null, instruction);

        regD = 4;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_LSR.executeInstruction();

        //Read Result
        assertEquals(2, dataMemory.readByte((0x01F0 & instruction) >> 4));

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void LSR_ShiftRightSignExtension_Return64() throws IllegalAccessException {

        instruction = 0x00F0;
        cpuField.set(null, instruction);

        regD = -128;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_LSR.executeInstruction();

        //Read Result
        assertEquals(64, dataMemory.readByte((0x01F0 & instruction) >> 4));

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void LSR_LastShift_Return0() throws IllegalAccessException {

        instruction = 0x00F0;
        cpuField.set(null, instruction);

        regD = 1;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_LSR.executeInstruction();

        //Read Result
        assertEquals(0, dataMemory.readByte((0x01F0 & instruction) >> 4));

        //Read Flags
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void LSR_AllZero_Return0() throws IllegalAccessException {

        instruction = 0x00F0;
        cpuField.set(null, instruction);

        regD = 0;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_LSR.executeInstruction();

        //Read Result
        assertEquals(0, dataMemory.readByte((0x01F0 & instruction) >> 4));

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void MOV_Move0x7FFromR0To31() throws IllegalAccessException {

        instruction = 0x01F0;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(0, (byte) 0x7F);
        dataMemory.writeByte(31, (byte) 0);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_MOV.executeInstruction();

        //Read Result
        assertEquals(0x7F, dataMemory.readByte(31));

    }

    @Test
    public void MOVW_Move0x087FFromR1R0ToR31R30() throws IllegalAccessException {

        instruction = 0x00F0;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(0, (byte) 0x7F);
        dataMemory.writeByte(1, (byte) 0x08);
        dataMemory.writeByte(30, (byte) 0);
        dataMemory.writeByte(31, (byte) 0);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_MOVW.executeInstruction();

        //Read Result
        assertEquals(0x7F, dataMemory.readByte(30));
        assertEquals(0x08, dataMemory.readByte(31));

    }

    @Test
    public void MUL_0x40Times0x40_Result0x1000() throws IllegalAccessException {

        instruction = 0x00FE;
        cpuField.set(null, instruction);

        regD = 0x40;
        regR = 0x40;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);
        dataMemory.writeByte(((0x0200 & instruction) >> 5) | (0x000F & instruction), regR);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_MUL.executeInstruction();

        //Read Result
        assertEquals(0x10, dataMemory.readByte(0x01));
        assertEquals(0x00, dataMemory.readByte(0x00));

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void MUL_0x40Times0_Result0() throws IllegalAccessException {

        instruction = 0x00FE;
        cpuField.set(null, instruction);

        regD = 0x40;
        regR = 0;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);
        dataMemory.writeByte(((0x0200 & instruction) >> 5) | (0x000F & instruction), regR);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_MUL.executeInstruction();

        //Read Result
        assertEquals(0x00, dataMemory.readByte(0x01));
        assertEquals(0x00, dataMemory.readByte(0x00));

        //Read Flags
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void MUL_0xFFTimes0xFF_Result0xFE01() throws IllegalAccessException {

        instruction = 0x00FE;
        cpuField.set(null, instruction);

        regD = -1; //0xFF
        regR = -1; //0xFF

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);
        dataMemory.writeByte(((0x0200 & instruction) >> 5) | (0x000F & instruction), regR);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_MUL.executeInstruction();

        //Read Result
        assertEquals((byte) 0xFE, dataMemory.readByte(0x01));
        assertEquals(0x01, dataMemory.readByte(0x00));

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void MULS_0x40Times0x40_Result0x1000() throws IllegalAccessException {

        instruction = 0x00FE;
        cpuField.set(null, instruction);

        regD = 0x40;
        regR = 0x40;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);
        dataMemory.writeByte(((0x0200 & instruction) >> 5) | (0x000F & instruction), regR);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_MULS.executeInstruction();

        //Read Result
        assertEquals(0x10, dataMemory.readByte(0x01));
        assertEquals(0x00, dataMemory.readByte(0x00));

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void MULS_0x40Times0_Result0() throws IllegalAccessException {

        instruction = 0x00FE;
        cpuField.set(null, instruction);

        regD = 0x40;
        regR = 0;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);
        dataMemory.writeByte(((0x0200 & instruction) >> 5) | (0x000F & instruction), regR);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_MULS.executeInstruction();

        //Read Result
        assertEquals(0x00, dataMemory.readByte(0x01));
        assertEquals(0x00, dataMemory.readByte(0x00));

        //Read Flags
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void MULS_0xFFTimes0xFF_Result0x0001() throws IllegalAccessException {

        instruction = 0x00FE;
        cpuField.set(null, instruction);

        regD = -1; //0xFF
        regR = -1; //0xFF

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);
        dataMemory.writeByte(((0x0200 & instruction) >> 5) | (0x000F & instruction), regR);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_MULS.executeInstruction();

        //Read Result
        assertEquals((byte) 0x00, dataMemory.readByte(0x01));
        assertEquals(0x01, dataMemory.readByte(0x00));

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void MULS_0xFFTimes0x7F_Result0xFF81() throws IllegalAccessException {

        instruction = 0x00FE;
        cpuField.set(null, instruction);

        regD = -1; //0xFF
        regR = 0x7F;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);
        dataMemory.writeByte(((0x0200 & instruction) >> 5) | (0x000F & instruction), regR);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_MULS.executeInstruction();

        //Read Result
        assertEquals((byte) 0xFF, dataMemory.readByte(0x01));
        assertEquals((byte) 0x81, dataMemory.readByte(0x00));

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void MULSU_0x40Times0x40_Result0x1000() throws IllegalAccessException {

        instruction = 0x00FE;
        cpuField.set(null, instruction);

        regD = 0x40;
        regR = 0x40;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);
        dataMemory.writeByte(((0x0200 & instruction) >> 5) | (0x000F & instruction), regR);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_MULSU.executeInstruction();

        //Read Result
        assertEquals(0x10, dataMemory.readByte(0x01));
        assertEquals(0x00, dataMemory.readByte(0x00));

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void MULSU_0x40Times0_Result0() throws IllegalAccessException {

        instruction = 0x00FE;
        cpuField.set(null, instruction);

        regD = 0x40;
        regR = 0;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);
        dataMemory.writeByte(((0x0200 & instruction) >> 5) | (0x000F & instruction), regR);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_MULSU.executeInstruction();

        //Read Result
        assertEquals(0x00, dataMemory.readByte(0x01));
        assertEquals(0x00, dataMemory.readByte(0x00));

        //Read Flags
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void MULSU_0xFFTimes0xFF_Result0xFF01() throws IllegalAccessException {

        instruction = 0x00FE;
        cpuField.set(null, instruction);

        regD = -1; //0xFF
        regR = -1; //0xFF

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);
        dataMemory.writeByte(((0x0200 & instruction) >> 5) | (0x000F & instruction), regR);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_MULSU.executeInstruction();

        //Read Result
        assertEquals((byte) 0xFF, dataMemory.readByte(0x01));
        assertEquals(0x01, dataMemory.readByte(0x00));

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void MULSU_0xFFTimes0x7F_Result0xFF81() throws IllegalAccessException {

        instruction = 0x00FE;
        cpuField.set(null, instruction);

        regD = -1; //0xFF
        regR = 0x7F;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);
        dataMemory.writeByte(((0x0200 & instruction) >> 5) | (0x000F & instruction), regR);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_MULSU.executeInstruction();

        //Read Result
        assertEquals((byte) 0xFF, dataMemory.readByte(0x01));
        assertEquals((byte) 0x81, dataMemory.readByte(0x00));

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void NEG_Zero_Return0() throws IllegalAccessException {

        instruction = 0x00F0;
        cpuField.set(null, instruction);

        regD = 0; //0xFF

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_NEG.executeInstruction();

        //Read Result
        assertEquals((byte) 0x00, dataMemory.readByte((0x01F0 & instruction) >> 4));

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,5));//H


    }

    @Test
    public void NEG_0x80_Return0x80() throws IllegalAccessException {

        instruction = 0x00F0;
        cpuField.set(null, instruction);

        regD = -128; //0x80

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_NEG.executeInstruction();

        //Read Result
        assertEquals((byte) 0x80, dataMemory.readByte((0x01F0 & instruction) >> 4));

        //Read Flags
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,5));//H

    }

    @Test
    public void NEG_0x08_Return0xF8() throws IllegalAccessException {

        instruction = 0x00F0;
        cpuField.set(null, instruction);

        regD = 0x08; //0x80

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_NEG.executeInstruction();

        //Read Result
        assertEquals((byte) 0xF8, dataMemory.readByte((0x01F0 & instruction) >> 4));

        //Read Flags
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,5));//H

    }

    @Test
    public void OR_ZeroAndZero_Return0() throws IllegalAccessException {

        instruction = 0x00FE;
        cpuField.set(null, instruction);

        regD = 0;
        regR = 0;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);
        dataMemory.writeByte(((0x0200 & instruction) >> 5) | (0x000F & instruction), regR);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_OR.executeInstruction();

        //Read Result
        assertEquals(0, dataMemory.readByte((0x01F0 & instruction) >> 4));

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z

    }

    @Test
    public void OR_ZeroAndFF_ReturnFF() throws IllegalAccessException {

        instruction = 0x00FE;
        cpuField.set(null, instruction);

        regD = 0;
        regR = -1;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);
        dataMemory.writeByte(((0x0200 & instruction) >> 5) | (0x000F & instruction), regR);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_OR.executeInstruction();

        //Read Result
        assertEquals((byte) 0xFF, dataMemory.readByte((0x01F0 & instruction) >> 4));

        //Read Flags
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z

    }

    @Test
    public void OR_FFAndF0_ReturnFF() throws IllegalAccessException {

        instruction = 0x00FE;
        cpuField.set(null, instruction);

        regD = -16;//0xF0
        regR = -1;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);
        dataMemory.writeByte(((0x0200 & instruction) >> 5) | (0x000F & instruction), regR);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_OR.executeInstruction();

        //Read Result
        assertEquals((byte) 0xFF, dataMemory.readByte((0x01F0 & instruction) >> 4));

        //Read Flags
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z

    }

    @Test
    public void ORI_ZeroAndZero_Return0() throws IllegalAccessException {

        instruction = 0x00F0;
        cpuField.set(null, instruction);

        regD = 0;

        //Fill Memory
        dataMemory.writeByte((0x10 | (0x00F0 & instruction) >> 4), regD);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_ORI.executeInstruction();

        //Read Result
        assertEquals(0, dataMemory.readByte((0x10 | (0x00F0 & instruction) >> 4)));

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z

    }

    @Test
    public void ORI_ZeroAndFF_ReturnFF() throws IllegalAccessException {

        instruction = 0x0FFF;
        cpuField.set(null, instruction);

        regD = 0;

        //Fill Memory
        dataMemory.writeByte((0x10 | (0x00F0 & instruction) >> 4), regD);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_ORI.executeInstruction();

        //Read Result
        assertEquals((byte)0xFF, dataMemory.readByte((0x10 | (0x00F0 & instruction) >> 4)));

        //Read Flags
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z

    }

    @Test
    public void ORI_FFAndF0_ReturnFF() throws IllegalAccessException {

        instruction = 0x0FF0;
        cpuField.set(null, instruction);

        regD = -1;

        //Fill Memory
        dataMemory.writeByte((0x10 | (0x00F0 & instruction) >> 4), regD);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_ORI.executeInstruction();

        //Read Result
        assertEquals((byte) 0xFF, dataMemory.readByte((0x10 | (0x00F0 & instruction) >> 4)));

        //Read Flags
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z

    }

    @Test
    public void OUT_Load0x77ToDDRB() throws Exception {

        instruction = 0x0004;
        cpuField.set(null, instruction);

        regD = 0x77;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_OUT.executeInstruction();

        //Read Result
        assertEquals(0x77, dataMemory.readByte(0x24));
    }

    @Test
    public void OUT_Load0x76ToPORTD() throws Exception {

        instruction = 0x000B;
        cpuField.set(null, instruction);

        regD = 0x76;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_OUT.executeInstruction();

        //Read Result
        assertEquals(0x76, dataMemory.readByte(0x2B));
    }

    @Test
    public void POP_Pop0x12() throws Exception {

        instruction = 0x00F0;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(0x400, (byte) 0x12);
        dataMemory.writeByte(DataMemory_ATmega328P.SPH_ADDR, (byte) 0x04);
        dataMemory.writeByte(DataMemory_ATmega328P.SPL_ADDR, (byte) 0x00);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_POP.executeInstruction();

        //Read Result

        //Stack in correct position?
        assertEquals(0x04, dataMemory.readByte(DataMemory_ATmega328P.SPH_ADDR));
        assertEquals((byte) 0x01, dataMemory.readByte(DataMemory_ATmega328P.SPL_ADDR));

        //New PC Value correct?
        assertEquals(0x12, dataMemory.readByte(0x0F));
    }

    @Test
    public void POP_Pop0x34() throws Exception {

        instruction = 0x00F0;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(0x8FF, (byte) 0x34);
        dataMemory.writeByte(DataMemory_ATmega328P.SPH_ADDR, (byte) 0x08);
        dataMemory.writeByte(DataMemory_ATmega328P.SPL_ADDR, (byte) 0xFF);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_POP.executeInstruction();

        //Read Result

        //Stack in correct position?
        assertEquals(0x09, dataMemory.readByte(DataMemory_ATmega328P.SPH_ADDR));
        assertEquals((byte) 0x00, dataMemory.readByte(DataMemory_ATmega328P.SPL_ADDR));

        //New PC Value correct?
        assertEquals(0x34, dataMemory.readByte(0x0F));
    }

    @Test
    public void PUSH_Push0x56() throws Exception {

        instruction = 0x00F0;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(0x0F, (byte) 0x56);
        dataMemory.writeByte(DataMemory_ATmega328P.SPH_ADDR, (byte) 0x08);
        dataMemory.writeByte(DataMemory_ATmega328P.SPL_ADDR, (byte) 0xFF);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_PUSH.executeInstruction();

        //Read Result

        //Stack in correct position?
        assertEquals(0x08, dataMemory.readByte(DataMemory_ATmega328P.SPH_ADDR));
        assertEquals((byte) 0xFE, dataMemory.readByte(DataMemory_ATmega328P.SPL_ADDR));

        int stackPointer = (dataMemory.readByte(DataMemory_ATmega328P.SPH_ADDR) << 8) |
                (0x000000FF & dataMemory.readByte(DataMemory_ATmega328P.SPL_ADDR));

        byte value = dataMemory.readByte(stackPointer);

        //Value correct in stack?
        assertEquals(0x56, value);
    }

    @Test
    public void PUSH_Push0x78() throws Exception {

        instruction = 0x00F0;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(0x0F, (byte) 0x78);
        dataMemory.writeByte(DataMemory_ATmega328P.SPH_ADDR, (byte) 0x00);
        dataMemory.writeByte(DataMemory_ATmega328P.SPL_ADDR, (byte) 0x01);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_PUSH.executeInstruction();

        //Read Result

        //Stack in correct position?
        assertEquals(0x00, dataMemory.readByte(DataMemory_ATmega328P.SPH_ADDR));
        assertEquals((byte) 0x00, dataMemory.readByte(DataMemory_ATmega328P.SPL_ADDR));

        int stackPointer = (dataMemory.readByte(DataMemory_ATmega328P.SPH_ADDR) << 8) |
                (0x000000FF & dataMemory.readByte(DataMemory_ATmega328P.SPL_ADDR));

        byte value = dataMemory.readByte(stackPointer);

        //Value correct in stack?
        assertEquals(0x78, value);
    }

    @Test
    public void RCALL_JumpMin() throws Exception {

        instruction = 0x0000;
        cpuField.set(null, instruction);

        //Fill Memory
        programMemory.setPC(10);
        dataMemory.writeByte(DataMemory_ATmega328P.SPH_ADDR, (byte) 0x08);
        dataMemory.writeByte(DataMemory_ATmega328P.SPL_ADDR, (byte) 0xFF);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_RCALL.executeInstruction();

        //Read Result

        //Stack in correct position?
        assertEquals(0x08, dataMemory.readByte(DataMemory_ATmega328P.SPH_ADDR));
        assertEquals(-3, dataMemory.readByte(DataMemory_ATmega328P.SPL_ADDR));

        int stackPointer = (dataMemory.readByte(DataMemory_ATmega328P.SPH_ADDR) << 8) |
                (0x000000FF & dataMemory.readByte(DataMemory_ATmega328P.SPL_ADDR));

        byte pcHigh = dataMemory.readByte(stackPointer);
        stackPointer += 1;
        byte pcLow = dataMemory.readByte(stackPointer);

        //PC Correct in stack?
        assertEquals(0, pcHigh);
        assertEquals(10, pcLow);

        //New PC Value correct?
        assertEquals(10, programMemory.getPC());
    }

    @Test
    public void RCALL_JumpMaxPositive() throws Exception {

        instruction = 0x07FF;
        cpuField.set(null, instruction);

        //Fill Memory
        programMemory.setPC(10);
        dataMemory.writeByte(DataMemory_ATmega328P.SPH_ADDR, (byte) 0x08);
        dataMemory.writeByte(DataMemory_ATmega328P.SPL_ADDR, (byte) -1);//0xFF

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_RCALL.executeInstruction();

        //Read Result

        //Stack in correct position?
        assertEquals(0x08, dataMemory.readByte(DataMemory_ATmega328P.SPH_ADDR));
        assertEquals(-3, dataMemory.readByte(DataMemory_ATmega328P.SPL_ADDR));

        int stackPointer = (dataMemory.readByte(DataMemory_ATmega328P.SPH_ADDR) << 8) |
                (0x000000FF & dataMemory.readByte(DataMemory_ATmega328P.SPL_ADDR));

        byte pcHigh = dataMemory.readByte(stackPointer);
        stackPointer += 1;
        byte pcLow = dataMemory.readByte(stackPointer);

        //PC Correct in stack?
        assertEquals(0, pcHigh);
        assertEquals(10, pcLow);

        //New PC Value correct? (16-bits PC)
        assertEquals(0x0809, programMemory.getPC());
    }

    @Test
    public void RCALL_JumpMaxNegative() throws Exception {

        instruction = 0x0800;
        cpuField.set(null, instruction);

        //Fill Memory
        programMemory.setPC(4000);
        dataMemory.writeByte(DataMemory_ATmega328P.SPH_ADDR, (byte) 0x08);
        dataMemory.writeByte(DataMemory_ATmega328P.SPL_ADDR, (byte) -1);//0xFF

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_RCALL.executeInstruction();

        //Read Result

        //Stack in correct position?
        assertEquals(0x08, dataMemory.readByte(DataMemory_ATmega328P.SPH_ADDR));
        assertEquals(-3, dataMemory.readByte(DataMemory_ATmega328P.SPL_ADDR));

        int stackPointer = (dataMemory.readByte(DataMemory_ATmega328P.SPH_ADDR) << 8) |
                (0x000000FF & dataMemory.readByte(DataMemory_ATmega328P.SPL_ADDR));

        byte pcHigh = dataMemory.readByte(stackPointer);
        stackPointer += 1;
        byte pcLow = dataMemory.readByte(stackPointer);

        //PC Correct in stack?
        assertEquals(0x0F, pcHigh);
        assertEquals((byte) 0xA0, pcLow);

        //New PC Value correct? (16-bits PC)
        assertEquals(0x07A0, programMemory.getPC());
    }

    @Test
    public void RET_RETI_Recover0x1234() throws Exception {

        instruction = 0x0000;
        cpuField.set(null, instruction);

        //Fill Memory
        programMemory.setPC(0);
        dataMemory.writeByte(0x400, (byte) 0x12);   //Little - Endian
        dataMemory.writeByte(0x401, (byte) 0x34);
        dataMemory.writeByte(DataMemory_ATmega328P.SPH_ADDR, (byte) 0x04);
        dataMemory.writeByte(DataMemory_ATmega328P.SPL_ADDR, (byte) 0x00);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_RET.executeInstruction();

        //Read Result

        //Stack in correct position?
        assertEquals(0x04, dataMemory.readByte(DataMemory_ATmega328P.SPH_ADDR));
        assertEquals((byte) 0x02, dataMemory.readByte(DataMemory_ATmega328P.SPL_ADDR));

        //New PC Value correct?
        assertEquals(0x1234, programMemory.getPC());
    }

    @Test
    public void RET_RETI_Recover0x3412() throws Exception {

        instruction = 0x0000;
        cpuField.set(null, instruction);

        //Fill Memory
        programMemory.setPC(0);
        dataMemory.writeByte(0x400, (byte) 0x34);   //Little - Endian
        dataMemory.writeByte(0x401, (byte) 0x12);
        dataMemory.writeByte(DataMemory_ATmega328P.SPH_ADDR, (byte) 0x04);
        dataMemory.writeByte(DataMemory_ATmega328P.SPL_ADDR, (byte) 0x00);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_RET.executeInstruction();

        //Read Result

        //Stack in correct position?
        assertEquals(0x04, dataMemory.readByte(DataMemory_ATmega328P.SPH_ADDR));
        assertEquals((byte) 0x02, dataMemory.readByte(DataMemory_ATmega328P.SPL_ADDR));

        //New PC Value correct?
        assertEquals(0x3412, programMemory.getPC());
    }

    @Test
    public void RJMP_JumpMin() throws Exception {

        instruction = 0x0000;
        cpuField.set(null, instruction);

        //Fill Memory
        programMemory.setPC(10);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_RJMP.executeInstruction();

        //Read Result

        //New PC Value correct?
        assertEquals(10, programMemory.getPC());
    }

    @Test
    public void RJMP_JumpMaxPositive() throws Exception {

        instruction = 0x07FF;
        cpuField.set(null, instruction);

        //Fill Memory
        programMemory.setPC(10);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_RJMP.executeInstruction();

        //Read Result

        //New PC Value correct? (16-bits PC)
        assertEquals(0x0809, programMemory.getPC());
    }

    @Test
    public void RJMP_JumpMaxNegative() throws Exception {

        instruction = 0x0800;
        cpuField.set(null, instruction);

        //Fill Memory
        programMemory.setPC(4000);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_RJMP.executeInstruction();

        //Read Result

        //New PC Value correct? (16-bits PC)
        assertEquals(0x07A0, programMemory.getPC());
    }

    @Test
    public void ROR_ShiftRight_Return2() throws IllegalAccessException {

        instruction = 0x00F0;
        cpuField.set(null, instruction);

        regD = 4;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_ROR.executeInstruction();

        //Read Result
        assertEquals(2, dataMemory.readByte((0x01F0 & instruction) >> 4));

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void ROR_ShiftRightSignExtension_Return64() throws IllegalAccessException {

        instruction = 0x00F0;
        cpuField.set(null, instruction);

        regD = -128;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_ROR.executeInstruction();

        //Read Result
        assertEquals(64, dataMemory.readByte((0x01F0 & instruction) >> 4));

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void ROR_LastShift_Return0() throws IllegalAccessException {

        instruction = 0x00F0;
        cpuField.set(null, instruction);

        regD = 1;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_ROR.executeInstruction();

        //Read Result
        assertEquals(0, dataMemory.readByte((0x01F0 & instruction) >> 4));

        //Read Flags
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void ROR_AllZero_Return0() throws IllegalAccessException {

        instruction = 0x00F0;
        cpuField.set(null, instruction);

        regD = 0;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_ROR.executeInstruction();

        //Read Result
        assertEquals(0, dataMemory.readByte((0x01F0 & instruction) >> 4));

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void ROR_AllZeroWithCarry_ReturnNeg128() throws IllegalAccessException {

        instruction = 0x00F0;
        cpuField.set(null, instruction);

        regD = 0;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 0,true);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_ROR.executeInstruction();

        //Read Result
        assertEquals((byte)0x80, dataMemory.readByte((0x01F0 & instruction) >> 4));

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void SBC_SubZeroNoCarry_Return0() throws IllegalAccessException {

        instruction = 0x00FE;
        cpuField.set(null, instruction);

        regD = 0;
        regR = 0;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);
        dataMemory.writeByte(((0x0200 & instruction) >> 5) | (0x000F & instruction), regR);
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 0, false);
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 1, false);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_SBC.executeInstruction();

        //Read Result
        assertEquals(0, dataMemory.readByte((0x01F0 & instruction) >> 4));

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,5));//H
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1)); //Z
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void SBC_SubZeroAndCarry_ReturnNeg1() throws IllegalAccessException {

        instruction = 0x00FE;
        cpuField.set(null, instruction);

        regD = 0;
        regR = 0;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);
        dataMemory.writeByte(((0x0200 & instruction) >> 5) | (0x000F & instruction), regR);
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 0, true);
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 1, true);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_SBC.executeInstruction();

        //Read Result
        assertEquals(-1, dataMemory.readByte((0x01F0 & instruction) >> 4));

        //Read Flags
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,5));//H
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void SBC_SubUnderflowNoCarry_ReturnNeg1() throws IllegalAccessException {

        instruction = 0x00FE;
        cpuField.set(null, instruction);

        regD = 0;
        regR = 1;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);
        dataMemory.writeByte(((0x0200 & instruction) >> 5) | (0x000F & instruction), regR);
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 0, false);
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 1, false);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_SBC.executeInstruction();

        //Read Result
        assertEquals(-1, dataMemory.readByte((0x01F0 & instruction) >> 4));

        //Read Flags
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,5));//H
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void SBC_SubTwoComplementOverflowWithCarry_Return127() throws IllegalAccessException {

        instruction = 0x00FE;
        cpuField.set(null, instruction);

        regD = -128;
        regR = 0;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);
        dataMemory.writeByte(((0x0200 & instruction) >> 5) | (0x000F & instruction), regR);
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 0, true);
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 1, true);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_SBC.executeInstruction();

        //Read Result
        assertEquals(127, dataMemory.readByte((0x01F0 & instruction) >> 4));

        //Read Flags
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,5));//H
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void SBCI_SubZeroNoCarry_Return0() throws IllegalAccessException {

        instruction = 0x00F0;
        cpuField.set(null, instruction);

        regD = 0;

        //Fill Memory
        dataMemory.writeByte((0x10 | (0x00F0 & instruction) >> 4), regD);
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 0, false);
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 1, false);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_SBCI.executeInstruction();

        //Read Result
        assertEquals(0, dataMemory.readByte((0x10 | (0x00F0 & instruction) >> 4)));

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,5));//H
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1)); //Z
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void SBCI_SubZeroAndCarry_ReturnNeg1() throws IllegalAccessException {

        instruction = 0x00F0;
        cpuField.set(null, instruction);

        regD = 0;

        //Fill Memory
        dataMemory.writeByte((0x10 | (0x00F0 & instruction) >> 4), regD);
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 0, true);
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 1, false);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_SBCI.executeInstruction();

        //Read Result
        assertEquals(-1, dataMemory.readByte((0x10 | (0x00F0 & instruction) >> 4)));

        //Read Flags
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,5));//H
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void SBCI_SubUnderflowNoCarry_ReturnNeg1() throws IllegalAccessException {

        instruction = 0x00F1;
        cpuField.set(null, instruction);

        regD = 0;

        //Fill Memory
        dataMemory.writeByte((0x10 | (0x00F0 & instruction) >> 4), regD);
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 0, false);
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 1, false);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_SBCI.executeInstruction();

        //Read Result
        assertEquals(-1, dataMemory.readByte((0x10 | (0x00F0 & instruction) >> 4)));

        //Read Flags
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,5));//H
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void SBCI_SubTwoComplementOverflowWithCarry_Return127() throws IllegalAccessException {

        instruction = 0x00F0;
        cpuField.set(null, instruction);

        regD = -128;

        //Fill Memory
        dataMemory.writeByte((0x10 | (0x00F0 & instruction) >> 4), regD);
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 0, true);
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 1, false);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_SBCI.executeInstruction();

        //Read Result
        assertEquals(127, dataMemory.readByte((0x10 | (0x00F0 & instruction) >> 4)));

        //Read Flags
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,5));//H
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void SBCI_SubMaxImediateNoCarry_Return0() throws IllegalAccessException {

        instruction = 0x0FFF;
        cpuField.set(null, instruction);

        regD = -1;

        //Fill Memory
        dataMemory.writeByte((0x10 | (0x00F0 & instruction) >> 4), regD);
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 0, false);
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 1, true);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_SBCI.executeInstruction();

        //Read Result
        assertEquals(0, dataMemory.readByte((0x10 | (0x00F0 & instruction) >> 4)));

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,5));//H
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void SBI_SetFourthBitInPORTB() throws IllegalAccessException {
        instruction = 0x002C;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeBit(DataMemory_ATmega328P.PORTB_ADDR, 4, false);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_SBI.executeInstruction();

        //Read Flags
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.PORTB_ADDR,4));
    }

    @Test
    public void SBI_SetSecondBitInDDRD() throws IllegalAccessException {
        instruction = 0x0052;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeBit(DataMemory_ATmega328P.DDRD_ADDR, 2, false);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_SBI.executeInstruction();

        //Read Flags
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.DDRD_ADDR,2));
    }

    @Test
    public void SBIC_JumpOneWordInstruction() throws IllegalAccessException {

        instruction = 0x0010;
        cpuField.set(null, instruction);

        //Fill Memory
        programMemory.setPC(0);
        programMemory.writeWord(0, 0x0000);
        dataMemory.writeBit(0x22, 0, false);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_SBIC.executeInstruction();

        assertEquals(1, programMemory.getPC());
    }

    @Test
    public void SBIC_JumpTwoWordInstructionSTS() throws IllegalAccessException {

        instruction = 0x0010;
        cpuField.set(null, instruction);

        //Fill Memory
        programMemory.setPC(0);
        programMemory.writeWord(0, 0x9200);
        dataMemory.writeBit(0x22, 0, false);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_SBIC.executeInstruction();

        assertEquals(2, programMemory.getPC());
    }

    @Test
    public void SBIC_JumpTwoWordInstructionLDS() throws IllegalAccessException {

        instruction = 0x0010;
        cpuField.set(null, instruction);

        //Fill Memory
        programMemory.setPC(0);
        programMemory.writeWord(0, 0x90F0);
        dataMemory.writeBit(0x22, 0, false);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_SBIC.executeInstruction();

        assertEquals(2, programMemory.getPC());
    }

    @Test
    public void SBIC_JumpTwoWordInstructionCALL() throws IllegalAccessException {

        instruction = 0x0010;
        cpuField.set(null, instruction);

        //Fill Memory
        programMemory.setPC(0);
        programMemory.writeWord(0, 0x940E);
        dataMemory.writeBit(0x22, 0, false);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_SBIC.executeInstruction();

        assertEquals(2, programMemory.getPC());
    }

    @Test
    public void SBIC_JumpTwoWordInstructionJMP() throws IllegalAccessException {

        instruction = 0x0010;
        cpuField.set(null, instruction);

        //Fill Memory
        programMemory.setPC(0);
        programMemory.writeWord(0, 0x940C);
        dataMemory.writeBit(0x22, 0, false);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_SBIC.executeInstruction();

        assertEquals(2, programMemory.getPC());
    }

    @Test
    public void SBIC_NoJump() throws IllegalAccessException {

        instruction = 0x0010;
        cpuField.set(null, instruction);

        //Fill Memory
        programMemory.setPC(0);
        programMemory.writeWord(0, 0x940C);
        dataMemory.writeBit(0x22, 0, true);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_SBIC.executeInstruction();

        assertEquals(0, programMemory.getPC());
    }

    @Test
    public void SBIS_JumpOneWordInstruction() throws IllegalAccessException {

        instruction = 0x0038;
        cpuField.set(null, instruction);

        //Fill Memory
        programMemory.setPC(0);
        programMemory.writeWord(0, 0x0000);
        dataMemory.writeBit(0x27, 0, true);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_SBIS.executeInstruction();

        assertEquals(1, programMemory.getPC());
    }

    @Test
    public void SBIS_JumpTwoWordInstructionSTS() throws IllegalAccessException {

        instruction = 0x0038;
        cpuField.set(null, instruction);

        //Fill Memory
        programMemory.setPC(0);
        programMemory.writeWord(0, 0x9200);
        dataMemory.writeBit(0x27, 0, true);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_SBIS.executeInstruction();

        assertEquals(2, programMemory.getPC());
    }

    @Test
    public void SBIS_JumpTwoWordInstructionLDS() throws IllegalAccessException {

        instruction = 0x0038;
        cpuField.set(null, instruction);

        //Fill Memory
        programMemory.setPC(0);
        programMemory.writeWord(0, 0x90F0);
        dataMemory.writeBit(0x27, 0, true);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_SBIS.executeInstruction();

        assertEquals(2, programMemory.getPC());
    }

    @Test
    public void SBIS_JumpTwoWordInstructionCALL() throws IllegalAccessException {

        instruction = 0x0038;
        cpuField.set(null, instruction);

        //Fill Memory
        programMemory.setPC(0);
        programMemory.writeWord(0, 0x940E);
        dataMemory.writeBit(0x27, 0, true);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_SBIS.executeInstruction();

        assertEquals(2, programMemory.getPC());
    }

    @Test
    public void SBIS_JumpTwoWordInstructionJMP() throws IllegalAccessException {

        instruction = 0x0038;
        cpuField.set(null, instruction);

        //Fill Memory
        programMemory.setPC(0);
        programMemory.writeWord(0, 0x940C);
        dataMemory.writeBit(0x27, 0, true);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_SBIS.executeInstruction();

        assertEquals(2, programMemory.getPC());
    }

    @Test
    public void SBIS_NoJump() throws IllegalAccessException {

        instruction = 0x0038;
        cpuField.set(null, instruction);

        //Fill Memory
        programMemory.setPC(0);
        programMemory.writeWord(0, 0x940C);
        dataMemory.writeBit(0x27, 0, false);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_SBIS.executeInstruction();

        assertEquals(0, programMemory.getPC());
    }

    @Test
    public void SBIW_SubZero24And25_Return0And0() throws Exception {

        instruction = 0x0000;
        cpuField.set(null, instruction);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_SBIW.executeInstruction();

        //Read Result
        assertEquals(0, dataMemory.readByte(24));
        assertEquals(0, dataMemory.readByte(25));

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void SBIW_SubOneTwoComplementOverflow26And27_Return128AndNeg1() throws Exception {

        instruction = 0x0011;
        cpuField.set(null, instruction);

        //Fill memory
        dataMemory.writeByte(26, (byte) 0);
        dataMemory.writeByte(27, (byte) -128);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_SBIW.executeInstruction();

        //Read Result
        assertEquals(-1, dataMemory.readByte(26));
        assertEquals(127, dataMemory.readByte(27));

        //Read Flags
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void SBIW_SubOneUnderflow28And29_ReturnNeg1AndNeg1() throws Exception {

        instruction = 0x0021;
        cpuField.set(null, instruction);

        //Fill memory
        dataMemory.writeByte(28, (byte) 0);
        dataMemory.writeByte(29, (byte) 0);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_SBIW.executeInstruction();

        //Read Result
        assertEquals(-1, dataMemory.readByte(28));
        assertEquals(-1, dataMemory.readByte(29));

        //Read Flags
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void SBIW_SubTen30And31_ReturnNeg128And10() throws Exception {

        instruction = 0x003A;
        cpuField.set(null, instruction);

        //Fill memory
        dataMemory.writeByte(30, (byte) 10);
        dataMemory.writeByte(31, (byte) -1);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_SBIW.executeInstruction();

        //Read Result
        assertEquals(0, dataMemory.readByte(30));
        assertEquals(-1, dataMemory.readByte(31));

        //Read Flags
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void SBRC_JumpOneWordInstruction() throws IllegalAccessException {

        instruction = 0x0010;
        cpuField.set(null, instruction);

        //Fill Memory
        programMemory.setPC(0);
        programMemory.writeWord(0, 0x0000);
        dataMemory.writeBit(1, 0, false);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_SBRC.executeInstruction();

        assertEquals(1, programMemory.getPC());
    }

    @Test
    public void SBRC_JumpTwoWordInstructionSTS() throws IllegalAccessException {

        instruction = 0x0010;
        cpuField.set(null, instruction);

        //Fill Memory
        programMemory.setPC(0);
        programMemory.writeWord(0, 0x9200);
        dataMemory.writeBit(1, 0, false);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_SBRC.executeInstruction();

        assertEquals(2, programMemory.getPC());
    }

    @Test
    public void SBRC_JumpTwoWordInstructionLDS() throws IllegalAccessException {

        instruction = 0x0010;
        cpuField.set(null, instruction);

        //Fill Memory
        programMemory.setPC(0);
        programMemory.writeWord(0, 0x90F0);
        dataMemory.writeBit(1, 0, false);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_SBRC.executeInstruction();

        assertEquals(2, programMemory.getPC());
    }

    @Test
    public void SBRC_JumpTwoWordInstructionCALL() throws IllegalAccessException {

        instruction = 0x0010;
        cpuField.set(null, instruction);

        //Fill Memory
        programMemory.setPC(0);
        programMemory.writeWord(0, 0x940E);
        dataMemory.writeBit(1, 0, false);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_SBRC.executeInstruction();

        assertEquals(2, programMemory.getPC());
    }

    @Test
    public void SBRC_JumpTwoWordInstructionJMP() throws IllegalAccessException {

        instruction = 0x0010;
        cpuField.set(null, instruction);

        //Fill Memory
        programMemory.setPC(0);
        programMemory.writeWord(0, 0x940C);
        dataMemory.writeBit(1, 0, false);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_SBRC.executeInstruction();

        assertEquals(2, programMemory.getPC());
    }

    @Test
    public void SBRC_NoJump() throws IllegalAccessException {

        instruction = 0x0010;
        cpuField.set(null, instruction);

        //Fill Memory
        programMemory.setPC(0);
        programMemory.writeWord(0, 0x940C);
        dataMemory.writeBit(1, 0, true);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_SBRC.executeInstruction();

        assertEquals(0, programMemory.getPC());
    }

    @Test
    public void SBRS_JumpOneWordInstruction() throws IllegalAccessException {

        instruction = 0x0030;
        cpuField.set(null, instruction);

        //Fill Memory
        programMemory.setPC(0);
        programMemory.writeWord(0, 0x0000);
        dataMemory.writeBit(3, 0, true);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_SBRS.executeInstruction();

        assertEquals(1, programMemory.getPC());
    }

    @Test
    public void SBRS_JumpTwoWordInstructionSTS() throws IllegalAccessException {

        instruction = 0x0030;
        cpuField.set(null, instruction);

        //Fill Memory
        programMemory.setPC(0);
        programMemory.writeWord(0, 0x9200);
        dataMemory.writeBit(3, 0, true);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_SBRS.executeInstruction();

        assertEquals(2, programMemory.getPC());
    }

    @Test
    public void SBRS_JumpTwoWordInstructionLDS() throws IllegalAccessException {

        instruction = 0x0030;
        cpuField.set(null, instruction);

        //Fill Memory
        programMemory.setPC(0);
        programMemory.writeWord(0, 0x90F0);
        dataMemory.writeBit(3, 0, true);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_SBRS.executeInstruction();

        assertEquals(2, programMemory.getPC());
    }

    @Test
    public void SBRS_JumpTwoWordInstructionCALL() throws IllegalAccessException {

        instruction = 0x0030;
        cpuField.set(null, instruction);

        //Fill Memory
        programMemory.setPC(0);
        programMemory.writeWord(0, 0x940E);
        dataMemory.writeBit(3, 0, true);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_SBRS.executeInstruction();

        assertEquals(2, programMemory.getPC());
    }

    @Test
    public void SBRS_JumpTwoWordInstructionJMP() throws IllegalAccessException {

        instruction = 0x0030;
        cpuField.set(null, instruction);

        //Fill Memory
        programMemory.setPC(0);
        programMemory.writeWord(0, 0x940C);
        dataMemory.writeBit(3, 0, true);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_SBRS.executeInstruction();

        assertEquals(2, programMemory.getPC());
    }

    @Test
    public void SBRS_NoJump() throws IllegalAccessException {

        instruction = 0x0030;
        cpuField.set(null, instruction);

        //Fill Memory
        programMemory.setPC(0);
        programMemory.writeWord(0, 0x940C);
        dataMemory.writeBit(3, 0, false);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_SBRS.executeInstruction();

        assertEquals(0, programMemory.getPC());
    }

    @Test
    public void SPM_Move0x1234ToProgramMemoryAddress0x100() throws Exception {

        instruction = 0x0000;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(1, (byte) 0x12);
        dataMemory.writeByte(0, (byte) 0x34);
        dataMemory.writeByte(31, (byte) 0x01);
        dataMemory.writeByte(30, (byte) 0x00);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_SPM.executeInstruction();

        //Read Result
        //Read using byte address
        assertEquals(0x34, programMemory.readByte(0x200));
        assertEquals(0x12, programMemory.readByte(0x201));
    }

    @Test
    public void SPM_Move0x5678ToProgramMemoryAddress0x200() throws Exception {

        instruction = 0x0000;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(1, (byte) 0x56);
        dataMemory.writeByte(0, (byte) 0x78);
        dataMemory.writeByte(31, (byte) 0x02);
        dataMemory.writeByte(30, (byte) 0x00);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_SPM.executeInstruction();

        //Read Result
        //Read using byte address
        assertEquals(0x78, programMemory.readByte(0x400));
        assertEquals(0x56, programMemory.readByte(0x401));
    }

    @Test
    public void ST_X_POST_INCREMENT_Move0x7EFromRegister14To0x200() throws Exception {

        instruction = 0x00E0;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(0x0E, (byte) 0x7E);
        dataMemory.writeByte(26, (byte) 0x00);
        dataMemory.writeByte(27, (byte) 0x02);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_ST_X_POST_INCREMENT.executeInstruction();

        //Read Result
        assertEquals(0x7E, dataMemory.readByte(0x200));
        assertEquals(0x01, dataMemory.readByte(26));
        assertEquals(0x02, dataMemory.readByte(27));
    }

    @Test
    public void ST_X_POST_INCREMENT_Move0x7DFromRegister15To0x2FF_OverflowLow() throws Exception {

        instruction = 0x00F0;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(15, (byte) 0x7D);
        dataMemory.writeByte(26, (byte) 0xFF);
        dataMemory.writeByte(27, (byte) 0x02);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_ST_X_POST_INCREMENT.executeInstruction();

        //Read Result
        assertEquals(0x7D, dataMemory.readByte(0x2FF));
        assertEquals(0x00, dataMemory.readByte(26));
        assertEquals(0x03, dataMemory.readByte(27));
    }

    @Test
    public void ST_X_POST_INCREMENT_Move0x7CFromRegister16ToLastAddress() throws Exception {

        instruction = 0x0100;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(16, (byte) 0x7C);
        dataMemory.writeByte(26, (byte) 0xFF);
        dataMemory.writeByte(27, (byte) 0x08);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_ST_X_POST_INCREMENT.executeInstruction();

        //Read Result
        assertEquals(0x7C, dataMemory.readByte(0x8FF));
        assertEquals(0x00, dataMemory.readByte(26));
        assertEquals(0x09, dataMemory.readByte(27));
    }

    @Test
    public void ST_X_PRE_DECREMENT_Move0x7BFromRegister17To0x200() throws Exception {

        instruction = 0x0110;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(17, (byte) 0x7B);
        dataMemory.writeByte(26, (byte) 0x01);
        dataMemory.writeByte(27, (byte) 0x02);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_ST_X_PRE_DECREMENT.executeInstruction();

        //Read Result
        assertEquals(0x7B, dataMemory.readByte(0x200));
        assertEquals(0x00, dataMemory.readByte(26));
        assertEquals(0x02, dataMemory.readByte(27));
    }

    @Test
    public void ST_X_PRE_DECREMENT_Move0x7AFromRegister18To0x1FF_UnderflowLow() throws Exception {

        instruction = 0x0120;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(18, (byte) 0x7A);
        dataMemory.writeByte(26, (byte) 0x00);
        dataMemory.writeByte(27, (byte) 0x02);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_ST_X_PRE_DECREMENT.executeInstruction();

        //Read Result
        assertEquals(0x7A, dataMemory.readByte(0x1FF));
        assertEquals((byte) 0xFF, dataMemory.readByte(26));
        assertEquals(0x01, dataMemory.readByte(27));
    }

    @Test
    public void ST_X_PRE_DECREMENT_Move0x70FromRegister19ToFirstAddress() throws Exception {

        instruction = 0x0130;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(19, (byte) 0x70);
        dataMemory.writeByte(26, (byte) 0x01);
        dataMemory.writeByte(27, (byte) 0x00);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_ST_X_PRE_DECREMENT.executeInstruction();

        //Read Result
        assertEquals(0x70, dataMemory.readByte(0x0000));
        assertEquals(0x00, dataMemory.readByte(26));
        assertEquals(0x00, dataMemory.readByte(27));
    }

    @Test
    public void ST_X_UNCHANGED_Move0x6FFromRegister20To0x200() throws Exception {

        instruction = 0x0140;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(20, (byte) 0x6F);
        dataMemory.writeByte(26, (byte) 0x00);
        dataMemory.writeByte(27, (byte) 0x02);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_ST_X_UNCHANGED.executeInstruction();

        //Read Result
        assertEquals(0x6F, dataMemory.readByte(0x200));
        assertEquals(0x00, dataMemory.readByte(26));
        assertEquals(0x02, dataMemory.readByte(27));
    }

    @Test
    public void ST_X_UNCHANGED_Move0x80FromRegister21ToFirstAddress() throws Exception {

        instruction = 0x0150;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(21, (byte) 0x80);
        dataMemory.writeByte(26, (byte) 0x00);
        dataMemory.writeByte(27, (byte) 0x00);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_ST_X_UNCHANGED.executeInstruction();

        //Read Result
        assertEquals((byte) 0x80, dataMemory.readByte(0x00));
        assertEquals(0x00, dataMemory.readByte(26));
        assertEquals(0x00, dataMemory.readByte(27));
    }

    @Test
    public void ST_X_UNCHANGED_Move0x81FromRegister22ToLastAddress() throws Exception {

        instruction = 0x0160;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(22, (byte) 0x81);
        dataMemory.writeByte(26, (byte) 0xFF);
        dataMemory.writeByte(27, (byte) 0x08);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_ST_X_UNCHANGED.executeInstruction();

        //Read Result
        assertEquals((byte) 0x81, dataMemory.readByte(0x8FF));
        assertEquals((byte) 0xFF, dataMemory.readByte(26));
        assertEquals(0x08, dataMemory.readByte(27));
    }

    @Test
    public void ST_Y_POST_INCREMENT_Move0x7EFromRegister14To0x200() throws Exception {

        instruction = 0x00E0;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(0x0E, (byte) 0x7E);
        dataMemory.writeByte(28, (byte) 0x00);
        dataMemory.writeByte(29, (byte) 0x02);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_ST_Y_POST_INCREMENT.executeInstruction();

        //Read Result
        assertEquals(0x7E, dataMemory.readByte(0x200));
        assertEquals(0x01, dataMemory.readByte(28));
        assertEquals(0x02, dataMemory.readByte(29));
    }

    @Test
    public void ST_Y_POST_INCREMENT_Move0x7DFromRegister15To0x2FF_OverflowLow() throws Exception {

        instruction = 0x00F0;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(15, (byte) 0x7D);
        dataMemory.writeByte(28, (byte) 0xFF);
        dataMemory.writeByte(29, (byte) 0x02);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_ST_Y_POST_INCREMENT.executeInstruction();

        //Read Result
        assertEquals(0x7D, dataMemory.readByte(0x2FF));
        assertEquals(0x00, dataMemory.readByte(28));
        assertEquals(0x03, dataMemory.readByte(29));
    }

    @Test
    public void ST_Y_POST_INCREMENT_Move0x7CFromRegister16ToLastAddress() throws Exception {

        instruction = 0x0100;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(16, (byte) 0x7C);
        dataMemory.writeByte(28, (byte) 0xFF);
        dataMemory.writeByte(29, (byte) 0x08);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_ST_Y_POST_INCREMENT.executeInstruction();

        //Read Result
        assertEquals(0x7C, dataMemory.readByte(0x8FF));
        assertEquals(0x00, dataMemory.readByte(28));
        assertEquals(0x09, dataMemory.readByte(29));
    }

    @Test
    public void ST_Y_PRE_DECREMENT_Move0x7BFromRegister17To0x200() throws Exception {

        instruction = 0x0110;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(17, (byte) 0x7B);
        dataMemory.writeByte(28, (byte) 0x01);
        dataMemory.writeByte(29, (byte) 0x02);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_ST_Y_PRE_DECREMENT.executeInstruction();

        //Read Result
        assertEquals(0x7B, dataMemory.readByte(0x200));
        assertEquals(0x00, dataMemory.readByte(28));
        assertEquals(0x02, dataMemory.readByte(29));
    }

    @Test
    public void ST_Y_PRE_DECREMENT_Move0x7AFromRegister18To0x1FF_UnderflowLow() throws Exception {

        instruction = 0x0120;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(18, (byte) 0x7A);
        dataMemory.writeByte(28, (byte) 0x00);
        dataMemory.writeByte(29, (byte) 0x02);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_ST_Y_PRE_DECREMENT.executeInstruction();

        //Read Result
        assertEquals(0x7A, dataMemory.readByte(0x1FF));
        assertEquals((byte) 0xFF, dataMemory.readByte(28));
        assertEquals(0x01, dataMemory.readByte(29));
    }

    @Test
    public void ST_Y_PRE_DECREMENT_Move0x70FromRegister19ToFirstAddress() throws Exception {

        instruction = 0x0130;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(19, (byte) 0x70);
        dataMemory.writeByte(28, (byte) 0x01);
        dataMemory.writeByte(29, (byte) 0x00);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_ST_Y_PRE_DECREMENT.executeInstruction();

        //Read Result
        assertEquals(0x70, dataMemory.readByte(0x0000));
        assertEquals(0x00, dataMemory.readByte(28));
        assertEquals(0x00, dataMemory.readByte(29));
    }

    @Test
    public void ST_Y_UNCHANGED_Move0x6FFromRegister20To0x200() throws Exception {

        instruction = 0x0140;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(20, (byte) 0x6F);
        dataMemory.writeByte(28, (byte) 0x00);
        dataMemory.writeByte(29, (byte) 0x02);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_ST_Y_UNCHANGED.executeInstruction();

        //Read Result
        assertEquals(0x6F, dataMemory.readByte(0x200));
        assertEquals(0x00, dataMemory.readByte(28));
        assertEquals(0x02, dataMemory.readByte(29));
    }

    @Test
    public void ST_Y_UNCHANGED_Move0x80FromRegister21ToFirstAddress() throws Exception {

        instruction = 0x0150;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(21, (byte) 0x80);
        dataMemory.writeByte(28, (byte) 0x00);
        dataMemory.writeByte(29, (byte) 0x00);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_ST_Y_UNCHANGED.executeInstruction();

        //Read Result
        assertEquals((byte) 0x80, dataMemory.readByte(0x00));
        assertEquals(0x00, dataMemory.readByte(28));
        assertEquals(0x00, dataMemory.readByte(29));
    }

    @Test
    public void ST_Y_UNCHANGED_Move0x81FromRegister22ToLastAddress() throws Exception {

        instruction = 0x0160;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(22, (byte) 0x81);
        dataMemory.writeByte(28, (byte) 0xFF);
        dataMemory.writeByte(29, (byte) 0x08);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_ST_Y_UNCHANGED.executeInstruction();

        //Read Result
        assertEquals((byte) 0x81, dataMemory.readByte(0x8FF));
        assertEquals((byte) 0xFF, dataMemory.readByte(28));
        assertEquals(0x08, dataMemory.readByte(29));
    }

    @Test
    public void ST_Z_POST_INCREMENT_Move0x7EFromRegister14To0x200() throws Exception {

        instruction = 0x00E0;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(0x0E, (byte) 0x7E);
        dataMemory.writeByte(30, (byte) 0x00);
        dataMemory.writeByte(31, (byte) 0x02);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_ST_Z_POST_INCREMENT.executeInstruction();

        //Read Result
        assertEquals(0x7E, dataMemory.readByte(0x200));
        assertEquals(0x01, dataMemory.readByte(30));
        assertEquals(0x02, dataMemory.readByte(31));
    }

    @Test
    public void ST_Z_POST_INCREMENT_Move0x7DFromRegister15To0x2FF_OverflowLow() throws Exception {

        instruction = 0x00F0;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(15, (byte) 0x7D);
        dataMemory.writeByte(30, (byte) 0xFF);
        dataMemory.writeByte(31, (byte) 0x02);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_ST_Z_POST_INCREMENT.executeInstruction();

        //Read Result
        assertEquals(0x7D, dataMemory.readByte(0x2FF));
        assertEquals(0x00, dataMemory.readByte(30));
        assertEquals(0x03, dataMemory.readByte(31));
    }

    @Test
    public void ST_Z_POST_INCREMENT_Move0x7CFromRegister16ToLastAddress() throws Exception {

        instruction = 0x0100;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(16, (byte) 0x7C);
        dataMemory.writeByte(30, (byte) 0xFF);
        dataMemory.writeByte(31, (byte) 0x08);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_ST_Z_POST_INCREMENT.executeInstruction();

        //Read Result
        assertEquals(0x7C, dataMemory.readByte(0x8FF));
        assertEquals(0x00, dataMemory.readByte(30));
        assertEquals(0x09, dataMemory.readByte(31));
    }

    @Test
    public void ST_Z_PRE_DECREMENT_Move0x7BFromRegister17To0x200() throws Exception {

        instruction = 0x0110;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(17, (byte) 0x7B);
        dataMemory.writeByte(30, (byte) 0x01);
        dataMemory.writeByte(31, (byte) 0x02);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_ST_Z_PRE_DECREMENT.executeInstruction();

        //Read Result
        assertEquals(0x7B, dataMemory.readByte(0x200));
        assertEquals(0x00, dataMemory.readByte(30));
        assertEquals(0x02, dataMemory.readByte(31));
    }

    @Test
    public void ST_Z_PRE_DECREMENT_Move0x7AFromRegister18To0x1FF_UnderflowLow() throws Exception {

        instruction = 0x0120;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(18, (byte) 0x7A);
        dataMemory.writeByte(30, (byte) 0x00);
        dataMemory.writeByte(31, (byte) 0x02);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_ST_Z_PRE_DECREMENT.executeInstruction();

        //Read Result
        assertEquals(0x7A, dataMemory.readByte(0x1FF));
        assertEquals((byte) 0xFF, dataMemory.readByte(30));
        assertEquals(0x01, dataMemory.readByte(31));
    }

    @Test
    public void ST_Z_PRE_DECREMENT_Move0x70FromRegister19ToFirstAddress() throws Exception {

        instruction = 0x0130;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(19, (byte) 0x70);
        dataMemory.writeByte(30, (byte) 0x01);
        dataMemory.writeByte(31, (byte) 0x00);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_ST_Z_PRE_DECREMENT.executeInstruction();

        //Read Result
        assertEquals(0x70, dataMemory.readByte(0x0000));
        assertEquals(0x00, dataMemory.readByte(30));
        assertEquals(0x00, dataMemory.readByte(31));
    }

    @Test
    public void ST_Z_UNCHANGED_Move0x6FFromRegister20To0x200() throws Exception {

        instruction = 0x0140;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(20, (byte) 0x6F);
        dataMemory.writeByte(30, (byte) 0x00);
        dataMemory.writeByte(31, (byte) 0x02);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_ST_Z_UNCHANGED.executeInstruction();

        //Read Result
        assertEquals(0x6F, dataMemory.readByte(0x200));
        assertEquals(0x00, dataMemory.readByte(30));
        assertEquals(0x02, dataMemory.readByte(31));
    }

    @Test
    public void ST_Z_UNCHANGED_Move0x80FromRegister21ToFirstAddress() throws Exception {

        instruction = 0x0150;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(21, (byte) 0x80);
        dataMemory.writeByte(30, (byte) 0x00);
        dataMemory.writeByte(31, (byte) 0x00);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_ST_Z_UNCHANGED.executeInstruction();

        //Read Result
        assertEquals((byte) 0x80, dataMemory.readByte(0x00));
        assertEquals(0x00, dataMemory.readByte(30));
        assertEquals(0x00, dataMemory.readByte(31));
    }

    @Test
    public void ST_Z_UNCHANGED_Move0x81FromRegister22ToLastAddress() throws Exception {

        instruction = 0x0160;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(22, (byte) 0x81);
        dataMemory.writeByte(30, (byte) 0xFF);
        dataMemory.writeByte(31, (byte) 0x08);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_ST_Z_UNCHANGED.executeInstruction();

        //Read Result
        assertEquals((byte) 0x81, dataMemory.readByte(0x8FF));
        assertEquals((byte) 0xFF, dataMemory.readByte(30));
        assertEquals(0x08, dataMemory.readByte(31));
    }

    @Test
    public void STD_Y_Move0x01FromRegister01To0x200_MinDisplacement() throws Exception {

        instruction = 0x0010;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(1, (byte) 0x01);
        dataMemory.writeByte(28, (byte) 0x00);
        dataMemory.writeByte(29, (byte) 0x02);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_STD_Y.executeInstruction();

        //Read Result
        assertEquals((byte) 0x01, dataMemory.readByte(0x200));
        assertEquals((byte) 0x00, dataMemory.readByte(28));
        assertEquals(0x02, dataMemory.readByte(29));
    }

    @Test
    public void STD_Y_Move0x02FromRegister02To0x200_MaxDisplacement() throws Exception {

        instruction = 0x2C27;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(2, (byte) 0x02);
        dataMemory.writeByte(28, (byte) 0xC1);
        dataMemory.writeByte(29, (byte) 0x01);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_STD_Y.executeInstruction();

        //Read Result
        assertEquals((byte) 0x02, dataMemory.readByte(0x200));
        assertEquals((byte) 0xC1, dataMemory.readByte(28));
        assertEquals(0x01, dataMemory.readByte(29));
    }

    @Test
    public void STD_Z_Move0x01FromRegister01To0x200_MinDisplacement() throws Exception {

        instruction = 0x0010;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(1, (byte) 0x01);
        dataMemory.writeByte(30, (byte) 0x00);
        dataMemory.writeByte(31, (byte) 0x02);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_STD_Z.executeInstruction();

        //Read Result
        assertEquals((byte) 0x01, dataMemory.readByte(0x200));
        assertEquals((byte) 0x00, dataMemory.readByte(30));
        assertEquals(0x02, dataMemory.readByte(31));
    }

    @Test
    public void STD_Z_Move0x02FromRegister02To0x200_MaxDisplacement() throws Exception {

        instruction = 0x2C27;
        cpuField.set(null, instruction);

        //Fill Memory
        dataMemory.writeByte(2, (byte) 0x02);
        dataMemory.writeByte(30, (byte) 0xC1);
        dataMemory.writeByte(31, (byte) 0x01);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_STD_Z.executeInstruction();

        //Read Result
        assertEquals((byte) 0x02, dataMemory.readByte(0x200));
        assertEquals((byte) 0xC1, dataMemory.readByte(30));
        assertEquals(0x01, dataMemory.readByte(31));
    }

    @Test
    public void STS_Move0xFFFromRegister1To0x0345() throws Exception {

        instruction = 0x0010;
        cpuField.set(null, instruction);

        //Fill Memory
        programMemory.setPC(0);
        programMemory.writeWord(0,0x0345);
        dataMemory.writeByte(1, (byte) 0xFF);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_STS.executeInstruction();

        //Read Result
        assertEquals((byte) 0xFF, dataMemory.readByte(0x0345));
    }

    @Test
    public void SUB_SubOneAndZero_Return1() throws IllegalAccessException {

        instruction = 0x00FE;
        cpuField.set(null, instruction);

        regD = 1;
        regR = 0;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);
        dataMemory.writeByte(((0x0200 & instruction) >> 5) | (0x000F & instruction), regR);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_SUB.executeInstruction();

        //Read Result
        assertEquals(1, dataMemory.readByte((0x01F0 & instruction) >> 4));

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,5));//H
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void SUB_SubZeroAndOneUnderflow_ReturnNeg1() throws IllegalAccessException {

        instruction = 0x00FE;
        cpuField.set(null, instruction);

        regD = 0;
        regR = 1;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);
        dataMemory.writeByte(((0x0200 & instruction) >> 5) | (0x000F & instruction), regR);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_SUB.executeInstruction();

        //Read Result
        assertEquals(-1, dataMemory.readByte((0x01F0 & instruction) >> 4));

        //Read Flags
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,5));//H
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void SUB_SubOneAndOne_Return0() throws IllegalAccessException {

        instruction = 0x00FE;
        cpuField.set(null, instruction);

        regD = 1;
        regR = 1;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);
        dataMemory.writeByte(((0x0200 & instruction) >> 5) | (0x000F & instruction), regR);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_SUB.executeInstruction();

        //Read Result
        assertEquals(0, dataMemory.readByte((0x01F0 & instruction) >> 4));

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,5));//H
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void SUB_SubTwoComplementOverflow_ReturnNeg128() throws IllegalAccessException {

        instruction = 0x00FE;
        cpuField.set(null, instruction);

        regD = 0;
        regR = -128;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);
        dataMemory.writeByte(((0x0200 & instruction) >> 5) | (0x000F & instruction), regR);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_SUB.executeInstruction();

        //Read Result
        assertEquals(-128, dataMemory.readByte((0x01F0 & instruction) >> 4));

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,5));//H
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void SUBI_SubOneAndZero_Return1() throws IllegalAccessException {

        instruction = 0x00F0;
        cpuField.set(null, instruction);

        regD = 1;

        //Fill Memory
        dataMemory.writeByte((0x10 | (0x00F0 & instruction) >> 4), regD);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_SUBI.executeInstruction();

        //Read Result
        assertEquals(1, dataMemory.readByte((0x10 | (0x00F0 & instruction) >> 4)));

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,5));//H
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void SUBI_SubZeroAndOneUnderflow_ReturnNeg1() throws IllegalAccessException {

        instruction = 0x00F1;
        cpuField.set(null, instruction);

        regD = 0;

        //Fill Memory
        dataMemory.writeByte((0x10 | (0x00F0 & instruction) >> 4), regD);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_SUBI.executeInstruction();

        //Read Result
        assertEquals(-1, dataMemory.readByte((0x10 | (0x00F0 & instruction) >> 4)));

        //Read Flags
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,5));//H
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void SUBI_SubOneAndOne_Return0() throws IllegalAccessException {

        instruction = 0x00F1;
        cpuField.set(null, instruction);

        regD = 1;

        //Fill Memory
        dataMemory.writeByte((0x10 | (0x00F0 & instruction) >> 4), regD);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_SUBI.executeInstruction();

        //Read Result
        assertEquals(0, dataMemory.readByte((0x10 | (0x00F0 & instruction) >> 4)));

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,5));//H
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void SUBI_SubTwoComplementOverflow_ReturnNeg128() throws IllegalAccessException {

        instruction = 0x08F0;
        cpuField.set(null, instruction);

        regD = 0;

        //Fill Memory
        dataMemory.writeByte((0x10 | (0x00F0 & instruction) >> 4), regD);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_SUBI.executeInstruction();

        //Read Result
        assertEquals(-128, dataMemory.readByte((0x10 | (0x00F0 & instruction) >> 4)));

        //Read Flags
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,5));//H
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,4));//S
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,3));//V
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,2));//N
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,1));//Z
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR,0));//C

    }

    @Test
    public void SWAP_Swap0x0F_Return0xF0() throws IllegalAccessException {

        instruction = 0x00F0;
        cpuField.set(null, instruction);

        regD = 0x0F;

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_SWAP.executeInstruction();

        //Read Result
        assertEquals((byte) 0xF0, dataMemory.readByte((0x01F0 & instruction) >> 4));

    }

    @Test
    public void SWAP_Swap0xF0_Return0x0F() throws IllegalAccessException {

        instruction = 0x00F0;
        cpuField.set(null, instruction);

        regD = -16;//0xF0

        //Fill Memory
        dataMemory.writeByte((0x01F0 & instruction) >> 4, regD);

        //Run Instruction
        CPUModule.Executor.INSTRUCTION_SWAP.executeInstruction();

        //Read Result
        assertEquals((byte) 0x0F, dataMemory.readByte((0x01F0 & instruction) >> 4));

    }
}