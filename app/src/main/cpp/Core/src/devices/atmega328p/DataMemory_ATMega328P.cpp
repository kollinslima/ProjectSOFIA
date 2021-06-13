//
// Created by kollins on 27/09/20.
//

#include "../../../include/devices/atmega328p/DataMemory_ATMega328P.h"
#include "../../../include/utils/Functions.h"

#define SOFIA_DATA_MEMORY_ATMEGA328P_TAG "SOFIA DATA MEMORY ATMEGA328P"

#define SDRAM_EXTERNAL_SIZE 2048    //2kB external SDRAM
#define REGISTERS           32      //32 Registers
#define IO_REGISTERS        64      //64 I/O Registers
#define EXT_IO_REGISTERS    160     //160 External I/O Registers

#define MEMORY_SIZE (SDRAM_EXTERNAL_SIZE + REGISTERS + IO_REGISTERS + EXT_IO_REGISTERS)
#define RAMEND              0x08FF

//Interrupt Vectors
#define INT0                0x0002
#define INT1                0x0004
#define PCINT0              0x0006
#define PCINT1              0x0008
#define PCINT2              0x000A
#define WDT                 0x000C
#define TIMER2_COMPA        0x000E
#define TIMER2_COMPB        0x0010
#define TIMER2_OVF          0x0012
#define TIMER1_CAPT         0x0014
#define TIMER1_COMPA        0x0016
#define TIMER1_COMPB        0x0018
#define TIMER1_OVF          0x001A
#define TIMER0_COMPA        0x001C
#define TIMER0_COMPB        0x001E
#define TIMER0_OVF          0x0020
#define SPI_STC             0x0022
#define USART_RX            0x0024
#define USART_UDRE          0x0026
#define USART_TX            0x0028
#define ADC                 0x002A
#define EE_READY            0x002C
#define ANALOG_COMP         0x002E
#define TWI                 0x0030
#define SPM_READY           0x0032

DataMemory_ATMega328P::DataMemory_ATMega328P(GenericMeter *meter) {
    this->freqDcMeter = dynamic_cast<FreqDcMeter *>(meter);
    size = MEMORY_SIZE;
    buffer = new sbyte[size];
    memset(buffer, 0, size);
    setupDataMemory();

    //Notify UI in case of reset or if it's loading another .hex file
//    this->notifier->addNotification(
//            IO_CHANGED_LISTENER, to_string(DDRD_ADDR) + ":" + to_string(buffer[DDRD_ADDR]));
//    this->notifier->addNotification(
//            IO_CHANGED_LISTENER, to_string(DDRC_ADDR) + ":" + to_string(buffer[DDRC_ADDR]));
//    this->notifier->addNotification(
//            IO_CHANGED_LISTENER, to_string(DDRB_ADDR) + ":" + to_string(buffer[DDRB_ADDR]));
//    this->notifier->addNotification(
//            IO_CHANGED_LISTENER, to_string(PORTD_ADDR) + ":" + to_string(buffer[PORTD_ADDR]));
//    this->notifier->addNotification(
//            IO_CHANGED_LISTENER, to_string(PORTC_ADDR) + ":" + to_string(buffer[PORTC_ADDR]));
//    this->notifier->addNotification(
//            IO_CHANGED_LISTENER, to_string(PORTB_ADDR) + ":" + to_string(buffer[PORTB_ADDR]));
}

DataMemory_ATMega328P::~DataMemory_ATMega328P() {
    delete[] buffer;
}

void DataMemory_ATMega328P::setupDataMemory() {
    /**************RESET CONDITION************/
    buffer[UDR0_ADDR] = 0x00;
    buffer[UBRR0H_ADDR] = 0x00;
    buffer[UBRR0L_ADDR] = 0x00;

    buffer[UCSR0C_ADDR] = 0x06;
    buffer[UCSR0B_ADDR] = 0x00;
    buffer[UCSR0A_ADDR] = 0x40;

    buffer[TWAMR_ADDR] = 0x00;
    buffer[TWCR_ADDR] = 0x00;
    buffer[TWDR_ADDR] = 0xFF;
    buffer[TWAR_ADDR] = 0xFE;
    buffer[TWSR_ADDR] = 0xF8;
    buffer[TWBR_ADDR] = 0x00;

    buffer[ASSR_ADDR] = 0x00;

    buffer[OCR2B_ADDR] = 0x00;
    buffer[OCR2A_ADDR] = 0x00;
    buffer[TCNT2_ADDR] = 0x00;
    buffer[TCCR2B_ADDR] = 0x00;
    buffer[TCCR2A_ADDR] = 0x00;

    buffer[OCR1BH_ADDR] = 0x00;
    buffer[OCR1BL_ADDR] = 0x00;
    buffer[OCR1AH_ADDR] = 0x00;
    buffer[OCR1AL_ADDR] = 0x00;
    buffer[ICR1H_ADDR] = 0x00;
    buffer[ICR1L_ADDR] = 0x00;
    buffer[TCNT1H_ADDR] = 0x00;
    buffer[TCNT1L_ADDR] = 0x00;

    buffer[TCCR1C_ADDR] = 0x00;
    buffer[TCCR1B_ADDR] = 0x00;
    buffer[TCCR1A_ADDR] = 0x00;
    buffer[DIDR1_ADDR] = 0x00;
    buffer[DIDR0_ADDR] = 0x00;

    buffer[ADMUX_ADDR] = 0x00;
    buffer[ADCSRB_ADDR] = 0x00;
    buffer[ADCSRA_ADDR] = 0x00;
    buffer[ADCH_ADDR] = 0x00;
    buffer[ADCL_ADDR] = 0x00;

    buffer[TIMSK2_ADDR] = 0x00;
    buffer[TIMSK1_ADDR] = 0x00;
    buffer[TIMSK0_ADDR] = 0x00;
    buffer[PCMSK2_ADDR] = 0x00;
    buffer[PCMSK1_ADDR] = 0x00;
    buffer[PCMSK0_ADDR] = 0x00;

    buffer[EICRA_ADDR] = 0x00;
    buffer[PCICR_ADDR] = 0x00;

    buffer[OSCCAL_ADDR] = 0x9F; //This is actually device specific

    buffer[PRR_ADDR] = 0x00;

    buffer[CLKPR_ADDR] = 0x00;
    buffer[WDTCSR_ADDR] = 0x00;
    buffer[SREG_ADDR] = 0x00;
    buffer[SPH_ADDR] = static_cast<sbyte>(RAMEND >> 8);    //0x80
    buffer[SPL_ADDR] = static_cast<sbyte>(RAMEND);       //0xFF

    buffer[SPMCSR_ADDR] = 0x00;

    buffer[MCUCR_ADDR] = 0x00;
    buffer[MCUSR_ADDR] = 0x00;
    buffer[SMCR_ADDR] = 0x00;

    buffer[ACSR_ADDR] = 0x00;

    buffer[SPDR_ADDR] = 0x00;
    buffer[SPSR_ADDR] = 0x00;
    buffer[SPCR_ADDR] = 0x00;
    buffer[GPIOR2_ADDR] = 0x00;
    buffer[GPIOR1_ADDR] = 0x00;

    buffer[OCR0B_ADDR] = 0x00;
    buffer[OCR0A_ADDR] = 0x00;
    buffer[TCNT0_ADDR] = 0x00;
    buffer[TCCR0B_ADDR] = 0x00;
    buffer[TCCR0A_ADDR] = 0x00;
    buffer[GTCCR_ADDR] = 0x00;
    buffer[EEARH_ADDR] = 0x00;
    buffer[EEARL_ADDR] = 0x00;
    buffer[EEDR_ADDR] = 0x00;
    buffer[EECR_ADDR] = 0x00;
    buffer[GPIOR0_ADDR] = 0x00;
    buffer[EIMSK_ADDR] = 0x00;
    buffer[EIFR_ADDR] = 0x00;
    buffer[PCIFR_ADDR] = 0x00;

    buffer[TIFR2_ADDR] = 0x00;
    buffer[TIFR1_ADDR] = 0x00;
    buffer[TIFR0_ADDR] = 0x00;

    buffer[PORTD_ADDR] = 0x00;
    buffer[DDRD_ADDR] = 0x00;
    buffer[PIND_ADDR] = 0x00;
    buffer[PORTC_ADDR] = 0x00;
    buffer[DDRC_ADDR] = 0x00;
    buffer[PINC_ADDR] = 0x00;
    buffer[PORTB_ADDR] = 0x00;
    buffer[DDRB_ADDR] = 0x00;
    buffer[PINB_ADDR] = 0x00;
    /*****************************************/
}

bool DataMemory_ATMega328P::checkInterruption(spc32 *interAddr) {
    if (buffer[SREG_ADDR] & I_FLAG_MASK) {

        ////////////////////////External interrupt request 0////////////////////////
        if (buffer[EIMSK_ADDR] & 0x01) {
            if (buffer[EIFR_ADDR] & 0x01) {
                *interAddr = INT0;
                //The flag is cleared when the interrupt routine is executed.
                buffer[EIFR_ADDR] &= 0xFE;
                return true;
            } else if (!(buffer[EICRA_ADDR] & 0x03)) {
                //Level interrupt - INTF0 is always cleared
                //if enabled, the interrupts will trigger even if pins are configured as outputs.
                if (buffer[DDRD_ADDR] & 0x04) {
                    //Pin configured as output, check PORT
                    if (!(buffer[PORTD_ADDR] & 0x04)) {
                        *interAddr = INT0;
                        return true;
                    }
                } else {
                    //Pin configured as output, check PIN
                    if (!(buffer[PIND_ADDR] & 0x04)) {
                        *interAddr = INT0;
                        return true;
                    }
                }
            }
        }

        ////////////////////////External interrupt request 1////////////////////////
        if (buffer[EIMSK_ADDR] & 0x02) {
            if (buffer[EIFR_ADDR] & 0x02) {
                *interAddr = INT1;
                //The flag is cleared when the interrupt routine is executed.
                buffer[EIFR_ADDR] &= 0xFD;
                return true;
            } else if (!(buffer[EICRA_ADDR] & 0x0C)) {
                //Level interrupt - INTF1 is always cleared
                //if enabled, the interrupts will trigger even if pins are configured as outputs.
                if (buffer[DDRD_ADDR] & 0x08) {
                    //Pin configured as output, check PORT
                    if (!(buffer[PORTD_ADDR] & 0x08)) {
                        *interAddr = INT1;
                        return true;
                    }
                } else {
                    //Pin configured as output, check PIN
                    if (!(buffer[PIND_ADDR] & 0x08)) {
                        *interAddr = INT1;
                        return true;
                    }
                }
            }
        }

        ///////////////////////Pin change interrupt request 0///////////////////////

        ///////////////////////Pin change interrupt request 1///////////////////////

        ///////////////////////Pin change interrupt request 2///////////////////////

        ////////////////////////Watchdog time-out interrupt/////////////////////////

        ///////////////////////Timer/Counter2 compare match A///////////////////////
        if (buffer[TIFR2_ADDR] & 0x02) {
            *interAddr = TIMER2_COMPA;
            //The flag is cleared when the interrupt routine is executed.
            buffer[TIFR2_ADDR] &= 0xFD;
            return true;
        }

        ///////////////////////Timer/Counter2 compare match B///////////////////////
        if (buffer[TIFR2_ADDR] & 0x04) {
            *interAddr = TIMER2_COMPB;
            //The flag is cleared when the interrupt routine is executed.
            buffer[TIFR2_ADDR] &= 0xFB;
            return true;
        }

        //////////////////////////Timer/Counter2 overflow///////////////////////////
        if (buffer[TIFR2_ADDR] & 0x01) {
            *interAddr = TIMER2_OVF;
            //The flag is cleared when the interrupt routine is executed.
            buffer[TIFR2_ADDR] &= 0xFE;
            return true;
        }

        ////////////////////////Timer/Counter1 capture event////////////////////////
        if (buffer[TIFR1_ADDR] & 0x20) {
            *interAddr = TIMER1_CAPT;
            //The flag is cleared when the interrupt routine is executed.
            buffer[TIFR2_ADDR] &= 0xDF;
            return true;
        }

        ///////////////////////Timer/Counter1 compare match A///////////////////////
        if (buffer[TIFR1_ADDR] & 0x02) {
            *interAddr = TIMER1_COMPA;
            //The flag is cleared when the interrupt routine is executed.
            buffer[TIFR1_ADDR] &= 0xFD;
            return true;
        }

        ///////////////////////Timer/Counter1 compare match B///////////////////////
        if (buffer[TIFR1_ADDR] & 0x04) {
            *interAddr = TIMER1_COMPB;
            //The flag is cleared when the interrupt routine is executed.
            buffer[TIFR1_ADDR] &= 0xFB;
            return true;
        }

        //////////////////////////Timer/Counter1 overflow///////////////////////////
        if (buffer[TIFR1_ADDR] & 0x01) {
            *interAddr = TIMER1_OVF;
            //The flag is cleared when the interrupt routine is executed.
            buffer[TIFR1_ADDR] &= 0xFE;
            return true;
        }

        ///////////////////////Timer/Counter0 compare match A///////////////////////
        if (buffer[TIFR0_ADDR] & 0x02) {
            *interAddr = TIMER0_COMPA;
            //The flag is cleared when the interrupt routine is executed.
            buffer[TIFR0_ADDR] &= 0xFD;
            return true;
        }

        ///////////////////////Timer/Counter0 compare match B///////////////////////
        if (buffer[TIFR0_ADDR] & 0x04) {
            *interAddr = TIMER0_COMPB;
            //The flag is cleared when the interrupt routine is executed.
            buffer[TIFR0_ADDR] &= 0xFB;
            return true;
        }

        //////////////////////////Timer/Counter0 overflow///////////////////////////
        if (buffer[TIFR0_ADDR] & 0x01) {
            *interAddr = TIMER0_OVF;
            //The flag is cleared when the interrupt routine is executed.
            buffer[TIFR0_ADDR] &= 0xFE;
            return true;
        }

        ////////////////////////SPI serial transfer complete////////////////////////

        /////////////////////////////USART Rx complete//////////////////////////////

        /////////////////////////USART, data register empty/////////////////////////

        /////////////////////////////USART, Tx complete/////////////////////////////

        //////////////////////////ADC conversion complete///////////////////////////
        if (buffer[ADCSRA_ADDR] & 0x08) {
            *interAddr = ADC;
            //The flag is cleared when the interrupt routine is executed.
            buffer[ADCSRA_ADDR] &= 0xF7;
            return true;
        }

        ////////////////////////////////EEPROM ready////////////////////////////////

        /////////////////////////////Analog comparator//////////////////////////////

        //////////////////////////2-wire serial interface///////////////////////////

        /////////////////////////Store program memory ready/////////////////////////
    }
    return false;
}

sbyte DataMemory_ATMega328P::togglePort(sbyte portByte, sbyte pinByte) {
    sbyte toggled = 0x00;
    sbyte mask = 0x01;
    sbyte pin = pinByte;
    for (int i = 0; i < 8; ++i, mask = mask << 1, pin = pin >> 1) {
        toggled |= 0x01 & pin ? (~portByte) | mask : portByte | mask;
    }
    return toggled;
}

bool DataMemory_ATMega328P::write(smemaddr16 addr, void *data) {
    sbyte byte = *(static_cast<sbyte *>(data));

    switch (addr) {
        case PORTD_ADDR:
        case PORTC_ADDR:
        case PORTB_ADDR: {
            if (buffer[addr] != byte) {
                this->freqDcMeter->measureFreqDc(addr, buffer[addr], byte);
                buffer[addr] = byte;
            }
            break;
        }
//        case DDRD_ADDR:
//        case DDRC_ADDR:
//        case DDRB_ADDR:
            /*
             * Writing a logic one to PINxn toggles the value of PORTxn,
             * independent on the value of DDRxn.
             */
        case PIND_ADDR: {
            tmp = togglePort(buffer[PORTD_ADDR], byte);
            freqDcMeter->measureFreqDc(addr, buffer[PORTD_ADDR], tmp);
            buffer[PORTD_ADDR] = tmp;
            break;
        }
        case PINC_ADDR: {
            tmp = togglePort(buffer[PORTC_ADDR], byte);
            freqDcMeter->measureFreqDc(addr, buffer[PORTC_ADDR], tmp);
            buffer[PORTC_ADDR] = tmp;
            break;
        }
        case PINB_ADDR: {
            tmp = togglePort(buffer[PORTD_ADDR], byte);
            freqDcMeter->measureFreqDc(addr, buffer[PORTD_ADDR], tmp);
            buffer[PORTB_ADDR] = tmp;
            break;
        }
        case OCR0A_ADDR: {
            switch ((buffer[TCCR0A_ADDR] & 0x03)) {
                case 0x01:
                case 0x03:
                    //Enable double buffer for PWM mode
                    doubleBuffer[DOUBLE_BUFFER_OCR0A] = byte;
                    break;
                default:
                    buffer[OCR0A_ADDR] = byte;
            }
            break;
        }
        case OCR0B_ADDR: {
            switch ((buffer[TCCR0A_ADDR] & 0x03)) {
                case 0x01:
                case 0x03:
                    //Enable double buffer for PWM mode
                    doubleBuffer[DOUBLE_BUFFER_OCR0B] = byte;
                    break;
                default:
                    buffer[OCR0B_ADDR] = byte;
            }
            break;
        }
        case TCNT1H_ADDR: {
            doubleBuffer[TEMP] = byte;
            break;
        }
        case TCNT1L_ADDR: {
            buffer[TCNT1H_ADDR] = doubleBuffer[TEMP];
            buffer[TCNT1L_ADDR] = byte;
            break;
        }
        case OCR1AH_ADDR: {
            doubleBuffer[TEMP] = byte;
            break;
        }
        case OCR1AL_ADDR: {
            switch (((buffer[TCCR1B_ADDR] >> 1) & WGM_TCCR1B_MASK) |
                    (buffer[TCCR1A_ADDR] & WGM_TCCR1A_MASK)) {
                case 0x00:
                case 0x04:
                case 0x0C:
                case 0x0D:
                    buffer[OCR1AH_ADDR] = doubleBuffer[TEMP];
                    buffer[OCR1AL_ADDR] = byte;
                    break;
                default:
                    //Enable double buffer for PWM mode
                    doubleBuffer[DOUBLE_BUFFER_OCR1AH] = doubleBuffer[TEMP];
                    doubleBuffer[DOUBLE_BUFFER_OCR1AL] = byte;
            }
            break;
        }
        case OCR1BH_ADDR: {
            doubleBuffer[TEMP] = byte;
            break;
        }
        case OCR1BL_ADDR: {
            switch (((buffer[TCCR1B_ADDR] >> 1) & WGM_TCCR1B_MASK) |
                    (buffer[TCCR1A_ADDR] & WGM_TCCR1A_MASK)) {
                case 0x00:
                case 0x04:
                case 0x0C:
                case 0x0D:
                    buffer[OCR1BH_ADDR] = doubleBuffer[TEMP];
                    buffer[OCR1BL_ADDR] = byte;
                    break;
                default:
                    //Enable double buffer for PWM mode
                    doubleBuffer[DOUBLE_BUFFER_OCR1BH] = doubleBuffer[TEMP];
                    doubleBuffer[DOUBLE_BUFFER_OCR1BL] = byte;
            }
            break;
        }
            /* TODO: The ICR1 register can only be written when using a waveform generation mode that
             * utilizes the ICR1 register for defining the counter’s TOP value.
             */
        case ICR1H_ADDR: {
            doubleBuffer[TEMP] = byte;
            break;
        }
        case ICR1L_ADDR: {
            buffer[ICR1H_ADDR] = doubleBuffer[TEMP];
            buffer[ICR1L_ADDR] = byte;
            break;
        }
        case OCR2A_ADDR: {
            switch ((buffer[TCCR2A_ADDR] & 0x03)) {
                case 0x01:
                case 0x03:
                    //Enable double buffer for PWM mode
                    doubleBuffer[DOUBLE_BUFFER_OCR2A] = byte;
                    break;
                default:
                    buffer[OCR2A_ADDR] = byte;
            }
            break;
        }
        case OCR2B_ADDR: {
            switch ((buffer[TCCR2A_ADDR] & 0x03)) {
                case 0x01:
                case 0x03:
                    //Enable double buffer for PWM mode
                    doubleBuffer[DOUBLE_BUFFER_OCR2B] = byte;
                    break;
                default:
                    buffer[OCR2B_ADDR] = byte;
            }
            break;
        }
        case EIFR_ADDR: {
            buffer[EIFR_ADDR] &= (~(byte & 0x03));
            break;
        }
        case TIFR0_ADDR: {
            buffer[TIFR0_ADDR] &= (~(byte & 0x07));
            break;
        }
        case TIFR1_ADDR: {
            buffer[TIFR1_ADDR] &= (~(byte & 0x27));
            break;
        }
        case TIFR2_ADDR: {
            buffer[TIFR2_ADDR] &= (~(byte & 0x07));
            break;
        }
        case ADCSRA_ADDR:
            buffer[ADCSRA_ADDR] = (byte & 0xAF) |
                                  ((buffer[ADCSRA_ADDR] | byte) & 0x40) |
                                  // ADSC: ADC Start Conversion
                                  ((buffer[ADCSRA_ADDR] & (~byte)) &
                                   0x10); // ADIF: ADC Interrupt Flag
            break;
        default:
            buffer[SAFE_ADDR(addr, MEMORY_SIZE)] = byte;
    }
    return true;
}

bool DataMemory_ATMega328P::read(smemaddr16 addr, void *data) {
    sbyte byte;
    switch (addr) {
        case TCNT1H_ADDR: {
            byte = doubleBuffer[TEMP];
            break;
        }
        case TCNT1L_ADDR: {
            doubleBuffer[TEMP] = buffer[TCNT1H_ADDR];
            byte = buffer[TCNT1L_ADDR];
            break;
        }
        case ICR1H_ADDR: {
            byte = doubleBuffer[TEMP];
            break;
        }
        case ICR1L_ADDR: {
            doubleBuffer[TEMP] = buffer[ICR1H_ADDR];
            byte = buffer[ICR1L_ADDR];
            break;
        }
        default:
            byte = buffer[SAFE_ADDR(addr, MEMORY_SIZE)];
    }
    *(static_cast<sbyte *>(data)) = byte;
    return true;
}

smemaddr16 DataMemory_ATMega328P::getSize() {
    return size;
}

smemaddr16 DataMemory_ATMega328P::getSREGAddress() {
    return SREG_ADDR;
}

smemaddr16 DataMemory_ATMega328P::getSPLAddress() {
    return SPL_ADDR;
}

smemaddr16 DataMemory_ATMega328P::getSPHAddress() {
    return SPH_ADDR;
}

bool DataMemory_ATMega328P::isDigitalInputDisabled(int input) {
    return 0x01 & (buffer[DIDR0_ADDR] >> input);
}

bool DataMemory_ATMega328P::isPullUpDisabled(int input) {
    if (0x01 & (buffer[MCUCR_ADDR] >> 4)) {
        return true;
    } else {
        sbyte ddr, port;
        int pos = getIoRegisters(input, &ddr, &port);
        return !((0x01 & (~(buffer[ddr] >> pos))) & (0x01 & (buffer[port] >> pos)));
    }
}

int DataMemory_ATMega328P::getIoRegisters(int input, sbyte *ddr, sbyte *port, sbyte *pin) {
    int pos;
    if (input < PORTB_START_PIN) {
        SAFE_ATTR(ddr, DDRD_ADDR)
        SAFE_ATTR(port, PORTD_ADDR)
        SAFE_ATTR(pin, PIND_ADDR)
        pos = input - PORTD_PIN_OFFSET;
    } else if (input < PORTC_START_PIN) {
        SAFE_ATTR(ddr, DDRB_ADDR)
        SAFE_ATTR(port, PORTB_ADDR)
        SAFE_ATTR(pin, PINB_ADDR)
        pos = input - PORTB_PIN_OFFSET;
    } else {
        SAFE_ATTR(ddr, DDRC_ADDR)
        SAFE_ATTR(port, PORTC_ADDR)
        SAFE_ATTR(pin, PINC_ADDR)
        pos = input - PORTC_PIN_OFFSET;
    }
    return pos;
}

int DataMemory_ATMega328P::getPinNumber(smemaddr16 addr, int position) {
    int ret = 0;
    switch (addr) {
        case PORTB_ADDR:
            ret = (position < 6) ? PORTB_START_PIN + position : 9 + (position - 6);
            break;
        case PORTC_ADDR:
            //PORTC contains only 7 pins
            ret = (position < 6) ? PORTC_START_PIN + position : 0;
            break;
        case PORTD_ADDR:
            ret = (position < 5) ? PORTD_START_PIN + position : 11 + (position - 5);
            break;
        default:
            LOGE(SOFIA_DATA_MEMORY_ATMEGA328P_TAG, "Can't get pin number for %X (%d)", addr,
                 position);
    }
    return ret;
}

void DataMemory_ATMega328P::setDigitalInput(int input, bool state) {
    sbyte pin;
    int pos = getIoRegisters(input, nullptr, nullptr, &pin);
    sbyte mask = 0x01 << pos;
    buffer[pin] = state ? buffer[pin] | mask : buffer[pin] & (~mask);
}
