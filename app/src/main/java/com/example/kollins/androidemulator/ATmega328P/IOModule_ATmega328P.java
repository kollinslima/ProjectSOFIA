package com.example.kollins.androidemulator.ATmega328P;

import com.example.kollins.androidemulator.R;
import com.example.kollins.androidemulator.uCInterfaces.IOModule;
import com.example.kollins.androidemulator.uCModule;

public class IOModule_ATmega328P implements IOModule {

    private com.example.kollins.androidemulator.uCModule uCModule;

    public IOModule_ATmega328P(uCModule uCModule) {
        this.uCModule = uCModule;
    }

    public void loadBaseLayout(){
        uCModule.setContentView(R.layout.io_interface);
    }

}
