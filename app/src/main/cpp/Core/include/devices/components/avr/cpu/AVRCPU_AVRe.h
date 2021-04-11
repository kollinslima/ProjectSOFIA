//
// Created by kollins on 19/09/20.
//

#ifndef PROJECTSOFIA_AVRCPU_AVRE_H
#define PROJECTSOFIA_AVRCPU_AVRE_H

#include "../../../../../include/devices/components/avr/cpu/AVRCPU.h"

class AVRCPU_AVRe : public AVRCPU {

public:

    AVRCPU_AVRe(GenericProgramMemory *programMemory, GenericAVRDataMemory *dataMemory);
    ~AVRCPU_AVRe();

    void run();

private:
    void checkInterruption();
    void instruction_CALL();
    void instruction_CBI();
    void instruction_EIJMP();
    void instruction_ELPM1();
    void instruction_ELPM2();
    void instruction_ELPM3();
    void instruction_ICALL();
    void instruction_LD_X_UNCHANGED();
    void instruction_LD_X_POST_INCREMENT();
    void instruction_LD_X_PRE_DECREMENT();
    void instruction_LD_Y_UNCHANGED();
    void instruction_LD_Y_POST_INCREMENT();
    void instruction_LD_Y_PRE_DECREMENT();
    void instruction_LDD_Y();
    void instruction_LD_Z_UNCHANGED();
    void instruction_LD_Z_POST_INCREMENT();
    void instruction_LD_Z_PRE_DECREMENT();
    void instruction_LDD_Z();
    void instruction_PUSH();
    void instruction_RCALL();
    void instruction_RET();
    void instruction_RETI();
    void instruction_SBI();
    void instruction_ST_X_UNCHANGED();
    void instruction_ST_X_POST_INCREMENT();
    void instruction_ST_X_PRE_DECREMENT();
    void instruction_ST_Y_UNCHANGED();
    void instruction_ST_Y_POST_INCREMENT();
    void instruction_ST_Y_PRE_DECREMENT();
    void instruction_STD_Y();
    void instruction_ST_Z_UNCHANGED();
    void instruction_ST_Z_POST_INCREMENT();
    void instruction_ST_Z_PRE_DECREMENT();
    void instruction_STD_Z();
};


#endif //PROJECTSOFIA_AVRCPU_AVRE_H
