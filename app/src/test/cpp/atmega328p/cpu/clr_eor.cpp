// Kollins G. Lima - 10/10/2020
// UNIT TEST FOR CLR/EOR INSTRUCTION

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

    result = regD ^ regR;
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
    printf("ClearRegister\n");
    testADD(0x0F,0x0F,0x00);
    printf("Result: %X\n", output.result);
    assert(output.result == 0);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0x02);

    printf("ZeroXor85 - Return 85\n");
    testADD(85,0x00,0x01);
    printf("Result: %X\n", output.result);
    assert(output.result == 85);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0x01);

    printf("FFXor85 - Return -86\n");
    testADD(85,0xFF,0x00);
    printf("Result: %X\n", output.result);
    assert(output.result == 0xAA);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0x14);

    return 0;
}

