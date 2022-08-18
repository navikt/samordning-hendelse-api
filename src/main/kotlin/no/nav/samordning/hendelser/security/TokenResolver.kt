package no.nav.samordning.hendelser.security

import com.auth0.jwt.JWT
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.samordning.hendelser.consumer.TpConfigConsumer
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
    private val bearerTokenResolver: BearerTokenResolver, private val tpConfigConsumer: TpConfigConsumer
) : BearerTokenResolver {

    override fun resolve(request: HttpServletRequest) = bearerTokenResolver.resolve(request)?.takeIf {
        validateClaims(
            getClaims(it), request.getParameter("tpnr").substringBefore('?')
        )
    }

    private fun validateClaims(claims: JsonNode, tpnr: String) =
        claims.validScope().and(claims.validOrganisation(tpnr)).also {
            log(if (it) "Valid" else "Invalid", tpnr, claims)
            if (!it) LOG.info("Invalid token")
        }

    private fun JsonNode.validOrganisation(tpnr: String) = tpConfigConsumer.validateOrganisation(
        orgNo, tpnr
    )!!

    companion object {

        private val LOG = LoggerFactory.getLogger(TokenResolver::class.java)
        private const val REQUIRED_SCOPE = "nav:pensjon/v1/samordning"
        private const val CLIENT_ID = "client_id"
        private const val CLIENT_ORGANISATION_NUMBER = "client_orgno"
        private const val CONSUMER = "consumer"
        private const val SUPPLIER = "supplier"
        private const val ID = "ID"
        private const val ISSUER = "iss"
        private const val SCOPE = "scope"
        private val mapper = ObjectMapper()

        private val REQUIRED_CLAIM_KEYS = listOf(
            CLIENT_ID, ISSUER, SCOPE
        )

        private fun getClaims(token: String) =
            mapper.readTree(decode(token)).also(::checkThatRequiredParametersAreProvided)

        private fun decode(token: String) = String(
            Base64.getUrlDecoder().decode(
                JWT.decode(token).payload
            )
        )

        private fun checkThatRequiredParametersAreProvided(claims: JsonNode) =
            REQUIRED_CLAIM_KEYS.filterNot(claims::has).joinToString(", ").takeUnless(String::isEmpty)
                ?.let { throw tokenError("Missing parameters: $it") } ?: LOG.logClaims(claims)

        private fun Logger.logClaims(claims: JsonNode) {
            info("Client_ID: ${claims[CLIENT_ID].asText()}")
            info("ORNGNR: ${claims.orgNo}")
            info("SCOPE: ${claims[SCOPE].asText()}")
            info("ISSUER: ${claims[ISSUER].asText()}")
        }

        private fun JsonNode.validScope() = this[SCOPE].asText().let { scope ->
            (scope == REQUIRED_SCOPE).also { if (!it) LOG.info("Invalid scope: $scope") }
        }

        private val JsonNode.orgNo: String
            get() = stsOrgNo ?: supplier ?: consumer ?: ""

        private val JsonNode.stsOrgNo: String?
            get() = get(CLIENT_ORGANISATION_NUMBER).takeUnless { it.isMissingNode }?.asText()

        private val JsonNode.supplier: String?
            get() = get(SUPPLIER).takeUnless { it.isMissingNode }?.get(ID)?.takeUnless { it.isMissingNode }?.asText()
                ?.substringAfterLast(':')

        private val JsonNode.consumer: String?
            get() = get(CONSUMER).takeUnless { it.isMissingNode }?.get(ID)?.takeUnless { it.isMissingNode }?.asText()
                ?.substringAfterLast(':')

        private fun tokenError(message: String) = OAuth2AuthenticationException(
            BearerTokenError(
                INVALID_REQUEST, BAD_REQUEST, message, "https://tools.ietf.org/html/rfc6750#section-3.1"
            )
        )

        private fun log(status: String, tpnr: String, claims: JsonNode) =
            LOG.info("$status tpnr $tpnr for client_id ${claims[CLIENT_ID].asText()}")
    }
}
