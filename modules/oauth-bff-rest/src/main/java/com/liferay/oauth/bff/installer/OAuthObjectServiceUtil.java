package com.liferay.oauth.bff.installer;

import com.liferay.list.type.model.ListTypeDefinition;
import com.liferay.list.type.service.ListTypeDefinitionLocalService;
import com.liferay.list.type.service.ListTypeEntryLocalService;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectFieldSetting;
import com.liferay.object.model.ObjectFolder;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.object.service.ObjectFolderLocalService;
import com.liferay.object.service.persistence.ObjectFieldSettingPersistence;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.util.LocaleUtil;

import java.io.InputStream;

import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Charles Pederson
 */
@Component(immediate = true, service = OAuthObjectServiceUtil.class)
public class OAuthObjectServiceUtil {

	protected void installObjectDefinition(
			Company company, long userId, String resourcePath)
		throws Exception {

		try (InputStream inputStream = getClass(
			).getClassLoader(
			).getResourceAsStream(
				_JSON_DEFINITION_PATH + resourcePath
			)) {

			if (inputStream == null) {
				throw new IllegalArgumentException(
					"Recurso não encontrado: " + _JSON_DEFINITION_PATH +
						resourcePath);
			}

			String json = new String(
				inputStream.readAllBytes(), StandardCharsets.UTF_8);

			Locale locale = company.getLocale();

			json = _setLocale(locale, json);

			JSONObject jsonObject = _jsonFactory.createJSONObject(json);

			String name = jsonObject.getString("name");

			long companyId = company.getCompanyId();

			ObjectDefinition existing = null;

			try {
				existing = _objectDefinitionLocalService.getObjectDefinition(
					companyId, "C_" + name);
			}
			catch (PortalException portalException) {

				// log

			}

			if (existing != null) {
				return; // já existe, não tenta instalar de novo
			}

			ObjectDefinition objectDefinition = null;

			try {
				ObjectFolder objectFolder =
					_objectFolderLocalService.getOrAddDefaultObjectFolder(
						companyId);

                objectDefinition = _objectDefinitionLocalService.addCustomObjectDefinition(
                        userId,
                        objectFolder.getObjectFolderId(),
                        "",
                        false, // enableComments
                        false, // enableFriendlyURLCustomization
                        false, // enableIndexSearch
                        false, // enableLocalization
                        false, // enableObjectEntryDraft
                        Map.of(LocaleUtil.US, name, locale, name),
                        name,
                        "100",
                        "category.oauthbff",
                        Map.of(LocaleUtil.US, name + "s", locale, name + "s"),
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
							companyId, userId, objectDefinition, filedName,
							listERC, required, locale);
					}
					else {
						_addBasicField(userId, objectDefinition, field, locale);
					}
				}

				_objectDefinitionLocalService.publishCustomObjectDefinition(
					userId, objectDefinition.getObjectDefinitionId());
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

	protected void installPicklist(
			Company company, long userId, String resourcePath)
		throws Exception {

		try (InputStream inputStream = getClass(
			).getClassLoader(
			).getResourceAsStream(
				_JSON_DEFINITION_PATH + resourcePath
			)) {

			if (inputStream == null) {
				throw new IllegalArgumentException(
					"Recurso não encontrado: " + _JSON_DEFINITION_PATH +
						resourcePath);
			}

			String json = new String(
				inputStream.readAllBytes(), StandardCharsets.UTF_8);

			Locale locale = company.getLocale();

			json = _setLocale(locale, json);

			JSONObject jsonObject = _jsonFactory.createJSONObject(json);

			String externalReferenceCode = jsonObject.getString(
				"externalReferenceCode");

			ListTypeDefinition existing =
				_listTypeDefinitionLocalService.
					fetchListTypeDefinitionByExternalReferenceCode(
						externalReferenceCode, company.getCompanyId());

			if (existing != null) {
				return;
			}

			ListTypeDefinition definition =
				_listTypeDefinitionLocalService.addListTypeDefinition(
					externalReferenceCode, userId, false);

			JSONArray entries = jsonObject.getJSONArray("listTypeEntries");

			for (int i = 0; i < entries.length(); i++) {
				JSONObject entry = entries.getJSONObject(i);

				_listTypeEntryLocalService.addListTypeEntry(
					entry.getString("externalReferenceCode"), userId,
					definition.getListTypeDefinitionId(),
					entry.getString("key"),
					Map.of(
						LocaleUtil.US, entry.getString("name"), locale,
						entry.getString("name")));
			}
		}
	}

	private void _addBasicField(
			long userId, ObjectDefinition def, JSONObject field, Locale locale)
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
			null, userId, 0, def.getObjectDefinitionId(), type, type, false,
			false, null, Map.of(LocaleUtil.US, name, locale, name), false, name,
			"false", null, required, false, settings);
	}

	private void _addPicklistField(
			long companyId, long userId, ObjectDefinition def, String name,
			String listERC, boolean required, Locale locale)
		throws Exception {

		_objectFieldLocalService.addCustomObjectField(
			null, userId,
			_listTypeDefinitionLocalService.
				getListTypeDefinitionByExternalReferenceCode(
					listERC, companyId
				).getListTypeDefinitionId(),
			def.getObjectDefinitionId(), "Picklist", "String", false, false,
			null, Map.of(LocaleUtil.US, name, locale, name), false, name,
			"false", null, required, false, List.of());
	}

	private String _setLocale(Locale locale, String json) {
		return json.replace("[$LOCALE_LABEL]", locale.toString());
	}

	private static final String _JSON_DEFINITION_PATH = "objects/";

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

	@Reference
	private ObjectFolderLocalService _objectFolderLocalService;

}