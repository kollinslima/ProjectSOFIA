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

package com.example.kollins.sofia.atmega328p;

import com.example.kollins.sofia.UCModule;
import com.example.kollins.sofia.atmega328p.iomodule_atmega328p.IOModule_ATmega328P;
import com.example.kollins.sofia.ucinterfaces.IOModule;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.mock;


@RunWith(PowerMockRunner.class)
@PrepareForTest({Timer2_ATmega328P.class, DataMemory_ATmega328P.class, UCModule.class, InterruptionModule_ATmega328P.class})
public class Timer2Test {

    private Field clockPrescalerField;
    private Field outputControlOC2AField, outputControlOC2BField;
    private Field stateOC2AField, stateOC2BField;
    private Field upCountField;
    private Field doubleBufferOCR2AField, doubleBufferOCR2BField;
    private Field nextOverflowField, nextClearField;

    private Timer2_ATmega328P timer2;
    private DataMemory_ATmega328P dataMemory;

    private static IOModule ioModule;

    @Before
    public void prepareForTest() throws Exception {
        ioModule = mock(IOModule_ATmega328P.class);

        dataMemory = new DataMemory_ATmega328P(ioModule);
        timer2 = new Timer2_ATmega328P(dataMemory, ioModule);
        UCModule.interruptionModule = new InterruptionModule_ATmega328P();
        UCModule.interruptionModule.setMemory(dataMemory);

        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2B_ADDR, (byte) 0x01);
        dataMemory.writeIOBit(DataMemory_ATmega328P.GTCCR_ADDR, 1, false);

        clockPrescalerField = Timer2_ATmega328P.class.getDeclaredField("clockCount");
        clockPrescalerField.setAccessible(true);
        
        outputControlOC2AField = Timer2_ATmega328P.class.getDeclaredField("timerOutputControl_OC2A");
        outputControlOC2AField.setAccessible(true);

        outputControlOC2BField = Timer2_ATmega328P.class.getDeclaredField("timerOutputControl_OC2B");
        outputControlOC2BField.setAccessible(true);

        stateOC2AField = Timer2_ATmega328P.class.getDeclaredField("stateOC2A");
        stateOC2AField.setAccessible(true);

        stateOC2BField = Timer2_ATmega328P.class.getDeclaredField("stateOC2B");
        stateOC2BField.setAccessible(true);

        upCountField = Timer2_ATmega328P.class.getDeclaredField("phaseCorrect_UPCount");
        upCountField.setAccessible(true);

        doubleBufferOCR2AField = Timer2_ATmega328P.class.getDeclaredField("doubleBufferOCR2A");
        doubleBufferOCR2AField.setAccessible(true);

        doubleBufferOCR2BField = Timer2_ATmega328P.class.getDeclaredField("doubleBufferOCR2B");
        doubleBufferOCR2BField.setAccessible(true);

        nextClearField = Timer2_ATmega328P.class.getDeclaredField("nextClear");
        nextClearField.setAccessible(true);

        nextOverflowField = Timer2_ATmega328P.class.getDeclaredField("nextOverflow");
        nextOverflowField.setAccessible(true);

        PowerMockito.doNothing().when(ioModule, "setOC2A", Matchers.anyInt(), Matchers.anyLong());
        PowerMockito.doNothing().when(ioModule, "setOC2B", Matchers.anyInt(), Matchers.anyLong());
    }

    @Test
    public void noClock(){
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2B_ADDR, (byte) 0x00);
        assertFalse(Timer2_ATmega328P.ClockSource.values()[0x07 & dataMemory.readByte(DataMemory_ATmega328P.TCCR2B_ADDR)].work());
    }

    @Test
    public void prescaler1(){
        assertTrue(Timer2_ATmega328P.ClockSource.values()[0x07 & dataMemory.readByte(DataMemory_ATmega328P.TCCR2B_ADDR)].work());
    }

    @Test
    public void prescaler8_notFull() throws IllegalAccessException {
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2B_ADDR, (byte) 0x02);
        clockPrescalerField.set(null, (short) 0);
        assertFalse(Timer2_ATmega328P.ClockSource.values()[0x07 & dataMemory.readByte(DataMemory_ATmega328P.TCCR2B_ADDR)].work());
    }

    @Test
    public void prescaler8_full() throws IllegalAccessException {
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2B_ADDR, (byte) 0x02);
        clockPrescalerField.set(null, (short) 8);
        assertTrue(Timer2_ATmega328P.ClockSource.values()[0x07 & dataMemory.readByte(DataMemory_ATmega328P.TCCR2B_ADDR)].work());
        assertEquals((short) 0, clockPrescalerField.get(null));
    }

    @Test
    public void prescaler32_notFull() throws IllegalAccessException {
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2B_ADDR, (byte) 0x03);
        clockPrescalerField.set(null, (short) 0);
        assertFalse(Timer2_ATmega328P.ClockSource.values()[0x07 & dataMemory.readByte(DataMemory_ATmega328P.TCCR2B_ADDR)].work());
    }

    @Test
    public void prescaler32_full() throws IllegalAccessException {
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2B_ADDR, (byte) 0x03);
        clockPrescalerField.set(null, (short) 32);
        assertTrue(Timer2_ATmega328P.ClockSource.values()[0x07 & dataMemory.readByte(DataMemory_ATmega328P.TCCR2B_ADDR)].work());
        assertEquals((short) 0, clockPrescalerField.get(null));
    }

    @Test
    public void prescaler64_notFull() throws IllegalAccessException {
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2B_ADDR, (byte) 0x04);
        clockPrescalerField.set(null, (short) 0);
        assertFalse(Timer2_ATmega328P.ClockSource.values()[0x07 & dataMemory.readByte(DataMemory_ATmega328P.TCCR2B_ADDR)].work());
    }

    @Test
    public void prescaler64_full() throws IllegalAccessException {
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2B_ADDR, (byte) 0x04);
        clockPrescalerField.set(null, (short) 256);
        assertTrue(Timer2_ATmega328P.ClockSource.values()[0x07 & dataMemory.readByte(DataMemory_ATmega328P.TCCR2B_ADDR)].work());
        assertEquals((short) 0, clockPrescalerField.get(null));
    }

    @Test
    public void prescaler128_notFull() throws IllegalAccessException {
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2B_ADDR, (byte) 0x05);
        clockPrescalerField.set(null, (short) 0);
        assertFalse(Timer2_ATmega328P.ClockSource.values()[0x07 & dataMemory.readByte(DataMemory_ATmega328P.TCCR2B_ADDR)].work());
    }

    @Test
    public void prescaler128_full() throws IllegalAccessException {
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2B_ADDR, (byte) 0x05);
        clockPrescalerField.set(null, (short) 128);
        assertTrue(Timer2_ATmega328P.ClockSource.values()[0x07 & dataMemory.readByte(DataMemory_ATmega328P.TCCR2B_ADDR)].work());
        assertEquals((short) 0, clockPrescalerField.get(null));
    }

    @Test
    public void prescaler256_notFull() throws IllegalAccessException {
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2B_ADDR, (byte) 0x06);
        clockPrescalerField.set(null, (short) 0);
        assertFalse(Timer2_ATmega328P.ClockSource.values()[0x07 & dataMemory.readByte(DataMemory_ATmega328P.TCCR2B_ADDR)].work());
    }

    @Test
    public void prescaler256_full() throws IllegalAccessException {
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2B_ADDR, (byte) 0x06);
        clockPrescalerField.set(null, (short) 256);
        assertTrue(Timer2_ATmega328P.ClockSource.values()[0x07 & dataMemory.readByte(DataMemory_ATmega328P.TCCR2B_ADDR)].work());
        assertEquals((short) 0, clockPrescalerField.get(null));
    }

    @Test
    public void prescaler1024_notFull() throws IllegalAccessException {
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2B_ADDR, (byte) 0x07);
        clockPrescalerField.set(null, (short) 0);
        assertFalse(Timer2_ATmega328P.ClockSource.values()[0x07 & dataMemory.readByte(DataMemory_ATmega328P.TCCR2B_ADDR)].work());
    }

    @Test
    public void prescaler1024_full() throws IllegalAccessException {
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2B_ADDR, (byte) 0x07);
        clockPrescalerField.set(null, (short) 1024);
        assertTrue(Timer2_ATmega328P.ClockSource.values()[0x07 & dataMemory.readByte(DataMemory_ATmega328P.TCCR2B_ADDR)].work());
        assertEquals((short) 0, clockPrescalerField.get(null));
    }

    @Test
    public void synchronizationMode(){
        dataMemory.writeIOBit(DataMemory_ATmega328P.GTCCR_ADDR, 1, true);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR2B_ADDR, 3, true);
        Whitebox.setInternalState(timer2,"buffer_WGM22", false);

        assertFalse((Boolean) Whitebox.getInternalState(timer2,"buffer_WGM22"));
    }

    @Test
    public void normalMode_count(){
        byte initialProgress = 0x00;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR2B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2A_ADDR, (byte) 0x00);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT2_ADDR, initialProgress);

        timer2.run();

        assertEquals(initialProgress+1, dataMemory.readByte(DataMemory_ATmega328P.TCNT2_ADDR));
    }

    @Test
    public void normalMode_countOverflow(){
        byte initialProgress = (byte) 0xFF;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR2B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2A_ADDR, (byte) 0x00);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT2_ADDR, initialProgress);

        timer2.run();

        assertEquals((byte) 0x00, dataMemory.readByte(DataMemory_ATmega328P.TCNT2_ADDR));
    }

    @Test
    public void normalMode_outputDisconected() throws IllegalAccessException {
        byte initialProgress = (byte) 0x00;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR2B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2A_ADDR, (byte) 0x00);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT2_ADDR, initialProgress);

        outputControlOC2AField.set(null, true);
        outputControlOC2BField.set(null, true);

        timer2.run();

        assertFalse((Boolean) outputControlOC2AField.get(null));
        assertFalse((Boolean) outputControlOC2BField.get(null));
    }

    @Test
    public void normalMode_toggleOnCompareMath() throws Exception {
        byte initialProgress = (byte) 0x09;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR2B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2A_ADDR, (byte) 0x50);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT2_ADDR, initialProgress);

        dataMemory.writeByte(DataMemory_ATmega328P.OCR2A_ADDR, (byte) (initialProgress+1));
        dataMemory.writeByte(DataMemory_ATmega328P.OCR2B_ADDR, (byte) (initialProgress+1));

        stateOC2AField.set(null, 0);
        stateOC2BField.set(null, 1);

        timer2.run();

        assertTrue((Boolean) outputControlOC2AField.get(null));
        assertTrue((Boolean) outputControlOC2BField.get(null));
        assertEquals(1, stateOC2AField.get(null));
        assertEquals(0, stateOC2BField.get(null));
    }

    @Test
    public void normalMode_toggleOnCompareMath_forceMatch() throws Exception {
        byte initialProgress = (byte) 0x00;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR2B_ADDR, 3, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR2B_ADDR, 7, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR2B_ADDR, 6, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2A_ADDR, (byte) 0x50);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT2_ADDR, initialProgress);

        dataMemory.writeByte(DataMemory_ATmega328P.OCR2A_ADDR, (byte) 0x09);
        dataMemory.writeByte(DataMemory_ATmega328P.OCR2B_ADDR, (byte) 0x09);

        stateOC2AField.set(null, 0);
        stateOC2BField.set(null, 1);

        timer2.run();

        assertTrue((Boolean) outputControlOC2AField.get(null));
        assertTrue((Boolean) outputControlOC2BField.get(null));
        assertEquals(1, stateOC2AField.get(null));
        assertEquals(0, stateOC2BField.get(null));
    }

    @Test
    public void normalMode_clearOnCompareMath() throws Exception {
        byte initialProgress = (byte) 0x09;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR2B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2A_ADDR, (byte) 0xA0);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT2_ADDR, initialProgress);

        dataMemory.writeByte(DataMemory_ATmega328P.OCR2A_ADDR, (byte) (initialProgress+1));
        dataMemory.writeByte(DataMemory_ATmega328P.OCR2B_ADDR, (byte) (initialProgress+1));

        stateOC2AField.set(null, 1);
        stateOC2BField.set(null, 1);

        timer2.run();

        assertTrue((Boolean) outputControlOC2AField.get(null));
        assertTrue((Boolean) outputControlOC2BField.get(null));
        assertEquals(0, stateOC2AField.get(null));
        assertEquals(0, stateOC2BField.get(null));
    }

    @Test
    public void normalMode_setOnCompareMath() throws Exception {
        byte initialProgress = (byte) 0x09;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR2B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2A_ADDR, (byte) 0xF0);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT2_ADDR, initialProgress);

        dataMemory.writeByte(DataMemory_ATmega328P.OCR2A_ADDR, (byte) (initialProgress+1));
        dataMemory.writeByte(DataMemory_ATmega328P.OCR2B_ADDR, (byte) (initialProgress+1));

        stateOC2AField.set(null, 0);
        stateOC2BField.set(null, 0);

        timer2.run();

        assertTrue((Boolean) outputControlOC2AField.get(null));
        assertTrue((Boolean) outputControlOC2BField.get(null));
        assertEquals(1, stateOC2AField.get(null));
        assertEquals(1, stateOC2BField.get(null));
    }

    @Test
    public void phaseCorrect_topOCRA_upCount() throws Exception {
        byte initialProgress = (byte) 0x00;
        byte top = 0x0A;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR2B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2A_ADDR, (byte) 0x01);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT2_ADDR, initialProgress);

        upCountField.set(null, true);
        doubleBufferOCR2AField.set(null, top);

        timer2.run();

        assertEquals(initialProgress+1, dataMemory.readByte(DataMemory_ATmega328P.TCNT2_ADDR));
    }

    @Test
    public void phaseCorrect_topOCRA_downCount() throws Exception {
        byte initialProgress = (byte) 0x01;
        byte top = 0x0A;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR2B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2A_ADDR, (byte) 0x01);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT2_ADDR, initialProgress);

        upCountField.set(null, false);
        doubleBufferOCR2AField.set(null, top);

        timer2.run();

        assertEquals(initialProgress-1, dataMemory.readByte(DataMemory_ATmega328P.TCNT2_ADDR));
    }

    @Test
    public void phaseCorrect_topOCRA_changeToDownCount() throws Exception {
        byte initialProgress = (byte) 0x09;
        byte top = 0x0A;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR2B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2A_ADDR, (byte) 0x01);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT2_ADDR, initialProgress);

        upCountField.set(null, true);
        doubleBufferOCR2AField.set(null, top);

        timer2.run();

        assertFalse((Boolean) upCountField.get(null));
    }

    @Test
    public void phaseCorrect_topOCRA_changeToUpCount() throws Exception {
        byte initialProgress = (byte) 0x01;
        byte top = 0x0A;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR2B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2A_ADDR, (byte) 0x01);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT2_ADDR, initialProgress);

        upCountField.set(null, false);
        doubleBufferOCR2AField.set(null, top);

        timer2.run();

        assertTrue((Boolean) upCountField.get(null));
    }

    @Test
    public void phaseCorrect_topOCRA_updateTop() throws Exception {
        byte initialProgress = (byte) 0x0A;
        byte top = 0x0A;
        byte newTop = 0x0B;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR2B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2A_ADDR, (byte) 0x01);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT2_ADDR, initialProgress);
        dataMemory.writeByte(DataMemory_ATmega328P.OCR2A_ADDR, newTop);
        dataMemory.writeByte(DataMemory_ATmega328P.OCR2B_ADDR, newTop);

        doubleBufferOCR2AField.set(null, top);
        doubleBufferOCR2BField.set(null, top);

        timer2.run();

        assertEquals(newTop, doubleBufferOCR2AField.get(null));
        assertEquals(newTop, doubleBufferOCR2BField.get(null));
    }

    @Test
    public void phaseCorrect_topOCRA_normalOperation() throws Exception {
        byte initialProgress = (byte) 0x00;
        byte top = 0x0A;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR2B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2A_ADDR, (byte) 0x01);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT2_ADDR, initialProgress);

        doubleBufferOCR2AField.set(null, top);
        outputControlOC2AField.set(null, true);
        outputControlOC2BField.set(null, true);

        timer2.run();

        assertFalse((Boolean) outputControlOC2AField.get(null));
        assertFalse((Boolean) outputControlOC2BField.get(null));

    }

    @Test
    public void phaseCorrect_topOCRA_toggleOC0AOnCompareMatch() throws Exception {
        byte initialProgress = (byte) 0x09;
        byte top = 0x0A;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR2B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2A_ADDR, (byte) 0x51);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT2_ADDR, initialProgress);

        doubleBufferOCR2AField.set(null, top);
        doubleBufferOCR2BField.set(null, top);
        outputControlOC2AField.set(null, false);
        outputControlOC2BField.set(null, true);
        stateOC2AField.set(null, 1);

        timer2.run();

        assertTrue((Boolean) outputControlOC2AField.get(null));
        assertFalse((Boolean) outputControlOC2BField.get(null));
        assertEquals(0, stateOC2AField.get(null));
    }

    @Test
    public void phaseCorrect_topOCRA_clearOnCompareMatch() throws Exception {
        byte initialProgress = (byte) 0x09;
        byte top = 0x0A;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR2B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2A_ADDR, (byte) 0xA1);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT2_ADDR, initialProgress);

        upCountField.set(null, true);
        doubleBufferOCR2AField.set(null, top);
        doubleBufferOCR2BField.set(null, top);

        outputControlOC2AField.set(null, false);
        outputControlOC2BField.set(null, false);

        stateOC2AField.set(null, 1);
        stateOC2BField.set(null, 1);

        timer2.run();

        assertTrue((Boolean) outputControlOC2AField.get(null));
        assertTrue((Boolean) outputControlOC2BField.get(null));
        assertEquals(0, stateOC2AField.get(null));
        assertEquals(0, stateOC2BField.get(null));
    }

    @Test
    public void phaseCorrect_topOCRA_setOnCompareMatch() throws Exception {
        byte initialProgress = (byte) 0x09;
        byte top = 0x0A;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR2B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2A_ADDR, (byte) 0xF1);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT2_ADDR, initialProgress);

        upCountField.set(null, true);
        doubleBufferOCR2AField.set(null, top);
        doubleBufferOCR2BField.set(null, top);

        outputControlOC2AField.set(null, false);
        outputControlOC2BField.set(null, false);

        stateOC2AField.set(null, 0);
        stateOC2BField.set(null, 0);

        timer2.run();

        assertTrue((Boolean) outputControlOC2AField.get(null));
        assertTrue((Boolean) outputControlOC2BField.get(null));
        assertEquals(1, stateOC2AField.get(null));
        assertEquals(1, stateOC2BField.get(null));
    }

    @Test
    public void phaseCorrect_topMAX_upCount() throws Exception {
        byte initialProgress = (byte) 0x00;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR2B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2A_ADDR, (byte) 0x01);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT2_ADDR, initialProgress);

        upCountField.set(null, true);

        timer2.run();

        assertEquals(initialProgress+1, dataMemory.readByte(DataMemory_ATmega328P.TCNT2_ADDR));
    }

    @Test
    public void phaseCorrect_topMax_downCount() throws Exception {
        byte initialProgress = (byte) 0x01;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR2B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2A_ADDR, (byte) 0x01);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT2_ADDR, initialProgress);

        upCountField.set(null, false);

        timer2.run();

        assertEquals(initialProgress-1, dataMemory.readByte(DataMemory_ATmega328P.TCNT2_ADDR));
    }

    @Test
    public void phaseCorrect_topMAX_changeToDownCount() throws Exception {
        byte initialProgress = Timer2_ATmega328P.MAX - 1;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR2B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2A_ADDR, (byte) 0x01);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT2_ADDR, initialProgress);

        upCountField.set(null, true);

        timer2.run();

        assertFalse((Boolean) upCountField.get(null));
    }

    @Test
    public void phaseCorrect_topMAX_changeToUpCount() throws Exception {
        byte initialProgress = Timer2_ATmega328P.BOTTOM + 1;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR2B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2A_ADDR, (byte) 0x01);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT2_ADDR, initialProgress);

        upCountField.set(null, false);

        timer2.run();

        assertTrue((Boolean) upCountField.get(null));
    }

    @Test
    public void phaseCorrect_topMAX_update_OCRA_OCRB() throws Exception {
        byte initialProgress = Timer2_ATmega328P.MAX;
        byte newOCRA = 0x0A;
        byte newOCRB = 0x0B;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR2B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2A_ADDR, (byte) 0x01);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT2_ADDR, initialProgress);

        dataMemory.writeByte(DataMemory_ATmega328P.OCR2A_ADDR, newOCRA);
        dataMemory.writeByte(DataMemory_ATmega328P.OCR2B_ADDR, newOCRB);

        timer2.run();

        assertEquals(newOCRA, doubleBufferOCR2AField.get(null));
        assertEquals(newOCRB, doubleBufferOCR2BField.get(null));
    }

    @Test
    public void phaseCorrect_topMAX_normalOperation() throws Exception {
        byte initialProgress = (byte) 0x00;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR2B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2A_ADDR, (byte) 0x01);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT2_ADDR, initialProgress);

        outputControlOC2AField.set(null, true);
        outputControlOC2BField.set(null, true);

        timer2.run();

        assertFalse((Boolean) outputControlOC2AField.get(null));
        assertFalse((Boolean) outputControlOC2BField.get(null));

    }

    @Test
    public void phaseCorrect_topMAX_normalOperation2() throws Exception {
        byte initialProgress = (byte) 0x00;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR2B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2A_ADDR, (byte) 0x51);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT2_ADDR, initialProgress);

        outputControlOC2AField.set(null, true);
        outputControlOC2BField.set(null, true);

        timer2.run();

        assertFalse((Boolean) outputControlOC2AField.get(null));
        assertFalse((Boolean) outputControlOC2BField.get(null));
    }

    @Test
    public void phaseCorrect_topMAX_clearOnCompareMatchUpCount() throws Exception {
        byte valueOCRA = 0x0A;
        byte valueOCRB = valueOCRA;
        byte initialProgress = (byte) (valueOCRA - 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR2B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2A_ADDR, (byte) 0xA1);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT2_ADDR, initialProgress);

        upCountField.set(null, true);
        doubleBufferOCR2AField.set(null, valueOCRA);
        doubleBufferOCR2BField.set(null, valueOCRB);

        outputControlOC2AField.set(null, false);
        outputControlOC2BField.set(null, false);
        stateOC2AField.set(null, 1);
        stateOC2BField.set(null, 1);

        timer2.run();

        assertTrue((Boolean) outputControlOC2AField.get(null));
        assertTrue((Boolean) outputControlOC2BField.get(null));
        assertEquals(0, stateOC2AField.get(null));
        assertEquals(0, stateOC2BField.get(null));
    }

    @Test
    public void phaseCorrect_topMAX_setOnCompareMatchDownCount() throws Exception {
        byte valueOCRA = 0x0A;
        byte valueOCRB = valueOCRA;
        byte initialProgress = (byte) (valueOCRA + 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR2B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2A_ADDR, (byte) 0xA1);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT2_ADDR, initialProgress);

        upCountField.set(null, false);
        doubleBufferOCR2AField.set(null, valueOCRA);
        doubleBufferOCR2BField.set(null, valueOCRB);

        outputControlOC2AField.set(null, false);
        outputControlOC2BField.set(null, false);
        stateOC2AField.set(null, 0);
        stateOC2BField.set(null, 0);

        timer2.run();

        assertTrue((Boolean) outputControlOC2AField.get(null));
        assertTrue((Boolean) outputControlOC2BField.get(null));
        assertEquals(1, stateOC2AField.get(null));
        assertEquals(1, stateOC2BField.get(null));
    }

    @Test
    public void phaseCorrect_topMAX_clearOnCompareMatchDownCount() throws Exception {
        byte valueOCRA = 0x0A;
        byte valueOCRB = valueOCRA;
        byte initialProgress = (byte) (valueOCRA + 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR2B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2A_ADDR, (byte) 0xF1);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT2_ADDR, initialProgress);

        upCountField.set(null, false);
        doubleBufferOCR2AField.set(null, valueOCRA);
        doubleBufferOCR2BField.set(null, valueOCRB);

        outputControlOC2AField.set(null, false);
        outputControlOC2BField.set(null, false);
        stateOC2AField.set(null, 1);
        stateOC2BField.set(null, 1);

        timer2.run();

        assertTrue((Boolean) outputControlOC2AField.get(null));
        assertTrue((Boolean) outputControlOC2BField.get(null));
        assertEquals(0, stateOC2AField.get(null));
        assertEquals(0, stateOC2BField.get(null));
    }

    @Test
    public void phaseCorrect_topMAX_setOnCompareMatchUpCount() throws Exception {
        byte valueOCRA = 0x0A;
        byte valueOCRB = valueOCRA;
        byte initialProgress = (byte) (valueOCRA - 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR2B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2A_ADDR, (byte) 0xF1);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT2_ADDR, initialProgress);

        upCountField.set(null, true);
        doubleBufferOCR2AField.set(null, valueOCRA);
        doubleBufferOCR2BField.set(null, valueOCRB);

        outputControlOC2AField.set(null, false);
        outputControlOC2BField.set(null, false);
        stateOC2AField.set(null, 0);
        stateOC2BField.set(null, 0);

        timer2.run();

        assertTrue((Boolean) outputControlOC2AField.get(null));
        assertTrue((Boolean) outputControlOC2BField.get(null));
        assertEquals(1, stateOC2AField.get(null));
        assertEquals(1, stateOC2BField.get(null));
    }

    @Test
    public void ctcMode_count() throws IllegalAccessException {
        byte initialProgress = 0x00;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR2B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2A_ADDR, (byte) 0x02);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT2_ADDR, initialProgress);

        nextOverflowField.set(null, false);
        nextClearField.set(null, false);

        timer2.run();

        assertEquals(initialProgress+1, dataMemory.readByte(DataMemory_ATmega328P.TCNT2_ADDR));
    }

    @Test
    public void ctcMode_countOverflow() throws IllegalAccessException {
        byte initialProgress = Timer2_ATmega328P.MAX;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR2B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2A_ADDR, (byte) 0x02);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT2_ADDR, initialProgress);

        nextOverflowField.set(null, true);
        nextClearField.set(null, false);

        timer2.run();

        assertEquals(Timer2_ATmega328P.BOTTOM, dataMemory.readByte(DataMemory_ATmega328P.TCNT2_ADDR));
        assertFalse((Boolean) nextOverflowField.get(null));
    }

    @Test
    public void ctcMode_countClear() throws IllegalAccessException {
        byte initialProgress = 0x01;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR2B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2A_ADDR, (byte) 0x02);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT2_ADDR, initialProgress);
        dataMemory.writeByte(DataMemory_ATmega328P.OCR2A_ADDR, (byte) 0x0F);

        nextOverflowField.set(null, false);
        nextClearField.set(null, true);

        timer2.run();

        assertEquals(Timer2_ATmega328P.BOTTOM, dataMemory.readByte(DataMemory_ATmega328P.TCNT2_ADDR));
        assertFalse((Boolean) nextClearField.get(null));
    }

    @Test
    public void ctcMode_outputDisconected() throws IllegalAccessException {
        byte initialProgress = 0x01;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR2B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2A_ADDR, (byte) 0x02);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT2_ADDR, initialProgress);
        dataMemory.writeByte(DataMemory_ATmega328P.OCR2A_ADDR, (byte) 0x0F);

        nextOverflowField.set(null, false);
        nextClearField.set(null, false);
        outputControlOC2AField.set(null, true);
        outputControlOC2BField.set(null, true);

        timer2.run();

        assertFalse((Boolean) outputControlOC2AField.get(null));
        assertFalse((Boolean) outputControlOC2BField.get(null));
    }

    @Test
    public void ctcMode_toggleOnCompareMatch() throws IllegalAccessException {
        byte initialProgress = 0x0E;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR2B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2A_ADDR, (byte) 0x52);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT2_ADDR, initialProgress);

        dataMemory.writeByte(DataMemory_ATmega328P.OCR2A_ADDR, (byte) 0x0F);
        dataMemory.writeByte(DataMemory_ATmega328P.OCR2B_ADDR, (byte) 0x0F);

        nextOverflowField.set(null, false);
        nextClearField.set(null, false);
        outputControlOC2AField.set(null, false);
        outputControlOC2BField.set(null, false);
        stateOC2AField.set(null, 0);
        stateOC2BField.set(null, 1);

        timer2.run();

        assertEquals(1, stateOC2AField.get(null));
        assertEquals(0, stateOC2BField.get(null));
        assertTrue((Boolean) nextClearField.get(null));
    }

    @Test
    public void ctcMode_toggleOnCompareMatch_forceMatch() throws IllegalAccessException {
        byte initialProgress = 0x01;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR2B_ADDR, 3, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR2B_ADDR, 7, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR2B_ADDR, 6, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2A_ADDR, (byte) 0x52);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT2_ADDR, initialProgress);

        dataMemory.writeByte(DataMemory_ATmega328P.OCR2A_ADDR, (byte) 0x0F);
        dataMemory.writeByte(DataMemory_ATmega328P.OCR2B_ADDR, (byte) 0x0F);

        nextOverflowField.set(null, false);
        nextClearField.set(null, false);
        outputControlOC2AField.set(null, false);
        outputControlOC2BField.set(null, false);
        stateOC2AField.set(null, 0);
        stateOC2BField.set(null, 1);

        timer2.run();

        assertEquals(1, stateOC2AField.get(null));
        assertEquals(0, stateOC2BField.get(null));
        assertFalse((Boolean) nextClearField.get(null));
    }

    @Test
    public void ctcMode_clearOnCompareMatch() throws IllegalAccessException {
        byte initialProgress = 0x0E;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR2B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2A_ADDR, (byte) 0xA2);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT2_ADDR, initialProgress);

        dataMemory.writeByte(DataMemory_ATmega328P.OCR2A_ADDR, (byte) 0x0F);
        dataMemory.writeByte(DataMemory_ATmega328P.OCR2B_ADDR, (byte) 0x0F);

        nextOverflowField.set(null, false);
        nextClearField.set(null, false);
        outputControlOC2AField.set(null, false);
        outputControlOC2BField.set(null, false);
        stateOC2AField.set(null, 1);
        stateOC2BField.set(null, 1);

        timer2.run();

        assertEquals(0, stateOC2AField.get(null));
        assertEquals(0, stateOC2BField.get(null));
        assertTrue((Boolean) nextClearField.get(null));
    }

    @Test
    public void ctcMode_setOnCompareMatch_forceMatch() throws IllegalAccessException {
        byte initialProgress = 0x0E;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR2B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2A_ADDR, (byte) 0xF2);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT2_ADDR, initialProgress);

        dataMemory.writeByte(DataMemory_ATmega328P.OCR2A_ADDR, (byte) 0x0F);
        dataMemory.writeByte(DataMemory_ATmega328P.OCR2B_ADDR, (byte) 0x0F);

        nextOverflowField.set(null, false);
        nextClearField.set(null, false);
        outputControlOC2AField.set(null, false);
        outputControlOC2BField.set(null, false);
        stateOC2AField.set(null, 0);
        stateOC2BField.set(null, 0);

        timer2.run();

        assertEquals(1, stateOC2AField.get(null));
        assertEquals(1, stateOC2BField.get(null));
        assertTrue((Boolean) nextClearField.get(null));
    }

    @Test
    public void fastPWM_topOCRA_count() throws Exception {
        byte initialProgress = (byte) 0x00;
        byte top = 0x0A;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR2B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2A_ADDR, (byte) 0x03);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT2_ADDR, initialProgress);

        doubleBufferOCR2AField.set(null, top);

        timer2.run();

        assertEquals(initialProgress+1, dataMemory.readByte(DataMemory_ATmega328P.TCNT2_ADDR));
    }

    @Test
    public void fastPWM_topOCRA_updateTop() throws Exception {
        byte initialProgress = (byte) 0x00;
        byte top = 0x0A;
        byte newTop = 0x0B;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR2B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2A_ADDR, (byte) 0x03);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT2_ADDR, initialProgress);
        dataMemory.writeByte(DataMemory_ATmega328P.OCR2A_ADDR, newTop);

        doubleBufferOCR2AField.set(null, top);

        timer2.run();

        assertEquals(newTop, doubleBufferOCR2AField.get(null));

    }

    @Test
    public void fastPWM_topOCRA_normalOperation() throws Exception {
        byte initialProgress = (byte) 0x00;
        byte top = 0x0A;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR2B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2A_ADDR, (byte) 0x03);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT2_ADDR, initialProgress);

        doubleBufferOCR2AField.set(null, top);
        outputControlOC2AField.set(null, true);
        outputControlOC2BField.set(null, true);

        timer2.run();

        assertFalse((Boolean) outputControlOC2AField.get(null));
        assertFalse((Boolean) outputControlOC2BField.get(null));

    }

    @Test
    public void fastPWM_topOCRA_toggleOC0AOnCompareMatch() throws Exception {
        byte initialProgress = (byte) 0x09;
        byte top = 0x0A;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR2B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2A_ADDR, (byte) 0x53);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT2_ADDR, initialProgress);

        doubleBufferOCR2AField.set(null, top);
        doubleBufferOCR2BField.set(null, top);
        outputControlOC2AField.set(null, false);
        outputControlOC2BField.set(null, true);
        stateOC2AField.set(null, 1);

        timer2.run();

        assertTrue((Boolean) outputControlOC2AField.get(null));
        assertFalse((Boolean) outputControlOC2BField.get(null));
        assertEquals(0, stateOC2AField.get(null));
        assertTrue((Boolean) nextClearField.get(null));
    }

    @Test
    public void fastPWM_topOCRA_clearOnCompareMatch() throws Exception {
        byte initialProgress = (byte) 0x09;
        byte top = 0x0A;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR2B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2A_ADDR, (byte) 0xA3);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT2_ADDR, initialProgress);

        doubleBufferOCR2AField.set(null, top);
        doubleBufferOCR2BField.set(null, top);

        outputControlOC2AField.set(null, false);
        outputControlOC2BField.set(null, false);

        stateOC2AField.set(null, 1);
        stateOC2BField.set(null, 1);

        timer2.run();

        assertTrue((Boolean) outputControlOC2AField.get(null));
        assertTrue((Boolean) outputControlOC2BField.get(null));
        assertEquals(0, stateOC2AField.get(null));
        assertEquals(0, stateOC2BField.get(null));
    }

    @Test
    public void fastPWM_topOCRA_clearOnCompareMatch_setAtBottom() throws Exception {
        byte initialProgress = (byte) 0x0A;
        byte top = 0x0A;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR2B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2A_ADDR, (byte) 0xA3);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT2_ADDR, initialProgress);

        nextClearField.set(null, true);
        doubleBufferOCR2AField.set(null, top);
        doubleBufferOCR2BField.set(null, top);

        outputControlOC2AField.set(null, false);
        outputControlOC2BField.set(null, false);

        stateOC2AField.set(null, 0);
        stateOC2BField.set(null, 0);

        timer2.run();

        assertTrue((Boolean) outputControlOC2AField.get(null));
        assertTrue((Boolean) outputControlOC2BField.get(null));
        assertEquals(1, stateOC2AField.get(null));
        assertEquals(1, stateOC2BField.get(null));
        assertEquals(Timer2_ATmega328P.BOTTOM, dataMemory.readByte(DataMemory_ATmega328P.TCNT2_ADDR));
    }

    @Test
    public void fastPWM_topOCRA_setOnCompareMatch() throws Exception {
        byte initialProgress = (byte) 0x09;
        byte top = 0x0A;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR2B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2A_ADDR, (byte) 0xF3);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT2_ADDR, initialProgress);

        doubleBufferOCR2AField.set(null, top);
        doubleBufferOCR2BField.set(null, top);

        outputControlOC2AField.set(null, false);
        outputControlOC2BField.set(null, false);

        stateOC2AField.set(null, 0);
        stateOC2BField.set(null, 0);

        timer2.run();

        assertTrue((Boolean) outputControlOC2AField.get(null));
        assertTrue((Boolean) outputControlOC2BField.get(null));
        assertEquals(1, stateOC2AField.get(null));
        assertEquals(1, stateOC2BField.get(null));
    }

    @Test
    public void fastPWM_topOCRA_setOnCompareMatch_clearAtBottom() throws Exception {
        byte initialProgress = (byte) 0x0A;
        byte top = 0x0A;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR2B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2A_ADDR, (byte) 0xF3);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT2_ADDR, initialProgress);

        nextClearField.set(null, true);
        doubleBufferOCR2AField.set(null, top);
        doubleBufferOCR2BField.set(null, top);

        outputControlOC2AField.set(null, false);
        outputControlOC2BField.set(null, false);

        stateOC2AField.set(null, 1);
        stateOC2BField.set(null, 1);

        timer2.run();

        assertTrue((Boolean) outputControlOC2AField.get(null));
        assertTrue((Boolean) outputControlOC2BField.get(null));
        assertEquals(0, stateOC2AField.get(null));
        assertEquals(0, stateOC2BField.get(null));
        assertEquals(Timer2_ATmega328P.BOTTOM, dataMemory.readByte(DataMemory_ATmega328P.TCNT2_ADDR));
    }

    @Test
    public void fastPWM_topMAX_count() throws Exception {
        byte initialProgress = (byte) 0x00;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR2B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2A_ADDR, (byte) 0x03);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT2_ADDR, initialProgress);

        timer2.run();

        assertEquals(initialProgress+1, dataMemory.readByte(DataMemory_ATmega328P.TCNT2_ADDR));
    }

    @Test
    public void fastPWM_topMAX_updateTop() throws Exception {
        byte initialProgress = (byte) 0x00;
        byte top = 0x0A;
        byte newTop = 0x0B;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR2B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2A_ADDR, (byte) 0x03);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT2_ADDR, initialProgress);
        dataMemory.writeByte(DataMemory_ATmega328P.OCR2A_ADDR, newTop);
        dataMemory.writeByte(DataMemory_ATmega328P.OCR2B_ADDR, newTop);

        doubleBufferOCR2AField.set(null, top);
        doubleBufferOCR2BField.set(null, top);

        timer2.run();

        assertEquals(newTop, doubleBufferOCR2AField.get(null));
        assertEquals(newTop, doubleBufferOCR2BField.get(null));
    }

    @Test
    public void fastPWM_topMAX_normalOperation() throws Exception {
        byte initialProgress = (byte) 0x00;
        byte top = 0x0A;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR2B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2A_ADDR, (byte) 0x03);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT2_ADDR, initialProgress);

        doubleBufferOCR2AField.set(null, top);
        doubleBufferOCR2BField.set(null, top);
        outputControlOC2AField.set(null, true);
        outputControlOC2BField.set(null, true);

        timer2.run();

        assertFalse((Boolean) outputControlOC2AField.get(null));
        assertFalse((Boolean) outputControlOC2BField.get(null));

    }

    @Test
    public void fastPWM_topMAX_normalOperation2() throws Exception {
        byte initialProgress = (byte) 0x00;
        byte top = 0x0A;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR2B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2A_ADDR, (byte) 0x53);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT2_ADDR, initialProgress);

        doubleBufferOCR2AField.set(null, top);
        doubleBufferOCR2BField.set(null, top);
        outputControlOC2AField.set(null, true);
        outputControlOC2BField.set(null, true);

        timer2.run();

        assertFalse((Boolean) outputControlOC2AField.get(null));
        assertFalse((Boolean) outputControlOC2BField.get(null));

    }

    @Test
    public void fastPWM_topMAX_clearOnCompareMatch() throws Exception {
        byte initialProgress = (byte) 0x09;
        byte top = 0x0A;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR2B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2A_ADDR, (byte) 0xA3);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT2_ADDR, initialProgress);

        doubleBufferOCR2AField.set(null, top);
        doubleBufferOCR2BField.set(null, top);

        outputControlOC2AField.set(null, false);
        outputControlOC2BField.set(null, false);

        stateOC2AField.set(null, 1);
        stateOC2BField.set(null, 1);

        timer2.run();

        assertTrue((Boolean) outputControlOC2AField.get(null));
        assertTrue((Boolean) outputControlOC2BField.get(null));
        assertEquals(0, stateOC2AField.get(null));
        assertEquals(0, stateOC2BField.get(null));
    }

    @Test
    public void fastPWM_topMAX_clearOnCompareMatch_setAtBottom() throws Exception {
        byte initialProgress = Timer2_ATmega328P.MAX;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR2B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2A_ADDR, (byte) 0xA3);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT2_ADDR, initialProgress);

        outputControlOC2AField.set(null, false);
        outputControlOC2BField.set(null, false);

        stateOC2AField.set(null, 0);
        stateOC2BField.set(null, 0);

        timer2.run();

        assertTrue((Boolean) outputControlOC2AField.get(null));
        assertTrue((Boolean) outputControlOC2BField.get(null));
        assertEquals(1, stateOC2AField.get(null));
        assertEquals(1, stateOC2BField.get(null));
        assertEquals(Timer2_ATmega328P.BOTTOM, dataMemory.readByte(DataMemory_ATmega328P.TCNT2_ADDR));
    }

    @Test
    public void fastPWM_topMAX_setOnCompareMatch() throws Exception {
        byte initialProgress = (byte) 0x09;
        byte top = 0x0A;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR2B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2A_ADDR, (byte) 0xF3);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT2_ADDR, initialProgress);

        doubleBufferOCR2AField.set(null, top);
        doubleBufferOCR2BField.set(null, top);

        outputControlOC2AField.set(null, false);
        outputControlOC2BField.set(null, false);

        stateOC2AField.set(null, 0);
        stateOC2BField.set(null, 0);

        timer2.run();

        assertTrue((Boolean) outputControlOC2AField.get(null));
        assertTrue((Boolean) outputControlOC2BField.get(null));
        assertEquals(1, stateOC2AField.get(null));
        assertEquals(1, stateOC2BField.get(null));
    }

    @Test
    public void fastPWM_topMAX_setOnCompareMatch_clearAtBottom() throws Exception {
        byte initialProgress = Timer2_ATmega328P.MAX;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR2B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR2A_ADDR, (byte) 0xF3);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT2_ADDR, initialProgress);

        outputControlOC2AField.set(null, false);
        outputControlOC2BField.set(null, false);

        stateOC2AField.set(null, 1);
        stateOC2BField.set(null, 1);

        timer2.run();

        assertTrue((Boolean) outputControlOC2AField.get(null));
        assertTrue((Boolean) outputControlOC2BField.get(null));
        assertEquals(0, stateOC2AField.get(null));
        assertEquals(0, stateOC2BField.get(null));
        assertEquals(Timer2_ATmega328P.BOTTOM, dataMemory.readByte(DataMemory_ATmega328P.TCNT2_ADDR));
    }
}