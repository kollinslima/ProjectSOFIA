//
// Created by kollins on 12/09/20.
//

#include "../../../include/devices/atmega328p/ProgramMemory_ATMega328P.h"
#include "../../../include/CommonCore.h"
#include "../../../include/parsers/IntelParser.h"

#define SOFIA_PROGRAM_MEMORY_ATMEGA328P_TAG "SOFIA PROGRAM MEMORY ATMEGA328P"

#define MEMORY_SIZE 32768   //32kB

ProgramMemory_ATMega328P::ProgramMemory_ATMega328P() {
    size = MEMORY_SIZE;
    buffer = new sbyte[size];
}

ProgramMemory_ATMega328P::~ProgramMemory_ATMega328P() {
    delete [] buffer;
};

bool ProgramMemory_ATMega328P::loadFile(int fd) {
    LOGI(SOFIA_PROGRAM_MEMORY_ATMEGA328P_TAG, "Loading program memory...");
    return IntelParser::parse(fd, this);
}

bool ProgramMemory_ATMega328P::loadInstruction(unsigned long pc, void *data) {
//    LOGD(SOFIA_PROGRAM_MEMORY_ATMEGA328P_TAG, "Loading instruction -> PC: %X", pc);
    unsigned int instAddrBegin = pc*2;  //PC points to the next instruction, and each instruction has 2 bytes
    *(static_cast<sword16 *>(data)) = (buffer[instAddrBegin+1] << 8) | buffer[instAddrBegin];
    return true;
}

bool ProgramMemory_ATMega328P::write(unsigned long addr, void *data) {
//    if (addr < this->size) {
    buffer[addr] = *(static_cast<sbyte *>(data));
    return true;
//    }
//    return false;
}

bool ProgramMemory_ATMega328P::read(unsigned long addr, void *data) {
//    if (addr < this->size) {
    *(static_cast<sbyte *>(data)) = buffer[addr];
    return true;
//    }
//    return false;
}

unsigned long ProgramMemory_ATMega328P::getSize() {
    return size;
}


