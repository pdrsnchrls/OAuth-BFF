package com.liferay.oauth.bff.resolver.token.impl;

import com.liferay.oauth.bff.resolver.token.TokenResolver;
import com.liferay.oauth.bff.token.request.model.TokenRequestContext;
import com.liferay.oauth.bff.token.request.model.impl.OidcSessionInput;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.security.auth.PrincipalThreadLocal;
import com.liferay.portal.security.sso.openid.connect.persistence.model.OpenIdConnectSession;
import com.liferay.portal.security.sso.openid.connect.persistence.service.OpenIdConnectSessionLocalServiceUtil;

import org.osgi.service.component.annotations.Component;

/**
 * @author Marcel Tanuri
 */
@Component(
	property = "resolver.type=oidc_session", service = TokenResolver.class
)
public class OidcSessionTokenResolver
	implements TokenResolver<OidcSessionInput> {

	@Override
	public String resolve(OidcSessionInput input, TokenRequestContext context) {
		long userId = (input.getUserId() > 0) ? input.getUserId() :
			PrincipalThreadLocal.getUserId();

		OpenIdConnectSession session =
			OpenIdConnectSessionLocalServiceUtil.fetchOpenIdConnectSession(
				userId, input.getWellKnownURI(), input.getClientId());

		if (session == null) {
			_log.warn("OIDC session not found for userId " + userId);

			return null;
		}

		String accessToken = session.getAccessToken();

		_log.debug("OIDC session access token found: " + (accessToken != null));

		return accessToken;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		OidcSessionTokenResolver.class);

}