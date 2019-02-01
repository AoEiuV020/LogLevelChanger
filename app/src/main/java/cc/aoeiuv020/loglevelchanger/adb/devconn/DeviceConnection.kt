package cc.aoeiuv020.loglevelchanger.adb.devconn

import cc.aoeiuv020.loglevelchanger.adb.AdbUtils
import com.cgutman.adblib.AdbConnection
import com.cgutman.adblib.AdbCrypto
import com.cgutman.adblib.AdbStream
import java.io.Closeable
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.LinkedBlockingQueue

@Suppress("MemberVisibilityCanBePrivate", "MayBeConstant")
class DeviceConnection(
    private val listener: DeviceConnectionListener,
    private val crypto: AdbCrypto,
    val host: String,
    val port: Int
) : Closeable {

    private var connection: AdbConnection? = null
    private var shellStream: AdbStream? = null

    var isClosed: Boolean = false
        private set

    private val commandQueue = LinkedBlockingQueue<ByteArray>()

    fun queueCommand(command: String): Boolean {
        return queueBytes((command + '\n').toByteArray(Charsets.UTF_8))
    }

    fun queueBytes(buffer: ByteArray): Boolean {
        /* Queue it up for sending to the device */
        commandQueue.add(buffer)
        return true
    }

    fun startConnect() {
        Thread(Runnable {
            var connected = false
            val socket = Socket()

            try {
                /* Establish a connect to the remote host */
                socket.connect(InetSocketAddress(host, port), CONN_TIMEOUT)
            } catch (e: IOException) {
                listener.notifyConnectionFailed(this@DeviceConnection, e)
                return@Runnable
            }

            try {
                /* Establish the application layer connection */
                connection = AdbConnection.create(socket, crypto)
                connection!!.connect()

                /* Open the shell stream */
                shellStream = connection!!.open("shell:")
                connected = true
            } catch (e: IOException) {
                listener.notifyConnectionFailed(this@DeviceConnection, e)
            } catch (e: InterruptedException) {
                listener.notifyConnectionFailed(this@DeviceConnection, e)
            } finally {
                /* Cleanup if the connection failed */
                if (!connected) {
                    AdbUtils.safeClose(shellStream)

                    /* The AdbConnection object will close the underlying socket
						 * but we need to close it ourselves if the AdbConnection object
						 * wasn't successfully constructed.
						 */
                    if (!AdbUtils.safeClose(connection)) {
                        try {
                            socket.close()
                        } catch (e: IOException) {
                        }

                    }

                    return@Runnable
                }
            }

            /* Notify the listener that the connection is complete */
            listener.notifyConnectionEstablished(this@DeviceConnection)

            /* Start the receive thread */
            startReceiveThread()

            /* Enter the blocking send loop */
            sendLoop()
        }).start()
    }

    private fun sendLoop() {
        /* We become the send thread */
        try {
            while (true) {
                /* Get the next command */
                val command = commandQueue.take()

                /* This may be a close indication */
                if (shellStream!!.isClosed) {
                    listener.notifyStreamClosed(this@DeviceConnection)
                    break
                }

                /* Issue it to the device */
                shellStream!!.write(command)
            }
        } catch (e: IOException) {
            listener.notifyStreamFailed(this@DeviceConnection, e)
        } catch (e: InterruptedException) {
        } finally {
            AdbUtils.safeClose(this@DeviceConnection)
        }
    }

    private fun startReceiveThread() {
        Thread(Runnable {
            try {
                while (!shellStream!!.isClosed) {
                    val data = shellStream!!.read()
                    listener.receivedData(this@DeviceConnection, data, 0, data.size)
                }
                listener.notifyStreamClosed(this@DeviceConnection)
            } catch (e: IOException) {
                listener.notifyStreamFailed(this@DeviceConnection, e)
            } catch (e: InterruptedException) {
            } finally {
                AdbUtils.safeClose(this@DeviceConnection)
            }
        }).start()
    }

    @Throws(IOException::class)
    override fun close() {
        if (isClosed) {
            return
        } else {
            isClosed = true
        }

        /* Close the stream first */
        AdbUtils.safeClose(shellStream)

        /* Now the connection (and underlying socket) */
        AdbUtils.safeClose(connection)

        /* Finally signal the command queue to allow the send thread to terminate */
        commandQueue.add(ByteArray(0))
    }

    override fun toString(): String {
        return "DeviceConnection(listener=$listener, host='$host', port=$port, connection=$connection, isClosed=$isClosed, commandQueue=$commandQueue)"
    }

    companion object {
        private val CONN_TIMEOUT = 5000
    }
}
