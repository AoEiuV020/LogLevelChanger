package cc.aoeiuv020.log.level

import android.content.Context
import cc.aoeiuv020.adb.AdbManager

/**
 * Created by AoEiuV020 on 2019.02.01-18:57:06.
 */
object LogLevelManager {
    private const val HOST = "127.0.0.1"
    private const val PORT = 5555

    fun prepare(ctx: Context) {
        AdbManager.init(ctx)
        AdbManager.connect(HOST, PORT)
    }

    fun setLogLevel(tag: String, level: LogLevel) {
        val command = LogLevelUtil.createCommand(tag, level)
        AdbManager.send(command)
    }

}