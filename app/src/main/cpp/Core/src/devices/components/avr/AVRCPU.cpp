//
// Created by kollins on 19/09/20.
//

#include <cstdlib>
#include "../../../../include/devices/components/avr/AVRCPU.h"
#include "../../../../include/CommonCore.h"
#include "../../../../include/devices/components/avr/GenericAVRDataMemory.h"

//ADC/ROL, ADD/LSL, AND
//BRBC/BRCC/BRGE/BRHC/BRID/BRNE/BRPL/BRSH/BRTC/BRVC
//BRBS/BRCS/BREQ/BRHS/BRIE/BRLO/BRLT/BRMI/BRTS/BRVS
//CLR/EOR, CP, CPC, CPSE
//MOV, MUL
//OR
//SBC
//TST
#define INSTRUCTION_GROUP1_MASK  0xFC00
//ADIW
//CBI
//MOVW, MULS
//SBI, SBIC, SBIS, SBIW
#define INSTRUCTION_GROUP2_MASK  0xFF00
//ANDI/CBR
//CPI
//LDI/SER, LDS (16-bit)
//ORI/SBR
//RCALL, RJMP
//SBCI
#define INSTRUCTION_GROUP3_MASK  0xF000
//ASR
//COM
//NEG
//ROL
#define INSTRUCTION_GROUP4_MASK  0xFE0F
//BCLR/CLC/CLH/CLI/CLN/CLS/CLT/CLV/CLZ,
//BSET/SEC/SEH/SEI/SEN/SES/SET/SEV/SEZ
#define INSTRUCTION_GROUP5_MASK  0xFF8F
//BLD/BST
//SBRC/SBRS
#define INSTRUCTION_GROUP6_MASK  0xFE08
//CALL
//JMP
//LSR
#define INSTRUCTION_GROUP7_MASK  0xFE0E
//DEC
//ELPM2/ELPM3
//INC
//LAC, LAS, LAT, LD(X), LD(Y), LD(Z), LDS, LPM
//POP, PUSH
//STS
#define INSTRUCTION_GROUP8_MASK  0xFE0F
//DES
#define INSTRUCTION_GROUP9_MASK  0xFF0F
//FMUL, FMULS, FMULSU, MULSU
#define INSTRUCTION_GROUP10_MASK  0xFF88
//IN
//OUT
#define INSTRUCTION_GROUP11_MASK  0xF800
//LDD(Y), LDD(Z)
#define INSTRUCTION_GROUP12_MASK  0xD208

#define ADC_ROL_OPCODE                                              0x1C00
#define ADD_LSL_OPCODE                                              0x0C00
#define ADIW_OPCODE                                                 0x9600
#define AND_TST_OPCODE                                              0x2000
#define ANDI_CBR_OPCODE                                             0x7000
#define ASR_OPCODE                                                  0x9405
#define BCLR_CLC_CLH_CLI_CLN_CLS_CLT_CLV_CLZ_OPCODE                 0x9488
#define BLD_OPCODE                                                  0xF800
#define BRBC_BRCC_BRGE_BRHC_BRID_BRNE_BRPL_BRSH_BRTC_BRVC_OPCODE    0xF400
#define BRBS_BRCS_BREQ_BRHS_BRIE_BRLO_BRLT_BRMI_BRTS_BRVS_OPCODE    0xF000
#define BREAK_OPCODE                                                0x9698
#define BSET_SEC_SEH_SEI_SEN_SES_SET_SEV_SEZ_OPCODE                 0x9408
#define BST_OPCODE                                                  0xFA00
#define CALL_OPCODE                                                 0x940E
#define CBI_OPCODE                                                  0x9800
#define CLR_EOR_OPCODE                                              0x2400
#define COM_OPCODE                                                  0x9400
#define CP_OPCODE                                                   0x1400
#define CPC_OPCODE                                                  0x0400
#define CPI_OPCODE                                                  0x3000
#define CPSE_OPCODE                                                 0x1000
#define DEC_OPCODE                                                  0x940A
#define DES_OPCODE                                                  0x940B
#define EICALL_OPCODE                                               0x9519
#define EIJMP_OPCODE                                                0x9419
#define ELPM1_OPCODE                                                0x95D8
#define ELPM2_OPCODE                                                0x9006
#define ELPM3_OPCODE                                                0x9007
#define FMUL_OPCODE                                                 0x0308
#define FMULS_OPCODE                                                0x0380
#define FMULSU_OPCODE                                               0x0388
#define ICALL_OPCODE                                                0x9509
#define IJMP_OPCODE                                                 0x9409
#define IN_OPCODE                                                   0xB000
#define INC_OPCODE                                                  0x9403
#define JMP_OPCODE                                                  0x940C
#define LAC_OPCODE                                                  0x9206
#define LAS_OPCODE                                                  0x9205
#define LAT_OPCODE                                                  0x9207
#define LD_X_UNCHANGED_OPCODE                                       0x900C
#define LD_X_POST_INCREMENT_OPCODE                                  0x900D
#define LD_X_PRE_DECREMENT_OPCODE                                   0x900E
#define LD_Y_UNCHANGED_OPCODE                                       0x8008
#define LD_Y_POST_INCREMENT_OPCODE                                  0x9009
#define LD_Y_PRE_DECREMENT_OPCODE                                   0x900A
#define LDD_Y_OPCODE                                                0x8008
#define LD_Z_UNCHANGED_OPCODE                                       0x8000
#define LD_Z_POST_INCREMENT_OPCODE                                  0x9001
#define LD_Z_PRE_DECREMENT_OPCODE                                   0x9002
#define LDD_Z_OPCODE                                                0x8000
#define LDI_SER_OPCODE                                              0xE000
#define LDS_OPCODE                                                  0x9000
#define LDS_16_OPCODE                                               0xA000
#define LPM_Z_UNCHANGED_DEST_R0_OPCODE                              0x95C8
#define LPM_Z_UNCHANGED_OPCODE                                      0x9004
#define LPM_Z_POST_INCREMENT_OPCODE                                 0x9005
#define LSR_OPCODE                                                  0x9406
#define MOV_OPCODE                                                  0x2C00
#define MOVW_OPCODE                                                 0x0100
#define MUL_OPCODE                                                  0x9C00
#define MULS_OPCODE                                                 0x0200
#define MULSU_OPCODE                                                0x0300
#define NEG_OPCODE                                                  0x9401
#define NOP_OPCODE                                                  0x0000
#define OR_OPCODE                                                   0x2800
#define ORI_SBR_OPCODE                                              0x6000
#define OUT_OPCODE                                                  0xB800
#define POP_OPCODE                                                  0x900F
#define PUSH_OPCODE                                                 0x920F
#define RCALL_OPCODE                                                0xD000
#define RET_OPCODE                                                  0x9508
#define RETI_OPCODE                                                 0x9518
#define RJMP_OPCODE                                                 0xC000
#define ROR_OPCODE                                                  0x9407
#define SBC_OPCODE                                                  0x0800
#define SBCI_OPCODE                                                 0x4000
#define SBI_OPCODE                                                  0x9A00
#define SBIC_OPCODE                                                 0x9900
#define SBIS_OPCODE                                                 0x9B00
#define SBIW_OPCODE                                                 0x9700
#define SBRC_OPCODE                                                 0xFC00
#define SBRS_OPCODE                                                 0xFE00
#define SLEEP_OPCODE                                                0x9588
#define SPM_Z_UNCHANGED_OPCODE                                      0x95E8
#define SPM_Z_POST_INCREMENT_OPCODE                                 0x95F8
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
#define STS_OPCODE  0x9200
#define INSTRUCTION_SUB_MASK  86
#define INSTRUCTION_SUBI_MASK  87
#define INSTRUCTION_SWAP_MASK  88
#define INSTRUCTION_WDR_MASK  89

#define I_FLAG_MASK 0x80
#define T_FLAG_MASK 0x40
#define H_FLAG_MASK 0x20
#define S_FLAG_MASK 0x10
#define V_FLAG_MASK 0x08
#define N_FLAG_MASK 0x04
#define Z_FLAG_MASK 0x02
#define C_FLAG_MASK 0x01

#define REG00_ADDR 0x00
#define REG01_ADDR 0x01
#define REG16_ADDR 0x10
#define REG24_ADDR 0x18
#define REG25_ADDR 0x19
//X Register
#define REG26_ADDR 0x1A
#define REG27_ADDR 0x1B
//Y Register
#define REG28_ADDR 0x1C
#define REG29_ADDR 0x1D
//Z Register
#define REG30_ADDR 0x1E
#define REG31_ADDR 0x1F

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
        switch (i & INSTRUCTION_GROUP1_MASK) {
            case ADC_ROL_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_ADC_ROL;
                continue;
            case ADD_LSL_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_ADD_LSL;
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
            case CPSE_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_CPSE;
                continue;
            case MOV_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_MOV;
                continue;
            case MUL_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_MUL;
                continue;
            case OR_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_OR;
                continue;
            case SBC_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_SBC;
                continue;
        }
        switch (i & INSTRUCTION_GROUP2_MASK) {
            case ADIW_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_ADIW;
                continue;
            case CBI_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_CBI;
                continue;
            case MOVW_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_MOVW;
                continue;
            case MULS_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_MULS;
                continue;
            case SBI_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_SBI;
                continue;
            case SBIC_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_SBIC;
                continue;
            case SBIS_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_SBIS;
                continue;
            case SBIW_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_SBIW;
                continue;
        }
        switch (i & INSTRUCTION_GROUP3_MASK) {
            case ANDI_CBR_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_ANDI_CBR;
                continue;
            case CPI_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_CPI;
                continue;
            case LDI_SER_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_LDI_SER;
                continue;
            case LDS_16_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_LDS16;
                continue;
            case ORI_SBR_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_ORI_SBR;
                continue;
            case RCALL_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_RCALL;
                continue;
            case RJMP_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_RJMP;
                continue;
            case SBCI_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_SBCI;
                continue;
        }
        switch (i & INSTRUCTION_GROUP4_MASK) {
            case ASR_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_ASR;
                continue;
            case COM_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_COM;
                continue;
            case NEG_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_NEG;
                continue;
            case ROR_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_ROR;
                continue;
        }
        switch (i & INSTRUCTION_GROUP5_MASK) {
            case BCLR_CLC_CLH_CLI_CLN_CLS_CLT_CLV_CLZ_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_BCLR_CLC_CLH_CLI_CLN_CLS_CLT_CLV_CLZ;
                continue;
            case BSET_SEC_SEH_SEI_SEN_SES_SET_SEV_SEZ_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_BSET_SEC_SEH_SEI_SEN_SES_SET_SEV_SEZ;
                continue;
        }
        switch (i & INSTRUCTION_GROUP6_MASK) {
            case BLD_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_BLD;
                continue;
            case BST_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_BST;
                continue;
            case SBRC_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_SBRC;
                continue;
            case SBRS_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_SBRS;
                continue;
        }
        switch (i & INSTRUCTION_GROUP7_MASK) {
            case CALL_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_CALL;
                continue;
            case JMP_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_JMP;
                continue;
            case LSR_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_LSR;
                continue;
        }
        switch (i & INSTRUCTION_GROUP8_MASK) {
            case DEC_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_DEC;
                continue;
            case ELPM2_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_ELPM2;
                continue;
            case ELPM3_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_ELPM3;
                continue;
            case INC_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_INC;
                continue;
            case LAC_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_LAC;
                continue;
            case LAS_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_LAS;
                continue;
            case LAT_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_LAT;
                continue;
            case LD_X_UNCHANGED_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_LD_X_UNCHANGED;
                continue;
            case LD_X_POST_INCREMENT_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_LD_X_POST_INCREMENT;
                continue;
            case LD_X_PRE_DECREMENT_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_LD_X_PRE_DECREMENT;
                continue;
            case LD_Y_UNCHANGED_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_LD_Y_UNCHANGED;
                continue;
            case LD_Y_POST_INCREMENT_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_LD_Y_POST_INCREMENT;
                continue;
            case LD_Y_PRE_DECREMENT_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_LD_Y_PRE_DECREMENT;
                continue;
            case LD_Z_UNCHANGED_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_LD_Z_UNCHANGED;
                continue;
            case LD_Z_POST_INCREMENT_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_LD_Z_POST_INCREMENT;
                continue;
            case LD_Z_PRE_DECREMENT_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_LD_Z_PRE_DECREMENT;
                continue;
            case LDS_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_LDS;
                continue;
            case LPM_Z_UNCHANGED_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_LPM_Z_UNCHANGED;
                continue;
            case LPM_Z_POST_INCREMENT_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_LPM_Z_POST_INCREMENT;
                continue;
            case POP_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_POP;
                continue;
            case PUSH_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_PUSH;
                continue;
        }
        switch (i & INSTRUCTION_GROUP9_MASK) {
            case DES_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_DES;
                continue;
        }
        switch (i & INSTRUCTION_GROUP10_MASK) {
            case FMUL_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_FMUL;
                continue;
            case FMULS_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_FMULS;
                continue;
            case FMULSU_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_FMULSU;
                continue;
            case MULSU_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_MULSU;
                continue;
        }
        switch (i & INSTRUCTION_GROUP11_MASK) {
            case IN_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_IN;
                continue;
            case OUT_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_OUT;
                continue;
        }
        switch (i & INSTRUCTION_GROUP12_MASK) {
            case LDD_Y_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_LDD_Y;
                continue;
            case LDD_Z_OPCODE:
                instructionDecoder[i] = &AVRCPU::instruction_LDD_Z;
                continue;
        }
        if (i == BREAK_OPCODE) {
            instructionDecoder[i] = &AVRCPU::instruction_BREAK;
            continue;
        }
        if (i == EICALL_OPCODE) {
            instructionDecoder[i] = &AVRCPU::instruction_EICALL;
            continue;
        }
        if (i == EIJMP_OPCODE) {
            instructionDecoder[i] = &AVRCPU::instruction_EIJMP;
            continue;
        }
        if (i == ELPM1_OPCODE) {
            instructionDecoder[i] = &AVRCPU::instruction_ELPM1;
            continue;
        }
        if (i == ICALL_OPCODE) {
            instructionDecoder[i] = &AVRCPU::instruction_ICALL;
            continue;
        }
        if (i == IJMP_OPCODE) {
            instructionDecoder[i] = &AVRCPU::instruction_IJMP;
            continue;
        }
        if (i == LPM_Z_UNCHANGED_DEST_R0_OPCODE) {
            instructionDecoder[i] = &AVRCPU::instruction_LPM_Z_UNCHANGED_DEST_R0;
            continue;
        }
        if (i == NOP_OPCODE) {
            instructionDecoder[i] = &AVRCPU::instruction_NOP;
            continue;
        }
        if (i == RET_OPCODE) {
            instructionDecoder[i] = &AVRCPU::instruction_RET;
            continue;
        }
        if (i == RETI_OPCODE) {
            instructionDecoder[i] = &AVRCPU::instruction_RETI;
            continue;
        }
        if (i == SLEEP_OPCODE) {
            instructionDecoder[i] = &AVRCPU::instruction_SLEEP;
            continue;
        }
        if (i == SPM_Z_UNCHANGED_OPCODE) {
            instructionDecoder[i] = &AVRCPU::instruction_SPM_Z_UNCHANGED;
            continue;
        }
        if (i == SPM_Z_POST_INCREMENT_OPCODE) {
            instructionDecoder[i] = &AVRCPU::instruction_SPM_POST_INCREMENT;
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

void AVRCPU::instruction_ADC_ROL() {
    /*************************ADC/ROL***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction ADC/ROL");

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

void AVRCPU::instruction_ADD_LSL() {
    /*************************ADD/LSL***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction ADD/LSL");

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
    dataLAddr = REG24_ADDR + offset;
    dataHAddr = REG25_ADDR + offset;

    datMem->read(dataLAddr, &dataL);
    datMem->read(dataHAddr, &dataH);
    datMem->read(sregAddr, &sreg);

    outData = ((dataH<<8) | dataL) + (((0x00C0 & instruction)>>2) | (0x000F & instruction));
    sreg &= 0xE0;

    //Flag V
    sreg |= (((~dataH)>>4)&(outData>>12))&V_FLAG_MASK;

    //Flag N
    sreg |= (outData>>13)&N_FLAG_MASK;

    //Flag S
    sreg |= (((sreg << 1) ^ sreg) << 1) & S_FLAG_MASK;

    //Flag Z
    sreg |= outData?0x0000:Z_FLAG_MASK;

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
    /*************************ANDI/CBR***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction ANDI/CBR");

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

void AVRCPU::instruction_BSET_SEC_SEH_SEI_SEN_SES_SET_SEV_SEZ() {
    /*************************BSET/SEC/SEH/SEI/SEN/SES/SET/SEV/SEZ***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction BSET/SEC/SEH/SEI/SEN/SES/SET/SEV/SEZ");

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

    datMem->read((0x01F0 & instruction) >> 4, &regD);
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

    datMem->read((0x01F0 & instruction) >> 4, &regD);
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

void AVRCPU::instruction_CPI() {
    /*************************CPI***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction CPI");

    immediate = ((0x0F00 & instruction) >> 4) | (0x000F & instruction);
    datMem->read(REG16_ADDR | ((0x00F0 & instruction) >> 4), &regD);
    datMem->read(sregAddr, &sreg);

    result = regD - immediate;
    sreg &= 0xC0;

    immediate_and_result = immediate & result;
    not_result = ~result;
    not_regD = ~regD;

    hc_flag = (not_regD & immediate) | immediate_and_result | (result & not_regD);

    //Flag H
    sreg |= (hc_flag << 2) & H_FLAG_MASK;

    //Flag V
    sreg |= (((regD & (~immediate) & not_result) | (not_regD & immediate_and_result)) >> 4) & V_FLAG_MASK;

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

void AVRCPU::instruction_CPSE() {
    /*************************CPSE***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction CPSE");

    datMem->read((0x01F0 & instruction) >> 4, &regD);
    datMem->read(((0x0200 & instruction) >> 5) | (0x000F & instruction), &regR);

    if (regD == regR) {
        progMem->loadInstruction(pc++, &instruction);

        //Test 2 word instruction
        testJMP_CALL = instruction & INSTRUCTION_GROUP7_MASK;
        testLDS_STS = instruction & INSTRUCTION_GROUP8_MASK;
        if (testJMP_CALL == JMP_OPCODE ||
            testJMP_CALL == CALL_OPCODE||
            testLDS_STS  == LDS_OPCODE ||
            testLDS_STS  == STS_OPCODE) {
            pc += 1;
        }
    }
}

void AVRCPU::instruction_DEC() {
    /*************************DEC***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction DEC");

    wbAddr = (0x01F0 & instruction) >> 4;

    datMem->read(wbAddr, &regD);
    datMem->read(sregAddr, &sreg);

    result = regD - 1;
    sreg &= 0xE1;

    //Flag V
    sreg |= (regD == 0x80)?V_FLAG_MASK:0x00;

    //Flag N
    sreg |= (result >> 5) & N_FLAG_MASK;

    //Flag S
    sreg |= (((sreg << 1) ^ sreg) << 1) & S_FLAG_MASK;

    //Flag Z
    sreg |= result?0x00:Z_FLAG_MASK;

    datMem->write(wbAddr, &result);
    datMem->write(sregAddr, &sreg);
}

void AVRCPU::instruction_DES() {
    /*************************DES***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction DES - NOT IMPLEMENTED");
}

void AVRCPU::instruction_EICALL() {
    /*************************EICALL***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction EICALL - NOT IMPLEMENTED");
}

void AVRCPU::instruction_EIJMP() {
    /*************************EIJMP***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction EIJMP - NOT IMPLEMENTED");
}

void AVRCPU::instruction_ELPM1() {
    /*************************ELPM1***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction ELPM1 - NOT IMPLEMENTED");
}

void AVRCPU::instruction_ELPM2() {
    /*************************ELPM2***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction ELPM2 - NOT IMPLEMENTED");
}

void AVRCPU::instruction_ELPM3() {
    /*************************ELPM3***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction ELPM3 - NOT IMPLEMENTED");
}

void AVRCPU::instruction_FMUL() {
    /*************************FMUL***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction FMUL");

    datMem->read(REG16_ADDR|((0x0070&instruction)>>4), &regD);
    datMem->read(REG16_ADDR|(0x0007&instruction), &regR);
    datMem->read(sregAddr, &sreg);

    outData = regD * regR;
    sreg &= 0xFC;

    //Flag Z
    sreg |= outData?0x0000:Z_FLAG_MASK;

    //Flag C
    sreg |= (outData>>15)&C_FLAG_MASK;

    //"A left shift is necessary for the high byte of the product to be
    //in the same format as the inputs"
    outData = outData << 1;

    datMem->write(REG00_ADDR, &outData);
    outData = outData >> 8;
    datMem->write(REG01_ADDR, &outData);
    datMem->write(sregAddr, &sreg);
}

void AVRCPU::instruction_FMULS() {
    /*************************FMULS***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction FMULS");

    datMem->read(REG16_ADDR|((0x0070&instruction)>>4), &regD);
    datMem->read(REG16_ADDR|(0x0007&instruction), &regR);
    datMem->read(sregAddr, &sreg);

    outData = ((__int8_t)regD) * ((__int8_t)regR); //signed multiplication
    sreg &= 0xFC;

    //Flag Z
    sreg |= outData?0x0000:Z_FLAG_MASK;

    //Flag C
    sreg |= (outData>>15)&C_FLAG_MASK;

    //"A left shift is necessary for the high byte of the product to be
    //in the same format as the inputs"
    outData = outData << 1;

    datMem->write(REG00_ADDR, &outData);
    outData = outData >> 8;
    datMem->write(REG01_ADDR, &outData);
    datMem->write(sregAddr, &sreg);
}

void AVRCPU::instruction_FMULSU() {
    /*************************FMULSU***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction FMULSU");

    datMem->read(REG16_ADDR|((0x0070&instruction)>>4), &regD);
    datMem->read(REG16_ADDR|(0x0007&instruction), &regR);
    datMem->read(sregAddr, &sreg);

    outData = ((__int8_t)regD) * regR; //signed * unsigned
    sreg &= 0xFC;

    //Flag Z
    sreg |= outData?0x0000:Z_FLAG_MASK;

    //Flag C
    sreg |= (outData>>15)&C_FLAG_MASK;

    //"A left shift is necessary for the high byte of the product to be
    //in the same format as the inputs"
    outData = outData << 1;

    datMem->write(REG00_ADDR, &outData);
    outData = outData >> 8;
    datMem->write(REG01_ADDR, &outData);
    datMem->write(sregAddr, &sreg);
}

void AVRCPU::instruction_ICALL() {
    /*************************ICALL***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction ICALL");

    //Read Z Register
    datMem->read(REG30_ADDR, &dataL);
    datMem->read(REG31_ADDR, &dataH);

    jumpValue = (dataH<<8) | dataL;

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

void AVRCPU::instruction_IJMP() {
    /*************************IJMP***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction IJMP");

    //Read Z Register
    datMem->read(REG30_ADDR, &dataL);
    datMem->read(REG31_ADDR, &dataH);

    jumpValue = (dataH<<8) | dataL;

    pc = jumpValue;
}

void AVRCPU::instruction_IN() {
    /*************************IN***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction IN");

    datMem->read(IOREG_BASEADDR + (((0x0600&instruction)>>5)|(0x000F&instruction)), &result);
    datMem->write((0x01F0&instruction)>>4, &result);
}

void AVRCPU::instruction_INC() {
    /*************************INC***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction INC");

    wbAddr = (0x01F0 & instruction) >> 4;

    datMem->read(wbAddr, &regD);
    datMem->read(sregAddr, &sreg);

    result = regD + 1;
    sreg &= 0xE1;

    //Flag V
    sreg |= (regD == 0x7F)?V_FLAG_MASK:0x00;

    //Flag N
    sreg |= (result >> 5) & N_FLAG_MASK;

    //Flag S
    sreg |= (((sreg << 1) ^ sreg) << 1) & S_FLAG_MASK;

    //Flag Z
    sreg |= result?0x00:Z_FLAG_MASK;

    datMem->write(wbAddr, &result);
    datMem->write(sregAddr, &sreg);
}

void AVRCPU::instruction_JMP() {
    /*************************JMP***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction JMP");

    jumpValue = ((instruction&0x01F0)>>3) | (instruction&0x0001);
    progMem->loadInstruction(pc++, &instruction);
    jumpValue = (jumpValue<<16) | instruction;

    pc = jumpValue;
}

void AVRCPU::instruction_LAC() {
    /*************************LAC***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction LAC - NOT IMPLEMENTED");
}

void AVRCPU::instruction_LAS() {
    /*************************LAS***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction LAS - NOT IMPLEMENTED");
}

void AVRCPU::instruction_LAT() {
    /*************************LAT***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction LAT - NOT IMPLEMENTED");
}

void AVRCPU::instruction_LD_X_UNCHANGED() {
    /*************************LD (X UNCHANGED)***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction LD (X UNCHANGED)");

    //Read X Register
    datMem->read(REG26_ADDR, &dataL);
    datMem->read(REG27_ADDR, &dataH);
    wbAddr = (dataH<<8) | dataL;

    datMem->read(wbAddr, &result);
    datMem->write((0x01F0 & instruction)>>4, &result);
}

void AVRCPU::instruction_LD_X_POST_INCREMENT() {
    /*************************LD (X POST INCREMENT)***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction LD (X POST INCREMENT)");

    //Read X Register
    datMem->read(REG26_ADDR, &dataL);
    datMem->read(REG27_ADDR, &dataH);
    wbAddr = (dataH<<8) | dataL;

    datMem->read(wbAddr++, &result);
    datMem->write((0x01F0 & instruction)>>4, &result);
    datMem->write(REG26_ADDR, &wbAddr);
    wbAddr = wbAddr >> 8;
    datMem->write(REG27_ADDR, &wbAddr);
}

void AVRCPU::instruction_LD_X_PRE_DECREMENT() {
    /*************************LD (X PRE DECREMENT)***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction LD (X PRE DECREMENT)");

    //Read X Register
    datMem->read(REG26_ADDR, &dataL);
    datMem->read(REG27_ADDR, &dataH);
    wbAddr = (dataH<<8) | dataL;

    datMem->read(--wbAddr, &result);
    datMem->write((0x01F0 & instruction)>>4, &result);
    datMem->write(REG26_ADDR, &wbAddr);
    wbAddr = wbAddr >> 8;
    datMem->write(REG27_ADDR, &wbAddr);
}

void AVRCPU::instruction_LD_Y_UNCHANGED() {
    /*************************LD (Y UNCHANGED)***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction LD (Y UNCHANGED)");

    //Read Y Register
    datMem->read(REG28_ADDR, &dataL);
    datMem->read(REG29_ADDR, &dataH);
    wbAddr = (dataH<<8) | dataL;

    datMem->read(wbAddr, &result);
    datMem->write((0x01F0 & instruction)>>4, &result);
}

void AVRCPU::instruction_LD_Y_POST_INCREMENT() {
    /*************************LD (Y POST INCREMENT)***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction LD (Y POST INCREMENT)");

    //Read Y Register
    datMem->read(REG28_ADDR, &dataL);
    datMem->read(REG29_ADDR, &dataH);
    wbAddr = (dataH<<8) | dataL;

    datMem->read(wbAddr++, &result);
    datMem->write((0x01F0 & instruction)>>4, &result);
    datMem->write(REG28_ADDR, &wbAddr);
    wbAddr = wbAddr >> 8;
    datMem->write(REG29_ADDR, &wbAddr);
}

void AVRCPU::instruction_LD_Y_PRE_DECREMENT() {
    /*************************LD (Y PRE DECREMENT)***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction LD (Y PRE DECREMENT)");

    //Read Y Register
    datMem->read(REG28_ADDR, &dataL);
    datMem->read(REG29_ADDR, &dataH);
    wbAddr = (dataH<<8) | dataL;

    datMem->read(--wbAddr, &result);
    datMem->write((0x01F0 & instruction)>>4, &result);
    datMem->write(REG28_ADDR, &wbAddr);
    wbAddr = wbAddr >> 8;
    datMem->write(REG29_ADDR, &wbAddr);
}

void AVRCPU::instruction_LDD_Y() {
    /*************************LDD (Y)***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction LDD (Y)");

    //Read Y Register
    datMem->read(REG28_ADDR, &dataL);
    datMem->read(REG29_ADDR, &dataH);
    wbAddr = ((dataH<<8) | dataL) + (((0x2000 & instruction)>>8) | ((0x0C00 & instruction)>>7)| (0x0007 & instruction));

    datMem->read(wbAddr, &result);
    datMem->write((0x01F0 & instruction)>>4, &result);
}

void AVRCPU::instruction_LD_Z_UNCHANGED() {
    /*************************LD (Z UNCHANGED)***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction LD (Z UNCHANGED)");

    //Read Z Register
    datMem->read(REG30_ADDR, &dataL);
    datMem->read(REG31_ADDR, &dataH);
    wbAddr = (dataH<<8) | dataL;

    datMem->read(wbAddr, &result);
    datMem->write((0x01F0 & instruction)>>4, &result);
}

void AVRCPU::instruction_LD_Z_POST_INCREMENT() {
    /*************************LD (Z POST INCREMENT)***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction LD (Z POST INCREMENT)");

    //Read Z Register
    datMem->read(REG30_ADDR, &dataL);
    datMem->read(REG31_ADDR, &dataH);
    wbAddr = (dataH<<8) | dataL;

    datMem->read(wbAddr++, &result);
    datMem->write((0x01F0 & instruction)>>4, &result);
    datMem->write(REG30_ADDR, &wbAddr);
    wbAddr = wbAddr >> 8;
    datMem->write(REG31_ADDR, &wbAddr);
}

void AVRCPU::instruction_LD_Z_PRE_DECREMENT() {
    /*************************LD (Z PRE DECREMENT)***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction LD (Z PRE DECREMENT)");

    //Read Z Register
    datMem->read(REG30_ADDR, &dataL);
    datMem->read(REG31_ADDR, &dataH);
    wbAddr = (dataH<<8) | dataL;

    datMem->read(--wbAddr, &result);
    datMem->write((0x01F0 & instruction)>>4, &result);
    datMem->write(REG30_ADDR, &wbAddr);
    wbAddr = wbAddr >> 8;
    datMem->write(REG31_ADDR, &wbAddr);
}

void AVRCPU::instruction_LDD_Z() {
    /*************************LDD (Z)***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction LDD (Z)");

    //Read Z Register
    datMem->read(REG30_ADDR, &dataL);
    datMem->read(REG31_ADDR, &dataH);
    wbAddr = ((dataH<<8) | dataL) + (((0x2000 & instruction)>>8) | ((0x0C00 & instruction)>>7)| (0x0007 & instruction));

    datMem->read(wbAddr, &result);
    datMem->write((0x01F0 & instruction)>>4, &result);
}

void AVRCPU::instruction_LDI_SER() {
    /*************************LDI/SER***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction LDI/SER");

    result = ((0x0F00 & instruction)>>4) | (0x000F & instruction);
    datMem->write(REG16_ADDR | ((0x00F0 & instruction) >> 4), &result);
}

void AVRCPU::instruction_LDS() {
    /*************************LDS***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction LDS");

    wbAddr = (0x01F0 & instruction) >> 4;

    progMem->loadInstruction(pc++, &instruction);

    datMem->read(instruction, &result);
    datMem->write(wbAddr, &result);
}

void AVRCPU::instruction_LDS16() {
    /*************************LDS (16-bit)***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction LDS (16-bit) - NOT IMPLEMENTED");
}

void AVRCPU::instruction_LPM_Z_UNCHANGED_DEST_R0() {
    /*************************LPM (Z UNCHANGED - DEST RO)***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction LPM (Z UNCHANGED - DEST RO)");

    //Read Z Register
    datMem->read(REG30_ADDR, &dataL);
    datMem->read(REG31_ADDR, &dataH);

    wbAddr = (dataH<<8) | dataL;

    progMem->read(wbAddr, &result);
    datMem->write(REG00_ADDR, &result);
}

void AVRCPU::instruction_LPM_Z_UNCHANGED() {
    /*************************LPM (Z UNCHANGED)***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction LPM (Z UNCHANGED)");

    //Read Z Register
    datMem->read(REG30_ADDR, &dataL);
    datMem->read(REG31_ADDR, &dataH);

    wbAddr = (dataH<<8) | dataL;

    progMem->read(wbAddr, &result);
    datMem->write((0x01F0 & instruction)>>4, &result);
}

void AVRCPU::instruction_LPM_Z_POST_INCREMENT() {
    /*************************LPM (Z POST INCREMENT)***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction LPM (Z POST INCREMENT)");

    //Read Z Register
    datMem->read(REG30_ADDR, &dataL);
    datMem->read(REG31_ADDR, &dataH);

    wbAddr = (dataH<<8) | dataL;

    progMem->read(wbAddr++, &result);
    datMem->write((0x01F0 & instruction)>>4, &result);
    datMem->write(REG30_ADDR, &wbAddr);
    wbAddr = wbAddr >> 8;
    datMem->write(REG31_ADDR, &wbAddr);
}

void AVRCPU::instruction_LSR() {
    /*************************LSR***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction LSR");

    wbAddr = (0x01F0 & instruction) >> 4;

    datMem->read(wbAddr, &regD);
    datMem->read(sregAddr, &sreg);

    result = regD >> 1;
    sreg &= 0xE0;

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

void AVRCPU::instruction_MOV() {
    /*************************MOV***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction MOV");

    datMem->read((((0x0200&instruction)>>5) | (0x000F&instruction)), &result);
    datMem->write((0x01F0 & instruction) >> 4, &result);
}

void AVRCPU::instruction_MOVW() {
    /*************************MOVW***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction MOVW");

    regRAddr = (0x000F&instruction)<<1;
    regDAddr = (0x00F0&instruction)>>3;

    datMem->read(regRAddr, &result);
    datMem->write(regDAddr, &result);

    datMem->read(regRAddr+1, &result);
    datMem->write(regDAddr+1, &result);
}

void AVRCPU::instruction_MUL() {
    /*************************MUL***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction MUL");

    datMem->read((0x01F0&instruction)>>4, &regD);
    datMem->read(((0x0200&instruction)>>5) | (0x000F&instruction), &regR);
    datMem->read(sregAddr, &sreg);

    outData = regD * regR;
    sreg &= 0xFC;

    //Flag Z
    sreg |= outData?0x0000:Z_FLAG_MASK;

    //Flag C
    sreg |= (outData>>15)&C_FLAG_MASK;

    datMem->write(REG00_ADDR, &outData);
    outData = outData >> 8;
    datMem->write(REG01_ADDR, &outData);
    datMem->write(sregAddr, &sreg);
}

void AVRCPU::instruction_MULS() {
    /*************************MULS***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction MULS");

    datMem->read(REG16_ADDR | ((0x00F0 & instruction) >> 4), &regD);
    datMem->read(REG16_ADDR | (0x000F & instruction), &regR);
    datMem->read(sregAddr, &sreg);

    outData = ((__int8_t)regD) * ((__int8_t)regR); //signed multiplication
    sreg &= 0xFC;

    //Flag Z
    sreg |= outData?0x0000:Z_FLAG_MASK;

    //Flag C
    sreg |= (outData>>15)&C_FLAG_MASK;

    datMem->write(REG00_ADDR, &outData);
    outData = outData >> 8;
    datMem->write(REG01_ADDR, &outData);
    datMem->write(sregAddr, &sreg);
}

void AVRCPU::instruction_MULSU() {
    /*************************MULSU***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction MULSU");

    datMem->read(REG16_ADDR | ((0x0070 & instruction) >> 4), &regD);
    datMem->read(REG16_ADDR | (0x0007 & instruction), &regR);
    datMem->read(sregAddr, &sreg);

    outData = ((__int8_t)regD) * regR; //signed * unsigned
    sreg &= 0xFC;

    //Flag Z
    sreg |= outData?0x0000:Z_FLAG_MASK;

    //Flag C
    sreg |= (outData>>15)&C_FLAG_MASK;

    datMem->write(REG00_ADDR, &outData);
    outData = outData >> 8;
    datMem->write(REG01_ADDR, &outData);
    datMem->write(sregAddr, &sreg);
}

void AVRCPU::instruction_NEG() {
    /*************************NEG***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction NEG");

    wbAddr = (0x01F0&instruction)>>4;
    datMem->read(wbAddr, &regD);
    datMem->read(sregAddr, &sreg);

    result = 0x00 - regD;
    sreg &= 0xC0;

    //Flag H
    sreg |= ((result & regD)<<2)&H_FLAG_MASK;

    //Flag V
    sreg |= (result == 0x80)?V_FLAG_MASK:0x00;

    //Flag N
    sreg |= (result >> 5) & N_FLAG_MASK;

    //Flag S
    sreg |= (((sreg << 1) ^ sreg) << 1) & S_FLAG_MASK;

    //Flag Z and C
    sreg |= result?C_FLAG_MASK:Z_FLAG_MASK;

    datMem->write(wbAddr, &result);
    datMem->write(sregAddr, &sreg);
}

void AVRCPU::instruction_NOP() {
    /*************************NOP***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction NOP");
}

void AVRCPU::instruction_OR() {
    /*************************OR***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction OR");

    wbAddr = (0x01F0 & instruction) >> 4;

    datMem->read(wbAddr, &regD);
    datMem->read(((0x0200 & instruction) >> 5) | (0x000F & instruction), &regR);
    datMem->read(sregAddr, &sreg);

    result = regD | regR;
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

void AVRCPU::instruction_ORI_SBR() {
    /*************************ORI/SBR***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction ORI/SBR");

    wbAddr = REG16_ADDR | ((0x00F0 & instruction) >> 4);

    datMem->read(wbAddr, &regD);
    datMem->read(sregAddr, &sreg);

    result = regD | (((0x0F00&instruction)>>4)|(0x000F&instruction));
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

void AVRCPU::instruction_OUT() {
    /*************************OUT***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction OUT");

    datMem->read((0x01F0&instruction)>>4, &result);
    datMem->write(IOREG_BASEADDR + (((0x0600&instruction)>>5)|(0x000F&instruction)), &result);
}

void AVRCPU::instruction_POP() {
    /*************************POP***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction POP");

    datMem->read(stackLAddr, &dataL);
    datMem->read(stackHAddr, &dataH);
    stackPointer = (dataH<<8)|dataL;

    datMem->read(++stackPointer, &result);
    datMem->write((0x01F0&instruction)>>4, &result);

    //Update SPL and SPH
    datMem->write(stackLAddr, &stackPointer);
    stackPointer = stackPointer >> 8;
    datMem->write(stackHAddr, &stackPointer);
}

void AVRCPU::instruction_PUSH() {
    /*************************PUSH***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction PUSH");

    datMem->read(stackLAddr, &dataL);
    datMem->read(stackHAddr, &dataH);
    stackPointer = (dataH<<8)|dataL;

    datMem->read((0x01F0&instruction)>>4, &result);
    datMem->write(stackPointer--, &result);

    //Update SPL and SPH
    datMem->write(stackLAddr, &stackPointer);
    stackPointer = stackPointer >> 8;
    datMem->write(stackHAddr, &stackPointer);
}

void AVRCPU::instruction_RCALL() {
    /*************************RCALL***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction RCALL");

    jumpValue = 0x0FFF&instruction;

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

    pc += (((__int32_t)jumpValue)<<20)>>20; //Cast to make sign extension
}

void AVRCPU::instruction_RET() {
    /*************************RET***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction RET");

    datMem->read(stackLAddr, &dataL);
    datMem->read(stackHAddr, &dataH);
    stackPointer = (dataH<<8)|dataL;

    //PC (read little-endian)
    datMem->read(++stackPointer, &dataH);
    datMem->read(++stackPointer, &dataL);
    pc = (dataH<<8)|dataL;

    //Update SPL and SPH
    datMem->write(stackLAddr, &stackPointer);
    stackPointer = stackPointer >> 8;
    datMem->write(stackHAddr, &stackPointer);
}

void AVRCPU::instruction_RETI() {
    /*************************RETI***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction RETI");

    datMem->read(stackLAddr, &dataL);
    datMem->read(stackHAddr, &dataH);
    stackPointer = (dataH<<8)|dataL;

    //PC (read little-endian)
    datMem->read(++stackPointer, &dataH);
    datMem->read(++stackPointer, &dataL);
    pc = (dataH<<8)|dataL;

    datMem->read(sregAddr, &sreg);
    sreg |= I_FLAG_MASK; //FIXME: AVR XMEGA will not perform this set
    datMem->write(sregAddr, &sreg);

    //Update SPL and SPH
    datMem->write(stackLAddr, &stackPointer);
    stackPointer = stackPointer >> 8;
    datMem->write(stackHAddr, &stackPointer);
}

void AVRCPU::instruction_RJMP() {
    /*************************RJMP***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction RJMP");

    jumpValue = 0x0FFF&instruction;
    pc += (((__int32_t)jumpValue)<<20)>>20; //Cast to make sign extension
}

void AVRCPU::instruction_ROR() {
    /*************************ROR***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction ROR");

    wbAddr = (0x01F0 & instruction) >> 4;

    datMem->read(wbAddr, &regD);
    datMem->read(sregAddr, &sreg);

    result = (sreg << 7) | (regD >> 1);
    sreg &= 0xE0;

    //Flag C
    sreg |= regD & C_FLAG_MASK;

    //Flag N
    sreg |= (result >> 5) & N_FLAG_MASK;

    //Flag V
    sreg |= (((sreg << 2) ^ sreg) << 1) & V_FLAG_MASK;

    //Flag S
    sreg |= (((sreg << 1) ^ sreg) << 1) & S_FLAG_MASK;

    //Flag Z
    sreg |= result?0x00:Z_FLAG_MASK;

    datMem->write(wbAddr, &result);
    datMem->write(sregAddr, &sreg);
}

void AVRCPU::instruction_SBC() {
    /*************************SBC***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction SBC");

    wbAddr = (0x01F0 & instruction) >> 4;

    datMem->read(wbAddr, &regD);
    datMem->read(((0x0200 & instruction) >> 5) | (0x000F & instruction), &regR);
    datMem->read(sregAddr, &sreg);

    result = regD - regR - (sreg & C_FLAG_MASK);
    sreg &= 0xC2; //Do not clear previous Z flag

    not_regD = (~regD);
    notRegD_and_regR = not_regD & regR;

    hc_flag = notRegD_and_regR | (regR & result) | (result & not_regD);

    //Flag H
    sreg |= (hc_flag << 2) & H_FLAG_MASK;

    //Flag V
    sreg |= (((regD & (~regR) & (~result)) | (notRegD_and_regR & result)) >> 4) & V_FLAG_MASK;

    //Flag N
    sreg |= (result >> 5) & N_FLAG_MASK;

    //Flag S
    sreg |= (((sreg << 1) ^ sreg) << 1) & S_FLAG_MASK;

    //Flag Z
    sreg &= result?(~Z_FLAG_MASK):sreg;

    //Flag C
    sreg |= (hc_flag >> 7) & C_FLAG_MASK;

    datMem->write(wbAddr, &result);
    datMem->write(sregAddr, &sreg);
}

void AVRCPU::instruction_SBCI() {
    /*************************SBCI***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction SBCI");

    wbAddr = REG16_ADDR | ((0x00F0 & instruction) >> 4);
    immediate = ((0x0F00 & instruction) >> 4) | (0x000F & instruction);

    datMem->read(wbAddr, &regD);
    datMem->read(sregAddr, &sreg);

    result = regD - immediate - (sreg & C_FLAG_MASK);
    sreg &= 0xC2; //Do not clear previous Z flag

    immediate_and_result = immediate & result;
    not_result = ~result;
    not_regD = ~regD;

    hc_flag = (not_regD & immediate) | immediate_and_result | (result & not_regD);

    //Flag H
    sreg |= (hc_flag << 2) & H_FLAG_MASK;

    //Flag V
    sreg |= (((regD & (~immediate) & not_result) | (not_regD & immediate_and_result)) >> 4) & V_FLAG_MASK;

    //Flag N
    sreg |= (result >> 5) & N_FLAG_MASK;

    //Flag S
    sreg |= (((sreg << 1) ^ sreg) << 1) & S_FLAG_MASK;

    //Flag Z
    sreg &= result?(~Z_FLAG_MASK):sreg;

    //Flag C
    sreg |= (hc_flag >> 7) & C_FLAG_MASK;

    datMem->write(wbAddr, &result);
    datMem->write(sregAddr, &sreg);
}

void AVRCPU::instruction_SBI() {
    /*************************SBI***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction SBI");

    wbAddr = ((instruction&0x00F8)>>3) + IOREG_BASEADDR;
    datMem->read(wbAddr, &result);
    result |= (0x01 << (instruction&0x0007));
    datMem->write(wbAddr, &result);
}

void AVRCPU::instruction_SBIC() {
    /*************************SBIC***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction SBIC");

    datMem->read(((instruction&0x00F8)>>3) + IOREG_BASEADDR, &result);

    result &= (0x01 << (instruction&0x0007));

    if (!result) {
        progMem->loadInstruction(pc++, &instruction);

        //Test 2 word instruction
        testJMP_CALL = instruction & INSTRUCTION_GROUP7_MASK;
        testLDS_STS = instruction & INSTRUCTION_GROUP8_MASK;
        if (testJMP_CALL == JMP_OPCODE ||
            testJMP_CALL == CALL_OPCODE||
            testLDS_STS  == LDS_OPCODE ||
            testLDS_STS  == STS_OPCODE) {
            pc += 1;
        }
    }
}

void AVRCPU::instruction_SBIS() {
    /*************************SBIS***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction SBIS");

    datMem->read(((instruction&0x00F8)>>3) + IOREG_BASEADDR, &result);

    result &= (0x01 << (instruction&0x0007));

    if (result) {
        progMem->loadInstruction(pc++, &instruction);

        //Test 2 word instruction
        testJMP_CALL = instruction & INSTRUCTION_GROUP7_MASK;
        testLDS_STS = instruction & INSTRUCTION_GROUP8_MASK;
        if (testJMP_CALL == JMP_OPCODE ||
            testJMP_CALL == CALL_OPCODE||
            testLDS_STS  == LDS_OPCODE ||
            testLDS_STS  == STS_OPCODE) {
            pc += 1;
        }
    }
}

void AVRCPU::instruction_SBIW() {
    /*************************SBIW***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction SBIW");

    offset = ((0x0030&instruction)>>3); //(>>4)*2 = >>3

    //SBIW operates on the upper four registers pairs
    dataLAddr = REG24_ADDR + offset;
    dataHAddr = REG25_ADDR + offset;

    datMem->read(dataLAddr, &dataL);
    datMem->read(dataHAddr, &dataH);
    datMem->read(sregAddr, &sreg);

    outData = ((dataH<<8) | dataL) - (((0x00C0 & instruction)>>2) | (0x000F & instruction));
    sreg &= 0xE0;

    //Flag V
    sreg |= ((dataH>>4)&((~outData)>>12))&V_FLAG_MASK;

    //Flag N
    sreg |= (outData>>13)&N_FLAG_MASK;

    //Flag S
    sreg |= (((sreg << 1) ^ sreg) << 1) & S_FLAG_MASK;

    //Flag Z
    sreg |= outData?0x0000:Z_FLAG_MASK;

    //Flag C
    sreg |= (((~dataH)>>7)&(outData>>15))&C_FLAG_MASK;

    datMem->write(dataLAddr, &outData);
    outData = outData>>8;
    datMem->write(dataHAddr, &outData);
    datMem->write(sregAddr, &sreg);
}

void AVRCPU::instruction_SBRC() {
    /*************************SBRC***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction SBRC");

    datMem->read((instruction&0x01F0)>>4, &result);

    result &= (0x01 << (instruction&0x0007));

    if (!result) {
        progMem->loadInstruction(pc++, &instruction);

        //Test 2 word instruction
        testJMP_CALL = instruction & INSTRUCTION_GROUP7_MASK;
        testLDS_STS = instruction & INSTRUCTION_GROUP8_MASK;
        if (testJMP_CALL == JMP_OPCODE ||
            testJMP_CALL == CALL_OPCODE||
            testLDS_STS  == LDS_OPCODE ||
            testLDS_STS  == STS_OPCODE) {
            pc += 1;
        }
    }
}

void AVRCPU::instruction_SBRS() {
    /*************************SBRS***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction SBRS");

    datMem->read((instruction&0x01F0)>>4, &result);

    result &= (0x01 << (instruction&0x0007));

    if (result) {
        progMem->loadInstruction(pc++, &instruction);

        //Test 2 word instruction
        testJMP_CALL = instruction & INSTRUCTION_GROUP7_MASK;
        testLDS_STS = instruction & INSTRUCTION_GROUP8_MASK;
        if (testJMP_CALL == JMP_OPCODE ||
            testJMP_CALL == CALL_OPCODE||
            testLDS_STS  == LDS_OPCODE ||
            testLDS_STS  == STS_OPCODE) {
            pc += 1;
        }
    }
}

void AVRCPU::instruction_SLEEP() {
    /*************************SLEEP***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction SLEEP - NOT IMPLEMENTED");
}

void AVRCPU::instruction_SPM_Z_UNCHANGED() {
    /*************************SPM (Z UNCHANGED)***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction SPM (Z UNCHANGED)");

    datMem->read(REG00_ADDR, &dataL);
    datMem->read(REG01_ADDR, &dataH);
    outData = (dataH<<8) | dataL;

    //Read Z Register
    datMem->read(REG30_ADDR, &dataL);
    datMem->read(REG31_ADDR, &dataH);
    wbAddr = ((dataH<<8) | dataL)<<1; //Z is a page/word address

    progMem->write(wbAddr, &outData);
    outData = outData >> 8;
    progMem->write(wbAddr+1, &outData);
}

void AVRCPU::instruction_SPM_POST_INCREMENT(){
    /*************************SPM (Z POST INCREMENT)***********************/
    LOGD(SOFIA_AVRCPU_TAG, "Instruction SPM (Z POST INCREMENT) - NOT IMPLEMENTED");
}

void AVRCPU::instruction_ST_X_UNCHANGED() {

}

void AVRCPU::instruction_ST_X_POST_INCREMENT() {

}

void AVRCPU::instruction_ST_X_PRE_DECREMENT() {

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


