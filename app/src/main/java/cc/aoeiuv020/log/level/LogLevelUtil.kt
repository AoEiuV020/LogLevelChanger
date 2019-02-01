package cc.aoeiuv020.log.level

import cc.aoeiuv020.log.level.LogLevel.*

/**
 * Created by AoEiuV020 on 2019.02.01-19:04:06.
 */
object LogLevelUtil {
    fun createCommand(tag: String, level: LogLevel): String {
        val sLevel = when (level) {
            VERBOSE -> "V"
            DEBUG -> "D"
            INFO -> "I"
            WARNING -> "W"
            ERROR -> "E"
            SILENT -> "S"
        }
        return "setprop log.tag.$tag $sLevel"
    }
}