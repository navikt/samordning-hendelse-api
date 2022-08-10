package no.nav.samordning.hendelser.security

import no.nav.samordning.hendelser.security.support.MaskinportenJwtAuthenticationConverter
import no.nav.samordning.hendelser.security.support.ServiceTokenServiceJwtAuthenticationConverter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver

@Configuration
class AuthenticationManagerConfig(
    @Value("\${oauth2.maskinporten.issuer}") val maskinportenIssuer: String,
    @Value("\${oauth2.maskinporten.jwkSetUri}") val maskinportenJwkSetUri: String,
    @Value("\${oauth2.sts.issuer}") val stsIssuer: String,
    @Value("\${oauth2.sts.jwkSetUri}") val stsJwkSetUri: String
) {
    @Bean
    fun authenticationManagerResolver() = JwtIssuerAuthenticationManagerResolver(
        listOf(
            JwtAuthenticationManager(
                issuer = maskinportenIssuer,
                jwkSetUri = maskinportenJwkSetUri,
                jwtAuthenticationConverter = MaskinportenJwtAuthenticationConverter()
            ),
            JwtAuthenticationManager(
                issuer = stsIssuer,
                jwkSetUri = stsJwkSetUri,
                jwtAuthenticationConverter = ServiceTokenServiceJwtAuthenticationConverter()
            )
        ).associateBy { it.issuer }::get
    )

    class JwtAuthenticationManager(
        val issuer: String,
        jwkSetUri: String,
        jwtAuthenticationConverter: Converter<Jwt, AbstractAuthenticationToken>,
    ) : AuthenticationManager {
        private val jwtAuthenticationProvider = JwtAuthenticationProvider(NimbusJwtDecoder.withJwkSetUri(jwkSetUri)
            .jwsAlgorithm(SignatureAlgorithm.from("RS256")).build()
            .apply { setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer)) }).apply {
            setJwtAuthenticationConverter(jwtAuthenticationConverter)
        }

        override fun authenticate(authentication: Authentication): Authentication =
            jwtAuthenticationProvider.authenticate(authentication)
    }
}
