package no.nav.samordning.hendelser.security.support

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter

class MaskinportenJwtAuthenticationConverter : JwtAuthenticationConverter()  {
    init {
        val jwtGrantedAuthoritiesConverter = JwtGrantedAuthoritiesConverter()
        setJwtGrantedAuthoritiesConverter { jwt ->
            val tokenAuthorities = jwtGrantedAuthoritiesConverter.convert(jwt)
            if (tokenAuthorities?.any { it.authority == SCOPE_PREFIX + SCOPE_SAMORDNING } == true) {
                tokenAuthorities + listOf(SimpleGrantedAuthority(ROLE_SAMHANDLER))
            } else {
                tokenAuthorities
            }.toSet()
        }
    }
}
