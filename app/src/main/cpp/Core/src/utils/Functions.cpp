//
// Created by kollins on 13/09/20.
//

#include <cstring>
#include "../../include/utils/Functions.h"
#include "../../include/CommonCore.h"

#define SOFIA_FUNCTIONS_TAG "SOFIA FUNCTIONS"

int Functions::hexStrToByte(const char* hexStr, sbyte *byes) {
    size_t len = strlen(hexStr);
    if (len % 2 != 0) {
        return -1;
    }
    size_t final_len = len / 2;
    for (size_t i=0, j=0; j<final_len; i+=2, j++) {
        byes[j] = (hexStr[i] % 32 + 9) % 25 * 16 + (hexStr[i + 1] % 32 + 9) % 25;
//        LOGD(SOFIA_FUNCTIONS_TAG, "CHAR: %c%c",hexStr[i], hexStr[i+1]);
//        LOGD(SOFIA_FUNCTIONS_TAG, "BYTES: %X",bytes[j]);
    }
    return final_len;
}