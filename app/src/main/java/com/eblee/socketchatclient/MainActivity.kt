package com.eblee.socketchatclient

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.InetAddress
import java.net.Socket

class MainActivity : AppCompatActivity() {

    private lateinit var socket: Socket
    private var input: DataInputStream? = null
    private var output: DataOutputStream? = null

    var isConnected = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_connect.setOnClickListener {
            clientSocketOpen()
        }

        btn_send.setOnClickListener {
            sendMessage()
        }
    }


    private fun clientSocketOpen() {
        Thread {
            try {
                val ip = et_ip_address.text.toString()
                val port = et_port.text.toString()
                if (ip.isEmpty() || port.isEmpty()) {
                    // 입력 안내 토스트
                } else {
                    socket = Socket(InetAddress.getByName(ip), port.toInt())

                    input = DataInputStream(socket.getInputStream())
                    output = DataOutputStream(socket.getOutputStream())

                    this.runOnUiThread {
                        Toast.makeText(this, "Connect With Server", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

            while (isConnected) {
                try {
                    var message = input?.readUTF()

                    runOnUiThread {
                        message = if (tv_chatting.text.toString().isEmpty()) {
                            "[RECV] $message"
                        } else {
                            "${tv_chatting.text}\n[RECV] $message"
                        }
                        tv_chatting.text = message
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }.start()
    }


    private fun sendMessage() {
        if (output == null) return

        Thread {
            var message = et_message.text.toString()

            try {
                runOnUiThread {
                    message = if (tv_chatting.text.toString().isEmpty()) {
                        "[SEND] $message"
                    } else {
                        "${tv_chatting.text}\n[SEND] $message"
                    }

                    tv_chatting.text = message
                }

                output?.writeUTF(et_message.text.toString())
                output?.flush()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }.start()
    }

    override fun onStop() {
        super.onStop()
        try {
            socket.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}