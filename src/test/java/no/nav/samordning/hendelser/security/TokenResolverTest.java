package no.nav.samordning.hendelser.security;

import no.nav.samordning.hendelser.TestTokenHelper;
import no.nav.samordning.hendelser.consumer.TpregisteretConsumer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;

import javax.servlet.http.HttpServletRequest;
import java.security.NoSuchAlgorithmException;

import static no.nav.samordning.hendelser.TestTokenHelper.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests TokenResolver in isolation.
 */
class TokenResolverTest {

    private static final String GOOD_ORG_NUMBER = "987654321";
    private TpregisteretConsumer tpRegisteretConsumer;
    private BearerTokenResolver bearerTokenResolver;
    private HttpServletRequest request;

    @BeforeAll
    static void setUpOnce() throws NoSuchAlgorithmException {
        TestTokenHelper.init();
    }

    @BeforeEach
    void setUp() {
        request = mock(HttpServletRequest.class);
        bearerTokenResolver = mock(BearerTokenResolver.class);
        tpRegisteretConsumer = mock(TpregisteretConsumer.class);
        when(request.getParameter("tpnr")).thenReturn("123");
        when(tpRegisteretConsumer.validateOrganisation(GOOD_ORG_NUMBER, "123")).thenReturn(true);
    }

    @Test
    void resolving_good_nonService_token_shall_return_token() throws NoSuchAlgorithmException {
        when(bearerTokenResolver.resolve(any())).thenReturn(pureToken(GOOD_ORG_NUMBER, true));
        var resolver = new TokenResolver(bearerTokenResolver, tpRegisteretConsumer, "non-service", "issuer");

        String result = resolver.resolve(request);

        assertEquals("ey", result.substring(0, 2));
    }

    @Test
    void resolving_good_service_token_shall_return_token() {
        when(bearerTokenResolver.resolve(any())).thenReturn(pureServiceToken());
        var resolver = new TokenResolver(bearerTokenResolver, tpRegisteretConsumer, "srvTest", "test");

        String result = resolver.resolve(request);

        assertEquals("ey", result.substring(0, 2));
    }

    @Test
    void resolving_invalid_scope_token_shall_return_null() throws NoSuchAlgorithmException {
        when(bearerTokenResolver.resolve(any())).thenReturn(pureToken("BAD SCOPE", GOOD_ORG_NUMBER, true));
        var resolver = new TokenResolver(bearerTokenResolver, tpRegisteretConsumer, "srvTest", "test");

        String result = resolver.resolve(request);

        assertNull(result);
    }

    @Test
    void resolving_invalid_org_token_shall_return_null() throws NoSuchAlgorithmException {
        when(bearerTokenResolver.resolve(any())).thenReturn(pureToken("-1", true));
        var resolver = new TokenResolver(bearerTokenResolver, tpRegisteretConsumer, "srvTest", "test");

        String result = resolver.resolve(request);

        assertNull(result);
    }

    @Test
    void resolving_nonService_token_with_missing_claims_shall_give_exception_telling_which_claims() {
        when(bearerTokenResolver.resolve(any())).thenReturn(pureServiceToken());
        var resolver = new TokenResolver(bearerTokenResolver, tpRegisteretConsumer, "non-service", "issuer");

        var exception = assertThrows(OAuth2AuthenticationException.class, () -> resolver.resolve(request));

        assertEquals("Missing parameters: client_id, client_orgno, scope", exception.getMessage());
    }
}
