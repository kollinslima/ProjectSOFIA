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
import org.mockito.Mock;
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
@PrepareForTest({Timer0_ATmega328P.class, DataMemory_ATmega328P.class, UCModule.class, InterruptionModule_ATmega328P.class})
public class Timer0Test {

    private Field clockPrescalerField, oldExternalT0Field;
    private Field outputControlOC0AField, outputControlOC0BField;
    private Field stateOC0AField, stateOC0BField;
    private Field upCountField;
    private Field doubleBufferOCR0AField, doubleBufferOCR0BField;
    private Field nextOverflowField, nextClearField;

    private Timer0_ATmega328P timer0;
    private DataMemory_ATmega328P dataMemory;

    private static IOModule ioModule;

    @Before
    public void prepareForTest() throws Exception {
        ioModule = mock(IOModule_ATmega328P.class);

        dataMemory = new DataMemory_ATmega328P(ioModule);
        timer0 = new Timer0_ATmega328P(dataMemory, ioModule);
        UCModule.interruptionModule = new InterruptionModule_ATmega328P();
        UCModule.interruptionModule.setMemory(dataMemory);

        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0B_ADDR, (byte) 0x01);
        dataMemory.writeIOBit(DataMemory_ATmega328P.GTCCR_ADDR, 0, false);

        clockPrescalerField = Timer0_ATmega328P.class.getDeclaredField("clockCount");
        clockPrescalerField.setAccessible(true);

        oldExternalT0Field = Timer0_ATmega328P.class.getDeclaredField("oldExternalT0");
        oldExternalT0Field.setAccessible(true);

        outputControlOC0AField = Timer0_ATmega328P.class.getDeclaredField("timerOutputControl_OC0A");
        outputControlOC0AField.setAccessible(true);

        outputControlOC0BField = Timer0_ATmega328P.class.getDeclaredField("timerOutputControl_OC0B");
        outputControlOC0BField.setAccessible(true);

        stateOC0AField = Timer0_ATmega328P.class.getDeclaredField("stateOC0A");
        stateOC0AField.setAccessible(true);

        stateOC0BField = Timer0_ATmega328P.class.getDeclaredField("stateOC0B");
        stateOC0BField.setAccessible(true);

        upCountField = Timer0_ATmega328P.class.getDeclaredField("phaseCorrect_UPCount");
        upCountField.setAccessible(true);

        doubleBufferOCR0AField = Timer0_ATmega328P.class.getDeclaredField("doubleBufferOCR0A");
        doubleBufferOCR0AField.setAccessible(true);

        doubleBufferOCR0BField = Timer0_ATmega328P.class.getDeclaredField("doubleBufferOCR0B");
        doubleBufferOCR0BField.setAccessible(true);

        nextClearField = Timer0_ATmega328P.class.getDeclaredField("nextClear");
        nextClearField.setAccessible(true);

        nextOverflowField = Timer0_ATmega328P.class.getDeclaredField("nextOverflow");
        nextOverflowField.setAccessible(true);

        PowerMockito.doNothing().when(ioModule, "setOC0A", Matchers.anyInt(), Matchers.anyLong());
        PowerMockito.doNothing().when(ioModule, "setOC0B", Matchers.anyInt(), Matchers.anyLong());
    }

    @Test
    public void noClock(){
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0B_ADDR, (byte) 0x00);
        assertFalse(Timer0_ATmega328P.ClockSource.values()[0x07 & dataMemory.readByte(DataMemory_ATmega328P.TCCR0B_ADDR)].work());
    }

    @Test
    public void prescaler1(){
        assertTrue(Timer0_ATmega328P.ClockSource.values()[0x07 & dataMemory.readByte(DataMemory_ATmega328P.TCCR0B_ADDR)].work());
    }

    @Test
    public void prescaler8_notFull() throws IllegalAccessException {
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0B_ADDR, (byte) 0x02);
        clockPrescalerField.set(null, (short) 0);
        assertFalse(Timer0_ATmega328P.ClockSource.values()[0x07 & dataMemory.readByte(DataMemory_ATmega328P.TCCR0B_ADDR)].work());
    }

    @Test
    public void prescaler8_full() throws IllegalAccessException {
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0B_ADDR, (byte) 0x02);
        clockPrescalerField.set(null, (short) 8);
        assertTrue(Timer0_ATmega328P.ClockSource.values()[0x07 & dataMemory.readByte(DataMemory_ATmega328P.TCCR0B_ADDR)].work());
        assertEquals((short) 0, clockPrescalerField.get(null));
    }

    @Test
    public void prescaler64_notFull() throws IllegalAccessException {
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0B_ADDR, (byte) 0x03);
        clockPrescalerField.set(null, (short) 0);
        assertFalse(Timer0_ATmega328P.ClockSource.values()[0x07 & dataMemory.readByte(DataMemory_ATmega328P.TCCR0B_ADDR)].work());
    }

    @Test
    public void prescaler64_full() throws IllegalAccessException {
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0B_ADDR, (byte) 0x03);
        clockPrescalerField.set(null, (short) 64);
        assertTrue(Timer0_ATmega328P.ClockSource.values()[0x07 & dataMemory.readByte(DataMemory_ATmega328P.TCCR0B_ADDR)].work());
        assertEquals((short) 0, clockPrescalerField.get(null));
    }

    @Test
    public void prescaler256_notFull() throws IllegalAccessException {
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0B_ADDR, (byte) 0x04);
        clockPrescalerField.set(null, (short) 0);
        assertFalse(Timer0_ATmega328P.ClockSource.values()[0x07 & dataMemory.readByte(DataMemory_ATmega328P.TCCR0B_ADDR)].work());
    }

    @Test
    public void prescaler256_full() throws IllegalAccessException {
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0B_ADDR, (byte) 0x04);
        clockPrescalerField.set(null, (short) 256);
        assertTrue(Timer0_ATmega328P.ClockSource.values()[0x07 & dataMemory.readByte(DataMemory_ATmega328P.TCCR0B_ADDR)].work());
        assertEquals((short) 0, clockPrescalerField.get(null));
    }

    @Test
    public void prescaler1024_notFull() throws IllegalAccessException {
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0B_ADDR, (byte) 0x05);
        clockPrescalerField.set(null, (short) 0);
        assertFalse(Timer0_ATmega328P.ClockSource.values()[0x07 & dataMemory.readByte(DataMemory_ATmega328P.TCCR0B_ADDR)].work());
    }

    @Test
    public void prescaler1024_full() throws IllegalAccessException {
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0B_ADDR, (byte) 0x05);
        clockPrescalerField.set(null, (short) 1024);
        assertTrue(Timer0_ATmega328P.ClockSource.values()[0x07 & dataMemory.readByte(DataMemory_ATmega328P.TCCR0B_ADDR)].work());
        assertEquals((short) 0, clockPrescalerField.get(null));
    }

    @Test
    public void externalClock_fallingEdge_notDetected() throws IllegalAccessException {
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0B_ADDR, (byte) 0x06);

        dataMemory.writeIOBit(DataMemory_ATmega328P.PIND_ADDR, 4, true);
        oldExternalT0Field.set(null, false);

        assertFalse(Timer0_ATmega328P.ClockSource.values()[0x07 & dataMemory.readByte(DataMemory_ATmega328P.TCCR0B_ADDR)].work());
        assertTrue((Boolean) oldExternalT0Field.get(null));
    }

    @Test
    public void externalClock_fallingEdge_detected() throws IllegalAccessException {
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0B_ADDR, (byte) 0x06);

        dataMemory.writeIOBit(DataMemory_ATmega328P.PIND_ADDR, 4, false);
        oldExternalT0Field.set(null, true);

        assertTrue(Timer0_ATmega328P.ClockSource.values()[0x07 & dataMemory.readByte(DataMemory_ATmega328P.TCCR0B_ADDR)].work());
        assertFalse((Boolean) oldExternalT0Field.get(null));
    }

    @Test
    public void externalClock_risingEdge_notDetected() throws IllegalAccessException {
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0B_ADDR, (byte) 0x07);

        dataMemory.writeIOBit(DataMemory_ATmega328P.PIND_ADDR, 4, false);
        oldExternalT0Field.set(null, true);

        assertFalse(Timer0_ATmega328P.ClockSource.values()[0x07 & dataMemory.readByte(DataMemory_ATmega328P.TCCR0B_ADDR)].work());
        assertFalse((Boolean) oldExternalT0Field.get(null));
    }

    @Test
    public void externalClock_risingEdge_detected() throws IllegalAccessException {
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0B_ADDR, (byte) 0x07);

        dataMemory.writeIOBit(DataMemory_ATmega328P.PIND_ADDR, 4, true);
        oldExternalT0Field.set(null, false);

        assertTrue(Timer0_ATmega328P.ClockSource.values()[0x07 & dataMemory.readByte(DataMemory_ATmega328P.TCCR0B_ADDR)].work());
        assertTrue((Boolean) oldExternalT0Field.get(null));
    }

    @Test
    public void synchronizationMode(){
        dataMemory.writeIOBit(DataMemory_ATmega328P.GTCCR_ADDR, 0, true);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR0B_ADDR, 3, true);
        Whitebox.setInternalState(timer0,"buffer_WGM02", false);

        assertFalse((Boolean) Whitebox.getInternalState(timer0,"buffer_WGM02"));
    }

    @Test
    public void normalMode_count(){
        byte initialProgress = 0x00;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR0B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0A_ADDR, (byte) 0x00);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT0_ADDR, initialProgress);

        timer0.run();

        assertEquals(initialProgress+1, dataMemory.readByte(DataMemory_ATmega328P.TCNT0_ADDR));
    }

    @Test
    public void normalMode_countOverflow(){
        byte initialProgress = (byte) 0xFF;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR0B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0A_ADDR, (byte) 0x00);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT0_ADDR, initialProgress);

        timer0.run();

        assertEquals((byte) 0x00, dataMemory.readByte(DataMemory_ATmega328P.TCNT0_ADDR));
    }

    @Test
    public void normalMode_outputDisconected() throws IllegalAccessException {
        byte initialProgress = (byte) 0x00;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR0B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0A_ADDR, (byte) 0x00);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT0_ADDR, initialProgress);

        outputControlOC0AField.set(null, true);
        outputControlOC0BField.set(null, true);

        timer0.run();

        assertFalse((Boolean) outputControlOC0AField.get(null));
        assertFalse((Boolean) outputControlOC0BField.get(null));
    }

    @Test
    public void normalMode_toggleOnCompareMath() throws Exception {
        byte initialProgress = (byte) 0x09;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR0B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0A_ADDR, (byte) 0x50);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT0_ADDR, initialProgress);

        dataMemory.writeByte(DataMemory_ATmega328P.OCR0A_ADDR, (byte) (initialProgress+1));
        dataMemory.writeByte(DataMemory_ATmega328P.OCR0B_ADDR, (byte) (initialProgress+1));

        stateOC0AField.set(null, 0);
        stateOC0BField.set(null, 1);

        timer0.run();

        assertTrue((Boolean) outputControlOC0AField.get(null));
        assertTrue((Boolean) outputControlOC0BField.get(null));
        assertEquals(1, stateOC0AField.get(null));
        assertEquals(0, stateOC0BField.get(null));
    }

    @Test
    public void normalMode_toggleOnCompareMath_forceMatch() throws Exception {
        byte initialProgress = (byte) 0x00;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR0B_ADDR, 3, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR0B_ADDR, 7, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR0B_ADDR, 6, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0A_ADDR, (byte) 0x50);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT0_ADDR, initialProgress);

        dataMemory.writeByte(DataMemory_ATmega328P.OCR0A_ADDR, (byte) 0x09);
        dataMemory.writeByte(DataMemory_ATmega328P.OCR0B_ADDR, (byte) 0x09);

        stateOC0AField.set(null, 0);
        stateOC0BField.set(null, 1);

        timer0.run();

        assertTrue((Boolean) outputControlOC0AField.get(null));
        assertTrue((Boolean) outputControlOC0BField.get(null));
        assertEquals(1, stateOC0AField.get(null));
        assertEquals(0, stateOC0BField.get(null));
    }

    @Test
    public void normalMode_clearOnCompareMath() throws Exception {
        byte initialProgress = (byte) 0x09;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR0B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0A_ADDR, (byte) 0xA0);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT0_ADDR, initialProgress);

        dataMemory.writeByte(DataMemory_ATmega328P.OCR0A_ADDR, (byte) (initialProgress+1));
        dataMemory.writeByte(DataMemory_ATmega328P.OCR0B_ADDR, (byte) (initialProgress+1));

        stateOC0AField.set(null, 1);
        stateOC0BField.set(null, 1);

        timer0.run();

        assertTrue((Boolean) outputControlOC0AField.get(null));
        assertTrue((Boolean) outputControlOC0BField.get(null));
        assertEquals(0, stateOC0AField.get(null));
        assertEquals(0, stateOC0BField.get(null));
    }

    @Test
    public void normalMode_setOnCompareMath() throws Exception {
        byte initialProgress = (byte) 0x09;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR0B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0A_ADDR, (byte) 0xF0);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT0_ADDR, initialProgress);

        dataMemory.writeByte(DataMemory_ATmega328P.OCR0A_ADDR, (byte) (initialProgress+1));
        dataMemory.writeByte(DataMemory_ATmega328P.OCR0B_ADDR, (byte) (initialProgress+1));

        stateOC0AField.set(null, 0);
        stateOC0BField.set(null, 0);

        timer0.run();

        assertTrue((Boolean) outputControlOC0AField.get(null));
        assertTrue((Boolean) outputControlOC0BField.get(null));
        assertEquals(1, stateOC0AField.get(null));
        assertEquals(1, stateOC0BField.get(null));
    }

    @Test
    public void phaseCorrect_topOCRA_upCount() throws Exception {
        byte initialProgress = (byte) 0x00;
        byte top = 0x0A;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR0B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0A_ADDR, (byte) 0x01);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT0_ADDR, initialProgress);

        upCountField.set(null, true);
        doubleBufferOCR0AField.set(null, top);

        timer0.run();

        assertEquals(initialProgress+1, dataMemory.readByte(DataMemory_ATmega328P.TCNT0_ADDR));
    }

    @Test
    public void phaseCorrect_topOCRA_downCount() throws Exception {
        byte initialProgress = (byte) 0x01;
        byte top = 0x0A;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR0B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0A_ADDR, (byte) 0x01);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT0_ADDR, initialProgress);

        upCountField.set(null, false);
        doubleBufferOCR0AField.set(null, top);

        timer0.run();

        assertEquals(initialProgress-1, dataMemory.readByte(DataMemory_ATmega328P.TCNT0_ADDR));
    }

    @Test
    public void phaseCorrect_topOCRA_changeToDownCount() throws Exception {
        byte initialProgress = (byte) 0x09;
        byte top = 0x0A;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR0B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0A_ADDR, (byte) 0x01);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT0_ADDR, initialProgress);

        upCountField.set(null, true);
        doubleBufferOCR0AField.set(null, top);

        timer0.run();

        assertFalse((Boolean) upCountField.get(null));
    }

    @Test
    public void phaseCorrect_topOCRA_changeToUpCount() throws Exception {
        byte initialProgress = (byte) 0x01;
        byte top = 0x0A;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR0B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0A_ADDR, (byte) 0x01);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT0_ADDR, initialProgress);

        upCountField.set(null, false);
        doubleBufferOCR0AField.set(null, top);

        timer0.run();

        assertTrue((Boolean) upCountField.get(null));
    }

    @Test
    public void phaseCorrect_topOCRA_updateTop() throws Exception {
        byte initialProgress = (byte) 0x0A;
        byte top = 0x0A;
        byte newTop = 0x0B;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR0B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0A_ADDR, (byte) 0x01);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT0_ADDR, initialProgress);
        dataMemory.writeByte(DataMemory_ATmega328P.OCR0A_ADDR, newTop);
        dataMemory.writeByte(DataMemory_ATmega328P.OCR0B_ADDR, newTop);

        doubleBufferOCR0AField.set(null, top);
        doubleBufferOCR0BField.set(null, top);

        timer0.run();

        assertEquals(newTop, doubleBufferOCR0AField.get(null));
        assertEquals(newTop, doubleBufferOCR0BField.get(null));
    }

    @Test
    public void phaseCorrect_topOCRA_normalOperation() throws Exception {
        byte initialProgress = (byte) 0x00;
        byte top = 0x0A;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR0B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0A_ADDR, (byte) 0x01);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT0_ADDR, initialProgress);

        doubleBufferOCR0AField.set(null, top);
        outputControlOC0AField.set(null, true);
        outputControlOC0BField.set(null, true);

        timer0.run();

        assertFalse((Boolean) outputControlOC0AField.get(null));
        assertFalse((Boolean) outputControlOC0BField.get(null));

    }

    @Test
    public void phaseCorrect_topOCRA_toggleOC0AOnCompareMatch() throws Exception {
        byte initialProgress = (byte) 0x09;
        byte top = 0x0A;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR0B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0A_ADDR, (byte) 0x51);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT0_ADDR, initialProgress);

        doubleBufferOCR0AField.set(null, top);
        doubleBufferOCR0BField.set(null, top);
        outputControlOC0AField.set(null, false);
        outputControlOC0BField.set(null, true);
        stateOC0AField.set(null, 1);

        timer0.run();

        assertTrue((Boolean) outputControlOC0AField.get(null));
        assertFalse((Boolean) outputControlOC0BField.get(null));
        assertEquals(0, stateOC0AField.get(null));
    }

    @Test
    public void phaseCorrect_topOCRA_clearOnCompareMatch() throws Exception {
        byte initialProgress = (byte) 0x09;
        byte top = 0x0A;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR0B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0A_ADDR, (byte) 0xA1);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT0_ADDR, initialProgress);

        upCountField.set(null, true);
        doubleBufferOCR0AField.set(null, top);
        doubleBufferOCR0BField.set(null, top);

        outputControlOC0AField.set(null, false);
        outputControlOC0BField.set(null, false);

        stateOC0AField.set(null, 1);
        stateOC0BField.set(null, 1);

        timer0.run();

        assertTrue((Boolean) outputControlOC0AField.get(null));
        assertTrue((Boolean) outputControlOC0BField.get(null));
        assertEquals(0, stateOC0AField.get(null));
        assertEquals(0, stateOC0BField.get(null));
    }

    @Test
    public void phaseCorrect_topOCRA_setOnCompareMatch() throws Exception {
        byte initialProgress = (byte) 0x09;
        byte top = 0x0A;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR0B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0A_ADDR, (byte) 0xF1);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT0_ADDR, initialProgress);

        upCountField.set(null, true);
        doubleBufferOCR0AField.set(null, top);
        doubleBufferOCR0BField.set(null, top);

        outputControlOC0AField.set(null, false);
        outputControlOC0BField.set(null, false);

        stateOC0AField.set(null, 0);
        stateOC0BField.set(null, 0);

        timer0.run();

        assertTrue((Boolean) outputControlOC0AField.get(null));
        assertTrue((Boolean) outputControlOC0BField.get(null));
        assertEquals(1, stateOC0AField.get(null));
        assertEquals(1, stateOC0BField.get(null));
    }

    @Test
    public void phaseCorrect_topMAX_upCount() throws Exception {
        byte initialProgress = (byte) 0x00;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR0B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0A_ADDR, (byte) 0x01);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT0_ADDR, initialProgress);

        upCountField.set(null, true);

        timer0.run();

        assertEquals(initialProgress+1, dataMemory.readByte(DataMemory_ATmega328P.TCNT0_ADDR));
    }

    @Test
    public void phaseCorrect_topMax_downCount() throws Exception {
        byte initialProgress = (byte) 0x01;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR0B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0A_ADDR, (byte) 0x01);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT0_ADDR, initialProgress);

        upCountField.set(null, false);

        timer0.run();

        assertEquals(initialProgress-1, dataMemory.readByte(DataMemory_ATmega328P.TCNT0_ADDR));
    }

    @Test
    public void phaseCorrect_topMAX_changeToDownCount() throws Exception {
        byte initialProgress = Timer0_ATmega328P.MAX - 1;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR0B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0A_ADDR, (byte) 0x01);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT0_ADDR, initialProgress);

        upCountField.set(null, true);

        timer0.run();

        assertFalse((Boolean) upCountField.get(null));
    }

    @Test
    public void phaseCorrect_topMAX_changeToUpCount() throws Exception {
        byte initialProgress = Timer0_ATmega328P.BOTTOM + 1;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR0B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0A_ADDR, (byte) 0x01);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT0_ADDR, initialProgress);

        upCountField.set(null, false);

        timer0.run();

        assertTrue((Boolean) upCountField.get(null));
    }

    @Test
    public void phaseCorrect_topMAX_update_OCRA_OCRB() throws Exception {
        byte initialProgress = Timer0_ATmega328P.MAX - 1;
        byte newOCRA = 0x0A;
        byte newOCRB = 0x0B;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR0B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0A_ADDR, (byte) 0x01);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT0_ADDR, initialProgress);

        dataMemory.writeByte(DataMemory_ATmega328P.OCR0A_ADDR, newOCRA);
        dataMemory.writeByte(DataMemory_ATmega328P.OCR0B_ADDR, newOCRB);

        timer0.run();

        assertEquals(newOCRA, dataMemory.readByte(DataMemory_ATmega328P.OCR0A_ADDR));
        assertEquals(newOCRB, dataMemory.readByte(DataMemory_ATmega328P.OCR0B_ADDR));
    }

    @Test
    public void phaseCorrect_topMAX_normalOperation() throws Exception {
        byte initialProgress = (byte) 0x00;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR0B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0A_ADDR, (byte) 0x01);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT0_ADDR, initialProgress);

        outputControlOC0AField.set(null, true);
        outputControlOC0BField.set(null, true);

        timer0.run();

        assertFalse((Boolean) outputControlOC0AField.get(null));
        assertFalse((Boolean) outputControlOC0BField.get(null));

    }

    @Test
    public void phaseCorrect_topMAX_normalOperation2() throws Exception {
        byte initialProgress = (byte) 0x00;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR0B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0A_ADDR, (byte) 0x51);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT0_ADDR, initialProgress);

        outputControlOC0AField.set(null, true);
        outputControlOC0BField.set(null, true);

        timer0.run();

        assertFalse((Boolean) outputControlOC0AField.get(null));
        assertFalse((Boolean) outputControlOC0BField.get(null));
    }

    @Test
    public void phaseCorrect_topMAX_clearOnCompareMatchUpCount() throws Exception {
        byte valueOCRA = 0x0A;
        byte valueOCRB = valueOCRA;
        byte initialProgress = (byte) (valueOCRA - 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR0B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0A_ADDR, (byte) 0xA1);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT0_ADDR, initialProgress);

        upCountField.set(null, true);
        doubleBufferOCR0AField.set(null, valueOCRA);
        doubleBufferOCR0BField.set(null, valueOCRB);

        outputControlOC0AField.set(null, false);
        outputControlOC0BField.set(null, false);
        stateOC0AField.set(null, 1);
        stateOC0BField.set(null, 1);

        timer0.run();

        assertTrue((Boolean) outputControlOC0AField.get(null));
        assertTrue((Boolean) outputControlOC0BField.get(null));
        assertEquals(0, stateOC0AField.get(null));
        assertEquals(0, stateOC0BField.get(null));
    }

    @Test
    public void phaseCorrect_topMAX_setOnCompareMatchDownCount() throws Exception {
        byte valueOCRA = 0x0A;
        byte valueOCRB = valueOCRA;
        byte initialProgress = (byte) (valueOCRA + 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR0B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0A_ADDR, (byte) 0xA1);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT0_ADDR, initialProgress);

        upCountField.set(null, false);
        doubleBufferOCR0AField.set(null, valueOCRA);
        doubleBufferOCR0BField.set(null, valueOCRB);

        outputControlOC0AField.set(null, false);
        outputControlOC0BField.set(null, false);
        stateOC0AField.set(null, 0);
        stateOC0BField.set(null, 0);

        timer0.run();

        assertTrue((Boolean) outputControlOC0AField.get(null));
        assertTrue((Boolean) outputControlOC0BField.get(null));
        assertEquals(1, stateOC0AField.get(null));
        assertEquals(1, stateOC0BField.get(null));
    }

    @Test
    public void phaseCorrect_topMAX_clearOnCompareMatchDownCount() throws Exception {
        byte valueOCRA = 0x0A;
        byte valueOCRB = valueOCRA;
        byte initialProgress = (byte) (valueOCRA + 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR0B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0A_ADDR, (byte) 0xF1);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT0_ADDR, initialProgress);

        upCountField.set(null, false);
        doubleBufferOCR0AField.set(null, valueOCRA);
        doubleBufferOCR0BField.set(null, valueOCRB);

        outputControlOC0AField.set(null, false);
        outputControlOC0BField.set(null, false);
        stateOC0AField.set(null, 1);
        stateOC0BField.set(null, 1);

        timer0.run();

        assertTrue((Boolean) outputControlOC0AField.get(null));
        assertTrue((Boolean) outputControlOC0BField.get(null));
        assertEquals(0, stateOC0AField.get(null));
        assertEquals(0, stateOC0BField.get(null));
    }

    @Test
    public void phaseCorrect_topMAX_setOnCompareMatchUpCount() throws Exception {
        byte valueOCRA = 0x0A;
        byte valueOCRB = valueOCRA;
        byte initialProgress = (byte) (valueOCRA - 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR0B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0A_ADDR, (byte) 0xF1);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT0_ADDR, initialProgress);

        upCountField.set(null, true);
        doubleBufferOCR0AField.set(null, valueOCRA);
        doubleBufferOCR0BField.set(null, valueOCRB);

        outputControlOC0AField.set(null, false);
        outputControlOC0BField.set(null, false);
        stateOC0AField.set(null, 0);
        stateOC0BField.set(null, 0);

        timer0.run();

        assertTrue((Boolean) outputControlOC0AField.get(null));
        assertTrue((Boolean) outputControlOC0BField.get(null));
        assertEquals(1, stateOC0AField.get(null));
        assertEquals(1, stateOC0BField.get(null));
    }

    @Test
    public void ctcMode_count() throws IllegalAccessException {
        byte initialProgress = 0x00;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR0B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0A_ADDR, (byte) 0x02);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT0_ADDR, initialProgress);

        nextOverflowField.set(null, false);
        nextClearField.set(null, false);

        timer0.run();

        assertEquals(initialProgress+1, dataMemory.readByte(DataMemory_ATmega328P.TCNT0_ADDR));
    }

    @Test
    public void ctcMode_countOverflow() throws IllegalAccessException {
        byte initialProgress = Timer0_ATmega328P.MAX;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR0B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0A_ADDR, (byte) 0x02);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT0_ADDR, initialProgress);

        nextOverflowField.set(null, true);
        nextClearField.set(null, false);

        timer0.run();

        assertEquals(Timer0_ATmega328P.BOTTOM, dataMemory.readByte(DataMemory_ATmega328P.TCNT0_ADDR));
        assertFalse((Boolean) nextOverflowField.get(null));
    }

    @Test
    public void ctcMode_countClear() throws IllegalAccessException {
        byte initialProgress = 0x01;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR0B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0A_ADDR, (byte) 0x02);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT0_ADDR, initialProgress);
        dataMemory.writeByte(DataMemory_ATmega328P.OCR0A_ADDR, (byte) 0x0F);

        nextOverflowField.set(null, false);
        nextClearField.set(null, true);

        timer0.run();

        assertEquals(Timer0_ATmega328P.BOTTOM, dataMemory.readByte(DataMemory_ATmega328P.TCNT0_ADDR));
        assertFalse((Boolean) nextClearField.get(null));
    }

    @Test
    public void ctcMode_outputDisconected() throws IllegalAccessException {
        byte initialProgress = 0x01;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR0B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0A_ADDR, (byte) 0x02);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT0_ADDR, initialProgress);
        dataMemory.writeByte(DataMemory_ATmega328P.OCR0A_ADDR, (byte) 0x0F);

        nextOverflowField.set(null, false);
        nextClearField.set(null, false);
        outputControlOC0AField.set(null, true);
        outputControlOC0BField.set(null, true);

        timer0.run();

        assertFalse((Boolean) outputControlOC0AField.get(null));
        assertFalse((Boolean) outputControlOC0BField.get(null));
    }

    @Test
    public void ctcMode_toggleOnCompareMatch() throws IllegalAccessException {
        byte initialProgress = 0x0E;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR0B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0A_ADDR, (byte) 0x52);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT0_ADDR, initialProgress);

        dataMemory.writeByte(DataMemory_ATmega328P.OCR0A_ADDR, (byte) 0x0F);
        dataMemory.writeByte(DataMemory_ATmega328P.OCR0B_ADDR, (byte) 0x0F);

        nextOverflowField.set(null, false);
        nextClearField.set(null, false);
        outputControlOC0AField.set(null, false);
        outputControlOC0BField.set(null, false);
        stateOC0AField.set(null, 0);
        stateOC0BField.set(null, 1);

        timer0.run();

        assertEquals(1, stateOC0AField.get(null));
        assertEquals(0, stateOC0BField.get(null));
        assertTrue((Boolean) nextClearField.get(null));
    }

    @Test
    public void ctcMode_toggleOnCompareMatch_forceMatch() throws IllegalAccessException {
        byte initialProgress = 0x01;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR0B_ADDR, 3, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR0B_ADDR, 7, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR0B_ADDR, 6, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0A_ADDR, (byte) 0x52);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT0_ADDR, initialProgress);

        dataMemory.writeByte(DataMemory_ATmega328P.OCR0A_ADDR, (byte) 0x0F);
        dataMemory.writeByte(DataMemory_ATmega328P.OCR0B_ADDR, (byte) 0x0F);

        nextOverflowField.set(null, false);
        nextClearField.set(null, false);
        outputControlOC0AField.set(null, false);
        outputControlOC0BField.set(null, false);
        stateOC0AField.set(null, 0);
        stateOC0BField.set(null, 1);

        timer0.run();

        assertEquals(1, stateOC0AField.get(null));
        assertEquals(0, stateOC0BField.get(null));
        assertFalse((Boolean) nextClearField.get(null));
    }

    @Test
    public void ctcMode_clearOnCompareMatch() throws IllegalAccessException {
        byte initialProgress = 0x0E;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR0B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0A_ADDR, (byte) 0xA2);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT0_ADDR, initialProgress);

        dataMemory.writeByte(DataMemory_ATmega328P.OCR0A_ADDR, (byte) 0x0F);
        dataMemory.writeByte(DataMemory_ATmega328P.OCR0B_ADDR, (byte) 0x0F);

        nextOverflowField.set(null, false);
        nextClearField.set(null, false);
        outputControlOC0AField.set(null, false);
        outputControlOC0BField.set(null, false);
        stateOC0AField.set(null, 1);
        stateOC0BField.set(null, 1);

        timer0.run();

        assertEquals(0, stateOC0AField.get(null));
        assertEquals(0, stateOC0BField.get(null));
        assertTrue((Boolean) nextClearField.get(null));
    }

    @Test
    public void ctcMode_setOnCompareMatch_forceMatch() throws IllegalAccessException {
        byte initialProgress = 0x0E;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR0B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0A_ADDR, (byte) 0xF2);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT0_ADDR, initialProgress);

        dataMemory.writeByte(DataMemory_ATmega328P.OCR0A_ADDR, (byte) 0x0F);
        dataMemory.writeByte(DataMemory_ATmega328P.OCR0B_ADDR, (byte) 0x0F);

        nextOverflowField.set(null, false);
        nextClearField.set(null, false);
        outputControlOC0AField.set(null, false);
        outputControlOC0BField.set(null, false);
        stateOC0AField.set(null, 0);
        stateOC0BField.set(null, 0);

        timer0.run();

        assertEquals(1, stateOC0AField.get(null));
        assertEquals(1, stateOC0BField.get(null));
        assertTrue((Boolean) nextClearField.get(null));
    }

    @Test
    public void fastPWM_topOCRA_count() throws Exception {
        byte initialProgress = (byte) 0x00;
        byte top = 0x0A;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR0B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0A_ADDR, (byte) 0x03);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT0_ADDR, initialProgress);

        doubleBufferOCR0AField.set(null, top);

        timer0.run();

        assertEquals(initialProgress+1, dataMemory.readByte(DataMemory_ATmega328P.TCNT0_ADDR));
    }

    @Test
    public void fastPWM_topOCRA_updateTop() throws Exception {
        byte initialProgress = (byte) 0x00;
        byte top = 0x0A;
        byte newTop = 0x0B;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR0B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0A_ADDR, (byte) 0x03);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT0_ADDR, initialProgress);
        dataMemory.writeByte(DataMemory_ATmega328P.OCR0A_ADDR, newTop);

        doubleBufferOCR0AField.set(null, top);

        timer0.run();

        assertEquals(newTop, doubleBufferOCR0AField.get(null));

    }

    @Test
    public void fastPWM_topOCRA_normalOperation() throws Exception {
        byte initialProgress = (byte) 0x00;
        byte top = 0x0A;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR0B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0A_ADDR, (byte) 0x03);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT0_ADDR, initialProgress);

        doubleBufferOCR0AField.set(null, top);
        outputControlOC0AField.set(null, true);
        outputControlOC0BField.set(null, true);

        timer0.run();

        assertFalse((Boolean) outputControlOC0AField.get(null));
        assertFalse((Boolean) outputControlOC0BField.get(null));

    }

    @Test
    public void fastPWM_topOCRA_toggleOC0AOnCompareMatch() throws Exception {
        byte initialProgress = (byte) 0x09;
        byte top = 0x0A;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR0B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0A_ADDR, (byte) 0x53);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT0_ADDR, initialProgress);

        doubleBufferOCR0AField.set(null, top);
        doubleBufferOCR0BField.set(null, top);
        outputControlOC0AField.set(null, false);
        outputControlOC0BField.set(null, true);
        stateOC0AField.set(null, 1);

        timer0.run();

        assertTrue((Boolean) outputControlOC0AField.get(null));
        assertFalse((Boolean) outputControlOC0BField.get(null));
        assertEquals(0, stateOC0AField.get(null));
        assertTrue((Boolean) nextClearField.get(null));
    }

    @Test
    public void fastPWM_topOCRA_clearOnCompareMatch() throws Exception {
        byte initialProgress = (byte) 0x09;
        byte top = 0x0A;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR0B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0A_ADDR, (byte) 0xA3);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT0_ADDR, initialProgress);

        doubleBufferOCR0AField.set(null, top);
        doubleBufferOCR0BField.set(null, top);

        outputControlOC0AField.set(null, false);
        outputControlOC0BField.set(null, false);

        stateOC0AField.set(null, 1);
        stateOC0BField.set(null, 1);

        timer0.run();

        assertTrue((Boolean) outputControlOC0AField.get(null));
        assertTrue((Boolean) outputControlOC0BField.get(null));
        assertEquals(0, stateOC0AField.get(null));
        assertEquals(0, stateOC0BField.get(null));
    }

    @Test
    public void fastPWM_topOCRA_clearOnCompareMatch_setAtBottom() throws Exception {
        byte initialProgress = (byte) 0x0A;
        byte top = 0x0A;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR0B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0A_ADDR, (byte) 0xA3);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT0_ADDR, initialProgress);

        nextClearField.set(null, true);
        doubleBufferOCR0AField.set(null, top);
        doubleBufferOCR0BField.set(null, top);

        outputControlOC0AField.set(null, false);
        outputControlOC0BField.set(null, false);

        stateOC0AField.set(null, 0);
        stateOC0BField.set(null, 0);

        timer0.run();

        assertTrue((Boolean) outputControlOC0AField.get(null));
        assertTrue((Boolean) outputControlOC0BField.get(null));
        assertEquals(1, stateOC0AField.get(null));
        assertEquals(1, stateOC0BField.get(null));
        assertEquals(Timer0_ATmega328P.BOTTOM, dataMemory.readByte(DataMemory_ATmega328P.TCNT0_ADDR));
    }

    @Test
    public void fastPWM_topOCRA_setOnCompareMatch() throws Exception {
        byte initialProgress = (byte) 0x09;
        byte top = 0x0A;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR0B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0A_ADDR, (byte) 0xF3);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT0_ADDR, initialProgress);

        doubleBufferOCR0AField.set(null, top);
        doubleBufferOCR0BField.set(null, top);

        outputControlOC0AField.set(null, false);
        outputControlOC0BField.set(null, false);

        stateOC0AField.set(null, 0);
        stateOC0BField.set(null, 0);

        timer0.run();

        assertTrue((Boolean) outputControlOC0AField.get(null));
        assertTrue((Boolean) outputControlOC0BField.get(null));
        assertEquals(1, stateOC0AField.get(null));
        assertEquals(1, stateOC0BField.get(null));
    }

    @Test
    public void fastPWM_topOCRA_setOnCompareMatch_clearAtBottom() throws Exception {
        byte initialProgress = (byte) 0x0A;
        byte top = 0x0A;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR0B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0A_ADDR, (byte) 0xF3);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT0_ADDR, initialProgress);

        nextClearField.set(null, true);
        doubleBufferOCR0AField.set(null, top);
        doubleBufferOCR0BField.set(null, top);

        outputControlOC0AField.set(null, false);
        outputControlOC0BField.set(null, false);

        stateOC0AField.set(null, 1);
        stateOC0BField.set(null, 1);

        timer0.run();

        assertTrue((Boolean) outputControlOC0AField.get(null));
        assertTrue((Boolean) outputControlOC0BField.get(null));
        assertEquals(0, stateOC0AField.get(null));
        assertEquals(0, stateOC0BField.get(null));
        assertEquals(Timer0_ATmega328P.BOTTOM, dataMemory.readByte(DataMemory_ATmega328P.TCNT0_ADDR));
    }

    @Test
    public void fastPWM_topMAX_count() throws Exception {
        byte initialProgress = (byte) 0x00;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR0B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0A_ADDR, (byte) 0x03);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT0_ADDR, initialProgress);

        timer0.run();

        assertEquals(initialProgress+1, dataMemory.readByte(DataMemory_ATmega328P.TCNT0_ADDR));
    }

    @Test
    public void fastPWM_topMAX_updateTop() throws Exception {
        byte initialProgress = (byte) 0x00;
        byte top = 0x0A;
        byte newTop = 0x0B;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR0B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0A_ADDR, (byte) 0x03);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT0_ADDR, initialProgress);
        dataMemory.writeByte(DataMemory_ATmega328P.OCR0A_ADDR, newTop);
        dataMemory.writeByte(DataMemory_ATmega328P.OCR0B_ADDR, newTop);

        doubleBufferOCR0AField.set(null, top);
        doubleBufferOCR0BField.set(null, top);

        timer0.run();

        assertEquals(newTop, doubleBufferOCR0AField.get(null));
        assertEquals(newTop, doubleBufferOCR0BField.get(null));
    }

    @Test
    public void fastPWM_topMAX_normalOperation() throws Exception {
        byte initialProgress = (byte) 0x00;
        byte top = 0x0A;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR0B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0A_ADDR, (byte) 0x03);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT0_ADDR, initialProgress);

        doubleBufferOCR0AField.set(null, top);
        doubleBufferOCR0BField.set(null, top);
        outputControlOC0AField.set(null, true);
        outputControlOC0BField.set(null, true);

        timer0.run();

        assertFalse((Boolean) outputControlOC0AField.get(null));
        assertFalse((Boolean) outputControlOC0BField.get(null));

    }

    @Test
    public void fastPWM_topMAX_normalOperation2() throws Exception {
        byte initialProgress = (byte) 0x00;
        byte top = 0x0A;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR0B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0A_ADDR, (byte) 0x53);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT0_ADDR, initialProgress);

        doubleBufferOCR0AField.set(null, top);
        doubleBufferOCR0BField.set(null, top);
        outputControlOC0AField.set(null, true);
        outputControlOC0BField.set(null, true);

        timer0.run();

        assertFalse((Boolean) outputControlOC0AField.get(null));
        assertFalse((Boolean) outputControlOC0BField.get(null));

    }

    @Test
    public void fastPWM_topMAX_clearOnCompareMatch() throws Exception {
        byte initialProgress = (byte) 0x09;
        byte top = 0x0A;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR0B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0A_ADDR, (byte) 0xA3);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT0_ADDR, initialProgress);

        doubleBufferOCR0AField.set(null, top);
        doubleBufferOCR0BField.set(null, top);

        outputControlOC0AField.set(null, false);
        outputControlOC0BField.set(null, false);

        stateOC0AField.set(null, 1);
        stateOC0BField.set(null, 1);

        timer0.run();

        assertTrue((Boolean) outputControlOC0AField.get(null));
        assertTrue((Boolean) outputControlOC0BField.get(null));
        assertEquals(0, stateOC0AField.get(null));
        assertEquals(0, stateOC0BField.get(null));
    }

    @Test
    public void fastPWM_topMAX_clearOnCompareMatch_setAtBottom() throws Exception {
        byte initialProgress = Timer0_ATmega328P.MAX;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR0B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0A_ADDR, (byte) 0xA3);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT0_ADDR, initialProgress);

        outputControlOC0AField.set(null, false);
        outputControlOC0BField.set(null, false);

        stateOC0AField.set(null, 0);
        stateOC0BField.set(null, 0);

        timer0.run();

        assertTrue((Boolean) outputControlOC0AField.get(null));
        assertTrue((Boolean) outputControlOC0BField.get(null));
        assertEquals(1, stateOC0AField.get(null));
        assertEquals(1, stateOC0BField.get(null));
        assertEquals(Timer0_ATmega328P.BOTTOM, dataMemory.readByte(DataMemory_ATmega328P.TCNT0_ADDR));
    }

    @Test
    public void fastPWM_topMAX_setOnCompareMatch() throws Exception {
        byte initialProgress = (byte) 0x09;
        byte top = 0x0A;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR0B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0A_ADDR, (byte) 0xF3);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT0_ADDR, initialProgress);

        doubleBufferOCR0AField.set(null, top);
        doubleBufferOCR0BField.set(null, top);

        outputControlOC0AField.set(null, false);
        outputControlOC0BField.set(null, false);

        stateOC0AField.set(null, 0);
        stateOC0BField.set(null, 0);

        timer0.run();

        assertTrue((Boolean) outputControlOC0AField.get(null));
        assertTrue((Boolean) outputControlOC0BField.get(null));
        assertEquals(1, stateOC0AField.get(null));
        assertEquals(1, stateOC0BField.get(null));
    }

    @Test
    public void fastPWM_topMAX_setOnCompareMatch_clearAtBottom() throws Exception {
        byte initialProgress = Timer0_ATmega328P.MAX;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR0B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR0A_ADDR, (byte) 0xF3);
        dataMemory.writeByte(DataMemory_ATmega328P.TCNT0_ADDR, initialProgress);

        outputControlOC0AField.set(null, false);
        outputControlOC0BField.set(null, false);

        stateOC0AField.set(null, 1);
        stateOC0BField.set(null, 1);

        timer0.run();

        assertTrue((Boolean) outputControlOC0AField.get(null));
        assertTrue((Boolean) outputControlOC0BField.get(null));
        assertEquals(0, stateOC0AField.get(null));
        assertEquals(0, stateOC0BField.get(null));
        assertEquals(Timer0_ATmega328P.BOTTOM, dataMemory.readByte(DataMemory_ATmega328P.TCNT0_ADDR));
    }
}