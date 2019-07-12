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

    private static RSAPrivateKey privateKey;
    private static RSAPublicKey publicKey;

    public static String generateJwks() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(1024);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            privateKey = (RSAPrivateKey) keyPair.getPrivate();
            publicKey = (RSAPublicKey) keyPair.getPublic();
        } catch (NoSuchAlgorithmException e) {
            System.exit(-1);
        }

        return String.format("{\n" +
            "  \"keys\": [{\n" +
            "      \"kty\": \"RSA\",\n" +
            "      \"e\": \"AQAB\",\n" +
            "      \"use\": \"sig\",\n" +
            "      \"n\": \"%s\"\n" +
            "    }\n" +
            "  ]\n" +
            "}", Base64.getUrlEncoder().encodeToString(publicKey.getModulus().toByteArray()));
    }

    public static String getValidAccessToken() {
        var algorithm = Algorithm.RSA256(publicKey, privateKey);
        var builder = JWT.create();
        builder.withClaim("name", "bob");
        builder.withClaim("sub", "hendelser");
        return "Bearer " + builder.sign(algorithm);
    }

    public static String getInvalidAccessToken() {
        return "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.TCYt5XsITJX1CxPCT8yAV-TVkIEq_PbChOMqsLfRoPsnsgw5WEuts01mq-pQy7UJiN5mgRxD-WUcX16dUEMGlv50aqzpqh4Qktb3rk-BuQy72IFLOqV0G_zS245-kronKb78cPN25DGlcTwLtjPAYuNzVBAh4vGHSrQyHUdBBPM";
    }
}
