//
// Created by kollins on 12/09/20.
//

#include "../../../include/devices/atmega328p/ATMega328P.h"

ATMega328P::ATMega328P() {
    this->programMemory = new ProgramMemory_ATMega328P();
}

ATMega328P::~ATMega328P() {
    delete this->programMemory;
}

void ATMega328P::run() {

}

bool ATMega328P::loadFile(int fd) {
    return this->programMemory->loadFile(fd);
}
