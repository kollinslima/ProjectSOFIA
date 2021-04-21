//
// Created by kollins on 18/04/21.
//

#ifndef PROJECTSOFIA_TIMER_ATMEGA328P_H
#define PROJECTSOFIA_TIMER_ATMEGA328P_H

#include "../components/avr/timer/GenericAVRTimer.h"
#include "../components/avr/memory/GenericAVRDataMemory.h"

#define NUM_CLOCK_SOURCES   8
#define NUM_MODES           16

#define CS_MASK             0x07
#define FOCXA_MASK          0x80
#define FOCXB_MASK          0x40
#define COMXA_MASK          0xC0
#define COMXB_MASK          0x30

#define PRESCALER_8_MASK    0x007
#define PRESCALER_32_MASK   0x01F
#define PRESCALER_64_MASK   0x03F
#define PRESCALER_128_MASK  0x07F
#define PRESCALER_256_MASK  0x0FF
#define PRESCALER_1024_MASK 0x3FF

class Timer_ATMega328P : public GenericAVRTimer {

public:
    Timer_ATMega328P(GenericAVRDataMemory *dataMemory);
    virtual ~Timer_ATMega328P() {}

private:
    sbyte outReg;
    void setupClockSourceDecoder();
    void setupOperationMode();

protected:
    GenericAVRDataMemory *dataMemory;

    sbyte tccrxaReg;
    sbyte tccrxbReg;
    sbyte interrFlags;

    sword16 progress, ocrxa, ocrxb, bottom;
    bool matchA, matchB;
    sbyte outARegAddr, outBRegAddr;
    sbyte ocxaMask, ocxbMask;

    typedef bool (Timer_ATMega328P::*ClockSource)();
    short clockCount;
    ClockSource clockSource[NUM_CLOCK_SOURCES];
    bool clockSource_000();  //No clock
    bool clockSource_001();  //No prescaler
    bool clockSource_010();  //Prescaler 8
    virtual bool clockSource_011() = 0;  //Prescaler 32 (Timer 2) or 64 (Timer 0 and Timer 1)
    virtual bool clockSource_100() = 0;  //Prescaler 64 (Timer 2) or 256 (Timer 0 and Timer 1)
    virtual bool clockSource_101() = 0;  //Prescaler 128 (Timer 2) or 1024 (Timer 0 and Timer 1)
    virtual bool clockSource_110() = 0;  //Prescaler 256 (Timer 2), External falling edge T0 (Timer 0) or T1 (Timer 1)
    virtual bool clockSource_111() = 0;  //Prescaler 1024 (Timer 2), External rising edge T0 (Timer 0) or T1 (Timer 1)

    typedef void (Timer_ATMega328P::*Mode)();
    Mode mode[NUM_MODES];
    virtual void normal();
    virtual void pwmPhaseCorrect1();            //8b (Timer 1)
    virtual void pwmPhaseCorrect2();            //9b (Timer 1), 8b (Timer 0 and Timer 2)
    virtual void pwmPhaseCorrect3();            //10b (Timer 1)
    virtual void ctc1();                        //TOP OCR
    virtual void fastPWM1();                    //8b (Timer 1)
    virtual void fastPWM2();                    //9b (Timer 1), 8b (Timer 0 and Timer 2)
    virtual void fastPWM3();                    //10b (Timer 1)
    virtual void pwmPhaseAndFreqCorrect1();     //TOP ICR (Timer 1), Reserved (Timer 0 and Timer 2)
    virtual void pwmPhaseAndFreqCorrect2();     //TOP OCR (Timer 1), Reserved (Timer 0 and Timer 2)
    virtual void pwmPhaseCorrect4();            //TOP ICR (Timer 1), TOP OCR (Timer 0 and Timer 2)
    virtual void pwmPhaseCorrect5();            //TOP OCR (Timer 1)
    virtual void ctc2();                        //TOP ICR (Timer 1) Reserved (Timer 0 and Timer 2)
    virtual void reserved();                    //Reserved for all
    virtual void fastPWM4();                    //TOP ICR (Timer 1), TOP OCR (Timer 0 and Timer 2)
    virtual void fastPWM5();                    //TOP OCR (Timer 1)
};

#endif //PROJECTSOFIA_TIMER_ATMEGA328P_H
