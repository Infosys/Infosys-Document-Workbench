/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.security;

import java.util.List;

public class IdmsConfigData {
	private IdmsConfig idmsConfig;

	public static class IdmsConfig {
		private List<TenantConfigData> tenantConfig;
		private boolean ldapAuthEnabled;
		private LdapConfigData ldapConfig;

		public List<TenantConfigData> getTenantConfig() {
			return tenantConfig;
		}

		public void setTenantConfig(List<TenantConfigData> tenantConfig) {
			this.tenantConfig = tenantConfig;
		}

		public boolean isLdapAuthEnabled() {
			return ldapAuthEnabled;
		}

		public void setLdapAuthEnabled(boolean ldapAuthEnabled) {
			this.ldapAuthEnabled = ldapAuthEnabled;
		}

		public LdapConfigData getLdapConfig() {
			return ldapConfig;
		}

		public void setLdapConfig(LdapConfigData ldapConfig) {
			this.ldapConfig = ldapConfig;
		}
	}

	public static class LdapConfigData {
		private String providerUrl;
		private String userDomainPrefix;
		private String baseDn;
		private String searchFilter;
		private DocwbToLdapMapping docwbToLdapMapping;   

		public String getProviderUrl() {
			return providerUrl;
		}

		public void setProviderUrl(String providerUrl) {
			this.providerUrl = providerUrl;
		}

		public String getUserDomainPrefix() {
			return userDomainPrefix;
		}

		public void setUserDomainPrefix(String userDomainPrefix) {
			this.userDomainPrefix = userDomainPrefix;
		}

		public String getBaseDn() {
			return baseDn;
		}

		public void setBaseDn(String baseDn) {
			this.baseDn = baseDn;
		}

		public String getSearchFilter() {
			return searchFilter;
		}

		public void setSearchFilter(String searchFilter) {
			this.searchFilter = searchFilter;
		}

		public DocwbToLdapMapping getDocwbToLdapMapping() {
			return docwbToLdapMapping;
		}

		public void setDocwbToLdapMapping(DocwbToLdapMapping docwbToLdapMapping) {
			this.docwbToLdapMapping = docwbToLdapMapping;
		}
	}
	
	public static class DocwbToLdapMapping {
		private String fullName;
		private String email;
		public String getFullName() {
			return fullName;
		}
		public void setFullName(String fullName) {
			this.fullName = fullName;
		}
		public String getEmail() {
			return email;
		}
		public void setEmail(String email) {
			this.email = email;
		}
		
	}
	public static class TenantConfigData {
		private List<String> tenantIds;
		private List<String> dbAuthentication;
		private List<String> serviceAccountNames;
		private NewUserAccountSettingData newUserAccountSetting;

		public List<String> getServiceAccountNames() {
			return serviceAccountNames;
		}

		public void setServiceAccountNames(List<String> serviceAccountNames) {
			this.serviceAccountNames = serviceAccountNames;
		}

		public List<String> getTenantIds() {
			return tenantIds;
		}

		public void setTenantIds(List<String> tenantIds) {
			this.tenantIds = tenantIds;
		}

		public List<String> getDbAuthentication() {
			return dbAuthentication;
		}

		public void setDbAuthentication(List<String> dbAuthentication) {
			this.dbAuthentication = dbAuthentication;
		}

		public NewUserAccountSettingData getNewUserAccountSetting() {
			return newUserAccountSetting;
		}

		public void setNewUserAccountSetting(NewUserAccountSettingData newUserAccountSetting) {
			this.newUserAccountSetting = newUserAccountSetting;
		}
	}

	public static class NewUserAccountSettingData {
		private boolean activateAccountEnabled;
		private AddRoleData addRole;
		private AddQueueData addQueue;

		public boolean isActivateAccountEnabled() {
			return activateAccountEnabled;
		}

		public void setActivateAccountEnabled(boolean activateAccountEnabled) {
			this.activateAccountEnabled = activateAccountEnabled;
		}

		public AddRoleData getAddRole() {
			return addRole;
		}

		public void setAddRole(AddRoleData addRole) {
			this.addRole = addRole;
		}

		public AddQueueData getAddQueue() {
			return addQueue;
		}

		public void setAddQueue(AddQueueData addQueue) {
			this.addQueue = addQueue;
		}
	}

	public static class AddRoleData {
		private boolean enabled;
		private int roleTypeCde;

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public int getRoleTypeCde() {
			return roleTypeCde;
		}

		public void setRoleTypeCde(int roleTypeCde) {
			this.roleTypeCde = roleTypeCde;
		}
	}

	public static class AddQueueData {
		private boolean enabled;
		private int queueNameCde;

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public int getQueueNameCde() {
			return queueNameCde;
		}

		public void setQueueNameCde(int queueNameCde) {
			this.queueNameCde = queueNameCde;
		}
	}

	public IdmsConfig getIdmsConfig() {
		return idmsConfig;
	}

	public void setIdmsConfig(IdmsConfig idmsConfig) {
		this.idmsConfig = idmsConfig;
	}

}
