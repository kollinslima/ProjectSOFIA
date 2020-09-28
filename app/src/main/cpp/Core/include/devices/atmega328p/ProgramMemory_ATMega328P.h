//
// Created by kollins on 12/09/20.
//

#ifndef PROJECTSOFIA_PROGRAMMEMORY_ATMEGA328P_H
#define PROJECTSOFIA_PROGRAMMEMORY_ATMEGA328P_H

#include "../components/generic/GenericProgramMemory.h"

#include<string>
using namespace std;

class ProgramMemory_ATMega328P : public GenericProgramMemory {

    public:
        ProgramMemory_ATMega328P();
        ~ProgramMemory_ATMega328P();

        bool loadFile(int fd);

        bool write(unsigned long addr, void *data) override;
        bool read(unsigned long addr, void *data) override;
        unsigned long getSize() override;

        bool loadInstruction(unsigned long pc, void *data) override;
};


#endif //PROJECTSOFIA_PROGRAMMEMORY_ATMEGA328P_H
