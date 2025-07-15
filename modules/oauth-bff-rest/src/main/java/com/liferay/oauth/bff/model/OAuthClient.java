package com.liferay.oauth.bff.model;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Marcel Tanuri
 */
public class OAuthClient {

	public OAuthClient(
		String clientId, String clientSecret, String tokenEndpoint,
		String authEndpoint, String type, String baseURL,
		String allowedEndpoints) {

		_clientId = clientId;
		_clientSecret = clientSecret;
		_tokenEndpoint = tokenEndpoint;
		_authEndpoint = authEndpoint;
		_type = type;
		_baseURL = baseURL;
		_allowedEndpoints = allowedEndpoints;
	}

	public List<String> getAllowedEndpoints() {
		if ((_allowedEndpoints == null) || _allowedEndpoints.isBlank()) {
			return List.of();
		}

		return Arrays.stream(
			_allowedEndpoints.split("\\r?\\n")
		).map(
			String::trim
		).filter(
			s -> !s.isEmpty()
		).collect(
			Collectors.toList()
		);
	}

	public String getAllowedEndpointsRaw() {
		return _allowedEndpoints;
	}

	public String getAuthEndpoint() {
		return _authEndpoint;
	}

	public String getBaseURL() {
		return _baseURL;
	}

	public String getClientId() {
		return _clientId;
	}

	public String getClientSecret() {
		return _clientSecret;
	}

	public String getTokenEndpoint() {
		return _tokenEndpoint;
	}

	public String getType() {
		return _type;
	}

	public boolean isEndpointAllowed(String proxyPath) {
		return getAllowedEndpoints(
		).stream(
		).anyMatch(
			proxyPath::startsWith
		);
	}

	private final String _allowedEndpoints;
	private final String _authEndpoint;
	private final String _baseURL;
	private final String _clientId;
	private final String _clientSecret;
	private final String _tokenEndpoint;
	private final String _type;

}