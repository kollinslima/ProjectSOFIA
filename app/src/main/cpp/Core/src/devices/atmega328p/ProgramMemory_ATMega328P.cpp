//
// Created by kollins on 12/09/20.
//

#include "../../../include/devices/atmega328p/ProgramMemory_ATMega328P.h"
#include "../../../include/CommonCore.h"
#include "../../../include/parsers/IntelParser.h"

static const char *SOFIA_PROGRAM_MEMORY_ATMEGA328P_TAG = "SOFIA PROGRAM MEMORY ATMEGA328P";

ProgramMemory_ATMega328P::ProgramMemory_ATMega328P() : GenericMemory(MEMORY_SIZE){

}

ProgramMemory_ATMega328P::~ProgramMemory_ATMega328P() = default;

bool ProgramMemory_ATMega328P::loadFile(int fd) {
    LOGI(SOFIA_PROGRAM_MEMORY_ATMEGA328P_TAG, "Loading program memory...");
    return IntelParser::parse(fd, this);
}

unsigned short int ProgramMemory_ATMega328P::loadInstruction(unsigned int pc) {
    LOGD(SOFIA_PROGRAM_MEMORY_ATMEGA328P_TAG, "Loading instruction -> PC: %X", pc);
    unsigned char byte1 = 0x00, byte2 = 0x00;
    unsigned int instAddrBegin = pc*2;  //PC points to the next instruction, and each instruction has 2 bytes
    read(instAddrBegin, &byte1);
    read(instAddrBegin+1, &byte2);
    return (byte2 << 8) | byte1;
}


