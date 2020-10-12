// Kollins G. Lima - 10/12/2020
// UNIT TEST FOR SUB INSTRUCTION

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

void testSUB (sbyte regD, sbyte regR, sbyte initSreg) {
    sbyte sreg = initSreg;
    sbyte result;

    result = regD - regR;
    sreg &= 0xC0;

    sbyte not_regD = ~regD;
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
    printf("SubZeroAndOne - Return 1\n");
    testSUB(1,0,0x00);
    printf("Result: %X\n", output.result);
    assert(output.result == 1);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0x00);

    printf("SubZeroAndOneUnderflow - Return -1\n");
    testSUB(0,1,0x00);
    printf("Result: %X\n", output.result);
    assert(output.result == 0xFF);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0x35);

    printf("SubOneAndOne - Return 0\n");
    testSUB(1,1,0x00);
    printf("Result: %X\n", output.result);
    assert(output.result == 0x00);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0x02);

    printf("SubTwoComplementUnderflow - Return 0x7F\n");
    testSUB(0x80,0x01,0x00);
    printf("Result: %X\n", output.result);
    assert(output.result == 0x7F);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0x38);

    return 0;
}

