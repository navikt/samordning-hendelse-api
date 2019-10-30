package no.nav.samordning.hendelser

import no.nav.samordning.hendelser.TestTokenHelper.createExpiredJwt
import no.nav.samordning.hendelser.TestTokenHelper.createFutureJwt
import java.security.NoSuchAlgorithmException

object TestAuthHelper {

    private const val AUTH_SCHEME = "Bearer"

    @Throws(NoSuchAlgorithmException::class)
    fun token(orgno: String, verifiedSignature: Boolean) =
            header(TestTokenHelper.token(orgno, verifiedSignature))

    fun expiredToken(orgno: String) = header(createExpiredJwt(orgno))

    fun futureToken(orgno: String) = header(createFutureJwt(orgno))

    fun serviceToken() = header(TestTokenHelper.serviceToken())

    fun emptyToken() = header(TestTokenHelper.emptyToken())

    private fun header(token: String) = "$AUTH_SCHEME $token"
}
