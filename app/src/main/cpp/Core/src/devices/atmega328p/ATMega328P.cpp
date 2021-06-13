//
// Created by kollins on 12/09/20.
//

#include <unistd.h>
#include <ctime>
#include "../../../include/devices/atmega328p/ATMega328P.h"
#include "../../../include/SofiaCoreController.h"
#include "../../../include/parsers/IntelParser.h"
#include "../../../include/devices/components/avr/cpu/AVRCPU_AVRe.h"
#include "../../../include/devices/atmega328p/Timer0_ATMega328P.h"
#include "../../../include/devices/atmega328p/Timer1_ATMega328P.h"
#include "../../../include/devices/atmega328p/Timer2_ATMega328P.h"

#define SOFIA_ATMEGA328P_TAG "SOFIA ATMEGA328P"

#define DEFAULT_CLOCK_FREQ 16000000
#define CPU_MODULE_INDEX 0
#define TIMER0_MODULE_INDEX 1
#define TIMER1_MODULE_INDEX 2
#define TIMER2_MODULE_INDEX 3
#define ADC_MODULE_INDEX 4

#define LOWEST_ANALOG_PIN_NUMBER PORTC_START_PIN

#define DEFAULT_VCC 5.0
#define MIN_INPUT_HIGHT_VOLTAGE_PERCENTAGE  0.7
#define MAX_INPUT_LOW_VOLTAGE_PERCENTAGE  0.1

#define N_PINS 28

ATMega328P::ATMega328P() :
        dataMemory(this),
        adc(dataMemory) {

    srand(time(nullptr));

    auto *cpu = new AVRCPU_AVRe(programMemory, dataMemory);
    //Cofigure CPU to ATMega328P
    //ATmega328P has a 14-bits PC, but the behavior is the same as a 16-bits for the CPU
    cpu->setPCSize(AVRCPU::PCBits::PC16);
    cpu->setIOBaseAddr(0x0020);
    modules[CPU_MODULE_INDEX] = cpu;

    auto *timer0 = new Timer0_ATMega328P(dataMemory);
    modules[TIMER0_MODULE_INDEX] = timer0;

    auto *timer1 = new Timer1_ATMega328P(dataMemory);
    modules[TIMER1_MODULE_INDEX] = timer1;

    auto *timer2 = new Timer2_ATMega328P(dataMemory);
    modules[TIMER2_MODULE_INDEX] = timer2;

    modules[ADC_MODULE_INDEX] = &adc;

    clockFreqHz = DEFAULT_CLOCK_FREQ;
    clockPerNs = 1000000000 / DEFAULT_CLOCK_FREQ;
    vcc = DEFAULT_VCC;
    minInputHight = MIN_INPUT_HIGHT_VOLTAGE_PERCENTAGE * vcc;
    maxInputLow = MAX_INPUT_LOW_VOLTAGE_PERCENTAGE * vcc;
    isRunning = false;

    periodMeasure = new IoPeriods[N_PINS];
    for (int i = 0; i < N_PINS; ++i) {
        clock_gettime(CLOCK_REALTIME, &periodMeasure->dcPeriod);
        clock_gettime(CLOCK_REALTIME, &periodMeasure->freqPeriod);
    }
}

ATMega328P::~ATMega328P() {
    ATMega328P::stop();

    for (auto &module : modules) {
        delete module;
    }

    delete[] periodMeasure;
}

void ATMega328P::start() {
    if (!isRunning) {
        isRunning = true;
        for (unsigned int &i : syncCounter) {
            i = clockFreqHz;
        }

        for (int i = 0; i < NUM_MODULES; ++i) {
            scheduler[i] = thread(&ATMega328P::moduleThread, this, i);
        }
        syncThread = thread(&ATMega328P::syncronizationThread, this);
    }
}

void ATMega328P::stop() {
    if (isRunning) {
        isRunning = false;
        for (auto &i : scheduler) {
            i.join();
        }
        syncThread.join();
    }
}

void ATMega328P::load(int fd) {
    switch (programMemory.loadFile(fd)) {
        case INTEL_CHECKSUM_ERROR:
            addNotification(CHECKSUM_ERROR_LISTENER);
            break;
        case INTEL_INVALID_FILE:
            addNotification(INVALID_FILE_LISTENER);
            break;
        case INTEL_FILE_OPEN_FAILED:
            addNotification(FILE_OPEN_FAIL_LISTENER);
            break;
        default:
            addNotification(LOAD_SUCCESS_LISTENER);
            break;
    }
}

void ATMega328P::signalInput(int pin, float voltage) {
    if (isDigitalInput(pin)) {
        dataMemory.setDigitalInput(pin, getLogicState(pin, voltage));
    }
    /*
     * No else here because even if an analog input is enabled,
     * DIDR0 (Digital Input Disable Register 0) may say that the value
     * should also be sent to digital buffer
     */
    if (isAnalogInput(pin)) {
        //TODO: Handle voltage in mV to avoid using float
        adc.setAnalogInput(pin - LOWEST_ANALOG_PIN_NUMBER, voltage);
    }
}

bool ATMega328P::isDigitalInput(int pin) {
    if (pin < 0) {
        return false;
    } else if (pin < LOWEST_ANALOG_PIN_NUMBER) {
        return true;
    } else {
        return !dataMemory.isDigitalInputDisabled(pin - LOWEST_ANALOG_PIN_NUMBER);
    }
}

bool ATMega328P::isAnalogInput(int pin) {
    return pin >= LOWEST_ANALOG_PIN_NUMBER;
}

bool ATMega328P::getLogicState(int pin, float voltage) {
    if (voltage <= maxInputLow) {
        return false;
    } else if (voltage >= minInputHight) {
        return true;
    } else {
        if (dataMemory.isPullUpDisabled(pin)) {
            return rand() % 2;
        } else {
            return true;
        }
    }
}

list<pair<int, string>> *ATMega328P::getNotifications() {
    list<pair<int, string>> *notificationList = GenericDevice::getNotifications();
    sbyte byte;

    //IO Configuration
    dataMemory.read(DDRB_ADDR, &byte);
    notificationList->emplace_back(IO_CONFIGURE_LISTENER,
                                   to_string(DDRB_ADDR) + ":" + to_string(byte));
    dataMemory.read(DDRC_ADDR, &byte);
    notificationList->emplace_back(IO_CONFIGURE_LISTENER,
                                   to_string(DDRC_ADDR) + ":" + to_string(byte));
    dataMemory.read(DDRD_ADDR, &byte);
    notificationList->emplace_back(IO_CONFIGURE_LISTENER,
                                   to_string(DDRD_ADDR) + ":" + to_string(byte));

    //IO State
    dataMemory.read(PORTB_ADDR, &byte);
    notificationList->emplace_back(IO_CHANGED_LISTENER,
                                   to_string(PORTB_ADDR) + ":" + to_string(byte));
    dataMemory.read(PORTC_ADDR, &byte);
    notificationList->emplace_back(IO_CHANGED_LISTENER,
                                   to_string(PORTC_ADDR) + ":" + to_string(byte));
    dataMemory.read(PORTD_ADDR, &byte);
    notificationList->emplace_back(IO_CHANGED_LISTENER,
                                   to_string(PORTD_ADDR) + ":" + to_string(byte));

    return notificationList;
}

void ATMega328P::moduleThread(int index) {
    while (isRunning) {
        modules[index]->run();
        syncCounter[index]--;
        while (!syncCounter[index]) { usleep(1000); }
    }
    syncCounter[index] = 0;
}

void ATMega328P::syncronizationThread() {
    unsigned int finishCondition[NUM_MODULES];
    unsigned int initialCondition[NUM_MODULES];

    for (unsigned int &i : initialCondition) {
        i = clockFreqHz;
    }
    memset(finishCondition, 0, sizeof(finishCondition));

    while (true) {
        while (memcmp(syncCounter, finishCondition, sizeof(syncCounter)) != 0) {
            simulatedTime.tv_nsec = clockPerNs * (clockFreqHz - syncCounter[0]);
            usleep(1000);
        }
        if (!isRunning) { break; }
        simulatedTime.tv_sec++;
        simulatedTime.tv_nsec = 0;
        memcpy(syncCounter, initialCondition, sizeof(syncCounter));
    }
}

int ATMega328P::getPinNumber(smemaddr16 addr, int position) {
    return dataMemory.getPinNumber(addr, position);
}

