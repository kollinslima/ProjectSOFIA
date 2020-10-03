//
// Created by kollins on 13/09/20.
//

#include <cstdio>
#include "../../include/parsers/IntelParser.h"
#include "../../include/devices/components/generic/GenericMemory.h"
#include "../../include/CommonCore.h"
#include "../../include/utils/Functions.h"

#define INTEL_DATA_SIZE 0
#define INTEL_ADDRESS 1
#define INTEL_RECORD_TYPE 3
#define INTEL_DATA 4

#define MAX_READ_BUFFER 600

#define SOFIA_INTEL_PARSER_TAG "SOFIA INTEL PARSER"

bool IntelParser::parse(int fd, GenericProgramMemory *progMem) {
    FILE *fp = fdopen(fd, "r");
    if (fp) {
        LOGD(SOFIA_INTEL_PARSER_TAG, "Hex file opened");
        char line[MAX_READ_BUFFER];
        bool parseFail = false;

        bool extendedSegmentAddress = false;
        bool extendedLinearAddress = false;
        unsigned int extendedAddress = 0;

        while (fscanf(fp, "%s", line) == 1){
            sbyte bytes[MAX_READ_BUFFER/2];
            char *cleanLine = line + 1; //skip colon
            LOGD(SOFIA_INTEL_PARSER_TAG, "%s", cleanLine);

            int bytesLen = Functions::hexStrToByte(cleanLine, bytes);
            if (bytesLen < 0) {
                LOGE(SOFIA_INTEL_PARSER_TAG, "Error reading file");
                parseFail = true;
                break;
            }

            if (!checksum(bytes, bytesLen)){
                LOGE(SOFIA_INTEL_PARSER_TAG, "Checksum error");
                parseFail = true;
                break;
            }

            switch (bytes[INTEL_RECORD_TYPE]) {
                //Data
                case 00: {
                    unsigned long address = 0;
                    address = (bytes[INTEL_ADDRESS] << 8) | bytes[INTEL_ADDRESS + 1];
                    if (extendedSegmentAddress) {
                        address += extendedAddress;
                    } else if (extendedLinearAddress) {
                        address = (extendedAddress<<16) | address;
                    }

                    for (int i = 0; i < bytes[INTEL_DATA_SIZE]; ++i) {
//                        LOGD(SOFIA_INTEL_PARSER_TAG, "Loading byte %X to address %X",
//                                bytes[INTEL_DATA+i], (address+i));
                        progMem->write(address+i, &bytes[INTEL_DATA+i]);
                    }
                    break;
                }
                //End of File
                case 01: {
                    LOGI(SOFIA_INTEL_PARSER_TAG, "Hex code loaded to program memory.");
                    break;
                }
                //Beginning of memory segment
                case 03: {
                    LOGW(SOFIA_INTEL_PARSER_TAG,
                         "Record type 0x03 not implemented: Beginning of memory segment");
                    break;
                }
                //Extended segment address
                case 02: {
                    extendedSegmentAddress = true;
                    extendedLinearAddress = false;
                    extendedAddress = (bytes[INTEL_ADDRESS] << 8) | bytes[INTEL_ADDRESS+1];
                    LOGI(SOFIA_INTEL_PARSER_TAG, "Address extended by: %X (Extended segment)", extendedAddress);
                    break;
                }
                //Extended linear address
                case 04: {
                    extendedSegmentAddress = false;
                    extendedLinearAddress = true;
                    extendedAddress = (bytes[INTEL_ADDRESS] << 8) | bytes[INTEL_ADDRESS+1];
                    LOGI(SOFIA_INTEL_PARSER_TAG, "Address extended by: %X (Extended linear)", extendedAddress);
                    break;
                }
                default:
                    LOGE(SOFIA_INTEL_PARSER_TAG, "Record type unknown");
                    break;
            }
        }
        fclose(fp);
        return !parseFail;
    }
    return false;
}

bool IntelParser::checksum(sbyte *bytes, int bytesLen) {
    unsigned int sum = 0;
    for (int i = 0; i < bytesLen-1; ++i) {
        sum += bytes[i];
    }
//    LOGD(SOFIA_INTEL_PARSER_TAG, "CHECK BYTE: %X", bytes[bytesLen-1]);
//    LOGD(SOFIA_INTEL_PARSER_TAG, "SUM: %X", (((~sum)+1))&0xFF);
    return (bytes[bytesLen-1] == (((~sum)+1)&0xFF));
}

