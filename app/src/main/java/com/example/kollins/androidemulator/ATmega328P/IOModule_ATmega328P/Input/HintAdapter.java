package com.example.kollins.androidemulator.ATmega328P.IOModule_ATmega328P.Input;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.ArrayAdapter;

/**
 * Created by kollins on 3/21/18.
 */

public class HintAdapter extends ArrayAdapter<Object> {


    public HintAdapter(@NonNull Context context, int resource, @NonNull Object[] objects) {
        super(context, resource, objects);
    }

    @Override
    public int getCount() {
        // don't display last item. It is used as hint.
        int count = super.getCount();
        return count > 0 ? count - 1 : count;
    }
}
