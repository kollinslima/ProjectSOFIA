//
// Created by kollins on 27/09/20.
//

#include "../../../include/devices/atmega328p/DataMemory_ATMega328P.h"

#define SDRAM_EXTERNAL_SIZE 2048    //2kB external SDRAM
#define REGISTERS 32                //32 Registers
#define IO_REGISTERS 64             //64 I/O Registers
#define EXT_IO_REGISTERS 160        //160 External I/O Registers

#define MEMORY_SIZE (SDRAM_EXTERNAL_SIZE + REGISTERS + IO_REGISTERS + EXT_IO_REGISTERS)

#define SREG_ADDR 0x5F

DataMemory_ATMega328P::DataMemory_ATMega328P() {
    size = MEMORY_SIZE;
    buffer = new sbyte[size];
}

DataMemory_ATMega328P::~DataMemory_ATMega328P() {
    delete [] buffer;
}

bool DataMemory_ATMega328P::write(unsigned long addr, void *data) {
//    if (addr < this->size) {
    buffer[addr] = *(static_cast<sbyte *>(data));
    return true;
//    }
//    return false;
}

bool DataMemory_ATMega328P::read(unsigned long addr, void *data) {
//    if (addr < this->size) {
    *(static_cast<sbyte *>(data)) = buffer[addr];
    return true;
//    }
//    return false;
}

unsigned long DataMemory_ATMega328P::getSize() {
    return size;
}

unsigned long DataMemory_ATMega328P::getSREGAddres() {
    return 0;
}
