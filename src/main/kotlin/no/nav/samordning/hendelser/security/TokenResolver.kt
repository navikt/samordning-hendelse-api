package no.nav.samordning.hendelser.security

import com.auth0.jwt.JWT
import no.nav.samordning.hendelser.consumer.TpregisteretConsumer
import no.nav.samordning.hendelser.security.TokenResolver.ClaimKeys.CLIENT_ID
import no.nav.samordning.hendelser.security.TokenResolver.ClaimKeys.CLIENT_ORGANISATION_NUMBER
import no.nav.samordning.hendelser.security.TokenResolver.ClaimKeys.ISSUER
import no.nav.samordning.hendelser.security.TokenResolver.ClaimKeys.SCOPE
import org.json.JSONException
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.server.resource.BearerTokenError
import org.springframework.security.oauth2.server.resource.BearerTokenErrorCodes.INVALID_REQUEST
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver
import java.util.*
import javax.servlet.http.HttpServletRequest

class TokenResolver(
        private val bearerTokenResolver: BearerTokenResolver,
        private val tpRegisteretConsumer: TpregisteretConsumer
) : BearerTokenResolver {

    private object ClaimKeys {
        const val CLIENT_ID = "client_id"
        const val CLIENT_ORGANISATION_NUMBER = "client_orgno"
        const val ISSUER = "iss"
        const val SCOPE = "scope"
    }

    override fun resolve(request: HttpServletRequest): String? {

        val token = bearerTokenResolver.resolve(request) ?: return null
        val claims = JSONObject(decode(token))

        if (validServiceUser(claims)) {
            LOG.info("Valid service user token")
            claims.put(CLIENT_ORGANISATION_NUMBER, "00000000")
            return token
        }

        return bearerTokenResolver
                .resolve(request)?.takeIf {
                    validateClaims(
                            getClaims(it),
                            request.getParameter("tpnr").substringBefore('?')
                    )
                }
    }

    private fun validServiceUser(claims: JSONObject): Boolean {
        try {
            LOG.info("Checking sub: ${claims.get("sub")}")
            return claims.get("sub") == "srvpensjon"
        } catch (e: JSONException) {
            LOG.info("Failed to validate sub claim")
            return false
        }
    }

    private fun validateClaims(claims: JSONObject, tpnr: String) =
            claims.validScope().and(claims.validOrganisation(tpnr)).also {
                log(if (it) "Valid" else "Invalid", tpnr, claims)
                if (!it) LOG.info("Invalid token")
            }

    private fun JSONObject.validOrganisation(tpnr: String) =
            tpRegisteretConsumer.validateOrganisation(
                    getString(CLIENT_ORGANISATION_NUMBER),
                    tpnr
            )!!


    companion object {

        private val LOG = LoggerFactory.getLogger(TokenResolver::class.java)
        private const val REQUIRED_SCOPE = "nav:pensjon/v1/samordning"

        private val REQUIRED_CLAIM_KEYS = listOf(
                CLIENT_ID,
                CLIENT_ORGANISATION_NUMBER,
                ISSUER,
                SCOPE)

        private fun getClaims(token: String) = JSONObject(decode(token))
                .also(::checkThatRequiredParametersAreProvided)

        private fun decode(token: String) = String(
                Base64.getUrlDecoder().decode(
                        JWT.decode(token).payload
                ))

        private fun checkThatRequiredParametersAreProvided(claims: JSONObject) = REQUIRED_CLAIM_KEYS
                .filterNot(claims::has)
                .joinToString(", ")
                .takeUnless(String::isEmpty)
                ?.let { throw tokenError("Missing parameters: $it") }
                ?: LOG.logClaims(claims)

        private fun Logger.logClaims(claims: JSONObject) {
            info("Client_ID: ${claims[CLIENT_ID]}")
            info("ORNGNR: ${claims[CLIENT_ORGANISATION_NUMBER]}")
            info("SCOPE: ${claims[SCOPE]}")
            info("ISSUER: ${claims[ISSUER]}")
        }

        private fun JSONObject.validScope() = getString(SCOPE).let { scope ->
            (scope == REQUIRED_SCOPE)
                    .also { if (!it) LOG.info("Invalid scope: $scope") }
        }

        private fun tokenError(message: String) = OAuth2AuthenticationException(
                BearerTokenError(INVALID_REQUEST, BAD_REQUEST, message,
                        "https://tools.ietf.org/html/rfc6750#section-3.1"
                ))

        private fun log(status: String, tpnr: String, claims: JSONObject) =
                LOG.info("$status tpnr $tpnr for client_id ${claims[CLIENT_ID]}")
    }
}
