package no.nav.samordning.hendelser.security

import com.auth0.jwt.JWT
import no.nav.samordning.hendelser.consumer.TpregisteretConsumer
import no.nav.samordning.hendelser.security.TokenResolver.ClaimKeys.CLIENT_ID
import no.nav.samordning.hendelser.security.TokenResolver.ClaimKeys.CLIENT_ORGANISATION_NUMBER
import no.nav.samordning.hendelser.security.TokenResolver.ClaimKeys.ISSUER
import no.nav.samordning.hendelser.security.TokenResolver.ClaimKeys.SCOPE
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.server.resource.BearerTokenError
import org.springframework.security.oauth2.server.resource.BearerTokenErrorCodes.INVALID_REQUEST
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver
import java.nio.charset.StandardCharsets.UTF_8
import java.util.*
import javax.servlet.http.HttpServletRequest

class TokenResolver(
        private val bearerTokenResolver: BearerTokenResolver,
        private val tpRegisteretConsumer: TpregisteretConsumer,
        private val serviceUser: String,
        private val serviceUserIssuer: String) : BearerTokenResolver {

    private object ClaimKeys {
        internal const val AUTHORIZED_PARTY = "azp"
        const val CLIENT_ID = "client_id"
        const val CLIENT_ORGANISATION_NUMBER = "client_orgno"
        const val ISSUER = "iss"
        const val SCOPE = "scope"
    }

    override fun resolve(request: HttpServletRequest): String? {
        val token = bearerTokenResolver.resolve(request) ?: return null

        val claims = getClaims(token)
        val tpnr = request.getParameter("tpnr").split('?').dropLastWhile(String::isEmpty).first()

        if (validServiceUser(claims)) {
            LOG.info("Valid service user token")
            claims[CLIENT_ORGANISATION_NUMBER] = "00000000"
            LOG.info(
                    if (validOrganisation(tpnr, claims)) "Valid orgnr mapping"
                    else "Invalid orgnr mapping"
            )
            return token
        }

        checkThatRequiredParametersAreProvided(claims)

        if (validScope(claims) && validOrganisation(tpnr, claims)) {
            log("Valid", tpnr, claims)
            return token
        }

        log("Invalid", tpnr, claims)
        LOG.info("Invalid token")
        return null
    }

    private fun checkThatRequiredParametersAreProvided(claims: Map<String, Any>) {
        REQUIRED_CLAIM_KEYS
                .filterNot(claims::containsKey)
                .joinToString(", ")
                .takeUnless(String::isEmpty)
                ?.let { throw tokenError("Missing parameters: $it") }

        LOG.info("Client_ID: ${claims[CLIENT_ID]}")
        LOG.info("ORNGNR: ${claims[CLIENT_ORGANISATION_NUMBER]}")
        LOG.info("SCOPE: ${claims[SCOPE]}")
        LOG.info("ISSUER: ${claims[ISSUER]}")
    }

    private fun validOrganisation(tpnr: String, claims: Map<String, Any>): Boolean {
        val organisationNumber = claims[CLIENT_ORGANISATION_NUMBER].toString()
        return tpRegisteretConsumer.validateOrganisation(organisationNumber, tpnr)!!
    }

    private fun validScope(claims: Map<String, Any>): Boolean {
        val validScope = REQUIRED_SCOPE == claims[SCOPE].toString()
        if (!validScope) LOG.info("Invalid scope: " + claims[SCOPE].toString())
        return validScope
    }

    private fun validServiceUser(claims: Map<String, Any>): Boolean {
        return (claims.containsKey(ClaimKeys.AUTHORIZED_PARTY)
                && claims.containsKey(ISSUER)
                && claims[ClaimKeys.AUTHORIZED_PARTY].toString() == serviceUser
                && claims[ISSUER].toString() == serviceUserIssuer)
    }


    companion object {

        private val LOG = LoggerFactory.getLogger(TokenResolver::class.java)
        private const val REQUIRED_SCOPE = "nav:pensjon/v1/samordning"

        private val REQUIRED_CLAIM_KEYS = listOf(
                CLIENT_ID,
                CLIENT_ORGANISATION_NUMBER,
                ISSUER,
                SCOPE)

        private fun getClaims(token: String): MutableMap<String, Any> {
            val base64Payload = JWT.decode(token).payload
            val payload = String(Base64.getUrlDecoder().decode(base64Payload), UTF_8)
            return JSONObject(payload).toMap()
        }

        private fun tokenError(message: String): OAuth2AuthenticationException {
            val error = BearerTokenError(INVALID_REQUEST, BAD_REQUEST, message,
                    "https://tools.ietf.org/html/rfc6750#section-3.1")
            return OAuth2AuthenticationException(error)
        }

        private fun log(status: String, tpnr: String, claims: Map<String, Any>) =
                LOG.info("$status tpnr $tpnr for client_id ${claims[CLIENT_ID]}")
    }
}
