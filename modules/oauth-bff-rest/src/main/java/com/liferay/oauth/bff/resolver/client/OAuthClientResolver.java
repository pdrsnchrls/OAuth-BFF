package com.liferay.oauth.bff.resolver.client;

import com.liferay.oauth.bff.model.OAuthClient;

/**
 * @author Marcel Tanuri
 */
public interface OAuthClientResolver {

	public OAuthClient resolve(String alias) throws Exception;

}