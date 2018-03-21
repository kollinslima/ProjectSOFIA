package com.example.kollins.androidemulator.uCInterfaces;

/**
 * Created by kollins on 3/21/18.
 */

public interface DigitalInputFragment extends IOModule{

    String TAG_DIGITAL_INPUT_FRAGMENT = "digitalInputFragmentTAG";

    void addDigitalInput();
    boolean haveDigitalInput();
    void clearAll();

    void setDataMemory(DataMemory dataMemory);
}
