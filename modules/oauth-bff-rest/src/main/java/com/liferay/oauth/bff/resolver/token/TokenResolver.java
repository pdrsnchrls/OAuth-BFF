package com.liferay.oauth.bff.resolver.token;

import com.liferay.oauth.bff.token.request.model.TokenRequestContext;
import com.liferay.oauth.bff.token.request.model.TokenRequestInput;

/**
 * @author Marcel Tanuri
 */
public interface TokenResolver<T extends TokenRequestInput> {

	public String resolve(T input, TokenRequestContext context);

}