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

package com.kollins.project.sofia.atmega328p;

import android.os.Environment;
import android.os.Handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.io.File;

import static org.junit.Assert.assertEquals;


@RunWith(PowerMockRunner.class)
@PrepareForTest({ProgramMemory_ATmega328P.class, Environment.class})
public class ProgramMemoryTest {

    private ProgramMemory_ATmega328P programMemory;
    private byte[] flashMemoryTest;
    private File fileDir;

    @Mock
    private static Handler uCHandler;

    @BeforeEach
    public void prepareForTest() throws Exception {
        programMemory = PowerMockito.spy(new ProgramMemory_ATmega328P(uCHandler));
        flashMemoryTest = Whitebox.getInternalState(programMemory,"flashMemory");
        fileDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
    }

    @Test
    public void hexStringToByteArray_Input0x07_1Byte() throws Exception {
        String hexString = "07";
        byte[] hexByte;

        hexByte = Whitebox.invokeMethod(programMemory, "hexStringToByteArray", hexString);

        assertEquals((byte) 0x07, hexByte[0]);
    }

    @org.junit.jupiter.api.Test
    public void hexStringToByteArray_Input0xFF_1Byte() throws Exception {
        String hexString = "FF";
        byte[] hexByte;

        hexByte = Whitebox.invokeMethod(programMemory, "hexStringToByteArray", hexString);

        assertEquals((byte) 0xFF, hexByte[0]);
    }

    @org.junit.jupiter.api.Test
    public void hexStringToByteArray_Input0xABCDEF_3Byte() throws Exception {
        String hexString = "ABCDEF";
        byte[] hexByte;

        hexByte = Whitebox.invokeMethod(programMemory, "hexStringToByteArray", hexString);

        assertEquals((byte) 0xAB, hexByte[0]);
        assertEquals((byte) 0xCD, hexByte[1]);
        assertEquals((byte) 0xEF, hexByte[2]);
    }

//    @Test
//    public void loadProgramMemory_FileFound() throws Exception {
//        boolean testFileLoaded;
//        File fileSpy = PowerMockito.spy(new File(fileDir,UCModule.DEFAULT_HEX_LOCATION));
//
//        PowerMockito.mockStatic(Environment.class);
//        when(Environment.getExternalStorageState()).thenReturn(Environment.MEDIA_MOUNTED);
//        when(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)).thenReturn(fileSpy);
//
//        PowerMockito.doReturn(true).when(fileSpy, "exists");
//        PowerMockito.doNothing().when(programMemory, "startCodeObserver",fileSpy);
//        PowerMockito.doNothing().when(programMemory, "loadHexFile", null);
//
//        testFileLoaded = programMemory.loadProgramMemory(UCModule.DEFAULT_HEX_LOCATION);
//        assertTrue(testFileLoaded);
//    }

    @org.junit.jupiter.api.Test
    public void loadInstruction_Begin() {
        flashMemoryTest[0] = (byte) 0xAB;
        flashMemoryTest[1] = (byte) 0xCD;

        Whitebox.setInternalState(programMemory,"pcPointer", (char) 0);

        assertEquals(0xCDAB, programMemory.loadInstruction());

        char pcPointerTest = Whitebox.getInternalState(programMemory, "pcPointer");
        assertEquals(1, pcPointerTest);
    }

    @org.junit.jupiter.api.Test
    public void loadInstruction_Middle() {
        flashMemoryTest[10] = (byte) 0xAB;
        flashMemoryTest[11] = (byte) 0xCD;

        Whitebox.setInternalState(programMemory,"pcPointer", (char) 5);

        assertEquals(0xCDAB, programMemory.loadInstruction());

        char pcPointerTest = Whitebox.getInternalState(programMemory, "pcPointer");
        assertEquals(6, pcPointerTest);
    }

    @Test(expected = NullPointerException.class)
    public void loadInstruction_Last_Error() {
        flashMemoryTest[ProgramMemory_ATmega328P.FLASH_SIZE - 2] = (byte) 0xAB;
        flashMemoryTest[ProgramMemory_ATmega328P.FLASH_SIZE - 1] = (byte) 0xCD;

        Whitebox.setInternalState(programMemory,"pcPointer", (char) (ProgramMemory_ATmega328P.FLASH_SIZE/2));

        programMemory.loadInstruction();
    }

    @Test
    public void setPC_to_0x50() {
        int test_value = 0x50;
        programMemory.setPC(test_value);
        char pcPointer_test = Whitebox.getInternalState(programMemory,"pcPointer");
        assertEquals(test_value, pcPointer_test);
    }

    @Test
    public void getPC_0x60() {
        int test_value = 0x60;
        Whitebox.setInternalState(programMemory,"pcPointer", (char) test_value);
        int pcPointer_test = programMemory.getPC();
        assertEquals(test_value, pcPointer_test);
    }

    @Test
    public void addToPC_add_0xF1() {
        int add_value = 0xF1;
        Whitebox.setInternalState(programMemory,"pcPointer", (char) 0);
        programMemory.addToPC(add_value);
        char pcPointer_test = Whitebox.getInternalState(programMemory,"pcPointer");
        assertEquals(add_value, pcPointer_test);
    }

    @Test
    public void writeWord_write0xABCD_to0x02() {
        int data = 0xABCD;
        int address = 0x02; //Word Address

        programMemory.writeWord(address, data);

        assertEquals((byte) 0xCD, flashMemoryTest[(address*2)]);
        assertEquals((byte) 0xAB, flashMemoryTest[(address*2) + 1]);
    }

    @org.junit.jupiter.api.Test
    public void readByte_readLowByte() {
        int data = 0xABCD;
        int address = 0x01; //Word Address

        programMemory.writeWord(address, data);

        assertEquals((byte) 0xCD, programMemory.readByte(address*2));
    }

    @Test
    public void readByte_readHighByte() {
        int data = 0xABCD;
        int address = 0x01; //Word Address

        programMemory.writeWord(address, data);

        assertEquals((byte) 0xAB, programMemory.readByte(address*2 + 1));
    }
}