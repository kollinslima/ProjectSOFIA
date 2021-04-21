//
// Created by kollins on 12/09/20.
//

#ifndef PROJECTSOFIA_GENERICAVRMEMORY_H
#define PROJECTSOFIA_GENERICAVRMEMORY_H

#include "../../generic/GenericMemory.h"

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

#define I_FLAG_MASK     0x80
#define T_FLAG_MASK     0x40
#define H_FLAG_MASK     0x20
#define S_FLAG_MASK     0x10
#define V_FLAG_MASK     0x08
#define N_FLAG_MASK     0x04
#define Z_FLAG_MASK     0x02
#define C_FLAG_MASK     0x01

class GenericAVRDataMemory : public GenericMemory {

public:
    virtual ~GenericAVRDataMemory() {}

    virtual bool write(smemaddr16 addr, void *data) = 0;

    virtual bool read(smemaddr16 addr, void *data) = 0;

    virtual smemaddr16 getSize() = 0;

    virtual smemaddr16 getSREGAddress() = 0;

    virtual smemaddr16 getSPLAddress() = 0;

    virtual smemaddr16 getSPHAddress() = 0;

    //TODO: Maybe this can be on parent class
    virtual bool checkInterruption(spc32 *interAddr) = 0;
};


#endif //PROJECTSOFIA_GENERICMEMORY_H
