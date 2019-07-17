package no.nav.samordning.hendelser.security;

import com.auth0.jwt.JWT;
import no.nav.samordning.hendelser.consumer.TpregisteretConsumer;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.server.resource.BearerTokenError;
import org.springframework.security.oauth2.server.resource.BearerTokenErrorCodes;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TokenResolver implements org.springframework.security.oauth2.server.resource.web.BearerTokenResolver {

    @Autowired
    private TpregisteretConsumer tpregisteretConsumer;

    @Override
    public String resolve(HttpServletRequest request) {
        var token = new DefaultBearerTokenResolver().resolve(request);

        if (token == null)
            return null;

        var claims = getClaims(token);
        var tpnr = request.getParameter("tpnr");

        if (claims.get("client_id").toString().equals("srvtjenestepensjon"))
            return token;

        if (tpregisteretConsumer.validateOrganisation(claims.get("client_orgno").toString(), tpnr))
            return token;

        return null;
    }

    private Map<String, Object> getClaims(String token) {
        var decoded = JWT.decode(token);
        var payload = new String(Base64.getUrlDecoder().decode(decoded.getPayload()), StandardCharsets.UTF_8);
        var json = new JSONObject(payload);

        var map = new HashMap<String, Object>();
        var keys = json.keys();

        while (keys.hasNext()) {
            var key = keys.next();
            map.put(key, json.get(key));
        }

        var requiredParams = List.of("client_id", "client_orgno", "iss", "scope");
        for (var param : requiredParams) {
            if (!map.containsKey(param))
                throw tokenError("Missing parameter: " + param);
        }

        return map;
    }

    private OAuth2AuthenticationException tokenError(String message) {
        var error = new BearerTokenError(BearerTokenErrorCodes.INVALID_REQUEST, HttpStatus.BAD_REQUEST, message,
            "https://tools.ietf.org/html/rfc6750#section-3.1");
        return new OAuth2AuthenticationException(error);
    }
}
