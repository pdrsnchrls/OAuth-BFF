package com.liferay.oauth.bff.token.request.factory;

import com.liferay.oauth.bff.model.OAuthClient;
import com.liferay.oauth.bff.token.request.model.TokenRequestInput;

/**
 * @author Marcel Tanuri
 */
public interface TokenRequestInputFactoryStrategy {

	public TokenRequestInput create(OAuthClient client);

	public boolean supports(String type);

}