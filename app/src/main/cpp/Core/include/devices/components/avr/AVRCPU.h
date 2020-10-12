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
    smemaddr16 sregAddr;
    smemaddr16 stackLAddr, stackHAddr;

    /**********Auxiliar for processing************/
    sbyte regD, regR, sreg, result;
    smemaddr16 regDAddr, regRAddr, wbAddr;

    sbyte offset, dataL, dataH;
    smemaddr16 dataLAddr, dataHAddr;
    sword16 outData, immediate;
    sword16 testJMP_CALL, testLDS_STS;

    smemaddr16 stackPointer;
    spc32 jumpValue;

    sbyte regD_and_regR;
    sbyte notRegD_and_regR;
    sbyte regR_and_result;
    sbyte immediate_and_result;
    sbyte not_result, not_regD;
    sbyte hc_flag;
    /*********************************************/

    void setupInstructionDecoder();

    void instruction_ADC_ROL();

    void instruction_ADD_LSL();

    void instruction_ADIW();

    void instruction_AND_TST();

    void instruction_ANDI_CBR();

    void instruction_ASR();

    void instruction_BCLR_CLC_CLH_CLI_CLN_CLS_CLT_CLV_CLZ();

    void instruction_BLD();

    void instruction_BREAK();

    void instruction_BRBC_BRCC_BRGE_BRHC_BRID_BRNE_BRPL_BRSH_BRTC_BRVC();

    void instruction_BRBS_BRCS_BREQ_BRHS_BRIE_BRLO_BRLT_BRMI_BRTS_BRVS();

    void instruction_BSET_SEC();

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

    void instruction_JMP();

    void instruction_LAC();

    void instruction_LAS();

    void instruction_LAT();

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

    void instruction_LDI();

    void instruction_LDS();
    void instruction_LDS16();

    void instruction_LPM_Z_UNCHANGED_DEST_R0();
    void instruction_LPM_Z_UNCHANGED();
    void instruction_LPM_Z_POST_INCREMENT();

    void instruction_LSR();

    void instruction_MOV();

    void instruction_MOVW();

    void instruction_MUL();

    void instruction_MULS();

    void instruction_MULSU();

    void instruction_NEG();

    void instruction_NOP();

    void instruction_OR();

    void instruction_ORI_SBR();

    void instruction_OUT();

    void instruction_POP();

    void instruction_PUSH();

    void instruction_RCALL();

    void instruction_RET();

    void instruction_RETI();

    void instruction_RJMP();

    void instruction_ROR();

    void instruction_SBC();

    void instruction_SBCI();

    void instruction_SBI();

    void instruction_SBIC();

    void instruction_SBIS();

    void instruction_SBIW();

    void instruction_SBRC();

    void instruction_SBRS();

    void instruction_SLEEP();

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
