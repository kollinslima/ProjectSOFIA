//
// Created by kollins on 18/04/21.
//

#include "../../../include/devices/atmega328p/ADC_ATMega328P.h"

#define SOFIA_ADC_ATMEGA328P_TAG "SOFIA ADC ATMEGA328P"

#define BANDGAP_REFERENCE_VOLTAGE 1100 //mV
#define BANDGAP_REFERENE_INDEX 14

#define GND_REFERENCE_VOLTAGE 0 //mV
#define GND_REFERENCE_INDEX 15

//TODO: Make this configurable and enable support to AVcc and 1.1V reference
#define AREF 5000 //mV
#define ADC_RESOLUTION 0x3FF

ADC_ATMega328P::ADC_ATMega328P(DataMemory_ATMega328P &dataMemory) :
        datMem(dataMemory) {
    converted = 0;
    voltageReference = AREF;
    adcResolution = ADC_RESOLUTION;
    inputMux[BANDGAP_REFERENE_INDEX] = BANDGAP_REFERENCE_VOLTAGE;
    inputMux[GND_REFERENCE_INDEX] = GND_REFERENCE_VOLTAGE;
}

void ADC_ATMega328P::setAnalogInput(int pin, float voltage) {
    inputMux[pin] = voltage*1000;   //to mV
}

void ADC_ATMega328P::run() {
    if ((datMem.buffer[ADCSRA_ADDR]&0xC0) == 0xC0) {
        convert();
        if ((!(datMem.buffer[ADCSRA_ADDR]&0x20)) || (datMem.buffer[ADCSRB_ADDR]&0x07)){
            datMem.buffer[ADCSRA_ADDR] &= 0xBF; //Clear ADSC: ADC Start Conversion
        }
    }
}

/*
 * For SOFIA, conversion will take only 1 clock cycle,
 * instead of 25/13 of ATMega328P. (Will this cause any trouble?)
 */
void ADC_ATMega328P::convert() {
    converted = inputMux[datMem.buffer[ADMUX_ADDR]&0x0F];
    converted = (converted > voltageReference)?adcResolution:((converted*adcResolution)/voltageReference);
    converted <<= (datMem.buffer[ADMUX_ADDR]&0x20)?6:0;

    datMem.buffer[ADCL_ADDR] = converted;
    datMem.buffer[ADCH_ADDR] = (converted>>8);

    datMem.buffer[ADCSRA_ADDR] |= 0x10; //Set interruption flag
}
