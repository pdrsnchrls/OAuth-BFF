package com.liferay.oauth.bff.auth;

import com.liferay.oauth.bff.model.OAuthClient;
import com.liferay.oauth.bff.token.request.model.TokenRequestContext;

/**
 * @author Marcel Tanuri
 */
public interface AuthenticationStrategy {

	public String getAuthorizationHeader(
		OAuthClient client, TokenRequestContext context);

	public boolean supports(String authType);

}