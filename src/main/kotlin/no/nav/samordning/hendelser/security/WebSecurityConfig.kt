package no.nav.samordning.hendelser.security;

import no.nav.samordning.hendelser.consumer.TpregisteretConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers(
                "/isAlive",
                "/isReady",
                "/actuator/**",
                "/v2/api-docs",
                "/swagger-ui.html",
                "/swagger-resources/**",
                "/webjars/**");
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .anyRequest().authenticated()
                .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and().oauth2ResourceServer().jwt();
    }

    @Bean
    public TokenResolver tokenResolver(TpregisteretConsumer tpRegisteretConsumer,
                                       @Value("${service.user}") String serviceUser,
                                       @Value("${service.user.iss}") String serviceUserIssuer) {
        return new TokenResolver(
                new DefaultBearerTokenResolver(),
                tpRegisteretConsumer,
                serviceUser,
                serviceUserIssuer);
    }
}
