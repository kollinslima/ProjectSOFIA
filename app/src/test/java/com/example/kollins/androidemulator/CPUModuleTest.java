package com.example.kollins.androidemulator;

import android.os.Handler;

import com.example.kollins.androidemulator.ATmega328P.DataMemory_ATmega328P;
import com.example.kollins.androidemulator.ATmega328P.ProgramMemory_ATmega328P;
import com.example.kollins.androidemulator.uCInterfaces.DataMemory;
import com.example.kollins.androidemulator.uCInterfaces.IOModule;
import com.example.kollins.androidemulator.uCInterfaces.ProgramMemory;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.locks.Lock;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.spy;

//@RunWith(MockitoJUnitRunner.class)
@RunWith(PowerMockRunner.class)
@PrepareForTest({CPUModule.class, ProgramMemory.class, DataMemory.class})
public class CPUModuleTest {

    private static Field cpuField;

    private static ProgramMemory programMemory;
    private static DataMemory dataMemory;
    private static CPUModule cpuModule;

    private int instruction;
    private byte regD,regR;

    @Mock
    private static IOModule ioModule;

    @BeforeClass
    public static void initModules() throws Exception {
        dataMemory = new DataMemory_ATmega328P(ioModule);

        UCModule uCModule = mock(UCModule.class);
        Handler uCHandler = mock(Handler.class);
        Lock clockLock = PowerMockito.mock(Lock.class);

        programMemory = new ProgramMemory_ATmega328P(uCHandler);
        cpuModule = new CPUModule(programMemory,dataMemory,uCModule,uCHandler,clockLock);

        cpuField = CPUModule.class.getDeclaredField("instruction");
        cpuField.setAccessible(true);
    }

    @Before
    public void mockWaitClock() throws Exception {
        PowerMockito.spy(CPUModule.class);
        PowerMockito.doNothing().when(CPUModule.class, "waitClock");
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
    
}