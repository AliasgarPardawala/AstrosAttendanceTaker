package com.arsiwala.shamoil.astrosattendance.ui

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.arsiwala.shamoil.astrosattendance.AES
import com.arsiwala.shamoil.astrosattendance.MainActivity
import com.arsiwala.shamoil.astrosattendance.R
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import java.util.jar.Manifest

class ScanFragment: Fragment() {

    private val aes = AES()
    private val passwords = Firebase.firestore.collection("admin").document("passwords")
    private val attend = Firebase.firestore.collection("2020-2021").document("Attendance")
    private lateinit var key:String
    private lateinit var codeScanner: CodeScanner


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        getkey()
        return inflater.inflate(R.layout.fragment_scan, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val scannerView = view.findViewById<CodeScannerView>(R.id.scanner_view)
        val activity = requireActivity()
        codeScanner = CodeScanner(activity, scannerView)
        codeScanner.decodeCallback = DecodeCallback {
            activity.runOnUiThread {
                lifecycleScope.launch {
                    check(it.text)
                }
            }
        }
        scannerView.setOnClickListener {
            codeScanner.startPreview()
        }
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    fun getkey() = CoroutineScope(Dispatchers.IO).launch {
        try {
            val querySnapshot = passwords.get().await()
            key = querySnapshot.get("AES Encryption").toString()
        } catch (e:Exception) {
            Log.d("Scan", e.message.toString())
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend  fun check(scan : String) {
        val cm = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        val isConnected: Boolean = activeNetwork?.isConnected == true
        Log.d("QR", key)
        try {
            val sdf = SimpleDateFormat("dd/MM/yy")
            val date = sdf.format(Date())
            Log.d("QR", key)
            var last_attendance: String? = null
            lifecycleScope.launch {
                last_attendance = (activity as MainActivity).read("last")
            }.join()
            val email = (activity as MainActivity).signedIn()
            Log.d("QR", email.toString())
            if(last_attendance != date) {
                if(email != null) {
                    if (isConnected) {
                        val decrypted = aes.decrypt(scan, key)
                        Log.d("QR", decrypted.toString())
                        if (decrypted == date) {
                            lifecycleScope.launch {
                                (activity as MainActivity).save("last", date)
                                attend.update(email.substring(0,email.indexOf('@')), FieldValue.arrayUnion(Timestamp.now()))
                            }.join()
                            Toast.makeText(activity, "Attendance Marked", Toast.LENGTH_SHORT).show()
                        } else
                            Toast.makeText(activity, "Invalid QR", Toast.LENGTH_SHORT).show()
                    } else
                        Toast.makeText(activity, "No Internet Connection", Toast.LENGTH_SHORT).show()
                }else
                    Toast.makeText(activity, "Not Logged In ", Toast.LENGTH_SHORT).show()
            }else
                Toast.makeText(activity, "Attendance can only be marked once per day", Toast.LENGTH_SHORT).show()
        }
        catch (e:Exception){
            Toast.makeText(activity,"Invalid QR", Toast.LENGTH_SHORT).show()
        }
        findNavController().popBackStack(R.id.nav_home, false)
    }
}