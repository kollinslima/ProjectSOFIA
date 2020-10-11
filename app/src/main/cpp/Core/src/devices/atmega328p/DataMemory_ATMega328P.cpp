//
// Created by kollins on 27/09/20.
//

#include "../../../include/devices/atmega328p/DataMemory_ATMega328P.h"

#define SDRAM_EXTERNAL_SIZE 2048    //2kB external SDRAM
#define REGISTERS 32                //32 Registers
#define IO_REGISTERS 64             //64 I/O Registers
#define EXT_IO_REGISTERS 160        //160 External I/O Registers

#define MEMORY_SIZE (SDRAM_EXTERNAL_SIZE + REGISTERS + IO_REGISTERS + EXT_IO_REGISTERS)
#define LAST_ADDR 0x08FF

#define SREG_ADDR 0x5F
#define SPL_ADDR 0x5D
#define SPH_ADDR 0x5E

DataMemory_ATMega328P::DataMemory_ATMega328P() {
    size = MEMORY_SIZE;
    buffer = new sbyte[size];
}

DataMemory_ATMega328P::~DataMemory_ATMega328P() {
    delete [] buffer;
}

bool DataMemory_ATMega328P::write(smemaddr16 addr, void *data) {
    buffer[SAFE_ADDR(addr, LAST_ADDR)] = *(static_cast<sbyte *>(data));
    return true;
}

bool DataMemory_ATMega328P::read(smemaddr16 addr, void *data) {
    *(static_cast<sbyte *>(data)) = buffer[SAFE_ADDR(addr, LAST_ADDR)];
    return true;
}

smemaddr16 DataMemory_ATMega328P::getSize() {
    return size;
}

smemaddr16 DataMemory_ATMega328P::getSREGAddres() {
    return SREG_ADDR;
}

smemaddr16 DataMemory_ATMega328P::getSPLAddres() {
    return SPL_ADDR;
}

smemaddr16 DataMemory_ATMega328P::getSPHAddres() {
    return SPH_ADDR;
}
