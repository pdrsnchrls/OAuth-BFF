package com.liferay.oauthbff.token.request.model;

/**
 * @author Marcel Tanuri
 */
public class TokenRequestContext {

	public TokenRequestContext() {
	}

	public TokenRequestContext(long companyId) {
		_companyId = companyId;
	}

	public long getCompanyId() {
		return _companyId;
	}

	public void setCompanyId(long companyId) {
		_companyId = companyId;
	}

	private long _companyId;

}