package com.liferay.oauth.bff.token.cache.service;

import com.liferay.oauth.bff.token.cache.CachedToken;

import java.util.Optional;

/**
 * @author Marcel Tanuri
 */
public interface OAuthTokenCacheService {

	public Optional<CachedToken> getCachedToken(
		String providerKey, String ownerType, String ownerId);

	public void saveToken(
		String providerKey, String ownerType, String ownerId,
		String accessToken, String refreshToken, String scope, long expiresAt,
		boolean reuseEnabled);

}