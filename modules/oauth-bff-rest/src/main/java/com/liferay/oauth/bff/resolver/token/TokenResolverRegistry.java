package com.liferay.oauth.bff.resolver.token;

import com.liferay.oauth.bff.token.request.model.TokenRequestInput;

/**
 * @author Marcel Tanuri
 */
public interface TokenResolverRegistry {

	public <T extends TokenRequestInput> TokenResolver<T> getResolver(T input);

}