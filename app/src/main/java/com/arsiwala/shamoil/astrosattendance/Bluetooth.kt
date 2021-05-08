package com.arsiwala.shamoil.astrosattendance

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import android.widget.Toast
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class Bluetooth {

    val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    var bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    var device: BluetoothDevice? = null
    var connected: Boolean = false

    fun check_paired(): Boolean {
        if (bluetoothAdapter != null) {
            if (bluetoothAdapter?.isEnabled == false) {
                bluetoothAdapter!!.enable()
            }
            val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
            pairedDevices?.forEach {
                if(it.name == "AstrosLights") {
                    device = it
                    return true
                }
            }
        }
        return false
    }

    fun connect(): Boolean {
        var conn = ConnectThread(device)
        conn.run()
        return connected
    }

    private inner class ConnectThread(device: BluetoothDevice?) : Thread() {

        private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            device?.createRfcommSocketToServiceRecord(uuid)
        }

        public override fun run() {
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter?.cancelDiscovery()

            mmSocket?.use { socket ->
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                try {
                    socket.connect()
                    Log.d("Paired", "Connected")
                    write("S")
                    receive()
                    connected = true
                } catch (e: IOException) {
                    Log.d("Bluetooth",e.message.toString())
                }
            }
        }

        // Closes the client socket and causes the thread to finish.
        fun cancel() {
            try {
                mmSocket?.close()
                connected = false
                Log.e("Paired", "Socket Closed")
            } catch (e: IOException) {
                Log.e("Paired", "Could not close the client socket", e)
            }
        }

        private fun write(s:String) {
            if (mmSocket != null) {
                try { // Converting the string to bytes for transferring
                    mmSocket?.outputStream?.write(s.toByteArray())
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        private fun receive() {
            if(mmSocket != null) {
                val inputStream = mmSocket?.inputStream
                try {
                    var available = inputStream?.available().toString().length
                    var bytes = available?.let { ByteArray(it) }
                    if (available > 0) {
                        Log.d("Paired", available.toString())
                        inputStream?.read(bytes, 0, available)
                        val text = bytes?.let { String(it) }
                        val dat = text.toInt().toByte()
                        Log.d("Value", dat.toString())
                        write("C")
                    }
                } catch (e: Exception) {
                    Log.d("Paired", "Cannot read data", e)
                }
            }
        }
    }
}