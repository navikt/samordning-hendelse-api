package no.nav.samordning.hendelser

import no.nav.samordning.hendelser.TestTokenHelper.createExpiredJwt
import no.nav.samordning.hendelser.TestTokenHelper.createFutureJwt
import java.security.NoSuchAlgorithmException

object TestAuthHelper {

    private const val AUTH_SCHEME = "Bearer"

    @Throws(NoSuchAlgorithmException::class)
    fun token(orgno: String, verifiedSignature: Boolean): String {
        return header(TestTokenHelper.token(orgno, verifiedSignature))
    }

    fun expiredToken(orgno: String): String {
        return header(createExpiredJwt(orgno))
    }

    fun futureToken(orgno: String): String {
        return header(createFutureJwt(orgno))
    }

    fun serviceToken(): String {
        return header(TestTokenHelper.serviceToken())
    }

    fun emptyToken(): String {
        return header(TestTokenHelper.emptyToken())
    }

    private fun header(token: String): String {
        return "$AUTH_SCHEME $token"
    }
}
