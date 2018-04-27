package com.example.kollins.androidemulator;

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

import com.example.kollins.androidemulator.Extra.PathUtil;
import com.example.kollins.androidemulator.uCInterfaces.DataMemory;
import com.example.kollins.androidemulator.uCInterfaces.InputFragment;
import com.example.kollins.androidemulator.uCInterfaces.IOModule;
import com.example.kollins.androidemulator.uCInterfaces.OutputFragment;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import static android.app.Activity.RESULT_OK;

/**
 * Created by kollins on 3/23/18.
 */

public class UCModule_View extends Fragment implements Runnable {

    public enum LED_STATUS {RUNNING, SHORT_CIRCUIT, HEX_FILE_ERROR}

    private static final int FILE_IMPORT_CODE = 0;

    public static final int REMOVE_OUTPUT_FRAGMENT = 0;
    public static final int REMOVE_INPUT_FRAGMENT = 1;

    private int oscilator = 16 * ((int) Math.pow(10, 6));
    private long clockPeriod = (long) ((1 / (double) oscilator) * Math.pow(10, 10));

    private Lock clockLock;
    private Condition ucViewClockCondition;

    private FragmentManager mFragmentManager;
    private FragmentTransaction mFragmentTransaction;

    private FrameLayout outputFrame;
    private FrameLayout inputFrame;

    private OutputFragment outputFragment;
    private InputFragment inputFragment;
    private IOModule ioModule;

    private static UCModule.uCHandler uCHandler;
    private UCModule ucModule;

    private Toolbar toolbar;

    private TextView simulatedTimeDisplay, startInstructions, statusInfo, memoryUsage, hexFileErrorInstructions;
    private long simulatedTime;
    private String simulatedText, memoryUsageText;
    private long nanoSeconds;
//    private long microSeconds;
    private long seconds;

    private Resources resources;

    private ScreenUpdater screenUpdater;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        screenUpdater = new ScreenUpdater();
        resources = getResources();

        try {
            Class outputFragmentDevice = Class.forName(UCModule.PACKAGE_NAME + "." + UCModule.device + ".IOModule_" +
                    UCModule.device + ".Output.OutputFragment_" + UCModule.device);
            outputFragment = (OutputFragment) outputFragmentDevice.newInstance();
            outputFragment.setScreenUpdater(screenUpdater);

            Class inputFragmentDevice = Class.forName(UCModule.PACKAGE_NAME + "." + UCModule.device + ".IOModule_" +
                    UCModule.device + ".Input.InputFragment_" + UCModule.device);
            inputFragment = (InputFragment) inputFragmentDevice.newInstance();
            inputFragment.setScreenUpdater(screenUpdater);

            Class ioModuleDevice = Class.forName(UCModule.PACKAGE_NAME + "." + UCModule.device + ".IOModule_" +
                    UCModule.device + ".IOModule_" + UCModule.device);
            ioModule = (IOModule) ioModuleDevice
                    .getDeclaredConstructor(OutputFragment.class, InputFragment.class)
                    .newInstance(outputFragment, inputFragment);

        } catch (ClassNotFoundException | IllegalAccessException | java.lang.InstantiationException
                | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
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
//        ((AppCompatActivity)getActivity()).setSupportActionBar((Toolbar) view.findViewById(R.id.mainToolbar));

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
            simulatedTime += clockPeriod;

            nanoSeconds = simulatedTime / 10;
//            microSeconds = (simulatedTime/10)/1000;
            seconds = TimeUnit.NANOSECONDS.toSeconds(nanoSeconds);
//            seconds = TimeUnit.MICROSECONDS.toSeconds(microSeconds);

            simulatedText = resources.getString(R.string.simulated_time_format, seconds, nanoSeconds);
            memoryUsageText = resources.getString(R.string.memory_usage_format, ucModule.getMemoryUsage());

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
        clockLock.lock();
        try {
            UCModule.clockVector[UCModule.SIMULATED_TIMER_ID] = true;

            for (int i = 0; i < UCModule.clockVector.length; i++) {
                if (!UCModule.clockVector[i]) {
                    ucViewClockCondition.await();
                    return;
                }
            }

            UCModule.resetClockVector();

            //Send Broadcast
            uCHandler.sendEmptyMessage(UCModule.CLOCK_ACTION);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            clockLock.unlock();
        }
    }

    public void clockUCView() {
        clockLock.lock();
        try {
            ucViewClockCondition.signal();
        } finally {
            clockLock.unlock();
        }
    }

    public void setMemoryIO(DataMemory dataMemory) {
        outputFragment.setDataMemory(dataMemory);
        inputFragment.setDataMemory(dataMemory);
    }

    public IOModule getIOModule() {
        return ioModule;
    }

    public void setClockLock(Lock clockLock) {
        this.clockLock = clockLock;
        ucViewClockCondition = clockLock.newCondition();
    }

    public void resetIO() {
        outputFragment.resetOuputs();
    }

    public void setUCHandler(UCModule.uCHandler uCHandler) {
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
//                    outputFragment.resetOuputs();
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

                case R.id.action_clear_io:
                    outputFragment.clearAll();
                    outputFrame.setVisibility(View.GONE);
                    inputFragment.clearAll();
                    inputFrame.setVisibility(View.GONE);
                    startInstructions.setVisibility(View.VISIBLE);
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
                    Log.d("FileImporter", "Path: " + PathUtil.getPath(getContext(),uri));
                    ucModule.changeFileLocation(PathUtil.getPath(getContext(),uri));
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
