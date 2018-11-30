package no.nav.samordning.hendelser.security;

import no.nav.samordning.hendelser.security.jwt.JwtAuthenticationEntryPoint;
import no.nav.samordning.hendelser.security.jwt.JwtAuthenticationFilter;
import no.nav.samordning.hendelser.security.jwt.JwtAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final JwtAuthenticationFilter jwtAuthFilter;

    private final JwtAuthenticationProvider jwtAuthProvider;

    private final JwtAuthenticationEntryPoint jwtAuthEntryPoint;

    @Autowired
    public WebSecurityConfig(JwtAuthenticationFilter jwtAuthFilter, JwtAuthenticationProvider jwtAuthProvider, JwtAuthenticationEntryPoint jwtAuthEntryPoint) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.jwtAuthProvider = jwtAuthProvider;
        this.jwtAuthEntryPoint = jwtAuthEntryPoint;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable().authorizeRequests()
                .antMatchers("/isAlive", "/isReady", "/actuator/prometheus").permitAll()
                .antMatchers("/hendelser").authenticated()
                .and()
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling()
                .authenticationEntryPoint(jwtAuthEntryPoint)
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(jwtAuthProvider);
    }
}