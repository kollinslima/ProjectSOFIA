package com.example.kollins.sofia.uCInterfaces;

public interface Timer2Module extends Runnable {

    int NO_CLOCK_SOURCE                 = 0;
    int CLOCK_PRESCALER_1               = 1;
    int CLOCK_PRESCALER_8               = 2;
    int CLOCK_PRESCALER_32              = 3;
    int CLOCK_PRESCALER_64              = 4;
    int CLOCK_PRESCALER_128             = 5;
    int CLOCK_PRESCALER_256             = 6;
    int CLOCK_PRESCALER_1024            = 7;

    void clockTimer2();
}
