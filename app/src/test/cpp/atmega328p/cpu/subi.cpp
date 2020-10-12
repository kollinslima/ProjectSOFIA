// Kollins G. Lima - 10/12/2020
// UNIT TEST FOR SUBI INSTRUCTION

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

typedef uint8_t sbyte;
typedef uint16_t sword16;

typedef struct {
    sbyte result;
    sbyte outL;
    sbyte outH;
    sbyte sreg;
} Out;

Out output;

void testSUBI (sbyte regD, sbyte initSreg, sword16 instruction) {
    sbyte sreg = initSreg;
    sbyte result;
    sbyte immediate = ((0x0F00 & instruction) >> 4) | (0x000F & instruction);

    result = regD - immediate;
    sreg &= 0xC0;

    sbyte immediate_and_result = immediate & result;
    sbyte not_result = ~result;
    sbyte not_regD = ~regD;

    sbyte hc_flag = (not_regD & immediate) | immediate_and_result | (result & not_regD);

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

    output.sreg = sreg;
    output.result = result;
}

int main(int argc, char *argv[])
{
    printf("SubOneAndZero - Return 1\n");
    testSUBI(1,0x00,0x00F0);
    printf("Result: %X\n", output.result);
    assert(output.result == 0x01);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0x00);

    printf("SubZeroAndOneUnderflow - Return 1\n");
    testSUBI(0,0x00,0x00F1);
    printf("Result: %X\n", output.result);
    assert(output.result == 0xFF);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0x35);

    printf("SubOneAndOne - Return 0\n");
    testSUBI(1,0x00,0x00F1);
    printf("Result: %X\n", output.result);
    assert(output.result == 0x00);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0x02);

    printf("SubTwoComplementUnderflow - Return 0x7F\n");
    testSUBI(0x80,0x00,0x00F1);
    printf("Result: %X\n", output.result);
    assert(output.result == 0x7F);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0x38);

    return 0;
}

