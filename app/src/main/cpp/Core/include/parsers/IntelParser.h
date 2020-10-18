//
// Created by kollins on 13/09/20.
//

#ifndef PROJECTSOFIA_INTELPARSER_H
#define PROJECTSOFIA_INTELPARSER_H

#include "../devices/components/generic/GenericProgramMemory.h"

#define INTEL_PARSE_SUCCESS 0
#define INTEL_CHECKSUM_ERROR -1
#define INTEL_INVALID_FILE -2
#define INTEL_FILE_OPEN_FAILED -3

class IntelParser {

    public:
        static int parse(int fd, GenericProgramMemory *progMem);

    private:
        static bool checksum(sbyte *bytes, int bytesLen);
};


#endif //PROJECTSOFIA_INTELPARSER_H
