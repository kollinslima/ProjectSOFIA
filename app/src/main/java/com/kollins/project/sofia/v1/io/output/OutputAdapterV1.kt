package com.kollins.project.sofia.v1.io.output

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter

class OutputAdapterV1(private var outputList: MutableList<OutputPinV1>) : BaseAdapter() {

    override fun getCount(): Int {
        return outputList.size
    }

    override fun getItem(position: Int): Any {
        return outputList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        TODO("Not yet implemented")
    }
}