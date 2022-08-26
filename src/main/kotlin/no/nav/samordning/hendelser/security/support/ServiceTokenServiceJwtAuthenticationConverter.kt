package no.nav.samordning.hendelser.security.support

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter

class ServiceTokenServiceJwtAuthenticationConverter : JwtAuthenticationConverter() {
    init {
        val jwtGrantedAuthoritiesConverter = JwtGrantedAuthoritiesConverter()
        setJwtGrantedAuthoritiesConverter { jwt ->
            jwtGrantedAuthoritiesConverter.convert(jwt)
                ?.plus(SimpleGrantedAuthority(ROLE_PREFIX + ROLE_SAMHANDLER))?.toSet()
        }
    }
}
