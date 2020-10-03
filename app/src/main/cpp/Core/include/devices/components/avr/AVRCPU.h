//
// Created by kollins on 19/09/20.
//

#ifndef PROJECTSOFIA_AVRCPU_H
#define PROJECTSOFIA_AVRCPU_H


#include "../generic/GenericCPU.h"
#include "../generic/GenericMemory.h"
#include "../generic/GenericProgramMemory.h"
#include "GenericAVRDataMemory.h"

#define INSTRUCTION_DECODER_SIZE 65536 //2^16

class AVRCPU : public GenericCPU {

public:
    AVRCPU(GenericProgramMemory *programMemory, GenericAVRDataMemory *dataMemory);

    virtual ~AVRCPU();

    void run();

private:
    typedef void (AVRCPU::*Instruction)();

    Instruction instructionDecoder[INSTRUCTION_DECODER_SIZE];

    GenericProgramMemory *progMem;
    GenericAVRDataMemory *datMem;

    unsigned short instruction;
    sbyte sregAddr;

    //Auxiliar for processing
    sbyte regD, regR, sreg, result, wbAddr;
    sbyte offset, dataL, dataH;
    sword16 outData;

    sbyte regD_and_regR;
    sbyte not_result;
    sbyte hc_flag;

    void setupInstructionDecoder();

    void instructionADC();

    void instructionADD();

    void instructionADIW();

    void instructionAND();     //AND - TST
    void instructionANDI();

    void instructionASR();

    void instructionBCLR();

    void instructionBLD();

    void instructionBRBC();

    void instructionBRBS();

    void instructionBREAK();

    void instructionBSET();

    void instructionBST();

    void instructionCALL();

    void instructionCBI();

    void instructionCOM();

    void instructionCP();

    void instructionCPC();

    void instructionCPI();

    void instructionCPSE();

    void instructionDEC();

    void instructionEOR(); //EOR - CLR

    void instructionFMUL();

    void instructionFMULS();

    void instructionFMULSU();

    void instructionICALL();

    void instructionIJMP();

    void instructionIN();

    void instructionINC();

    void instructionJMP();

    void instructionLD_X_POST_INCREMENT();

    void instructionLD_X_PRE_INCREMENT();

    void instructionLD_X_UNCHANGED();

    void instructionLD_Y_POST_INCREMENT();

    void instructionLD_Y_PRE_INCREMENT();

    void instructionLD_Y_UNCHANGED();

    void instructionLD_Z_POST_INCREMENT();

    void instructionLD_Z_PRE_INCREMENT();

    void instructionLD_Z_UNCHANGED();

    void instructionLDD_Y();

    void instructionLDD_Z();

    void instructionLDI(); //LDI - SER
    void instructionLDS();

    void instructionLPM_Z_POST_INCREMENT();

    void instructionLPM_Z_UNCHANGED_DEST_R();

    void instructionLPM_Z_UNCHANGED();

    void instructionLSR();

    void instructionMOV();

    void instructionMOVW();

    void instructionMUL();

    void instructionMULS();

    void instructionMULSU();

    void instructionNEG();

    void instructionNOP();

    void instructionOR();

    void instructionORI();   //ORI - SBR
    void instructionOUT();

    void instructionPOP();

    void instructionPUSH();

    void instructionRCALL();

    void instructionRET();

    void instructionRETI();

    void instructionRJMP();

    void instructionROR();

    void instructionSBC();

    void instructionSBCI();

    void instructionSBI();

    void instructionSBIC();

    void instructionSBIS();

    void instructionSBIW();

    void instructionSBRC();

    void instructionSBRS();

    void instructionSLEEP();

    void instructionSPM();

    void instructionST_X_POST_INCREMENT();

    void instructionST_X_PRE_INCREMENT();

    void instructionST_X_UNCHANGED();

    void instructionST_Y_POST_INCREMENT();

    void instructionST_Y_PRE_INCREMENT();

    void instructionST_Y_UNCHANGED();

    void instructionST_Z_POST_INCREMENT();

    void instructionST_Z_PRE_INCREMENT();

    void instructionST_Z_UNCHANGED();

    void instructionSTD_Y();

    void instructionSTD_Z();

    void instructionSTS();

    void instructionSUB();

    void instructionSUBI();

    void instructionSWAP();

    void instructionWDR();

    void unknownInstruction();
};


#endif //PROJECTSOFIA_AVRCPU_H
