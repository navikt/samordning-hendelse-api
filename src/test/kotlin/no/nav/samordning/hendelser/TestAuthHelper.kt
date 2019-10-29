package no.nav.samordning.hendelser;

import java.security.NoSuchAlgorithmException;

import static no.nav.samordning.hendelser.TestTokenHelper.*;

public class TestAuthHelper {

    private static final String AUTH_SCHEME = "Bearer";

    public static String token(String orgno, boolean verifiedSignature) throws NoSuchAlgorithmException {
        return header(TestTokenHelper.token(orgno, verifiedSignature));
    }

    public static String expiredToken(String orgno) {
        return header(createExpiredJwt(orgno));
    }

    public static String futureToken(String orgno) {
        return header(createFutureJwt(orgno));
    }

    public static String serviceToken() {
        return header(TestTokenHelper.serviceToken());
    }

    public static String emptyToken() {
        return header(TestTokenHelper.emptyToken());
    }

    private static String header(String token) {
        return AUTH_SCHEME + " " + token;
    }
}
