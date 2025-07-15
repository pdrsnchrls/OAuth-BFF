package com.liferay.oauth.bff.token.request.model.impl;

import com.liferay.oauth.bff.token.request.model.TokenRequestInput;

/**
 * @author Marcel Tanuri
 */
public class ClientCredentialsInput implements TokenRequestInput {

	public ClientCredentialsInput(
		String clientId, String clientSecret, String tokenEndpoint) {

		_clientId = clientId;
		_clientSecret = clientSecret;
		_tokenEndpoint = tokenEndpoint;
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

	private final String _clientId;
	private final String _clientSecret;
	private final String _tokenEndpoint;

}