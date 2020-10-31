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

package com.kollins.project.sofia;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.kollins.project.sofia.atmega328p.ADC_ATmega328P;
import com.kollins.project.sofia.atmega328p.iomodule_atmega328p.IOModule_ATmega328P;
import com.kollins.project.sofia.atmega328p.iomodule_atmega328p.input.InputFragment_ATmega328P;
import com.kollins.project.sofia.atmega328p.iomodule_atmega328p.output.OutputFragment_ATmega328P;
import com.kollins.project.sofia.extra.AREF_Configuration;
import com.kollins.project.sofia.extra.AboutPage;
import com.kollins.project.sofia.extra.PathUtil;
import com.kollins.project.sofia.extra.memory_map.MemoryFragment;
import com.kollins.project.sofia.serial_monitor.SerialFragment;
import com.kollins.project.sofia.ucinterfaces.DataMemory;
import com.kollins.project.sofia.ucinterfaces.IOModule;
import com.kollins.project.sofia.ucinterfaces.InputFragment;
import com.kollins.project.sofia.ucinterfaces.OutputFragment;

import java.util.concurrent.TimeUnit;

import static android.app.Activity.RESULT_OK;

/**
 * Created by kollins on 3/23/18.
 */

public class UCModule_View extends Fragment {

    public enum LED_STATUS {RUNNING, SHORT_CIRCUIT, HEX_FILE_ERROR}

    private static final String DOCUMENTATION_URL = "https://project-sofia.gitbook.io/project/using-sofia";
    private static final String REPORT_EMAIL = "kollins.lima@gmail.com";
    private static final String REPORT_SUBJECT = "[SOFIA FEEDBACK]";

    private static final int FILE_IMPORT_CODE = 0;
    private static final int AREF_VALUE = 1;

    public static final int REMOVE_OUTPUT_FRAGMENT = 0;
    public static final int REMOVE_INPUT_FRAGMENT = 1;
    public static final int REMOVE_SERIAL_FRAGMENT = 2;

    public static final int OSCILATOR = 16 * ((int) Math.pow(10, 6));
    public static final long CLOCK_PERIOD = (long) ((1 / (double) OSCILATOR) * Math.pow(10, 10));

    public static final short DELAY_SCREEN_UPDATE = 128;
//    public static final short DELAY_SCREEN_UPDATE = 1;

    private short delayScreenUpdateCount;

    private FragmentManager mFragmentManager;
    private FragmentTransaction mFragmentTransaction;

    private FrameLayout outputFrame;
    private FrameLayout inputFrame;

    private OutputFragment outputFragment;
    private InputFragment inputFragment;
    private SerialFragment serialFragment;
    private IOModule ioModule;
    private MemoryFragment memoryFragment;

    private static UCModule.UCHandler uCHandler;
    private UCModule ucModule;

    private Toolbar toolbar;

    private TextView simulatedTimeDisplay, startInstructions, statusInfo, memoryUsage, hexFileErrorInstructions;
    public static long simulatedTime;
    private int memorySize;
    private String simulatedText;
    private long microSeconds;
    private long seconds;

    public static ScreenUpdater screenUpdater;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        screenUpdater = new ScreenUpdater();

        simulatedTime = 0;
        delayScreenUpdateCount = 0;

//        try {
//            Class outputFragmentDevice = Class.forName(UCModule.PACKAGE_NAME + "." + UCModule.device.toLowerCase() + ".iomodule_" +
//                    UCModule.device.toLowerCase() + ".output.OutputFragment_" + UCModule.device);
//            outputFragment = (OutputFragment) outputFragmentDevice.newInstance();
//            outputFragment.setScreenUpdater(screenUpdater);
//
//            Class inputFragmentDevice = Class.forName(UCModule.PACKAGE_NAME + "." + UCModule.device.toLowerCase() + ".iomodule_" +
//                    UCModule.device.toLowerCase() + ".input.InputFragment_" + UCModule.device);
//            inputFragment = (InputFragment) inputFragmentDevice.newInstance();
//            inputFragment.setScreenUpdater(screenUpdater);
//
//            Class ioModuleDevice = Class.forName(UCModule.PACKAGE_NAME + "." + UCModule.device.toLowerCase() + ".iomodule_" +
//                    UCModule.device.toLowerCase() + ".IOModule_" + UCModule.device);
//            ioModule = (IOModule) ioModuleDevice
//                    .getDeclaredConstructor(OutputFragment.class, InputFragment.class)
//                    .newInstance(outputFragment, inputFragment);

            outputFragment = new OutputFragment_ATmega328P();
            outputFragment.setScreenUpdater(screenUpdater);

            inputFragment = new InputFragment_ATmega328P();
            inputFragment.setScreenUpdater(screenUpdater);

            ioModule = new IOModule_ATmega328P(outputFragment, inputFragment);

            memoryFragment = new MemoryFragment();
            serialFragment = new SerialFragment();
            serialFragment.setScreenUpdater(screenUpdater);

//        } catch (ClassNotFoundException | IllegalAccessException | java.lang.InstantiationException
//                | NoSuchMethodException | InvocationTargetException e) {
//            Log.e(UCModule.MY_LOG_TAG, "Error Starting UCModule_View", e);
//        }
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

        startInstructions = (TextView) view.findViewById(R.id.startInstructions);
        hexFileErrorInstructions = (TextView) view.findViewById(R.id.hexFileErrorInstructions);

        outputFrame = (FrameLayout) view.findViewById(R.id.outputPins);
        inputFrame = (FrameLayout) view.findViewById(R.id.inputPins);

        return view;
    }

    @SuppressLint("StringFormatInvalid")
    public void run() {
        simulatedTime += CLOCK_PERIOD;

        if (++delayScreenUpdateCount >= DELAY_SCREEN_UPDATE) {

            delayScreenUpdateCount = 0;

            microSeconds = simulatedTime / (10000);
            seconds = TimeUnit.MICROSECONDS.toSeconds(microSeconds);

            simulatedText = UCModule.resources.getString(R.string.simulated_time_format, seconds, microSeconds);

            screenUpdater.post(new Runnable() {
                @Override
                public void run() {
                    simulatedTimeDisplay.setText(simulatedText);
                }
            });
        }
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

    public void resetIO() {
        if (outputFragment != null) {
            outputFragment.resetOuputs();
        }
        if (serialFragment != null) {
            serialFragment.resetSerial();
        }

        simulatedTime = 0;
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

                    // Ask for permission to use external storage
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
                    }

                    /*
                    Thanks: https://stackoverflow.com/questions/7856959/android-file-chooser
                     */
//                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.setType("*/*");
                    intent.addCategory(Intent.CATEGORY_OPENABLE);

                    try {
                        startActivityForResult(
                                Intent.createChooser(intent, "Select a File to Import"),
                                FILE_IMPORT_CODE);
                    } catch (android.content.ActivityNotFoundException ex) {
                        // Potentially direct the user to the Market with a Dialog
                        Toast.makeText(getContext(), getString(R.string.filemanager_not_found),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;

                case R.id.action_memory_map:

                    if (memoryFragment.getDataMemory() == null) {
                        Toast.makeText(getContext(), getString(R.string.action_memory_map_error),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        mFragmentManager = (getActivity().getSupportFragmentManager());
                        mFragmentTransaction = mFragmentManager.beginTransaction();

                        mFragmentTransaction.addToBackStack(null);
                        mFragmentTransaction.add(R.id.fragment_memory, memoryFragment, MemoryFragment.TAG_MEM_FRAGMENT);
                        mFragmentTransaction.commit();
                    }
                    break;

                case R.id.action_adc_adjust:
                    startActivityForResult(new Intent(getActivity().getBaseContext(), AREF_Configuration.class), AREF_VALUE);
                    break;

                case R.id.action_clear_io:
                    outputFragment.clearAll();
                    outputFrame.setVisibility(View.GONE);
                    inputFragment.clearAll();
                    inputFrame.setVisibility(View.GONE);
                    startInstructions.setVisibility(View.VISIBLE);
                    break;

                case R.id.action_help:
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(DOCUMENTATION_URL));
                    startActivity(browserIntent);
                    break;

                case R.id.action_report_bug:
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO,
                            Uri.fromParts("mailto", "", null));
                    emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { REPORT_EMAIL });
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, REPORT_SUBJECT);
                    emailIntent.putExtra(Intent.EXTRA_TEXT,
                            "================" +
                            "\nSOFIA: " + UCModule.getSofiaVersion() +
                            "\nAPI level: " + Build.VERSION.SDK_INT +
                            "\n================\n\n"
                            );
                    startActivity(Intent.createChooser(emailIntent, ""));
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
                    Log.d("FileImporter", "Path: " + uri.toString());
//                    ucModule.changeFileLocation(PathUtil.getPath(getContext(), uri));
                    ucModule.changeFileLocation(uri);
                    uCHandler.sendEmptyMessage(UCModule.RESET_ACTION);
                }
                break;

            case AREF_VALUE:
                if (resultCode == RESULT_OK){
                    double voltage = data.getDoubleExtra(AREF_Configuration.AREF_EXTRA, 0);
                    ADC_ATmega328P.AREF = (short) (voltage);
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

                        mFragmentManager = getActivity().getSupportFragmentManager();
                        mFragmentTransaction = mFragmentManager.beginTransaction();

                        mFragmentTransaction.add(R.id.outputPins, (Fragment) outputFragment, OutputFragment.TAG_OUTPUT_FRAGMENT);
                        mFragmentTransaction.commit();
                    } else {
                        outputFragment.addOuput();
                    }
                    break;

                case R.id.action_digital_input:
                    inputFrame.setVisibility(View.VISIBLE);
                    if (!inputFragment.haveInput()) {

                        mFragmentManager = getActivity().getSupportFragmentManager();
                        mFragmentTransaction = mFragmentManager.beginTransaction();

                        mFragmentTransaction.add(R.id.inputPins, (Fragment) inputFragment, InputFragment.TAG_INPUT_FRAGMENT);
                        mFragmentTransaction.commit();

                    }
                    inputFragment.addDigitalInput();

                    break;

                case R.id.action_analog_input:
                    inputFrame.setVisibility(View.VISIBLE);
                    if (!inputFragment.haveInput()) {

                        mFragmentManager = getActivity().getSupportFragmentManager();
                        mFragmentTransaction = mFragmentManager.beginTransaction();

                        mFragmentTransaction.add(R.id.inputPins, (Fragment) inputFragment, InputFragment.TAG_INPUT_FRAGMENT);
                        mFragmentTransaction.commit();

                    }
                    inputFragment.addAnalogicInput();
                    break;

                case R.id.action_serial_monitor:
                    outputFrame.setVisibility(View.VISIBLE);
                    mFragmentManager = getActivity().getSupportFragmentManager();

                    Fragment old_fragment = mFragmentManager.findFragmentByTag(SerialFragment.TAG_SERIAL_FRAGMENT);
                    if (old_fragment != null){
                        mFragmentManager.beginTransaction().remove(old_fragment).commit();
                    }

                    mFragmentTransaction = mFragmentManager.beginTransaction();
                    mFragmentTransaction.add(R.id.outputPins, serialFragment, SerialFragment.TAG_SERIAL_FRAGMENT);
                    mFragmentTransaction.addToBackStack(null);
                    mFragmentTransaction.commit();

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

//                case REMOVE_SERIAL_FRAGMENT:
//                    if (inputFrame.getVisibility() != View.VISIBLE) {
//                        startInstructions.setVisibility(View.VISIBLE);
//                    }
//                    break;
            }
        }
    }
}
