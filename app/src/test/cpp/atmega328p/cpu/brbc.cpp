// Kollins G. Lima - 10/04/2020
// UNIT TEST FOR BRBC INSTRUCTION

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
    unsigned long result;
    sbyte sreg;
} Out;

Out output;

void testBRBC (unsigned long initPC, sbyte initSreg, sword16 instruction) {
    sbyte sreg = initSreg;
    unsigned long result = initPC;

    sbyte offset = instruction&0x0007;
    printf("OFFSET: %d\n", (((__int8_t)(instruction&0x03F8))<<6>>9));
    printf("MASK: %X\n", ~(((__int8_t )(sreg<<(7-offset)))>>7));
    result += (((__int8_t)(instruction&0x03F8))<<6>>9)&(~(((__int8_t )(sreg<<(7-offset)))>>7)); //Cast to make sign extension

    output.result = result;
}

int main(int argc, char *argv[])
{
    printf("BranchWithI ReturnPC 1\n");
    testBRBC(0x00, 0x00, 0x000F);
    printf("Result: %X\n", output.result);
    assert(output.result == 0x01);

    printf("NoBranchWithI ReturnPC 0\n");
    testBRBC(0x00, 0x80, 0x000F);
    printf("Result: %X\n", output.result);
    assert(output.result == 0x00);

    printf("BranchWithT ReturnPC 2\n");
    testBRBC(0x00, 0x00, 0x0016);
    printf("Result: %X\n", output.result);
    assert(output.result == 0x02);

    printf("NoBranchWithT ReturnPC 0\n");
    testBRBC(0x00, 0x40, 0x0016);
    printf("Result: %X\n", output.result);
    assert(output.result == 0x00);

    printf("BranchWithH ReturnPC 3\n");
    testBRBC(0x00, 0x00, 0x001D);
    printf("Result: %X\n", output.result);
    assert(output.result == 0x03);

    printf("NoBranchWithH ReturnPC 0\n");
    testBRBC(0x00, 0x20, 0x001D);
    printf("Result: %X\n", output.result);
    assert(output.result == 0x00);

    printf("BranchWithS ReturnPC 4\n");
    testBRBC(0x00, 0x00, 0x0024);
    printf("Result: %X\n", output.result);
    assert(output.result == 0x04);

    printf("NoBranchWithS ReturnPC 0\n");
    testBRBC(0x00, 0x10, 0x0024);
    printf("Result: %X\n", output.result);
    assert(output.result == 0x00);

    printf("BranchWithV ReturnPC 5\n");
    testBRBC(0x00, 0x00, 0x002B);
    printf("Result: %X\n", output.result);
    assert(output.result == 0x05);

    printf("NoBranchWithV ReturnPC 0\n");
    testBRBC(0x00, 0x08, 0x002B);
    printf("Result: %X\n", output.result);
    assert(output.result == 0x00);

    printf("BranchWithN ReturnPC 6\n");
    testBRBC(0x00, 0x00, 0x0032);
    printf("Result: %X\n", output.result);
    assert(output.result == 0x06);

    printf("NoBranchWithN ReturnPC 0\n");
    testBRBC(0x00, 0x04, 0x0032);
    printf("Result: %X\n", output.result);
    assert(output.result == 0x00);

    printf("BranchWithZ ReturnPC 7\n");
    testBRBC(0x00, 0x00, 0x0039);
    printf("Result: %X\n", output.result);
    assert(output.result == 0x07);

    printf("NoBranchWithZ ReturnPC 0\n");
    testBRBC(0x00, 0x02, 0x0039);
    printf("Result: %X\n", output.result);
    assert(output.result == 0x00);

    printf("BranchWithC ReturnPC 8\n");
    testBRBC(0x00, 0x00, 0x0040);
    printf("Result: %X\n", output.result);
    assert(output.result == 0x08);

    printf("NoBranchWithC ReturnPC 0\n");
    testBRBC(0x00, 0x01, 0x0040);
    printf("Result: %X\n", output.result);
    assert(output.result == 0x00);

    printf("BranchWithCNegative ReturnPC 0\n");
    testBRBC(0x01, 0x00, 0x02F8);
    printf("Result: %X\n", output.result);
    assert(output.result == 0x00);

    return 0;
}

