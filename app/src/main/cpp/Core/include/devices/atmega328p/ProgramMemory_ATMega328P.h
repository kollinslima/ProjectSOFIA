//
// Created by kollins on 12/09/20.
//

#ifndef PROJECTSOFIA_PROGRAMMEMORY_ATMEGA328P_H
#define PROJECTSOFIA_PROGRAMMEMORY_ATMEGA328P_H

#include "../components/GenericMemory.h"

#include<string>
using namespace std;

class ProgramMemory_ATMega328P : public GenericMemory {

    public:
        ProgramMemory_ATMega328P();
        ~ProgramMemory_ATMega328P();

        bool loadFile(int fd);

        bool write(unsigned int addr, unsigned char data) override;
        bool read(unsigned int addr, unsigned char *data) override;
        unsigned int getSize() override;

        unsigned short int loadInstruction(unsigned int pc);
};


#endif //PROJECTSOFIA_PROGRAMMEMORY_ATMEGA328P_H
