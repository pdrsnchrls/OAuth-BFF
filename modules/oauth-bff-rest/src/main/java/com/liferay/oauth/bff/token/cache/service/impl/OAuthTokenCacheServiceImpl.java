package com.liferay.oauth.bff.token.cache.service.impl;

import com.liferay.oauth.bff.token.cache.CachedToken;
import com.liferay.oauth.bff.token.cache.service.OAuthTokenCacheService;
import com.liferay.oauth.bff.util.DTOContextUtil;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.rest.dto.v1_0.ObjectEntry;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManager;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManagerRegistry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Marcel Tanuri
 */
@Component(service = OAuthTokenCacheService.class)
public class OAuthTokenCacheServiceImpl implements OAuthTokenCacheService {

	@Override
	public Optional<CachedToken> getCachedToken(
		String providerKey, String ownerType, String ownerId) {

		try {
			long companyId = CompanyThreadLocal.getCompanyId();

			ObjectDefinition objectDefinition =
				_objectDefinitionLocalService.fetchObjectDefinition(
					companyId, _OAUTH_TOKEN);

			if (objectDefinition == null) {
				_log.warn(
					"Object Definition for OAuthToken not found. Cannot " +
						"retrieve cached token.");

				return Optional.empty();
			}

			ObjectEntryManager manager =
				_objectEntryManagerRegistry.getObjectEntryManager(
					objectDefinition.getStorageType());

			String filter = String.format(
				"providerKey eq '%s' and ownerType eq '%s' and ownerId eq '%s'",
				providerKey, ownerType, ownerId);

			Page<ObjectEntry> page = manager.getObjectEntries(
				companyId, objectDefinition, null, null,
				DTOContextUtil.contextWithDefaultUser(companyId), filter,
				Pagination.of(1, 1), null, null);

			List<ObjectEntry> items = (List<ObjectEntry>)page.getItems();

			if (!items.isEmpty()) {
				ObjectEntry entry = items.get(0);

				Map<String, Object> props = entry.getProperties();

				Number expiresAt = (Number)props.get("expiresAt");

				return Optional.of(
					new CachedToken(
						(String)props.get("accessToken"),
						(String)props.get("refreshToken"),
						(String)props.get("scope"), expiresAt.longValue(),
						(Boolean)props.getOrDefault("reuseEnabled", false)));
			}
		}
		catch (Exception exception) {
			_log.error(exception);
		}

		return Optional.empty();
	}

	@Override
	public void saveToken(
		String providerKey, String ownerType, String ownerId,
		String accessToken, String refreshToken, String scope, long expiresAt,
		boolean reuseEnabled) {

		try {
			long companyId = CompanyThreadLocal.getCompanyId();

			ObjectDefinition objectDefinition =
				_objectDefinitionLocalService.fetchObjectDefinition(
					companyId, _OAUTH_TOKEN);

			if (objectDefinition == null) {
				_log.warn(
					"Object Definition for '" + _OAUTH_TOKEN +
						"' not found. Cannot save token.");

				return;
			}

			ObjectEntryManager manager =
				_objectEntryManagerRegistry.getObjectEntryManager(
					objectDefinition.getStorageType());

			Map<String, Object> properties = new HashMap<>();

			properties.put("accessToken", accessToken);
			properties.put("expiresAt", expiresAt);
			properties.put("ownerId", ownerId);
			properties.put("ownerType", ownerType);
			properties.put("providerKey", providerKey);
			properties.put("refreshToken", refreshToken);
			properties.put("reuseEnabled", reuseEnabled);
			properties.put("scope", scope);

			ObjectEntry entry = new ObjectEntry();

			entry.setProperties(properties);

			ObjectEntry entrySaved = manager.addObjectEntry(
				DTOContextUtil.contextWithDefaultUser(companyId),
				objectDefinition, entry, null);

			_log.info(entrySaved.getId());
		}
		catch (Exception exception) {
			_log.info(
				"Error while accessing or persisting OAuth token", exception);
		}
	}

	private static final String _OAUTH_TOKEN = "C_OAuthToken";

	private static final Log _log = LogFactoryUtil.getLog(
		OAuthTokenCacheServiceImpl.class);

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Reference
	private ObjectEntryManagerRegistry _objectEntryManagerRegistry;

}