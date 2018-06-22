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

package com.example.kollins.sofia.extra.memory_map;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.kollins.sofia.R;
import com.example.kollins.sofia.UCModule;
import com.example.kollins.sofia.UCModule_View;
import com.example.kollins.sofia.ucinterfaces.DataMemory;

public class MemoryFragment extends Fragment {

    public static final String TAG_MEM_FRAGMENT = "inputFragmentTAG";

    private RecyclerView mRecyclerView;
    public static MemoryAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private DataMemory dataMemory;

    private static TextView memUsageBytesText, memUsagePercentText;

    private Toolbar toolbar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_memory_map, container, false);

        //Set Toolbar
        toolbar = ((Toolbar) layout.findViewById(R.id.mep_map_toolbar));
        toolbar.inflateMenu(R.menu.menu_memory_map);
        toolbar.setOnMenuItemClickListener(new ToolBarMenuItemClick());
        toolbar.setTitle("Arduino " + UCModule.model);

        mRecyclerView = (RecyclerView) layout.findViewById(R.id.memory_map);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new MemoryAdapter(dataMemory);
        mRecyclerView.setAdapter(mAdapter);

        memUsageBytesText = layout.findViewById(R.id.memoryUsageBytes);
        memUsagePercentText = layout.findViewById(R.id.memoryUsagePercent);

        int memoryUsage = dataMemory.getMemoryUsage();
        int memorySize = dataMemory.getMemorySize();
        memUsageBytesText.setText("Bytes: " + memoryUsage + "/" + memorySize);
        memUsagePercentText.setText(String.valueOf((memoryUsage*100)/memorySize) + "%");

        dataMemory.startTimer();

        return layout;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        dataMemory.stopTimer();
    }

    public void setDataMemory(DataMemory dataMemory) {
        this.dataMemory = dataMemory;
    }

    public static void updateMemoryUsage(int memoryUsage, int memorySize) {
        new UpdateMemoryUsage().execute(memoryUsage, memorySize);
    }

    private class ToolBarMenuItemClick implements Toolbar.OnMenuItemClickListener{

        private SearchView searchView;

        @Override
        public boolean onMenuItemClick(final MenuItem item) {
            mRecyclerView.scrollToPosition(0);
            searchView = (SearchView) item.getActionView();
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    if(!searchView.isIconified()) {
                        searchView.setIconified(true);
                    }
                    item.collapseActionView();
                    return true;
                }
                @Override
                public boolean onQueryTextChange(String s) {
                    mAdapter.getFilter().filter(s.toString());
                    return true;
                }
            });
            return true;
        }
    }

    private static class UpdateMemoryUsage extends AsyncTask<Integer, Void, Void>{

        private String memoryByte, memoryPercent;

        @Override
        protected Void doInBackground(Integer... integers) {
            memoryByte = "Bytes: " + integers[0] + "/" + integers[1];
            memoryPercent = String.valueOf((integers[0]*100)/integers[1]) + "%";
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            memUsageBytesText.setText(memoryByte);
            memUsagePercentText.setText(memoryPercent);
        }
    }
}
