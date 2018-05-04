package com.example.kollins.androidemulator;

import android.os.Handler;

import com.example.kollins.androidemulator.ATmega328P.DataMemory_ATmega328P;
import com.example.kollins.androidemulator.uCInterfaces.DataMemory;
import com.example.kollins.androidemulator.uCInterfaces.IOModule;
import com.example.kollins.androidemulator.uCInterfaces.ProgramMemory;

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
@PrepareForTest({CPUModule.class})
public class CPUModuleTest {

    private static Field cpuField;

    private static DataMemory dataMemory;
    private static CPUModule cpuModule;

    private int instruction;
    private byte regD,regR;

    @Mock
    private static ProgramMemory programMemory;
    @Mock
    private static IOModule ioModule;

    @BeforeClass
    public static void initModules() throws Exception {
        dataMemory = new DataMemory_ATmega328P(ioModule);

        UCModule uCModule = mock(UCModule.class);
        Handler uCHandler = mock(Handler.class);
        Lock clockLock = PowerMockito.mock(Lock.class);

        cpuModule = new CPUModule(programMemory,dataMemory,uCModule,uCHandler,clockLock);

        cpuField = CPUModule.class.getDeclaredField("instruction");
        cpuField.setAccessible(true);
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
        PowerMockito.spy(CPUModule.class);
        PowerMockito.doNothing().when(CPUModule.class, "waitClock");
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


}