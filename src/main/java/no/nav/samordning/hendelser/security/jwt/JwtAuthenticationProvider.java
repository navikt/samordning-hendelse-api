package no.nav.samordning.hendelser.security.jwt;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

@Component
public class JwtAuthenticationProvider implements AuthenticationProvider {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkURI;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        try {
            Claims claims = Jwts.parser().setSigningKey(getSigningKey()).parseClaimsJws((String) authentication.getCredentials()).getBody();
            return new JwtAuthenticatedUser(claims.getId());
        } catch (SignatureException | IllegalArgumentException | MalformedJwtException e) {
            throw new BadCredentialsException("Failed to verify token", e);
        } catch (ExpiredJwtException e) {
            throw new CredentialsExpiredException("Expired token", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get signing key", e);
        }
    }

    private PublicKey getSigningKey() throws Exception {
        JWKSet publicKeys = JWKSet.load(new URL(jwkURI));
        JWK jwk = publicKeys.getKeys().get(0);

        RSAKey rsaKey = (RSAKey) jwk;

        var exponent = rsaKey.getPublicExponent().decodeToBigInteger();
        var modulus = rsaKey.getModulus().decodeToBigInteger();

        RSAPublicKeySpec rsaKeySpec = new RSAPublicKeySpec(modulus, exponent);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(rsaKeySpec);


        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKey.getEncoded());
        return keyFactory.generatePublic(publicKeySpec);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthenticationToken.class.equals(authentication);
    }
}
