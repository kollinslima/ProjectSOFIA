// Kollins G. Lima - 10/11/2020
// UNIT TEST FOR NEG INSTRUCTION

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

void testNEG (sbyte regD, sbyte regR, sbyte initSreg) {
    sbyte sreg = initSreg;
    sbyte result;

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

    output.result = result;
    output.sreg = sreg;
}

int main(int argc, char *argv[])
{
    printf("NegZero - Return 0\n");
    testNEG(0x00,0x00,0x00);
    printf("Result: %X\n", output.result);
    assert(output.result == 0x00);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0x02);

    printf("Neg0x80 - Return 0x80\n");
    testNEG(0x80,0x00,0x00);
    printf("Result: %X\n", output.result);
    assert(output.result == 0x80);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0x0D);

    printf("Neg0x08 - Return 0xF8\n");
    testNEG(0x08,0x00,0x00);
    printf("Result: %X\n", output.result);
    assert(output.result == 0xF8);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0x35);

    return 0;
}

