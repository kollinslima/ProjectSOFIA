// Kollins G. Lima - 10/12/2020
// UNIT TEST FOR SBIW INSTRUCTION

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

void testSBIW (sbyte dataL, sbyte dataH, sbyte initSreg, sword16 instruction) {
    sbyte sreg = initSreg;
    sbyte result;

    sword16 outData = ((dataH<<8) | dataL) - (((0x00C0 & instruction)>>2) | (0x000F & instruction));
    sreg &= 0xE0;

    //Flag V
    sreg |= ((dataH>>4)&((~outData)>>12))&V_FLAG_MASK;

    //Flag N
    sreg |= (outData>>13)&N_FLAG_MASK;

    //Flag S
    sreg |= (((sreg << 1) ^ sreg) << 1) & S_FLAG_MASK;

    //Flag Z
    sreg |= outData?0x0000:Z_FLAG_MASK;

    //Flag C
    sreg |= (((~dataH)>>7)&(outData>>15))&C_FLAG_MASK;

    output.outL = outData;
    output.outH = outData >> 8;
    output.sreg = sreg;
}

int main(int argc, char *argv[])
{
    printf("SubZero24And25 - Return 0 and 0\n");
    testSBIW(0,0,0x00,0x0000);
    printf("DataL: %X\n", output.outL);
    printf("DataH: %X\n", output.outH);
    assert(output.outL == 0);
    assert(output.outH == 0);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0x02);

    printf("SubOneTwoComplementOverflow26And27 - Return 0x7FFF\n");
    testSBIW(0x00,0x80,0x00,0x0011);
    printf("DataL: %X\n", output.outL);
    printf("DataH: %X\n", output.outH);
    assert(output.outL == 0xFF);
    assert(output.outH == 0x7F);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0x18);

    printf("SubOneUnderflow28And29 - Return 0xFFFF\n");
    testSBIW(0x00,0x00,0x00,0x0021);
    printf("DataL: %X\n", output.outL);
    printf("DataH: %X\n", output.outH);
    assert(output.outL == 0xFF);
    assert(output.outH == 0xFF);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0x15);

    printf("SubTen30And31 - Return 0xFF14\n");
    testSBIW(0x0A,0xFF,0x00,0x003A);
    printf("DataL: %X\n", output.outL);
    printf("DataH: %X\n", output.outH);
    assert(output.outL == 0x00);
    assert(output.outH == 0xFF);
    printf("SREG: %X\n", output.sreg);
    assert(output.sreg == 0x14);

    return 0;
}

