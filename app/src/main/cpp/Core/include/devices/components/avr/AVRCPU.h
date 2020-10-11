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

    sword16 instruction;
    smemaddr sregAddr;
    smemaddr stackLAddr, stackHAddr;

    /**********Auxiliar for processing************/
    sbyte regD, regR, sreg, result;
    smemaddr wbAddr;

    sbyte offset, dataL, dataH;
    sword16 outData, immediate;
    sword16 testJMP_CALL, testLDS_STS;

    smemaddr stackPointer;
    spc jumpValue;

    sbyte regD_and_regR;
    sbyte regR_and_result;
    sbyte immediate_and_result;
    sbyte not_result, not_regD;
    sbyte hc_flag;
    /*********************************************/

    void setupInstructionDecoder();

    void instruction_ADC();

    void instruction_ADD();

    void instruction_ADIW();

    void instruction_AND_TST();

    void instruction_ANDI_CBR();

    void instruction_ASR();

    void instruction_BCLR_CLC_CLH_CLI_CLN_CLS_CLT_CLV_CLZ();

    void instruction_BLD();

    void instruction_BREAK();

    void instruction_BRBC_BRCC_BRGE_BRHC_BRID_BRNE_BRPL_BRSH_BRTC_BRVC();

    void instruction_BRBS_BRCS_BREQ_BRHS_BRIE_BRLO_BRLT_BRMI_BRTS_BRVS();

    void instruction_BSET();

    void instruction_BST();

    void instruction_CALL();

    void instruction_CBI();

    void instruction_CLR_EOR();

    void instruction_COM();

    void instruction_CP();

    void instruction_CPC();

    void instruction_CPI();

    void instruction_CPSE();

    void instruction_DEC();

    void instruction_DES();

    void instruction_EICALL();

    void instruction_EIJMP();

    void instruction_ELPM1();
    void instruction_ELPM2();
    void instruction_ELPM3();

    void instruction_FMUL();

    void instruction_FMULS();

    void instruction_FMULSU();

    void instruction_ICALL();

    void instruction_IJMP();

    void instruction_IN();

    void instruction_INC();

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
