package no.nav.samordning.hendelser;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import org.jetbrains.annotations.NotNull;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class TestTokenHelper {

    private static final String DEFAULT_SCOPE = "nav:pensjon/v1/samordning";
    private static KeyPair keyPair;

    public static void init() throws NoSuchAlgorithmException {
        if (keyPair == null) {
            keyPair = generateKeyPair();
        }
    }

    public static String token(String orgno, boolean verifiedSignature) throws NoSuchAlgorithmException {
        return token(DEFAULT_SCOPE, orgno, verifiedSignature);
    }

    public static String token(String scope, String orgno, boolean verifiedSignature) throws NoSuchAlgorithmException {
        var signingKeys = verifiedSignature ? keyPair : generateKeyPair();
        var algorithm = Algorithm.RSA256(publicKey(signingKeys), privateKey(signingKeys));
        return createJwt(scope, orgno, algorithm);
    }

    public static String serviceToken() {
        return createServiceJwt(algorithm());
    }

    static String emptyToken() {
        return JWT.create().sign(algorithm());
    }

    static String generateJwks() throws NoSuchAlgorithmException {
        init();

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

    static String createExpiredJwt(String orgno) {
        return addClaims(expiredJwt(), orgno, algorithm());
    }

    static String createFutureJwt(String orgno) {
        return addClaims(futureJwt(), orgno, algorithm());
    }

    private static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        var generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(1024);
        return generator.generateKeyPair();
    }

    private static String createJwt(String scope, String orgno, Algorithm algorithm) {
        return addClaims(JWT.create(), scope, orgno, algorithm);
    }

    private static String addClaims(JWTCreator.Builder jwt, String orgno, Algorithm algorithm) {
        return addClaims(jwt, DEFAULT_SCOPE, orgno, algorithm);
    }

    private static String addClaims(JWTCreator.Builder jwt, String scope, String orgno, Algorithm algorithm) {
        return jwt
                .withArrayClaim("aud", new String[]{"tp_ordning", "preprod"})
                .withClaim("iss", "https://badserver/provider/")
                .withClaim("scope", scope)
                .withClaim("client_id", "tp_ordning")
                .withClaim("client_orgno", orgno)
                .sign(algorithm);
    }

    private static JWTCreator.Builder expiredJwt() {
        return JWT.create().withExpiresAt(new GregorianCalendar(2018, Calendar.DECEMBER, 31).getTime());
    }

    private static JWTCreator.Builder futureJwt() {
        return JWT.create().withNotBefore(new GregorianCalendar(2101, Calendar.JANUARY, 1).getTime());
    }

    private static String createServiceJwt(Algorithm algorithm) {
        return JWT.create()
                .withClaim("azp", "srvTest")
                .withClaim("iss", "test")
                .withClaim("client_orgno", "00000000")
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
}
