// Kollins G. Lima - 10/10/2020
// UNIT TEST FOR FMULS INSTRUCTION

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

void testFMULS (sbyte regD, sbyte regR, sbyte initSreg) {
    sbyte sreg = initSreg;
    sbyte result;

    sword16 outData = ((__int8_t)regD) * ((__int8_t)regR);
    sreg &= 0xFC;

    //Flag Z
    sreg |= outData?0x0000:Z_FLAG_MASK;

    //Flag C
    sreg |= (outData>>15)&C_FLAG_MASK;

    //A left shift is necessary according to the documentation
    outData = outData << 1;

    output.outL = outData;
    output.outH = outData >> 8;
    output.sreg = sreg;
}

int main(int argc, char *argv[])
{

    printf("0xC0Times0x40 - Return 0xE000\n");
    testFMULS(0xC0,0x40,0x00); //(-0,5)*0,5 = -0,25 (1,75 in two complement)
    printf("DataL: %X\n", output.outL);
    printf("DataH: %X\n", output.outH);
    assert(output.outL == 0x00);
    assert(output.outH == 0xE0);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0x01);

    printf("0x80Times0x80 - Return 00 and 00\n");
    testFMULS(0x80,0x80,0x00); //-1*-1 = -1 (two complement overflow)
    printf("DataL: %X\n", output.outL);
    printf("DataH: %X\n", output.outH);
    assert(output.outL == 0x00);
    assert(output.outH == 0x80);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0x00);

    printf("SquareHalf - Return 20 and 00\n");
    testFMULS(0x40,0x40,0x00); //0,5*0,5 = 0,25
    printf("DataL: %X\n", output.outL);
    printf("DataH: %X\n", output.outH);
    assert(output.outL == 0x00);
    assert(output.outH == 0x20);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0x00);

    printf("HalfTimesZero - Return 00 and 00\n");
    testFMULS(0x40,0x00,0x00); //0,5*0 = 0
    printf("DataL: %X\n", output.outL);
    printf("DataH: %X\n", output.outH);
    assert(output.outL == 0x00);
    assert(output.outH == 0x00);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0x02);

    return 0;
}

