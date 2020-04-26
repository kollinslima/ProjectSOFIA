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

    void dataRegisterEmptyUSART();

    void transmissionCompleteUSART();

    void receiveCompleteUSART();
    void receiveBufferReadedUSART();
}
