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

package com.example.kollins.sofia.serial_monitor;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.kollins.sofia.R;
import com.example.kollins.sofia.UCModule_View;

public class SerialFragment extends Fragment {

    public static String TAG_SERIAL_FRAGMENT = "serialFragmentTAG";
    private static StringBuilder serialStringBuilder = new StringBuilder();
    public static String buffer = new String();

    private static TextView serialMonitor;
    private Button closeButton, sendButton;
    private EditText messageSerial;

    private Handler screenUpdater;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.frament_serial_monitor, container, false);

        messageSerial = (EditText) layout.findViewById(R.id.serialEditText);

        closeButton = layout.findViewById(R.id.serialCloseButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
                screenUpdater.sendEmptyMessage(UCModule_View.REMOVE_SERIAL_FRAGMENT);
            }
        });

        sendButton = layout.findViewById(R.id.serialSendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buffer = messageSerial.getEditableText().toString() + "\n";
                messageSerial.setText("");
            }
        });


        serialMonitor = layout.findViewById(R.id.serialMessages);
        serialMonitor.setMovementMethod(new ScrollingMovementMethod());
        serialMonitor.append(serialStringBuilder.toString());
        serialStringBuilder.setLength(0);

        return layout;
    }

    public static void appendByte(byte newByte){
        new UpdateSerialMonitor().execute(newByte);
    }

    public void resetSerial() {
        if (serialMonitor != null) {
            serialMonitor.setText("");
        }
        serialStringBuilder.setLength(0);
    }

    public void setScreenUpdater(Handler screenUpdater) {
        this.screenUpdater = screenUpdater;
    }

    private static class UpdateSerialMonitor extends AsyncTask<Byte,Void,String>{

        @Override
        protected String doInBackground(Byte... bytes) {
            serialStringBuilder.append((char) (bytes[0] & 0xFF));
            return serialStringBuilder.toString();
        }

        @Override
        protected void onPostExecute(String message) {
            super.onPostExecute(message);
            if (serialMonitor != null) {
                serialMonitor.append(message);
                serialStringBuilder.setLength(0);
            }
        }
    }

}
