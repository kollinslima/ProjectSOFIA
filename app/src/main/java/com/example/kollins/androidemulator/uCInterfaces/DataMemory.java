package com.example.kollins.androidemulator.uCInterfaces;

/**
 * Created by kollins on 3/9/18.
 */

public interface DataMemory {

    int getMemorySize();

    void writeByte(int byteAddress, int byteData);
    byte readByte(int byteAddress);

    void writeBit(int byteAddress, byte bitPosition, boolean bitState);
    boolean readBit(int byteAddress, byte bitPosition);

}
