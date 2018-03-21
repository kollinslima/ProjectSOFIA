package com.example.kollins.androidemulator.uCInterfaces;

/**
 * Created by kollins on 3/21/18.
 */

public interface IOModule {

    public static final int LOW_LEVEL   = 0;
    public static final int HIGH_LEVEL  = 1;
    public static final int TRI_STATE   = 2;

    public static final int PUSH_GND    = 3;
    public static final int PUSH_VDD    = 4;
    public static final int PULL_UP     = 5;
    public static final int PULL_DOWN   = 6;
    public static final int TOGGLE      = 7;

    public static int[] PIN_MODES = {IOModule.PUSH_GND, IOModule.PUSH_VDD, IOModule.PULL_UP, IOModule.PULL_DOWN, IOModule.TOGGLE};
}
