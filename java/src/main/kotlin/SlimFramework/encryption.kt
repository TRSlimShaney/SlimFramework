package SlimFramework

import java.security.NoSuchAlgorithmException
import java.security.spec.InvalidKeySpecException
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec


class FrwAESEncryption(private val plainText: String, private val AESSalt: String, private val iv: String) {

    private val pswdIterations = 10
    private val keySize = 128;
    private val cypherInstance = "AES/CBC/PKCS5Padding"
    private val secretKeyInstance = "PBKDF2WithHMacSHA1"
    //private val plainText = "sampleText"
    //private val AESSalt = "exampleSalt"
    //private val initVector = "8119745113154120"

    fun encrypt(text: String): String {
        val skeyspec = SecretKeySpec(getRaw(text, AESSalt), "AES")
        val cipher = Cipher.getInstance(cypherInstance)
        cipher.init(Cipher.ENCRYPT_MODE, skeyspec, IvParameterSpec(iv.toByteArray()))
        val encrypted = cipher.doFinal(text.toByteArray())
        return Base64.getEncoder().encodeToString(encrypted)
    }

    fun decrypt(text: String): String {
        val encbytes = Base64.getDecoder().decode(text)
        val skeyspec = SecretKeySpec(getRaw(text, AESSalt), "AES")
        val cipher = Cipher.getInstance(cypherInstance)
        cipher.init(Cipher.DECRYPT_MODE, skeyspec, IvParameterSpec(iv.toByteArray()))
        val decrypted = cipher.doFinal(encbytes)
        return decrypted.toString()
    }

    private fun getRaw(text: String, salt: String): ByteArray? {
        try {
            var fact = SecretKeyFactory.getInstance(secretKeyInstance)
            var spec = PBEKeySpec(text.toCharArray(), salt.toByteArray(), pswdIterations, keySize)
            return fact.generateSecret(spec).encoded
        }
        catch (e: InvalidKeySpecException) {
            e.printStackTrace()
        }
        catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        val arr = ByteArray(1)
        arr[0] = 0
        return arr
    }
}