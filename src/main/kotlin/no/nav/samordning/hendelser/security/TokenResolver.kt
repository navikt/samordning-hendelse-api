package no.nav.samordning.hendelser.security

import com.auth0.jwt.JWT
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import no.nav.samordning.hendelser.consumer.TpConfigConsumer
import org.slf4j.Logger
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.server.resource.BearerTokenError
import org.springframework.security.oauth2.server.resource.BearerTokenErrorCodes.INVALID_REQUEST
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver
import org.springframework.stereotype.Service
import java.util.*

@Service
class TokenResolver(
    private val tpConfigConsumer: TpConfigConsumer,
    @Value("\${oauth2.acceptAll:#{null}}") private val acceptAll: String?,
    private val mapper: ObjectMapper
) : BearerTokenResolver {
    private val log = getLogger(javaClass)

    internal var bearerTokenResolver: BearerTokenResolver = DefaultBearerTokenResolver()

    init {
        log.info("Granting access to all resources for orgno ${acceptAll ?: "N/A"}.")
    }

    override fun resolve(request: HttpServletRequest) = bearerTokenResolver.resolve(request)?.takeIf {
        validateClaims(
            getClaims(it), request.getParameter("tpnr").substringBefore('?')
        )
    }

    private fun validateClaims(claims: JsonNode, tpnr: String) =
        claims.validScope().and(claims.validOrganisation(tpnr)).also {
            log(if (it) "Valid" else "Invalid", tpnr, claims)
            if (!it) log.info("Invalid token")
        }

    private fun JsonNode.validOrganisation(tpnr: String) = orgNo == acceptAll || tpConfigConsumer.validateOrganisation(
        orgNo, tpnr
    )!!

    private fun getClaims(token: String) =
        mapper.readTree(decode(token)).also(::checkThatRequiredParametersAreProvided)

    private fun decode(token: String) = String(
        Base64.getUrlDecoder().decode(
            JWT.decode(token).payload
        )
    )

    private fun checkThatRequiredParametersAreProvided(claims: JsonNode) {
        REQUIRED_CLAIM_KEYS.filterNot(claims::has).joinToString(", ").takeUnless(String::isEmpty)
            ?.let { throw tokenError("Missing parameters: $it") } ?: log.logClaims(claims)
    }

    private fun Logger.logClaims(claims: JsonNode) {
        info("Client_ID: ${claims[CLIENT_ID].asText()}")
        info("ORNGNR: ${claims.orgNo}")
        info("SCOPE: ${claims[SCOPE].asText()}")
        info("ISSUER: ${claims[ISSUER].asText()}")
    }

    private fun JsonNode.validScope() = this[SCOPE].asText().split(' ').let {
        if (REQUIRED_SCOPE in it) true
        else {
            log.info("Invalid scope: $it")
            false
        }
    }

    private val JsonNode.orgNo: String
        get() = stsOrgNo ?: supplier ?: consumer ?: ""

    private val JsonNode.stsOrgNo: String?
        get() = get(CLIENT_ORGANISATION_NUMBER)?.takeUnless { it.isMissingNode }?.asText()

    private val JsonNode.supplier: String?
        get() = get(SUPPLIER)?.takeUnless { it.isMissingNode }?.get(ID)?.takeUnless { it.isMissingNode }?.asText()
            ?.substringAfterLast(':')

    private val JsonNode.consumer: String?
        get() = get(CONSUMER)?.takeUnless { it.isMissingNode }?.get(ID)?.takeUnless { it.isMissingNode }?.asText()
            ?.substringAfterLast(':')

    private fun tokenError(message: String) = OAuth2AuthenticationException(
        BearerTokenError(
            INVALID_REQUEST, BAD_REQUEST, message, "https://tools.ietf.org/html/rfc6750#section-3.1"
        )
    )

    private fun log(status: String, tpnr: String, claims: JsonNode) =
        log.info("$status tpnr $tpnr for client_id ${claims[CLIENT_ID].asText()}")

    companion object {
        private const val REQUIRED_SCOPE = "nav:pensjon/v1/samordning"
        private const val CLIENT_ID = "client_id"
        private const val CLIENT_ORGANISATION_NUMBER = "client_orgno"
        private const val CONSUMER = "consumer"
        private const val SUPPLIER = "supplier"
        private const val ID = "ID"
        private const val ISSUER = "iss"
        private const val SCOPE = "scope"

        private val REQUIRED_CLAIM_KEYS = listOf(
            CLIENT_ID, ISSUER, SCOPE
        )
    }
}
