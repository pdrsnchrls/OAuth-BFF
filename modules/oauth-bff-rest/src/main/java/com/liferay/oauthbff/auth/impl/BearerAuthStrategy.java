package com.liferay.oauthbff.auth.impl;

import com.liferay.oauthbff.auth.AuthenticationStrategy;
import com.liferay.oauthbff.model.OAuthClient;
import com.liferay.oauthbff.resolver.token.TokenResolverRegistry;
import com.liferay.oauthbff.token.request.factory.TokenRequestInputFactory;
import com.liferay.oauthbff.token.request.model.TokenRequestContext;
import com.liferay.oauthbff.token.request.model.TokenRequestInput;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Marcel Tanuri
 */
@Component(service = AuthenticationStrategy.class)
public class BearerAuthStrategy implements AuthenticationStrategy {

	@Override
	public String getAuthorizationHeader(
		OAuthClient client, TokenRequestContext context) {

		TokenRequestInput input = _tokenRequestInputFactory.create(client);

		String token = _tokenResolverRegistry.getResolver(
			input
		).resolve(
			input, context
		);

		return "Bearer " + token;
	}

	@Override
	public boolean supports(String authType) {
		if ("clientCredentials".equalsIgnoreCase(authType) ||
			"authorizationCode".equalsIgnoreCase(authType) ||
			"oidcSession".equalsIgnoreCase(authType)) {

			return true;
		}

		return false;
	}

	@Reference
	private TokenRequestInputFactory _tokenRequestInputFactory;

	@Reference
	private TokenResolverRegistry _tokenResolverRegistry;

}