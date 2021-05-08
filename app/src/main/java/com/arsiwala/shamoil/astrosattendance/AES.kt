package com.arsiwala.shamoil.astrosattendance

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.io.UnsupportedEncodingException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.security.Security
import java.util.*
import javax.crypto.*
import javax.crypto.spec.SecretKeySpec

class AES {
    @RequiresApi(Build.VERSION_CODES.O)
    fun encrypt(strToEncrypt: String, secret_key: String): String? {
        Security.addProvider(BouncyCastleProvider())
        var keyBytes: ByteArray

        try {
            keyBytes = secret_key.toByteArray(charset("UTF8"))
            val skey = SecretKeySpec(keyBytes, "AES")
            val input = strToEncrypt.toByteArray(charset("UTF8"))

            synchronized(Cipher::class.java) {
                val cipher = Cipher.getInstance("AES/ECB/PKCS7Padding")
                cipher.init(Cipher.ENCRYPT_MODE, skey)

                val cipherText = ByteArray(cipher.getOutputSize(input.size))
                var ctLength = cipher.update(
                    input, 0, input.size,
                    cipherText, 0
                )
                ctLength += cipher.doFinal(cipherText, ctLength)
                return String(
                    Base64.getEncoder().encode(cipherText)
                )
            }
        } catch (uee: UnsupportedEncodingException) {
            Log.d("Encrypt",uee.toString())
        } catch (ibse: IllegalBlockSizeException) {
            Log.d("Encrypt",ibse.toString())
        } catch (bpe: BadPaddingException) {
            Log.d("Encrypt",bpe.toString())
        } catch (ike: InvalidKeyException) {
            Log.d("Encrypt",ike.toString())
        } catch (nspe: NoSuchPaddingException) {
            Log.d("Encrypt",nspe.toString())
        } catch (nsae: NoSuchAlgorithmException) {
            Log.d("Encrypt",nsae.toString())
        } catch (e: ShortBufferException) {
            Log.d("Encrypt",e.toString())
        }

        return null
    }

    /**
     * aes decryption
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun decrypt(strToDecrypt: String?, key: String): String? {
        Security.addProvider(BouncyCastleProvider())
        var keyBytes: ByteArray

        try {
            keyBytes = key.toByteArray(charset("UTF8"))
            val skey = SecretKeySpec(keyBytes, "AES")
            val input = org.bouncycastle.util.encoders.Base64
                .decode(strToDecrypt?.trim { it <= ' ' }?.toByteArray(charset("UTF8")))

            synchronized(Cipher::class.java) {
                val cipher = Cipher.getInstance("AES/ECB/PKCS7Padding")
                cipher.init(Cipher.DECRYPT_MODE, skey)

                val plainText = ByteArray(cipher.getOutputSize(input.size))
                var ptLength = cipher.update(input, 0, input.size, plainText, 0)
                ptLength += cipher.doFinal(plainText, ptLength)
                val decryptedString = String(plainText)
                return decryptedString.trim { it <= ' ' }
            }
        } catch (uee: UnsupportedEncodingException) {
            Log.d("Encrypt",uee.toString())
        } catch (ibse: IllegalBlockSizeException) {
            Log.d("Encrypt",ibse.toString())
        } catch (bpe: BadPaddingException) {
            Log.d("Encrypt",bpe.toString())
        } catch (ike: InvalidKeyException) {
            Log.d("Encrypt",ike.toString())
        } catch (nspe: NoSuchPaddingException) {
            Log.d("Encrypt",nspe.toString())
        } catch (nsae: NoSuchAlgorithmException) {
            Log.d("Encrypt",nsae.toString())
        } catch (e: ShortBufferException) {
            Log.d("Encrypt",e.toString())
        }
        return null
    }
}