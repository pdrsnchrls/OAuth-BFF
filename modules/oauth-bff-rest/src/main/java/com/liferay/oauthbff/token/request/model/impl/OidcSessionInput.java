package com.liferay.oauthbff.token.request.model.impl;

import com.liferay.oauthbff.token.request.model.TokenRequestInput;

/**
 * @author Marcel Tanuri
 */
public class OidcSessionInput implements TokenRequestInput {

	public OidcSessionInput(long userId, String clientId, String wellKnownURI) {
		_userId = userId;
		_clientId = clientId;
		_wellKnownURI = wellKnownURI;
	}

	public String getClientId() {
		return _clientId;
	}

	public long getUserId() {
		return _userId;
	}

	public String getWellKnownURI() {
		return _wellKnownURI;
	}

	private final String _clientId;
	private final long _userId;
	private final String _wellKnownURI;

}