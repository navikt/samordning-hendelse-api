package no.nav.samordning.hendelser.security;

import com.auth0.jwt.JWT;
import no.nav.samordning.hendelser.consumer.TpregisteretConsumer;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.server.resource.BearerTokenError;
import org.springframework.security.oauth2.server.resource.BearerTokenErrorCodes;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class TokenResolver implements BearerTokenResolver {

    private static final Logger LOG = LoggerFactory.getLogger(TokenResolver.class);
    private static final String REQUIRED_SCOPE = "nav:pensjon/v1/samordning";

    private static final List<String> REQUIRED_CLAIM_KEYS = List.of(
            ClaimKeys.CLIENT_ID,
            ClaimKeys.CLIENT_ORGANISATION_NUMBER,
            ClaimKeys.ISSUER,
            ClaimKeys.SCOPE);

    private final BearerTokenResolver bearerTokenResolver;
    private final TpregisteretConsumer tpRegisteretConsumer;
    private final String serviceUser;
    private final String serviceUserIssuer;

    public TokenResolver(
            BearerTokenResolver bearerTokenResolver,
            TpregisteretConsumer tpRegisteretConsumer,
            String serviceUser,
            String serviceUserIssuer) {
        this.bearerTokenResolver = bearerTokenResolver;
        this.tpRegisteretConsumer = tpRegisteretConsumer;
        this.serviceUser = serviceUser;
        this.serviceUserIssuer = serviceUserIssuer;
    }

    @Override
    public String resolve(HttpServletRequest request) {
        var token = bearerTokenResolver.resolve(request);

        if (token == null) {
            return null;
        }

        var claims = getClaims(token);
        var tpnr = request.getParameter("tpnr").split("\\?")[0];

        if (validServiceUser(claims)) {
            LOG.info("Valid service user token");
            claims.put(ClaimKeys.CLIENT_ORGANISATION_NUMBER, "00000000");
            if (validOrganisation(tpnr, claims)) {
                LOG.info("Valid orgnr mapping");
            } else {
                LOG.info("Invalid orgnr mapping");
            }
            return token;
        }

        checkThatRequiredParametersAreProvided(claims);

        if (validScope(claims) && validOrganisation(tpnr, claims)) {
            log("Valid", tpnr, claims);
            return token;
        }

        log("Invalid", tpnr, claims);
        LOG.info("Invalid token");
        return null;
    }

    private void checkThatRequiredParametersAreProvided(Map<String, Object> claims) {
        String missingKeys = REQUIRED_CLAIM_KEYS
                .stream()
                .filter(key -> !claims.containsKey(key))
                .collect(Collectors.joining(", "));

        if (missingKeys.length() > 0) {
            throw tokenError("Missing parameters: " + missingKeys);
        } else {
            LOG.info("Client_ID: " + claims.get(ClaimKeys.CLIENT_ID).toString());
            LOG.info("ORNGNR: " + claims.get(ClaimKeys.CLIENT_ORGANISATION_NUMBER).toString());
            LOG.info("SCOPE: " + claims.get(ClaimKeys.SCOPE).toString());
            LOG.info("ISSUER: " + claims.get(ClaimKeys.ISSUER).toString());
        }
    }

    private boolean validOrganisation(String tpnr, Map<String, Object> claims) {
        String organisationNumber = claims.get(ClaimKeys.CLIENT_ORGANISATION_NUMBER).toString();
        return tpRegisteretConsumer.validateOrganisation(organisationNumber, tpnr);
    }

    private boolean validScope(Map<String, Object> claims) {
        var validScope = REQUIRED_SCOPE.equals(claims.get(ClaimKeys.SCOPE).toString());
        if (!validScope) {
            LOG.info("Invalid scope: " + claims.get(ClaimKeys.SCOPE).toString());
        }
        return validScope;
    }

    private boolean validServiceUser(Map<String, Object> claims) {
        return claims.containsKey(ClaimKeys.AUTHORIZED_PARTY)
                && claims.containsKey(ClaimKeys.ISSUER)
                && claims.get(ClaimKeys.AUTHORIZED_PARTY).toString().equals(serviceUser)
                && claims.get(ClaimKeys.ISSUER).toString().equals(serviceUserIssuer);
    }

    private static Map<String, Object> getClaims(String token) {
        var base64Payload = JWT.decode(token).getPayload();
        var payload = new String(Base64.getUrlDecoder().decode(base64Payload), StandardCharsets.UTF_8);
        return asMap(new JSONObject(payload));
    }

    private static Map<String, Object> asMap(JSONObject json) {
        var map = new HashMap<String, Object>();
        var keys = json.keys();

        while (keys.hasNext()) {
            var key = keys.next();
            map.put(key, json.get(key));
        }

        return map;
    }

    private static OAuth2AuthenticationException tokenError(String message) {
        var error = new BearerTokenError(BearerTokenErrorCodes.INVALID_REQUEST, HttpStatus.BAD_REQUEST, message,
                "https://tools.ietf.org/html/rfc6750#section-3.1");
        return new OAuth2AuthenticationException(error);
    }

    private static void log(String status, String tpnr, Map<String, Object> claims) {
        LOG.info(String.format("%s tpnr %s for client_id %s", status, tpnr, claims.get(ClaimKeys.CLIENT_ID)));
    }

    private class ClaimKeys {
        static final String AUTHORIZED_PARTY = "azp";
        static final String CLIENT_ID = "client_id";
        static final String CLIENT_ORGANISATION_NUMBER = "client_orgno";
        static final String ISSUER = "iss";
        static final String SCOPE = "scope";
    }
}
