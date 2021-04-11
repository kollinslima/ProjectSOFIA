//
// Created by kollins on 27/09/20.
//

#ifndef PROJECTSOFIA_DATAMEMORY_ATMEGA328P_H
#define PROJECTSOFIA_DATAMEMORY_ATMEGA328P_H


#include "../components/avr/memory/GenericAVRDataMemory.h"
#include "../../../include/SofiaCoreController.h"
#include <string>

#define PORTB_START_PIN 14
#define PORTC_START_PIN 23

#define PORTD_PIN_OFFSET 2
#define PORTB_PIN_OFFSET PORTB_START_PIN
#define PORTC_PIN_OFFSET PORTC_START_PIN

using namespace std;

class DataMemory_ATMega328P : public GenericAVRDataMemory {

public:
    DataMemory_ATMega328P(SofiaUiNotifier *notifier);

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
    void setupDataMemory();

    int getIoRegisters(int input, sbyte *ddr = nullptr, sbyte *port = nullptr, sbyte *pin = nullptr);
    sbyte togglePort(sbyte portByte, sbyte pinByte);

    SofiaUiNotifier *notifier;
};


#endif //PROJECTSOFIA_DATAMEMORY_ATMEGA328P_H
