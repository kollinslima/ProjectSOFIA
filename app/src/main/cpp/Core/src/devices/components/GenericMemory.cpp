//
// Created by kollins on 12/09/20.
//

#include "../../../include/devices/components/GenericMemory.h"

GenericMemory::GenericMemory(unsigned int size) {
    this->buffer = new unsigned char[size];
    this->size = size;
}

GenericMemory::~GenericMemory() {
    delete [] this->buffer;
}

bool GenericMemory::write(unsigned int addr, unsigned char data) {
    if (addr < this->size) {
        this->buffer[addr] = data;
        return true;
    }
    return false;
}

bool GenericMemory::read(unsigned int addr, unsigned char *data) {
    if (addr < this->size) {
        (*data) = this->buffer[addr];
        return true;
    }
    return false;
}

unsigned int GenericMemory::getSize() {
    return this->size;
}
