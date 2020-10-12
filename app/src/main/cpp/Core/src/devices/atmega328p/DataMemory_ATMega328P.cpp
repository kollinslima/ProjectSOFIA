//
// Created by kollins on 27/09/20.
//

#include "../../../include/devices/atmega328p/DataMemory_ATMega328P.h"

#define SDRAM_EXTERNAL_SIZE 2048    //2kB external SDRAM
#define REGISTERS           32      //32 Registers
#define IO_REGISTERS        64      //64 I/O Registers
#define EXT_IO_REGISTERS    160     //160 External I/O Registers

#define MEMORY_SIZE (SDRAM_EXTERNAL_SIZE + REGISTERS + IO_REGISTERS + EXT_IO_REGISTERS)
#define RAMEND              0x08FF

#define UDR0_ADDR           0xC6
#define UBRR0H_ADDR         0xC5
#define UBRR0L_ADDR         0xC4

#define UCSR0C_ADDR         0xC2
#define UCSR0B_ADDR         0xC1
#define UCSR0A_ADDR         0xC0

#define TWAMR_ADDR          0xBD
#define TWCR_ADDR           0xBC
#define TWDR_ADDR           0xBB
#define TWAR_ADDR           0xBA
#define TWSR_ADDR           0xB9
#define TWBR_ADDR           0xB8

#define ASSR_ADDR           0xB6

#define OCR2B_ADDR          0xB4
#define OCR2A_ADDR          0xB4
#define TCNT2_ADDR          0xB2
#define TCCR2B_ADDR         0xB1
#define TCCR2A_ADDR         0xB0

#define OCR1BH_ADDR         0x8B
#define OCR1BL_ADDR         0xBA
#define OCR1AH_ADDR         0x89
#define OCR1AL_ADDR         0x88
#define ICR1H_ADDR          0x87
#define ICR1L_ADDR          0x86
#define TCNT1H_ADDR         0x85
#define TCNT1L_ADDR         0x84

#define TCCR1C_ADDR         0x82
#define TCCR1B_ADDR         0x81
#define TCCR1A_ADDR         0x80
#define DIDR1_ADDR          0x7F
#define DIDR0_ADDR          0x7E

#define ADMUX_ADDR          0x7C
#define ADCSRB_ADDR         0x7B
#define ADCSRA_ADDR         0x7A
#define ADCH_ADDR           0x79
#define ADCL_ADDR           0x78

#define TIMSK2_ADDR         0x70
#define TIMSK1_ADDR         0x6F
#define TIMSK0_ADDR         0x6E
#define PCMSK2_ADDR         0x6D
#define PCMSK1_ADDR         0x6C
#define PCMSK0_ADDR         0x6B

#define EICRA_ADDR          0x69
#define PCICR_ADDR          0x68

#define OSCCAL_ADDR         0x66

#define PRR_ADDR            0x64

#define CLKPR_ADDR          0x61
#define WDTCSR_ADDR         0x60
#define SREG_ADDR           0x5F
#define SPH_ADDR            0x5E
#define SPL_ADDR            0x5D

#define SPMCSR_ADDR         0x57

#define MCUCR_ADDR          0x55
#define MCUSR_ADDR          0x54
#define SMCR_ADDR           0x53

#define ACSR_ADDR           0x50

#define SPDR_ADDR           0x4E
#define SPSR_ADDR           0x4D
#define SPCR_ADDR           0x4C
#define GPIOR2_ADDR         0x4B
#define GPIOR1_ADDR         0x4A

#define OCR0B_ADDR          0x48
#define OCR0A_ADDR          0x47
#define TCNT0_ADDR          0x46
#define TCCR0B_ADDR         0x45
#define TCCR0A_ADDR         0x44
#define GTCCR_ADDR          0x43
#define EEARH_ADDR          0x42
#define EEARL_ADDR          0x41
#define EEDR_ADDR           0x40
#define EECR_ADDR           0x3F
#define GPIOR0_ADDR         0x3E
#define EIMSK_ADDR          0x3D
#define EIFR_ADDR           0x3C
#define PCIFR_ADDR          0x3B

#define TIFR2_ADDR          0x37
#define TIFR1_ADDR          0x36
#define TIFR0_ADDR          0x35

#define PORTD_ADDR          0x2B
#define DDRD_ADDR           0x2A
#define PIND_ADDR           0x29
#define PORTC_ADDR          0x28
#define DDRC_ADDR           0x27
#define PINC_ADDR           0x26
#define PORTB_ADDR          0x25
#define DDRB_ADDR           0x24
#define PINB_ADDR           0x23

DataMemory_ATMega328P::DataMemory_ATMega328P() {
    size = MEMORY_SIZE;
    buffer = new sbyte[size];
    setupDataMemory();
}

DataMemory_ATMega328P::~DataMemory_ATMega328P() {
    delete [] buffer;
}

void DataMemory_ATMega328P::setupDataMemory() {
    /**************RESET CONDITION************/

    buffer[UDR0_ADDR]   = 0x00;
    buffer[UBRR0H_ADDR] = 0x00;
    buffer[UBRR0L_ADDR] = 0x00;

    buffer[UCSR0C_ADDR] = 0x06;
    buffer[UCSR0B_ADDR] = 0x00;
    buffer[UCSR0A_ADDR] = 0x40;

    buffer[TWAMR_ADDR]  = 0x00;
    buffer[TWCR_ADDR]   = 0x00;
    buffer[TWDR_ADDR]   = 0xFF;
    buffer[TWAR_ADDR]   = 0xFE;
    buffer[TWSR_ADDR]   = 0xF8;
    buffer[TWBR_ADDR]   = 0x00;

    buffer[ASSR_ADDR]   = 0x00;

    buffer[OCR2B_ADDR]  = 0x00;
    buffer[OCR2A_ADDR]  = 0x00;
    buffer[TCNT2_ADDR]  = 0x00;
    buffer[TCCR2B_ADDR] = 0x00;
    buffer[TCCR2A_ADDR] = 0x00;

    buffer[OCR1BH_ADDR] = 0x00;
    buffer[OCR1BL_ADDR] = 0x00;
    buffer[OCR1AH_ADDR] = 0x00;
    buffer[OCR1AL_ADDR] = 0x00;
    buffer[ICR1H_ADDR]  = 0x00;
    buffer[ICR1L_ADDR]  = 0x00;
    buffer[TCNT1H_ADDR] = 0x00;
    buffer[TCNT1L_ADDR] = 0x00;

    buffer[TCCR1C_ADDR] = 0x00;
    buffer[TCCR1B_ADDR] = 0x00;
    buffer[TCCR1A_ADDR] = 0x00;
    buffer[DIDR1_ADDR]  = 0x00;
    buffer[DIDR0_ADDR]  = 0x00;

    buffer[ADMUX_ADDR]  = 0x00;
    buffer[ADCSRB_ADDR] = 0x00;
    buffer[ADCSRA_ADDR] = 0x00;
    buffer[ADCH_ADDR]   = 0x00;
    buffer[ADCL_ADDR]   = 0x00;

    buffer[TIMSK2_ADDR] = 0x00;
    buffer[TIMSK1_ADDR] = 0x00;
    buffer[TIMSK0_ADDR] = 0x00;
    buffer[PCMSK2_ADDR] = 0x00;
    buffer[PCMSK1_ADDR] = 0x00;
    buffer[PCMSK0_ADDR] = 0x00;

    buffer[EICRA_ADDR]  = 0x00;
    buffer[PCICR_ADDR]  = 0x00;

    buffer[OSCCAL_ADDR] = 0x9F; //This is actually device specific

    buffer[PRR_ADDR]    = 0x00;

    buffer[CLKPR_ADDR]  = 0x00;
    buffer[WDTCSR_ADDR] = 0x00;
    buffer[SREG_ADDR]   = 0x00;
    buffer[SPH_ADDR]    = (RAMEND>>8); //0x80
    buffer[SPL_ADDR]    = RAMEND;      //0xFF

    buffer[SPMCSR_ADDR] = 0x00;

    buffer[MCUCR_ADDR]  = 0x00;
    buffer[MCUSR_ADDR]  = 0x00;
    buffer[SMCR_ADDR]   = 0x00;

    buffer[ACSR_ADDR]   = 0x00;

    buffer[SPDR_ADDR]   = 0x00;
    buffer[SPSR_ADDR]   = 0x00;
    buffer[SPCR_ADDR]   = 0x00;
    buffer[GPIOR2_ADDR] = 0x00;
    buffer[GPIOR1_ADDR] = 0x00;

    buffer[OCR0B_ADDR]  = 0x00;
    buffer[OCR0A_ADDR]  = 0x00;
    buffer[TCNT0_ADDR]  = 0x00;
    buffer[TCCR0B_ADDR] = 0x00;
    buffer[TCCR0A_ADDR] = 0x00;
    buffer[GTCCR_ADDR]  = 0x00;
    buffer[EEARH_ADDR]  = 0x00;
    buffer[EEARL_ADDR]  = 0x00;
    buffer[EEDR_ADDR]   = 0x00;
    buffer[EECR_ADDR]   = 0x00;
    buffer[GPIOR0_ADDR] = 0x00;
    buffer[EIMSK_ADDR]  = 0x00;
    buffer[EIFR_ADDR]   = 0x00;
    buffer[PCIFR_ADDR]  = 0x00;

    buffer[TIFR2_ADDR]  = 0x00;
    buffer[TIFR1_ADDR]  = 0x00;
    buffer[TIFR0_ADDR]  = 0x00;

    buffer[PORTD_ADDR]  = 0x00;
    buffer[DDRD_ADDR]   = 0x00;
    buffer[PIND_ADDR]   = 0x00;
    buffer[PORTC_ADDR]  = 0x00;
    buffer[DDRC_ADDR]   = 0x00;
    buffer[PINC_ADDR]   = 0x00;
    buffer[PORTB_ADDR]  = 0x00;
    buffer[DDRB_ADDR]   = 0x00;
    buffer[PINB_ADDR]   = 0x00;
    /*****************************************/
}

bool DataMemory_ATMega328P::write(smemaddr16 addr, void *data) {
    buffer[SAFE_ADDR(addr, RAMEND)] = *(static_cast<sbyte *>(data));
    return true;
}

bool DataMemory_ATMega328P::read(smemaddr16 addr, void *data) {
    *(static_cast<sbyte *>(data)) = buffer[SAFE_ADDR(addr, RAMEND)];
    return true;
}

smemaddr16 DataMemory_ATMega328P::getSize() {
    return size;
}

smemaddr16 DataMemory_ATMega328P::getSREGAddres() {
    return SREG_ADDR;
}

smemaddr16 DataMemory_ATMega328P::getSPLAddres() {
    return SPL_ADDR;
}

smemaddr16 DataMemory_ATMega328P::getSPHAddres() {
    return SPH_ADDR;
}
