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

int ProgramMemory_ATMega328P::loadFile(int fd) {
    LOGI(SOFIA_PROGRAM_MEMORY_ATMEGA328P_TAG, "Loading program memory...");
    return IntelParser::parse(fd, this);
}

bool ProgramMemory_ATMega328P::loadInstruction(spc32 pc, void *data) {
    LOGD(SOFIA_PROGRAM_MEMORY_ATMEGA328P_TAG, "Loading instruction -> PC: %X", pc);
    smemaddr16 instAddrBegin = SAFE_ADDR((pc<<1), MEMORY_SIZE);  //PC points to the next instruction, and each instruction has 2 bytes
    *(static_cast<sword16 *>(data)) = (buffer[instAddrBegin+1] << 8) | buffer[instAddrBegin];
    return true;
}

bool ProgramMemory_ATMega328P::write(smemaddr16 addr, void *data) {
    buffer[SAFE_ADDR(addr, MEMORY_SIZE)] = *(static_cast<sbyte *>(data));
    return true;
}

bool ProgramMemory_ATMega328P::read(smemaddr16 addr, void *data) {
    *(static_cast<sbyte *>(data)) = buffer[SAFE_ADDR(addr, MEMORY_SIZE)];
    return true;
}

smemaddr16 ProgramMemory_ATMega328P::getSize() {
    return size;
}


