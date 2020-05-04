package no.nav.samordning.hendelser.security

import no.nav.samordning.hendelser.consumer.TpregisteretConsumer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver

@Configuration
@EnableWebSecurity
class WebSecurityConfig : WebSecurityConfigurerAdapter() {

    @Value("\${ISSUER_URI}")
    lateinit var issuerUri: String

    override fun configure(web: WebSecurity) {
        web.ignoring().antMatchers(
                "/isAlive",
                "/isReady",
                "/actuator/**")
    }

    @Throws(Exception::class)
    public override fun configure(http: HttpSecurity) {
        var issuerResolver = JwtIssuerAuthenticationManagerResolver(issuerUri)

        http.authorizeRequests()
                .anyRequest().authenticated()
                .and().oauth2ResourceServer().authenticationManagerResolver(issuerResolver)
    }

    @Bean
    fun tokenResolver(tpRegisteretConsumer: TpregisteretConsumer) = TokenResolver(DefaultBearerTokenResolver(), tpRegisteretConsumer)
}
