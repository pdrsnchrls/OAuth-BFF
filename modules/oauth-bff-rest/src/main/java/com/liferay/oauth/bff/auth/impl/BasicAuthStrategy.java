package com.liferay.oauth.bff.auth.impl;

import com.liferay.oauth.bff.auth.AuthenticationStrategy;
import com.liferay.oauth.bff.model.OAuthClient;
import com.liferay.oauth.bff.token.request.model.TokenRequestContext;

import java.util.Base64;

import org.osgi.service.component.annotations.Component;

/**
 * @author Marcel Tanuri
 */
@Component(service = AuthenticationStrategy.class)
public class BasicAuthStrategy implements AuthenticationStrategy {

	@Override
	public String getAuthorizationHeader(
		OAuthClient client, TokenRequestContext context) {

		String credentials =
			client.getClientId() + ":" + client.getClientSecret();

		String encodedCredentials = Base64.getEncoder(
		).encodeToString(
			credentials.getBytes()
		);

		return "Basic " + encodedCredentials;
	}

	@Override
	public boolean supports(String authType) {
		return "basicAuth".equalsIgnoreCase(authType);
	}

}