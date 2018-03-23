package com.example.kollins.androidemulator.uCInterfaces;

import java.util.logging.Handler;

/**
 * Created by kollins on 3/21/18.
 */

public interface IOModule{

    int LOW_LEVEL   = 0;
    int HIGH_LEVEL  = 1;
    int TRI_STATE   = 2;

    int PUSH_GND    = 3;
    int PUSH_VDD    = 4;
    int PULL_UP     = 5;
    int PULL_DOWN   = 6;
    int TOGGLE      = 7;

    int[] PIN_MODES = {IOModule.PUSH_GND, IOModule.PUSH_VDD, IOModule.PULL_UP, IOModule.PULL_DOWN, IOModule.TOGGLE};

    int PORTB_EVENT = 100;

    String PORT_IOMESSAGE = "PORT_VALUE";
    String CONFIG_IOMESSAGE = "CONFIG_VALUE";

}
