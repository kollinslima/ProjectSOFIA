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
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.kollins.project.sofia.R;
import com.kollins.project.sofia.UCModule;
import com.kollins.project.sofia.atmega328p.ADC_ATmega328P;

public class AREF_Configuration extends AppCompatActivity {

    public static final String AREF_EXTRA = "AREF_EXTRA";

    private TextView curentAREF;
    private EditText newAREF;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aref_adjust);

        curentAREF = (TextView) findViewById(R.id.adc_current_aref);
        curentAREF.setText(String.valueOf(ADC_ATmega328P.AREF/1000f) + "V");

        newAREF = (EditText) findViewById(R.id.adc_new_aref);
    }

    public void cancelButton(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }

    public void okButton(View view) {
        String value = newAREF.getText().toString();
        double valueDouble = 0;

        try {
            valueDouble = Double.parseDouble(value);

            if (valueDouble > 5 || valueDouble < 1){
                toastError();
            } else {
                Intent devolve = new Intent();
                devolve.putExtra(AREF_EXTRA, valueDouble*1000);
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
