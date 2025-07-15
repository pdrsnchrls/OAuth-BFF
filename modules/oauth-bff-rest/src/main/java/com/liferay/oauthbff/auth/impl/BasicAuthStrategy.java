package com.liferay.oauthbff.auth.impl;

import com.liferay.oauthbff.auth.AuthenticationStrategy;
import com.liferay.oauthbff.model.OAuthClient;
import com.liferay.oauthbff.token.request.model.TokenRequestContext;

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