package no.nav.samordning.hendelser

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTCreator
import com.auth0.jwt.algorithms.Algorithm
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.NoSuchAlgorithmException
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.*
import java.util.Calendar.DECEMBER
import java.util.Calendar.JANUARY

object TestTokenHelper {

    private const val DEFAULT_SCOPE = "nav:pensjon/v1/samordning"
    private var keyPair = generatedKeyPair

    private val keyModulus: ByteArray
        get() = publicKey(keyPair).modulus.toByteArray()

    @Throws(NoSuchAlgorithmException::class)
    fun token(orgno: String, verifiedSignature: Boolean) = token(DEFAULT_SCOPE, orgno, verifiedSignature)

    @Throws(NoSuchAlgorithmException::class)
    fun token(scope: String, orgno: String, verifiedSignature: Boolean): String {
        val signingKeys = if (verifiedSignature) keyPair else generatedKeyPair
        val algorithm = Algorithm.RSA256(publicKey(signingKeys), privateKey(signingKeys))
        return createJwt(scope, orgno, algorithm)
    }

    fun serviceToken() = createServiceJwt(algorithm)!!

    internal fun emptyToken() = JWT.create().sign(algorithm)

    @Throws(NoSuchAlgorithmException::class)
    internal fun generateJwks() = String.format(
            """{
                |  "keys": [{
                |      "kty": "RSA",
                |      "e": "AQAB",
                |      "use": "sig",
                |      "n": "%s"
                |    }
                |  ]
                }""".trimMargin(),
            Base64.getUrlEncoder().encodeToString(keyModulus))

    internal fun createExpiredJwt(orgno: String) = addClaims(expiredJwt, orgno, algorithm)

    internal fun createFutureJwt(orgno: String) = addClaims(futureJwt, orgno, algorithm)

    private fun createJwt(scope: String, orgno: String, algorithm: Algorithm) =
            addClaims(JWT.create(), scope, orgno, algorithm)

    private fun addClaims(jwt: JWTCreator.Builder, orgno: String, algorithm: Algorithm) =
            addClaims(jwt, DEFAULT_SCOPE, orgno, algorithm)

    private fun addClaims(jwt: JWTCreator.Builder, scope: String, orgno: String, algorithm: Algorithm) = jwt
            .withArrayClaim("aud", arrayOf("tp_ordning", "preprod"))
            .withClaim("iss", "https://badserver/provider/")
            .withClaim("scope", scope)
            .withClaim("client_id", "tp_ordning")
            .withClaim("consumer", """{ "authority" : "iso6523-actorid-upis", "ID" : "0192:$orgno"}""")
        //.withClaim("client_orgno", orgno)
            .sign(algorithm)

    private fun createServiceJwt(algorithm: Algorithm) = JWT.create()
            .withClaim("azp", "srvTest")
            .withClaim("iss", "test")
            .sign(algorithm)

    private val expiredJwt: JWTCreator.Builder
        get() = JWT.create().withExpiresAt(GregorianCalendar(2018, DECEMBER, 31).time)

    private val futureJwt: JWTCreator.Builder
        get() = JWT.create().withNotBefore(GregorianCalendar(2101, JANUARY, 1).time)

    private val algorithm: Algorithm
        get() = Algorithm.RSA256(publicKey(keyPair), privateKey(keyPair))

    private val generatedKeyPair: KeyPair
        @Throws(NoSuchAlgorithmException::class)
        get() = KeyPairGenerator.getInstance("RSA").run {
            initialize(1024)
            generateKeyPair()
        }

    private fun privateKey(keyPair: KeyPair) = keyPair.private as RSAPrivateKey

    private fun publicKey(keyPair: KeyPair) = keyPair.public as RSAPublicKey
}
