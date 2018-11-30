package no.nav.samordning.hendelser.security.jwt;

import io.jsonwebtoken.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

@Component
public class JwtAuthenticationProvider implements AuthenticationProvider {

    private final Resource publicKey = new ClassPathResource("test_id_rsa.pub"); //TODO: Get key form KeyStore

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        try {
            Claims claims = Jwts.parser().setSigningKey(getSigningKey()).parseClaimsJws((String) authentication.getCredentials()).getBody();
            return new JwtAuthenticatedUser(claims.getId());
        } catch (SignatureException | IllegalArgumentException | MalformedJwtException e) {
            throw new BadCredentialsException("Failed to verify JWT", e);
        } catch (ExpiredJwtException e) {
            throw new CredentialsExpiredException("Expired JWT", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get signing key", e);
        }
    }

    private PublicKey getSigningKey() throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        var keyBytes = Files.readAllBytes(publicKey.getFile().toPath());
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(keyBytes);

        return keyFactory.generatePublic(publicKeySpec);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthenticationToken.class.equals(authentication);
    }
}
