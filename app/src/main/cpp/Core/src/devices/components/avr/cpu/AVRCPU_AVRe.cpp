//
// Created by kollins on 19/09/20.
//

#include "../../../../../include/devices/components/avr/cpu/AVRCPU_AVRe.h"

#define SOFIA_AVRCPU_AVRE_TAG "SOFIA AVRCPU AVRE"

AVRCPU_AVRe::AVRCPU_AVRe(GenericProgramMemory *programMemory, GenericAVRDataMemory *dataMemory):
    AVRCPU(programMemory, dataMemory, AVRCPU::Core::AVRe) {
}

AVRCPU_AVRe::~AVRCPU_AVRe() {
}

void AVRCPU_AVRe::run() {
    if (needExtraCycles == 0) {
        progMem->loadInstruction(pc++, &instruction);
        (this->*instructionDecoder[instruction])();
    } else {
        needExtraCycles--;
    }
}

void AVRCPU_AVRe::instruction_CALL() {
    /*************************CALL***********************/
    LOGD(SOFIA_AVRCPU_AVRE_TAG, "Instruction CALL");
    AVRCPU::instruction_CALL();

    //PC is already in position to go to stack (write little-endian)
    if (pcBits == PC16) {
        datMem->write(stackPointer--, &pc);
        pc = pc >> 8;
        datMem->write(stackPointer--, &pc);

        //Update SPL and SPH
        datMem->write(stackLAddr, &stackPointer);
        stackPointer = stackPointer >> 8;
        datMem->write(stackHAddr, &stackPointer);

        needExtraCycles = 3;
    } else {
        LOGD(SOFIA_AVRCPU_AVRE_TAG, "Instruction CALL - SP update not implemented for this PC size");
    }

    pc = jumpValue;
}

void AVRCPU_AVRe::instruction_CBI() {
    /*************************CBI***********************/
    LOGD(SOFIA_AVRCPU_AVRE_TAG, "Instruction CBI");
    AVRCPU::instruction_CBI();
    needExtraCycles = 1;
}

void AVRCPU_AVRe::instruction_EIJMP() {
    /*************************EIJMP***********************/
    LOGD(SOFIA_AVRCPU_AVRE_TAG, "Instruction EIJMP - Instruction not available for this device");
}

void AVRCPU_AVRe::instruction_ELPM1() {
    /*************************ELPM1***********************/
    LOGD(SOFIA_AVRCPU_AVRE_TAG, "Instruction ELPM1 - Instruction not available for this device");
}

void AVRCPU_AVRe::instruction_ELPM2() {
    /*************************ELPM2***********************/
    LOGD(SOFIA_AVRCPU_AVRE_TAG, "Instruction ELPM2 - Instruction not available for this device");
}

void AVRCPU_AVRe::instruction_ELPM3() {
    /*************************ELPM3***********************/
    LOGD(SOFIA_AVRCPU_AVRE_TAG, "Instruction ELPM3 - Instruction not available for this device");
}

void AVRCPU_AVRe::instruction_ICALL() {
    /*************************ICALL***********************/
    LOGD(SOFIA_AVRCPU_AVRE_TAG, "Instruction ICALL");
    AVRCPU::instruction_ICALL();

    //PC is already in position to go to stack (write little-endian)
    if (pcBits == PC16) {
        datMem->write(stackPointer--, &pc);
        pc = pc >> 8;
        datMem->write(stackPointer--, &pc);

        //Update SPL and SPH
        datMem->write(stackLAddr, &stackPointer);
        stackPointer = stackPointer >> 8;
        datMem->write(stackHAddr, &stackPointer);

        needExtraCycles = 2;
    } else {
        LOGD(SOFIA_AVRCPU_AVRE_TAG,
             "Instruction ICALL - SP update not implemented for this PC size");
    }
    pc = jumpValue;
}

void AVRCPU_AVRe::instruction_LD_X_UNCHANGED() {
    /*************************LD (X UNCHANGED)***********************/
    LOGD(SOFIA_AVRCPU_AVRE_TAG, "Instruction LD (X UNCHANGED)");
    AVRCPU::instruction_LD_X_UNCHANGED();
    needExtraCycles = 1;
}

void AVRCPU_AVRe::instruction_LD_X_POST_INCREMENT(){
    /*************************LD (X POST INCREMENT)***********************/
    LOGD(SOFIA_AVRCPU_AVRE_TAG, "Instruction LD (X POST INCREMENT)");
    AVRCPU::instruction_LD_X_POST_INCREMENT();
    needExtraCycles = 1;
}

void AVRCPU_AVRe::instruction_LD_X_PRE_DECREMENT(){
    /*************************LD (X PRE DECREMENT)***********************/
    LOGD(SOFIA_AVRCPU_AVRE_TAG, "Instruction LD (X PRE DECREMENT)");
    AVRCPU::instruction_LD_X_PRE_DECREMENT();
    needExtraCycles = 1;
}

void AVRCPU_AVRe::instruction_LD_Y_UNCHANGED(){
/*************************LD (Y UNCHANGED)***********************/
    LOGD(SOFIA_AVRCPU_AVRE_TAG, "Instruction LD (Y UNCHANGED)");
    AVRCPU::instruction_LD_Y_UNCHANGED();
    needExtraCycles = 1;
}

void AVRCPU_AVRe::instruction_LD_Y_POST_INCREMENT(){
    /*************************LD (Y POST INCREMENT)***********************/
    LOGD(SOFIA_AVRCPU_AVRE_TAG, "Instruction LD (Y POST INCREMENT)");
    AVRCPU::instruction_LD_Y_POST_INCREMENT();
    needExtraCycles = 1;
}

void AVRCPU_AVRe::instruction_LD_Y_PRE_DECREMENT(){
    /*************************LD (Y PRE DECREMENT)***********************/
    LOGD(SOFIA_AVRCPU_AVRE_TAG, "Instruction LD (Y PRE DECREMENT)");
    AVRCPU::instruction_LD_Y_PRE_DECREMENT();
    needExtraCycles = 1;
}

void AVRCPU_AVRe::instruction_LDD_Y(){
    /*************************LDD (Y)***********************/
    LOGD(SOFIA_AVRCPU_AVRE_TAG, "Instruction LDD (Y)");
    AVRCPU::instruction_LDD_Y();
    needExtraCycles = 1;
}

void AVRCPU_AVRe::instruction_LD_Z_UNCHANGED(){
    /*************************LD (Z UNCHANGED)***********************/
    LOGD(SOFIA_AVRCPU_AVRE_TAG, "Instruction LD (Z UNCHANGED)");
    AVRCPU::instruction_LD_Z_UNCHANGED();
    needExtraCycles = 1;
}

void AVRCPU_AVRe::instruction_LD_Z_POST_INCREMENT(){
    /*************************LD (Z POST INCREMENT)***********************/
    LOGD(SOFIA_AVRCPU_AVRE_TAG, "Instruction LD (Z POST INCREMENT)");
    AVRCPU::instruction_LD_Z_POST_INCREMENT();
    needExtraCycles = 1;
}

void AVRCPU_AVRe::instruction_LD_Z_PRE_DECREMENT(){
    /*************************LD (Z PRE DECREMENT)***********************/
    LOGD(SOFIA_AVRCPU_AVRE_TAG, "Instruction LD (Z PRE DECREMENT)");
    AVRCPU::instruction_LD_Z_PRE_DECREMENT();
    needExtraCycles = 1;
}

void AVRCPU_AVRe::instruction_LDD_Z(){
    /*************************LDD (Z)***********************/
    LOGD(SOFIA_AVRCPU_AVRE_TAG, "Instruction LDD (Z)");
    AVRCPU::instruction_LDD_Z();
    needExtraCycles = 1;
}

void AVRCPU_AVRe::instruction_PUSH(){
    /*************************PUSH***********************/
    LOGD(SOFIA_AVRCPU_AVRE_TAG, "Instruction PUSH");
    AVRCPU::instruction_PUSH();
    needExtraCycles = 1;
}

void AVRCPU_AVRe::instruction_RCALL() {
    /*************************RCALL***********************/
    LOGD(SOFIA_AVRCPU_AVRE_TAG, "Instruction RCALL");
    AVRCPU::instruction_RCALL();

    //PC is already in position to go to stack (write little-endian)
    if (pcBits == PC16) {
        datMem->write(stackPointer--, &pc);
        pc = pc >> 8;
        datMem->write(stackPointer--, &pc);

        //Update SPL and SPH
        datMem->write(stackLAddr, &stackPointer);
        stackPointer = stackPointer >> 8;
        datMem->write(stackHAddr, &stackPointer);

        needExtraCycles = 2;
    } else {
        LOGD(SOFIA_AVRCPU_AVRE_TAG,
             "Instruction RCALL - SP update not implemented for this PC size");
    }

    pc += (((__int32_t) jumpValue) << 20) >> 20; //Cast to make sign extension
}

void AVRCPU_AVRe::instruction_RET() {
    /*************************RET***********************/
    LOGD(SOFIA_AVRCPU_AVRE_TAG, "Instruction RET");
    AVRCPU::instruction_RET();

    if (pcBits == PC16) {
        //PC (read little-endian)
        datMem->read(++stackPointer, &dataH);
        datMem->read(++stackPointer, &dataL);
        pc = (dataH << 8) | dataL;

        //Update SPL and SPH
        datMem->write(stackLAddr, &stackPointer);
        stackPointer = stackPointer >> 8;
        datMem->write(stackHAddr, &stackPointer);

        needExtraCycles = 3;
    } else {
        LOGD(SOFIA_AVRCPU_AVRE_TAG,
             "Instruction RET - SP update not implemented for this PC size");
    }
}

void AVRCPU_AVRe::instruction_RETI() {
    /*************************RETI***********************/
    LOGD(SOFIA_AVRCPU_AVRE_TAG, "Instruction RETI");
    AVRCPU::instruction_RETI();

    datMem->read(sregAddr, &sreg);
    sreg |= I_FLAG_MASK;
    datMem->write(sregAddr, &sreg);

    if (pcBits == PC16) {
        //PC (read little-endian)
        datMem->read(++stackPointer, &dataH);
        datMem->read(++stackPointer, &dataL);
        pc = (dataH << 8) | dataL;

        //Update SPL and SPH
        datMem->write(stackLAddr, &stackPointer);
        stackPointer = stackPointer >> 8;
        datMem->write(stackHAddr, &stackPointer);

        needExtraCycles = 3;
    } else {
        LOGD(SOFIA_AVRCPU_AVRE_TAG,
             "Instruction RETI - SP update not implemented for this PC size");
    }
}

void AVRCPU_AVRe::instruction_SBI() {
    /*************************SBII***********************/
    LOGD(SOFIA_AVRCPU_AVRE_TAG, "Instruction SBII");
    AVRCPU::instruction_SBI();
    needExtraCycles = 1;
}

void AVRCPU_AVRe::instruction_ST_X_UNCHANGED() {
    /*************************ST (X UNCHANGED)***********************/
    LOGD(SOFIA_AVRCPU_AVRE_TAG, "Instruction ST (X UNCHANGED)");
    AVRCPU::instruction_ST_X_UNCHANGED();
    needExtraCycles = 1;
}

void AVRCPU_AVRe::instruction_ST_X_POST_INCREMENT(){
    /*************************ST (X POST INCREMENT)***********************/
    LOGD(SOFIA_AVRCPU_AVRE_TAG, "Instruction ST (X POST INCREMENT)");
    AVRCPU::instruction_ST_X_POST_INCREMENT();
    needExtraCycles = 1;
}

void AVRCPU_AVRe::instruction_ST_X_PRE_DECREMENT(){
    /*************************ST (X PRE DECREMENT)***********************/
    LOGD(SOFIA_AVRCPU_AVRE_TAG, "Instruction ST (X PRE DECREMENT)");
    AVRCPU::instruction_ST_X_PRE_DECREMENT();
    needExtraCycles = 1;
}

void AVRCPU_AVRe::instruction_ST_Y_UNCHANGED(){
/*************************ST (Y UNCHANGED)***********************/
    LOGD(SOFIA_AVRCPU_AVRE_TAG, "Instruction ST (Y UNCHANGED)");
    AVRCPU::instruction_ST_Y_UNCHANGED();
    needExtraCycles = 1;
}

void AVRCPU_AVRe::instruction_ST_Y_POST_INCREMENT(){
    /*************************ST (Y POST INCREMENT)***********************/
    LOGD(SOFIA_AVRCPU_AVRE_TAG, "Instruction ST (Y POST INCREMENT)");
    AVRCPU::instruction_ST_Y_POST_INCREMENT();
    needExtraCycles = 1;
}

void AVRCPU_AVRe::instruction_ST_Y_PRE_DECREMENT(){
    /*************************ST (Y PRE DECREMENT)***********************/
    LOGD(SOFIA_AVRCPU_AVRE_TAG, "Instruction ST (Y PRE DECREMENT)");
    AVRCPU::instruction_ST_Y_PRE_DECREMENT();
    needExtraCycles = 1;
}

void AVRCPU_AVRe::instruction_STD_Y(){
    /*************************STD (Y)***********************/
    LOGD(SOFIA_AVRCPU_AVRE_TAG, "Instruction STD (Y)");
    AVRCPU::instruction_STD_Y();
    needExtraCycles = 1;
}

void AVRCPU_AVRe::instruction_ST_Z_UNCHANGED(){
    /*************************ST (Z UNCHANGED)***********************/
    LOGD(SOFIA_AVRCPU_AVRE_TAG, "Instruction ST (Z UNCHANGED)");
    AVRCPU::instruction_ST_Z_UNCHANGED();
    needExtraCycles = 1;
}

void AVRCPU_AVRe::instruction_ST_Z_POST_INCREMENT(){
    /*************************ST (Z POST INCREMENT)***********************/
    LOGD(SOFIA_AVRCPU_AVRE_TAG, "Instruction ST (Z POST INCREMENT)");
    AVRCPU::instruction_ST_Z_POST_INCREMENT();
    needExtraCycles = 1;
}

void AVRCPU_AVRe::instruction_ST_Z_PRE_DECREMENT(){
    /*************************ST (Z PRE DECREMENT)***********************/
    LOGD(SOFIA_AVRCPU_AVRE_TAG, "Instruction ST (Z PRE DECREMENT)");
    AVRCPU::instruction_ST_Z_PRE_DECREMENT();
    needExtraCycles = 1;
}

void AVRCPU_AVRe::instruction_STD_Z(){
    /*************************STD (Z)***********************/
    LOGD(SOFIA_AVRCPU_AVRE_TAG, "Instruction STD (Z)");
    AVRCPU::instruction_STD_Z();
    needExtraCycles = 1;
}