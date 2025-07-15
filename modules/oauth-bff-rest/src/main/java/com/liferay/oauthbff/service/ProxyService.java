package com.liferay.oauthbff.service;

import com.liferay.oauthbff.model.OAuthClient;
import com.liferay.oauthbff.model.ProxyRequestContext;

import javax.ws.rs.core.Response;

/**
 * @author Marcel Tanuri
 */
public interface ProxyService {

	public Response forward(OAuthClient client, ProxyRequestContext context)
		throws Exception;

}