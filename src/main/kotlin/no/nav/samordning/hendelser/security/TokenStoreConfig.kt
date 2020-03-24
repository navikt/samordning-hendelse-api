package no.nav.samordning.hendelser.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.provider.token.store.jwk.JwkTokenStore

@Configuration
class TokenStoreConfig(@Value("\${JWK_SET_URI}") jwkSetUris: String) {

    @get:Bean
    val jwkTokenStore = JwkTokenStore(jwkSetUris.split("[,\\s]*".toRegex()))

}