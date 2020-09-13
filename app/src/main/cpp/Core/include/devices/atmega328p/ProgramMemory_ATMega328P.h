//
// Created by kollins on 12/09/20.
//

#ifndef PROJECTSOFIA_PROGRAMMEMORY_ATMEGA328P_H
#define PROJECTSOFIA_PROGRAMMEMORY_ATMEGA328P_H

#include "../components/GenericMemory.h"

#include<string>
using namespace std;

#define MEMORY_SIZE (32*1024)   //32kB

class ProgramMemory_ATMega328P : public GenericMemory {

    public:
        ProgramMemory_ATMega328P();
        ~ProgramMemory_ATMega328P();

        bool loadFile(int fd);

        unsigned short int loadInstruction(unsigned int pc);
};


#endif //PROJECTSOFIA_PROGRAMMEMORY_ATMEGA328P_H
