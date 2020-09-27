//
// Created by kollins on 12/09/20.
//

#ifndef PROJECTSOFIA_GENERICMEMORY_H
#define PROJECTSOFIA_GENERICMEMORY_H

class GenericMemory {

    public:
        virtual ~GenericMemory() {}

        virtual bool write(unsigned int addr, unsigned char data) = 0;
        virtual bool read(unsigned int addr, unsigned char *data) = 0;
        virtual unsigned int getSize() = 0;

    protected:
        unsigned int size;
        unsigned char *buffer;
};


#endif //PROJECTSOFIA_GENERICMEMORY_H
