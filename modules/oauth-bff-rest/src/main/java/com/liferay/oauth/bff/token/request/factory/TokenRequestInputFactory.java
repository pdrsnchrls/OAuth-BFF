package com.liferay.oauth.bff.token.request.factory;

import com.liferay.oauth.bff.model.OAuthClient;
import com.liferay.oauth.bff.token.request.model.TokenRequestInput;

/**
 * @author Marcel Tanuri
 */
public interface TokenRequestInputFactory {

	public TokenRequestInput create(OAuthClient client);

	public class UnauthorizedException extends RuntimeException {

		public UnauthorizedException(String message) {
			super(message);
		}

	}

}