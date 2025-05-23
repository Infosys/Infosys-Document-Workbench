/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.process.auth;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonValue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.FileUtility;
import com.infosys.ainauto.docwb.service.common.SessionHelper;
import com.infosys.ainauto.docwb.service.common.WorkbenchConstants;
import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.model.api.AppVarResData;
import com.infosys.ainauto.docwb.service.model.api.UserFeatureAuthResData;
import com.infosys.ainauto.docwb.service.model.security.ApiRoleAuthData;
import com.infosys.ainauto.docwb.service.process.variable.IVariableProcess;

@Component
public class ApiRoleAuthorizationProcess implements IApiRoleAuthorizationProcess {

	@Autowired
	private IVariableProcess variableProcess;

	@Value("${docwb.service.rbac.map.config}")
	private String APP_RBAC_MAP_CONFIG_FILE;

	@Value("${docwb.service.feature.to.api.map.config}")
	private String APP_FEATURE_TO_API_MAP_CONFIG_FILE;

	@Value("${docwb.service.rbac.enabled.rl.bit.position}")
	private String ACCESS_LEVEL_CDE_ENABLED_RL_BIT_POSITION;

	@Value("${docwb.service.rbac.exclude.api.config}")
	private String APP_RBAC_EXCLUDE_API_CONFIG_FILE;

	private JsonObject rbacExcludeApiConfigJsonObj = null;
	private JsonObject featureToApiConfigJsonObj = null;

	private static Map<String, Date> fileLmdMap = new HashMap<>();
	private static Map<String, JsonObject> rbacConfigJsonData = new HashMap<>();

	@PostConstruct
	private void init() {
		rbacExcludeApiConfigJsonObj = FileUtility.readJsonAsObject(APP_RBAC_EXCLUDE_API_CONFIG_FILE);
		featureToApiConfigJsonObj = FileUtility.readJsonAsObject(APP_FEATURE_TO_API_MAP_CONFIG_FILE);
	}

	/**
	 * This method is to check if an API is authorized for the logged in user. Note:
	 * An API may be comprised of one or many features. If it has ONLY one feature,
	 * then this method can be used for first level validation.
	 */
	@Override
	public boolean isApiAccessAllowed(String api, String apiMethod, boolean isCheckForExcludeOnly)
			throws WorkbenchException {
		boolean isApiAccessEnabled = false;
		boolean isExcludeHttpReq = false;
//		First check in exclude list
		JsonArray listOfExcludeApi = rbacExcludeApiConfigJsonObj.getJsonArray(apiMethod);
		if (listOfExcludeApi != null) {
			for (int i = 0; i < listOfExcludeApi.size(); i++) {
				if (api.matches(listOfExcludeApi.getString(i))) {
					isExcludeHttpReq = true;
					break;
				}
			}
		}
//		If not in exclude list then check in rbac config list
		if (!isCheckForExcludeOnly && !isExcludeHttpReq) {
			// If at least one item has access enabled, consider as permission granted
			for (ApiRoleAuthData apiAuthData : getApiRoleAuthData(api, apiMethod)) {
				if (isAccessLevelCdeHasEnableBit(apiAuthData.getAccessLevelCde())) {
					isApiAccessEnabled = true;
					break;
				}
			}
		}
		return isExcludeHttpReq || isApiAccessEnabled;
	}

	/**
	 * This method is to check if a feature (within an API) is authorized for the
	 * logged in user. Note: An API may be comprised of one or many features. If it
	 * has many features, then this method can be used for second level validation.
	 */
	@Override
	public boolean isFeatureAccessAllowed(String featureId) throws WorkbenchException {
		JsonObject rbacConfigJsonObj = null;
		// rbacConfigJsonObj = getRbacConfigJsonDataFromFile();
		rbacConfigJsonObj = getRbacConfigJsonDataFromDb();
		List<Boolean> isFeatureAuthorizedList = new ArrayList<>();

		if ((rbacConfigJsonObj != null && !rbacConfigJsonObj.isNull("appRoleBasedAccessControlMapping"))
				&& (featureToApiConfigJsonObj != null && !featureToApiConfigJsonObj.isNull("appFeatureToApiMapping"))) {
			JsonArray rbacJsonArr = rbacConfigJsonObj.getJsonArray("appRoleBasedAccessControlMapping");

			List<JsonValue> featureMatchedRbacJsonObjList = rbacJsonArr.stream()
					.filter(rbacJsonObj -> ((JsonObject) rbacJsonObj).getString("featureId").equals(featureId))
					.collect(Collectors.toList());

			featureMatchedRbacJsonObjList.forEach(feaMatRbacJsonObj -> {
				List<JsonValue> roleTypeMatchedPermissionList = ((JsonObject) feaMatRbacJsonObj)
						.getJsonArray("permissions").stream()
						.filter(jsonObj -> ((JsonObject) jsonObj).getJsonNumber("roleTypeCde")
								.longValue() == SessionHelper.getLoginUserData().getRoleTypeCde())
						.collect(Collectors.toList());
				if (roleTypeMatchedPermissionList != null && roleTypeMatchedPermissionList.size() > 0) {
					int accessLevelCde = ((JsonObject) roleTypeMatchedPermissionList.get(0))
							.getJsonNumber("accessLevelCde").intValue();
					if (isAccessLevelCdeHasEnableBit(accessLevelCde)) {
						isFeatureAuthorizedList.add(true);
					}
				}
			});
		}
		if (isFeatureAuthorizedList.size() > 0)
			return true;

		return false;

	}

	/**
	 * This method is to check if a feature (within an API) is authorized for the
	 * logged in user. Note: An API may be comprised of one or many features. If it
	 * has many features, then this method can be used for second level validation.
	 */
	@Override
	public List<Long> getFeatureAllowedRoleTypeCde(String featureId) throws WorkbenchException {
		JsonObject rbacConfigJsonObj = null;
		rbacConfigJsonObj = getRbacConfigJsonDataFromDb();
		List<Long> isFeatureAuthorizedList = new ArrayList<>();

		if ((rbacConfigJsonObj != null && !rbacConfigJsonObj.isNull("appRoleBasedAccessControlMapping"))
				&& (featureToApiConfigJsonObj != null && !featureToApiConfigJsonObj.isNull("appFeatureToApiMapping"))) {
			JsonArray rbacJsonArr = rbacConfigJsonObj.getJsonArray("appRoleBasedAccessControlMapping");

			List<JsonValue> featureMatchedRbacJsonObjList = rbacJsonArr.stream()
					.filter(rbacJsonObj -> ((JsonObject) rbacJsonObj).getString("featureId").equals(featureId))
					.collect(Collectors.toList());
			featureMatchedRbacJsonObjList.forEach(feaMatRbacJsonObj -> {
				JsonArray featrueMapPerList = ((JsonObject) feaMatRbacJsonObj).getJsonArray("permissions");
				featrueMapPerList.stream().forEach(jsonObj -> {
					int accessLevelCde = ((JsonObject)jsonObj).getInt("accessLevelCde");
					if (isAccessLevelCdeHasEnableBit(accessLevelCde)) {
						isFeatureAuthorizedList.add(((JsonObject)jsonObj).getJsonNumber("roleTypeCde").longValue());
					}
				});
			});
		}
		return isFeatureAuthorizedList;
	}

	@Override
	public List<UserFeatureAuthResData> getLoggedInUserRoleFeatureAuthData() throws WorkbenchException {
		List<UserFeatureAuthResData> userFeatureAuthResDataList = new ArrayList<UserFeatureAuthResData>();
		Map<String, Long> featureToAccessCdeMap = new HashMap<String, Long>();
		for (ApiRoleAuthData apiAuthData : getApiRoleAuthData(null, null)) {
			if (!featureToAccessCdeMap.containsKey(apiAuthData.getFeatureId())
					|| featureToAccessCdeMap.get(apiAuthData.getFeatureId()) < apiAuthData.getAccessLevelCde()) {
				featureToAccessCdeMap.put(apiAuthData.getFeatureId(), apiAuthData.getAccessLevelCde());
			}
		}
		if (featureToAccessCdeMap != null && !featureToAccessCdeMap.isEmpty()) {
			Map<String, Long> sortedMap = new TreeMap<String, Long>(featureToAccessCdeMap);
			for (Entry<String, Long> entry : sortedMap.entrySet()) {
				UserFeatureAuthResData userFeatureAuthResData = new UserFeatureAuthResData();
				userFeatureAuthResData.setAccessLevelCde(entry.getValue());
				userFeatureAuthResData.setFeatureId(entry.getKey());
				userFeatureAuthResDataList.add(userFeatureAuthResData);
			}
		}
		return userFeatureAuthResDataList;

	}

	private boolean isAccessLevelCdeHasEnableBit(long accessLevelCde) {
		String binaryNumberStr = Long.toBinaryString(accessLevelCde);
		int accessLevelEnabledBit = Integer
				.parseInt(binaryNumberStr.length() >= Integer.valueOf(ACCESS_LEVEL_CDE_ENABLED_RL_BIT_POSITION)
						? String.valueOf(binaryNumberStr.charAt(
								binaryNumberStr.length() - Integer.valueOf(ACCESS_LEVEL_CDE_ENABLED_RL_BIT_POSITION)))
						: "0");
		return (accessLevelEnabledBit == 1) ? true : false;
	}

	private List<ApiRoleAuthData> getApiRoleAuthData(String filterApi, String filterHttpRequestMethod)
			throws WorkbenchException {

		List<ApiRoleAuthData> apiAuthDataList = new ArrayList<ApiRoleAuthData>();
		JsonObject rbacConfigJsonObj = null;
//		Enable getRbacConfigJsonDataFromFile() method to test rbactoapi mapping  with local rabcConfig.json and without DB changes.
//		rbacConfigJsonObj = getRbacConfigJsonDataFromFile();
		rbacConfigJsonObj = getRbacConfigJsonDataFromDb();

		if ((rbacConfigJsonObj != null && !rbacConfigJsonObj.isNull("appRoleBasedAccessControlMapping"))
				&& (featureToApiConfigJsonObj != null && !featureToApiConfigJsonObj.isNull("appFeatureToApiMapping"))) {
			JsonArray rbacJsonArr = rbacConfigJsonObj.getJsonArray("appRoleBasedAccessControlMapping");
			JsonObject featureToApiJsonObj = featureToApiConfigJsonObj.getJsonObject("appFeatureToApiMapping");
			if (rbacJsonArr == null || featureToApiJsonObj == null || rbacJsonArr.isEmpty()
					|| featureToApiJsonObj.isEmpty()) {
				return apiAuthDataList;
			}

//			Get Flat Feature To API JsonObject List
			List<JsonObject> featureApiJsonObjList = getFeatureToApiData(featureToApiJsonObj, filterHttpRequestMethod,
					filterApi);

//			Match the List of Api with RBAC config and filter for user roletype
			featureApiJsonObjList.forEach(featureToApiMapJsonObj -> {
				List<JsonValue> featureMatchedRbacJsonObjList = rbacJsonArr.stream()
						.filter(rbacJsonObj -> ((JsonObject) rbacJsonObj).getString("featureId")
								.equals(featureToApiMapJsonObj.getString("featureId")))
						.collect(Collectors.toList());
				featureMatchedRbacJsonObjList.forEach(feaMatRbacJsonObj -> {
					List<JsonValue> roleTypeMatchedPermissionList = ((JsonObject) feaMatRbacJsonObj)
							.getJsonArray("permissions").stream()
							.filter(jsonObj -> ((JsonObject) jsonObj).getJsonNumber("roleTypeCde")
									.longValue() == SessionHelper.getLoginUserData().getRoleTypeCde())
							.collect(Collectors.toList());
					if (roleTypeMatchedPermissionList != null && roleTypeMatchedPermissionList.size() > 0) {
						apiAuthDataList.add(setGetApiRoleAuthDataObj((JsonObject) feaMatRbacJsonObj,
								featureToApiMapJsonObj, (JsonObject) roleTypeMatchedPermissionList.get(0)));
					}
				});
			});
		}
		return apiAuthDataList;
	}

	private List<JsonObject> getFeatureToApiData(JsonObject featureToApiJsonObj, String filterHttpRequestMethod,
			String filterApi) {
		List<JsonObject> featureApiJsonObjList = new ArrayList<JsonObject>();
		try {
			featureToApiJsonObj.keySet().forEach(apiControllerKey -> {
//			e.g, about, session controller
				JsonObject apiControllerJsonObj = featureToApiJsonObj.getJsonObject(apiControllerKey);
				apiControllerJsonObj.keySet().forEach(apiMethodKey -> {
//				e.g, get, put, post, delete
					if (filterHttpRequestMethod == null || filterHttpRequestMethod.equalsIgnoreCase(apiMethodKey)) {
						JsonArray apiMethodJsonArr = apiControllerJsonObj.getJsonArray(apiMethodKey);
						apiMethodJsonArr.stream().forEach(apiMethodJsonObj -> {
							JsonObject apiMethodJsonObjTemp = (JsonObject) apiMethodJsonObj;
							String featureApi = apiMethodJsonObjTemp.getString("api");
							if (filterApi == null || filterApi.matches(featureApi)) {
								JsonArray featureIdsJsonArr = apiMethodJsonObjTemp.getJsonArray("featureIds");

								featureIdsJsonArr.stream().forEach(featureIdJsonObj -> {
									JsonObjectBuilder object = Json.createObjectBuilder();
									object.add("method", apiMethodKey);
									object.add("api", featureApi);
									object.add("featureId", featureIdJsonObj);
									featureApiJsonObjList.add(object.build());
								});
								if (filterApi != null) {
									throw new RuntimeException("FilterApi Matched");
								}
							}
						});

					}
				});
			});
		} catch (Exception ex) {
//			Known Runtime Exception thrown to break foreach loop when filterApi is matched
		}

		return featureApiJsonObjList;
	}

	/**
	 * This method used for getting rbac from config json file
	 */
	@SuppressWarnings("unused")
	@Deprecated
	private JsonObject getRbacConfigJsonDataFromFile() throws WorkbenchException {
		String rbacTenantConfigFilePath = APP_RBAC_MAP_CONFIG_FILE.replace("{tenantId}", SessionHelper.getTenantId());
		if (!FileUtility.doesResourceExist(rbacTenantConfigFilePath)) {
			throw new WorkbenchException("File not found " + rbacTenantConfigFilePath);
		}
		JsonObject rbacConfigJsonObj = null;
		if (checkAndUpdateResourceLastModification(rbacTenantConfigFilePath)) {
			rbacConfigJsonObj = FileUtility.readJsonAsObject(rbacTenantConfigFilePath);
			rbacConfigJsonData.put(rbacTenantConfigFilePath, rbacConfigJsonObj);
		} else {
			rbacConfigJsonObj = rbacConfigJsonData.get(rbacTenantConfigFilePath);

		}
		return rbacConfigJsonObj;
	}

	private JsonObject getRbacConfigJsonDataFromDb() throws WorkbenchException {
		AppVarResData appVarResData = variableProcess.getAppVariableData(WorkbenchConstants.APP_VARIABLE_KEY_RBAC);
		JsonReader jsonReader = Json.createReader(new StringReader(appVarResData.getAppVarValue()));
		JsonObject rbacConfigJsonObj = jsonReader.readObject();
		jsonReader.close();
		return rbacConfigJsonObj;
	}

	private boolean checkAndUpdateResourceLastModification(String resourceFileLocation) throws WorkbenchException {
		Date lastModifiedDtm = FileUtility.getLastModifiedDtm(resourceFileLocation);
		boolean isModified = false;
		// Should be done only once per class instance and not per request
		synchronized (this) {
			// Check if LastModifiedDateMap has entry for ruleFileLocation
			// OR if file was modified after last check
			if (!fileLmdMap.containsKey(resourceFileLocation)
					|| fileLmdMap.get(resourceFileLocation).before(lastModifiedDtm)) {
				fileLmdMap.put(resourceFileLocation, lastModifiedDtm);
				isModified = true;
			}
		}
		return isModified;
	}

	private ApiRoleAuthData setGetApiRoleAuthDataObj(JsonObject jsonRbacFeatureObj, JsonObject jsonFeatApiObj,
			JsonObject rbacPermissionObj) {
		ApiRoleAuthData apiAuthData = new ApiRoleAuthData();
		apiAuthData.setApi(jsonFeatApiObj.getString("api"));
		apiAuthData.setApiMethod(jsonFeatApiObj.getString("method"));
		apiAuthData.setFeatureDescription(jsonRbacFeatureObj.getString("featureDescription"));
		apiAuthData.setFeatureId(jsonRbacFeatureObj.getString("featureId"));
		apiAuthData.setModule(jsonRbacFeatureObj.getString("module"));
		apiAuthData.setVerb(jsonRbacFeatureObj.getString("verb"));
		apiAuthData.setAccessLevelCde(rbacPermissionObj.getJsonNumber("accessLevelCde").longValue());
		return apiAuthData;
	}

}
