//
// Created by kollins on 13/09/20.
//

#ifndef PROJECTSOFIA_INTELPARSER_H
#define PROJECTSOFIA_INTELPARSER_H


#include "../devices/components/GenericMemory.h"

class IntelParser {

    public:
        static bool parse(int fd, GenericMemory *progMem);

    private:
        static bool checksum(unsigned char *bytes, int bytesLen);
};


#endif //PROJECTSOFIA_INTELPARSER_H
