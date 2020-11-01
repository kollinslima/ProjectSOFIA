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

package com.kollins.project.sofia.extra;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.kollins.project.sofia.R;
import com.kollins.project.sofia.UCModule;

import static com.kollins.project.sofia.UCModule.AREF_SETTINGS;
import static com.kollins.project.sofia.UCModule.SETTINGS;
import static com.kollins.project.sofia.UCModule.START_PAUSED_SETTINGS;

public class Settings extends AppCompatActivity {

    private EditText curentAREF;
    private CheckBox startPaused;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        //Load device settings
        SharedPreferences settings = getSharedPreferences(SETTINGS, MODE_PRIVATE);
        curentAREF = (EditText) findViewById(R.id.adc_current_aref);
        curentAREF.setText(String.valueOf((short) settings.getInt(AREF_SETTINGS, 5000) / 1000f));

        startPaused = (CheckBox) findViewById(R.id.checkbox_start_paused);
        startPaused.setChecked(settings.getBoolean(START_PAUSED_SETTINGS, false));
    }

    public void cancelButton(View view) {
        finish();
    }

    public void okButton(View view) {
        String value = curentAREF.getText().toString();
        double valueDouble = 0;

        try {
            valueDouble = Double.parseDouble(value);

            if (valueDouble > 5 || valueDouble < 1){
                toastError();
            } else {
                Log.d("Settings", "SAVE SETTINGS...");
                //Save settings
                SharedPreferences.Editor editor = getSharedPreferences(SETTINGS, MODE_PRIVATE).edit();
                editor.putInt(AREF_SETTINGS, (int) valueDouble*1000);
                editor.putBoolean(START_PAUSED_SETTINGS, startPaused.isChecked());
                editor.apply();

                Intent devolve = new Intent();
                setResult(RESULT_OK, devolve);

                finish();
            }

        } catch (Exception e){
            toastError();
        }
    }

    private void toastError(){
        Toast.makeText(this, UCModule.getAREFError(), Toast.LENGTH_SHORT).show();
    }
}
