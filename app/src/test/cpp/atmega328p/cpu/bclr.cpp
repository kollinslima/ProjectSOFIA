// Kollins G. Lima - 10/04/2020
// UNIT TEST FOR BCLR INSTRUCTION

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
    sbyte sreg;
} Out;

Out output;

void testBCLR (sbyte initSreg, sword16 instruction) {
    sbyte sreg = initSreg;

    sreg &= ~(0x01 << ((instruction>>4)&0x0007));

    output.sreg = sreg;
}

int main(int argc, char *argv[])
{
    printf("ClearI\n");
    testBCLR(0xFF, 0x00F0);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0x7F);

    printf("ClearT\n");
    testBCLR(0xFF, 0x00E0);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0xBF);

    printf("ClearH\n");
    testBCLR(0xFF, 0x00D0);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0xDF);

    printf("ClearS\n");
    testBCLR(0xFF, 0x00C0);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0xEF);

    printf("ClearV\n");
    testBCLR(0xFF, 0x00B0);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0xF7);

    printf("ClearN\n");
    testBCLR(0xFF, 0x00A0);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0xFB);

    printf("ClearZ\n");
    testBCLR(0xFF, 0x0090);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0xFD);

    printf("ClearC\n");
    testBCLR(0xFF, 0x0080);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0xFE);

    return 0;
}

