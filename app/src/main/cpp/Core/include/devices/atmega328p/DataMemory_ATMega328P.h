//
// Created by kollins on 27/09/20.
//

#ifndef PROJECTSOFIA_DATAMEMORY_ATMEGA328P_H
#define PROJECTSOFIA_DATAMEMORY_ATMEGA328P_H


#include "../components/avr/memory/GenericAVRDataMemory.h"
#include "../../../include/SofiaCoreController.h"
#include <string>

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
#define OCR2A_ADDR          0xB3
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

//Double buffer
#define TEMP                    0
#define DOUBLE_BUFFER_OCR0A     1
#define DOUBLE_BUFFER_OCR0B     2
#define DOUBLE_BUFFER_OCR1AL    3
#define DOUBLE_BUFFER_OCR1AH    4
#define DOUBLE_BUFFER_OCR1BL    5
#define DOUBLE_BUFFER_OCR1BH    6
#define DOUBLE_BUFFER_OCR2A     7
#define DOUBLE_BUFFER_OCR2B     8
#define DOUBLE_BUFFER_SIZE      9

//Timers mask
#define WGM_TCCR0A_MASK     0x06
#define WGM_TCCR0B_MASK     0x08
#define WGM_TCCR1A_MASK     0x03
#define WGM_TCCR1B_MASK     0x0C
#define WGM_TCCR2A_MASK     0x06
#define WGM_TCCR2B_MASK     0x08

#define PORTB_START_PIN 14
#define PORTC_START_PIN 23

#define PORTD_PIN_OFFSET 2
#define PORTB_PIN_OFFSET PORTB_START_PIN
#define PORTC_PIN_OFFSET PORTC_START_PIN

using namespace std;

class DataMemory_ATMega328P : public GenericAVRDataMemory {

public:
    friend class ADC_ATMega328P;
    friend class Timer_ATMega328P;
    friend class Timer0_ATMega328P;
    friend class Timer1_ATMega328P;
    friend class Timer2_ATMega328P;

    DataMemory_ATMega328P();

    ~DataMemory_ATMega328P();

    bool write(smemaddr16 addr, void *data) override;

    bool read(smemaddr16 addr, void *data) override;

    smemaddr16 getSize() override;

    smemaddr16 getSREGAddress() override;

    smemaddr16 getSPLAddress() override;

    smemaddr16 getSPHAddress() override;

    bool checkInterruption(spc32 *interAddr) override;

    bool isDigitalInputDisabled(int input);
    bool isPullUpDisabled(int input);
    void setDigitalInput(int input, bool state);

private:
    sbyte doubleBuffer[DOUBLE_BUFFER_SIZE];

    void setupDataMemory();

    int getIoRegisters(int input, sbyte *ddr = nullptr, sbyte *port = nullptr, sbyte *pin = nullptr);
    sbyte togglePort(sbyte portByte, sbyte pinByte);

};


#endif //PROJECTSOFIA_DATAMEMORY_ATMEGA328P_H