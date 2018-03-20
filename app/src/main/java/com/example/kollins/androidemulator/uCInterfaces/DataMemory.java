package com.example.kollins.androidemulator.uCInterfaces;

import android.os.Handler;

import java.io.Serializable;

/**
 * Created by kollins on 3/9/18.
 */

public interface DataMemory {

    int getMemorySize();

    void writeByte(int byteAddress, byte byteData);
    byte readByte(int byteAddress);

    void writeBit(int byteAddress, int bitPosition, boolean bitState);
    boolean readBit(int byteAddress, int bitPosition);
}
