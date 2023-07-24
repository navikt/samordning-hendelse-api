package no.nav.samordning.hendelser.security

import jakarta.servlet.http.HttpServletRequest
import no.nav.samordning.hendelser.security.support.ROLE_SAMHANDLER
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManagerResolver
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy.STATELESS
import org.springframework.security.web.DefaultSecurityFilterChain

@Configuration
@EnableWebSecurity
class WebSecurityConfig(
    private val jwtIssuerAuthenticationManagerResolver: AuthenticationManagerResolver<HttpServletRequest>?
) {

    @Bean
    fun configure(http: HttpSecurity): DefaultSecurityFilterChain = http.run {
        csrf().disable()
        authorizeHttpRequests()
            .requestMatchers("/isAlive").permitAll()
            .requestMatchers("/isReady").permitAll()
            .requestMatchers("/actuator/**").permitAll()
            .anyRequest().hasRole(ROLE_SAMHANDLER)
        sessionManagement().sessionCreationPolicy(STATELESS)
        jwtIssuerAuthenticationManagerResolver?.let {
            oauth2ResourceServer().authenticationManagerResolver(it)
        }
        build()
    }
}
