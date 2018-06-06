/*
 * Copyright 2018
 * Kollins Lima (kollins.lima@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.kollins.sofia;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kollins.sofia.extra.AboutPage;
import com.example.kollins.sofia.extra.memory_map.MemoryFragment;
import com.example.kollins.sofia.extra.PathUtil;
import com.example.kollins.sofia.ucinterfaces.DataMemory;
import com.example.kollins.sofia.ucinterfaces.InputFragment;
import com.example.kollins.sofia.ucinterfaces.IOModule;
import com.example.kollins.sofia.ucinterfaces.OutputFragment;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static android.app.Activity.RESULT_OK;

/**
 * Created by kollins on 3/23/18.
 */

public class UCModule_View extends Fragment implements Runnable {

    public enum LED_STATUS {RUNNING, SHORT_CIRCUIT, HEX_FILE_ERROR}

    private static final int FILE_IMPORT_CODE = 0;

    public static final int REMOVE_OUTPUT_FRAGMENT = 0;
    public static final int REMOVE_INPUT_FRAGMENT = 1;

    public static final int OSCILATOR = 16 * ((int) Math.pow(10, 6));
    public static final long CLOCK_PERIOD = (long) ((1 / (double) OSCILATOR) * Math.pow(10, 10));

    private Lock clockLock;
    private Lock ucViewLock;
    private Condition ucViewClockCondition;

    private FragmentManager mFragmentManager;
    private FragmentTransaction mFragmentTransaction;

    private FrameLayout outputFrame;
    private FrameLayout inputFrame;

    private OutputFragment outputFragment;
    private InputFragment inputFragment;
    private IOModule ioModule;
    private MemoryFragment memoryFragment;

    private static UCModule.UCHandler uCHandler;
    private UCModule ucModule;

    private Toolbar toolbar;

    private TextView simulatedTimeDisplay, startInstructions, statusInfo, memoryUsage, hexFileErrorInstructions;
    public static long simulatedTime;
    private int memorySize;
    private String simulatedText, memoryUsageText;
    private long nanoSeconds;
    private long seconds;

    private Resources resources;

    public static ScreenUpdater screenUpdater;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        screenUpdater = new ScreenUpdater();
        resources = getResources();

        ucViewLock = new ReentrantLock();
        ucViewClockCondition = ucViewLock.newCondition();

        try {
            Class outputFragmentDevice = Class.forName(UCModule.PACKAGE_NAME + "." + UCModule.device.toLowerCase() + ".iomodule_" +
                    UCModule.device.toLowerCase() + ".output.OutputFragment_" + UCModule.device);
            outputFragment = (OutputFragment) outputFragmentDevice.newInstance();
            outputFragment.setScreenUpdater(screenUpdater);

            Class inputFragmentDevice = Class.forName(UCModule.PACKAGE_NAME + "." + UCModule.device.toLowerCase() + ".iomodule_" +
                    UCModule.device.toLowerCase() + ".input.InputFragment_" + UCModule.device);
            inputFragment = (InputFragment) inputFragmentDevice.newInstance();
            inputFragment.setScreenUpdater(screenUpdater);

            Class ioModuleDevice = Class.forName(UCModule.PACKAGE_NAME + "." + UCModule.device.toLowerCase() + ".iomodule_" +
                    UCModule.device.toLowerCase() + ".IOModule_" + UCModule.device);
            ioModule = (IOModule) ioModuleDevice
                    .getDeclaredConstructor(OutputFragment.class, InputFragment.class)
                    .newInstance(outputFragment, inputFragment);

            memoryFragment = new MemoryFragment();

        } catch (ClassNotFoundException | IllegalAccessException | java.lang.InstantiationException
                | NoSuchMethodException | InvocationTargetException e) {
            Log.e(UCModule.MY_LOG_TAG, "Error Starting UCModule_View", e);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_io_interface, container, false);

        //Set Toolbar
        toolbar = ((Toolbar) view.findViewById(R.id.mainToolbar));
        toolbar.inflateMenu(R.menu.menu_layout);
        toolbar.setOnMenuItemClickListener(new ToolBarMenuItemClick());
        toolbar.setTitle("Arduino " + UCModule.model);

        statusInfo = (TextView) view.findViewById(R.id.statusInfo);
        simulatedTimeDisplay = (TextView) view.findViewById(R.id.simulatedTime);
        memoryUsage = (TextView) view.findViewById(R.id.memoryUsage);

        startInstructions = (TextView) view.findViewById(R.id.startInstructions);
        hexFileErrorInstructions = (TextView) view.findViewById(R.id.hexFileErrorInstructions);

        outputFrame = (FrameLayout) view.findViewById(R.id.outputPins);
        inputFrame = (FrameLayout) view.findViewById(R.id.inputPins);

        return view;
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void run() {
        Thread.currentThread().setName("UCModule_View");
        simulatedTime = 0;
        while (!ucModule.getResetFlag()) {
            waitClock();
            simulatedTime += CLOCK_PERIOD;

            nanoSeconds = simulatedTime / 10;
            seconds = TimeUnit.NANOSECONDS.toSeconds(nanoSeconds);

            simulatedText = resources.getString(R.string.simulated_time_format, seconds, nanoSeconds);
            memoryUsageText = resources.getString(R.string.memory_usage_format, ucModule.getMemoryUsage(), memorySize);

            screenUpdater.post(new Runnable() {
                @Override
                public void run() {
                    simulatedTimeDisplay.setText(simulatedText);
                    memoryUsage.setText(memoryUsageText);
                }
            });
        }

        Log.i(UCModule.MY_LOG_TAG, "Finishing UCView");
    }

    private void waitClock() {

        UCModule.clockVector.set(UCModule.SIMULATED_TIMER_ID, Boolean.TRUE);

        if (UCModule.clockVector.contains(Boolean.FALSE)) {
            while (UCModule.clockVector.get(UCModule.SIMULATED_TIMER_ID)) {
                Thread.yield();
//                ucViewLock.lock();
//                try {
//                    ucViewClockCondition.await();
//                } catch (InterruptedException e) {
//                    Log.e(UCModule.MY_LOG_TAG, "ERROR: waitClock UCModule_View", e);
//                } finally {
//                    ucViewLock.unlock();
//                }
            }
            return;
        }

        UCModule.resetClockVector();

        //Send Broadcast
        uCHandler.sendEmptyMessage(UCModule.CLOCK_ACTION);

//        clockLock.lock();
//        try {
//            UCModule.clockVector[UCModule.SIMULATED_TIMER_ID] = true;
//
//            for (int i = 0; i < UCModule.clockVector.length; i++) {
//                if (!UCModule.clockVector[i]) {
//
//                    while (UCModule.clockVector[UCModule.SIMULATED_TIMER_ID]) {
//                        ucViewClockCondition.await();
//                    }
//                    return;
//                }
//            }
//
//            UCModule.resetClockVector();
//
//            //Send Broadcast
//            uCHandler.sendEmptyMessage(UCModule.CLOCK_ACTION);
//
//        } catch (InterruptedException e) {
//            Log.e(UCModule.MY_LOG_TAG, "ERROR: waitClock UCModule_View", e);
//        } finally {
//            clockLock.unlock();
//        }
    }

    public void clockUCView() {
//        ucViewLock.lock();
//        try {
//            ucViewClockCondition.signal();
//        } finally {
//            ucViewLock.unlock();
//        }
    }

    public void setMemoryIO(DataMemory dataMemory) {
        outputFragment.setDataMemory(dataMemory);
        inputFragment.setDataMemory(dataMemory);
        memoryFragment.setDataMemory(dataMemory);
        memorySize = dataMemory.getMemorySize();
    }

    public IOModule getIOModule() {
        return ioModule;
    }

    public void setClockLock(Lock clockLock) {
        this.clockLock = clockLock;
//        ucViewClockCondition = clockLock.newCondition();
    }

    public void resetIO() {
        if (outputFragment != null) {
            outputFragment.resetOuputs();
        }
    }

    public void setUCHandler(UCModule.UCHandler uCHandler) {
        this.uCHandler = uCHandler;
    }

    public void setStatus(LED_STATUS status) {
        switch (status) {
            case RUNNING:
                statusInfo.setText(UCModule.getStatusRunning());
                statusInfo.setTextColor(UCModule.getStatusRunningColor());

                hexFileErrorInstructions.setVisibility(View.GONE);
                break;

            case SHORT_CIRCUIT:
                statusInfo.setText(UCModule.getStatusShortCircuit());
                statusInfo.setTextColor(UCModule.getStatusShortCircuitColor());
                break;

            case HEX_FILE_ERROR:
                statusInfo.setText(UCModule.getStatusHexFileError());
                statusInfo.setTextColor(UCModule.getStatusHexFileErrorColor());

                hexFileErrorInstructions.setVisibility(View.VISIBLE);
                break;
        }
    }

    public static void sendShortCircuit() {
        uCHandler.sendEmptyMessage(UCModule.SHORT_CIRCUIT_ACTION);
    }

    public void setUCDevice(UCModule ucModule) {
        this.ucModule = ucModule;
    }

    private class ToolBarMenuItemClick implements Toolbar.OnMenuItemClickListener {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_reset:
                    uCHandler.sendEmptyMessage(UCModule.RESET_ACTION);
                    break;

                case R.id.action_add:
                    if (!UCModule.setUpSuccessful) {
                        break;
                    }

                    PopupMenu popup = new PopupMenu(getActivity(), getView().findViewById(R.id.action_add));
                    popup.setOnMenuItemClickListener(new PopUpMenuItemClick());
                    MenuInflater inflater = popup.getMenuInflater();
                    inflater.inflate(R.menu.pop_up_menu, popup.getMenu());
                    popup.show();
                    break;

                case R.id.action_import:
                    /*
                    Thanks: https://stackoverflow.com/questions/7856959/android-file-chooser
                     */
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");
                    intent.addCategory(Intent.CATEGORY_OPENABLE);

                    try {
                        startActivityForResult(
                                Intent.createChooser(intent, "Select a File to Import"),
                                FILE_IMPORT_CODE);
                    } catch (android.content.ActivityNotFoundException ex) {
                        // Potentially direct the user to the Market with a Dialog
                        Toast.makeText(getContext(), "Please install a File Manager.",
                                Toast.LENGTH_SHORT).show();
                    }
                    break;

                case R.id.action_memory_map:
                    mFragmentManager = (getActivity().getSupportFragmentManager());
                    mFragmentTransaction = mFragmentManager.beginTransaction();

                    mFragmentTransaction.addToBackStack(null);
                    mFragmentTransaction.add(R.id.fragment_memory, memoryFragment, MemoryFragment.TAG_MEM_FRAGMENT);
                    mFragmentTransaction.commit();
                    break;

                case R.id.action_clear_io:
                    outputFragment.clearAll();
                    outputFrame.setVisibility(View.GONE);
                    inputFragment.clearAll();
                    inputFrame.setVisibility(View.GONE);
                    startInstructions.setVisibility(View.VISIBLE);
                    break;

                case R.id.action_about:
                    startActivity(new Intent(getActivity().getBaseContext(), AboutPage.class));
                    break;

                default:
                    //This shouldn't happen...
                    break;
            }
            return true;
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_IMPORT_CODE:
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    Log.d("FileImporter", "Path: " + PathUtil.getPath(getContext(), uri));
                    ucModule.changeFileLocation(PathUtil.getPath(getContext(), uri));
                    uCHandler.sendEmptyMessage(UCModule.RESET_ACTION);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private class PopUpMenuItemClick implements PopupMenu.OnMenuItemClickListener {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            startInstructions.setVisibility(View.GONE);
            switch (item.getItemId()) {
                case R.id.action_output:
                    outputFrame.setVisibility(View.VISIBLE);
                    if (!outputFragment.haveOutput()) {

                        mFragmentManager = ((AppCompatActivity) getActivity()).getSupportFragmentManager();
                        mFragmentTransaction = mFragmentManager.beginTransaction();

                        mFragmentTransaction.add(R.id.outputPins, (android.support.v4.app.Fragment) outputFragment, OutputFragment.TAG_OUTPUT_FRAGMENT);
                        mFragmentTransaction.commit();
                    } else {
                        outputFragment.addOuput();
                    }
                    break;

                case R.id.action_digital_input:
                    inputFrame.setVisibility(View.VISIBLE);
                    if (!inputFragment.haveInput()) {

                        mFragmentManager = ((AppCompatActivity) getActivity()).getSupportFragmentManager();
                        mFragmentTransaction = mFragmentManager.beginTransaction();

                        mFragmentTransaction.add(R.id.inputPins, (android.support.v4.app.Fragment) inputFragment, InputFragment.TAG_INPUT_FRAGMENT);
                        mFragmentTransaction.commit();

                    }
                    inputFragment.addDigitalInput();

                    break;

                case R.id.action_analog_input:
                    inputFrame.setVisibility(View.VISIBLE);
                    if (!inputFragment.haveInput()) {

                        mFragmentManager = ((AppCompatActivity) getActivity()).getSupportFragmentManager();
                        mFragmentTransaction = mFragmentManager.beginTransaction();

                        mFragmentTransaction.add(R.id.inputPins, (android.support.v4.app.Fragment) inputFragment, InputFragment.TAG_INPUT_FRAGMENT);
                        mFragmentTransaction.commit();

                    }
                    inputFragment.addAnalogicInput();
                    break;
            }
            return true;
        }
    }

    public class ScreenUpdater extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REMOVE_OUTPUT_FRAGMENT:
                    outputFrame.setVisibility(View.GONE);
                    if (inputFrame.getVisibility() != View.VISIBLE) {
                        startInstructions.setVisibility(View.VISIBLE);
                    }
                    break;

                case REMOVE_INPUT_FRAGMENT:
                    inputFrame.setVisibility(View.GONE);
                    if (outputFrame.getVisibility() != View.VISIBLE) {
                        startInstructions.setVisibility(View.VISIBLE);
                    }
                    break;

            }
        }
    }
}
