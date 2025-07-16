package com.liferay.oauth.bff.controller;

import com.liferay.oauth.bff.model.OAuthClient;
import com.liferay.oauth.bff.model.ProxyRequestContext;
import com.liferay.oauth.bff.resolver.client.OAuthClientResolver;
import com.liferay.oauth.bff.service.ProxyService;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import java.io.InputStream;

import java.util.Objects;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Marcel Tanuri
 */
@Component(
	immediate = true,
	property = {
		"osgi.jaxrs.application.select=(osgi.jaxrs.name=OAuthBff.Rest)",
		"osgi.jaxrs.resource=true"
	},
	service = OAuthProxyController.class
)
@Path("/proxy")
public class OAuthProxyController {

	@DELETE
	@Path("/{alias}/{proxyPath: .+}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response proxyDelete(
		@PathParam(_CLIENT_ALIAS) String alias,
		@PathParam(_PROXY_PATH) String proxyPath,
		@Context HttpServletRequest request, @Context HttpHeaders headers,
		@Context UriInfo uriInfo) {

		return _proxyRequest(
			"DELETE", alias, proxyPath, request, headers, uriInfo, null);
	}

	@GET
	@Path("/{alias}/{proxyPath: .+}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response proxyGet(
		@PathParam(_CLIENT_ALIAS) String alias,
		@PathParam(_PROXY_PATH) String proxyPath,
		@Context HttpServletRequest request, @Context HttpHeaders headers,
		@Context UriInfo uriInfo) {

		return _proxyRequest(
			"GET", alias, proxyPath, request, headers, uriInfo, null);
	}

	@Consumes(MediaType.WILDCARD)
	@PATCH
	@Path("/{alias}/{proxyPath: .+}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response proxyPatch(
		@PathParam(_CLIENT_ALIAS) String alias,
		@PathParam(_PROXY_PATH) String proxyPath,
		@Context HttpServletRequest request, @Context HttpHeaders headers,
		@Context UriInfo uriInfo, InputStream requestBody) {

		return _proxyRequest(
			"PATCH", alias, proxyPath, request, headers, uriInfo, requestBody);
	}

	@Consumes(MediaType.WILDCARD)
	@Path("/{alias}/{proxyPath: .+}")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Response proxyPost(
		@PathParam(_CLIENT_ALIAS) String alias,
		@PathParam(_PROXY_PATH) String proxyPath,
		@Context HttpServletRequest request, @Context HttpHeaders headers,
		@Context UriInfo uriInfo, InputStream requestBody) {

		return _proxyRequest(
			"POST", alias, proxyPath, request, headers, uriInfo, requestBody);
	}

	@Consumes(MediaType.WILDCARD)
	@Path("/{alias}/{proxyPath: .+}")
	@Produces(MediaType.APPLICATION_JSON)
	@PUT
	public Response proxyPut(
		@PathParam(_CLIENT_ALIAS) String alias,
		@PathParam(_PROXY_PATH) String proxyPath,
		@Context HttpServletRequest request, @Context HttpHeaders headers,
		@Context UriInfo uriInfo, InputStream requestBody) {

		return _proxyRequest(
			"PUT", alias, proxyPath, request, headers, uriInfo, requestBody);
	}

	@GET
	@Path("/ready")
	@Produces(MediaType.TEXT_PLAIN)
	public Response readinessCheck() {
		return Response.ok(
			"OAuth BFF is ready"
		).build();
	}

	private String _getQueryString(UriInfo uriInfo) {
		String queryString = uriInfo.getRequestUri(
		).getQuery();

		if ((queryString != null) && queryString.contains("p_auth")) {
			queryString = queryString.replaceAll("(&|^)p_auth=[^&]*", "");
			queryString = queryString.replaceAll("^&", "");
		}

		return queryString;
	}

	private String[] _getSanitizedSegments(String path) {
		String sanitizedPath = path;

		if (sanitizedPath.startsWith("/")) {
			sanitizedPath = sanitizedPath.substring(1);
		}

		if (sanitizedPath.endsWith("/")) {
			sanitizedPath = sanitizedPath.substring(
				0, sanitizedPath.length() - 1);
		}

		if (sanitizedPath.isEmpty()) {
			return new String[0];
		}

		return sanitizedPath.split("/");
	}

	private boolean _isPathAllowed(OAuthClient client, String proxyPath) {
		String normalizedPath =
			proxyPath.startsWith("/") ? proxyPath : "/" + proxyPath;

		return client.getAllowedEndpoints(
		).stream(
		).map(
			endpoint -> endpoint.startsWith("/") ? endpoint : "/" + endpoint
		).anyMatch(
			allowedEndpoint -> {
				if (_inlineVariablePattern.matcher(
						allowedEndpoint
					).find()) {

					return _pathsMatch(allowedEndpoint, normalizedPath);
				}

				if (normalizedPath.equals(allowedEndpoint) ||
					normalizedPath.startsWith(allowedEndpoint + "/")) {

					return true;
				}

				return false;
			}
		);
	}

	private boolean _pathsMatch(String pattern, String path) {
		String[] patternSegments = _getSanitizedSegments(pattern);
		String[] pathSegments = _getSanitizedSegments(path);

		if (patternSegments.length != pathSegments.length) {
			return false;
		}

		for (int i = 0; i < patternSegments.length; i++) {
			String patternSegment = patternSegments[i];
			String pathSegment = pathSegments[i];

			if (_inlineVariablePattern.matcher(
					patternSegment
				).matches()) {

				if (pathSegment.isEmpty()) {
					return false;
				}
			}
			else if (!Objects.equals(patternSegment, pathSegment)) {
				return false;
			}
		}

		return true;
	}

	private Response _proxyRequest(
		String method, String alias, String proxyPath,
		HttpServletRequest request, HttpHeaders headers, UriInfo uriInfo,
		InputStream requestBody) {

		try {
			OAuthClient client = _oAuthClientResolver.resolve(alias);

			if (!_isPathAllowed(client, proxyPath)) {
				_log.warn(
					"Blocked proxy request to disallowed path: " + proxyPath);

				return Response.status(
					Response.Status.FORBIDDEN
				).entity(
					"{\"error\": \"Access to this endpoint is not allowed.\"}"
				).type(
					MediaType.APPLICATION_JSON
				).build();
			}

			ProxyRequestContext ctx = new ProxyRequestContext.Builder(
			).method(
				method
			).path(
				proxyPath
			).queryString(
				_getQueryString(uriInfo)
			).headers(
				headers
			).body(
				requestBody
			).servletRequest(
				request
			).uriInfo(
				uriInfo
			).build();

			return _proxyService.forward(client, ctx);
		}
		catch (Exception exception) {
			_log.error("Error during proxy forwarding", exception);

			return Response.serverError(
			).entity(
				"{\"error\": \"Unexpected error: " + exception.getMessage() +
					"\"}"
			).type(
				MediaType.APPLICATION_JSON
			).build();
		}
	}

	private static final String _CLIENT_ALIAS = "alias";

	private static final String _PROXY_PATH = "proxyPath";

	private static final Log _log = LogFactoryUtil.getLog(
		OAuthProxyController.class);

	private static final Pattern _inlineVariablePattern = Pattern.compile(
		"\\{[^/]+}");

	@Reference
	private OAuthClientResolver _oAuthClientResolver;

	@Reference
	private ProxyService _proxyService;

}