package com.liferay.oauth.bff.service.impl;

import com.liferay.oauth.bff.auth.AuthenticationStrategy;
import com.liferay.oauth.bff.model.OAuthClient;
import com.liferay.oauth.bff.model.ProxyRequestContext;
import com.liferay.oauth.bff.service.ProxyService;
import com.liferay.oauth.bff.token.request.model.TokenRequestContext;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import java.io.InputStream;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * @author Marcel Tanuri
 */
@Component(service = ProxyService.class)
public class ProxyServiceImpl implements ProxyService {

	@Override
	public Response forward(OAuthClient client, ProxyRequestContext ctx)
		throws Exception {

		String authHeader = _resolveAuthHeader(
			client, new TokenRequestContext());

		URI targetURI = _buildUri(
			client.getBaseURL(), ctx.getPath(), ctx.getQueryString());

		_log.info("Proxying request to: {" + targetURI + "}");

		HttpRequest.BodyPublisher bodyPublisher = _createBodyPublisher(
			ctx.getBody());

		HttpRequest.Builder proxyHttpRequest = HttpRequest.newBuilder(
		).uri(
			targetURI
		).method(
			ctx.getMethod(), bodyPublisher
		).header(
			"Authorization", authHeader
		).header(
			"Accept-Encoding", "gzip, deflate"
		);

		_copyHeaders(ctx, proxyHttpRequest);

		HttpResponse<InputStream> response = _httpClient.send(
			proxyHttpRequest.build(),
			HttpResponse.BodyHandlers.ofInputStream());

		InputStream responseStream = response.body();
		String encoding = response.headers(
		).firstValue(
			"Content-Encoding"
		).orElse(
			""
		);

		if ("gzip".equalsIgnoreCase(encoding)) {
			responseStream = new GZIPInputStream(responseStream);
		}
		else if ("deflate".equalsIgnoreCase(encoding)) {
			responseStream = new InflaterInputStream(responseStream);
		}

		String responseBody = new String(
			responseStream.readAllBytes(), StandardCharsets.UTF_8);

		return Response.status(
			response.statusCode()
		).entity(
			responseBody
		).type(
			response.headers(
			).firstValue(
				"Content-Type"
			).orElse(
				MediaType.APPLICATION_JSON
			)
		).build();
	}

	protected void addAuthenticationStrategy(AuthenticationStrategy strategy) {
		_authenticationStrategies.add(strategy);
	}

	protected void removeAuthenticationStrategy(
		AuthenticationStrategy strategy) {

		_authenticationStrategies.remove(strategy);
	}

	private URI _buildUri(String baseUrl, String path, String query) {
		return URI.create(
			baseUrl + (path.startsWith("/") ? path : "/" + path) +
				((query == null) || query.isBlank() ? "" : "?" + query));
	}

	private void _copyHeaders(
		ProxyRequestContext ctx, HttpRequest.Builder builder) {

		ctx.getHeaders(
		).getRequestHeaders(
		).entrySet(
		).stream(
		).filter(
			entry ->
				!entry.getKey(
				).equalsIgnoreCase(
					"Authorization"
				) &&
				!entry.getKey(
				).equalsIgnoreCase(
					"Host"
				) &&
				!entry.getKey(
				).equalsIgnoreCase(
					"Connection"
				)
		).forEach(
			entry -> entry.getValue(
			).forEach(
				value -> builder.header(entry.getKey(), value)
			)
		);
	}

	private HttpRequest.BodyPublisher _createBodyPublisher(InputStream body)
		throws Exception {

		if (body == null) {
			return HttpRequest.BodyPublishers.noBody();
		}

		byte[] bytes = body.readAllBytes();

		return (bytes.length > 0) ?
			HttpRequest.BodyPublishers.ofByteArray(bytes) :
				HttpRequest.BodyPublishers.noBody();
	}

	private String _resolveAuthHeader(
		OAuthClient client, TokenRequestContext context) {

		return _authenticationStrategies.stream(
		).filter(
			strategy -> strategy.supports(client.getType())
		).findFirst(
		).orElseThrow(
			() -> new IllegalArgumentException(
				"Unsupported auth type: " + client.getType())
		).getAuthorizationHeader(
			client, context
		);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ProxyServiceImpl.class);

	@Reference(
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.DYNAMIC
	)
	private volatile List<AuthenticationStrategy> _authenticationStrategies =
		new ArrayList<>();

	private final HttpClient _httpClient = HttpClient.newHttpClient();

}