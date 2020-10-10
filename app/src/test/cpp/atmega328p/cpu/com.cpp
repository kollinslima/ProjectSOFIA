// Kollins G. Lima - 10/10/2020
// UNIT TEST FOR COM INSTRUCTION

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

    result = ~regD;
    sreg &= 0xE0;

    //Flag N
    sreg |= (result >> 5) & N_FLAG_MASK;

    //Flag S
    sreg |= (((sreg << 1) ^ sreg) << 1) & S_FLAG_MASK;

    //Flag Z
    sreg |= result?0x00:Z_FLAG_MASK;

    //Flag C
    sreg |= C_FLAG_MASK;

    output.result = result;
    output.sreg = sreg;
}

int main(int argc, char *argv[])
{
    printf("ComplementAllZero - Return -1\n");
    testADD(0x00,0x00,0x00);
    printf("Result: %X\n", output.result);
    assert(output.result == 0xFF);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0x15);

    printf("ComplementAllOne - Return 0\n");
    testADD(0xFF,0x00,0x00);
    printf("Result: %X\n", output.result);
    assert(output.result == 0x0);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0x03);

    printf("ComplementHalf - Return 15\n");
    testADD(0xF0,0x00,0x00);
    printf("Result: %X\n", output.result);
    assert(output.result == 0x0F);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0x01);

    return 0;
}

