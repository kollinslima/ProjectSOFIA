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

import android.os.Environment;
import android.os.Handler;

import com.example.kollins.sofia.atmega328p.DataMemory_ATmega328P;
import com.example.kollins.sofia.atmega328p.ProgramMemory_ATmega328P;
import com.example.kollins.sofia.atmega328p.Timer1_ATmega328P;
import com.example.kollins.sofia.ucinterfaces.DataMemory;
import com.example.kollins.sofia.ucinterfaces.IOModule;
import com.example.kollins.sofia.ucinterfaces.ProgramMemory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.booleanThat;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.when;


@RunWith(PowerMockRunner.class)
@PrepareForTest({ProgramMemory_ATmega328P.class, Environment.class})
public class ProgramMemoryTest {

    private ProgramMemory_ATmega328P programMemory;
    private byte[] flashMemoryTest;
    private File fileDir;

    @Mock
    private static Handler uCHandler;

    @Before
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

    @Test
    public void hexStringToByteArray_Input0xFF_1Byte() throws Exception {
        String hexString = "FF";
        byte[] hexByte;

        hexByte = Whitebox.invokeMethod(programMemory, "hexStringToByteArray", hexString);

        assertEquals((byte) 0xFF, hexByte[0]);
    }

    @Test
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

    @Test
    public void loadInstruction_Begin() {
        flashMemoryTest[0] = (byte) 0xAB;
        flashMemoryTest[1] = (byte) 0xCD;

        Whitebox.setInternalState(programMemory,"pcPointer", (char) 0);

        assertEquals(0xCDAB, programMemory.loadInstruction());

        char pcPointerTest = Whitebox.getInternalState(programMemory, "pcPointer");
        assertEquals(1, pcPointerTest);
    }

    @Test
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
}