package no.nav.samordning.hendelser.security.jwt;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        var httpServletRequest = (HttpServletRequest) request;
        var authorization = httpServletRequest.getHeader("Authorization");

        if (authorization != null) {
            authorization = authorization.replaceAll("Bearer ", "");
            SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(authorization));
        }

        chain.doFilter(request, response);
    }
}
