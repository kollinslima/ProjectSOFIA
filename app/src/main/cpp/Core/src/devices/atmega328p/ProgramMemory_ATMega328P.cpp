//
// Created by kollins on 12/09/20.
//

#include "../../../include/devices/atmega328p/ProgramMemory_ATMega328P.h"
#include "../../../include/CommonCore.h"
#include "../../../include/parsers/IntelParser.h"

#define SOFIA_PROGRAM_MEMORY_ATMEGA328P_TAG "SOFIA PROGRAM MEMORY ATMEGA328P"

#define MEMORY_SIZE (32*1024)   //32kB

ProgramMemory_ATMega328P::ProgramMemory_ATMega328P() {
    size = MEMORY_SIZE;
    buffer = new unsigned char[size];
}

ProgramMemory_ATMega328P::~ProgramMemory_ATMega328P() {
    delete [] buffer;
};

bool ProgramMemory_ATMega328P::loadFile(int fd) {
    LOGI(SOFIA_PROGRAM_MEMORY_ATMEGA328P_TAG, "Loading program memory...");
    return IntelParser::parse(fd, this);
}

unsigned short int ProgramMemory_ATMega328P::loadInstruction(unsigned int pc) {
//    LOGD(SOFIA_PROGRAM_MEMORY_ATMEGA328P_TAG, "Loading instruction -> PC: %X", pc);
    unsigned int instAddrBegin = pc*2;  //PC points to the next instruction, and each instruction has 2 bytes
    return (buffer[instAddrBegin+1] << 8) | buffer[instAddrBegin];
}

bool ProgramMemory_ATMega328P::write(unsigned int addr, unsigned char data) {
//    if (addr < this->size) {
    buffer[addr] = data;
    return true;
//    }
//    return false;
}

bool ProgramMemory_ATMega328P::read(unsigned int addr, unsigned char *data) {
//    if (addr < this->size) {
    (*data) = buffer[addr];
    return true;
//    }
//    return false;
}

unsigned int ProgramMemory_ATMega328P::getSize() {
    return size;
}


