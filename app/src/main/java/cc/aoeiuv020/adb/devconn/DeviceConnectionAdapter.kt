package cc.aoeiuv020.adb.devconn

/**
 * Created by AoEiuV020 on 2019.02.01-16:59:31.
 */
abstract class DeviceConnectionAdapter : DeviceConnectionListener {
    override fun notifyConnectionEstablished(devConn: DeviceConnection) {
    }

    override fun notifyConnectionFailed(devConn: DeviceConnection, e: Exception) {
    }

    override fun notifyStreamFailed(devConn: DeviceConnection, e: Exception) {
    }

    override fun notifyStreamClosed(devConn: DeviceConnection) {
    }

    override fun receivedData(devConn: DeviceConnection, data: ByteArray, offset: Int, length: Int) {
        receivedString(devConn, String(data, offset, length))
    }

    open fun receivedString(devConn: DeviceConnection, data: String) {
    }
}