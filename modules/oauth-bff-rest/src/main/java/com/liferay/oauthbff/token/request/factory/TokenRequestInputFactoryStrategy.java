package com.liferay.oauthbff.token.request.factory;

import com.liferay.oauthbff.model.OAuthClient;
import com.liferay.oauthbff.token.request.model.TokenRequestInput;

/**
 * @author Marcel Tanuri
 */
public interface TokenRequestInputFactoryStrategy {

	public TokenRequestInput create(OAuthClient client);

	public boolean supports(String type);

}