package com.example.kollins.androidemulator.uCInterfaces;

import com.example.kollins.androidemulator.UCModule;

import android.os.Handler;

/**
 * Created by kollins on 3/21/18.
 */

public interface IOModule{

    int LOW_LEVEL   = 0;
    int HIGH_LEVEL  = 5;
    int TRI_STATE   = 2;

    int PUSH_GND    = 6;
    int PUSH_VDD    = 7;
    int PULL_UP     = 8;
    int PULL_DOWN   = 9;
    int TOGGLE      = 10;

    int[] PIN_MODES = {IOModule.PUSH_GND, IOModule.PUSH_VDD, IOModule.PULL_UP, IOModule.PULL_DOWN, IOModule.TOGGLE};

    int PORTB_EVENT = 100;
    int PORTC_EVENT = 101;
    int PORTD_EVENT = 102;

    String PORT_IOMESSAGE = "PORT_VALUE";
    String CONFIG_IOMESSAGE = "CONFIG_VALUE";

    boolean checkShortCircuit();

    void getPINConfig();
}
