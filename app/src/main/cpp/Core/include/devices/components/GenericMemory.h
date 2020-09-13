//
// Created by kollins on 12/09/20.
//

#ifndef PROJECTSOFIA_GENERICMEMORY_H
#define PROJECTSOFIA_GENERICMEMORY_H

class GenericMemory {

    public:
        GenericMemory(unsigned int size);
        virtual ~GenericMemory();

        virtual bool write(unsigned int addr, unsigned char data);
        virtual bool read(unsigned int addr, unsigned char *data);
        virtual unsigned int getSize();

    private:
        unsigned int size;
        unsigned char *buffer;
};


#endif //PROJECTSOFIA_GENERICMEMORY_H
