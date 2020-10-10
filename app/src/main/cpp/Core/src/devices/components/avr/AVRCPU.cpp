//
// Created by kollins on 19/09/20.
//

#include <cstdlib>
#include "../../../../include/devices/components/avr/AVRCPU.h"
#include "../../../../include/CommonCore.h"
#include "../../../../include/devices/components/avr/GenericAVRDataMemory.h"

//ADC, ADD, AND,
//BRBC/BRCC/BRGE/BRHC/BRID/BRNE/BRPL/BRSH/BRTC/BRVC,
//BRBS/BRCS/BREQ/BRHS/BRIE/BRLO/BRLT/BRMI/BRTS/BRVS,
//CLR/EOR, CP, CPC
//TST
#define GROUP1_MASK  0xFC00
//ADIW, CBI
#define GROUP2_MASK  0xFF00
//ANDI/CBR
#define GROUP3_MASK  0xF000
//ASR, COM
#define GROUP4_MASK  0xFE0F
//BCLR/CLC/CLH/CLI/CLN/CLS/CLT/CLV/CLZ, BSET
#define GROUP5_MASK  0xFF8F
//BLD/BST
#define GROUP6_MASK  0xFE08
//CALL
#define GROUP7_MASK  0xFE0E

#define ADC_OPCODE  0x1C00
#define ADD_OPCODE  0x0C00
#define ADIW_OPCODE  0x9600
#define AND_TST_OPCODE  0x2000
#define ANDI_CBR_OPCODE  0x7000
#define ASR_OPCODE  0x9405
#define BCLR_CLC_CLH_CLI_CLN_CLS_CLT_CLV_CLZ_OPCODE  0x9488
#define BLD_OPCODE  0xF800
#define BRBC_BRCC_BRGE_BRHC_BRID_BRNE_BRPL_BRSH_BRTC_BRVC_OPCODE  0xF400
#define BRBS_BRCS_BREQ_BRHS_BRIE_BRLO_BRLT_BRMI_BRTS_BRVS_OPCODE  0xF000
#define BREAK_OPCODE  0x9698
#define BSET_OPCODE  0x9408
#define BST_OPCODE  0xFA00
#define CALL_OPCODE  0x940E
#define CBI_OPCODE  0x9800
#define CLR_EOR_OPCODE  0x2400
#define COM_OPCODE  0x9400
#define CP_OPCODE  0x1400
#define CPC_OPCODE  0x0400
#define INSTRUCTION_CPI_MASK  18
#define INSTRUCTION_CPSE_MASK  19
#define INSTRUCTION_DEC_MASK  20
#define INSTRUCTION_FMUL_MASK  22
#define INSTRUCTION_FMULS_MASK  23
#define INSTRUCTION_FMULSU_MASK  24
#define INSTRUCTION_ICALL_MASK  25
#define INSTRUCTION_IJMP_MASK  26
#define INSTRUCTION_IN_MASK  27
#define INSTRUCTION_INC_MASK  28
#define INSTRUCTION_JMP_MASK  29
#define INSTRUCTION_LD_X_POST_INCREMENT_MASK  30
#define INSTRUCTION_LD_X_PRE_INCREMENT_MASK  31
#define INSTRUCTION_LD_X_UNCHANGED_MASK  32
#define INSTRUCTION_LD_Y_POST_INCREMENT_MASK  33
#define INSTRUCTION_LD_Y_PRE_INCREMENT_MASK  34
#define INSTRUCTION_LD_Y_UNCHANGED_MASK  35
#define INSTRUCTION_LD_Z_POST_INCREMENT_MASK  36
#define INSTRUCTION_LD_Z_PRE_INCREMENT_MASK  37
#define INSTRUCTION_LD_Z_UNCHANGED_MASK  38
#define INSTRUCTION_LDD_Y_MASK  39
#define INSTRUCTION_LDD_Z_MASK  40
#define INSTRUCTION_LDI_MASK  41 //LDI - SER
#define INSTRUCTION_LDS_MASK  42
#define INSTRUCTION_LPM_Z_POST_INCREMENT_MASK  43
#define INSTRUCTION_LPM_Z_UNCHANGED_DEST_R0_MASK  44
#define INSTRUCTION_LPM_Z_UNCHANGED_MASK  45
#define INSTRUCTION_LSR_MASK  46
#define INSTRUCTION_MOV_MASK  47
#define INSTRUCTION_MOVW_MASK  48
#define INSTRUCTION_MUL_MASK  49
#define INSTRUCTION_MULS_MASK  50
#define INSTRUCTION_MULSU_MASK  51
#define INSTRUCTION_NEG_MASK  52
#define INSTRUCTION_NOP_MASK  53
#define INSTRUCTION_OR_MASK  54
#define INSTRUCTION_ORI_MASK  55
#define INSTRUCTION_OUT_MASK  56
#define INSTRUCTION_POP_MASK  57
#define INSTRUCTION_PUSH_MASK  58
#define INSTRUCTION_RCALL_MASK  59
#define INSTRUCTION_RET_MASK  60
#define INSTRUCTION_RETI_MASK  61
#define INSTRUCTION_RJMP_MASK  62
#define INSTRUCTION_ROR_MASK  63
#define INSTRUCTION_SBC_MASK  64
#define INSTRUCTION_SBCI_MASK  65
#define INSTRUCTION_SBI_MASK  66
#define INSTRUCTION_SBIC_MASK  67
#define INSTRUCTION_SBIS_MASK  68
#define INSTRUCTION_SBIW_MASK  69
#define INSTRUCTION_SBRC_MASK  70
#define INSTRUCTION_SBRS_MASK  71
#define INSTRUCTION_SLEEP_MASK  72
#define INSTRUCTION_SPM_MASK  73
#define INSTRUCTION_ST_X_POST_INCREMENT_MASK  74
#define INSTRUCTION_ST_X_PRE_INCREMENT_MASK  75
#define INSTRUCTION_ST_X_UNCHANGED_MASK  76
#define INSTRUCTION_ST_Y_POST_INCREMENT_MASK  77
#define INSTRUCTION_ST_Y_PRE_INCREMENT_MASK  78
#define INSTRUCTION_ST_Y_UNCHANGED_MASK  79
#define INSTRUCTION_ST_Z_POST_INCREMENT_MASK  80
#define INSTRUCTION_ST_Z_PRE_INCREMENT_MASK  81
#define INSTRUCTION_ST_Z_UNCHANGED_MASK  82
#define INSTRUCTION_STD_Y_MASK  83
#define INSTRUCTION_STD_Z_MASK  84
#define INSTRUCTION_STS_MASK  85
#define INSTRUCTION_SUB_MASK  86
#define INSTRUCTION_SUBI_MASK  87
#define INSTRUCTION_SWAP_MASK  88
#define INSTRUCTION_WDR_MASK  89

#define T_FLAG_MASK 0x40
#define H_FLAG_MASK 0x20
#define S_FLAG_MASK 0x10
#define V_FLAG_MASK 0x08
#define N_FLAG_MASK 0x04
#define Z_FLAG_MASK 0x02
#define C_FLAG_MASK 0x01

#define REG16_ADDR 0x10
#define REG24_ADDR 0x18
#define REG25_ADDR 0x19

#define IOREG_BASEADDR 0x20

#define SOFIA_AVRCPU_TAG "SOFIA AVRCPU CONTROLLER"

AVRCPU::AVRCPU(GenericProgramMemory *programMemory, GenericAVRDataMemory *dataMemory) {
    pc = 0;
    progMem = programMemory;
    datMem = dataMemory;

    sregAddr = datMem->getSREGAddres();
    stackLAddr = datMem->getSPLAddres();
    stackHAddr = datMem->getSPHAddres();

    setupInstructionDecoder();
}

AVRCPU::~AVRCPU() {
}

void AVRCPU::setupInstructionDecoder() {
    for (int i = 0; i < INSTRUCTION_DECODER_SIZE; ++i) {
        switch (i&GROUP1_MASK) {
            case ADC_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_ADC;
                continue;
            case ADD_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_ADD;
                continue;
            case AND_TST_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_AND_TST;
                continue;
            case BRBC_BRCC_BRGE_BRHC_BRID_BRNE_BRPL_BRSH_BRTC_BRVC_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_BRBC_BRCC_BRGE_BRHC_BRID_BRNE_BRPL_BRSH_BRTC_BRVC;
                continue;
            case BRBS_BRCS_BREQ_BRHS_BRIE_BRLO_BRLT_BRMI_BRTS_BRVS_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_BRBS_BRCS_BREQ_BRHS_BRIE_BRLO_BRLT_BRMI_BRTS_BRVS;
                continue;
            case CLR_EOR_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_CLR_EOR;
                continue;
            case CP_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_CP;
                continue;
            case CPC_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_CPC;
                continue;
        }
        switch (i&GROUP2_MASK) {
            case ADIW_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_ADIW;
                continue;
            case CBI_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_CBI;
                continue;
        }
        switch (i&GROUP3_MASK) {
            case ANDI_CBR_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_ANDI_CBR;
                continue;
        }
        switch (i&GROUP4_MASK) {
            case ASR_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_ASR;
                continue;
            case COM_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_COM;
                continue;
        }
        switch (i&GROUP5_MASK) {
            case BCLR_CLC_CLH_CLI_CLN_CLS_CLT_CLV_CLZ_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_BCLR_CLC_CLH_CLI_CLN_CLS_CLT_CLV_CLZ;
                continue;
            case BSET_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_BSET;
                continue;
        }
        switch (i&GROUP6_MASK) {
            case BLD_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_BLD;
                continue;
            case BST_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_BST;
                continue;
        }
        switch (i&GROUP7_MASK) {
            case CALL_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_CALL;
                continue;
        }
        if (i == BREAK_OPCODE) {
            instructionDecoder[i] = &AVRCPU::instruction_BREAK;
            continue;
        }
        instructionDecoder[i] = &AVRCPU::unknownInstruction;
    }
}

void AVRCPU::run() {
    progMem->loadInstruction(pc++, &instruction);
    (this->*instructionDecoder[instruction])();
    pc = 0;
}

void AVRCPU::instruction_ADC() {
    /*************************ADC***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction ADC");

    wbAddr = (0x01F0 & instruction) >> 4;

    datMem->read(wbAddr, &regD);
    datMem->read(((0x0200 & instruction) >> 5) | (0x000F & instruction), &regR);
    datMem->read(sregAddr, &sreg);

    result = regD + regR + (sreg & C_FLAG_MASK);
    sreg &= 0xC0;

    regD_and_regR = regD & regR;
    not_result = ~result;

    hc_flag = regD_and_regR | (regR & not_result) | (not_result & regD);

    //Flag H
    sreg |= (hc_flag << 2) & H_FLAG_MASK;

    //Flag V
    sreg |= (((regD_and_regR & result) | ((~regD) & (~regR) & result)) >> 4) & V_FLAG_MASK;

    //Flag N
    sreg |= (result >> 5) & N_FLAG_MASK;

    //Flag S
    sreg |= (((sreg << 1) ^ sreg) << 1) & S_FLAG_MASK;

    //Flag Z
    sreg |= result?0x00:Z_FLAG_MASK;

    //Flag C
    sreg |= (hc_flag >> 7) & C_FLAG_MASK;

    datMem->write(wbAddr, &result);
    datMem->write(sregAddr, &sreg);

}

void AVRCPU::instruction_ADD() {
    /*************************ADD***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction ADD");

    wbAddr = (0x01F0 & instruction) >> 4;

    datMem->read(wbAddr, &regD);
    datMem->read(((0x0200 & instruction) >> 5) | (0x000F & instruction), &regR);
    datMem->read(sregAddr, &sreg);

    result = regD + regR;
    sreg &= 0xC0;

    regD_and_regR = regD & regR;
    not_result = ~result;

    hc_flag = regD_and_regR | (regR & not_result) | (not_result & regD);

    //Flag H
    sreg |= (hc_flag << 2) & H_FLAG_MASK;

    //Flag V
    sreg |= (((regD_and_regR & result) | ((~regD) & (~regR) & result)) >> 4) & V_FLAG_MASK;

    //Flag N
    sreg |= (result >> 5) & N_FLAG_MASK;

    //Flag S
    sreg |= (((sreg << 1) ^ sreg) << 1) & S_FLAG_MASK;

    //Flag Z
    sreg |= result?0x00:Z_FLAG_MASK;

    //Flag C
    sreg |= (hc_flag >> 7) & C_FLAG_MASK;

    datMem->write(wbAddr, &result);
    datMem->write(sregAddr, &sreg);
}

void AVRCPU::instruction_ADIW() {
    /*************************ADIW***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction ADIW");

    offset = ((0x0030&instruction)>>3); //(>>4)*2 = >>3

    //ADIW operates on the upper four registers pairs
    sbyte dataLAddr = REG24_ADDR + offset;
    sbyte dataHAddr = REG25_ADDR + offset;

    datMem->read(dataLAddr, &dataL);
    datMem->read(dataHAddr + offset, &dataH);
    datMem->read(sregAddr, &sreg);

    outData = (((0x00FF & dataH)<<8) | dataL) + (((0x00C0 & instruction)>>2) | (0x000F & instruction));
    sreg &= 0xE0;

    //Flag V
    sreg |= (((~dataH)>>4)&(outData>>12))&V_FLAG_MASK;

    //Flag N
    sreg |= (outData>>13)&N_FLAG_MASK;

    //Flag S
    sreg |= (((sreg << 1) ^ sreg) << 1) & S_FLAG_MASK;

    //Flag Z
    sreg |= outData?0x00:Z_FLAG_MASK;

    //Flag C
    sreg |= ((dataH>>7)&((~outData)>>15))&C_FLAG_MASK;

    datMem->write(dataLAddr, &outData);
    outData = outData>>8;
    datMem->write(dataHAddr, &outData);
    datMem->write(sregAddr, &sreg);
}

void AVRCPU::instruction_AND_TST() {
    /*************************AND/TST***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction AND/TSL");

    wbAddr = (0x01F0 & instruction) >> 4;

    datMem->read(wbAddr, &regD);
    datMem->read(((0x0200 & instruction) >> 5) | (0x000F & instruction), &regR);
    datMem->read(sregAddr, &sreg);

    result = regD & regR;
    sreg &= 0xE1;

    //Flag N
    sreg |= (result >> 5) & N_FLAG_MASK;

    //Flag S
    sreg |= (((sreg << 1) ^ sreg) << 1) & S_FLAG_MASK;

    //Flag Z
    sreg |= result?0x00:Z_FLAG_MASK;

    datMem->write(wbAddr, &result);
    datMem->write(sregAddr, &sreg);
}

void AVRCPU::instruction_ANDI_CBR() {
    /*************************ANDI***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction ANDI");

    wbAddr = REG16_ADDR | ((0x00F0 & instruction) >> 4);

    datMem->read(wbAddr, &regD);
    datMem->read(sregAddr, &sreg);

    result = regD & (((0x0F00&instruction)>>4)|(0x000F&instruction));
    sreg &= 0xE1;

    //Flag N
    sreg |= (result >> 5) & N_FLAG_MASK;

    //Flag S
    sreg |= (((sreg << 1) ^ sreg) << 1) & S_FLAG_MASK;

    //Flag Z
    sreg |= result?0x00:Z_FLAG_MASK;

    datMem->write(wbAddr, &result);
    datMem->write(sregAddr, &sreg);
}

void AVRCPU::instruction_ASR() {
    /*************************ASR***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction ASR");

    wbAddr = (0x01F0 & instruction) >> 4;

    datMem->read(wbAddr, &regD);
    datMem->read(sregAddr, &sreg);

    result = ((__int8_t)regD)>>1; //Cast to make sign extension
    sreg &= 0xE0;

    //Flag N
    sreg |= (result >> 5) & N_FLAG_MASK;

    //Flag C
    sreg |= regD & C_FLAG_MASK;

    //Flag V
    sreg |= (((sreg << 2) ^ sreg) << 1) & V_FLAG_MASK;

    //Flag S
    sreg |= (((sreg << 1) ^ sreg) << 1) & S_FLAG_MASK;

    //Flag Z
    sreg |= result?0x00:Z_FLAG_MASK;

    datMem->write(wbAddr, &result);
    datMem->write(sregAddr, &sreg);
}

void AVRCPU::instruction_BCLR_CLC_CLH_CLI_CLN_CLS_CLT_CLV_CLZ() {
    /*************************BCLR***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction BCLR");

    datMem->read(sregAddr, &sreg);
    sreg &= ~(0x01 << ((instruction>>4)&0x0007));
    datMem->write(sregAddr, &sreg);
}

void AVRCPU::instruction_BLD() {
    /*************************BLD***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction BLD");

    wbAddr = (0x01F0 & instruction) >> 4;
    datMem->read(wbAddr, &regD);
    datMem->read(sregAddr, &sreg);

    offset = instruction&0x0007;
    regD &= ~(0x01 << offset);
    regD |= ((sreg&T_FLAG_MASK)>>6)<<offset;

    datMem->write(wbAddr, &regD);
}

void AVRCPU::instruction_BRBC_BRCC_BRGE_BRHC_BRID_BRNE_BRPL_BRSH_BRTC_BRVC() {
    /****BRBC/BRCC/BRGE/BRHC/BRID/BRNE/BRPL/BRSH/BRTC/BRVC****/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction BRBC/BRCC/BRGE/BRHC/BRID/BRNE/BRPL/BRSH/BRTC/BRVC");

    datMem->read(sregAddr, &sreg);

    offset = instruction&0x0007;
    pc += (((__int8_t)(instruction&0x03F8))<<6>>9)&(~(((__int8_t )(sreg<<(7-offset)))>>7)); //Cast to make sign extension
}

void AVRCPU::instruction_BRBS_BRCS_BREQ_BRHS_BRIE_BRLO_BRLT_BRMI_BRTS_BRVS() {
    /****BRBS/BRCS/BREQ/BRHS/BRIE/BRLO/BRLT/BRMI/BRTS/BRVS****/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction BRBS/BRCS/BREQ/BRHS/BRIE/BRLO/BRLT/BRMI/BRTS/BRVS");

    datMem->read(sregAddr, &sreg);

    offset = instruction&0x0007;
    pc += (((__int8_t)(instruction&0x03F8))<<6>>9)&(((__int8_t )(sreg<<(7-offset)))>>7); //Cast to make sign extension
}

void AVRCPU::instruction_BREAK() {
    /*************************BREAK***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction BREAK - NOT IMPLEMENTED");
}

void AVRCPU::instruction_BSET() {
    /*************************BSET***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction BSET");

    datMem->read(sregAddr, &sreg);
    sreg |= (0x01 << ((instruction>>4)&0x0007));
    datMem->write(sregAddr, &sreg);
}

void AVRCPU::instruction_BST() {
    /*************************BST***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction BST");

    datMem->read((0x01F0 & instruction) >> 4, &regD);
    datMem->read(sregAddr, &sreg);

    offset = instruction&0x0007;
    sreg &= ~T_FLAG_MASK;
    sreg |= ((regD>>offset)&0x01)<<6;

    datMem->write(sregAddr, &sreg);
}

void AVRCPU::instruction_CALL() {
    /*************************CALL***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction CALL");

    jumpValue = ((instruction&0x01F0)>>3) | (instruction&0x0001);
    progMem->loadInstruction(pc++, &instruction);
    jumpValue = (jumpValue<<16) | instruction;

    datMem->read(stackLAddr, &dataL);
    datMem->read(stackHAddr, &dataH);
    stackPointer = (dataH<<8)|dataL;

    //PC is already in position to go to stack (write little-endian)
    datMem->write(stackPointer--, &pc);
    pc = pc >> 8;
    datMem->write(stackPointer--, &pc);

    //Update SPL and SPH
    datMem->write(stackLAddr, &stackPointer);
    stackPointer = stackPointer >> 8;
    datMem->write(stackHAddr, &stackPointer);

    pc = jumpValue;
}

void AVRCPU::instruction_CBI() {
    /*************************CBI***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction CBI");

    wbAddr = ((instruction&0x00F8)>>3) + IOREG_BASEADDR;
    datMem->read(wbAddr, &result);
    result &= ~(0x01 << (instruction&0x0007));
    datMem->write(wbAddr, &result);
}

void AVRCPU::instruction_CLR_EOR() {
    /*************************CLR/EOR***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction CLR/EOR");

    wbAddr = (0x01F0 & instruction) >> 4;

    datMem->read(wbAddr, &regD);
    datMem->read(((0x0200 & instruction) >> 5) | (0x000F & instruction), &regR);
    datMem->read(sregAddr, &sreg);

    result = regD ^ regR;
    sreg &= 0xE1;

    //Flag N
    sreg |= (result >> 5) & N_FLAG_MASK;

    //Flag S
    sreg |= (((sreg << 1) ^ sreg) << 1) & S_FLAG_MASK;

    //Flag Z
    sreg |= result?0x00:Z_FLAG_MASK;

    datMem->write(wbAddr, &result);
    datMem->write(sregAddr, &sreg);
}

void AVRCPU::instruction_COM() {
    /*************************COM***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction COM");

    wbAddr = (0x01F0 & instruction) >> 4;
    datMem->read(wbAddr, &regD);
    datMem->read(sregAddr, &sreg);

    regD = ~regD;
    sreg &= 0xE0;

    //Flag N
    sreg |= (result >> 5) & N_FLAG_MASK;

    //Flag S
    sreg |= (((sreg << 1) ^ sreg) << 1) & S_FLAG_MASK;

    //Flag Z
    sreg |= result?0x00:Z_FLAG_MASK;

    //Flag C
    sreg |= C_FLAG_MASK;

    datMem->write(wbAddr, &regD);
    datMem->write(sregAddr, &sreg);
}

void AVRCPU::instruction_CP() {
/*************************CP***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction CP");

    wbAddr = (0x01F0 & instruction) >> 4;

    datMem->read(wbAddr, &regD);
    datMem->read(((0x0200 & instruction) >> 5) | (0x000F & instruction), &regR);
    datMem->read(sregAddr, &sreg);

    result = regD - regR;
    sreg &= 0xC0;

    regR_and_result = regR & result;
    not_result = ~result;
    not_regD = ~regD;

    hc_flag = (not_regD & regR) | regR_and_result | (result & not_regD);

    //Flag H
    sreg |= (hc_flag << 2) & H_FLAG_MASK;

    //Flag V
    sreg |= (((regD & (~regR) & not_result) | (not_regD & regR_and_result)) >> 4) & V_FLAG_MASK;

    //Flag N
    sreg |= (result >> 5) & N_FLAG_MASK;

    //Flag S
    sreg |= (((sreg << 1) ^ sreg) << 1) & S_FLAG_MASK;

    //Flag Z
    sreg |= result?0x00:Z_FLAG_MASK;

    //Flag C
    sreg |= (hc_flag >> 7) & C_FLAG_MASK;

    datMem->write(sregAddr, &sreg);
}

void AVRCPU::instruction_CPC() {
    /*************************CPC***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction CPC");

    wbAddr = (0x01F0 & instruction) >> 4;

    datMem->read(wbAddr, &regD);
    datMem->read(((0x0200 & instruction) >> 5) | (0x000F & instruction), &regR);
    datMem->read(sregAddr, &sreg);

    result = regD - regR - (sreg & C_FLAG_MASK);
    sreg &= 0xC2; //Do not clear previous Z flag

    regR_and_result = regR & result;
    not_result = ~result;
    not_regD = ~regD;

    hc_flag = (not_regD & regR) | regR_and_result | (result & not_regD);

    //Flag H
    sreg |= (hc_flag << 2) & H_FLAG_MASK;

    //Flag V
    sreg |= (((regD & (~regR) & not_result) | (not_regD & regR_and_result)) >> 4) & V_FLAG_MASK;

    //Flag N
    sreg |= (result >> 5) & N_FLAG_MASK;

    //Flag S
    sreg |= (((sreg << 1) ^ sreg) << 1) & S_FLAG_MASK;

    //Flag Z
    sreg &= result?(~Z_FLAG_MASK):sreg;

    //Flag C
    sreg |= (hc_flag >> 7) & C_FLAG_MASK;

    datMem->write(sregAddr, &sreg);
}

void AVRCPU::instructionCPI() {

}

void AVRCPU::instructionCPSE() {

}

void AVRCPU::instructionDEC() {

}

void AVRCPU::instructionFMUL() {

}

void AVRCPU::instructionFMULS() {

}

void AVRCPU::instructionFMULSU() {

}

void AVRCPU::instructionICALL() {

}

void AVRCPU::instructionIJMP() {

}

void AVRCPU::instructionIN() {

}

void AVRCPU::instructionINC() {

}

void AVRCPU::instructionJMP() {

}

void AVRCPU::instructionLD_X_POST_INCREMENT() {

}

void AVRCPU::instructionLD_X_PRE_INCREMENT() {

}

void AVRCPU::instructionLD_X_UNCHANGED() {

}

void AVRCPU::instructionLD_Y_POST_INCREMENT() {

}

void AVRCPU::instructionLD_Y_PRE_INCREMENT() {

}

void AVRCPU::instructionLD_Y_UNCHANGED() {

}

void AVRCPU::instructionLD_Z_POST_INCREMENT() {

}

void AVRCPU::instructionLD_Z_PRE_INCREMENT() {

}

void AVRCPU::instructionLD_Z_UNCHANGED() {

}

void AVRCPU::instructionLDD_Y() {

}

void AVRCPU::instructionLDD_Z() {

}

void AVRCPU::instructionLDI() {

}

void AVRCPU::instructionLDS() {

}

void AVRCPU::instructionLPM_Z_POST_INCREMENT() {

}

void AVRCPU::instructionLPM_Z_UNCHANGED_DEST_R() {

}

void AVRCPU::instructionLPM_Z_UNCHANGED() {

}

void AVRCPU::instructionLSR() {

}

void AVRCPU::instructionMOV() {

}

void AVRCPU::instructionMOVW() {

}

void AVRCPU::instructionMUL() {

}

void AVRCPU::instructionMULS() {

}

void AVRCPU::instructionMULSU() {

}

void AVRCPU::instructionNEG() {

}

void AVRCPU::instructionNOP() {

}

void AVRCPU::instructionOR() {

}

void AVRCPU::instructionORI() {

}

void AVRCPU::instructionOUT() {

}

void AVRCPU::instructionPOP() {

}

void AVRCPU::instructionPUSH() {

}

void AVRCPU::instructionRCALL() {

}

void AVRCPU::instructionRET() {

}

void AVRCPU::instructionRETI() {

}

void AVRCPU::instructionRJMP() {

}

void AVRCPU::instructionROR() {

}

void AVRCPU::instructionSBC() {

}

void AVRCPU::instructionSBCI() {

}

void AVRCPU::instructionSBI() {

}

void AVRCPU::instructionSBIC() {

}

void AVRCPU::instructionSBIS() {

}

void AVRCPU::instructionSBIW() {

}

void AVRCPU::instructionSBRC() {

}

void AVRCPU::instructionSBRS() {

}

void AVRCPU::instructionSLEEP() {

}

void AVRCPU::instructionSPM() {

}

void AVRCPU::instructionST_X_POST_INCREMENT() {

}

void AVRCPU::instructionST_X_PRE_INCREMENT() {

}

void AVRCPU::instructionST_X_UNCHANGED() {

}

void AVRCPU::instructionST_Y_POST_INCREMENT() {

}

void AVRCPU::instructionST_Y_PRE_INCREMENT() {

}

void AVRCPU::instructionST_Y_UNCHANGED() {

}

void AVRCPU::instructionST_Z_POST_INCREMENT() {

}

void AVRCPU::instructionST_Z_PRE_INCREMENT() {

}

void AVRCPU::instructionST_Z_UNCHANGED() {

}

void AVRCPU::instructionSTD_Y() {

}

void AVRCPU::instructionSTD_Z() {

}

void AVRCPU::instructionSTS() {

}

void AVRCPU::instructionSUB() {

}

void AVRCPU::instructionSUBI() {

}

void AVRCPU::instructionSWAP() {

}

void AVRCPU::instructionWDR() {

}

void AVRCPU::unknownInstruction() {
//    LOGD(SOFIA_AVRCPU_TAG, "Unknown Instruction");
}


