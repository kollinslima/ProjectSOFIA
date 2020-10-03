// Kollins G. Lima - 09/27/2020
// UNIT TEST FOR ADC INSTRUCTION

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

void testADC (byte regD, byte regR, byte initSreg) {
    byte sreg = initSreg;
    byte result;

    result = regD + regR + (sreg & 0x01);
    sreg = 0;

    sbyte regD_AND_regR = regD & regR;
    sbyte NOT_result = ~result;

    sbyte HC = regD_AND_regR | (regR&NOT_result) | (NOT_result&regD);
    //Flag H
    sreg |= (HC<<2)&H_FLAG_MASK;

    //Flag V
    sreg |= (((regD_AND_regR & result) | ((~regD) & (~regR) & result) )>>4)&V_FLAG_MASK;

    //Flag N
    sreg |= (result>>5)&N_FLAG_MASK;

    //Flag S
    sreg |= (((sreg<<1)^sreg)<<1)&S_FLAG_MASK;

    //Flag Z
    sreg |= (((((((((((((NOT_result>>1)&NOT_result)>>1)&NOT_result)>>1)&NOT_result)>>1)&NOT_result)>>1)&NOT_result)>>1)&NOT_result)&(NOT_result<<1))&Z_FLAG_MASK;

    //Flag C
    sreg |= (HC>>7)&C_FLAG_MASK; 

    output.result = result;
    output.sreg = sreg;
}

int main(int argc, char *argv[])
{
    printf("SumZeroNoCarry - Return 0\n");
    testADC(0,0,0x00);
    printf("Result: %X\n", output.result);
    assert(output.result == 0);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0x02);

    printf("SumZeroAndCarry - Return 1\n");
    testADC(0,0,0x01);
    printf("Result: %X\n", output.result);
    assert(output.result == 1);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0x00);

    printf("SumOverflowCarry - Return 0\n");
    testADC(0xFF,1,0x00);
    printf("Result: %X\n", output.result);
    assert(output.result == 0);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0x23);

    printf("SumTwoComplementOverflowWithCarry - Return -128\n");
    testADC(0x7F,0,0x01);
    printf("Result: %X\n", output.result);
    assert(output.result == 0x80);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0x2C);

    printf("SumTwoComplementWitoutOverflowNoCarry - Return -127\n");
    testADC(0x80,1,0x00);
    printf("Result: %X\n", output.result);
    assert(output.result == 0x81);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0x14);

    printf("RandomSum\n");
    testADC(13,29,0x00);
    printf("Result: %X\n", output.result);
    assert(output.result == 0x2A);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0x20);

    return 0;
}

