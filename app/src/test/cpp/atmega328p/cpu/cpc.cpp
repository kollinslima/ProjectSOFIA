// Kollins G. Lima - 10/10/2020
// UNIT TEST FOR ADD INSTRUCTION

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

void testCPC (sbyte regD, sbyte regR, sbyte initSreg) {
    sbyte sreg = initSreg;
    sbyte result;

    result = regD - regR - (sreg & C_FLAG_MASK);
    sreg &= 0xC2; //Do not clear previous Z flag

    sbyte regR_and_result = regR & result;
    sbyte not_result = ~result;
    sbyte not_regD = ~regD;

    sbyte hc_flag = (not_regD & regR) | regR_and_result | (result & not_regD);

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

    output.sreg = sreg;
}

int main(int argc, char *argv[])
{
    printf("CompareEqualWithoutCarry\n");
    testCPC(1,1,0x02);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0x02);

    printf("CompareGreaterWithCarry\n");
    testCPC(2,1,0x03);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0x02);

    printf("CompareLowerWithoutCarry\n");
    testCPC(1,2,0x02);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0x35);

    printf("CompareTwoComplementOverflowWithCarry\n");
    testCPC(0x7F,0x80,0x03);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0x0D);

    return 0;
}

