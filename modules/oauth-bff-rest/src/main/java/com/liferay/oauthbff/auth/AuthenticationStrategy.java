package com.liferay.oauthbff.auth;

import com.liferay.oauthbff.model.OAuthClient;
import com.liferay.oauthbff.token.request.model.TokenRequestContext;

/**
 * @author Marcel Tanuri
 */
public interface AuthenticationStrategy {

	public String getAuthorizationHeader(
		OAuthClient client, TokenRequestContext context);

	public boolean supports(String authType);

}