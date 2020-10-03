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

import android.content.res.Resources;

import com.kollins.project.sofia.UCModule;
import com.kollins.project.sofia.atmega328p.iomodule_atmega328p.IOModule_ATmega328P;
import com.kollins.project.sofia.ucinterfaces.IOModule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.mock;


@RunWith(PowerMockRunner.class)
@PrepareForTest({ADC_ATmega328P.class, DataMemory_ATmega328P.class, UCModule.class, InterruptionModule_ATmega328P.class})
public class ADCTest {

    private ADC_ATmega328P adc;
    private DataMemory_ATmega328P dataMemory;

    private static IOModule ioModule;

    @BeforeEach
    public void prepareForTest() throws Exception {
        ioModule = mock(IOModule_ATmega328P.class);

        dataMemory = new DataMemory_ATmega328P(ioModule);
        adc = new ADC_ATmega328P(dataMemory);

        UCModule.interruptionModule = new InterruptionModule_ATmega328P();
        UCModule.interruptionModule.setMemory(dataMemory);
        UCModule.resources = mock(Resources.class);

        dataMemory.writeIOBit(DataMemory_ATmega328P.ADCSRA_ADDR, 7, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.ADCSRA_ADDR, 6, true);

        PowerMockito.doReturn(5).when(UCModule.resources, "getInteger", Matchers.anyInt());
        ADC_ATmega328P.AREF = 5000;
    }

    @Test
    public void conversion_ref_AVCC_ADC0_leftAdjust_notFreeRun(){
        ADC_ATmega328P.adcInput[0] = 3000;  //614 converted

        dataMemory.writeByte(DataMemory_ATmega328P.ADMUX_ADDR, (byte) 0x60);
        dataMemory.writeIOBit(DataMemory_ATmega328P.ADCSRA_ADDR, 5, false);

        adc.run();

        assertEquals((byte) 0x99, dataMemory.readByte(DataMemory_ATmega328P.ADCH_ADDR));
        assertEquals((byte) 0x80, dataMemory.readByte(DataMemory_ATmega328P.ADCL_ADDR));
    }

    @Test
    public void conversion_ref_AVCC_ADC0_rightAdjust_notFreeRun(){
        ADC_ATmega328P.adcInput[0] = 3000;  //614 converted

        dataMemory.writeByte(DataMemory_ATmega328P.ADMUX_ADDR, (byte) 0x40);
        dataMemory.writeIOBit(DataMemory_ATmega328P.ADCSRA_ADDR, 5, false);

        adc.run();

        assertEquals((byte) 0x02, dataMemory.readByte(DataMemory_ATmega328P.ADCH_ADDR));
        assertEquals((byte) 0x66, dataMemory.readByte(DataMemory_ATmega328P.ADCL_ADDR));
    }

    @org.junit.jupiter.api.Test
    public void conversion_ref_AVCC_ADC1_rightAdjust_notFreeRun(){
        ADC_ATmega328P.adcInput[1] = 3000;  //614 converted

        dataMemory.writeByte(DataMemory_ATmega328P.ADMUX_ADDR, (byte) 0x41);
        dataMemory.writeIOBit(DataMemory_ATmega328P.ADCSRA_ADDR, 5, false);

        adc.run();

        assertEquals((byte) 0x02, dataMemory.readByte(DataMemory_ATmega328P.ADCH_ADDR));
        assertEquals((byte) 0x66, dataMemory.readByte(DataMemory_ATmega328P.ADCL_ADDR));
    }

    @Test
    public void conversion_ref_AVCC_ADC2_rightAdjust_notFreeRun(){
        ADC_ATmega328P.adcInput[2] = 3000;  //614 converted

        dataMemory.writeByte(DataMemory_ATmega328P.ADMUX_ADDR, (byte) 0x42);
        dataMemory.writeIOBit(DataMemory_ATmega328P.ADCSRA_ADDR, 5, false);

        adc.run();

        assertEquals((byte) 0x02, dataMemory.readByte(DataMemory_ATmega328P.ADCH_ADDR));
        assertEquals((byte) 0x66, dataMemory.readByte(DataMemory_ATmega328P.ADCL_ADDR));
    }

    @org.junit.jupiter.api.Test
    public void conversion_ref_AVCC_ADC3_rightAdjust_notFreeRun(){
        ADC_ATmega328P.adcInput[3] = 3000;  //614 converted

        dataMemory.writeByte(DataMemory_ATmega328P.ADMUX_ADDR, (byte) 0x43);
        dataMemory.writeIOBit(DataMemory_ATmega328P.ADCSRA_ADDR, 5, false);

        adc.run();

        assertEquals((byte) 0x02, dataMemory.readByte(DataMemory_ATmega328P.ADCH_ADDR));
        assertEquals((byte) 0x66, dataMemory.readByte(DataMemory_ATmega328P.ADCL_ADDR));
    }

    @org.junit.jupiter.api.Test
    public void conversion_ref_AVCC_ADC4_rightAdjust_notFreeRun(){
        ADC_ATmega328P.adcInput[4] = 3000;  //614 converted

        dataMemory.writeByte(DataMemory_ATmega328P.ADMUX_ADDR, (byte) 0x44);
        dataMemory.writeIOBit(DataMemory_ATmega328P.ADCSRA_ADDR, 5, false);

        adc.run();

        assertEquals((byte) 0x02, dataMemory.readByte(DataMemory_ATmega328P.ADCH_ADDR));
        assertEquals((byte) 0x66, dataMemory.readByte(DataMemory_ATmega328P.ADCL_ADDR));
    }

    @Test
    public void conversion_ref_AVCC_ADC5_rightAdjust_notFreeRun(){
        ADC_ATmega328P.adcInput[5] = 3000;  //614 converted

        dataMemory.writeByte(DataMemory_ATmega328P.ADMUX_ADDR, (byte) 0x45);
        dataMemory.writeIOBit(DataMemory_ATmega328P.ADCSRA_ADDR, 5, false);

        adc.run();

        assertEquals((byte) 0x02, dataMemory.readByte(DataMemory_ATmega328P.ADCH_ADDR));
        assertEquals((byte) 0x66, dataMemory.readByte(DataMemory_ATmega328P.ADCL_ADDR));
    }

    @org.junit.jupiter.api.Test
    public void conversion_ref_AVCC_ADC6_rightAdjust_notFreeRun(){
        ADC_ATmega328P.adcInput[6] = 3000;  //614 converted

        dataMemory.writeByte(DataMemory_ATmega328P.ADMUX_ADDR, (byte) 0x46);
        dataMemory.writeIOBit(DataMemory_ATmega328P.ADCSRA_ADDR, 5, false);

        adc.run();

        assertEquals((byte) 0x02, dataMemory.readByte(DataMemory_ATmega328P.ADCH_ADDR));
        assertEquals((byte) 0x66, dataMemory.readByte(DataMemory_ATmega328P.ADCL_ADDR));
    }

    @org.junit.jupiter.api.Test
    public void conversion_ref_AVCC_ADC7_rightAdjust_notFreeRun(){
        ADC_ATmega328P.adcInput[7] = 3000;  //614 converted

        dataMemory.writeByte(DataMemory_ATmega328P.ADMUX_ADDR, (byte) 0x47);
        dataMemory.writeIOBit(DataMemory_ATmega328P.ADCSRA_ADDR, 5, false);

        adc.run();

        assertEquals((byte) 0x02, dataMemory.readByte(DataMemory_ATmega328P.ADCH_ADDR));
        assertEquals((byte) 0x66, dataMemory.readByte(DataMemory_ATmega328P.ADCL_ADDR));
    }

    @Test
    public void conversion_ref_AVCC_BANDGAP_rightAdjust_notFreeRun(){

        dataMemory.writeByte(DataMemory_ATmega328P.ADMUX_ADDR, (byte) 0x4E);
        dataMemory.writeIOBit(DataMemory_ATmega328P.ADCSRA_ADDR, 5, false);

        adc.run();

        assertEquals((byte) 0x00, dataMemory.readByte(DataMemory_ATmega328P.ADCH_ADDR));
        assertEquals((byte) 0xE1, dataMemory.readByte(DataMemory_ATmega328P.ADCL_ADDR));
    }

    @Test
    public void conversion_ref_BANDGAP_ADC0_rightAdjust_notFreeRun(){
        ADC_ATmega328P.adcInput[0] = 500;  //465 converted

        dataMemory.writeByte(DataMemory_ATmega328P.ADMUX_ADDR, (byte) 0xC0);
        dataMemory.writeIOBit(DataMemory_ATmega328P.ADCSRA_ADDR, 5, false);

        adc.run();

        assertEquals((byte) 0x01, dataMemory.readByte(DataMemory_ATmega328P.ADCH_ADDR));
        assertEquals((byte) 0xD1, dataMemory.readByte(DataMemory_ATmega328P.ADCL_ADDR));
    }

    @Test
    public void conversion_ref_AREF_3V_ADC0_rightAdjust_notFreeRun(){
        ADC_ATmega328P.adcInput[0] = 1000;  //341 converted
        ADC_ATmega328P.AREF = 3000;

        dataMemory.writeByte(DataMemory_ATmega328P.ADMUX_ADDR, (byte) 0x00);
        dataMemory.writeIOBit(DataMemory_ATmega328P.ADCSRA_ADDR, 5, false);

        adc.run();

        assertEquals((byte) 0x01, dataMemory.readByte(DataMemory_ATmega328P.ADCH_ADDR));
        assertEquals((byte) 0x55, dataMemory.readByte(DataMemory_ATmega328P.ADCL_ADDR));
    }

    @Test
    public void conversion_ref_AVCC_ADC0_rightAdjust_notFreeRun_overflow(){
        ADC_ATmega328P.adcInput[0] = 6000;  //614 converted

        dataMemory.writeByte(DataMemory_ATmega328P.ADMUX_ADDR, (byte) 0x40);
        dataMemory.writeIOBit(DataMemory_ATmega328P.ADCSRA_ADDR, 5, false);

        adc.run();

        assertEquals((byte) 0x03, dataMemory.readByte(DataMemory_ATmega328P.ADCH_ADDR));
        assertEquals((byte) 0xFF, dataMemory.readByte(DataMemory_ATmega328P.ADCL_ADDR));
    }

    @Test
    public void nonFreeRunConfiguration(){
        Whitebox.setInternalState(adc, "freeRunConversionEnable", true);
        Whitebox.setInternalState(adc, "isFreeRun", true);

        dataMemory.writeByte(DataMemory_ATmega328P.ADMUX_ADDR, (byte) 0x40);
        dataMemory.writeIOBit(DataMemory_ATmega328P.ADCSRA_ADDR, 5, false);

        adc.run();

        assertFalse((Boolean) Whitebox.getInternalState(adc, "freeRunConversionEnable"));
        assertFalse((Boolean) Whitebox.getInternalState(adc, "isFreeRun"));
    }

    @Test
    public void nonFreeRun_conversionEnable_true(){
        Whitebox.setInternalState(adc, "freeRunConversionEnable", true);
        Whitebox.setInternalState(adc, "isFreeRun", false);

        dataMemory.writeByte(DataMemory_ATmega328P.ADMUX_ADDR, (byte) 0x40);
        dataMemory.writeIOBit(DataMemory_ATmega328P.ADCSRA_ADDR, 5, true);
        dataMemory.writeByte(DataMemory_ATmega328P.ADCSRB_ADDR, (byte) 0x00);

        adc.run();

        assertTrue((Boolean) Whitebox.getInternalState(adc, "isFreeRun"));
        assertFalse((Boolean) Whitebox.getInternalState(adc, "freeRunConversionEnable"));
    }

    @Test
    public void nonFreeRun_conversionEnable_false_interruptionFlagSet(){
        Whitebox.setInternalState(adc, "freeRunConversionEnable", false);
        Whitebox.setInternalState(adc, "isFreeRun", false);

        dataMemory.writeByte(DataMemory_ATmega328P.ADMUX_ADDR, (byte) 0x40);
        dataMemory.writeIOBit(DataMemory_ATmega328P.ADCSRA_ADDR, 5, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.ADCSRA_ADDR, 4, true);
        dataMemory.writeByte(DataMemory_ATmega328P.ADCSRB_ADDR, (byte) 0x00);

        adc.run();

        assertTrue((Boolean) Whitebox.getInternalState(adc, "isFreeRun"));
        assertFalse((Boolean) Whitebox.getInternalState(adc, "freeRunConversionEnable"));
    }

    @Test
    public void nonFreeRun_conversionEnable_false_interruptionFlagClear(){
        Whitebox.setInternalState(adc, "freeRunConversionEnable", false);
        Whitebox.setInternalState(adc, "isFreeRun", false);

        dataMemory.writeByte(DataMemory_ATmega328P.ADMUX_ADDR, (byte) 0x40);
        dataMemory.writeIOBit(DataMemory_ATmega328P.ADCSRA_ADDR, 5, true);
        dataMemory.writeIOBit(DataMemory_ATmega328P.ADCSRA_ADDR, 4, false);
        dataMemory.writeByte(DataMemory_ATmega328P.ADCSRB_ADDR, (byte) 0x00);

        adc.run();

        assertTrue((Boolean) Whitebox.getInternalState(adc, "isFreeRun"));
        assertFalse((Boolean) Whitebox.getInternalState(adc, "freeRunConversionEnable"));
    }
}
