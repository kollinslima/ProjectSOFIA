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

import com.example.kollins.sofia.CPUModule;
import com.example.kollins.sofia.database.DataBaseHelper;
import com.example.kollins.sofia.R;
import com.example.kollins.sofia.UCModule;

import java.io.IOException;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

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
