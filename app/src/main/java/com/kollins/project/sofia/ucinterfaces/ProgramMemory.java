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

import android.content.ContentResolver;
import android.net.Uri;

/**
 * Created by kollins on 3/8/18.
 */

public interface ProgramMemory {
    int getMemorySize();
    boolean loadProgramMemory(Uri hexFileLocation, ContentResolver contentResolver);
    int loadInstruction();

    void stopCodeObserver(ContentResolver contentResolver);

    int getPC();
    void setPC(int pc);
    void addToPC(int offset);

    byte readByte(int address);
    void writeWord(int address, int data);
}
