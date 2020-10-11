// Kollins G. Lima - 10/11/2020

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

int main(int argc, char *argv[])
{
    //Test shift 8b to 16b
    sbyte dataL = 0xCD;
    sbyte dataH = 0xAB;
    sword16 outData = (dataH<<8) | dataL;
    assert(outData = 0xABCD);

    return 0;
}

