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

package com.kollins.project.sofia.ucinterfaces;

public interface Timer1Module {

    int NO_CLOCK_SOURCE                 = 0;
    int CLOCK_PRESCALER_1               = 1;
    int CLOCK_PRESCALER_8               = 2;
    int CLOCK_PRESCALER_64              = 3;
    int CLOCK_PRESCALER_256             = 4;
    int CLOCK_PRESCALER_1024            = 5;
    int EXTERNAL_CLOCK_T1_FALLING_EDGE  = 6;
    int EXTERNAL_CLOCK_T1_RISING_EDGE   = 7;

    void run();
}
