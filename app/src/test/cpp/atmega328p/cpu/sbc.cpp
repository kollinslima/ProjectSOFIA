// Kollins G. Lima - 10/11/2020
// UNIT TEST FOR SBC INSTRUCTION

#include <stdio.h>
#include <iostream>
#include <assert.h>

using namespace std;

#define H_FLAG_MASK 0x20
#define S_FLAG_MASK 0x10
#define V_FLAG_MASK 0x08
#define N_FLAG_MASK 0x04
#define Z_FLAG_MASK 0x02
#define C_FLAG_MASK 0x01

typedef unsigned char sbyte;

typedef struct {
    sbyte result;
    sbyte sreg;
} Out;

Out output;

void testSBC (sbyte regD, sbyte regR, sbyte initSreg) {
    sbyte sreg = initSreg;
    sbyte result;

    result = regD - regR - (sreg & C_FLAG_MASK);
    sreg &= 0xC0;

    sbyte not_regD = (~regD);
    sbyte notRegD_and_regR = not_regD & regR;

    sbyte hc_flag = notRegD_and_regR | (regR & result) | (result & not_regD);

    //Flag H
    sreg |= (hc_flag << 2) & H_FLAG_MASK;

    //Flag V
    sreg |= (((regD & (~regR) & (~result)) | (notRegD_and_regR & result)) >> 4) & V_FLAG_MASK;

    //Flag N
    sreg |= (result >> 5) & N_FLAG_MASK;

    //Flag S
    sreg |= (((sreg << 1) ^ sreg) << 1) & S_FLAG_MASK;

    //Flag Z
    sreg |= result?0x00:Z_FLAG_MASK;

    //Flag C
    sreg |= (hc_flag >> 7) & C_FLAG_MASK;

    output.result = result;
    output.sreg = sreg;
}

int main(int argc, char *argv[])
{
    printf("SubZeroNoCarry - Return 0\n");
    testSBC(0,0,0x00);
    printf("Result: %X\n", output.result);
    assert(output.result == 0);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0x02);

    printf("SubZeroAndCarry - Return -1\n");
    testSBC(0,0,0x01);
    printf("Result: %X\n", output.result);
    assert(output.result == 0xFF);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0x35);

    printf("SubUnderflowNoCarry - Return -1\n");
    testSBC(0x00,1,0x00);
    printf("Result: %X\n", output.result);
    assert(output.result == 0xFF);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0x35);

    printf("SubTwoComplementUnderflowWithCarry - Return -127\n");
    testSBC(0x80,0,0x01);
    printf("Result: %X\n", output.result);
    assert(output.result == 0x7F);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0x38);

    return 0;
}

