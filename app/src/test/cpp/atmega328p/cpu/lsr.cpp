// Kollins G. Lima - 10/11/2020
// UNIT TEST FOR LSR INSTRUCTION

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

void testLSR (sbyte regD, sbyte regR, sbyte initSreg) {
    sbyte sreg = initSreg;
    sbyte result;

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

    output.result = result;
    output.sreg = sreg;
}

int main(int argc, char *argv[])
{
    printf("ShiftRight - Return 2\n");
    testLSR(0x04,0,0x00);
    printf("Result: %X\n", output.result);
    assert(output.result == 0x02);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0x00);

    printf("ShiftRightSighExtention - Return 64\n");
    testLSR(0x80,0,0x00);
    printf("Result: %X\n", output.result);
    assert(output.result == 0x40);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0x00);

    printf("ShiftRightLastShift - Return 0\n");
    testLSR(0x01,0,0x00);
    printf("Result: %X\n", output.result);
    assert(output.result == 0x00);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0x1B);

    printf("AllZero - Return 0\n");
    testLSR(0x00,0,0x00);
    printf("Result: %X\n", output.result);
    assert(output.result == 0x00);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0x02);

    return 0;
}

