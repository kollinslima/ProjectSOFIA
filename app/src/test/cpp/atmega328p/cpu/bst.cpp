// Kollins G. Lima - 10/04/2020
// UNIT TEST FOR BST INSTRUCTION

#include <stdio.h>
#include <iostream>
#include <assert.h>

using namespace std;

#define T_FLAG_MASK 0x40
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

void testBST (sbyte regD, sbyte initSreg, sword16 instruction) {
    sbyte sreg = initSreg;

    sbyte offset = instruction&0x0007;
    sreg &= ~T_FLAG_MASK;
    sreg |= ((regD>>offset)&0x01)<<6;

    output.sreg = sreg;
}

int main(int argc, char *argv[])
{
    printf("MoveTrueFromPositionFour\n");
    testBST(0xFF, 0x00, 0x0084);
    printf("SREG : %X\n", output.sreg);
    assert(output.sreg == 0x40);

    printf("MoveFalseFromPositionFive\n");
    testBST(0x00, 0xFF, 0x0085);
    printf("SREG : %X\n", output.sreg);
    assert(output.sreg == 0xBF);

    return 0;
}

