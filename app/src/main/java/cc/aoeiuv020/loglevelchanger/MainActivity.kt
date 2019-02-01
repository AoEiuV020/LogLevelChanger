package cc.aoeiuv020.loglevelchanger

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cc.aoeiuv020.adb.AdbManager
import cc.aoeiuv020.adb.devconn.DeviceConnection
import cc.aoeiuv020.adb.devconn.DeviceConnectionAdapter
import cc.aoeiuv020.adb.devconn.DeviceConnectionListener
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

class MainActivity : AppCompatActivity(), AnkoLogger {
    private val listener: DeviceConnectionListener = object : DeviceConnectionAdapter() {
        override fun notifyConnectionEstablished(devConn: DeviceConnection) {
            status("连接成功")
        }

        override fun notifyConnectionFailed(devConn: DeviceConnection, e: Exception) {
            status("连接失败")
        }

        override fun notifyStreamFailed(devConn: DeviceConnection, e: Exception) {
            status("流读写失败")
        }

        override fun notifyStreamClosed(devConn: DeviceConnection) {
            status("连接已经关闭")
        }

        override fun receivedString(devConn: DeviceConnection, data: String) {
            runOnUiThread {
                etConsole.text.append(data)
                etConsole.setSelection(etConsole.text.length)
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        AdbManager.init(this)

        btnConnect.setOnClickListener {
            AdbManager.connect(etHost.text.toString(), etPort.text.toString().toInt())
        }

        btnDisconnect.setOnClickListener {
            AdbManager.close()
        }

        btnSend.setOnClickListener {
            if (AdbManager.send(etCommand.text.toString())) {
                status("命令发送成功")
            } else {
                status("命令发送失败")
            }
        }

        if (AdbManager.isConnected) {
            status("已经连接")
        }

        AdbManager.register(this, listener)
    }

    fun status(text: String) {
        info { "status: $text" }
        runOnUiThread {
            tvStatus.text = text
        }
    }
}
