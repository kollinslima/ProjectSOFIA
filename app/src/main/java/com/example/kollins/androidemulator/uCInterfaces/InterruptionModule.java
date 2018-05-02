package com.example.kollins.androidemulator.uCInterfaces;

public interface InterruptionModule {

    void checkIOInterruption(int pinAddress, int pinPosition, boolean oldState, boolean newState);

    void setMemory(DataMemory dataMemory);

    boolean haveInterruption();

    char getPCInterruptionAddress();

    void disableGlobalInterruptions();
    void enableGlobalInterruptions();

    void timer0Overflow();

    void timer0MatchA();

    void timer0MatchB();

    void timer1Overflow();

    void timer1MatchA();

    void timer1MatchB();

    void timer1InputCapture();

    void timer2Overflow();

    void timer2MatchA();

    void timer2MatchB();

    void conversionCompleteADC();
}
