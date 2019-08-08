package no.nav.samordning.hendelser.security;

import com.auth0.jwt.JWT;
import no.nav.samordning.hendelser.consumer.TpregisteretConsumer;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.server.resource.BearerTokenError;
import org.springframework.security.oauth2.server.resource.BearerTokenErrorCodes;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TokenResolver implements BearerTokenResolver {

    private static final Logger LOG = LoggerFactory.getLogger(TokenResolver.class);

    private static final List<String> REQUIRED_CLAIM_KEYS = List.of(
            ClaimKeys.CLIENT_ID,
            ClaimKeys.CLIENT_ORGANISATION_NUMBER,
            ClaimKeys.ISSUER,
            ClaimKeys.SCOPE);

    @Autowired
    private TpregisteretConsumer tpregisteretConsumer;

    @Value("${service.user}")
    private String srvUser;

    @Value("${service.user.iss}")
    private String srvUserIss;

    @Override
    public String resolve(HttpServletRequest request) {
        var token = new DefaultBearerTokenResolver().resolve(request);

        if (token == null) {
            return null;
        }

        var claims = getClaims(token);
        var tpnr = request.getParameter("tpnr");

        if (validServerUser(claims)) {
            LOG.info("Valid srvuser token");
            return token;
        }

        for (var key : REQUIRED_CLAIM_KEYS) {
            if (!claims.containsKey(key)) throw tokenError("Missing parameter: " + key);
        }

        if (validOrganisation(tpnr, claims)) {
            log("Validated", tpnr, claims);
            return token;
        }

        log("Unvalid", tpnr, claims);
        LOG.info("Invalid token");
        return null;
    }

    private Boolean validOrganisation(String tpnr, Map<String, Object> claims) {
        String organisationNumber = claims.get(ClaimKeys.CLIENT_ORGANISATION_NUMBER).toString();
        return tpregisteretConsumer.validateOrganisation(organisationNumber, tpnr);
    }

    private boolean validServerUser(Map<String, Object> claims) {
        return claims.containsKey(ClaimKeys.AUTHORIZED_PARTY)
                && claims.containsKey(ClaimKeys.ISSUER)
                && claims.get(ClaimKeys.AUTHORIZED_PARTY).toString().equals(srvUser)
                && claims.get(ClaimKeys.ISSUER).toString().equals(srvUserIss);
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
