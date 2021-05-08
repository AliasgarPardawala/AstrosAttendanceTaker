package com.arsiwala.shamoil.astrosattendance.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.arsiwala.shamoil.astrosattendance.AES
import com.arsiwala.shamoil.astrosattendance.R
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import kotlinx.android.synthetic.main.fragment_makeqr.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*


class QRFragment : Fragment() {

    val passwords = Firebase.firestore.collection("admin").document("passwords")
    val aes = AES()
    lateinit private var key : String
    lateinit private  var password:String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_makeqr, container, false)
        val etpass: TextView = root.findViewById(R.id.etpass)
        getpasswords()
        val sdf = SimpleDateFormat("dd/MM/yy")
        val date = sdf.format(Date())

        etpass.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if(s.toString() == password) {
                    val inputManager : InputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputManager.hideSoftInputFromWindow(view?.windowToken,0)
                    val text = aes.encrypt(date, key)
                    if(text != null) {
                        etpass.visibility = View.INVISIBLE
                        val bitmap = generateQR(text)
                        imageView.setImageBitmap(bitmap)
                        Log.d("QR", text.toString())
                        Toast.makeText(activity, text, Toast.LENGTH_SHORT).show()
                    }
                    else {
                        Toast.makeText(activity, "Couldn't Encrypt", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
        return root
    }

    fun getpasswords() = CoroutineScope(Dispatchers.IO).launch {
        try{
            val querySnapshot = passwords.get().await()
            password = querySnapshot.get("QR Password").toString()
            key = querySnapshot.get("AES Encryption").toString()
        } catch (e:Exception) {
            Log.d("QR", e.message.toString())
        }
    }

    fun generateQR(text :String): Bitmap {
        val width = 500
        val height = 500
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val codeWriter = MultiFormatWriter()
        try {
            val bitMatrix = codeWriter.encode(text, BarcodeFormat.QR_CODE, width, height)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
        } catch (e: WriterException) { Log.d(this.toString(), "generateQRCode: ${e.message}") }
        return bitmap
    }
}