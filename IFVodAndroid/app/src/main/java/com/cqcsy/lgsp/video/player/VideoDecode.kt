package com.cqcsy.lgsp.video.player

import java.io.*
import java.security.Key
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESKeySpec
import javax.crypto.spec.IvParameterSpec

/**
 * 视频文件解密
 */
class VideoDecode : Runnable {
    var filePath: String
    var outPath: String
    var key = "123456  "
    var firstPartLength = 512 * 1024
    var firstPartByte = ByteArray(firstPartLength)
    var inputStream: InputStream? = null
    var outStream: OutputStream? = null

    constructor(path: String, out: String) {
        this.filePath = path
        this.outPath = out
    }

    override fun run() {
        if (filePath.isEmpty() || outPath.isEmpty() || !File(filePath).exists()) {

        } else {
            val file = File(filePath)
            setDecodeKey(File(filePath).name)
            file.deleteOnExit()
            file.createNewFile()
            inputStream = FileInputStream(filePath)
            outStream = FileOutputStream(outPath)
            decode()
        }
    }

    private fun decode() {
        var readLen: Int

        val buffer: ByteArray? = ByteArray(512 * 1024)

        val len = readFirstPart()

        while (true) {
            readLen = inputStream?.read(buffer)!!

            if (readLen <= 0) break
            outStream?.write(buffer, 0, readLen)
        }
        outStream?.flush()
        outStream?.close()
        inputStream?.close()
    }

    private fun setDecodeKey(key: String) {
        var key = key
//        if (!BuildConfig.DEBUG) {
            if (key.length > 8) {
                key = key.substring(0, 8)
            } else {
                key = padRight(key, 8)
            }
            this.key = key
//        }
    }

    private fun padRight(s: String?, n: Int): String {
        return String.format("%-" + n + "s", s)
    }

    fun readFirstPart(): Int? {
        val readLen: Int? = inputStream?.read(firstPartByte)
        if (readLen != null) {
            if (readLen > 0) {
                outStream?.write(decrypt(key, firstPartByte), 0, readLen)
            }
        }
        return readLen
    }

    private fun decrypt(key: String, data: ByteArray?): ByteArray? {
        return if (data == null) null else try {
            val dks = DESKeySpec(key.toByteArray())
            val keyFactory =
                SecretKeyFactory.getInstance("DES")
            // key的长度不能够小于8位字节
            val secretKey: Key = keyFactory.generateSecret(dks)
            val cipher = Cipher.getInstance("DES/CBC/NoPadding")
            val iv =
                IvParameterSpec(key.toByteArray())
            val paramSpec: AlgorithmParameterSpec = iv
            cipher.init(Cipher.DECRYPT_MODE, secretKey, paramSpec)
            cipher.doFinal(data)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}