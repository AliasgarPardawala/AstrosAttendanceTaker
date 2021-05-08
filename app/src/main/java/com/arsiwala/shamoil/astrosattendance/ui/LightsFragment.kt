package com.arsiwala.shamoil.astrosattendance.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.arsiwala.shamoil.astrosattendance.R


class LightsFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_lights, container, false)

        val btnL =root.findViewById<Button>(R.id.LightL)
        val btnR =root.findViewById<Button>(R.id.LightR)
        val btnF =root.findViewById<Button>(R.id.Fan)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
}