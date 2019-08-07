package no.nav.samordning.hendelser;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.jetbrains.annotations.NotNull;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

public class TestTokenHelper {

    private static final String AUTH_SCHEME = "Bearer";
    private static KeyPair keyPair;

    public static String token(String orgno, boolean verifiedSignature) throws NoSuchAlgorithmException {
        var signingKeys = verifiedSignature ? keyPair : generateKeyPair();
        var algorithm = Algorithm.RSA256(publicKey(signingKeys), privateKey(signingKeys));
        return header(createJwt(orgno, algorithm));
    }

    public static String srvToken() {
        return header(createSrvJwt(algorithm()));
    }

    public static String emptyToken() {
        return header(JWT.create().sign(algorithm()));
    }

    static String generateJwks() throws NoSuchAlgorithmException {
        keyPair = generateKeyPair();

        return String.format("{\n" +
                "  \"keys\": [{\n" +
                "      \"kty\": \"RSA\",\n" +
                "      \"e\": \"AQAB\",\n" +
                "      \"use\": \"sig\",\n" +
                "      \"n\": \"%s\"\n" +
                "    }\n" +
                "  ]\n" +
                "}", Base64.getUrlEncoder().encodeToString(getKeyModulus()));
    }

    private static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        var generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(1024);
        return generator.generateKeyPair();
    }

    private static String createJwt(String orgno, Algorithm algorithm) {
        return JWT.create()
                .withArrayClaim("aud", new String[]{"tp_ordning", "preprod"})
                .withClaim("iss", "https://badserver/provider/")
                .withClaim("scope", "nav:samordning/v1/hendelser")
                .withClaim("client_id", "tp_ordning")
                .withClaim("client_orgno", orgno)
                .sign(algorithm);
    }

    private static String createSrvJwt(Algorithm algorithm) {
        return JWT.create()
                .withClaim("azp", "srvTest")
                .withClaim("iss", "test")
                .sign(algorithm);
    }

    @NotNull
    private static Algorithm algorithm() {
        return Algorithm.RSA256(publicKey(keyPair), privateKey(keyPair));
    }

    private static RSAPrivateKey privateKey(KeyPair keyPair) {
        return (RSAPrivateKey) keyPair.getPrivate();
    }

    private static RSAPublicKey publicKey(KeyPair keyPair) {
        return (RSAPublicKey) keyPair.getPublic();
    }

    private static byte[] getKeyModulus() {
        return publicKey(keyPair).getModulus().toByteArray();
    }

    private static String header(String token) {
        return AUTH_SCHEME + " " + token;
    }
}
