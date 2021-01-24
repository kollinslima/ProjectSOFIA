//
// Created by kollins on 13/09/20.
//

#include <cstring>
#include <cstdlib>
#include "../../include/utils/Functions.h"
#include "../../include/CommonCore.h"

#define SOFIA_FUNCTIONS_TAG "SOFIA FUNCTIONS"

//constexpr char hexmap[] = {'0', '1', '2', '3', '4', '5', '6', '7',
//                           '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
//
//char *Functions::byteToHexStr(sbyte *data, int len) {
//    char *s = (char *)malloc((len*2)*sizeof(char));
//    for (int i = 0; i < len; ++i) {
//        int pos = 2*i;
//        s[pos]     = hexmap[(data[i] & 0xF0) >> 4];
//        s[pos + 1] = hexmap[data[i] & 0x0F];
//    }
//    return s;
//}

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