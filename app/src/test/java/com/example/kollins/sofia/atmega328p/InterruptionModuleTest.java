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

import com.example.kollins.sofia.ucinterfaces.IOModule;
import com.example.kollins.sofia.ucinterfaces.InterruptionModule;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import static com.example.kollins.sofia.atmega328p.InterruptionModule_ATmega328P.POINTER_ADDR_ADC;
import static com.example.kollins.sofia.atmega328p.InterruptionModule_ATmega328P.POINTER_ADDR_INT0;
import static com.example.kollins.sofia.atmega328p.InterruptionModule_ATmega328P.POINTER_ADDR_INT1;
import static com.example.kollins.sofia.atmega328p.InterruptionModule_ATmega328P.POINTER_ADDR_PCINT0;
import static com.example.kollins.sofia.atmega328p.InterruptionModule_ATmega328P.POINTER_ADDR_PCINT1;
import static com.example.kollins.sofia.atmega328p.InterruptionModule_ATmega328P.POINTER_ADDR_PCINT2;
import static com.example.kollins.sofia.atmega328p.InterruptionModule_ATmega328P.POINTER_ADDR_TIMER0_COMP_A;
import static com.example.kollins.sofia.atmega328p.InterruptionModule_ATmega328P.POINTER_ADDR_TIMER0_COMP_B;
import static com.example.kollins.sofia.atmega328p.InterruptionModule_ATmega328P.POINTER_ADDR_TIMER0_OVERFLOW;
import static com.example.kollins.sofia.atmega328p.InterruptionModule_ATmega328P.POINTER_ADDR_TIMER1_CAPTURE_EVENT;
import static com.example.kollins.sofia.atmega328p.InterruptionModule_ATmega328P.POINTER_ADDR_TIMER1_COMP_A;
import static com.example.kollins.sofia.atmega328p.InterruptionModule_ATmega328P.POINTER_ADDR_TIMER1_COMP_B;
import static com.example.kollins.sofia.atmega328p.InterruptionModule_ATmega328P.POINTER_ADDR_TIMER1_OVERFLOW;
import static com.example.kollins.sofia.atmega328p.InterruptionModule_ATmega328P.POINTER_ADDR_TIMER2_COMP_A;
import static com.example.kollins.sofia.atmega328p.InterruptionModule_ATmega328P.POINTER_ADDR_TIMER2_COMP_B;
import static com.example.kollins.sofia.atmega328p.InterruptionModule_ATmega328P.POINTER_ADDR_TIMER2_OVERFLOW;
import static com.example.kollins.sofia.atmega328p.InterruptionModule_ATmega328P.POINTER_ADDR_USART_RX_COMPLETE;
import static com.example.kollins.sofia.atmega328p.InterruptionModule_ATmega328P.POINTER_ADDR_USART_TX_COMPLETE;
import static com.example.kollins.sofia.atmega328p.InterruptionModule_ATmega328P.POINTER_ADDR_USART_UDRE_EMPTY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;


@RunWith(PowerMockRunner.class)
@PrepareForTest({InterruptionModule_ATmega328P.class, DataMemory_ATmega328P.class})
public class InterruptionModuleTest {

    private InterruptionModule_ATmega328P interruptionModule;
    private DataMemory_ATmega328P dataMemory;

    @Mock
    private static IOModule ioModule;

    @Before
    public void prepareForTest() throws Exception {
        interruptionModule = new InterruptionModule_ATmega328P();
        dataMemory = new DataMemory_ATmega328P(ioModule);
        interruptionModule.setMemory(dataMemory);
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 7, true);
    }

    @Test
    public void globalInterruptionDisable(){
        dataMemory.writeBit(DataMemory_ATmega328P.SREG_ADDR, 7, false);
        assertFalse(interruptionModule.haveInterruption());
    }

    @Test
    public void int0_maskDisabled(){
        dataMemory.writeBit(DataMemory_ATmega328P.EIMSK_ADDR, 0, false);
        assertFalse(interruptionModule.haveInterruption());
    }

    @Test
    public void int0_levelInterrupt_HighLevel(){
        dataMemory.writeBit(DataMemory_ATmega328P.EIMSK_ADDR, 0, true);
        dataMemory.writeByte(DataMemory_ATmega328P.EICRA_ADDR, (byte)0x00);
        dataMemory.writeIOBit(DataMemory_ATmega328P.PIND_ADDR, 2, true);
        assertFalse(interruptionModule.haveInterruption());
    }

    @Test
    public void int0_levelInterrupt_LowLevel(){
        dataMemory.writeBit(DataMemory_ATmega328P.EIMSK_ADDR, 0, true);
        dataMemory.writeByte(DataMemory_ATmega328P.EICRA_ADDR, (byte)0x00);
        dataMemory.writeIOBit(DataMemory_ATmega328P.PIND_ADDR, 2, false);

        assertTrue(interruptionModule.haveInterruption());
        assertEquals(InterruptionModule_ATmega328P.INTERRUPT_VECTOR[POINTER_ADDR_INT0],
                Whitebox.getInternalState(interruptionModule,"pcInterruption"));
    }

    @Test
    public void int0_borderInterrupt_noFlag(){
        dataMemory.writeBit(DataMemory_ATmega328P.EIMSK_ADDR, 0, true);
        dataMemory.writeByte(DataMemory_ATmega328P.EICRA_ADDR, (byte)0x01);
        dataMemory.writeIOBit(DataMemory_ATmega328P.EIFR_ADDR, 0, false);

        assertFalse(interruptionModule.haveInterruption());
    }

    @Test
    public void int0_borderInterrupt(){
        dataMemory.writeBit(DataMemory_ATmega328P.EIMSK_ADDR, 0, true);
        dataMemory.writeByte(DataMemory_ATmega328P.EICRA_ADDR, (byte)0x01);
        dataMemory.writeIOBit(DataMemory_ATmega328P.EIFR_ADDR, 0, true);

        assertTrue(interruptionModule.haveInterruption());
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.EIFR_ADDR, 0));
        assertEquals(InterruptionModule_ATmega328P.INTERRUPT_VECTOR[POINTER_ADDR_INT0],
                Whitebox.getInternalState(interruptionModule,"pcInterruption"));
    }

    @Test
    public void int1_maskDisabled(){
        dataMemory.writeBit(DataMemory_ATmega328P.EIMSK_ADDR, 1, false);
        assertFalse(interruptionModule.haveInterruption());
    }

    @Test
    public void int1_levelInterrupt_HighLevel(){
        dataMemory.writeBit(DataMemory_ATmega328P.EIMSK_ADDR, 1, true);
        dataMemory.writeByte(DataMemory_ATmega328P.EICRA_ADDR, (byte)0x00);
        dataMemory.writeIOBit(DataMemory_ATmega328P.PIND_ADDR, 3, true);
        assertFalse(interruptionModule.haveInterruption());
    }

    @Test
    public void int1_levelInterrupt_LowLevel(){
        dataMemory.writeBit(DataMemory_ATmega328P.EIMSK_ADDR, 1, true);
        dataMemory.writeByte(DataMemory_ATmega328P.EICRA_ADDR, (byte)0x00);
        dataMemory.writeIOBit(DataMemory_ATmega328P.PIND_ADDR, 3, false);

        assertTrue(interruptionModule.haveInterruption());
        assertEquals(InterruptionModule_ATmega328P.INTERRUPT_VECTOR[POINTER_ADDR_INT1],
                Whitebox.getInternalState(interruptionModule,"pcInterruption"));
    }

    @Test
    public void int1_borderInterrupt_noFlag(){
        dataMemory.writeBit(DataMemory_ATmega328P.EIMSK_ADDR, 1, true);
        dataMemory.writeByte(DataMemory_ATmega328P.EICRA_ADDR, (byte)0x04);
        dataMemory.writeIOBit(DataMemory_ATmega328P.EIFR_ADDR, 1, false);

        assertFalse(interruptionModule.haveInterruption());
    }

    @Test
    public void int1_borderInterrupt(){
        dataMemory.writeBit(DataMemory_ATmega328P.EIMSK_ADDR, 1, true);
        dataMemory.writeByte(DataMemory_ATmega328P.EICRA_ADDR, (byte)0x04);
        dataMemory.writeIOBit(DataMemory_ATmega328P.EIFR_ADDR, 1, true);

        assertTrue(interruptionModule.haveInterruption());
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.EIFR_ADDR, 1));
        assertEquals(InterruptionModule_ATmega328P.INTERRUPT_VECTOR[POINTER_ADDR_INT1],
                Whitebox.getInternalState(interruptionModule,"pcInterruption"));
    }

    @Test
    public void pcint0_maskDisabled(){
        dataMemory.writeBit(DataMemory_ATmega328P.PCICR_ADDR, 0, false);
        assertFalse(interruptionModule.haveInterruption());
    }

    @Test
    public void pcint0_noFlag(){
        dataMemory.writeBit(DataMemory_ATmega328P.PCICR_ADDR, 0, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.PCIFR_ADDR, 0, false);

        assertFalse(interruptionModule.haveInterruption());
    }

    @Test
    public void pcint0_interrupt(){
        dataMemory.writeBit(DataMemory_ATmega328P.PCICR_ADDR, 0, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.PCIFR_ADDR, 0, true);

        assertTrue(interruptionModule.haveInterruption());
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.PCIFR_ADDR, 0));
        assertEquals(InterruptionModule_ATmega328P.INTERRUPT_VECTOR[POINTER_ADDR_PCINT0],
                Whitebox.getInternalState(interruptionModule,"pcInterruption"));
    }

    @Test
    public void pcint1_maskDisabled(){
        dataMemory.writeBit(DataMemory_ATmega328P.PCICR_ADDR, 1, false);
        assertFalse(interruptionModule.haveInterruption());
    }

    @Test
    public void pcint1_noFlag(){
        dataMemory.writeBit(DataMemory_ATmega328P.PCICR_ADDR, 1, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.PCIFR_ADDR, 1, false);

        assertFalse(interruptionModule.haveInterruption());
    }

    @Test
    public void pcint1_interrupt(){
        dataMemory.writeBit(DataMemory_ATmega328P.PCICR_ADDR, 1, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.PCIFR_ADDR, 1, true);

        assertTrue(interruptionModule.haveInterruption());
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.PCIFR_ADDR, 1));
        assertEquals(InterruptionModule_ATmega328P.INTERRUPT_VECTOR[POINTER_ADDR_PCINT1],
                Whitebox.getInternalState(interruptionModule,"pcInterruption"));
    }

    @Test
    public void pcint2_maskDisabled(){
        dataMemory.writeBit(DataMemory_ATmega328P.PCICR_ADDR, 2, false);
        assertFalse(interruptionModule.haveInterruption());
    }

    @Test
    public void pcint2_noFlag(){
        dataMemory.writeBit(DataMemory_ATmega328P.PCICR_ADDR, 2, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.PCIFR_ADDR, 2, false);

        assertFalse(interruptionModule.haveInterruption());
    }

    @Test
    public void pcint2_interrupt(){
        dataMemory.writeBit(DataMemory_ATmega328P.PCICR_ADDR, 2, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.PCIFR_ADDR, 2, true);

        assertTrue(interruptionModule.haveInterruption());
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.PCIFR_ADDR, 2));
        assertEquals(InterruptionModule_ATmega328P.INTERRUPT_VECTOR[POINTER_ADDR_PCINT2],
                Whitebox.getInternalState(interruptionModule,"pcInterruption"));
    }

    @Test
    public void timer2_CompA_maskDisabled(){
        dataMemory.writeBit(DataMemory_ATmega328P.TIMSK2_ADDR, 1, false);
        assertFalse(interruptionModule.haveInterruption());
    }

    @Test
    public void timer2_CompA_noFlag(){
        dataMemory.writeBit(DataMemory_ATmega328P.TIMSK2_ADDR, 1, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR2_ADDR, 1, false);

        assertFalse(interruptionModule.haveInterruption());
    }

    @Test
    public void timer2_CompA_interrupt(){
        dataMemory.writeBit(DataMemory_ATmega328P.TIMSK2_ADDR, 1, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR2_ADDR, 1, true);

        assertTrue(interruptionModule.haveInterruption());
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.TIFR2_ADDR, 1));
        assertEquals(InterruptionModule_ATmega328P.INTERRUPT_VECTOR[POINTER_ADDR_TIMER2_COMP_A],
                Whitebox.getInternalState(interruptionModule,"pcInterruption"));
    }

    @Test
    public void timer2_CompB_maskDisabled(){
        dataMemory.writeBit(DataMemory_ATmega328P.TIMSK2_ADDR, 2, false);
        assertFalse(interruptionModule.haveInterruption());
    }

    @Test
    public void timer2_CompB_noFlag(){
        dataMemory.writeBit(DataMemory_ATmega328P.TIMSK2_ADDR, 2, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR2_ADDR, 2, false);

        assertFalse(interruptionModule.haveInterruption());
    }

    @Test
    public void timer2_CompB_interrupt(){
        dataMemory.writeBit(DataMemory_ATmega328P.TIMSK2_ADDR, 2, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR2_ADDR, 2, true);

        assertTrue(interruptionModule.haveInterruption());
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.TIFR2_ADDR, 2));
        assertEquals(InterruptionModule_ATmega328P.INTERRUPT_VECTOR[POINTER_ADDR_TIMER2_COMP_B],
                Whitebox.getInternalState(interruptionModule,"pcInterruption"));
    }

    @Test
    public void timer2_Overflow_maskDisabled(){
        dataMemory.writeBit(DataMemory_ATmega328P.TIMSK2_ADDR, 0, false);
        assertFalse(interruptionModule.haveInterruption());
    }

    @Test
    public void timer2_Overflow_noFlag(){
        dataMemory.writeBit(DataMemory_ATmega328P.TIMSK2_ADDR, 0, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR2_ADDR, 0, false);

        assertFalse(interruptionModule.haveInterruption());
    }

    @Test
    public void timer2_Overflow_interrupt(){
        dataMemory.writeBit(DataMemory_ATmega328P.TIMSK2_ADDR, 0, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR2_ADDR, 0, true);

        assertTrue(interruptionModule.haveInterruption());
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.TIFR2_ADDR, 0));
        assertEquals(InterruptionModule_ATmega328P.INTERRUPT_VECTOR[POINTER_ADDR_TIMER2_OVERFLOW],
                Whitebox.getInternalState(interruptionModule,"pcInterruption"));
    }

    @Test
    public void timer1_captureEvent_maskDisabled(){
        dataMemory.writeBit(DataMemory_ATmega328P.TIMSK1_ADDR, 5, false);
        assertFalse(interruptionModule.haveInterruption());
    }

    @Test
    public void timer1_captureEvent_noFlag(){
        dataMemory.writeBit(DataMemory_ATmega328P.TIMSK1_ADDR, 5, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR1_ADDR, 5, false);

        assertFalse(interruptionModule.haveInterruption());
    }

    @Test
    public void timer1_captureEvent_interrupt(){
        dataMemory.writeBit(DataMemory_ATmega328P.TIMSK1_ADDR, 5, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR1_ADDR, 5, true);

        assertTrue(interruptionModule.haveInterruption());
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.TIFR1_ADDR, 5));
        assertEquals(InterruptionModule_ATmega328P.INTERRUPT_VECTOR[POINTER_ADDR_TIMER1_CAPTURE_EVENT],
                Whitebox.getInternalState(interruptionModule,"pcInterruption"));
    }

    @Test
    public void timer1_CompA_maskDisabled(){
        dataMemory.writeBit(DataMemory_ATmega328P.TIMSK1_ADDR, 1, false);
        assertFalse(interruptionModule.haveInterruption());
    }

    @Test
    public void timer1_CompA_noFlag(){
        dataMemory.writeBit(DataMemory_ATmega328P.TIMSK1_ADDR, 1, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR1_ADDR, 1, false);

        assertFalse(interruptionModule.haveInterruption());
    }

    @Test
    public void timer1_CompA_interrupt(){
        dataMemory.writeBit(DataMemory_ATmega328P.TIMSK1_ADDR, 1, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR1_ADDR, 1, true);

        assertTrue(interruptionModule.haveInterruption());
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.TIFR1_ADDR, 1));
        assertEquals(InterruptionModule_ATmega328P.INTERRUPT_VECTOR[POINTER_ADDR_TIMER1_COMP_A],
                Whitebox.getInternalState(interruptionModule,"pcInterruption"));
    }

    @Test
    public void timer1_CompB_maskDisabled(){
        dataMemory.writeBit(DataMemory_ATmega328P.TIMSK1_ADDR, 2, false);
        assertFalse(interruptionModule.haveInterruption());
    }

    @Test
    public void timer1_CompB_noFlag(){
        dataMemory.writeBit(DataMemory_ATmega328P.TIMSK1_ADDR, 2, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR1_ADDR, 2, false);

        assertFalse(interruptionModule.haveInterruption());
    }

    @Test
    public void timer1_CompB_interrupt(){
        dataMemory.writeBit(DataMemory_ATmega328P.TIMSK1_ADDR, 2, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR1_ADDR, 2, true);

        assertTrue(interruptionModule.haveInterruption());
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.TIFR1_ADDR, 2));
        assertEquals(InterruptionModule_ATmega328P.INTERRUPT_VECTOR[POINTER_ADDR_TIMER1_COMP_B],
                Whitebox.getInternalState(interruptionModule,"pcInterruption"));
    }

    @Test
    public void timer1_Overflow_maskDisabled(){
        dataMemory.writeBit(DataMemory_ATmega328P.TIMSK1_ADDR, 0, false);
        assertFalse(interruptionModule.haveInterruption());
    }

    @Test
    public void timer1_Overflow_noFlag(){
        dataMemory.writeBit(DataMemory_ATmega328P.TIMSK1_ADDR, 0, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR1_ADDR, 0, false);

        assertFalse(interruptionModule.haveInterruption());
    }

    @Test
    public void timer1_Overflow_interrupt(){
        dataMemory.writeBit(DataMemory_ATmega328P.TIMSK1_ADDR, 0, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR1_ADDR, 0, true);

        assertTrue(interruptionModule.haveInterruption());
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.TIFR1_ADDR, 0));
        assertEquals(InterruptionModule_ATmega328P.INTERRUPT_VECTOR[POINTER_ADDR_TIMER1_OVERFLOW],
                Whitebox.getInternalState(interruptionModule,"pcInterruption"));
    }

    @Test
    public void timer0_CompA_maskDisabled(){
        dataMemory.writeBit(DataMemory_ATmega328P.TIMSK0_ADDR, 1, false);
        assertFalse(interruptionModule.haveInterruption());
    }

    @Test
    public void timer0_CompA_noFlag(){
        dataMemory.writeBit(DataMemory_ATmega328P.TIMSK0_ADDR, 1, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR0_ADDR, 1, false);

        assertFalse(interruptionModule.haveInterruption());
    }

    @Test
    public void timer0_CompA_interrupt(){
        dataMemory.writeBit(DataMemory_ATmega328P.TIMSK0_ADDR, 1, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR0_ADDR, 1, true);

        assertTrue(interruptionModule.haveInterruption());
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.TIFR0_ADDR, 1));
        assertEquals(InterruptionModule_ATmega328P.INTERRUPT_VECTOR[POINTER_ADDR_TIMER0_COMP_A],
                Whitebox.getInternalState(interruptionModule,"pcInterruption"));
    }

    @Test
    public void timer0_CompB_maskDisabled(){
        dataMemory.writeBit(DataMemory_ATmega328P.TIMSK0_ADDR, 2, false);
        assertFalse(interruptionModule.haveInterruption());
    }

    @Test
    public void timer0_CompB_noFlag(){
        dataMemory.writeBit(DataMemory_ATmega328P.TIMSK0_ADDR, 2, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR0_ADDR, 2, false);

        assertFalse(interruptionModule.haveInterruption());
    }

    @Test
    public void timer0_CompB_interrupt(){
        dataMemory.writeBit(DataMemory_ATmega328P.TIMSK0_ADDR, 2, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR0_ADDR, 2, true);

        assertTrue(interruptionModule.haveInterruption());
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.TIFR0_ADDR, 2));
        assertEquals(InterruptionModule_ATmega328P.INTERRUPT_VECTOR[POINTER_ADDR_TIMER0_COMP_B],
                Whitebox.getInternalState(interruptionModule,"pcInterruption"));
    }

    @Test
    public void timer0_Overflow_maskDisabled(){
        dataMemory.writeBit(DataMemory_ATmega328P.TIMSK0_ADDR, 0, false);
        assertFalse(interruptionModule.haveInterruption());
    }

    @Test
    public void timer0_Overflow_noFlag(){
        dataMemory.writeBit(DataMemory_ATmega328P.TIMSK0_ADDR, 0, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR0_ADDR, 0, false);

        assertFalse(interruptionModule.haveInterruption());
    }

    @Test
    public void timer0_Overflow_interrupt(){
        dataMemory.writeBit(DataMemory_ATmega328P.TIMSK0_ADDR, 0, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR0_ADDR, 0, true);

        assertTrue(interruptionModule.haveInterruption());
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.TIFR0_ADDR, 0));
        assertEquals(InterruptionModule_ATmega328P.INTERRUPT_VECTOR[POINTER_ADDR_TIMER0_OVERFLOW],
                Whitebox.getInternalState(interruptionModule,"pcInterruption"));
    }



    @Test
    public void usart_RxComplete_maskDisabled(){
        dataMemory.writeBit(DataMemory_ATmega328P.UCSR0B_ADDR, 7, false);
        assertFalse(interruptionModule.haveInterruption());
    }

    @Test
    public void usart_RxComplete_noFlag(){
        dataMemory.writeBit(DataMemory_ATmega328P.UCSR0B_ADDR, 7, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.UCSR0A_ADDR, 7, false);

        assertFalse(interruptionModule.haveInterruption());
    }

    @Test
    public void usart_RxComplete_interrupt(){
        dataMemory.writeBit(DataMemory_ATmega328P.UCSR0B_ADDR, 7, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.UCSR0A_ADDR, 7, true);

        assertTrue(interruptionModule.haveInterruption());
        assertEquals(InterruptionModule_ATmega328P.INTERRUPT_VECTOR[POINTER_ADDR_USART_RX_COMPLETE],
                Whitebox.getInternalState(interruptionModule,"pcInterruption"));
    }

    @Test
    public void usart_UDRE_Empty_maskDisabled(){
        dataMemory.writeBit(DataMemory_ATmega328P.UCSR0B_ADDR, 5, false);
        assertFalse(interruptionModule.haveInterruption());
    }

    @Test
    public void usart_UDRE_Empty_noFlag(){
        dataMemory.writeBit(DataMemory_ATmega328P.UCSR0B_ADDR, 5, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.UCSR0A_ADDR, 5, false);

        assertFalse(interruptionModule.haveInterruption());
    }

    @Test
    public void usart_UDRE_Empty_interrupt(){
        dataMemory.writeBit(DataMemory_ATmega328P.UCSR0B_ADDR, 5, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.UCSR0A_ADDR, 5, true);

        assertTrue(interruptionModule.haveInterruption());
        assertEquals(InterruptionModule_ATmega328P.INTERRUPT_VECTOR[POINTER_ADDR_USART_UDRE_EMPTY],
                Whitebox.getInternalState(interruptionModule,"pcInterruption"));
    }

    @Test
    public void usart_TxComplete_maskDisabled(){
        dataMemory.writeBit(DataMemory_ATmega328P.UCSR0B_ADDR, 6, false);
        assertFalse(interruptionModule.haveInterruption());
    }

    @Test
    public void usart_TxComplete_noFlag(){
        dataMemory.writeBit(DataMemory_ATmega328P.UCSR0B_ADDR, 6, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.UCSR0A_ADDR, 6, false);

        assertFalse(interruptionModule.haveInterruption());
    }

    @Test
    public void usart_TxComplete_interrupt(){
        dataMemory.writeBit(DataMemory_ATmega328P.UCSR0B_ADDR, 6, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.UCSR0A_ADDR, 6, true);

        assertTrue(interruptionModule.haveInterruption());
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.UCSR0A_ADDR, 6));
        assertEquals(InterruptionModule_ATmega328P.INTERRUPT_VECTOR[POINTER_ADDR_USART_TX_COMPLETE],
                Whitebox.getInternalState(interruptionModule,"pcInterruption"));
    }

    @Test
    public void adc_maskDisabled(){
        dataMemory.writeBit(DataMemory_ATmega328P.ADCSRA_ADDR, 3, false);
        assertFalse(interruptionModule.haveInterruption());
    }

    @Test
    public void adc_noFlag(){
        dataMemory.writeBit(DataMemory_ATmega328P.ADCSRA_ADDR, 3, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.ADCSRA_ADDR, 4, false);

        assertFalse(interruptionModule.haveInterruption());
    }

    @Test
    public void adc_interrupt(){
        dataMemory.writeBit(DataMemory_ATmega328P.ADCSRA_ADDR, 3, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.ADCSRA_ADDR, 4, true);

        assertTrue(interruptionModule.haveInterruption());
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.ADCSRA_ADDR, 4));
        assertEquals(InterruptionModule_ATmega328P.INTERRUPT_VECTOR[POINTER_ADDR_ADC],
                Whitebox.getInternalState(interruptionModule,"pcInterruption"));
    }

    @Test
    public void getPCInterruptionAddress(){
        char data = 0x22;
        Whitebox.setInternalState(interruptionModule,"pcInterruption", data);
        assertEquals(data, interruptionModule.getPCInterruptionAddress());
    }

    @Test
    public void disableGlobalInterruptions(){
        dataMemory.writeIOBit(DataMemory_ATmega328P.SREG_ADDR, 7, true);
        interruptionModule.disableGlobalInterruptions();
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 7));
    }

    @Test
    public void enableGlobalInterruptions() {
        dataMemory.writeIOBit(DataMemory_ATmega328P.SREG_ADDR, 7, false);
        interruptionModule.enableGlobalInterruptions();
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.SREG_ADDR, 7));
    }

    @Test
    public void timer0Overflow_setFlag_autoTriggerMaskDisable(){
        dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR0_ADDR, 0, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.ADCSRA_ADDR, 5, false);

        interruptionModule.timer0Overflow();
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.TIFR0_ADDR, 0));
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.ADCSRA_ADDR, 6));
    }

    @Test
    public void timer0Overflow_setFlag_autoTriggerNotSelected(){
        dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR0_ADDR, 0, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.ADCSRA_ADDR, 5, true);
        dataMemory.writeByte(DataMemory_ATmega328P.ADCSRB_ADDR, (byte) 0x00);

        interruptionModule.timer0Overflow();
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.TIFR0_ADDR, 0));
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.ADCSRA_ADDR, 6));
    }

    @Test
    public void timer0Overflow_setFlag_autoTriggerEnabled(){
        dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR0_ADDR, 0, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.ADCSRA_ADDR, 5, true);
        dataMemory.writeByte(DataMemory_ATmega328P.ADCSRB_ADDR, (byte) 0x04);

        interruptionModule.timer0Overflow();
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.TIFR0_ADDR, 0));
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.ADCSRA_ADDR, 6));
    }

    @Test
    public void timer0MatchA_setFlag_autoTriggerMaskDisable(){
        dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR0_ADDR, 1, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.ADCSRA_ADDR, 5, false);

        interruptionModule.timer0MatchA();
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.TIFR0_ADDR, 1));
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.ADCSRA_ADDR, 6));
    }

    @Test
    public void timer0MatchA_setFlag_autoTriggerNotSelected(){
        dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR0_ADDR, 1, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.ADCSRA_ADDR, 5, true);
        dataMemory.writeByte(DataMemory_ATmega328P.ADCSRB_ADDR, (byte) 0x00);

        interruptionModule.timer0MatchA();
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.TIFR0_ADDR, 1));
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.ADCSRA_ADDR, 6));
    }

    @Test
    public void timer0MatchA_setFlag_autoTriggerEnabled(){
        dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR0_ADDR, 1, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.ADCSRA_ADDR, 5, true);
        dataMemory.writeByte(DataMemory_ATmega328P.ADCSRB_ADDR, (byte) 0x03);

        interruptionModule.timer0MatchA();
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.TIFR0_ADDR, 1));
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.ADCSRA_ADDR, 6));
    }

    @Test
    public void timer0MatchB_setFlag(){
        dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR0_ADDR, 2, false);

        interruptionModule.timer0MatchB();
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.TIFR0_ADDR, 2));
    }


    @Test
    public void timer1Overflow_setFlag_autoTriggerMaskDisable(){
        dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR1_ADDR, 0, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.ADCSRA_ADDR, 5, false);

        interruptionModule.timer1Overflow();
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.TIFR1_ADDR, 0));
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.ADCSRA_ADDR, 6));
    }

    @Test
    public void timer1Overflow_setFlag_autoTriggerNotSelected(){
        dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR1_ADDR, 0, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.ADCSRA_ADDR, 5, true);
        dataMemory.writeByte(DataMemory_ATmega328P.ADCSRB_ADDR, (byte) 0x00);

        interruptionModule.timer1Overflow();
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.TIFR1_ADDR, 0));
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.ADCSRA_ADDR, 6));
    }

    @Test
    public void timer1Overflow_setFlag_autoTriggerEnabled(){
        dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR1_ADDR, 0, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.ADCSRA_ADDR, 5, true);
        dataMemory.writeByte(DataMemory_ATmega328P.ADCSRB_ADDR, (byte) 0x06);

        interruptionModule.timer1Overflow();
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.TIFR1_ADDR, 0));
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.ADCSRA_ADDR, 6));
    }

    @Test
    public void timer1MatchA_setFlag(){
        dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR1_ADDR, 1, false);

        interruptionModule.timer1MatchA();
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.TIFR1_ADDR, 1));
    }

    @Test
    public void timer1MatchB_setFlag_autoTriggerMaskDisable(){
        dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR1_ADDR, 2, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.ADCSRA_ADDR, 5, false);

        interruptionModule.timer1MatchB();
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.TIFR1_ADDR, 2));
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.ADCSRA_ADDR, 6));
    }

    @Test
    public void timer1MatchB_setFlag_autoTriggerNotSelected(){
        dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR1_ADDR, 2, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.ADCSRA_ADDR, 5, true);
        dataMemory.writeByte(DataMemory_ATmega328P.ADCSRB_ADDR, (byte) 0x00);

        interruptionModule.timer1MatchB();
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.TIFR1_ADDR, 2));
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.ADCSRA_ADDR, 6));
    }

    @Test
    public void timer1MatchB_setFlag_autoTriggerEnabled(){
        dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR1_ADDR, 2, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.ADCSRA_ADDR, 5, true);
        dataMemory.writeByte(DataMemory_ATmega328P.ADCSRB_ADDR, (byte) 0x05);

        interruptionModule.timer1MatchB();
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.TIFR1_ADDR, 2));
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.ADCSRA_ADDR, 6));
    }

    @Test
    public void timer1InputCapture_setFlag_autoTriggerMaskDisable(){
        dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR1_ADDR, 5, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.ADCSRA_ADDR, 5, false);

        interruptionModule.timer1InputCapture();
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.TIFR1_ADDR, 5));
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.ADCSRA_ADDR, 6));
    }

    @Test
    public void timer1InputCapture_setFlag_autoTriggerNotSelected(){
        dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR1_ADDR, 5, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.ADCSRA_ADDR, 5, true);
        dataMemory.writeByte(DataMemory_ATmega328P.ADCSRB_ADDR, (byte) 0x00);

        interruptionModule.timer1InputCapture();
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.TIFR1_ADDR, 5));
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.ADCSRA_ADDR, 6));
    }

    @Test
    public void timer1InputCapture_setFlag_autoTriggerEnabled(){
        dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR1_ADDR, 5, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.ADCSRA_ADDR, 5, true);
        dataMemory.writeByte(DataMemory_ATmega328P.ADCSRB_ADDR, (byte) 0x07);

        interruptionModule.timer1InputCapture();
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.TIFR1_ADDR, 5));
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.ADCSRA_ADDR, 6));
    }


    @Test
    public void timer2Overflow_setFlag(){
        dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR2_ADDR, 0, false);

        interruptionModule.timer2Overflow();
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.TIFR2_ADDR, 0));
    }

    @Test
    public void timer2MatchA_setFlag(){
        dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR2_ADDR, 1, false);

        interruptionModule.timer2MatchA();
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.TIFR2_ADDR, 1));
    }

    @Test
    public void timer2MatchB_setFlag(){
        dataMemory.writeIOBit(DataMemory_ATmega328P.TIFR2_ADDR, 2, false);

        interruptionModule.timer2MatchB();
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.TIFR2_ADDR, 2));
    }

    @Test
    public void conversionCompleteADC_setFlag(){
        dataMemory.writeIOBit(DataMemory_ATmega328P.ADCSRA_ADDR, 4, false);

        interruptionModule.conversionCompleteADC();
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.ADCSRA_ADDR, 4));
    }

    @Test
    public void dataRegisterEmptyUSART_setFlag(){
        dataMemory.writeIOBit(DataMemory_ATmega328P.UCSR0A_ADDR, 5, false);

        interruptionModule.dataRegisterEmptyUSART();
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.UCSR0A_ADDR, 5));
    }

    @Test
    public void transmissionCompleteUSART_setFlag(){
        dataMemory.writeIOBit(DataMemory_ATmega328P.UCSR0A_ADDR, 6, false);

        interruptionModule.transmissionCompleteUSART();
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.UCSR0A_ADDR, 6));
    }

    @Test
    public void receiveCompleteUSART_setFlag(){
        dataMemory.writeIOBit(DataMemory_ATmega328P.UCSR0A_ADDR, 7, false);

        interruptionModule.receiveCompleteUSART();
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.UCSR0A_ADDR, 7));
    }

    @Test
    public void receiveBufferReadedUSART_setFlag(){
        dataMemory.writeIOBit(DataMemory_ATmega328P.UCSR0A_ADDR, 7, true);

        interruptionModule.receiveBufferReadedUSART();
        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.UCSR0A_ADDR, 7));
    }

    @Test
    public void checkINT0LevelInterrupt_interruptTrue(){
        dataMemory.writeIOBit(DataMemory_ATmega328P.EIMSK_ADDR, 0, true);
        dataMemory.writeByte(DataMemory_ATmega328P.EICRA_ADDR, (byte) 0);
        dataMemory.writeIOBit(DataMemory_ATmega328P.EIFR_ADDR, 0, true);

//        dataMemory.writeIOBit(DataMemory_ATmega328P.ADCSRA_ADDR, 5, false);

        interruptionModule.checkIOInterruption(DataMemory_ATmega328P.PIND_ADDR, 2, true, false);

        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.EIFR_ADDR, 0));
//        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.ADCSRA_ADDR, 5));
    }

    @Test
    public void checkINT0LevelInterrupt_interruptFalse(){
        dataMemory.writeIOBit(DataMemory_ATmega328P.EIMSK_ADDR, 0, true);
        dataMemory.writeByte(DataMemory_ATmega328P.EICRA_ADDR, (byte) 0);
        dataMemory.writeIOBit(DataMemory_ATmega328P.EIFR_ADDR, 0, true);

        interruptionModule.checkIOInterruption(DataMemory_ATmega328P.PIND_ADDR, 2, false, true);

        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.EIFR_ADDR, 0));
    }

    @Test
    public void checkINT0ChangeInterrupt_interruptTrue(){
        dataMemory.writeIOBit(DataMemory_ATmega328P.EIMSK_ADDR, 0, true);
        dataMemory.writeByte(DataMemory_ATmega328P.EICRA_ADDR, (byte) 0x01);
        dataMemory.writeIOBit(DataMemory_ATmega328P.EIFR_ADDR, 0, false);

        interruptionModule.checkIOInterruption(DataMemory_ATmega328P.PIND_ADDR, 2, false, true);

        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.EIFR_ADDR, 0));
    }

    @Test
    public void checkINT0ChangeInterrupt_interruptFalse(){
        dataMemory.writeIOBit(DataMemory_ATmega328P.EIMSK_ADDR, 0, true);
        dataMemory.writeByte(DataMemory_ATmega328P.EICRA_ADDR, (byte) 0x01);
        dataMemory.writeIOBit(DataMemory_ATmega328P.EIFR_ADDR, 0, false);

        interruptionModule.checkIOInterruption(DataMemory_ATmega328P.PIND_ADDR, 2, false, false);

        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.EIFR_ADDR, 0));
    }

    @Test
    public void checkINT0FallingEdge_interruptTrue(){
        dataMemory.writeIOBit(DataMemory_ATmega328P.EIMSK_ADDR, 0, true);
        dataMemory.writeByte(DataMemory_ATmega328P.EICRA_ADDR, (byte) 0x02);
        dataMemory.writeIOBit(DataMemory_ATmega328P.EIFR_ADDR, 0, false);

        interruptionModule.checkIOInterruption(DataMemory_ATmega328P.PIND_ADDR, 2, true, false);

        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.EIFR_ADDR, 0));
    }

    @Test
    public void checkINT0FallingEdge_interruptFalse(){
        dataMemory.writeIOBit(DataMemory_ATmega328P.EIMSK_ADDR, 0, true);
        dataMemory.writeByte(DataMemory_ATmega328P.EICRA_ADDR, (byte) 0x02);
        dataMemory.writeIOBit(DataMemory_ATmega328P.EIFR_ADDR, 0, false);

        interruptionModule.checkIOInterruption(DataMemory_ATmega328P.PIND_ADDR, 2, true, true);

        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.EIFR_ADDR, 0));
    }

    @Test
    public void checkINT0RisingEdge_interruptTrue(){
        dataMemory.writeIOBit(DataMemory_ATmega328P.EIMSK_ADDR, 0, true);
        dataMemory.writeByte(DataMemory_ATmega328P.EICRA_ADDR, (byte) 0x03);
        dataMemory.writeIOBit(DataMemory_ATmega328P.EIFR_ADDR, 0, false);

        interruptionModule.checkIOInterruption(DataMemory_ATmega328P.PIND_ADDR, 2, false, true);

        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.EIFR_ADDR, 0));
    }

    @Test
    public void checkINT0RisingEdge_interruptFalse(){
        dataMemory.writeIOBit(DataMemory_ATmega328P.EIMSK_ADDR, 0, true);
        dataMemory.writeByte(DataMemory_ATmega328P.EICRA_ADDR, (byte) 0x03);
        dataMemory.writeIOBit(DataMemory_ATmega328P.EIFR_ADDR, 0, false);

        interruptionModule.checkIOInterruption(DataMemory_ATmega328P.PIND_ADDR, 2, false, false);

        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.EIFR_ADDR, 0));
    }

    @Test
    public void checkINT1LevelInterrupt_interruptTrue(){
        dataMemory.writeIOBit(DataMemory_ATmega328P.EIMSK_ADDR, 1, true);
        dataMemory.writeByte(DataMemory_ATmega328P.EICRA_ADDR, (byte) 0);
        dataMemory.writeIOBit(DataMemory_ATmega328P.EIFR_ADDR, 1, true);

        interruptionModule.checkIOInterruption(DataMemory_ATmega328P.PIND_ADDR, 3, true, false);

        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.EIFR_ADDR, 1));
    }

    @Test
    public void checkINT1LevelInterrupt_interruptFalse(){
        dataMemory.writeIOBit(DataMemory_ATmega328P.EIMSK_ADDR, 1, true);
        dataMemory.writeByte(DataMemory_ATmega328P.EICRA_ADDR, (byte) 0);
        dataMemory.writeIOBit(DataMemory_ATmega328P.EIFR_ADDR, 1, true);

        interruptionModule.checkIOInterruption(DataMemory_ATmega328P.PIND_ADDR, 3, true, true);

        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.EIFR_ADDR, 1));
    }

    @Test
    public void checkINT1ChangeInterrupt_interruptTrue(){
        dataMemory.writeIOBit(DataMemory_ATmega328P.EIMSK_ADDR, 1, true);
        dataMemory.writeByte(DataMemory_ATmega328P.EICRA_ADDR, (byte) 0x04);
        dataMemory.writeIOBit(DataMemory_ATmega328P.EIFR_ADDR, 1, false);

        interruptionModule.checkIOInterruption(DataMemory_ATmega328P.PIND_ADDR, 3, false, true);

        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.EIFR_ADDR, 1));
    }

    @Test
    public void checkINT1ChangeInterrupt_interruptFalse(){
        dataMemory.writeIOBit(DataMemory_ATmega328P.EIMSK_ADDR, 1, true);
        dataMemory.writeByte(DataMemory_ATmega328P.EICRA_ADDR, (byte) 0x04);
        dataMemory.writeIOBit(DataMemory_ATmega328P.EIFR_ADDR, 1, false);

        interruptionModule.checkIOInterruption(DataMemory_ATmega328P.PIND_ADDR, 3, false, false);

        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.EIFR_ADDR, 1));
    }

    @Test
    public void checkINT1FallingEdge_interruptTrue(){
        dataMemory.writeIOBit(DataMemory_ATmega328P.EIMSK_ADDR, 1, true);
        dataMemory.writeByte(DataMemory_ATmega328P.EICRA_ADDR, (byte) 0x08);
        dataMemory.writeIOBit(DataMemory_ATmega328P.EIFR_ADDR, 1, false);

        interruptionModule.checkIOInterruption(DataMemory_ATmega328P.PIND_ADDR, 3, true, false);

        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.EIFR_ADDR, 1));
    }

    @Test
    public void checkINT1FallingEdge_interruptFalse(){
        dataMemory.writeIOBit(DataMemory_ATmega328P.EIMSK_ADDR, 1, true);
        dataMemory.writeByte(DataMemory_ATmega328P.EICRA_ADDR, (byte) 0x08);
        dataMemory.writeIOBit(DataMemory_ATmega328P.EIFR_ADDR, 1, false);

        interruptionModule.checkIOInterruption(DataMemory_ATmega328P.PIND_ADDR, 3, true, true);

        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.EIFR_ADDR, 1));
    }

    @Test
    public void checkINT1RisingEdge_interruptTrue(){
        dataMemory.writeIOBit(DataMemory_ATmega328P.EIMSK_ADDR, 1, true);
        dataMemory.writeByte(DataMemory_ATmega328P.EICRA_ADDR, (byte) 0x0C);
        dataMemory.writeIOBit(DataMemory_ATmega328P.EIFR_ADDR, 1, false);

        interruptionModule.checkIOInterruption(DataMemory_ATmega328P.PIND_ADDR, 3, false, true);

        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.EIFR_ADDR, 1));
    }

    @Test
    public void checkINT1RisingEdge_interruptFalse(){
        dataMemory.writeIOBit(DataMemory_ATmega328P.EIMSK_ADDR, 1, true);
        dataMemory.writeByte(DataMemory_ATmega328P.EICRA_ADDR, (byte) 0x0C);
        dataMemory.writeIOBit(DataMemory_ATmega328P.EIFR_ADDR, 1, false);

        interruptionModule.checkIOInterruption(DataMemory_ATmega328P.PIND_ADDR, 3, false, false);

        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.EIFR_ADDR, 1));
    }

    @Test
    public void checkPCINT0_PinPosition4_interruptTrue(){
        int pinPosition = 4;
        dataMemory.writeIOBit(DataMemory_ATmega328P.PCICR_ADDR, 0, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.PCMSK0_ADDR, pinPosition, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.PCIFR_ADDR, 0, false);

        interruptionModule.checkIOInterruption(DataMemory_ATmega328P.PINB_ADDR, pinPosition, false, true);

        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.PCIFR_ADDR, 0));
    }

    @Test
    public void checkPCINT0_PinPosition5_interruptFalse(){
        int pinPosition = 5;
        dataMemory.writeIOBit(DataMemory_ATmega328P.PCICR_ADDR, 0, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.PCMSK0_ADDR, pinPosition, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.PCIFR_ADDR, 0, false);

        interruptionModule.checkIOInterruption(DataMemory_ATmega328P.PINB_ADDR, pinPosition, false, false);

        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.PCIFR_ADDR, 0));
    }

    @Test
    public void checkPCINT1_PinPosition0_interruptTrue(){
        int pinPosition = 0;
        dataMemory.writeIOBit(DataMemory_ATmega328P.PCICR_ADDR, 1, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.PCMSK1_ADDR, pinPosition, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.PCIFR_ADDR, 1, false);

        interruptionModule.checkIOInterruption(DataMemory_ATmega328P.PINC_ADDR, pinPosition, true, false);

        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.PCIFR_ADDR, 1));
    }

    @Test
    public void checkPCINT1_PinPosition1_interruptFalse(){
        int pinPosition = 1;
        dataMemory.writeIOBit(DataMemory_ATmega328P.PCICR_ADDR, 1, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.PCMSK1_ADDR, pinPosition, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.PCIFR_ADDR, 1, false);

        interruptionModule.checkIOInterruption(DataMemory_ATmega328P.PINC_ADDR, pinPosition, true, true);

        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.PCIFR_ADDR, 1));
    }

    @Test
    public void checkPCINT2_PinPosition2_interruptTrue(){
        int pinPosition = 2;
        dataMemory.writeIOBit(DataMemory_ATmega328P.EIMSK_ADDR, 0, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.PCICR_ADDR, 2, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.PCMSK2_ADDR, pinPosition, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.PCIFR_ADDR, 2, false);

        interruptionModule.checkIOInterruption(DataMemory_ATmega328P.PIND_ADDR, pinPosition, false, true);

        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.PCIFR_ADDR, 2));
    }

    @Test
    public void checkPCINT2_PinPosition3_interruptFalse(){
        int pinPosition = 3;
        dataMemory.writeIOBit(DataMemory_ATmega328P.EIMSK_ADDR, 1, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.PCICR_ADDR, 2, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.PCMSK2_ADDR, pinPosition, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.PCIFR_ADDR, 2, false);

        interruptionModule.checkIOInterruption(DataMemory_ATmega328P.PIND_ADDR, pinPosition, true, true);

        assertFalse(dataMemory.readBit(DataMemory_ATmega328P.PCIFR_ADDR, 2));
    }

    @Test
    public void checkPCINT2_PinPosition6_interruptTrue(){
        int pinPosition = 6;
        dataMemory.writeIOBit(DataMemory_ATmega328P.PCICR_ADDR, 2, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.PCMSK2_ADDR, pinPosition, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.PCIFR_ADDR, 2, false);

        interruptionModule.checkIOInterruption(DataMemory_ATmega328P.PIND_ADDR, pinPosition, true, false);

        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.PCIFR_ADDR, 2));
    }
}