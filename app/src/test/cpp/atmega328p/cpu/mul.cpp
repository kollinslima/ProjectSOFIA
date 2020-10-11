// Kollins G. Lima - 10/10/2020
// UNIT TEST FOR FMUL INSTRUCTION

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

void testMUL (sbyte regD, sbyte regR, sbyte initSreg) {
    sbyte sreg = initSreg;
    sbyte result;

    sword16 outData = regD * regR;
    sreg &= 0xFC;

    //Flag Z
    sreg |= outData?0x0000:Z_FLAG_MASK;

    //Flag C
    sreg |= (outData>>15)&C_FLAG_MASK;

    output.outL = outData;
    output.outH = outData >> 8;
    output.sreg = sreg;
}

int main(int argc, char *argv[])
{
    printf("0x40Times0x40 - Return 0x1000\n");
    testMUL(0x40,0x40,0x00);
    printf("DataL: %X\n", output.outL);
    printf("DataH: %X\n", output.outH);
    assert(output.outL == 0x00);
    assert(output.outH == 0x10);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0x00);

    printf("0x40Times0 - Return 0x0000\n");
    testMUL(0x40,0,0x00);
    printf("DataL: %X\n", output.outL);
    printf("DataH: %X\n", output.outH);
    assert(output.outL == 0x00);
    assert(output.outH == 0x00);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0x02);

    printf("0xFFTimes0xFF - Return 0xFE01\n");
    testMUL(0xFF,0xFF,0x00);
    printf("DataL: %X\n", output.outL);
    printf("DataH: %X\n", output.outH);
    assert(output.outL == 0x01);
    assert(output.outH == 0xFE);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0x01);

    return 0;
}

