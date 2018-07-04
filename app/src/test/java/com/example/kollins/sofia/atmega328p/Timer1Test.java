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
@PrepareForTest({Timer1_ATmega328P.class, DataMemory_ATmega328P.class, UCModule.class, InterruptionModule_ATmega328P.class})
public class Timer1Test {

    private Field clockPrescalerField, oldExternalT1Field;
    private Field outputControlOC1AField, outputControlOC1BField;
    private Field stateOC1AField, stateOC1BField;
    private Field upCountField;
    private Field doubleBufferOCR1AField, doubleBufferOCR1BField;
    private Field nextOverflowField, nextClearField;
    private Field oldICP1Field;

    private Timer1_ATmega328P timer1;
    private DataMemory_ATmega328P dataMemory;

    private static IOModule ioModule;

    @Before
    public void prepareForTest() throws Exception {
        ioModule = mock(IOModule_ATmega328P.class);

        dataMemory = new DataMemory_ATmega328P(ioModule);
        timer1 = new Timer1_ATmega328P(dataMemory, ioModule);
        UCModule.interruptionModule = new InterruptionModule_ATmega328P();
        UCModule.interruptionModule.setMemory(dataMemory);

        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1B_ADDR, (byte) 0x01);
        dataMemory.writeIOBit(DataMemory_ATmega328P.GTCCR_ADDR, 0, false);

        clockPrescalerField = Timer1_ATmega328P.class.getDeclaredField("clockCount");
        clockPrescalerField.setAccessible(true);

        oldExternalT1Field = Timer1_ATmega328P.class.getDeclaredField("oldExternalT1");
        oldExternalT1Field.setAccessible(true);

        outputControlOC1AField = Timer1_ATmega328P.class.getDeclaredField("timerOutputControl_OC1A");
        outputControlOC1AField.setAccessible(true);

        outputControlOC1BField = Timer1_ATmega328P.class.getDeclaredField("timerOutputControl_OC1B");
        outputControlOC1BField.setAccessible(true);

        stateOC1AField = Timer1_ATmega328P.class.getDeclaredField("stateOC1A");
        stateOC1AField.setAccessible(true);

        stateOC1BField = Timer1_ATmega328P.class.getDeclaredField("stateOC1B");
        stateOC1BField.setAccessible(true);

        upCountField = Timer1_ATmega328P.class.getDeclaredField("phaseCorrect_UPCount");
        upCountField.setAccessible(true);

        doubleBufferOCR1AField = Timer1_ATmega328P.class.getDeclaredField("doubleBufferOCR1A");
        doubleBufferOCR1AField.setAccessible(true);

        doubleBufferOCR1BField = Timer1_ATmega328P.class.getDeclaredField("doubleBufferOCR1B");
        doubleBufferOCR1BField.setAccessible(true);

        nextClearField = Timer1_ATmega328P.class.getDeclaredField("nextClear");
        nextClearField.setAccessible(true);

        nextOverflowField = Timer1_ATmega328P.class.getDeclaredField("nextOverflow");
        nextOverflowField.setAccessible(true);

        oldICP1Field = Timer1_ATmega328P.class.getDeclaredField("oldICP1");
        oldICP1Field.setAccessible(true);

        PowerMockito.doNothing().when(ioModule, "setOC1A", Matchers.anyInt(), Matchers.anyLong());
        PowerMockito.doNothing().when(ioModule, "setOC1B", Matchers.anyInt(), Matchers.anyLong());

        Whitebox.setInternalState(dataMemory,"timer1WriteEnable", true);
        Whitebox.setInternalState(dataMemory,"flagOCR1AReady", true);
    }

    @Test
    public void noClock(){
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1B_ADDR, (byte) 0x00);
        assertFalse(Timer1_ATmega328P.ClockSource.values()[0x07 & dataMemory.readByte(DataMemory_ATmega328P.TCCR1B_ADDR)].work());
    }

    @Test
    public void prescaler1(){
        assertTrue(Timer1_ATmega328P.ClockSource.values()[0x07 & dataMemory.readByte(DataMemory_ATmega328P.TCCR1B_ADDR)].work());
    }

    @Test
    public void prescaler8_notFull() throws IllegalAccessException {
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1B_ADDR, (byte) 0x02);
        clockPrescalerField.set(null, (short) 0);
        assertFalse(Timer1_ATmega328P.ClockSource.values()[0x07 & dataMemory.readByte(DataMemory_ATmega328P.TCCR1B_ADDR)].work());
    }

    @Test
    public void prescaler8_full() throws IllegalAccessException {
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1B_ADDR, (byte) 0x02);
        clockPrescalerField.set(null, (short) 8);
        assertTrue(Timer1_ATmega328P.ClockSource.values()[0x07 & dataMemory.readByte(DataMemory_ATmega328P.TCCR1B_ADDR)].work());
        assertEquals((short) 0, clockPrescalerField.get(null));
    }

    @Test
    public void prescaler64_notFull() throws IllegalAccessException {
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1B_ADDR, (byte) 0x03);
        clockPrescalerField.set(null, (short) 0);
        assertFalse(Timer1_ATmega328P.ClockSource.values()[0x07 & dataMemory.readByte(DataMemory_ATmega328P.TCCR1B_ADDR)].work());
    }

    @Test
    public void prescaler64_full() throws IllegalAccessException {
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1B_ADDR, (byte) 0x03);
        clockPrescalerField.set(null, (short) 64);
        assertTrue(Timer1_ATmega328P.ClockSource.values()[0x07 & dataMemory.readByte(DataMemory_ATmega328P.TCCR1B_ADDR)].work());
        assertEquals((short) 0, clockPrescalerField.get(null));
    }

    @Test
    public void prescaler256_notFull() throws IllegalAccessException {
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1B_ADDR, (byte) 0x04);
        clockPrescalerField.set(null, (short) 0);
        assertFalse(Timer1_ATmega328P.ClockSource.values()[0x07 & dataMemory.readByte(DataMemory_ATmega328P.TCCR1B_ADDR)].work());
    }

    @Test
    public void prescaler256_full() throws IllegalAccessException {
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1B_ADDR, (byte) 0x04);
        clockPrescalerField.set(null, (short) 256);
        assertTrue(Timer1_ATmega328P.ClockSource.values()[0x07 & dataMemory.readByte(DataMemory_ATmega328P.TCCR1B_ADDR)].work());
        assertEquals((short) 0, clockPrescalerField.get(null));
    }

    @Test
    public void prescaler1024_notFull() throws IllegalAccessException {
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1B_ADDR, (byte) 0x05);
        clockPrescalerField.set(null, (short) 0);
        assertFalse(Timer1_ATmega328P.ClockSource.values()[0x07 & dataMemory.readByte(DataMemory_ATmega328P.TCCR1B_ADDR)].work());
    }

    @Test
    public void prescaler1024_full() throws IllegalAccessException {
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1B_ADDR, (byte) 0x05);
        clockPrescalerField.set(null, (short) 1024);
        assertTrue(Timer1_ATmega328P.ClockSource.values()[0x07 & dataMemory.readByte(DataMemory_ATmega328P.TCCR1B_ADDR)].work());
        assertEquals((short) 0, clockPrescalerField.get(null));
    }

    @Test
    public void externalClock_fallingEdge_notDetected() throws IllegalAccessException {
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1B_ADDR, (byte) 0x06);

        dataMemory.writeIOBit(DataMemory_ATmega328P.PIND_ADDR, 5, true);
        oldExternalT1Field.set(null, false);

        assertFalse(Timer1_ATmega328P.ClockSource.values()[0x07 & dataMemory.readByte(DataMemory_ATmega328P.TCCR1B_ADDR)].work());
        assertTrue((Boolean) oldExternalT1Field.get(null));
    }

    @Test
    public void externalClock_fallingEdge_detected() throws IllegalAccessException {
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1B_ADDR, (byte) 0x06);

        dataMemory.writeIOBit(DataMemory_ATmega328P.PIND_ADDR, 5, false);
        oldExternalT1Field.set(null, true);

        assertTrue(Timer1_ATmega328P.ClockSource.values()[0x07 & dataMemory.readByte(DataMemory_ATmega328P.TCCR1B_ADDR)].work());
        assertFalse((Boolean) oldExternalT1Field.get(null));
    }

    @Test
    public void externalClock_risingEdge_notDetected() throws IllegalAccessException {
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1B_ADDR, (byte) 0x07);

        dataMemory.writeIOBit(DataMemory_ATmega328P.PIND_ADDR, 5, false);
        oldExternalT1Field.set(null, true);

        assertFalse(Timer1_ATmega328P.ClockSource.values()[0x07 & dataMemory.readByte(DataMemory_ATmega328P.TCCR1B_ADDR)].work());
        assertFalse((Boolean) oldExternalT1Field.get(null));
    }

    @Test
    public void externalClock_risingEdge_detected() throws IllegalAccessException {
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1B_ADDR, (byte) 0x07);

        dataMemory.writeIOBit(DataMemory_ATmega328P.PIND_ADDR, 5, true);
        oldExternalT1Field.set(null, false);

        assertTrue(Timer1_ATmega328P.ClockSource.values()[0x07 & dataMemory.readByte(DataMemory_ATmega328P.TCCR1B_ADDR)].work());
        assertTrue((Boolean) oldExternalT1Field.get(null));
    }

    @Test
    public void synchronizationMode(){
        dataMemory.writeIOBit(DataMemory_ATmega328P.GTCCR_ADDR, 0, true);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        Whitebox.setInternalState(timer1,"modeSelector", (byte) 0xFF);

        assertEquals((byte) 0xFF, Whitebox.getInternalState(timer1,"modeSelector"));
    }

    @Test
    public void inputCapture_risingEdge() throws Exception {
        char progress = 0xABCD;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x00);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 6, true);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        dataMemory.write16bits(DataMemory_ATmega328P.ICR1L_ADDR, DataMemory_ATmega328P.ICR1H_ADDR, (byte) 0x00, (byte) 0x00);

        dataMemory.writeIOBit(DataMemory_ATmega328P.PINB_ADDR, 0, true);
        oldICP1Field.set(null, false);

        timer1.run();

        assertEquals(progress+1, dataMemory.read16bits(DataMemory_ATmega328P.ICR1L_ADDR, DataMemory_ATmega328P.ICR1H_ADDR));
    }

    @Test
    public void inputCapture_fallingEdge() throws Exception {
        char progress = 0xABCD;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x00);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 6, false);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        dataMemory.write16bits(DataMemory_ATmega328P.ICR1L_ADDR, DataMemory_ATmega328P.ICR1H_ADDR, (byte) 0x00, (byte) 0x00);

        dataMemory.writeIOBit(DataMemory_ATmega328P.PINB_ADDR, 0, false);
        oldICP1Field.set(null, true);

        timer1.run();

        assertEquals(progress+1, dataMemory.read16bits(DataMemory_ATmega328P.ICR1L_ADDR, DataMemory_ATmega328P.ICR1H_ADDR));
    }

    @Test
    public void normalMode_count(){
        char progress = 0x0000;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x00);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        timer1.run();

        assertEquals((char) 0x0001, dataMemory.read16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR));
    }

    @Test
    public void normalMode_count2(){
        char progress = 0x00FF;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x00);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        timer1.run();

        assertEquals((char) 0x0100, dataMemory.read16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR));
    }

    @Test
    public void normalMode_countOverflow(){
        char progress = 0xFFFF;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x00);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        timer1.run();

        assertEquals((char) 0x0000, dataMemory.read16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR));
    }

    @Test
    public void normalMode_outputDisconected() throws IllegalAccessException {
        char progress = 0x0000;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x00);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        outputControlOC1AField.set(null, true);
        outputControlOC1BField.set(null, true);

        timer1.run();

        assertFalse((Boolean) outputControlOC1AField.get(null));
        assertFalse((Boolean) outputControlOC1BField.get(null));
    }

    @Test
    public void normalMode_toggleOnCompareMath() throws Exception {
        char progress = 0x0000;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x50);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        progress += 1;
        dataMemory.write16bits(DataMemory_ATmega328P.OCR1AL_ADDR, DataMemory_ATmega328P.OCR1AH_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));
        dataMemory.write16bits(DataMemory_ATmega328P.OCR1BL_ADDR, DataMemory_ATmega328P.OCR1BH_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        stateOC1AField.set(null, 0);
        stateOC1BField.set(null, 1);

        timer1.run();

        assertEquals(1, stateOC1AField.get(null));
        assertEquals(0, stateOC1BField.get(null));
    }

    @Test
    public void normalMode_toggleOnCompareMath_forceMatch() throws Exception {
        char progress = 0x000A;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1C_ADDR, 7, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1C_ADDR, 6, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x50);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        stateOC1AField.set(null, 0);
        stateOC1BField.set(null, 1);

        timer1.run();

        assertEquals(1, stateOC1AField.get(null));
        assertEquals(0, stateOC1BField.get(null));
    }

    @Test
    public void normalMode_clearOnCompareMath() throws Exception {
        char progress = 0x0000;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xA0);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        progress += 1;
        dataMemory.write16bits(DataMemory_ATmega328P.OCR1AL_ADDR, DataMemory_ATmega328P.OCR1AH_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));
        dataMemory.write16bits(DataMemory_ATmega328P.OCR1BL_ADDR, DataMemory_ATmega328P.OCR1BH_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        stateOC1AField.set(null, 1);
        stateOC1BField.set(null, 1);

        timer1.run();

        assertEquals(0, stateOC1AField.get(null));
        assertEquals(0, stateOC1BField.get(null));
    }

    @Test
    public void normalMode_setOnCompareMath() throws Exception {
        char progress = 0x0000;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xF0);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        progress += 1;
        dataMemory.write16bits(DataMemory_ATmega328P.OCR1AL_ADDR, DataMemory_ATmega328P.OCR1AH_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));
        dataMemory.write16bits(DataMemory_ATmega328P.OCR1BL_ADDR, DataMemory_ATmega328P.OCR1BH_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        stateOC1AField.set(null, 0);
        stateOC1BField.set(null, 0);

        timer1.run();

        assertEquals(1, stateOC1AField.get(null));
        assertEquals(1, stateOC1BField.get(null));
    }

    @Test
    public void phaseCorrect_8B_upCount() throws Exception {
        char progress = 0x0000;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x01);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        
        upCountField.set(null, true);

        timer1.run();

        assertEquals(progress+1, dataMemory.read16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR));
    }

    @Test
    public void phaseCorrect_8B_downCount() throws Exception {
        char progress = 0x0001;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x01);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        
        upCountField.set(null, false);

        timer1.run();

        assertEquals(progress-1, dataMemory.read16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR));
    }

    @Test
    public void phaseCorrect_8B_changeToDownCount() throws Exception {
        char progress = Timer1_ATmega328P.MAX_8B-1;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x01);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        
        upCountField.set(null, true);

        timer1.run();

        assertFalse((Boolean) upCountField.get(null));
    }

    @Test
    public void phaseCorrect_8B_changeToUpCount() throws Exception {
        char progress = Timer1_ATmega328P.BOTTOM+1;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x01);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        
        upCountField.set(null, false);

        timer1.run();

        assertTrue((Boolean) upCountField.get(null));
    }

    @Test
    public void phaseCorrect_8B_update_OCRA_OCRB() throws Exception {
        char progress = Timer1_ATmega328P.MAX_8B;
        char newTop = 0x1234;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x01);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        dataMemory.write16bits(DataMemory_ATmega328P.OCR1AL_ADDR, DataMemory_ATmega328P.OCR1AH_ADDR,
                (byte) (0x00FF & newTop), (byte) (0x00FF & (newTop >> 8)));
        dataMemory.write16bits(DataMemory_ATmega328P.OCR1BL_ADDR, DataMemory_ATmega328P.OCR1BH_ADDR,
                (byte) (0x00FF & newTop), (byte) (0x00FF & (newTop >> 8)));

        
        upCountField.set(null, true);

        timer1.run();

        assertEquals(newTop, doubleBufferOCR1AField.get(null));
        assertEquals(newTop, doubleBufferOCR1BField.get(null));
    }

    @Test
    public void phaseCorrect_8B_normalOperation() throws Exception {
        char progress = 0x0000;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x01);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        
        upCountField.set(null, true);

        outputControlOC1AField.set(null, true);
        outputControlOC1BField.set(null, true);

        timer1.run();

        assertFalse((Boolean) outputControlOC1AField.get(null));
        assertFalse((Boolean) outputControlOC1BField.get(null));
    }

    @Test
    public void phaseCorrect_8B_normalOperation2() throws Exception {
        char progress = 0x0000;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x51);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        
        upCountField.set(null, true);

        outputControlOC1AField.set(null, true);
        outputControlOC1BField.set(null, true);

        timer1.run();

        assertFalse((Boolean) outputControlOC1AField.get(null));
        assertFalse((Boolean) outputControlOC1BField.get(null));
    }

    @Test
    public void phaseCorrect_8B_clearOnCompareMatchUpCount() throws Exception {
        char valueOCRA = 0xABCD;
        char valueOCRB = valueOCRA;
        char progress = (char) (valueOCRA - 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xA1);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        
        upCountField.set(null, true);

        doubleBufferOCR1AField.set(null, valueOCRA);
        doubleBufferOCR1BField.set(null, valueOCRB);

        outputControlOC1AField.set(null, false);
        outputControlOC1BField.set(null, false);
        stateOC1AField.set(null, 1);
        stateOC1BField.set(null, 1);

        timer1.run();

        assertTrue((Boolean) outputControlOC1AField.get(null));
        assertTrue((Boolean) outputControlOC1BField.get(null));
        assertEquals(0, stateOC1AField.get(null));
        assertEquals(0, stateOC1BField.get(null));
    }

    @Test
    public void phaseCorrect_8B_setOnCompareMatchDownCount() throws Exception {
        char valueOCRA = 0xABCD;
        char valueOCRB = valueOCRA;
        char progress = (char) (valueOCRA + 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xA1);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        
        upCountField.set(null, false);

        doubleBufferOCR1AField.set(null, valueOCRA);
        doubleBufferOCR1BField.set(null, valueOCRB);

        outputControlOC1AField.set(null, false);
        outputControlOC1BField.set(null, false);
        stateOC1AField.set(null, 0);
        stateOC1BField.set(null, 0);

        timer1.run();

        assertTrue((Boolean) outputControlOC1AField.get(null));
        assertTrue((Boolean) outputControlOC1BField.get(null));
        assertEquals(1, stateOC1AField.get(null));
        assertEquals(1, stateOC1BField.get(null));
    }

    @Test
    public void phaseCorrect_8B_clearOnCompareMatchDownCount() throws Exception {
        char valueOCRA = 0xABCD;
        char valueOCRB = valueOCRA;
        char progress = (char) (valueOCRA + 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xF1);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        
        upCountField.set(null, false);

        doubleBufferOCR1AField.set(null, valueOCRA);
        doubleBufferOCR1BField.set(null, valueOCRB);

        outputControlOC1AField.set(null, false);
        outputControlOC1BField.set(null, false);
        stateOC1AField.set(null, 1);
        stateOC1BField.set(null, 1);

        timer1.run();

        assertTrue((Boolean) outputControlOC1AField.get(null));
        assertTrue((Boolean) outputControlOC1BField.get(null));
        assertEquals(0, stateOC1AField.get(null));
        assertEquals(0, stateOC1BField.get(null));
    }

    @Test
    public void phaseCorrect_8B_setOnCompareMatchUpCount() throws Exception {
        char valueOCRA = 0xABCD;
        char valueOCRB = valueOCRA;
        char progress = (char) (valueOCRA - 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xF1);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        
        upCountField.set(null, true);

        doubleBufferOCR1AField.set(null, valueOCRA);
        doubleBufferOCR1BField.set(null, valueOCRB);

        outputControlOC1AField.set(null, false);
        outputControlOC1BField.set(null, false);
        stateOC1AField.set(null, 0);
        stateOC1BField.set(null, 0);

        timer1.run();

        assertTrue((Boolean) outputControlOC1AField.get(null));
        assertTrue((Boolean) outputControlOC1BField.get(null));
        assertEquals(1, stateOC1AField.get(null));
        assertEquals(1, stateOC1BField.get(null));
    }

    @Test
    public void phaseCorrect_9B_upCount() throws Exception {
        char progress = 0x0000;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x02);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        
        upCountField.set(null, true);

        timer1.run();

        assertEquals(progress+1, dataMemory.read16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR));
    }

    @Test
    public void phaseCorrect_9B_downCount() throws Exception {
        char progress = 0x0001;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x02);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        
        upCountField.set(null, false);

        timer1.run();

        assertEquals(progress-1, dataMemory.read16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR));
    }

    @Test
    public void phaseCorrect_9B_changeToDownCount() throws Exception {
        char progress = Timer1_ATmega328P.MAX_9B-1;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x02);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        
        upCountField.set(null, true);

        timer1.run();

        assertFalse((Boolean) upCountField.get(null));
    }

    @Test
    public void phaseCorrect_9B_changeToUpCount() throws Exception {
        char progress = Timer1_ATmega328P.BOTTOM+1;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x02);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        
        upCountField.set(null, false);

        timer1.run();

        assertTrue((Boolean) upCountField.get(null));
    }

    @Test
    public void phaseCorrect_9B_update_OCRA_OCRB() throws Exception {
        char progress = Timer1_ATmega328P.MAX_9B;
        char newTop = 0x1234;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x02);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        dataMemory.write16bits(DataMemory_ATmega328P.OCR1AL_ADDR, DataMemory_ATmega328P.OCR1AH_ADDR,
                (byte) (0x00FF & newTop), (byte) (0x00FF & (newTop >> 8)));
        dataMemory.write16bits(DataMemory_ATmega328P.OCR1BL_ADDR, DataMemory_ATmega328P.OCR1BH_ADDR,
                (byte) (0x00FF & newTop), (byte) (0x00FF & (newTop >> 8)));

        
        upCountField.set(null, true);

        timer1.run();

        assertEquals(newTop, doubleBufferOCR1AField.get(null));
        assertEquals(newTop, doubleBufferOCR1BField.get(null));
    }

    @Test
    public void phaseCorrect_9B_normalOperation() throws Exception {
        char progress = 0x0000;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x02);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        
        upCountField.set(null, true);

        outputControlOC1AField.set(null, true);
        outputControlOC1BField.set(null, true);

        timer1.run();

        assertFalse((Boolean) outputControlOC1AField.get(null));
        assertFalse((Boolean) outputControlOC1BField.get(null));
    }

    @Test
    public void phaseCorrect_9B_normalOperation2() throws Exception {
        char progress = 0x0000;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x52);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        
        upCountField.set(null, true);

        outputControlOC1AField.set(null, true);
        outputControlOC1BField.set(null, true);

        timer1.run();

        assertFalse((Boolean) outputControlOC1AField.get(null));
        assertFalse((Boolean) outputControlOC1BField.get(null));
    }

    @Test
    public void phaseCorrect_9B_clearOnCompareMatchUpCount() throws Exception {
        char valueOCRA = 0xABCD;
        char valueOCRB = valueOCRA;
        char progress = (char) (valueOCRA - 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xA2);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        
        upCountField.set(null, true);

        doubleBufferOCR1AField.set(null, valueOCRA);
        doubleBufferOCR1BField.set(null, valueOCRB);

        outputControlOC1AField.set(null, false);
        outputControlOC1BField.set(null, false);
        stateOC1AField.set(null, 1);
        stateOC1BField.set(null, 1);

        timer1.run();

        assertTrue((Boolean) outputControlOC1AField.get(null));
        assertTrue((Boolean) outputControlOC1BField.get(null));
        assertEquals(0, stateOC1AField.get(null));
        assertEquals(0, stateOC1BField.get(null));
    }

    @Test
    public void phaseCorrect_9B_setOnCompareMatchDownCount() throws Exception {
        char valueOCRA = 0xABCD;
        char valueOCRB = valueOCRA;
        char progress = (char) (valueOCRA + 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xA2);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        
        upCountField.set(null, false);

        doubleBufferOCR1AField.set(null, valueOCRA);
        doubleBufferOCR1BField.set(null, valueOCRB);

        outputControlOC1AField.set(null, false);
        outputControlOC1BField.set(null, false);
        stateOC1AField.set(null, 0);
        stateOC1BField.set(null, 0);

        timer1.run();

        assertTrue((Boolean) outputControlOC1AField.get(null));
        assertTrue((Boolean) outputControlOC1BField.get(null));
        assertEquals(1, stateOC1AField.get(null));
        assertEquals(1, stateOC1BField.get(null));
    }

    @Test
    public void phaseCorrect_9B_clearOnCompareMatchDownCount() throws Exception {
        char valueOCRA = 0xABCD;
        char valueOCRB = valueOCRA;
        char progress = (char) (valueOCRA + 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xF2);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        
        upCountField.set(null, false);

        doubleBufferOCR1AField.set(null, valueOCRA);
        doubleBufferOCR1BField.set(null, valueOCRB);

        outputControlOC1AField.set(null, false);
        outputControlOC1BField.set(null, false);
        stateOC1AField.set(null, 1);
        stateOC1BField.set(null, 1);

        timer1.run();

        assertTrue((Boolean) outputControlOC1AField.get(null));
        assertTrue((Boolean) outputControlOC1BField.get(null));
        assertEquals(0, stateOC1AField.get(null));
        assertEquals(0, stateOC1BField.get(null));
    }

    @Test
    public void phaseCorrect_9B_setOnCompareMatchUpCount() throws Exception {
        char valueOCRA = 0xABCD;
        char valueOCRB = valueOCRA;
        char progress = (char) (valueOCRA - 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xF2);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        
        upCountField.set(null, true);

        doubleBufferOCR1AField.set(null, valueOCRA);
        doubleBufferOCR1BField.set(null, valueOCRB);

        outputControlOC1AField.set(null, false);
        outputControlOC1BField.set(null, false);
        stateOC1AField.set(null, 0);
        stateOC1BField.set(null, 0);

        timer1.run();

        assertTrue((Boolean) outputControlOC1AField.get(null));
        assertTrue((Boolean) outputControlOC1BField.get(null));
        assertEquals(1, stateOC1AField.get(null));
        assertEquals(1, stateOC1BField.get(null));
    }

    @Test
    public void phaseCorrect_10B_upCount() throws Exception {
        char progress = 0x0000;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x03);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        
        upCountField.set(null, true);

        timer1.run();

        assertEquals(progress+1, dataMemory.read16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR));
    }

    @Test
    public void phaseCorrect_10B_downCount() throws Exception {
        char progress = 0x0001;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x03);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        
        upCountField.set(null, false);

        timer1.run();

        assertEquals(progress-1, dataMemory.read16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR));
    }

    @Test
    public void phaseCorrect_10B_changeToDownCount() throws Exception {
        char progress = Timer1_ATmega328P.MAX_10B-1;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x03);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        
        upCountField.set(null, true);

        timer1.run();

        assertFalse((Boolean) upCountField.get(null));
    }

    @Test
    public void phaseCorrect_10B_changeToUpCount() throws Exception {
        char progress = Timer1_ATmega328P.BOTTOM+1;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x03);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        
        upCountField.set(null, false);

        timer1.run();

        assertTrue((Boolean) upCountField.get(null));
    }

    @Test
    public void phaseCorrect_10B_update_OCRA_OCRB() throws Exception {
        char progress = Timer1_ATmega328P.MAX_10B;
        char newTop = 0x1234;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x03);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        dataMemory.write16bits(DataMemory_ATmega328P.OCR1AL_ADDR, DataMemory_ATmega328P.OCR1AH_ADDR,
                (byte) (0x00FF & newTop), (byte) (0x00FF & (newTop >> 8)));
        dataMemory.write16bits(DataMemory_ATmega328P.OCR1BL_ADDR, DataMemory_ATmega328P.OCR1BH_ADDR,
                (byte) (0x00FF & newTop), (byte) (0x00FF & (newTop >> 8)));

        
        upCountField.set(null, true);

        timer1.run();

        assertEquals(newTop, doubleBufferOCR1AField.get(null));
        assertEquals(newTop, doubleBufferOCR1BField.get(null));
    }

    @Test
    public void phaseCorrect_10B_normalOperation() throws Exception {
        char progress = 0x0000;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x03);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        
        upCountField.set(null, true);

        outputControlOC1AField.set(null, true);
        outputControlOC1BField.set(null, true);

        timer1.run();

        assertFalse((Boolean) outputControlOC1AField.get(null));
        assertFalse((Boolean) outputControlOC1BField.get(null));
    }

    @Test
    public void phaseCorrect_10B_normalOperation2() throws Exception {
        char progress = 0x0000;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x53);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        
        upCountField.set(null, true);

        outputControlOC1AField.set(null, true);
        outputControlOC1BField.set(null, true);

        timer1.run();

        assertFalse((Boolean) outputControlOC1AField.get(null));
        assertFalse((Boolean) outputControlOC1BField.get(null));
    }

    @Test
    public void phaseCorrect_10B_clearOnCompareMatchUpCount() throws Exception {
        char valueOCRA = 0xABCD;
        char valueOCRB = valueOCRA;
        char progress = (char) (valueOCRA - 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xA3);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        
        upCountField.set(null, true);

        doubleBufferOCR1AField.set(null, valueOCRA);
        doubleBufferOCR1BField.set(null, valueOCRB);

        outputControlOC1AField.set(null, false);
        outputControlOC1BField.set(null, false);
        stateOC1AField.set(null, 1);
        stateOC1BField.set(null, 1);

        timer1.run();

        assertTrue((Boolean) outputControlOC1AField.get(null));
        assertTrue((Boolean) outputControlOC1BField.get(null));
        assertEquals(0, stateOC1AField.get(null));
        assertEquals(0, stateOC1BField.get(null));
    }

    @Test
    public void phaseCorrect_10B_setOnCompareMatchDownCount() throws Exception {
        char valueOCRA = 0xABCD;
        char valueOCRB = valueOCRA;
        char progress = (char) (valueOCRA + 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xA3);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        
        upCountField.set(null, false);

        doubleBufferOCR1AField.set(null, valueOCRA);
        doubleBufferOCR1BField.set(null, valueOCRB);

        outputControlOC1AField.set(null, false);
        outputControlOC1BField.set(null, false);
        stateOC1AField.set(null, 0);
        stateOC1BField.set(null, 0);

        timer1.run();

        assertTrue((Boolean) outputControlOC1AField.get(null));
        assertTrue((Boolean) outputControlOC1BField.get(null));
        assertEquals(1, stateOC1AField.get(null));
        assertEquals(1, stateOC1BField.get(null));
    }

    @Test
    public void phaseCorrect_10B_clearOnCompareMatchDownCount() throws Exception {
        char valueOCRA = 0xABCD;
        char valueOCRB = valueOCRA;
        char progress = (char) (valueOCRA + 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xF3);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        
        upCountField.set(null, false);

        doubleBufferOCR1AField.set(null, valueOCRA);
        doubleBufferOCR1BField.set(null, valueOCRB);

        outputControlOC1AField.set(null, false);
        outputControlOC1BField.set(null, false);
        stateOC1AField.set(null, 1);
        stateOC1BField.set(null, 1);

        timer1.run();

        assertTrue((Boolean) outputControlOC1AField.get(null));
        assertTrue((Boolean) outputControlOC1BField.get(null));
        assertEquals(0, stateOC1AField.get(null));
        assertEquals(0, stateOC1BField.get(null));
    }

    @Test
    public void phaseCorrect_10B_setOnCompareMatchUpCount() throws Exception {
        char valueOCRA = 0xABCD;
        char valueOCRB = valueOCRA;
        char progress = (char) (valueOCRA - 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xF3);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        
        upCountField.set(null, true);

        doubleBufferOCR1AField.set(null, valueOCRA);
        doubleBufferOCR1BField.set(null, valueOCRB);

        outputControlOC1AField.set(null, false);
        outputControlOC1BField.set(null, false);
        stateOC1AField.set(null, 0);
        stateOC1BField.set(null, 0);

        timer1.run();

        assertTrue((Boolean) outputControlOC1AField.get(null));
        assertTrue((Boolean) outputControlOC1BField.get(null));
        assertEquals(1, stateOC1AField.get(null));
        assertEquals(1, stateOC1BField.get(null));
    }

    @Test
    public void ctcMode_topOCRA_count() {
        char progress = 0x0000;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x00);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        timer1.run();

        assertEquals((char) 0x0001, dataMemory.read16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR));
    }

    @Test
    public void ctcMode_topOCRA_count2(){
        char progress = 0x00FF;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x00);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        timer1.run();

        assertEquals((char) 0x0100, dataMemory.read16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR));
    }

    @Test
    public void ctcMode_topOCRA_countOverflow() throws IllegalAccessException {
        char progress = 0xFFFF;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x00);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        nextOverflowField.set(null, true);
        nextClearField.set(null, false);

        timer1.run();

        assertEquals((char) 0x0000, dataMemory.read16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR));
        assertFalse((Boolean) nextOverflowField.get(null));
    }

    @Test
    public void ctcMode_topOCRA_countClear() throws IllegalAccessException {
        char progress = 0x0001;
        char top = 0x0A00;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x00);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        dataMemory.write16bits(DataMemory_ATmega328P.OCR1AL_ADDR, DataMemory_ATmega328P.OCR1AH_ADDR,
                (byte) (0x00FF & top), (byte) (0x00FF & (top >> 8)));

        nextOverflowField.set(null, false);
        nextClearField.set(null, true);

        timer1.run();

        assertEquals((char) 0x0000, dataMemory.read16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR));
        assertFalse((Boolean) nextClearField.get(null));
    }

    @Test
    public void ctcMode_topOCRA_outputDisconected() throws IllegalAccessException {
        char progress = 0x0001;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x00);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        nextOverflowField.set(null, false);
        nextClearField.set(null, false);

        outputControlOC1AField.set(null, true);
        outputControlOC1BField.set(null, true);

        timer1.run();

        assertFalse((Boolean) outputControlOC1AField.get(null));
        assertFalse((Boolean) outputControlOC1BField.get(null));
    }

    @Test
    public void ctcMode_topOCRA_toggleOnCompareMath() throws IllegalAccessException {
        char top = 0x0A00;
        char progress = (char) (top - 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x50);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        dataMemory.write16bits(DataMemory_ATmega328P.OCR1AL_ADDR, DataMemory_ATmega328P.OCR1AH_ADDR,
                (byte) (0x00FF & top), (byte) (0x00FF & (top >> 8)));
        dataMemory.write16bits(DataMemory_ATmega328P.OCR1BL_ADDR, DataMemory_ATmega328P.OCR1BH_ADDR,
                (byte) (0x00FF & top), (byte) (0x00FF & (top >> 8)));


        nextOverflowField.set(null, false);
        nextClearField.set(null, false);

        stateOC1AField.set(null, 0);
        stateOC1BField.set(null, 1);

        timer1.run();

        assertEquals(1, stateOC1AField.get(null));
        assertEquals(0, stateOC1BField.get(null));
        assertTrue((Boolean) nextClearField.get(null));
    }

    @Test
    public void ctcMode_topOCRA_toggleOnCompareMath_forceMatch() throws IllegalAccessException {
        char top = 0x0A00;
        char progress = 0x0000;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1C_ADDR, 7, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1C_ADDR, 6, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x50);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        dataMemory.write16bits(DataMemory_ATmega328P.OCR1AL_ADDR, DataMemory_ATmega328P.OCR1AH_ADDR,
                (byte) (0x00FF & top), (byte) (0x00FF & (top >> 8)));
        dataMemory.write16bits(DataMemory_ATmega328P.OCR1BL_ADDR, DataMemory_ATmega328P.OCR1BH_ADDR,
                (byte) (0x00FF & top), (byte) (0x00FF & (top >> 8)));


        nextOverflowField.set(null, false);
        nextClearField.set(null, false);

        stateOC1AField.set(null, 0);
        stateOC1BField.set(null, 1);

        timer1.run();

        assertEquals(1, stateOC1AField.get(null));
        assertEquals(0, stateOC1BField.get(null));
    }

    @Test
    public void ctcMode_topOCRA_clearOnCompareMath() throws IllegalAccessException {
        char top = 0x0A00;
        char progress = (char) (top - 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xA0);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        dataMemory.write16bits(DataMemory_ATmega328P.OCR1AL_ADDR, DataMemory_ATmega328P.OCR1AH_ADDR,
                (byte) (0x00FF & top), (byte) (0x00FF & (top >> 8)));
        dataMemory.write16bits(DataMemory_ATmega328P.OCR1BL_ADDR, DataMemory_ATmega328P.OCR1BH_ADDR,
                (byte) (0x00FF & top), (byte) (0x00FF & (top >> 8)));


        nextOverflowField.set(null, false);
        nextClearField.set(null, false);

        stateOC1AField.set(null, 1);
        stateOC1BField.set(null, 1);

        timer1.run();

        assertEquals(0, stateOC1AField.get(null));
        assertEquals(0, stateOC1BField.get(null));
        assertTrue((Boolean) nextClearField.get(null));
    }

    @Test
    public void ctcMode_topOCRA_setOnCompareMath() throws IllegalAccessException {
        char top = 0x0A00;
        char progress = (char) (top - 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xF0);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        dataMemory.write16bits(DataMemory_ATmega328P.OCR1AL_ADDR, DataMemory_ATmega328P.OCR1AH_ADDR,
                (byte) (0x00FF & top), (byte) (0x00FF & (top >> 8)));
        dataMemory.write16bits(DataMemory_ATmega328P.OCR1BL_ADDR, DataMemory_ATmega328P.OCR1BH_ADDR,
                (byte) (0x00FF & top), (byte) (0x00FF & (top >> 8)));


        nextOverflowField.set(null, false);
        nextClearField.set(null, false);

        stateOC1AField.set(null, 0);
        stateOC1BField.set(null, 0);

        timer1.run();

        assertEquals(1, stateOC1AField.get(null));
        assertEquals(1, stateOC1BField.get(null));
        assertTrue((Boolean) nextClearField.get(null));
    }

    @Test
    public void fastPWM_8B_count() throws Exception {
        char progress = 0x0000;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x01);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        

        timer1.run();

        assertEquals(progress+1, dataMemory.read16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR));
    }


    @Test
    public void fastPWM_8B_update_OCRA_OCRB() throws Exception {
        char progress = Timer1_ATmega328P.BOTTOM;
        char newTop = 0x1234;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x01);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        dataMemory.write16bits(DataMemory_ATmega328P.OCR1AL_ADDR, DataMemory_ATmega328P.OCR1AH_ADDR,
                (byte) (0x00FF & newTop), (byte) (0x00FF & (newTop >> 8)));
        dataMemory.write16bits(DataMemory_ATmega328P.OCR1BL_ADDR, DataMemory_ATmega328P.OCR1BH_ADDR,
                (byte) (0x00FF & newTop), (byte) (0x00FF & (newTop >> 8)));

        

        timer1.run();

        assertEquals(newTop, doubleBufferOCR1AField.get(null));
        assertEquals(newTop, doubleBufferOCR1BField.get(null));
    }

    @Test
    public void fastPWM_8B_normalOperation() throws Exception {
        char progress = 0x0000;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x01);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        

        outputControlOC1AField.set(null, true);
        outputControlOC1BField.set(null, true);

        timer1.run();

        assertFalse((Boolean) outputControlOC1AField.get(null));
        assertFalse((Boolean) outputControlOC1BField.get(null));
    }

    @Test
    public void fastPWM_8B_normalOperation2() throws Exception {
        char progress = 0x0000;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x51);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        
        upCountField.set(null, true);

        outputControlOC1AField.set(null, true);
        outputControlOC1BField.set(null, true);

        timer1.run();

        assertFalse((Boolean) outputControlOC1AField.get(null));
        assertFalse((Boolean) outputControlOC1BField.get(null));
    }

    @Test
    public void fastPWM_8B_clearOnCompareMatch() throws Exception {
        char valueOCRA = 0xABCD;
        char valueOCRB = valueOCRA;
        char progress = (char) (valueOCRA - 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xA1);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        

        doubleBufferOCR1AField.set(null, valueOCRA);
        doubleBufferOCR1BField.set(null, valueOCRB);

        outputControlOC1AField.set(null, false);
        outputControlOC1BField.set(null, false);
        stateOC1AField.set(null, 1);
        stateOC1BField.set(null, 1);

        timer1.run();

        assertTrue((Boolean) outputControlOC1AField.get(null));
        assertTrue((Boolean) outputControlOC1BField.get(null));
        assertEquals(0, stateOC1AField.get(null));
        assertEquals(0, stateOC1BField.get(null));
    }

    @Test
    public void fastPWM_8B_clearOnCompareMatch_setAtBottom() throws Exception {
        char valueOCRA = 0xABCD;
        char valueOCRB = valueOCRA;
        char progress = Timer1_ATmega328P.MAX_8B;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xA1);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        

        nextClearField.set(null, true);
        doubleBufferOCR1AField.set(null, valueOCRA);
        doubleBufferOCR1BField.set(null, valueOCRB);

        outputControlOC1AField.set(null, false);
        outputControlOC1BField.set(null, false);
        stateOC1AField.set(null, 0);
        stateOC1BField.set(null, 0);

        timer1.run();

        assertTrue((Boolean) outputControlOC1AField.get(null));
        assertTrue((Boolean) outputControlOC1BField.get(null));
        assertEquals(1, stateOC1AField.get(null));
        assertEquals(1, stateOC1BField.get(null));
    }

    @Test
    public void fastPWM_8B_setOnCompareMatch() throws Exception {
        char valueOCRA = 0xABCD;
        char valueOCRB = valueOCRA;
        char progress = (char) (valueOCRA - 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xF1);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        

        doubleBufferOCR1AField.set(null, valueOCRA);
        doubleBufferOCR1BField.set(null, valueOCRB);

        outputControlOC1AField.set(null, false);
        outputControlOC1BField.set(null, false);
        stateOC1AField.set(null, 0);
        stateOC1BField.set(null, 0);

        timer1.run();

        assertTrue((Boolean) outputControlOC1AField.get(null));
        assertTrue((Boolean) outputControlOC1BField.get(null));
        assertEquals(1, stateOC1AField.get(null));
        assertEquals(1, stateOC1BField.get(null));
    }

    @Test
    public void fastPWM_8B_setOnCompareMatch_clearAtBottom() throws Exception {
        char valueOCRA = 0xABCD;
        char valueOCRB = valueOCRA;
        char progress = Timer1_ATmega328P.MAX_8B;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xF1);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        

        nextClearField.set(null, true);
        doubleBufferOCR1AField.set(null, valueOCRA);
        doubleBufferOCR1BField.set(null, valueOCRB);

        outputControlOC1AField.set(null, false);
        outputControlOC1BField.set(null, false);
        stateOC1AField.set(null, 1);
        stateOC1BField.set(null, 1);

        timer1.run();

        assertTrue((Boolean) outputControlOC1AField.get(null));
        assertTrue((Boolean) outputControlOC1BField.get(null));
        assertEquals(0, stateOC1AField.get(null));
        assertEquals(0, stateOC1BField.get(null));
    }

    @Test
    public void fastPWM_9B_count() throws Exception {
        char progress = 0x0000;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x02);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        

        timer1.run();

        assertEquals(progress+1, dataMemory.read16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR));
    }


    @Test
    public void fastPWM_9B_update_OCRA_OCRB() throws Exception {
        char progress = Timer1_ATmega328P.BOTTOM;
        char newTop = 0x1234;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x02);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        dataMemory.write16bits(DataMemory_ATmega328P.OCR1AL_ADDR, DataMemory_ATmega328P.OCR1AH_ADDR,
                (byte) (0x00FF & newTop), (byte) (0x00FF & (newTop >> 8)));
        dataMemory.write16bits(DataMemory_ATmega328P.OCR1BL_ADDR, DataMemory_ATmega328P.OCR1BH_ADDR,
                (byte) (0x00FF & newTop), (byte) (0x00FF & (newTop >> 8)));

        

        timer1.run();

        assertEquals(newTop, doubleBufferOCR1AField.get(null));
        assertEquals(newTop, doubleBufferOCR1BField.get(null));
    }

    @Test
    public void fastPWM_9B_normalOperation() throws Exception {
        char progress = 0x0000;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x02);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        

        outputControlOC1AField.set(null, true);
        outputControlOC1BField.set(null, true);

        timer1.run();

        assertFalse((Boolean) outputControlOC1AField.get(null));
        assertFalse((Boolean) outputControlOC1BField.get(null));
    }

    @Test
    public void fastPWM_9B_normalOperation2() throws Exception {
        char progress = 0x0000;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x52);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        
        upCountField.set(null, true);

        outputControlOC1AField.set(null, true);
        outputControlOC1BField.set(null, true);

        timer1.run();

        assertFalse((Boolean) outputControlOC1AField.get(null));
        assertFalse((Boolean) outputControlOC1BField.get(null));
    }

    @Test
    public void fastPWM_9B_clearOnCompareMatch() throws Exception {
        char valueOCRA = 0xABCD;
        char valueOCRB = valueOCRA;
        char progress = (char) (valueOCRA - 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xA2);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        

        doubleBufferOCR1AField.set(null, valueOCRA);
        doubleBufferOCR1BField.set(null, valueOCRB);

        outputControlOC1AField.set(null, false);
        outputControlOC1BField.set(null, false);
        stateOC1AField.set(null, 1);
        stateOC1BField.set(null, 1);

        timer1.run();

        assertTrue((Boolean) outputControlOC1AField.get(null));
        assertTrue((Boolean) outputControlOC1BField.get(null));
        assertEquals(0, stateOC1AField.get(null));
        assertEquals(0, stateOC1BField.get(null));
    }

    @Test
    public void fastPWM_9B_clearOnCompareMatch_setAtBottom() throws Exception {
        char valueOCRA = 0xABCD;
        char valueOCRB = valueOCRA;
        char progress = Timer1_ATmega328P.MAX_9B;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xA2);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        

        nextClearField.set(null, true);
        doubleBufferOCR1AField.set(null, valueOCRA);
        doubleBufferOCR1BField.set(null, valueOCRB);

        outputControlOC1AField.set(null, false);
        outputControlOC1BField.set(null, false);
        stateOC1AField.set(null, 0);
        stateOC1BField.set(null, 0);

        timer1.run();

        assertTrue((Boolean) outputControlOC1AField.get(null));
        assertTrue((Boolean) outputControlOC1BField.get(null));
        assertEquals(1, stateOC1AField.get(null));
        assertEquals(1, stateOC1BField.get(null));
    }

    @Test
    public void fastPWM_9B_setOnCompareMatch() throws Exception {
        char valueOCRA = 0xABCD;
        char valueOCRB = valueOCRA;
        char progress = (char) (valueOCRA - 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xF2);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        

        doubleBufferOCR1AField.set(null, valueOCRA);
        doubleBufferOCR1BField.set(null, valueOCRB);

        outputControlOC1AField.set(null, false);
        outputControlOC1BField.set(null, false);
        stateOC1AField.set(null, 0);
        stateOC1BField.set(null, 0);

        timer1.run();

        assertTrue((Boolean) outputControlOC1AField.get(null));
        assertTrue((Boolean) outputControlOC1BField.get(null));
        assertEquals(1, stateOC1AField.get(null));
        assertEquals(1, stateOC1BField.get(null));
    }

    @Test
    public void fastPWM_9B_setOnCompareMatch_clearAtBottom() throws Exception {
        char valueOCRA = 0xABCD;
        char valueOCRB = valueOCRA;
        char progress = Timer1_ATmega328P.MAX_9B;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xF2);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        

        nextClearField.set(null, true);
        doubleBufferOCR1AField.set(null, valueOCRA);
        doubleBufferOCR1BField.set(null, valueOCRB);

        outputControlOC1AField.set(null, false);
        outputControlOC1BField.set(null, false);
        stateOC1AField.set(null, 1);
        stateOC1BField.set(null, 1);

        timer1.run();

        assertTrue((Boolean) outputControlOC1AField.get(null));
        assertTrue((Boolean) outputControlOC1BField.get(null));
        assertEquals(0, stateOC1AField.get(null));
        assertEquals(0, stateOC1BField.get(null));
    }

    @Test
    public void fastPWM_10B_count() throws Exception {
        char progress = 0x0000;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x03);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        

        timer1.run();

        assertEquals(progress+1, dataMemory.read16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR));
    }


    @Test
    public void fastPWM_10B_update_OCRA_OCRB() throws Exception {
        char progress = Timer1_ATmega328P.BOTTOM;
        char newTop = 0x1234;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x03);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        dataMemory.write16bits(DataMemory_ATmega328P.OCR1AL_ADDR, DataMemory_ATmega328P.OCR1AH_ADDR,
                (byte) (0x00FF & newTop), (byte) (0x00FF & (newTop >> 8)));
        dataMemory.write16bits(DataMemory_ATmega328P.OCR1BL_ADDR, DataMemory_ATmega328P.OCR1BH_ADDR,
                (byte) (0x00FF & newTop), (byte) (0x00FF & (newTop >> 8)));

        

        timer1.run();

        assertEquals(newTop, doubleBufferOCR1AField.get(null));
        assertEquals(newTop, doubleBufferOCR1BField.get(null));
    }

    @Test
    public void fastPWM_10B_normalOperation() throws Exception {
        char progress = 0x0000;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x03);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        

        outputControlOC1AField.set(null, true);
        outputControlOC1BField.set(null, true);

        timer1.run();

        assertFalse((Boolean) outputControlOC1AField.get(null));
        assertFalse((Boolean) outputControlOC1BField.get(null));
    }

    @Test
    public void fastPWM_10B_normalOperation2() throws Exception {
        char progress = 0x0000;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x53);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        
        upCountField.set(null, true);

        outputControlOC1AField.set(null, true);
        outputControlOC1BField.set(null, true);

        timer1.run();

        assertFalse((Boolean) outputControlOC1AField.get(null));
        assertFalse((Boolean) outputControlOC1BField.get(null));
    }

    @Test
    public void fastPWM_10B_clearOnCompareMatch() throws Exception {
        char valueOCRA = 0xABCD;
        char valueOCRB = valueOCRA;
        char progress = (char) (valueOCRA - 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xA3);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        

        doubleBufferOCR1AField.set(null, valueOCRA);
        doubleBufferOCR1BField.set(null, valueOCRB);

        outputControlOC1AField.set(null, false);
        outputControlOC1BField.set(null, false);
        stateOC1AField.set(null, 1);
        stateOC1BField.set(null, 1);

        timer1.run();

        assertTrue((Boolean) outputControlOC1AField.get(null));
        assertTrue((Boolean) outputControlOC1BField.get(null));
        assertEquals(0, stateOC1AField.get(null));
        assertEquals(0, stateOC1BField.get(null));
    }

    @Test
    public void fastPWM_10B_clearOnCompareMatch_setAtBottom() throws Exception {
        char valueOCRA = 0xABCD;
        char valueOCRB = valueOCRA;
        char progress = Timer1_ATmega328P.MAX_9B;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xA3);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        

        nextClearField.set(null, true);
        doubleBufferOCR1AField.set(null, valueOCRA);
        doubleBufferOCR1BField.set(null, valueOCRB);

        outputControlOC1AField.set(null, false);
        outputControlOC1BField.set(null, false);
        stateOC1AField.set(null, 0);
        stateOC1BField.set(null, 0);

        timer1.run();

        assertTrue((Boolean) outputControlOC1AField.get(null));
        assertTrue((Boolean) outputControlOC1BField.get(null));
        assertEquals(1, stateOC1AField.get(null));
        assertEquals(1, stateOC1BField.get(null));
    }

    @Test
    public void fastPWM_10B_setOnCompareMatch() throws Exception {
        char valueOCRA = 0xABCD;
        char valueOCRB = valueOCRA;
        char progress = (char) (valueOCRA - 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xF3);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        

        doubleBufferOCR1AField.set(null, valueOCRA);
        doubleBufferOCR1BField.set(null, valueOCRB);

        outputControlOC1AField.set(null, false);
        outputControlOC1BField.set(null, false);
        stateOC1AField.set(null, 0);
        stateOC1BField.set(null, 0);

        timer1.run();

        assertTrue((Boolean) outputControlOC1AField.get(null));
        assertTrue((Boolean) outputControlOC1BField.get(null));
        assertEquals(1, stateOC1AField.get(null));
        assertEquals(1, stateOC1BField.get(null));
    }

    @Test
    public void fastPWM_10B_setOnCompareMatch_clearAtBottom() throws Exception {
        char valueOCRA = 0xABCD;
        char valueOCRB = valueOCRA;
        char progress = Timer1_ATmega328P.MAX_10B;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xF3);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        

        nextClearField.set(null, true);
        doubleBufferOCR1AField.set(null, valueOCRA);
        doubleBufferOCR1BField.set(null, valueOCRB);

        outputControlOC1AField.set(null, false);
        outputControlOC1BField.set(null, false);
        stateOC1AField.set(null, 1);
        stateOC1BField.set(null, 1);

        timer1.run();

        assertTrue((Boolean) outputControlOC1AField.get(null));
        assertTrue((Boolean) outputControlOC1BField.get(null));
        assertEquals(0, stateOC1AField.get(null));
        assertEquals(0, stateOC1BField.get(null));
    }

    @Test
    public void phaseAndFrequencyCorrect_topICR1_upCount() throws Exception {
        char progress = 0x0000;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x00);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        
        upCountField.set(null, true);

        timer1.run();

        assertEquals(progress+1, dataMemory.read16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR));
    }

    @Test
    public void phaseAndFrequencyCorrect_topICR1_downCount() throws Exception {
        char progress = 0x0001;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x00);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        
        upCountField.set(null, false);

        timer1.run();

        assertEquals(progress-1, dataMemory.read16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR));
    }

    @Test
    public void phaseAndFrequencyCorrect_topICR1_changeToDownCount() throws Exception {
        char top = 0x0A00;
        char progress = (char) (top - 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x00);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        dataMemory.write16bits(DataMemory_ATmega328P.ICR1L_ADDR, DataMemory_ATmega328P.ICR1H_ADDR,
                (byte) (0x00FF & top), (byte) (0x00FF & (top >> 8)));

        

        upCountField.set(null, true);

        timer1.run();

        assertFalse((Boolean) upCountField.get(null));
    }

    @Test
    public void phaseAndFrequencyCorrect_topICR1_changeToUpCount() throws Exception {
        char top = 0x0A00;
        char progress = Timer1_ATmega328P.BOTTOM + 1;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x00);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        dataMemory.write16bits(DataMemory_ATmega328P.ICR1L_ADDR, DataMemory_ATmega328P.ICR1H_ADDR,
                (byte) (0x00FF & top), (byte) (0x00FF & (top >> 8)));

        

        upCountField.set(null, false);

        timer1.run();

        assertTrue((Boolean) upCountField.get(null));
    }

    @Test
    public void phaseAndFrequencyCorrect_topICR1_updateTop() throws Exception {
        char top = 0x0A00;
        char newTop = 0x00B0;
        char progress = Timer1_ATmega328P.BOTTOM;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x00);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        dataMemory.write16bits(DataMemory_ATmega328P.OCR1AL_ADDR, DataMemory_ATmega328P.OCR1AH_ADDR,
                (byte) (0x00FF & newTop), (byte) (0x00FF & (newTop >> 8)));

        dataMemory.write16bits(DataMemory_ATmega328P.OCR1BL_ADDR, DataMemory_ATmega328P.OCR1BH_ADDR,
                (byte) (0x00FF & newTop), (byte) (0x00FF & (newTop >> 8)));

        

        doubleBufferOCR1AField.set(null, top);
        doubleBufferOCR1BField.set(null, top);
        upCountField.set(null, false);

        timer1.run();

        assertEquals(newTop, doubleBufferOCR1AField.get(null));
        assertEquals(newTop, doubleBufferOCR1BField.get(null));
    }

    @Test
    public void phaseAndFrequencyCorrect_topICR1_normalOperation() throws Exception {
        char top = 0x0A00;
        char progress = Timer1_ATmega328P.BOTTOM + 1;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x00);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        dataMemory.write16bits(DataMemory_ATmega328P.ICR1L_ADDR, DataMemory_ATmega328P.ICR1H_ADDR,
                (byte) (0x00FF & top), (byte) (0x00FF & (top >> 8)));

        

        upCountField.set(null, true);
        outputControlOC1AField.set(null, true);
        outputControlOC1BField.set(null, true);

        timer1.run();

        assertFalse((Boolean) outputControlOC1AField.get(null));
        assertFalse((Boolean) outputControlOC1BField.get(null));
    }

    @Test
    public void phaseAndFrequencyCorrect_topICR1_normalOperatio2() throws Exception {
        char top = 0x0A00;
        char progress = Timer1_ATmega328P.BOTTOM + 1;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x50);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        dataMemory.write16bits(DataMemory_ATmega328P.ICR1L_ADDR, DataMemory_ATmega328P.ICR1H_ADDR,
                (byte) (0x00FF & top), (byte) (0x00FF & (top >> 8)));

        

        upCountField.set(null, true);
        outputControlOC1AField.set(null, true);
        outputControlOC1BField.set(null, true);

        timer1.run();

        assertFalse((Boolean) outputControlOC1AField.get(null));
        assertFalse((Boolean) outputControlOC1BField.get(null));
    }

    @Test
    public void phaseAndFrequencyCorrect_topICR1_clearCompareMathUpCount() throws Exception {
        char top = 0x0A00;
        char valueOCR = 0x00B0;
        char progress = (char) (valueOCR - 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xA0);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        dataMemory.write16bits(DataMemory_ATmega328P.ICR1L_ADDR, DataMemory_ATmega328P.ICR1H_ADDR,
                (byte) (0x00FF & top), (byte) (0x00FF & (top >> 8)));

        

        upCountField.set(null, true);

        doubleBufferOCR1AField.set(null, valueOCR);
        doubleBufferOCR1BField.set(null, valueOCR);

        stateOC1AField.set(null, 1);
        stateOC1BField.set(null, 1);

        timer1.run();

        assertEquals(0, stateOC1AField.get(null));
        assertEquals(0, stateOC1BField.get(null));
    }

    @Test
    public void phaseAndFrequencyCorrect_topICR1_setCompareMathDownCount() throws Exception {
        char top = 0x0A00;
        char valueOCR = 0x00B0;
        char progress = (char) (valueOCR + 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xA0);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        dataMemory.write16bits(DataMemory_ATmega328P.ICR1L_ADDR, DataMemory_ATmega328P.ICR1H_ADDR,
                (byte) (0x00FF & top), (byte) (0x00FF & (top >> 8)));

        

        upCountField.set(null, false);

        doubleBufferOCR1AField.set(null, valueOCR);
        doubleBufferOCR1BField.set(null, valueOCR);

        stateOC1AField.set(null, 0);
        stateOC1BField.set(null, 0);

        timer1.run();

        assertEquals(1, stateOC1AField.get(null));
        assertEquals(1, stateOC1BField.get(null));
    }

    @Test
    public void phaseAndFrequencyCorrect_topICR1_setCompareMathUpCount() throws Exception {
        char top = 0x0A00;
        char valueOCR = 0x00B0;
        char progress = (char) (valueOCR - 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xF0);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        dataMemory.write16bits(DataMemory_ATmega328P.ICR1L_ADDR, DataMemory_ATmega328P.ICR1H_ADDR,
                (byte) (0x00FF & top), (byte) (0x00FF & (top >> 8)));

        

        upCountField.set(null, true);

        doubleBufferOCR1AField.set(null, valueOCR);
        doubleBufferOCR1BField.set(null, valueOCR);

        stateOC1AField.set(null, 0);
        stateOC1BField.set(null, 0);

        timer1.run();

        assertEquals(1, stateOC1AField.get(null));
        assertEquals(1, stateOC1BField.get(null));
    }

    @Test
    public void phaseAndFrequencyCorrect_topICR1_clearCompareMathDownCount() throws Exception {
        char top = 0x0A00;
        char valueOCR = 0x00B0;
        char progress = (char) (valueOCR + 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xF0);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        dataMemory.write16bits(DataMemory_ATmega328P.ICR1L_ADDR, DataMemory_ATmega328P.ICR1H_ADDR,
                (byte) (0x00FF & top), (byte) (0x00FF & (top >> 8)));

        

        upCountField.set(null, false);

        doubleBufferOCR1AField.set(null, valueOCR);
        doubleBufferOCR1BField.set(null, valueOCR);

        stateOC1AField.set(null, 1);
        stateOC1BField.set(null, 1);

        timer1.run();

        assertEquals(0, stateOC1AField.get(null));
        assertEquals(0, stateOC1BField.get(null));
    }

    @Test
    public void phaseAndFrequencyCorrect_topOCRA_upCount() throws Exception {
        char progress = 0x0000;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x01);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        
        upCountField.set(null, true);

        timer1.run();

        assertEquals(progress+1, dataMemory.read16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR));
    }

    @Test
    public void phaseAndFrequencyCorrect_topOCRA_downCount() throws Exception {
        char progress = 0x0001;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x01);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        
        upCountField.set(null, false);

        timer1.run();

        assertEquals(progress-1, dataMemory.read16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR));
    }

    @Test
    public void phaseAndFrequencyCorrect_topOCRA_changeToDownCount() throws Exception {
        char top = 0x0A00;
        char progress = (char) (top - 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x01);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        

        upCountField.set(null, true);
        doubleBufferOCR1AField.set(null, top);

        timer1.run();

        assertFalse((Boolean) upCountField.get(null));
    }

    @Test
    public void phaseAndFrequencyCorrect_topOCRA_changeToUpCount() throws Exception {
        char top = 0x0A00;
        char progress = Timer1_ATmega328P.BOTTOM + 1;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x01);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        

        upCountField.set(null, false);
        doubleBufferOCR1AField.set(null, top);

        timer1.run();

        assertTrue((Boolean) upCountField.get(null));
    }

    @Test
    public void phaseAndFrequencyCorrect_topOCRA_updateTop() throws Exception {
        char top = 0x0A00;
        char newTop = 0x00B0;
        char progress = Timer1_ATmega328P.BOTTOM;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x01);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        dataMemory.write16bits(DataMemory_ATmega328P.OCR1AL_ADDR, DataMemory_ATmega328P.OCR1AH_ADDR,
                (byte) (0x00FF & newTop), (byte) (0x00FF & (newTop >> 8)));

        dataMemory.write16bits(DataMemory_ATmega328P.OCR1BL_ADDR, DataMemory_ATmega328P.OCR1BH_ADDR,
                (byte) (0x00FF & newTop), (byte) (0x00FF & (newTop >> 8)));

        

        doubleBufferOCR1AField.set(null, top);
        doubleBufferOCR1BField.set(null, top);
        upCountField.set(null, false);

        timer1.run();

        assertEquals(newTop, doubleBufferOCR1AField.get(null));
        assertEquals(newTop, doubleBufferOCR1BField.get(null));
    }

    @Test
    public void phaseAndFrequencyCorrect_topOCRA_normalOperation() throws Exception {
        char top = 0x0A00;
        char progress = Timer1_ATmega328P.BOTTOM + 1;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x01);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));


        

        upCountField.set(null, true);
        doubleBufferOCR1AField.set(null, top);

        outputControlOC1AField.set(null, true);
        outputControlOC1BField.set(null, true);

        timer1.run();

        assertFalse((Boolean) outputControlOC1AField.get(null));
        assertFalse((Boolean) outputControlOC1BField.get(null));
    }

    @Test
    public void phaseAndFrequencyCorrect_topOCRA_toggleOnCompareMatch() throws Exception {
        char top = 0x0A00;
        char progress = (char) (top - 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x51);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        dataMemory.write16bits(DataMemory_ATmega328P.ICR1L_ADDR, DataMemory_ATmega328P.ICR1H_ADDR,
                (byte) (0x00FF & top), (byte) (0x00FF & (top >> 8)));

        

        upCountField.set(null, true);
        doubleBufferOCR1AField.set(null, top);

        outputControlOC1AField.set(null, false);
        outputControlOC1BField.set(null, true);

        stateOC1AField.set(null, 0);

        timer1.run();

        assertTrue((Boolean) outputControlOC1AField.get(null));
        assertFalse((Boolean) outputControlOC1BField.get(null));
        assertEquals(1, stateOC1AField.get(null));
    }

    @Test
    public void phaseAndFrequencyCorrect_topOCRA_clearCompareMathUpCount_chanelA() throws Exception {
        char top = 0x0A00;
        char progress = (char) (top - 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xA1);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        

        upCountField.set(null, true);

        doubleBufferOCR1AField.set(null, top);

        stateOC1AField.set(null, 1);

        timer1.run();

        assertEquals(0, stateOC1AField.get(null));
    }

    @Test
    public void phaseAndFrequencyCorrect_topOCRA_clearCompareMathUpCount_chanelB() throws Exception {
        char top = 0x0A00;
        char ocraTop = (char) (top + 1);
        char progress = (char) (top - 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xA1);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        

        upCountField.set(null, true);

        doubleBufferOCR1AField.set(null, ocraTop);
        doubleBufferOCR1BField.set(null, top);

        stateOC1BField.set(null, 1);

        timer1.run();

        assertEquals(0, stateOC1BField.get(null));
    }

    @Test
    public void phaseAndFrequencyCorrect_topOCRA_setCompareMathDownCount_chanelB() throws Exception {
        char top = 0x0A00;
        char ocraTop = (char) (top + 1);
        char progress = (char) (top + 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xA1);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        

        upCountField.set(null, false);

        doubleBufferOCR1AField.set(null, ocraTop);
        doubleBufferOCR1BField.set(null, top);

        stateOC1BField.set(null, 0);

        timer1.run();

        assertEquals(1, stateOC1BField.get(null));
    }

    @Test
    public void phaseAndFrequencyCorrect_topOCRA_setCompareMathUpCount_chanelA() throws Exception {
        char top = 0x0A00;
        char progress = (char) (top - 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xF1);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        

        upCountField.set(null, true);

        doubleBufferOCR1AField.set(null, top);

        stateOC1AField.set(null, 0);

        timer1.run();

        assertEquals(1, stateOC1AField.get(null));
    }

    @Test
    public void phaseAndFrequencyCorrect_topOCRA_setCompareMathUpCount_chanelB() throws Exception {
        char top = 0x0A00;
        char ocraTop = (char) (top + 1);
        char progress = (char) (top - 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xF1);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        

        upCountField.set(null, true);

        doubleBufferOCR1AField.set(null, ocraTop);
        doubleBufferOCR1BField.set(null, top);

        stateOC1BField.set(null, 0);

        timer1.run();

        assertEquals(1, stateOC1BField.get(null));
    }

    @Test
    public void phaseAndFrequencyCorrect_topOCRA_clearCompareMathDownCount_chanelB() throws Exception {
        char top = 0x0A00;
        char ocraTop = (char) (top + 1);
        char progress = (char) (top + 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xF1);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        

        upCountField.set(null, false);

        doubleBufferOCR1AField.set(null, ocraTop);
        doubleBufferOCR1BField.set(null, top);

        stateOC1BField.set(null, 1);

        timer1.run();

        assertEquals(0, stateOC1BField.get(null));
    }

    @Test
    public void phaseCorrect_topICR1_upCount() throws Exception {
        char progress = 0x0000;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x02);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        
        upCountField.set(null, true);

        timer1.run();

        assertEquals(progress+1, dataMemory.read16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR));
    }

    @Test
    public void phaseCorrect_topICR1_downCount() throws Exception {
        char progress = 0x0001;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x02);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        
        upCountField.set(null, false);

        timer1.run();

        assertEquals(progress-1, dataMemory.read16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR));
    }

    @Test
    public void phaseCorrect_topICR1_changeToDownCount() throws Exception {
        char top = 0x0A00;
        char progress = (char) (top - 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x02);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        dataMemory.write16bits(DataMemory_ATmega328P.ICR1L_ADDR, DataMemory_ATmega328P.ICR1H_ADDR,
                (byte) (0x00FF & top), (byte) (0x00FF & (top >> 8)));

        

        upCountField.set(null, true);

        timer1.run();

        assertFalse((Boolean) upCountField.get(null));
    }

    @Test
    public void phaseCorrect_topICR1_changeToUpCount() throws Exception {
        char top = 0x0A00;
        char progress = Timer1_ATmega328P.BOTTOM + 1;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x02);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        dataMemory.write16bits(DataMemory_ATmega328P.ICR1L_ADDR, DataMemory_ATmega328P.ICR1H_ADDR,
                (byte) (0x00FF & top), (byte) (0x00FF & (top >> 8)));

        

        upCountField.set(null, false);

        timer1.run();

        assertTrue((Boolean) upCountField.get(null));
    }

    @Test
    public void phaseCorrect_topICR1_updateTop() throws Exception {
        char top = 0x0A00;
        char newTop = 0x00B0;
        char progress = top;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x02);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        dataMemory.write16bits(DataMemory_ATmega328P.ICR1L_ADDR, DataMemory_ATmega328P.ICR1H_ADDR,
                (byte) (0x00FF & top), (byte) (0x00FF & (top >> 8)));

        dataMemory.write16bits(DataMemory_ATmega328P.OCR1AL_ADDR, DataMemory_ATmega328P.OCR1AH_ADDR,
                (byte) (0x00FF & newTop), (byte) (0x00FF & (newTop >> 8)));

        dataMemory.write16bits(DataMemory_ATmega328P.OCR1BL_ADDR, DataMemory_ATmega328P.OCR1BH_ADDR,
                (byte) (0x00FF & newTop), (byte) (0x00FF & (newTop >> 8)));

        

        doubleBufferOCR1AField.set(null, top);
        doubleBufferOCR1BField.set(null, top);
        upCountField.set(null, true);

        timer1.run();

        assertEquals(newTop, doubleBufferOCR1AField.get(null));
        assertEquals(newTop, doubleBufferOCR1BField.get(null));
    }

    @Test
    public void phaseCorrect_topICR1_normalOperation() throws Exception {
        char top = 0x0A00;
        char progress = Timer1_ATmega328P.BOTTOM + 1;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x02);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        dataMemory.write16bits(DataMemory_ATmega328P.ICR1L_ADDR, DataMemory_ATmega328P.ICR1H_ADDR,
                (byte) (0x00FF & top), (byte) (0x00FF & (top >> 8)));

        

        upCountField.set(null, true);
        outputControlOC1AField.set(null, true);
        outputControlOC1BField.set(null, true);

        timer1.run();

        assertFalse((Boolean) outputControlOC1AField.get(null));
        assertFalse((Boolean) outputControlOC1BField.get(null));
    }

    @Test
    public void phaseCorrect_topICR1_toggleOnCompareMatch() throws Exception {
        char top = 0x0A00;
        char valueOCR = 0x00B0;
        char progress = (char) (valueOCR - 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x51);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        dataMemory.write16bits(DataMemory_ATmega328P.ICR1L_ADDR, DataMemory_ATmega328P.ICR1H_ADDR,
                (byte) (0x00FF & top), (byte) (0x00FF & (top >> 8)));

        

        upCountField.set(null, true);
        doubleBufferOCR1AField.set(null, valueOCR);

        outputControlOC1AField.set(null, false);
        outputControlOC1BField.set(null, true);

        stateOC1AField.set(null, 0);

        timer1.run();

        assertTrue((Boolean) outputControlOC1AField.get(null));
        assertFalse((Boolean) outputControlOC1BField.get(null));
        assertEquals(1, stateOC1AField.get(null));
    }

    @Test
    public void phaseCorrect_topICR1_clearCompareMathUpCount() throws Exception {
        char top = 0x0A00;
        char valueOCR = 0x00B0;
        char progress = (char) (valueOCR - 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xA2);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        dataMemory.write16bits(DataMemory_ATmega328P.ICR1L_ADDR, DataMemory_ATmega328P.ICR1H_ADDR,
                (byte) (0x00FF & top), (byte) (0x00FF & (top >> 8)));

        

        upCountField.set(null, true);

        doubleBufferOCR1AField.set(null, valueOCR);
        doubleBufferOCR1BField.set(null, valueOCR);

        stateOC1AField.set(null, 1);
        stateOC1BField.set(null, 1);

        timer1.run();

        assertEquals(0, stateOC1AField.get(null));
        assertEquals(0, stateOC1BField.get(null));
    }

    @Test
    public void phaseCorrect_topICR1_setCompareMathDownCount() throws Exception {
        char top = 0x0A00;
        char valueOCR = 0x00B0;
        char progress = (char) (valueOCR + 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xA2);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        dataMemory.write16bits(DataMemory_ATmega328P.ICR1L_ADDR, DataMemory_ATmega328P.ICR1H_ADDR,
                (byte) (0x00FF & top), (byte) (0x00FF & (top >> 8)));

        

        upCountField.set(null, false);

        doubleBufferOCR1AField.set(null, valueOCR);
        doubleBufferOCR1BField.set(null, valueOCR);

        stateOC1AField.set(null, 0);
        stateOC1BField.set(null, 0);

        timer1.run();

        assertEquals(1, stateOC1AField.get(null));
        assertEquals(1, stateOC1BField.get(null));
    }

    @Test
    public void phaseCorrect_topICR1_setCompareMathUpCount() throws Exception {
        char top = 0x0A00;
        char valueOCR = 0x00B0;
        char progress = (char) (valueOCR - 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xF2);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        dataMemory.write16bits(DataMemory_ATmega328P.ICR1L_ADDR, DataMemory_ATmega328P.ICR1H_ADDR,
                (byte) (0x00FF & top), (byte) (0x00FF & (top >> 8)));

        

        upCountField.set(null, true);

        doubleBufferOCR1AField.set(null, valueOCR);
        doubleBufferOCR1BField.set(null, valueOCR);

        stateOC1AField.set(null, 0);
        stateOC1BField.set(null, 0);

        timer1.run();

        assertEquals(1, stateOC1AField.get(null));
        assertEquals(1, stateOC1BField.get(null));
    }

    @Test
    public void phaseCorrect_topICR1_clearCompareMathDownCount() throws Exception {
        char top = 0x0A00;
        char valueOCR = 0x00B0;
        char progress = (char) (valueOCR + 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xF2);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        dataMemory.write16bits(DataMemory_ATmega328P.ICR1L_ADDR, DataMemory_ATmega328P.ICR1H_ADDR,
                (byte) (0x00FF & top), (byte) (0x00FF & (top >> 8)));

        

        upCountField.set(null, false);

        doubleBufferOCR1AField.set(null, valueOCR);
        doubleBufferOCR1BField.set(null, valueOCR);

        stateOC1AField.set(null, 1);
        stateOC1BField.set(null, 1);

        timer1.run();

        assertEquals(0, stateOC1AField.get(null));
        assertEquals(0, stateOC1BField.get(null));
    }

    @Test
    public void phaseCorrect_topOCRA_upCount() throws Exception {
        char progress = 0x0000;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x03);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        
        upCountField.set(null, true);

        timer1.run();

        assertEquals(progress+1, dataMemory.read16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR));
    }

    @Test
    public void phaseCorrect_topOCRA_downCount() throws Exception {
        char progress = 0x0001;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x03);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        
        upCountField.set(null, false);

        timer1.run();

        assertEquals(progress-1, dataMemory.read16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR));
    }

    @Test
    public void phaseCorrect_topOCRA_changeToDownCount() throws Exception {
        char top = 0x0A00;
        char progress = (char) (top - 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x03);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        

        upCountField.set(null, true);
        doubleBufferOCR1AField.set(null, top);

        timer1.run();

        assertFalse((Boolean) upCountField.get(null));
    }

    @Test
    public void phaseCorrect_topOCRA_changeToUpCount() throws Exception {
        char top = 0x0A00;
        char progress = Timer1_ATmega328P.BOTTOM + 1;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x03);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        

        upCountField.set(null, false);
        doubleBufferOCR1AField.set(null, top);

        timer1.run();

        assertTrue((Boolean) upCountField.get(null));
    }

    @Test
    public void phaseCorrect_topOCRA_updateTop() throws Exception {
        char top = 0x0A00;
        char newTop = 0x00B0;
        char progress = top;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x03);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        dataMemory.write16bits(DataMemory_ATmega328P.OCR1AL_ADDR, DataMemory_ATmega328P.OCR1AH_ADDR,
                (byte) (0x00FF & newTop), (byte) (0x00FF & (newTop >> 8)));

        dataMemory.write16bits(DataMemory_ATmega328P.OCR1BL_ADDR, DataMemory_ATmega328P.OCR1BH_ADDR,
                (byte) (0x00FF & newTop), (byte) (0x00FF & (newTop >> 8)));

        

        doubleBufferOCR1AField.set(null, top);
        doubleBufferOCR1BField.set(null, top);
        upCountField.set(null, false);

        timer1.run();

        assertEquals(newTop, doubleBufferOCR1AField.get(null));
        assertEquals(newTop, doubleBufferOCR1BField.get(null));
    }

    @Test
    public void phaseCorrect_topOCRA_normalOperation() throws Exception {
        char top = 0x0A00;
        char progress = Timer1_ATmega328P.BOTTOM + 1;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x03);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));


        

        upCountField.set(null, true);
        doubleBufferOCR1AField.set(null, top);

        outputControlOC1AField.set(null, true);
        outputControlOC1BField.set(null, true);

        timer1.run();

        assertFalse((Boolean) outputControlOC1AField.get(null));
        assertFalse((Boolean) outputControlOC1BField.get(null));
    }

    @Test
    public void phaseCorrect_topOCRA_toggleOnCompareMatch() throws Exception {
        char top = 0x0A00;
        char progress = (char) (top - 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x53);

        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        upCountField.set(null, true);
        doubleBufferOCR1AField.set(null, top);

        outputControlOC1AField.set(null, false);
        outputControlOC1BField.set(null, true);

        stateOC1AField.set(null, 0);

        timer1.run();

        assertTrue((Boolean) outputControlOC1AField.get(null));
        assertFalse((Boolean) outputControlOC1BField.get(null));
        assertEquals(1, stateOC1AField.get(null));
    }

    @Test
    public void phaseCorrect_topOCRA_clearCompareMathUpCount_chanelA() throws Exception {
        char top = 0x0A00;
        char progress = (char) (top - 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xA3);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        

        upCountField.set(null, true);

        doubleBufferOCR1AField.set(null, top);

        stateOC1AField.set(null, 1);

        timer1.run();

        assertEquals(0, stateOC1AField.get(null));
    }

    @Test
    public void phaseCorrect_topOCRA_clearCompareMathUpCount_chanelB() throws Exception {
        char top = 0x0A00;
        char ocraTop = (char) (top + 1);
        char progress = (char) (top - 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xA3);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        

        upCountField.set(null, true);

        doubleBufferOCR1AField.set(null, ocraTop);
        doubleBufferOCR1BField.set(null, top);

        stateOC1BField.set(null, 1);

        timer1.run();

        assertEquals(0, stateOC1BField.get(null));
    }

    @Test
    public void phaseCorrect_topOCRA_setCompareMathDownCount_chanelB() throws Exception {
        char top = 0x0A00;
        char ocraTop = (char) (top + 1);
        char progress = (char) (top + 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xA3);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        

        upCountField.set(null, false);

        doubleBufferOCR1AField.set(null, ocraTop);
        doubleBufferOCR1BField.set(null, top);

        stateOC1BField.set(null, 0);

        timer1.run();

        assertEquals(1, stateOC1BField.get(null));
    }

    @Test
    public void phaseCorrect_topOCRA_setCompareMathUpCount_chanelA() throws Exception {
        char top = 0x0A00;
        char progress = (char) (top - 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xF3);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        

        upCountField.set(null, true);

        doubleBufferOCR1AField.set(null, top);

        stateOC1AField.set(null, 0);

        timer1.run();

        assertEquals(1, stateOC1AField.get(null));
    }

    @Test
    public void phaseCorrect_topOCRA_setCompareMathUpCount_chanelB() throws Exception {
        char top = 0x0A00;
        char ocraTop = (char) (top + 1);
        char progress = (char) (top - 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xF3);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        

        upCountField.set(null, true);

        doubleBufferOCR1AField.set(null, ocraTop);
        doubleBufferOCR1BField.set(null, top);

        stateOC1BField.set(null, 0);

        timer1.run();

        assertEquals(1, stateOC1BField.get(null));
    }

    @Test
    public void phaseCorrect_topOCRA_clearCompareMathDownCount_chanelB() throws Exception {
        char top = 0x0A00;
        char ocraTop = (char) (top + 1);
        char progress = (char) (top + 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, false);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xF3);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        

        upCountField.set(null, false);

        doubleBufferOCR1AField.set(null, ocraTop);
        doubleBufferOCR1BField.set(null, top);

        stateOC1BField.set(null, 1);

        timer1.run();

        assertEquals(0, stateOC1BField.get(null));
    }

    @Test
    public void ctcMode_topICR1_count() {
        char progress = 0x0000;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x00);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        timer1.run();

        assertEquals((char) 0x0001, dataMemory.read16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR));
    }

    @Test
    public void ctcMode_topICR1_count2(){
        char progress = 0x00FF;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x00);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        timer1.run();

        assertEquals((char) 0x0100, dataMemory.read16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR));
    }

    @Test
    public void ctcMode_topICR1_countOverflow() throws IllegalAccessException {
        char progress = 0xFFFF;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x00);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        nextOverflowField.set(null, true);
        nextClearField.set(null, false);

        timer1.run();

        assertEquals((char) 0x0000, dataMemory.read16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR));
        assertFalse((Boolean) nextOverflowField.get(null));
    }

    @Test
    public void ctcMode_topICR1_countClear() throws IllegalAccessException {
        char progress = 0x0001;
        char top = 0x0A00;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x00);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        dataMemory.write16bits(DataMemory_ATmega328P.ICR1L_ADDR, DataMemory_ATmega328P.ICR1H_ADDR,
                (byte) (0x00FF & top), (byte) (0x00FF & (top >> 8)));

        nextOverflowField.set(null, false);
        nextClearField.set(null, true);

        timer1.run();

        assertEquals((char) 0x0000, dataMemory.read16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR));
        assertFalse((Boolean) nextClearField.get(null));
    }

    @Test
    public void ctcMode_topICR1_outputDisconected() throws IllegalAccessException {
        char progress = 0x0001;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x00);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        nextOverflowField.set(null, false);
        nextClearField.set(null, false);

        outputControlOC1AField.set(null, true);
        outputControlOC1BField.set(null, true);

        timer1.run();

        assertFalse((Boolean) outputControlOC1AField.get(null));
        assertFalse((Boolean) outputControlOC1BField.get(null));
    }

    @Test
    public void ctcMode_topICR1_toggleOnCompareMath() throws IllegalAccessException {
        char top = 0x0A00;
        char progress = (char) (top - 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x50);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        dataMemory.write16bits(DataMemory_ATmega328P.ICR1L_ADDR, DataMemory_ATmega328P.ICR1H_ADDR,
                (byte) (0x00FF & top), (byte) (0x00FF & (top >> 8)));

        dataMemory.write16bits(DataMemory_ATmega328P.OCR1AL_ADDR, DataMemory_ATmega328P.OCR1AH_ADDR,
                (byte) (0x00FF & top), (byte) (0x00FF & (top >> 8)));
        dataMemory.write16bits(DataMemory_ATmega328P.OCR1BL_ADDR, DataMemory_ATmega328P.OCR1BH_ADDR,
                (byte) (0x00FF & top), (byte) (0x00FF & (top >> 8)));


        nextOverflowField.set(null, false);
        nextClearField.set(null, false);

        stateOC1AField.set(null, 0);
        stateOC1BField.set(null, 1);

        timer1.run();

        assertEquals(1, stateOC1AField.get(null));
        assertEquals(0, stateOC1BField.get(null));
        assertTrue((Boolean) nextClearField.get(null));
    }

    @Test
    public void ctcMode_topIRC1_toggleOnCompareMath_forceMatch() throws IllegalAccessException {
        char top = 0x0A00;
        char progress = 0x0000;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1C_ADDR, 7, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1C_ADDR, 6, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x50);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        dataMemory.write16bits(DataMemory_ATmega328P.ICR1L_ADDR, DataMemory_ATmega328P.ICR1H_ADDR,
                (byte) (0x00FF & top), (byte) (0x00FF & (top >> 8)));

        dataMemory.write16bits(DataMemory_ATmega328P.OCR1AL_ADDR, DataMemory_ATmega328P.OCR1AH_ADDR,
                (byte) (0x00FF & top), (byte) (0x00FF & (top >> 8)));
        dataMemory.write16bits(DataMemory_ATmega328P.OCR1BL_ADDR, DataMemory_ATmega328P.OCR1BH_ADDR,
                (byte) (0x00FF & top), (byte) (0x00FF & (top >> 8)));


        nextOverflowField.set(null, false);
        nextClearField.set(null, false);

        stateOC1AField.set(null, 0);
        stateOC1BField.set(null, 1);

        timer1.run();

        assertEquals(1, stateOC1AField.get(null));
        assertEquals(0, stateOC1BField.get(null));
    }

    @Test
    public void ctcMode_topICR1_clearOnCompareMath() throws IllegalAccessException {
        char top = 0x0A00;
        char progress = (char) (top - 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xA0);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        dataMemory.write16bits(DataMemory_ATmega328P.ICR1L_ADDR, DataMemory_ATmega328P.ICR1H_ADDR,
                (byte) (0x00FF & top), (byte) (0x00FF & (top >> 8)));

        dataMemory.write16bits(DataMemory_ATmega328P.OCR1AL_ADDR, DataMemory_ATmega328P.OCR1AH_ADDR,
                (byte) (0x00FF & top), (byte) (0x00FF & (top >> 8)));
        dataMemory.write16bits(DataMemory_ATmega328P.OCR1BL_ADDR, DataMemory_ATmega328P.OCR1BH_ADDR,
                (byte) (0x00FF & top), (byte) (0x00FF & (top >> 8)));


        nextOverflowField.set(null, false);
        nextClearField.set(null, false);

        stateOC1AField.set(null, 1);
        stateOC1BField.set(null, 1);

        timer1.run();

        assertEquals(0, stateOC1AField.get(null));
        assertEquals(0, stateOC1BField.get(null));
        assertTrue((Boolean) nextClearField.get(null));
    }

    @Test
    public void ctcMode_topICR1_setOnCompareMath() throws IllegalAccessException {
        char top = 0x0A00;
        char progress = (char) (top - 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xF0);

        
        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        dataMemory.write16bits(DataMemory_ATmega328P.ICR1L_ADDR, DataMemory_ATmega328P.ICR1H_ADDR,
                (byte) (0x00FF & top), (byte) (0x00FF & (top >> 8)));

        dataMemory.write16bits(DataMemory_ATmega328P.OCR1AL_ADDR, DataMemory_ATmega328P.OCR1AH_ADDR,
                (byte) (0x00FF & top), (byte) (0x00FF & (top >> 8)));
        dataMemory.write16bits(DataMemory_ATmega328P.OCR1BL_ADDR, DataMemory_ATmega328P.OCR1BH_ADDR,
                (byte) (0x00FF & top), (byte) (0x00FF & (top >> 8)));


        nextOverflowField.set(null, false);
        nextClearField.set(null, false);

        stateOC1AField.set(null, 0);
        stateOC1BField.set(null, 0);

        timer1.run();

        assertEquals(1, stateOC1AField.get(null));
        assertEquals(1, stateOC1BField.get(null));
        assertTrue((Boolean) nextClearField.get(null));
    }

    @Test
    public void fastPWM_topICR1_upCount() throws Exception {
        char progress = 0x0000;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x02);

        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        timer1.run();

        assertEquals(progress+1, dataMemory.read16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR));
    }


    @Test
    public void fastPWM_topICR1_updateTop() throws Exception {
        char top = 0x0A00;
        char newTop = 0x00B0;
        char progress = Timer1_ATmega328P.BOTTOM;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x02);

        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        dataMemory.write16bits(DataMemory_ATmega328P.OCR1AL_ADDR, DataMemory_ATmega328P.OCR1AH_ADDR,
                (byte) (0x00FF & newTop), (byte) (0x00FF & (newTop >> 8)));

        dataMemory.write16bits(DataMemory_ATmega328P.OCR1BL_ADDR, DataMemory_ATmega328P.OCR1BH_ADDR,
                (byte) (0x00FF & newTop), (byte) (0x00FF & (newTop >> 8)));

        doubleBufferOCR1AField.set(null, top);
        doubleBufferOCR1BField.set(null, top);

        timer1.run();

        assertEquals(newTop, doubleBufferOCR1AField.get(null));
        assertEquals(newTop, doubleBufferOCR1BField.get(null));
    }

    @Test
    public void fastPWM_topICR1_normalOperation() throws Exception {
        char top = 0x0A00;
        char progress = Timer1_ATmega328P.BOTTOM + 1;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x02);

        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        dataMemory.write16bits(DataMemory_ATmega328P.ICR1L_ADDR, DataMemory_ATmega328P.ICR1H_ADDR,
                (byte) (0x00FF & top), (byte) (0x00FF & (top >> 8)));


        outputControlOC1AField.set(null, true);
        outputControlOC1BField.set(null, true);

        timer1.run();

        assertFalse((Boolean) outputControlOC1AField.get(null));
        assertFalse((Boolean) outputControlOC1BField.get(null));
    }

    @Test
    public void fastPWM_topICR1_toggleOnCompareMatch() throws Exception {
        char top = 0x0A00;
        char valOCR = 0x00B00;
        char progress = (char) (valOCR-1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x52);

        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        dataMemory.write16bits(DataMemory_ATmega328P.ICR1L_ADDR, DataMemory_ATmega328P.ICR1H_ADDR,
                (byte) (0x00FF & top), (byte) (0x00FF & (top >> 8)));

        doubleBufferOCR1AField.set(null, valOCR);
        doubleBufferOCR1BField.set(null, valOCR);

        stateOC1AField.set(null, 0);
        outputControlOC1BField.set(null, true);

        timer1.run();

        assertEquals(1, stateOC1AField.get(null));
        assertFalse((Boolean) outputControlOC1BField.get(null));
    }

    @Test
    public void fastPWM_topICR1_clearCompareMath() throws Exception {
        char top = 0x0A00;
        char valueOCR = 0x00B0;
        char progress = (char) (valueOCR - 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xA2);

        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        dataMemory.write16bits(DataMemory_ATmega328P.ICR1L_ADDR, DataMemory_ATmega328P.ICR1H_ADDR,
                (byte) (0x00FF & top), (byte) (0x00FF & (top >> 8)));

        doubleBufferOCR1AField.set(null, valueOCR);
        doubleBufferOCR1BField.set(null, valueOCR);

        stateOC1AField.set(null, 1);
        stateOC1BField.set(null, 1);

        timer1.run();

        assertEquals(0, stateOC1AField.get(null));
        assertEquals(0, stateOC1BField.get(null));
    }

    @Test
    public void fastPWM_topICR1_clearCompareMath_setAtBottom() throws Exception {
        char top = 0x0A00;
        char valueOCR = 0x00B0;
        char progress = Timer1_ATmega328P.MAX;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xA2);

        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        dataMemory.write16bits(DataMemory_ATmega328P.ICR1L_ADDR, DataMemory_ATmega328P.ICR1H_ADDR,
                (byte) (0x00FF & top), (byte) (0x00FF & (top >> 8)));

        doubleBufferOCR1AField.set(null, valueOCR);
        doubleBufferOCR1BField.set(null, valueOCR);

        stateOC1AField.set(null, 0);
        stateOC1BField.set(null, 0);

        timer1.run();

        assertEquals(1, stateOC1AField.get(null));
        assertEquals(1, stateOC1BField.get(null));
    }

    @Test
    public void fastPWM_topICR1_setCompareMath() throws Exception {
        char top = 0x0A00;
        char valueOCR = 0x00B0;
        char progress = (char) (valueOCR - 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xF2);

        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        dataMemory.write16bits(DataMemory_ATmega328P.ICR1L_ADDR, DataMemory_ATmega328P.ICR1H_ADDR,
                (byte) (0x00FF & top), (byte) (0x00FF & (top >> 8)));


        doubleBufferOCR1AField.set(null, valueOCR);
        doubleBufferOCR1BField.set(null, valueOCR);

        stateOC1AField.set(null, 0);
        stateOC1BField.set(null, 0);

        timer1.run();

        assertEquals(1, stateOC1AField.get(null));
        assertEquals(1, stateOC1BField.get(null));
    }

    @Test
    public void fastPWM_topICR1_setCompareMath_clearAtBottom() throws Exception {
        char top = 0x0A00;
        char valueOCR = 0x00B0;
        char progress = Timer1_ATmega328P.MAX;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xF2);

        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        dataMemory.write16bits(DataMemory_ATmega328P.ICR1L_ADDR, DataMemory_ATmega328P.ICR1H_ADDR,
                (byte) (0x00FF & top), (byte) (0x00FF & (top >> 8)));

        doubleBufferOCR1AField.set(null, valueOCR);
        doubleBufferOCR1BField.set(null, valueOCR);

        stateOC1AField.set(null, 1);
        stateOC1BField.set(null, 1);

        timer1.run();

        assertEquals(0, stateOC1AField.get(null));
        assertEquals(0, stateOC1BField.get(null));
    }

    @Test
    public void fastPWM_topOCRA_upCount() throws Exception {
        char progress = 0x0000;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x03);

        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        timer1.run();

        assertEquals(progress+1, dataMemory.read16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR));
    }

    @Test
    public void fastPWM_topOCRA_updateTop() throws Exception {
        char top = 0x0A00;
        char newTop = 0x00B0;
        char progress = Timer1_ATmega328P.BOTTOM;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x03);

        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        dataMemory.write16bits(DataMemory_ATmega328P.OCR1AL_ADDR, DataMemory_ATmega328P.OCR1AH_ADDR,
                (byte) (0x00FF & newTop), (byte) (0x00FF & (newTop >> 8)));

        dataMemory.write16bits(DataMemory_ATmega328P.OCR1BL_ADDR, DataMemory_ATmega328P.OCR1BH_ADDR,
                (byte) (0x00FF & newTop), (byte) (0x00FF & (newTop >> 8)));

        doubleBufferOCR1AField.set(null, top);
        doubleBufferOCR1BField.set(null, top);

        timer1.run();

        assertEquals(newTop, doubleBufferOCR1AField.get(null));
        assertEquals(newTop, doubleBufferOCR1BField.get(null));
    }

    @Test
    public void fastPWM_topOCRA_normalOperation() throws Exception {
        char top = 0x0A00;
        char progress = Timer1_ATmega328P.BOTTOM + 1;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x03);

        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        doubleBufferOCR1AField.set(null, top);

        outputControlOC1AField.set(null, true);
        outputControlOC1BField.set(null, true);

        timer1.run();

        assertFalse((Boolean) outputControlOC1AField.get(null));
        assertFalse((Boolean) outputControlOC1BField.get(null));
    }

    @Test
    public void fastPWM_topOCRA_toggleOnCompareMatch() throws Exception {
        char top = 0x0A00;
        char progress = (char) (top - 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0x53);

        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        doubleBufferOCR1AField.set(null, top);

        outputControlOC1AField.set(null, false);
        outputControlOC1BField.set(null, true);

        stateOC1AField.set(null, 0);

        timer1.run();

        assertTrue((Boolean) outputControlOC1AField.get(null));
        assertFalse((Boolean) outputControlOC1BField.get(null));
        assertEquals(1, stateOC1AField.get(null));
    }

    @Test
    public void fastPWM_topOCRA_clearCompareMath_chanelA() throws Exception {
        char top = 0x0A00;
        char progress = (char) (top - 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xA3);

        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        doubleBufferOCR1AField.set(null, top);
        doubleBufferOCR1BField.set(null, top);

        stateOC1AField.set(null, 1);

        timer1.run();

        assertEquals(0, stateOC1AField.get(null));
    }

    @Test
    public void fastPWM_topOCRA_clearCompareMath_chanelB() throws Exception {
        char top = 0x0A00;
        char ocraTop = (char) (top + 1);
        char progress = (char) (top - 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xA3);

        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));


        doubleBufferOCR1AField.set(null, ocraTop);
        doubleBufferOCR1BField.set(null, top);

        stateOC1BField.set(null, 1);

        timer1.run();

        assertEquals(0, stateOC1BField.get(null));
    }

    @Test
    public void fastPWM_topOCRA_clearCompareMath_setAtBottom() throws Exception {
        char top = 0x0A00;
        char ocraTop = (char) (top + 1);
        char progress = Timer1_ATmega328P.MAX;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xA2);

        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        doubleBufferOCR1AField.set(null, ocraTop);
        doubleBufferOCR1BField.set(null, top);

        stateOC1AField.set(null, 0);
        stateOC1BField.set(null, 0);

        timer1.run();

        assertEquals(1, stateOC1AField.get(null));
        assertEquals(1, stateOC1BField.get(null));
    }

    @Test
    public void fastPWM_topOCRA_setCompareMath_chanelA() throws Exception {
        char top = 0x0A00;
        char progress = (char) (top - 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xF3);

        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        doubleBufferOCR1AField.set(null, top);

        stateOC1AField.set(null, 0);

        timer1.run();

        assertEquals(1, stateOC1AField.get(null));
    }

    @Test
    public void fastPWM_topOCRA_setCompareMatch_chanelB() throws Exception {
        char top = 0x0A00;
        char ocraTop = (char) (top + 1);
        char progress = (char) (top - 1);

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xF3);

        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        doubleBufferOCR1AField.set(null, ocraTop);
        doubleBufferOCR1BField.set(null, top);

        stateOC1BField.set(null, 0);

        timer1.run();

        assertEquals(1, stateOC1BField.get(null));
    }

    @Test
    public void fastPWM_topOCRA_setCompareMath_clearAtBottom() throws Exception {
        char top = 0x0A00;
        char ocraTop = (char) (top + 1);
        char progress = Timer1_ATmega328P.MAX;

        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TCCR1B_ADDR, 3, true);
        dataMemory.writeByte(DataMemory_ATmega328P.TCCR1A_ADDR, (byte) 0xF3);

        dataMemory.write16bits(DataMemory_ATmega328P.TCNT1L_ADDR, DataMemory_ATmega328P.TCNT1H_ADDR,
                (byte) (0x00FF & progress), (byte) (0x00FF & (progress >> 8)));

        doubleBufferOCR1AField.set(null, ocraTop);
        doubleBufferOCR1BField.set(null, top);

        stateOC1AField.set(null, 1);
        stateOC1BField.set(null, 1);

        timer1.run();

        assertEquals(0, stateOC1AField.get(null));
        assertEquals(0, stateOC1BField.get(null));
    }
}