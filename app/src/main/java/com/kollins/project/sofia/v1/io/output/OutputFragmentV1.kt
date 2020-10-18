package com.kollins.project.sofia.v1.io.output

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.ListFragment
import com.kollins.project.sofia.R

class OutputFragmentV1 : ListFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//            return inflater.inflate(R.layout.v1_output_fragment, container, false)
        val view = inflater.inflate(R.layout.v1_output_fragment, container, false)
        view.visibility = View.GONE
        return view
    }
}

