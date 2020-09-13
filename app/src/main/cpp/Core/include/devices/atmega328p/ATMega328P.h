//
// Created by kollins on 12/09/20.
//

#ifndef PROJECTSOFIA_ATMEGA328P_H
#define PROJECTSOFIA_ATMEGA328P_H


#include "../GenericDevice.h"
#include "ProgramMemory_ATMega328P.h"

class ATMega328P : public GenericDevice{

    public:
        ATMega328P();
        ~ATMega328P();

        bool loadFile(int fd) override;
        void run() override;

    private:
        ProgramMemory_ATMega328P *programMemory;
};


#endif //PROJECTSOFIA_ATMEGA328P_H
