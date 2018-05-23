package com.example.kollins.sofia.ucinterfaces;

/**
 * Created by kollins on 3/8/18.
 */

public interface ProgramMemory {
    int getMemorySize();
    boolean loadProgramMemory(String hexFileLocation);
    int loadInstruction();

    void stopCodeObserver();

    int getPC();
    void setPC(int pc);
    void addToPC(int offset);

    byte readByte(int address);
    void writeWord(int address, int data);
}