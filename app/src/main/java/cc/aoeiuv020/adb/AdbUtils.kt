package cc.aoeiuv020.adb

import cc.aoeiuv020.encrypt.base64
import com.cgutman.adblib.AdbBase64
import com.cgutman.adblib.AdbCrypto
import java.io.Closeable
import java.io.File
import java.io.IOException

object AdbUtils {

    private const val PUBLIC_KEY_NAME = "public.key"
    private const val PRIVATE_KEY_NAME = "private.key"

    fun readCryptoConfig(dataDir: File): AdbCrypto? {
        val pubKey = File(dataDir, PUBLIC_KEY_NAME)
        val privKey = File(dataDir, PRIVATE_KEY_NAME)

        var crypto: AdbCrypto? = null
        if (pubKey.exists() && privKey.exists()) {
            crypto = try {
                AdbCrypto.loadAdbKeyPair(AndroidBase64(), privKey, pubKey)
            } catch (e: Exception) {
                null
            }
        }

        return crypto
    }

    fun writeNewCryptoConfig(dataDir: File): AdbCrypto {
        val pubKey = File(dataDir, PUBLIC_KEY_NAME)
        val privKey = File(dataDir, PRIVATE_KEY_NAME)

        val crypto: AdbCrypto = AdbCrypto.generateAdbKeyPair(AndroidBase64())

        crypto.saveAdbKeyPair(privKey, pubKey)

        return crypto
    }

    fun safeClose(c: Closeable?): Boolean {
        if (c == null)
            return false

        try {
            c.close()
        } catch (e: IOException) {
            return false
        }

        return true
    }

    class AndroidBase64 : AdbBase64 {
        override fun encodeToString(data: ByteArray): String {
            return data.base64()
        }
    }
}
