package com.liferay.oauthbff.resolver.token.impl;

import com.liferay.oauthbff.resolver.token.TokenResolver;
import com.liferay.oauthbff.token.cache.CachedToken;
import com.liferay.oauthbff.token.cache.service.OAuthTokenCacheService;
import com.liferay.oauthbff.token.request.model.TokenRequestContext;
import com.liferay.oauthbff.token.request.model.impl.ClientCredentialsInput;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Marcel Tanuri
 */
@Component(
	property = "resolver.type=client_credentials", service = TokenResolver.class
)
public class ClientCredentialsTokenResolver
	implements TokenResolver<ClientCredentialsInput> {

	@Override
	public String resolve(
		ClientCredentialsInput input, TokenRequestContext context) {

		String providerKey = input.getClientId();
		String ownerType = "application";
		String ownerId = providerKey;

		Optional<CachedToken> cached = _oAuthTokenCacheService.getCachedToken(
			providerKey, ownerType, ownerId);

		if (cached.isPresent() &&
			!cached.get(
			).isExpired()) {

			_log.info("Reusing cached access token");

			return cached.get(
			).getAccessToken();
		}

		_log.info(input.getClientId());
		_log.info(input.getClientSecret());

		try {
			String data =
				"grant_type=client_credentials&client_id=" +
					URLEncoder.encode(input.getClientId(), "UTF-8") +
						"&client_secret=" +
							URLEncoder.encode(input.getClientSecret(), "UTF-8");

			URL url = new URL(input.getTokenEndpoint());

			HttpURLConnection conn = (HttpURLConnection)url.openConnection();

			conn.setRequestMethod("POST");
			conn.setRequestProperty(
				"Content-Type", "application/x-www-form-urlencoded");
			conn.setDoOutput(true);

			try (OutputStream os = conn.getOutputStream()) {
				os.write(data.getBytes());
			}

			int status = conn.getResponseCode();

			BufferedReader reader = new BufferedReader(
				new InputStreamReader(
					(status < 400) ? conn.getInputStream() :
						conn.getErrorStream()));

			StringBuilder responseBuilder = new StringBuilder();

			reader.lines(
			).forEach(
				responseBuilder::append
			);

			String response = responseBuilder.toString();

			JSONObject jsonObject = JSONFactoryUtil.createJSONObject(response);

			String token = jsonObject.getString("access_token");

			if (token != null) {
				long expiresIn = jsonObject.getLong("expires_in", 3600);
				String scope = jsonObject.getString("scope", "");

				long expiresAt =
					System.currentTimeMillis() + (expiresIn * 1000);

				_oAuthTokenCacheService.saveToken(
					providerKey, ownerType, ownerId, token, null, scope,
					expiresAt, true);

				_log.info(
					"Token cached successfully. Token starts with: " +
						token.substring(0, Math.min(token.length(), 25)) +
							"...");
			}
			else {
				_log.warn("Access token not found in response: " + response);
			}

			return token;
		}
		catch (Exception exception) {
			_log.error(
				"Error while resolving client_credentials token", exception);

			return null;
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ClientCredentialsTokenResolver.class);

	@Reference
	private OAuthTokenCacheService _oAuthTokenCacheService;

}