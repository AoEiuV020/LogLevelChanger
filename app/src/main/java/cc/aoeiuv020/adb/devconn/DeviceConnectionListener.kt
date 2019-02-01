package cc.aoeiuv020.adb.devconn

interface DeviceConnectionListener {

    fun notifyConnectionEstablished(devConn: DeviceConnection)

    fun notifyConnectionFailed(devConn: DeviceConnection, e: Exception)

    fun notifyStreamFailed(devConn: DeviceConnection, e: Exception)

    fun notifyStreamClosed(devConn: DeviceConnection)

    fun receivedData(devConn: DeviceConnection, data: ByteArray, offset: Int, length: Int)
}
