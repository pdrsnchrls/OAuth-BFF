package com.liferay.oauth.bff.token.request.factory.impl;

import com.liferay.oauth.bff.model.OAuthClient;
import com.liferay.oauth.bff.token.request.factory.TokenRequestInputFactory;
import com.liferay.oauth.bff.token.request.factory.TokenRequestInputFactoryStrategy;
import com.liferay.oauth.bff.token.request.model.TokenRequestInput;

import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Marcel Tanuri
 */
@Component(service = TokenRequestInputFactory.class)
public class DefaultTokenRequestInputFactory
	implements TokenRequestInputFactory {

	public TokenRequestInput create(OAuthClient client) {
		return _strategies.stream(
		).filter(
			s -> s.supports(client.getType())
		).findFirst(
		).orElseThrow(
			() -> new IllegalArgumentException(
				"Unsupported client type: " + client.getType())
		).create(
			client
		);
	}

	@Reference
	private List<TokenRequestInputFactoryStrategy> _strategies;

}