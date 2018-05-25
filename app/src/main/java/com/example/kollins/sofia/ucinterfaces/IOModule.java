/*
 * Copyright 2018
 * Kollins Lima (kollins.lima@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.kollins.sofia.ucinterfaces;

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
    int PORTC_EVENT = 101;
    int PORTD_EVENT = 102;

    String VALUE_IOMESSAGE = "VALUE";
    String CONFIG_IOMESSAGE = "CONFIG_VALUE";

    boolean checkShortCircuit();

    void getPINConfig();
}
