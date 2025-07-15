package com.liferay.oauth.bff.token.cache;

/**
 * @author Marcel Tanuri
 */
public class CachedToken {

	public CachedToken(
		String accessToken, String refreshToken, String scope, long expiresAt,
		boolean reuseEnabled) {

		_accessToken = accessToken;
		_refreshToken = refreshToken;
		_scope = scope;
		_expiresAt = expiresAt;
		_reuseEnabled = reuseEnabled;
	}

	public String getAccessToken() {
		return _accessToken;
	}

	public long getExpiresAt() {
		return _expiresAt;
	}

	public String getRefreshToken() {
		return _refreshToken;
	}

	public String getScope() {
		return _scope;
	}

	public boolean isExpired() {
		long bufferMillis = 30 * 1000; // 30-second buffer

		if (System.currentTimeMillis() > (_expiresAt - bufferMillis)) {
			return true;
		}

		return false;
	}

	public boolean isReuseEnabled() {
		return _reuseEnabled;
	}

	private final String _accessToken;
	private final long _expiresAt;
	private final String _refreshToken;
	private final boolean _reuseEnabled;
	private final String _scope;

}