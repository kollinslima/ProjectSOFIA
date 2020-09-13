//
// Created by kollins on 12/09/20.
//

#ifndef PROJECTSOFIA_GENERICDEVICE_H
#define PROJECTSOFIA_GENERICDEVICE_H

#include <string>
using namespace std;

class GenericDevice {

    public:
        virtual ~GenericDevice() {};

        virtual bool loadFile(int fd)=0;
        virtual void run()=0;

};


#endif //PROJECTSOFIA_GENERICDEVICE_H
