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

package com.example.kollins.sofia.extra;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.kollins.sofia.CPUModule;
import com.example.kollins.sofia.database.DataBaseHelper;
import com.example.kollins.sofia.R;
import com.example.kollins.sofia.UCModule;

import java.io.IOException;

public class SplashScreen extends AppCompatActivity {

    private static final int ANIMATION_DURATION = 3000;
    private ImageView logo;
    private AlphaAnimation fadeInAnimation;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        logo = (ImageView) findViewById(R.id.logo);

        fadeInAnimation = new AlphaAnimation(0, 1);
        fadeInAnimation.setDuration(ANIMATION_DURATION);
        fadeInAnimation.setFillAfter(true);
        fadeInAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) { }

            @Override
            public void onAnimationStart(Animation animation) {
                logo.setVisibility(View.VISIBLE);
            }
        });

        logo.startAnimation(fadeInAnimation);

        new LoadDBTask().execute();
    }

    private Context getContext(){
        return this;
    }

    private class LoadDBTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            DataBaseHelper mDBHelper = new DataBaseHelper(getContext());
            SQLiteDatabase mDb = null;

            try {
                mDBHelper.updateDataBase();
                mDb = mDBHelper.getWritableDatabase();

                Cursor cursor = mDb.query("instructionTable", null, null, null, null, null, null);

                if (cursor.moveToFirst()) {
                    do {

                        CPUModule.INSTRUCTION_ID[cursor.getInt(cursor.getColumnIndex("opcode"))] =
                                cursor.getShort(cursor.getColumnIndex("instruction_id"));

//                    Log.v(UCModule.MY_LOG_TAG, "DBLoad[" +
//                            cursor.getInt(cursor.getColumnIndex("opcode")) +
//                            "] = " +
//                            cursor.getInt(cursor.getColumnIndex("instruction_id")));
                    } while (cursor.moveToNext());
                }

            } catch (IOException e) {
                Log.e(UCModule.MY_LOG_TAG,"UnableToLoadDataBase",e);
            } finally {
                if (mDb != null) {
                    mDb.close();
                }
            }

            //Wait for animation to stop
            while (!fadeInAnimation.hasEnded());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            startActivity(new Intent(getBaseContext(), UCModule.class));
            finish();
        }
    }

}
