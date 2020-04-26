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

import com.kollins.project.sofia.UCModule;
import com.kollins.project.sofia.serial_monitor.SerialFragment;
import com.kollins.project.sofia.ucinterfaces.DataMemory;
import com.kollins.project.sofia.ucinterfaces.USARTModule;

public class USART_ATmega328P implements USARTModule {

    public static final String USART_LOG_TAG = "USART";

    public static byte transmitter_UDR0, receiver_UDR0;

    public static boolean usartOutputControl_Rx, usartOutputControl_Tx;

    private DataMemory_ATmega328P dataMemory;

    public USART_ATmega328P(DataMemory dataMemory) {
        this.dataMemory = (DataMemory_ATmega328P) dataMemory;
        usartOutputControl_Rx = false;
        usartOutputControl_Tx = false;
    }

    @Override
    public void run() {

        //Power Reduction Register
        if (dataMemory.readBit(DataMemory_ATmega328P.PRR_ADDR, 1)){
            return;
        }

        //Check if there is data to be transmitted
        if (dataMemory.readBit(DataMemory_ATmega328P.UCSR0B_ADDR, 3)) {

            usartOutputControl_Tx = true;

            if (!dataMemory.readBit(DataMemory_ATmega328P.UCSR0A_ADDR, 6)
                    && !dataMemory.readBit(DataMemory_ATmega328P.UCSR0A_ADDR, 5)) {

//                Log.d(USART_LOG_TAG, "Transmitting: " + ((char) transmitter_UDR0));
                SerialFragment.appendByte(transmitter_UDR0);
                UCModule.interruptionModule.dataRegisterEmptyUSART();
                UCModule.interruptionModule.transmissionCompleteUSART();
            }
        } else {
            usartOutputControl_Tx = false;
        }

        //Check if there is data to be received
        if (dataMemory.readBit(DataMemory_ATmega328P.UCSR0B_ADDR, 4)) {

            usartOutputControl_Rx = true;

            if (!dataMemory.readBit(DataMemory_ATmega328P.UCSR0A_ADDR, 7)
                    && SerialFragment.buffer.length() > 0) {

                receiver_UDR0 = SerialFragment.buffer.substring(0, 1).getBytes()[0];
                SerialFragment.buffer = SerialFragment.buffer.substring(1);
//                Log.d(USART_LOG_TAG, "Received: " + receiver_UDR0);
//                Log.d(USART_LOG_TAG, "BufferLeft " + SerialFragment.buffer);
                UCModule.interruptionModule.receiveCompleteUSART();
            }
        } else {
            usartOutputControl_Rx = false;
        }
    }
}
