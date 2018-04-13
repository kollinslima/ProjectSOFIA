package com.example.kollins.androidemulator.uCInterfaces;

public interface InterruptionModule {

    void checkIOInterruption(int pinAddress, int pinPosition, boolean oldState, boolean newState);

    void setMemory(DataMemory dataMemory);

    boolean haveInterruption();

    char getPCInterruptionAddress();

    void disableGlobalInterruptions();
    void enableGlobalInterruptions();

    void timer0Overflow();
}
