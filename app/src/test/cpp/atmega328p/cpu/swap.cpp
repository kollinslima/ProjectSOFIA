// Kollins G. Lima - 10/12/2020
// UNIT TEST FOR SWAP INSTRUCTION

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
    sbyte outL;
    sbyte outH;
    sbyte sreg;
} Out;

Out output;

void testSWAP (sbyte regD, sbyte initSreg, sword16 instruction) {
    sbyte sreg = initSreg;
    sbyte result;

    result = (regD<<4)|(regD>>4);

    output.result = result;
}

int main(int argc, char *argv[])
{
    printf("Swap0x0F - return 0xF0 \n");
    testSWAP(0x0F,0x00,0x0000);
    printf("Result: %X\n", output.result);
    assert(output.result == 0xF0);

    printf("Swap0xF0 - return 0x0F \n");
    testSWAP(0xF0,0x00,0x0000);
    printf("Result: %X\n", output.result);
    assert(output.result == 0x0F);

    return 0;
}

