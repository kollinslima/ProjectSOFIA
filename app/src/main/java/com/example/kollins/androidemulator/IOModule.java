package com.example.kollins.androidemulator;

public class IOModule {

    private uCModule uCModule;

    public IOModule(uCModule uCModule) {
        this.uCModule = uCModule;
        uCModule.setContentView(R.layout.io_interface);
    }

}
