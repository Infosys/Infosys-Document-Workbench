/** =============================================================================================================== *
 * Copyright 2022 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


package com.infosys.ainauto.docwb.service.service.idms;

import java.util.Hashtable;

import javax.annotation.PostConstruct;
import javax.json.JsonObject;
import javax.naming.AuthenticationException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infosys.ainauto.commonutils.FileUtility;
import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.model.security.IdmsConfigData;
import com.infosys.ainauto.docwb.service.model.security.IdmsConfigData.DocwbToLdapMapping;
import com.infosys.ainauto.docwb.service.model.security.IdmsConfigData.LdapConfigData;
import com.infosys.ainauto.docwb.service.model.security.IdmsConfigData.TenantConfigData;
import com.infosys.ainauto.docwb.service.model.service.IdmsUserReqData;
import com.infosys.ainauto.docwb.service.model.service.IdmsUserResData;

@Component
public class IdentityManagementSystemService implements IIdentityManagementSystemService{
	private static final Logger LOGGER = LoggerFactory.getLogger(IdentityManagementSystemService.class);
	
	private static final String IDMS_CONFIG = "idmsConfig.json";

	private static IdmsConfigData idmsConfigData = null;

	@PostConstruct
	private void init() {
		JsonObject idmsJsonConfigObj = FileUtility.readJsonAsObject(IDMS_CONFIG);
		try {
			idmsConfigData = new ObjectMapper().readValue(idmsJsonConfigObj.toString(), IdmsConfigData.class);
		} catch (Exception ex) {
			LOGGER.error("Error while reading idms config file", ex);
		}
	}
	
	@Override
	public boolean isLdapAuthEnabled() {
		if (null != idmsConfigData && null != idmsConfigData.getIdmsConfig()) {
			return idmsConfigData.getIdmsConfig().isLdapAuthEnabled();
		}
		return false;
	}
	
	@Override
	public TenantConfigData getTenantConfigData(String tenantId) {
		TenantConfigData tenantConfigData = null;
		if (null != idmsConfigData && null != idmsConfigData.getIdmsConfig()) {
			for (TenantConfigData tenantData : idmsConfigData.getIdmsConfig().getTenantConfig()) {
				if (null != tenantData.getTenantIds() && tenantData.getTenantIds().contains(tenantId)) {
					tenantConfigData = tenantData;
					break;
				}
			}
		}
		return tenantConfigData;
	}

	/**
	 * This method does user authentication and authorization. If authentication
	 * fails then returns null. If authorization fails then returns idmsUserResData
	 * 
	 * @param idmsUserReqData
	 * @return idmsUserResData
	 * @throws WorkbenchException
	 */
	@Override
	public IdmsUserResData getLdapAuthData(IdmsUserReqData idmsUserReqData) throws WorkbenchException {
		IdmsUserResData idmsUserResData = null;
		DirContext dirContext = null;
		Hashtable<String, String> contextEnv = getLdapDirContextConfig(idmsUserReqData);
		if (contextEnv != null) {
			try {
				//Throws Authentication fail error 
				dirContext = new InitialDirContext(contextEnv);
				LOGGER.info("dirContext" + dirContext);
				idmsUserResData = getIdmsUserResData(dirContext, idmsUserReqData);
				if (idmsUserResData == null) {
					throw new WorkbenchException("");
				}
			} catch (AuthenticationException ex) {
				LOGGER.error("Error while LDAP Auth: ", ex);
			} catch (Exception ex) {
				LOGGER.error("Error while LDAP Auth: ", ex);
			}
		}
		return idmsUserResData;
	}
	private DocwbToLdapMapping getDocwbToLdapMappingObj() {
		DocwbToLdapMapping docwbToLdapMappingObj=null;
		if (null != idmsConfigData && null != idmsConfigData.getIdmsConfig()) {
			docwbToLdapMappingObj =idmsConfigData.getIdmsConfig().getLdapConfig().getDocwbToLdapMapping();
			return docwbToLdapMappingObj;
		}
		return docwbToLdapMappingObj ;
	}
	private IdmsUserResData getIdmsUserResData(DirContext dirContext, IdmsUserReqData idmsUserReqData) {
		IdmsUserResData idmsUserResData = new IdmsUserResData();
		if(null != getDocwbToLdapMappingObj()) {
		try {
			String[] reqAtts= {getDocwbToLdapMappingObj().getFullName(),getDocwbToLdapMappingObj().getEmail()};
			SearchControls searchControls = new SearchControls();
			searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			searchControls.setReturningAttributes(reqAtts);
			searchControls.setCountLimit(5);
			String ldapSearchFilter = getLdapConfigData().getSearchFilter();
			ldapSearchFilter=ldapSearchFilter.replace("{sAMAccountName}", idmsUserReqData.getNetid());
			NamingEnumeration<SearchResult> searchResult = dirContext
					.search(getLdapConfigData().getBaseDn(), ldapSearchFilter,
					searchControls);
			while (searchResult.hasMore()) {
				SearchResult result = (SearchResult) searchResult.next();
				Attributes attrs = result.getAttributes();
				idmsUserResData.setFullname(getSearchAttrVal(attrs, getDocwbToLdapMappingObj().getFullName()));
				idmsUserResData.setEmailId(getSearchAttrVal(attrs, getDocwbToLdapMappingObj().getEmail()));
				idmsUserResData.setUserId(idmsUserReqData.getNetid());
				break;
			}
		} catch (Exception ex) {
			LOGGER.error("Error while retrieving ldap into : ", ex);
		} finally {
			closeConnection(dirContext);
		}
	}
		return idmsUserResData;
	}
	
	private String getSearchAttrVal(Attributes attrs, String attrName) {
		Attribute attr = attrs.get(attrName);
		String attrVal=null;
		if(null != attr) {
			try {
				attrVal= attr.get(0).toString();
			} catch (NamingException e) {
				LOGGER.error("Error while retrieving ldap search result for "+attrName+" : ", e);
			}
		}
		return attrVal;
	}

	private Hashtable<String, String> getLdapDirContextConfig(IdmsUserReqData idmsUserReqData) {
		Hashtable<String, String> env = null;
		String username = null;
		if (StringUtility.hasTrimmedValue(idmsUserReqData.getPassword())) {
			try {
				username = getLdapConfigData().getUserDomainPrefix() + "\\" + idmsUserReqData.getNetid();
				// Set up environment for creating initial context
				env = new Hashtable<String, String>(11);
				env.put(javax.naming.Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
				env.put(javax.naming.Context.PROVIDER_URL, getLdapConfigData().getProviderUrl());
				env.put(javax.naming.Context.REFERRAL, "follow");

				// Authenticate
				env.put(javax.naming.Context.SECURITY_AUTHENTICATION, "simple");
				env.put(javax.naming.Context.SECURITY_PRINCIPAL, username);
				env.put(javax.naming.Context.SECURITY_CREDENTIALS, idmsUserReqData.getPassword());
			} catch (Exception e) {
				LOGGER.error("Error while LDAP Dir context config: ", e);
			}
		}
		return env;
	}
	
	private LdapConfigData getLdapConfigData() {
		if (null != idmsConfigData && null != idmsConfigData.getIdmsConfig()) {
			return idmsConfigData.getIdmsConfig().getLdapConfig();
		}
		return null;
	}

	private static void closeConnection(DirContext dirContext) {
		// Close the context when we're done
		try {
			if (dirContext != null)
				dirContext.close();
			LOGGER.info("Connection closed");
		} catch (Exception ex) {
			LOGGER.error("Error while closing connection", ex);
		}
	}

}
