package com.example.kollins.androidemulator.uCInterfaces;

/**
 * Created by kollins on 3/20/18.
 */

public interface OutputFragment extends IOModule{

    String TAG_OUTPUT_FRAGMENT = "outputFragmentTAG";

    void addOuput();
    void resetOuputs();
    void clearAll();

    void setDataMemory(DataMemory dataMemory);

    boolean haveOutput();
}
