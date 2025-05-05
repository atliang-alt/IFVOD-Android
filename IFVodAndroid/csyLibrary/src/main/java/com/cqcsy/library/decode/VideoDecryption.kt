package com.cqcsy.library.decode

import java.io.InputStream
import java.io.OutputStream
import java.security.Key
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESKeySpec
import javax.crypto.spec.IvParameterSpec

class VideoDecryption {
    var inputStream: InputStream? = null
    var outStream: OutputStream? = null
    var key = "123456  "
    var firstPartLength = 512 * 1024
    var lastPartLength = 512 * 1024
    var eachPartLength = 1024 * 1024
    var totalLength: Long = 0
    var firstPartByte = ByteArray(firstPartLength)
    var lastPartByte = ByteArray(lastPartLength)
    var eachPartByte = ByteArray(eachPartLength)

    constructor(stream: InputStream?, length:Long) {
        inputStream = stream
        totalLength = length
//        totalLength = inputStream!!.available().toLong()  // 文件超过2G，用此方法读取文件大小返回0
        //this.outStream = out;
    }

    private fun padRight(s: String?, n: Int): String {
        return String.format("%-" + n + "s", s)
    }


    fun setDecodeKey(key: String) {
        val tempKey = if (key.length > 8) {
            key.substring(0, 8)
        } else {
            padRight(key, 8)
        }
        this.key = tempKey
    }

    private fun readFirstPart(): Int {
        val readLen = inputStream!!.read(firstPartByte)
        if (readLen > 0) {
            outStream!!.write(decrypt(key, firstPartByte), 0, readLen)
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
            val iv = IvParameterSpec(key.toByteArray())
            val paramSpec: AlgorithmParameterSpec = iv
            cipher.init(Cipher.DECRYPT_MODE, secretKey, paramSpec)
            cipher.doFinal(data)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun readLastPart(): Int {
        val readLen = inputStream!!.read(lastPartByte)
        if (readLen > 0) {
            outStream!!.write(decrypt(key, lastPartByte), 0, readLen)
        }
        return readLen
    }

    fun readEachPart(): Int {
        val readLen = inputStream!!.read(eachPartByte)
        if (readLen > 0) {
            outStream!!.write(decrypt(key, eachPartByte), 0, readLen)
        }
        return readLen
    }


    private fun readNormalPart(): Int {
        val readLen = inputStream!!.read(eachPartByte)
        if (readLen > 0) {
            outStream!!.write(eachPartByte, 0, readLen)
        }
        return readLen
    }

    fun start() {
        readFirstPart()
        while (true) {
            if (readNormalPart() <= 0) {
                break
            }
        }
    }
}