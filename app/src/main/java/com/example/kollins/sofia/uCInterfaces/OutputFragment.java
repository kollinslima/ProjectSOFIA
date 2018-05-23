package com.example.kollins.sofia.uCInterfaces;

import android.os.Handler;

/**
 * Created by kollins on 3/20/18.
 */

public interface OutputFragment extends IOModule{

    String TAG_OUTPUT_FRAGMENT = "outputFragmentTAG";

    void addOuput();
    void resetOuputs();
    void clearAll();

    void setDataMemory(DataMemory dataMemory);
    void setScreenUpdater(Handler screenUpdater);

    boolean haveOutput();
}
