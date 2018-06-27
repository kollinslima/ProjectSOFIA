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
import com.example.kollins.sofia.serial_monitor.SerialFragment;
import com.example.kollins.sofia.ucinterfaces.IOModule;

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
import static org.junit.Assert.assertTrue;


@RunWith(PowerMockRunner.class)
@PrepareForTest({USART_ATmega328P.class, DataMemory_ATmega328P.class})
public class USARTTest {

    private USART_ATmega328P usart;
    private DataMemory_ATmega328P dataMemory;

    @Mock
    private static IOModule ioModule;

    @Before
    public void prepareForTest() throws Exception {
        dataMemory = new DataMemory_ATmega328P(ioModule);
        usart = new USART_ATmega328P(dataMemory);
    }

    @Test
    public void noActivity(){
        dataMemory.writeIOBit(DataMemory_ATmega328P.UCSR0B_ADDR, 3, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.UCSR0B_ADDR, 4, false);

        USART_ATmega328P.usartOutputControl_Tx = true;
        USART_ATmega328P.usartOutputControl_Rx = true;

        usart.run();

        assertFalse(USART_ATmega328P.usartOutputControl_Tx);
        assertFalse(USART_ATmega328P.usartOutputControl_Rx);
    }

    @Test
    public void transmiterEnabled_NoDataToTransmit(){
        dataMemory.writeIOBit(DataMemory_ATmega328P.UCSR0B_ADDR, 3, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.UCSR0A_ADDR, 6, true);

        USART_ATmega328P.usartOutputControl_Tx = false;

        usart.run();

        assertTrue(USART_ATmega328P.usartOutputControl_Tx);
    }

    @Test
    public void transmiterEnabled_TransmissionComplete(){
        dataMemory.writeIOBit(DataMemory_ATmega328P.UCSR0B_ADDR, 3, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.UCSR0A_ADDR, 5, true);

        USART_ATmega328P.usartOutputControl_Tx = false;

        usart.run();

        assertTrue(USART_ATmega328P.usartOutputControl_Tx);
    }

    @Test
    public void transmiterEnabled_TransmitData(){
        dataMemory.writeIOBit(DataMemory_ATmega328P.UCSR0B_ADDR, 3, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.UCSR0A_ADDR, 6, false);
        dataMemory.writeIOBit(DataMemory_ATmega328P.UCSR0A_ADDR, 5, false);

        USART_ATmega328P.usartOutputControl_Tx = false;
        UCModule.interruptionModule = new InterruptionModule_ATmega328P();
        UCModule.interruptionModule.setMemory(dataMemory);

        usart.run();

        assertTrue(USART_ATmega328P.usartOutputControl_Tx);
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.UCSR0A_ADDR, 5));
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.UCSR0A_ADDR, 6));
    }



    @Test
    public void receiverEnabled_ReceiveComplete(){
        dataMemory.writeIOBit(DataMemory_ATmega328P.UCSR0B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.UCSR0A_ADDR, 7, true);

        USART_ATmega328P.usartOutputControl_Rx = false;

        usart.run();

        assertTrue(USART_ATmega328P.usartOutputControl_Rx);
    }

    @Test
    public void receiverEnabled_EmptyBuffer(){
        dataMemory.writeIOBit(DataMemory_ATmega328P.UCSR0B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.UCSR0A_ADDR, 7, false);
        SerialFragment.buffer = new String();

        USART_ATmega328P.usartOutputControl_Rx = false;

        usart.run();

        assertTrue(USART_ATmega328P.usartOutputControl_Rx);
    }

    @Test
    public void receiverEnabled_ReceiveData(){
        dataMemory.writeIOBit(DataMemory_ATmega328P.UCSR0B_ADDR, 4, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.UCSR0A_ADDR, 7, false);
        SerialFragment.buffer = new String("Teste");

        UCModule.interruptionModule = new InterruptionModule_ATmega328P();
        UCModule.interruptionModule.setMemory(dataMemory);

        USART_ATmega328P.usartOutputControl_Rx = false;

        usart.run();

        assertTrue(USART_ATmega328P.usartOutputControl_Rx);
        assertTrue(dataMemory.readBit(DataMemory_ATmega328P.UCSR0A_ADDR, 7));
        assertEquals("Teste".substring(0, 1).getBytes()[0], USART_ATmega328P.receiver_UDR0);
    }

}