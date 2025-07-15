package com.liferay.oauthbff.controller;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.osgi.service.component.annotations.Component;

/**
 * @author Marcel Tanuri
 */
@ApplicationPath("/oauth-bff")
@Component(
	property = {
		"osgi.jaxrs.application.base=/oauth-bff",
		"osgi.jaxrs.name=OAuthBff.Rest", "liferay.auth.verifier=true",
		"liferay.oauth2=false"
	},
	service = Application.class
)
public class OAuthBffApplication extends Application {

	@Override
	public Set<Class<?>> getClasses() {
		Set<Class<?>> classes = new HashSet<>();

		classes.add(OAuthProxyController.class);

		return classes;
	}

}