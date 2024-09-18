package no.nav.samordning.hendelser.security

import jakarta.servlet.http.HttpServletRequest
import no.nav.samordning.hendelser.security.support.ROLE_SAMHANDLER
import no.nav.samordning.hendelser.security.support.SCOPE_PREFIX
import no.nav.samordning.hendelser.security.support.SCOPE_SAMORDNING
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManagerResolver
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy.STATELESS
import org.springframework.security.web.DefaultSecurityFilterChain

@Configuration
@EnableWebSecurity
class WebSecurityConfig {

    @Bean
    fun configure(http: HttpSecurity): DefaultSecurityFilterChain = http.run {
        csrf {
            it.disable()
        }
        authorizeHttpRequests {
            it.requestMatchers("/isAlive").permitAll()
            .requestMatchers("/isReady").permitAll()
            .requestMatchers("/actuator/**").permitAll()
            .anyRequest().hasAuthority(SCOPE_PREFIX + SCOPE_SAMORDNING)
        }
        sessionManagement {
            it.sessionCreationPolicy(STATELESS)
        }
        oauth2ResourceServer {
            it.jwt {
            }
        }
        build()
    }
}
