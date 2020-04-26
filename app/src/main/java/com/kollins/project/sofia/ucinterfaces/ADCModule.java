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

public interface ADCModule {

    int CLOCK_PRESCALER_2_1             = 0;
    int CLOCK_PRESCALER_2_2             = 1;
    int CLOCK_PRESCALER_4               = 2;
    int CLOCK_PRESCALER_8               = 3;
    int CLOCK_PRESCALER_16              = 4;
    int CLOCK_PRESCALER_32              = 5;
    int CLOCK_PRESCALER_64              = 6;
    int CLOCK_PRESCALER_128             = 7;

    void run();
}
