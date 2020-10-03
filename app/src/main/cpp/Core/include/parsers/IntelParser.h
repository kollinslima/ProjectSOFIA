//
// Created by kollins on 13/09/20.
//

#ifndef PROJECTSOFIA_INTELPARSER_H
#define PROJECTSOFIA_INTELPARSER_H

#include "../devices/components/generic/GenericProgramMemory.h"

class IntelParser {

    public:
        static bool parse(int fd, GenericProgramMemory *progMem);

    private:
        static bool checksum(sbyte *bytes, int bytesLen);
};


#endif //PROJECTSOFIA_INTELPARSER_H
