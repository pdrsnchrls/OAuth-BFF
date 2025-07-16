package com.liferay.oauth.bff.installer;

import com.liferay.list.type.model.ListTypeDefinition;
import com.liferay.list.type.service.ListTypeDefinitionLocalService;
import com.liferay.list.type.service.ListTypeEntryLocalService;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectFieldSetting;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.object.service.persistence.ObjectFieldSettingPersistence;
import com.liferay.portal.kernel.cluster.ClusterExecutor;
import com.liferay.portal.kernel.cluster.ClusterNode;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.LocaleUtil;

import java.io.InputStream;

import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Marcel Tanuri
 */
@Component(immediate = true, service = OAuthObjectInstaller.class)
public class OAuthObjectInstaller {

	public static final String CATEGORY_OAUTHBFF = "category.oauthbff";

	public static final String JSON_DEFINITION_PATH = "objects/";

	public static final int OBJECT_FOLDER_ID = 0;

	public static final String PANEL_APP_ORDER = "100";

	public static final String SCOPE = "company";

	public static final String STORAGE_TYPE = "default";

	@Activate
	public void activate() {
		try {
			if (_clusterExecutor.isEnabled() && !_isMasterNode()) {
				return;
			}

			for (Company company : _companyLocalService.getCompanies()) {
				_companyId = company.getCompanyId();

				_userId = _userLocalService.getDefaultUserId(_companyId);

				_installPicklist("oauth_client_type_picklist.json");
				_installPicklist("oauth_owner_type_picklist.json");

				_installObjectDefinition("oauth_client_object_definition.json");
				_installObjectDefinition("oauth_token_object_definition.json");
			}
		}
		catch (Exception exception) {
			throw new RuntimeException(
				"Erro ao instalar objetos OAuth", exception);
		}
	}

	private void _addBasicField(ObjectDefinition def, JSONObject field)
		throws Exception {

		String name = field.getString("name");
		String type = field.getString("type");

		boolean required = false;

		if (field.has("required") && field.getBoolean("required")) {
			required = true;
		}

		List<ObjectFieldSetting> settings = new ArrayList<>();

		// Adiciona automaticamente o setting obrigatório para campos DateTime

		if (Objects.equals(type, "DateTime")) {
			ObjectFieldSetting timeStorageSetting =
				_objectFieldSettingPersistence.create(0L);

			timeStorageSetting.setName("timeStorage");
			timeStorageSetting.setValue("explicit");

			settings.add(timeStorageSetting);
		}

		_objectFieldLocalService.addCustomObjectField(
			null, _userId, 0, def.getObjectDefinitionId(), type, type, false,
			false, null, Map.of(LocaleUtil.US, name), false, name, "false",
			null, required, false, settings);
	}

	private void _addPicklistField(
			ObjectDefinition def, String name, String listERC, boolean required)
		throws Exception {

		_objectFieldLocalService.addCustomObjectField(
			null, _userId,
			_listTypeDefinitionLocalService.
				getListTypeDefinitionByExternalReferenceCode(
					listERC, _companyId
				).getListTypeDefinitionId(),
			def.getObjectDefinitionId(), "Picklist", "String", false, false,
			null, Map.of(LocaleUtil.US, name), false, name, "false", null,
			required, false, List.of());
	}

	private void _installObjectDefinition(String resourcePath)
		throws Exception {

		try (InputStream inputStream = getClass(
			).getClassLoader(
			).getResourceAsStream(
				JSON_DEFINITION_PATH + resourcePath
			)) {

			if (inputStream == null) {
				throw new IllegalArgumentException(
					"Recurso não encontrado: " + JSON_DEFINITION_PATH +
						resourcePath);
			}

			String json = new String(
				inputStream.readAllBytes(), StandardCharsets.UTF_8);

			JSONObject jsonObject = _jsonFactory.createJSONObject(json);

			String name = jsonObject.getString("name");

			ObjectDefinition existing = null;

			try {
				existing = _objectDefinitionLocalService.getObjectDefinition(
					_companyId, "C_" + name);
			}
			catch (PortalException portalException) {

				// log

			}

			if (existing != null) {
				return; // já existe, não tenta instalar de novo
			}

			ObjectDefinition objectDefinition = null;

			try {
				objectDefinition = _objectDefinitionLocalService.addCustomObjectDefinition(
						_userId,
						0,
						"",
						false, // enableComments
						false, // enableFriendlyURLCustomization
						false, // enableIndexSearch
						false, // enableLocalization
						false, // enableObjectEntryDraft
						Map.of(LocaleUtil.US, name),
						name,
						"100",
						"category.oauthbff",
						Map.of(LocaleUtil.US, name + "s"),
						false,
						"company",
						"default",
						List.of()
				);

				JSONArray fields = jsonObject.getJSONArray("objectFields");

				for (int i = 0; i < fields.length(); i++) {
					JSONObject field = fields.getJSONObject(i);

					String filedName = field.getString("name");
					String filedType = field.getString("type");

					boolean required = false;

					if (field.has("required") && field.getBoolean("required")) {
						required = true;
					}

					if (Objects.equals(filedType, "Picklist")) {
						String listERC = field.getString(
							"listTypeDefinitionExternalReferenceCode");

						_addPicklistField(
							objectDefinition, filedName, listERC, required);
					}
					else {
						_addBasicField(objectDefinition, field);
					}
				}

				_objectDefinitionLocalService.publishCustomObjectDefinition(
					_userId, objectDefinition.getObjectDefinitionId());
			}
			catch (Exception exception) {
				if (objectDefinition != null) {
					_objectDefinitionLocalService.deleteObjectDefinition(
						objectDefinition);
				}

				throw new RuntimeException(
					"Erro ao instalar ObjectDefinition " + name + ": " +
						exception.getMessage(),
					exception);
			}
		}
	}

	private void _installPicklist(String resourcePath) throws Exception {
		try (InputStream inputStream = getClass(
			).getClassLoader(
			).getResourceAsStream(
				JSON_DEFINITION_PATH + resourcePath
			)) {

			if (inputStream == null) {
				throw new IllegalArgumentException(
					"Recurso não encontrado: " + JSON_DEFINITION_PATH +
						resourcePath);
			}

			String json = new String(
				inputStream.readAllBytes(), StandardCharsets.UTF_8);

			JSONObject jsonObject = _jsonFactory.createJSONObject(json);

			String externalReferenceCode = jsonObject.getString(
				"externalReferenceCode");

			ListTypeDefinition existing =
				_listTypeDefinitionLocalService.
					fetchListTypeDefinitionByExternalReferenceCode(
						externalReferenceCode, _companyId);

			if (existing != null) {
				return;
			}

			ListTypeDefinition definition =
				_listTypeDefinitionLocalService.addListTypeDefinition(
					externalReferenceCode, _userId, false);

			JSONArray entries = jsonObject.getJSONArray("listTypeEntries");

			for (int i = 0; i < entries.length(); i++) {
				JSONObject entry = entries.getJSONObject(i);

				_listTypeEntryLocalService.addListTypeEntry(
					entry.getString("externalReferenceCode"), _userId,
					definition.getListTypeDefinitionId(),
					entry.getString("key"),
					Map.of(LocaleUtil.US, entry.getString("name")));
			}
		}
	}

	private boolean _isMasterNode() {
		if (!_clusterExecutor.isEnabled()) {
			return true;
		}

		List<ClusterNode> nodes = _clusterExecutor.getClusterNodes();
		ClusterNode localNode = _clusterExecutor.getLocalClusterNode();

		if ((localNode == null) || (nodes == null) || nodes.isEmpty()) {
			return true;
		}

		nodes.sort(
			(a, b) -> a.getClusterNodeId(
			).compareTo(
				b.getClusterNodeId()
			));

		return localNode.equals(nodes.get(0));
	}

	@Reference
	private ClusterExecutor _clusterExecutor;

	private long _companyId;

	@Reference
	private CompanyLocalService _companyLocalService;

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private ListTypeDefinitionLocalService _listTypeDefinitionLocalService;

	@Reference
	private ListTypeEntryLocalService _listTypeEntryLocalService;

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Reference
	private ObjectFieldLocalService _objectFieldLocalService;

	@Reference
	private ObjectFieldSettingPersistence _objectFieldSettingPersistence;

	private long _userId;

	@Reference
	private UserLocalService _userLocalService;

}