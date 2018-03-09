package com.example.kollins.androidemulator.uCInterfaces;

/**
 * Created by kollins on 3/9/18.
 */

public interface DataMemory {

    int getMemorySize();

    void writeByte(char byteAddress, byte byteData);
    byte readByte(char byteAddress);

    void writeBit(char byteAddress, byte bitPosition, boolean bitState);
    boolean readBit(char byteAddress, byte bitPosition);
}
