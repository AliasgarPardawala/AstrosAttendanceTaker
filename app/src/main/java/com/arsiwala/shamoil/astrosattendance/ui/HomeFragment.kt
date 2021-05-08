package com.arsiwala.shamoil.astrosattendance.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.arsiwala.shamoil.astrosattendance.MainActivity
import com.arsiwala.shamoil.astrosattendance.R
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment: Fragment() {

    val sdf = SimpleDateFormat("dd/MM/yy")
    val date = sdf.format(Date())


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        lifecycleScope.launch {
            val value = (activity as MainActivity).read("last")
//            Log.d("QR", value.toString())
        }
        return root
    }
}