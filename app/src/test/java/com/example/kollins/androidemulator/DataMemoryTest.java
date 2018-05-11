package com.example.kollins.androidemulator;

import android.os.Handler;

import com.example.kollins.androidemulator.ATmega328P.DataMemory_ATmega328P;
import com.example.kollins.androidemulator.ATmega328P.ProgramMemory_ATmega328P;
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
import static org.junit.Assert.assertTrue;
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

}