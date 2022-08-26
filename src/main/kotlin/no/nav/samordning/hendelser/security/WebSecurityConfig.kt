package no.nav.samordning.hendelser.security

import no.nav.samordning.hendelser.security.support.ROLE_SAMHANDLER
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManagerResolver
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy.STATELESS
import org.springframework.security.config.web.servlet.invoke
import javax.servlet.http.HttpServletRequest

@Configuration
@EnableWebSecurity
class WebSecurityConfig(
    private val jwtIssuerAuthenticationManagerResolver: AuthenticationManagerResolver<HttpServletRequest>?
) : WebSecurityConfigurerAdapter() {

    public override fun configure(http: HttpSecurity?) {
        http {
            csrf { disable() }
            authorizeRequests {
                authorize("/isAlive", permitAll)
                authorize("/isReady", permitAll)
                authorize("/actuator/**", permitAll)
                authorize(anyRequest, hasRole(ROLE_SAMHANDLER))
            }
            sessionManagement {
                sessionCreationPolicy = STATELESS
            }
            jwtIssuerAuthenticationManagerResolver?.let {
                oauth2ResourceServer {
                    authenticationManagerResolver = jwtIssuerAuthenticationManagerResolver
                }
            }
        }
    }
}
