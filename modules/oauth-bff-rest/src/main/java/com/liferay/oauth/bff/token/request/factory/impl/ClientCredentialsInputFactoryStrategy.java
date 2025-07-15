package com.liferay.oauth.bff.token.request.factory.impl;

import com.liferay.oauth.bff.model.OAuthClient;
import com.liferay.oauth.bff.token.request.factory.TokenRequestInputFactoryStrategy;
import com.liferay.oauth.bff.token.request.model.TokenRequestInput;
import com.liferay.oauth.bff.token.request.model.impl.ClientCredentialsInput;

import org.osgi.service.component.annotations.Component;

/**
 * @author Marcel Tanuri
 */
@Component(service = TokenRequestInputFactoryStrategy.class)
public class ClientCredentialsInputFactoryStrategy
	implements TokenRequestInputFactoryStrategy {

	@Override
	public TokenRequestInput create(OAuthClient client) {
		return new ClientCredentialsInput(
			client.getClientId(), client.getClientSecret(),
			client.getTokenEndpoint());
	}

	@Override
	public boolean supports(String type) {
		return "clientCredentials".equalsIgnoreCase(type);
	}

}