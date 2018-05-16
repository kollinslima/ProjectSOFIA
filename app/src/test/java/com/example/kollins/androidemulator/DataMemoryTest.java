package com.example.kollins.androidemulator;

import android.os.Handler;

import com.example.kollins.androidemulator.ATmega328P.DataMemory_ATmega328P;
import com.example.kollins.androidemulator.ATmega328P.ProgramMemory_ATmega328P;
import com.example.kollins.androidemulator.ATmega328P.Timer1_ATmega328P;
import com.example.kollins.androidemulator.uCInterfaces.DataMemory;
import com.example.kollins.androidemulator.uCInterfaces.IOModule;
import com.example.kollins.androidemulator.uCInterfaces.ProgramMemory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.lang.reflect.Field;
import java.util.concurrent.locks.Lock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.booleanThat;
import static org.mockito.Mockito.mock;


@RunWith(PowerMockRunner.class)
@PrepareForTest({DataMemory.class})
public class DataMemoryTest {

    private DataMemory dataMemory;
    private byte[] sdramMemoryTest;

    @Mock
    private static IOModule ioModule;

    @Before
    public void prepareForTest() throws Exception {
        dataMemory = new DataMemory_ATmega328P(ioModule);
        sdramMemoryTest = Whitebox.getInternalState(dataMemory,"sdramMemory");
    }

    @Test
    public void writeByte_Normal(){
        int byteAddress = 0x700;
        byte data = 0x0B;

        dataMemory.writeByte(byteAddress, data);

        assertEquals(data, sdramMemoryTest[byteAddress]);
    }

    @Test
    public void writeByte_PINB_TogglePORTB(){
        int byteAddress = DataMemory_ATmega328P.PINB_ADDR;
        byte data = (byte) 0xFF;

        sdramMemoryTest[DataMemory_ATmega328P.PORTB_ADDR] = 0x55;

        dataMemory.writeByte(byteAddress, data);

        assertEquals((byte) 0xAA, sdramMemoryTest[DataMemory_ATmega328P.PORTB_ADDR]);
    }

    @Test
    public void writeByte_PINC_TogglePORTC(){
        int byteAddress = DataMemory_ATmega328P.PINC_ADDR;
        byte data = (byte) 0xFF;

        sdramMemoryTest[DataMemory_ATmega328P.PORTC_ADDR] = 0x55;

        dataMemory.writeByte(byteAddress, data);

        assertEquals((byte) 0xAA, sdramMemoryTest[DataMemory_ATmega328P.PORTC_ADDR]);
    }

    @Test
    public void writeByte_PIND_TogglePORTD(){
        int byteAddress = DataMemory_ATmega328P.PIND_ADDR;
        byte data = (byte) 0xFF;

        sdramMemoryTest[DataMemory_ATmega328P.PORTD_ADDR] = 0x55;

        dataMemory.writeByte(byteAddress, data);

        assertEquals((byte) 0xAA, sdramMemoryTest[DataMemory_ATmega328P.PORTD_ADDR]);
    }

    @Test
    public void writeByte_clearFlag_EIFR(){
        int byteAddress = DataMemory_ATmega328P.EIFR_ADDR;
        byte data = 0x03;

        sdramMemoryTest[byteAddress] = 0x03;
        dataMemory.writeByte(byteAddress, data);


        assertEquals((byte) 0x00, sdramMemoryTest[byteAddress]);
    }

    @Test
    public void writeByte_clearFlag_PCIFR(){
        int byteAddress = DataMemory_ATmega328P.PCIFR_ADDR;
        byte data = 0x07;

        sdramMemoryTest[byteAddress] = 0x07;
        dataMemory.writeByte(byteAddress, data);


        assertEquals((byte) 0x00, sdramMemoryTest[byteAddress]);
    }

    @Test
    public void writeByte_clearFlag_TIFR0(){
        int byteAddress = DataMemory_ATmega328P.TIFR0_ADDR;
        byte data = 0x07;

        sdramMemoryTest[byteAddress] = 0x07;
        dataMemory.writeByte(byteAddress, data);


        assertEquals((byte) 0x00, sdramMemoryTest[byteAddress]);
    }

    @Test
    public void writeByte_clearFlag_TIFR1(){
        int byteAddress = DataMemory_ATmega328P.TIFR1_ADDR;
        byte data = 0x27;

        sdramMemoryTest[byteAddress] = 0x27;
        dataMemory.writeByte(byteAddress, data);


        assertEquals((byte) 0x00, sdramMemoryTest[byteAddress]);
    }

    @Test
    public void writeByte_clearFlag_TIFR2(){
        int byteAddress = DataMemory_ATmega328P.TIFR2_ADDR;
        byte data = 0x07;

        sdramMemoryTest[byteAddress] = 0x07;
        dataMemory.writeByte(byteAddress, data);


        assertEquals((byte) 0x00, sdramMemoryTest[byteAddress]);
    }

    @Test
    public void writeByte_clearFlag_ADCSRA(){
        int byteAddress = DataMemory_ATmega328P.ADCSRA_ADDR;
        byte data = 0x0F;

        sdramMemoryTest[byteAddress] = (byte) 0xAA;
        dataMemory.writeByte(byteAddress, data);


        assertEquals((byte) 0x0E, sdramMemoryTest[byteAddress]);
    }

    @Test
    public void writeByte_SynchronizationMode_HardwareClear(){
        int byteAddress = DataMemory_ATmega328P.GTCCR_ADDR;
        byte data = 0x03;

        sdramMemoryTest[byteAddress] = (byte) 0x83;
        dataMemory.writeByte(byteAddress, data);


        assertEquals((byte) 0x00, sdramMemoryTest[byteAddress]);
    }

    @Test
    public void writeByte_SynchronizationMode_KeepConfig(){
        int byteAddress = DataMemory_ATmega328P.GTCCR_ADDR;
        byte data = (byte) 0x81;

        sdramMemoryTest[byteAddress] = (byte) 0x83;
        dataMemory.writeByte(byteAddress, data);


        assertEquals((byte) 0x81, sdramMemoryTest[byteAddress]);
    }

    @Test
    public void writeByte_write16b_TCNT1H(){
        int byteAddress = DataMemory_ATmega328P.TCNT1H_ADDR;
        byte data = (byte) 0x81;

        sdramMemoryTest[byteAddress] = 0x00;
        dataMemory.writeByte(byteAddress, data);

        byte timer1_TEMPTest = Whitebox.getInternalState(dataMemory,"timer1_TEMP");

        assertEquals((byte) 0x00, sdramMemoryTest[byteAddress]);
        assertEquals(data, timer1_TEMPTest);
    }

    @Test
    public void writeByte_write16b_OCR1AH(){
        int byteAddress = DataMemory_ATmega328P.OCR1AH_ADDR;
        byte data = (byte) 0x81;

        sdramMemoryTest[byteAddress] = 0x00;
        dataMemory.writeByte(byteAddress, data);

        byte timer1_TEMPTest = Whitebox.getInternalState(dataMemory,"timer1_TEMP");

        assertEquals((byte) 0x00, sdramMemoryTest[byteAddress]);
        assertEquals(data, timer1_TEMPTest);
    }

    @Test
    public void writeByte_write16b_OCR1BH(){
        int byteAddress = DataMemory_ATmega328P.OCR1BH_ADDR;
        byte data = (byte) 0x81;

        sdramMemoryTest[byteAddress] = 0x00;
        dataMemory.writeByte(byteAddress, data);

        byte timer1_TEMPTest = Whitebox.getInternalState(dataMemory,"timer1_TEMP");

        assertEquals((byte) 0x00, sdramMemoryTest[byteAddress]);
        assertEquals(data, timer1_TEMPTest);
    }

    @Test
    public void writeByte_write16b_TCNT1L(){
        int byteAddress = DataMemory_ATmega328P.TCNT1L_ADDR;
        byte data = (byte) 0x81;

        Whitebox.setInternalState(dataMemory,"timer1_TEMP", (byte)0x55);

        sdramMemoryTest[byteAddress] = 0x00;
        dataMemory.writeByte(byteAddress, data);

        boolean timer1WriteEnableTest = Whitebox.getInternalState(dataMemory,"timer1WriteEnable");
        byte timer1_TEMPTest = Whitebox.getInternalState(dataMemory,"timer1_TEMP");

        assertEquals((byte) data, sdramMemoryTest[byteAddress]);
        assertEquals((byte) timer1_TEMPTest, sdramMemoryTest[DataMemory_ATmega328P.TCNT1H_ADDR]);
        assertFalse(timer1WriteEnableTest);
    }

    @Test
    public void writeByte_write16b_OCR1AL(){
        int byteAddress = DataMemory_ATmega328P.OCR1AL_ADDR;
        byte data = (byte) 0x81;

        Whitebox.setInternalState(dataMemory,"timer1_TEMP", (byte)0x55);
        byte timer1_TEMPTest = Whitebox.getInternalState(dataMemory,"timer1_TEMP");

        sdramMemoryTest[byteAddress] = 0x00;
        dataMemory.writeByte(byteAddress, data);

        boolean flagOCR1AReadyTest = Whitebox.getInternalState(dataMemory,"flagOCR1AReady");

        assertEquals((byte) data, sdramMemoryTest[byteAddress]);
        assertEquals((byte) timer1_TEMPTest, sdramMemoryTest[DataMemory_ATmega328P.OCR1AH_ADDR]);
        assertTrue(flagOCR1AReadyTest);
    }

    @Test
    public void writeByte_write16b_OCR1BL(){
        int byteAddress = DataMemory_ATmega328P.OCR1BL_ADDR;
        byte data = (byte) 0x81;

        Whitebox.setInternalState(dataMemory,"timer1_TEMP", (byte)0x55);
        byte timer1_TEMPTest = Whitebox.getInternalState(dataMemory,"timer1_TEMP");

        sdramMemoryTest[byteAddress] = 0x00;
        dataMemory.writeByte(byteAddress, data);

        assertEquals((byte) data, sdramMemoryTest[byteAddress]);
        assertEquals((byte) timer1_TEMPTest, sdramMemoryTest[DataMemory_ATmega328P.OCR1BH_ADDR]);
    }

    @Test
    public void writeByte_write16b_ICR1H_Enabled(){
        int byteAddress = DataMemory_ATmega328P.ICR1H_ADDR;
        byte data = (byte) 0x81;

        Timer1_ATmega328P.enableICRWrite = true;

        sdramMemoryTest[byteAddress] = 0x00;
        dataMemory.writeByte(byteAddress, data);

        byte timer1_TEMPTest = Whitebox.getInternalState(dataMemory,"timer1_TEMP");

        assertEquals((byte) 0x00, sdramMemoryTest[byteAddress]);
        assertEquals((byte) data, timer1_TEMPTest);
    }

    @Test
    public void writeByte_write16b_ICR1H_Disabled(){
        int byteAddress = DataMemory_ATmega328P.ICR1H_ADDR;
        byte data = (byte) 0x81;

        Timer1_ATmega328P.enableICRWrite = false;

        sdramMemoryTest[byteAddress] = 0x00;
        dataMemory.writeByte(byteAddress, data);

        byte timer1_TEMPTest = Whitebox.getInternalState(dataMemory,"timer1_TEMP");

        assertEquals((byte) 0x00, sdramMemoryTest[byteAddress]);
        assertNotEquals((byte) data, timer1_TEMPTest);
    }

    @Test
    public void writeByte_write16b_ICR1L_Enabled(){
        int byteAddress = DataMemory_ATmega328P.ICR1L_ADDR;
        byte data = (byte) 0x81;

        Timer1_ATmega328P.enableICRWrite = true;
        Whitebox.setInternalState(dataMemory,"timer1_TEMP",(byte)0x7F);

        sdramMemoryTest[byteAddress] = 0x00;
        dataMemory.writeByte(byteAddress, data);

        byte timer1_TEMPTest = Whitebox.getInternalState(dataMemory,"timer1_TEMP");

        assertEquals((byte) data, sdramMemoryTest[byteAddress]);
        assertEquals((byte) 0x7F, sdramMemoryTest[DataMemory_ATmega328P.ICR1H_ADDR]);
    }

    @Test
    public void writeByte_write16b_ICR1L_Disabled(){
        int byteAddress = DataMemory_ATmega328P.ICR1L_ADDR;
        byte data = (byte) 0x81;

        Timer1_ATmega328P.enableICRWrite = false;
        Whitebox.setInternalState(dataMemory,"timer1_TEMP",(byte)0x7F);

        sdramMemoryTest[byteAddress] = 0x00;
        dataMemory.writeByte(byteAddress, data);

        byte timer1_TEMPTest = Whitebox.getInternalState(dataMemory,"timer1_TEMP");

        assertNotEquals((byte) data, sdramMemoryTest[byteAddress]);
        assertNotEquals((byte) 0x7F, sdramMemoryTest[DataMemory_ATmega328P.ICR1H_ADDR]);
    }

    @Test
    public void writeByte_ADCL_Enabled(){
        int byteAddress = DataMemory_ATmega328P.ADCL_ADDR;
        byte data = (byte) 0x81;

        Whitebox.setInternalState(dataMemory,"adcWriteEnable",true);

        sdramMemoryTest[byteAddress] = 0x00;
        dataMemory.writeByte(byteAddress, data);

        assertEquals((byte) data, sdramMemoryTest[byteAddress]);
    }

    @Test
    public void writeByte_ADCL_Disabled(){
        int byteAddress = DataMemory_ATmega328P.ADCL_ADDR;
        byte data = (byte) 0x81;

        Whitebox.setInternalState(dataMemory,"adcWriteEnable",false);

        sdramMemoryTest[byteAddress] = 0x00;
        dataMemory.writeByte(byteAddress, data);

        assertNotEquals((byte) data, sdramMemoryTest[byteAddress]);
    }

    @Test
    public void writeByte_ADCH_Enabled(){
        int byteAddress = DataMemory_ATmega328P.ADCH_ADDR;
        byte data = (byte) 0x81;

        Whitebox.setInternalState(dataMemory,"adcWriteEnable",true);

        sdramMemoryTest[byteAddress] = 0x00;
        dataMemory.writeByte(byteAddress, data);

        assertEquals((byte) data, sdramMemoryTest[byteAddress]);
    }

    @Test
    public void writeByte_ADCH_Disabled(){
        int byteAddress = DataMemory_ATmega328P.ADCH_ADDR;
        byte data = (byte) 0x81;

        Whitebox.setInternalState(dataMemory,"adcWriteEnable",false);

        sdramMemoryTest[byteAddress] = 0x00;
        dataMemory.writeByte(byteAddress, data);

        assertNotEquals((byte) data, sdramMemoryTest[byteAddress]);
    }

    @Test
    public void readByte_Normal(){
        int byteAddress = 0x200;
        byte data = (byte) 0xFF;

        sdramMemoryTest[byteAddress] = data;

        assertEquals((byte) data, dataMemory.readByte(byteAddress));
    }

    @Test
    public void readByte_TCCR0B(){
        int byteAddress = DataMemory_ATmega328P.TCCR0B_ADDR;
        byte data = (byte) 0xFF;

        sdramMemoryTest[byteAddress] = data;

        assertEquals((byte) 0x0F, dataMemory.readByte(byteAddress));
    }

    @Test
    public void readByte_read16b_TCNT1L(){
        int byteAddress = DataMemory_ATmega328P.TCNT1L_ADDR;
        byte data = (byte) 0xFF;

        sdramMemoryTest[DataMemory_ATmega328P.TCNT1H_ADDR] = 0x55;
        sdramMemoryTest[byteAddress] = data;

        assertEquals((byte) data, dataMemory.readByte(byteAddress));

        byte timer1_TEMPTest = Whitebox.getInternalState(dataMemory,"timer1_TEMP");
        assertEquals((byte) 0x55, timer1_TEMPTest);
    }

    @Test
    public void readByte_read16b_OCR1AL(){
        int byteAddress = DataMemory_ATmega328P.OCR1AL_ADDR;
        byte data = (byte) 0xFF;

        sdramMemoryTest[DataMemory_ATmega328P.OCR1AH_ADDR] = 0x55;
        sdramMemoryTest[byteAddress] = data;

        assertEquals((byte) data, dataMemory.readByte(byteAddress));

        byte timer1_TEMPTest = Whitebox.getInternalState(dataMemory,"timer1_TEMP");
        assertEquals((byte) 0x55, timer1_TEMPTest);
    }

    @Test
    public void readByte_read16b_OCR1BL(){
        int byteAddress = DataMemory_ATmega328P.OCR1BL_ADDR;
        byte data = (byte) 0xFF;

        sdramMemoryTest[DataMemory_ATmega328P.OCR1BH_ADDR] = 0x55;
        sdramMemoryTest[byteAddress] = data;

        assertEquals((byte) data, dataMemory.readByte(byteAddress));

        byte timer1_TEMPTest = Whitebox.getInternalState(dataMemory,"timer1_TEMP");
        assertEquals((byte) 0x55, timer1_TEMPTest);
    }

    @Test
    public void readByte_read16b_ICR1L(){
        int byteAddress = DataMemory_ATmega328P.ICR1L_ADDR;
        byte data = (byte) 0xFF;

        sdramMemoryTest[DataMemory_ATmega328P.ICR1H_ADDR] = 0x55;
        sdramMemoryTest[byteAddress] = data;

        assertEquals((byte) data, dataMemory.readByte(byteAddress));

        byte timer1_TEMPTest = Whitebox.getInternalState(dataMemory,"timer1_TEMP");
        assertEquals((byte) 0x55, timer1_TEMPTest);
    }

    @Test
    public void readByte_read16b_TCNT1H(){
        int byteAddress = DataMemory_ATmega328P.TCNT1H_ADDR;
        byte data = (byte) 0xFF;

        Whitebox.setInternalState(dataMemory,"timer1_TEMP",data);

        assertEquals((byte) data, dataMemory.readByte(byteAddress));
    }

    @Test
    public void readByte_read16b_OCR1AH(){
        int byteAddress = DataMemory_ATmega328P.OCR1AH_ADDR;
        byte data = (byte) 0x8F;

        Whitebox.setInternalState(dataMemory,"timer1_TEMP",data);

        assertEquals((byte) data, dataMemory.readByte(byteAddress));
    }

    @Test
    public void readByte_read16b_OCR1BH(){
        int byteAddress = DataMemory_ATmega328P.OCR1BH_ADDR;
        byte data = (byte) 0x8F;

        Whitebox.setInternalState(dataMemory,"timer1_TEMP",data);

        assertEquals((byte) data, dataMemory.readByte(byteAddress));
    }

    @Test
    public void readByte_read16b_ICR1H(){
        int byteAddress = DataMemory_ATmega328P.ICR1H_ADDR;
        byte data = (byte) 0x8F;

        Whitebox.setInternalState(dataMemory,"timer1_TEMP",data);

        assertEquals((byte) data, dataMemory.readByte(byteAddress));
    }

    @Test
    public void readByte_ADCL(){
        int byteAddress = DataMemory_ATmega328P.ADCL_ADDR;
        byte data = (byte) 0xFF;

        sdramMemoryTest[byteAddress] = data;

        assertEquals((byte) data, dataMemory.readByte(byteAddress));

        boolean adcWriteEnableTest = Whitebox.getInternalState(dataMemory,"adcWriteEnable");
        assertFalse(adcWriteEnableTest);
    }

    @Test
    public void readByte_ADCH(){
        int byteAddress = DataMemory_ATmega328P.ADCH_ADDR;
        byte data = (byte) 0xFF;

        sdramMemoryTest[byteAddress] = data;

        assertEquals((byte) data, dataMemory.readByte(byteAddress));

        boolean adcWriteEnableTest = Whitebox.getInternalState(dataMemory,"adcWriteEnable");
        assertTrue(adcWriteEnableTest);
    }

    @Test
    public void writeBit_Normal_True(){
        int byteAddress = 0x700;
        byte position = 3;
        boolean state = true;

        sdramMemoryTest[byteAddress] = 0x00;
        dataMemory.writeBit(byteAddress, position, state);

        assertEquals((byte) 0x08, sdramMemoryTest[byteAddress]);
    }

    @Test
    public void writeBit_Normal_False(){
        int byteAddress = 0x700;
        byte position = 3;
        boolean state = false;

        sdramMemoryTest[byteAddress] = (byte) 0xFF;
        dataMemory.writeBit(byteAddress, position, state);

        assertEquals((byte) 0xF7, sdramMemoryTest[byteAddress]);
    }

    @Test
    public void writeBit_PINB_TogglePORTB(){
        int byteAddress = DataMemory_ATmega328P.PINB_ADDR;
        byte position = 2;
        boolean state = true;

        sdramMemoryTest[DataMemory_ATmega328P.PORTB_ADDR] = 0x55;
        dataMemory.writeBit(byteAddress, position, state);

        assertEquals((byte) 0x51, sdramMemoryTest[DataMemory_ATmega328P.PORTB_ADDR]);
    }

    @Test
    public void writeBit_PINC_TogglePORTC(){
        int byteAddress = DataMemory_ATmega328P.PINC_ADDR;
        byte position = 0;
        boolean state = true;

        sdramMemoryTest[DataMemory_ATmega328P.PORTC_ADDR] = 0x55;
        dataMemory.writeBit(byteAddress, position, state);

        assertEquals((byte) 0x54, sdramMemoryTest[DataMemory_ATmega328P.PORTC_ADDR]);
    }

    @Test
    public void writeBit_PIND_TogglePORTD(){
        int byteAddress = DataMemory_ATmega328P.PIND_ADDR;
        byte position = 1;
        boolean state = true;

        sdramMemoryTest[DataMemory_ATmega328P.PORTD_ADDR] = 0x55;
        dataMemory.writeBit(byteAddress, position, state);

        assertEquals((byte) 0x57, sdramMemoryTest[DataMemory_ATmega328P.PORTD_ADDR]);
    }

    @Test
    public void writeBit_EIFR(){
        int byteAddress = DataMemory_ATmega328P.EIFR_ADDR;
        byte position = 1;
        boolean state = true;

        sdramMemoryTest[byteAddress] = (byte) 0xFF;
        dataMemory.writeBit(byteAddress, position, state);

        assertEquals((byte) 0xFD, sdramMemoryTest[byteAddress]);
    }

    @Test
    public void writeBit_PCIFR(){
        int byteAddress = DataMemory_ATmega328P.PCIFR_ADDR;
        byte position = 0;
        boolean state = true;

        sdramMemoryTest[byteAddress] = (byte) 0xFF;
        dataMemory.writeBit(byteAddress, position, state);

        assertEquals((byte) 0xFE, sdramMemoryTest[byteAddress]);
    }

    @Test
    public void writeBit_TIFR0(){
        int byteAddress = DataMemory_ATmega328P.TIFR0_ADDR;
        byte position = 2;
        boolean state = true;

        sdramMemoryTest[byteAddress] = (byte) 0xFF;
        dataMemory.writeBit(byteAddress, position, state);

        assertEquals((byte) 0xFB, sdramMemoryTest[byteAddress]);
    }

    @Test
    public void writeBit_TIFR1(){
        int byteAddress = DataMemory_ATmega328P.TIFR1_ADDR;
        byte position = 3;
        boolean state = true;

        sdramMemoryTest[byteAddress] = (byte) 0xFF;
        dataMemory.writeBit(byteAddress, position, state);

        assertEquals((byte) 0xF7, sdramMemoryTest[byteAddress]);
    }

    @Test
    public void writeBit_TIFR2(){
        int byteAddress = DataMemory_ATmega328P.TIFR2_ADDR;
        byte position = 4;
        boolean state = true;

        sdramMemoryTest[byteAddress] = (byte) 0xFF;
        dataMemory.writeBit(byteAddress, position, state);

        assertEquals((byte) 0xEF, sdramMemoryTest[byteAddress]);
    }

    @Test
    public void writeBit_ADCSRA_ClearFlag(){
        int byteAddress = DataMemory_ATmega328P.ADCSRA_ADDR;
        byte position = 4;
        boolean state = true;

        sdramMemoryTest[byteAddress] = (byte) 0xFF;
        dataMemory.writeBit(byteAddress, position, state);

        assertEquals((byte) 0xEF, sdramMemoryTest[byteAddress]);
    }

    @Test
    public void writeBit_ADCSRA_ADSCToOne(){
        int byteAddress = DataMemory_ATmega328P.ADCSRA_ADDR;
        byte position = 6;
        boolean state = true;

        sdramMemoryTest[byteAddress] = (byte) 0x00;
        dataMemory.writeBit(byteAddress, position, state);

        assertEquals((byte) 0x40, sdramMemoryTest[byteAddress]);
    }

    @Test
    public void writeBit_ADCSRA_ADSCToZero_NoEffect(){
        int byteAddress = DataMemory_ATmega328P.ADCSRA_ADDR;
        byte position = 6;
        boolean state = false;

        sdramMemoryTest[byteAddress] = (byte) 0xFF;
        dataMemory.writeBit(byteAddress, position, state);

        assertEquals((byte) 0xFF, sdramMemoryTest[byteAddress]);
    }

    @Test
    public void writeBit_SynchronizationMode_HardwareClear(){
        int byteAddress = DataMemory_ATmega328P.GTCCR_ADDR;
        byte position = 7;
        boolean state = false;

        sdramMemoryTest[byteAddress] = (byte) 0x83;
        dataMemory.writeBit(byteAddress, position, state);


        assertEquals((byte) 0x00, sdramMemoryTest[byteAddress]);
    }

    @Test
    public void writeBit_SynchronizationMode_KeepConfig(){
        int byteAddress = DataMemory_ATmega328P.GTCCR_ADDR;
        byte position = 0;
        boolean state = true;

        sdramMemoryTest[byteAddress] = (byte) 0x82;
        dataMemory.writeBit(byteAddress, position, state);


        assertEquals((byte) 0x83, sdramMemoryTest[byteAddress]);
    }

    @Test
    public void readBit_Normal_False(){
        int byteAddress = 0x200;
        byte position = 3;

        sdramMemoryTest[byteAddress] = (byte) 0xF7;

        assertFalse(dataMemory.readBit(byteAddress,position));
    }

    @Test
    public void readBit_Normal_True(){
        int byteAddress = 0x200;
        byte position = 3;

        sdramMemoryTest[byteAddress] = (byte) 0x08;

        assertTrue(dataMemory.readBit(byteAddress,position));
    }

    @Test
    public void readBit_ForceMatchReadAsFalse(){
        int byteAddress = DataMemory_ATmega328P.TCCR0B_ADDR;
        byte position = 7;

        sdramMemoryTest[byteAddress] = (byte) 0xFF;

        assertFalse(dataMemory.readBit(byteAddress,position));
    }

}