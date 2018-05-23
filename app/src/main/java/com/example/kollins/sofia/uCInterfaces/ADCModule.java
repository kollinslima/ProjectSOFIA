package com.example.kollins.sofia.uCInterfaces;

public interface ADCModule extends Runnable {

    int CLOCK_PRESCALER_2_1             = 0;
    int CLOCK_PRESCALER_2_2             = 1;
    int CLOCK_PRESCALER_4               = 2;
    int CLOCK_PRESCALER_8               = 3;
    int CLOCK_PRESCALER_16              = 4;
    int CLOCK_PRESCALER_32              = 5;
    int CLOCK_PRESCALER_64              = 6;
    int CLOCK_PRESCALER_128             = 7;

    void clockADC();
}
