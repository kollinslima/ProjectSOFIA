// Kollins G. Lima - 09/27/2020
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

void testADD (sbyte regD, sbyte regR, sbyte initSreg) {
    sbyte sreg = initSreg;
    sbyte result;

    result = regD + regR;
    sreg = 0;

    sbyte regD_and_regR = regD & regR;
    sbyte not_result = ~result;

    sbyte hc_flag = regD_and_regR | (regR&not_result) | (not_result&regD);

    //Flag H
    sreg |= (hc_flag<<2)&H_FLAG_MASK;

    //Flag V
    sreg |= (((regD_and_regR & result) | ((~regD) & (~regR) & result) )>>4)&V_FLAG_MASK;

    //Flag N
    sreg |= (result>>5)&N_FLAG_MASK;

    //Flag S
    sreg |= (((sreg<<1)^sreg)<<1)&S_FLAG_MASK;

    //Flag Z
    sreg |= (((((((((((((not_result>>1)&not_result)>>1)&not_result)>>1)&not_result)>>1)&not_result)>>1)&not_result)>>1)&not_result)&(not_result<<1))&Z_FLAG_MASK;

    //Flag C
    sreg |= (hc_flag>>7)&C_FLAG_MASK;

    output.result = result;
    output.sreg = sreg;
}

int main(int argc, char *argv[])
{
    printf("SumZeroAndOne - Return 1\n");
    testADD(0,1,0x00);
    printf("Result: %X\n", output.result);
    assert(output.result == 1);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0x00);

    printf("ShiftLeft - Return 2\n");
    testADD(1,1,0x00);
    printf("Result: %X\n", output.result);
    assert(output.result == 2);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0x00);

    printf("SumOverflow - Return 0\n");
    testADD(0xFF,1,0x00);
    printf("Result: %X\n", output.result);
    assert(output.result == 0);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0x23);

    printf("SumTwoComplementOverflow - Return 0\n");
    testADD(0x7F,1,0x00);
    printf("Result: %X\n", output.result);
    assert(output.result == 0x80);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0x2C);

    printf("SumTwoComplementWithoutOverflow - Return 0\n");
    testADD(0x80,1,0x00);
    printf("Result: %X\n", output.result);
    assert(output.result == 0x81);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0x14);

    printf("RandomSum\n");
    testADD(13,29,0x00);
    printf("Result: %X\n", output.result);
    assert(output.result == 0x2A);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0x20);

    return 0;
}

