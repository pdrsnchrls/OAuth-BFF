package com.liferay.oauth.bff.service;

import com.liferay.oauth.bff.model.OAuthClient;
import com.liferay.oauth.bff.model.ProxyRequestContext;

import javax.ws.rs.core.Response;

/**
 * @author Marcel Tanuri
 */
public interface ProxyService {

	public Response forward(OAuthClient client, ProxyRequestContext context)
		throws Exception;

}