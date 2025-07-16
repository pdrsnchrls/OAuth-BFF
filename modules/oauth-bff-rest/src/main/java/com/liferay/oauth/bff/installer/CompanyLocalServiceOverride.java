package com.liferay.oauth.bff.installer;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.CompanyLocalServiceWrapper;
import com.liferay.portal.kernel.service.ServiceWrapper;
import com.liferay.portal.kernel.service.UserLocalService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Charles Pederson
 */
@Component(
	configurationPid = " com.liferay.oauth.bff.installer", immediate = true,
	service = ServiceWrapper.class
)
public class CompanyLocalServiceOverride extends CompanyLocalServiceWrapper {

	public CompanyLocalServiceOverride() {
	}

	@Override
	public Company addCompany(
			Long companyId, String webId, String virtualHostname, String mx,
			int maxUsers, boolean active, boolean addDefaultAdminUser,
			String defaultAdminPassword, String defaultAdminScreenName,
			String defaultAdminEmailAddress, String defaultAdminFirstName,
			String defaultAdminMiddleName, String defaultAdminLastName)
		throws PortalException {

		Company company = super.addCompany(
			companyId, webId, virtualHostname, mx, maxUsers, active,
			addDefaultAdminUser, defaultAdminPassword, defaultAdminScreenName,
			defaultAdminEmailAddress, defaultAdminFirstName,
			defaultAdminMiddleName, defaultAdminLastName);

		companyId = company.getCompanyId();

		long userId = _userLocalService.getDefaultUserId(companyId);

		try {
			_oAuthObjectServiceUtil.installPicklist(
				company, userId, "oauth_client_type_picklist.json");
			_oAuthObjectServiceUtil.installPicklist(
				company, userId, "oauth_owner_type_picklist.json");

			_oAuthObjectServiceUtil.installObjectDefinition(
				company, userId, "oauth_client_object_definition.json");
			_oAuthObjectServiceUtil.installObjectDefinition(
				company, userId, "oauth_token_object_definition.json");
		}
		catch (Exception exception) {
			_log.error(
				"Unable to add OAuth BFF Objects for Company " + companyId,
				exception);
		}

		return company;
	}

	@Reference(unbind = "-")
	private void _serviceSetter(CompanyLocalService companyLocalService) {
		setWrappedService(companyLocalService);
	}

	private final Log _log = LogFactoryUtil.getLog(
		OAuthObjectServiceUtil.class);

	@Reference
	private OAuthObjectServiceUtil _oAuthObjectServiceUtil;

	@Reference
	private UserLocalService _userLocalService;

}