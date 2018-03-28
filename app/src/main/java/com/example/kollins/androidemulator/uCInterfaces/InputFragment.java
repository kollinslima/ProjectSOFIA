package com.example.kollins.androidemulator.uCInterfaces;

import android.os.Handler;

/**
 * Created by kollins on 3/21/18.
 */

public interface InputFragment extends IOModule{

    String TAG_INPUT_FRAGMENT = "inputFragmentTAG";

    void addDigitalInput();
    void addAnalogicInput();
    boolean haveInput();
    void clearAll();

    void setDataMemory(DataMemory dataMemory);
    void setScreenUpdater(Handler screenUpdater);
}
