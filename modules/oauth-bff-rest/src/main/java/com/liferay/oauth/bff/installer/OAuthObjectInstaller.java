package com.liferay.oauth.bff.installer;

import com.liferay.portal.kernel.cluster.ClusterExecutor;
import com.liferay.portal.kernel.cluster.ClusterNode;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.UserLocalService;

import java.util.List;

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
				long userId = _userLocalService.getDefaultUserId(
					company.getCompanyId());

				_oAuthObjectServiceUtil.installPicklist(
					company, userId, "oauth_client_type_picklist.json");
				_oAuthObjectServiceUtil.installPicklist(
					company, userId, "oauth_owner_type_picklist.json");

				_oAuthObjectServiceUtil.installObjectDefinition(
					company, userId, "oauth_client_object_definition.json");
				_oAuthObjectServiceUtil.installObjectDefinition(
					company, userId, "oauth_token_object_definition.json");
			}
		}
		catch (Exception exception) {
			throw new RuntimeException(
				"Erro ao instalar objetos OAuth", exception);
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

	@Reference
	private CompanyLocalService _companyLocalService;

	@Reference
	private OAuthObjectServiceUtil _oAuthObjectServiceUtil;

	@Reference
	private UserLocalService _userLocalService;

}