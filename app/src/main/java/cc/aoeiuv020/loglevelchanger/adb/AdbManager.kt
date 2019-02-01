package cc.aoeiuv020.loglevelchanger.adb

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import cc.aoeiuv020.anull.notNull
import cc.aoeiuv020.loglevelchanger.adb.devconn.DeviceConnection
import cc.aoeiuv020.loglevelchanger.adb.devconn.DeviceConnectionListener
import com.cgutman.adblib.AdbCrypto
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.doAsync

/**
 * Created by AoEiuV020 on 2019.02.01-14:23:43.
 */
object AdbManager : AnkoLogger {
    private lateinit var crypto: AdbCrypto

    private var connection: DeviceConnection? = null

    private val listeners: MutableList<DeviceConnectionListener> = mutableListOf()

    val isConnected: Boolean
        get() = connection?.isClosed == false

    fun init(ctx: Context) {
        crypto = AdbUtils.readCryptoConfig(ctx.filesDir)
                ?: AdbUtils.writeNewCryptoConfig(ctx.filesDir)
    }

    fun addDataListener(listener: DeviceConnectionListener) {
        listeners.add(listener)
    }

    fun removeDataListener(listener: DeviceConnectionListener) {
        listeners.remove(listener)
    }

    fun register(lifecycleOwner: LifecycleOwner, listener: DeviceConnectionListener) {
        lifecycleOwner.lifecycle.addObserver(
            object : LifecycleObserver {
                @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
                fun onCreate() {
                    addDataListener(listener)
                }

                fun onDestory() {
                    removeDataListener(listener)
                }
            }
        )
    }

    fun connect(host: String, port: Int) {
        if (isConnected) {
            val conn = connection.notNull()
            if (conn.host == host && conn.port == port) {
                return
            } else {
                close()
            }
        }
        connection = DeviceConnection(object : DeviceConnectionListener {
            override fun notifyConnectionEstablished(devConn: DeviceConnection) {
                listeners.forEach {
                    it.notifyConnectionEstablished(devConn)
                }
            }

            override fun notifyConnectionFailed(devConn: DeviceConnection, e: Exception) {
                connection = null
                listeners.forEach {
                    it.notifyConnectionFailed(devConn, e)
                }
            }

            override fun notifyStreamFailed(devConn: DeviceConnection, e: Exception) {
                connection = null
                listeners.forEach {
                    it.notifyStreamFailed(devConn, e)
                }
            }

            override fun notifyStreamClosed(devConn: DeviceConnection) {
                connection = null
                listeners.forEach {
                    it.notifyStreamClosed(devConn)
                }
            }

            override fun receivedData(devConn: DeviceConnection, data: ByteArray, offset: Int, length: Int) {
                listeners.forEach {
                    it.receivedData(devConn, data, offset, length)
                }
            }
        }, crypto, host, port).apply {
            startConnect()
        }
    }

    fun close() {
        // close时有网络操作，不能放主线程，
        connection?.let {
            doAsync {
                it.close()
            }
        }
        connection = null
    }

    fun send(command: String): Boolean {
        return connection?.queueCommand(command) == true
    }
}