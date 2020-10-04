// Kollins G. Lima - 10/04/2020
// UNIT TEST FOR BLD INSTRUCTION

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

void testBLD (sbyte regD, sbyte initSreg, sword16 instruction) {
    sbyte sreg = initSreg;
    sbyte result = regD;

    sbyte offset = instruction&0x0007;
    result &= ~(0x01 << offset);
    result |= ((sreg&T_FLAG_MASK)>>6)<<offset;

    output.result = result;
    output.sreg = sreg;
}

int main(int argc, char *argv[])
{
    printf("MoveTrueToPositionOne - Return 2\n");
    testBLD(0, 0x40, 0x00F1);
    printf("Result: %X\n", output.result);
    assert(output.result == 0x02);

    printf("MoveFalseToPositionZero - Return 6\n");
    testBLD(0x07, 0x00, 0x00F0);
    printf("Result: %X\n", output.result);
    assert(output.result == 0x06);

    return 0;
}

