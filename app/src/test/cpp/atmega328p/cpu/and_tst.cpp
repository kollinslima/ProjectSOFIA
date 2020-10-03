// Kollins G. Lima - 10/03/2020
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

void testAND (sbyte regD, sbyte regR, sbyte initSreg) {
    sbyte sreg = initSreg;
    sbyte result;

    result = regD & regR;
    sreg &= 0xE1;

    //Flag N
    sreg |= (result >> 5) & N_FLAG_MASK;

    //Flag S
    sreg |= (((sreg << 1) ^ sreg) << 1) & S_FLAG_MASK;

    //Flag Z
    sreg |= result?0x00:Z_FLAG_MASK;

    output.result = result;
    output.sreg = sreg;
}

int main(int argc, char *argv[])
{
    printf("ZeroAndZero - Return 0\n");
    testAND(0,0,0x00);
    printf("Result: %X\n", output.result);
    assert(output.result == 0);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0x02);

    printf("ZeroAndFF - Return 0\n");
    testAND(0,0xFF,0x00);
    printf("Result: %X\n", output.result);
    assert(output.result == 0);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0x02);

    printf("FFAndF0 - Return 0xF0\n");
    testAND(0xFF,0xF0,0x00);
    printf("Result: %X\n", output.result);
    assert(output.result == 0xF0);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0x14);


    return 0;
}

