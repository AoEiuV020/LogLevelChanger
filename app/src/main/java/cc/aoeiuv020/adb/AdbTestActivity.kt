package cc.aoeiuv020.adb

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cc.aoeiuv020.adb.devconn.DeviceConnection
import cc.aoeiuv020.adb.devconn.DeviceConnectionAdapter
import cc.aoeiuv020.adb.devconn.DeviceConnectionListener
import cc.aoeiuv020.loglevelchanger.R
import kotlinx.android.synthetic.main.activity_adb_test.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.startActivity

class AdbTestActivity : AppCompatActivity(), AnkoLogger {
    companion object {
        fun start(ctx: Context) {
            ctx.startActivity<AdbTestActivity>()
        }
    }

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
        setContentView(R.layout.activity_adb_test)

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
