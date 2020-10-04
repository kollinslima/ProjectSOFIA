// Kollins G. Lima - 10/04/2020
// UNIT TEST FOR ASR INSTRUCTION

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

typedef struct {
    sbyte result;
    sbyte sreg;
} Out;

Out output;

void testASR (sbyte regD, sbyte regR, sbyte initSreg) {
    sbyte sreg = initSreg;
    sbyte result;

    result = (0x80&regD)|(regD>>1);
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

    output.result = result;
    output.sreg = sreg;
}

int main(int argc, char *argv[])
{
    printf("ShiftTwo - Return 1\n");
    testASR(2,0,0x00);
    printf("Result: %X\n", output.result);
    assert(output.result == 1);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0x00);

    printf("ShiftOne - Return 0\n");
    testASR(1,0,0x00);
    printf("Result: %X\n", output.result);
    assert(output.result == 0);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0x1B);

    printf("SignExtention - Return 0\n");
    testASR(0x80,0,0x00);
    printf("Result: %X\n", output.result);
    assert(output.result == 0xC0);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0x0C);

    return 0;
}

