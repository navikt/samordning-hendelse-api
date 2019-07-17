package no.nav.samordning.hendelser;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

public class TestTokenHelper {

    private static KeyPair keyPair;

    public static String generateJwks() throws NoSuchAlgorithmException {
        keyPair = generateKeyPair();
        return String.format("{\n" +
            "  \"keys\": [{\n" +
            "      \"kty\": \"RSA\",\n" +
            "      \"e\": \"AQAB\",\n" +
            "      \"use\": \"sig\",\n" +
            "      \"n\": \"%s\"\n" +
            "    }\n" +
            "  ]\n" +
            "}", Base64.getUrlEncoder().encodeToString(((RSAPublicKey)keyPair.getPublic()).getModulus().toByteArray()));
    }

    public static String token(String orgno, Boolean verifiedSignature) throws NoSuchAlgorithmException {
        var signingKeys = keyPair;

        if (!verifiedSignature) {
            signingKeys = generateKeyPair();
        }

        var algorithm = Algorithm.RSA256((RSAPublicKey) signingKeys.getPublic(), (RSAPrivateKey) signingKeys.getPrivate());
        var builder = JWT.create();
        builder.withClaim("iss", "https://badserver/provider/");
        builder.withClaim("scope", "nav:samordning/v1/hendelser");
        builder.withClaim("client_id", "tp_ordning");
        builder.withClaim("client_orgno", orgno);
        return "Bearer " + builder.sign(algorithm);
    }

    public static String emptyToken() {
        var algorithm = Algorithm.RSA256((RSAPublicKey) keyPair.getPublic(), (RSAPrivateKey) keyPair.getPrivate());
        var builder = JWT.create();
        return "Bearer " + builder.sign(algorithm);
    }

    private static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(1024);
        return keyPairGenerator.generateKeyPair();
    }
}
