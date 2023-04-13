package com.darley.unifound.printer.utils

import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher

object LoginUtil {
    fun encryptWithPublicKey(publicKeyStr: String, plaintext: String): String {
        val publicKeyBytes = Base64.getDecoder().decode(publicKeyStr) // 将 Base64 编码的公钥字符串解码为字节数组
        val publicKeySpec = X509EncodedKeySpec(publicKeyBytes) // 根据字节数组生成公钥规范
        val keyFactory = KeyFactory.getInstance("RSA") // 获取 RSA 密钥工厂
        val publicKey = keyFactory.generatePublic(publicKeySpec) // 生成公钥对象

        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding") // 获取 RSA 加密算法的 Cipher 实例
        cipher.init(Cipher.ENCRYPT_MODE, publicKey) // 初始化 Cipher 为加密模式，并传入公钥

        val plaintextBytes = plaintext.toByteArray(StandardCharsets.UTF_8) // 将明文转换为字节数组
        val ciphertextBytes = cipher.doFinal(plaintextBytes) // 进行加密操作

        return Base64.getEncoder().encodeToString(ciphertextBytes) // 将密文字节数组编码为 Base64 字符串并返回
    }
}