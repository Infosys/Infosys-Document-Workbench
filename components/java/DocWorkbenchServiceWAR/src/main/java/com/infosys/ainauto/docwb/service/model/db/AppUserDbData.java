/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.db;

import java.util.Set;

import org.springframework.security.core.GrantedAuthority;

public class AppUserDbData {
	
	private long appUserId;
	private String userLoginId;
	private String userEmail;
	private String userFullName;
	private long userTypeCde;
	private String userTypeTxt;
	private String userPassword;
	private String accessToken;
	private boolean isAccountNonLocked;
	private boolean isAccountNonExpired;
	private boolean isCredentialsNonExpired;
	private boolean isAccountEnabled;
	private Set<GrantedAuthority> grantedAuthorities;
	private long roleTypeCde;
	private String roleTypeTxt;
	private String tenantId;
	private String createBy;
	private String createDtm;
	private String lastModBy;
	private String lastModDtm;

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public String getRoleTypeTxt() {
		return roleTypeTxt;
	}

	public void setRoleTypeTxt(String roleTypeTxt) {
		this.roleTypeTxt = roleTypeTxt;
	}

	public long getRoleTypeCde() {
		return roleTypeCde;
	}

	public void setRoleTypeCde(long roleTypeCde) {
		this.roleTypeCde = roleTypeCde;
	}

	public long getAppUserId() {
		return appUserId;
	}

	public void setAppUserId(long appUserId) {
		this.appUserId = appUserId;
	}

	public String getUserLoginId() {
		return userLoginId;
	}

	public void setUserLoginId(String userLoginId) {
		this.userLoginId = userLoginId;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public String getUserPassword() {
		return userPassword;
	}

	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public boolean isAccountNonLocked() {
		return isAccountNonLocked;
	}

	public void setAccountNonLocked(boolean isAccountNonLocked) {
		this.isAccountNonLocked = isAccountNonLocked;
	}

	public boolean isAccountNonExpired() {
		return isAccountNonExpired;
	}

	public void setAccountNonExpired(boolean isAccountNonExpired) {
		this.isAccountNonExpired = isAccountNonExpired;
	}

	public boolean isCredentialsNonExpired() {
		return isCredentialsNonExpired;
	}

	public void setCredentialsNonExpired(boolean isCredentialsNonExpired) {
		this.isCredentialsNonExpired = isCredentialsNonExpired;
	}

	public boolean isAccountEnabled() {
		return isAccountEnabled;
	}

	public void setAccountEnabled(boolean isAccountEnabled) {
		this.isAccountEnabled = isAccountEnabled;
	}

	public Set<GrantedAuthority> getGrantedAuthorities() {
		return grantedAuthorities;
	}

	public void setGrantedAuthorities(Set<GrantedAuthority> grantedAuthorities) {
		this.grantedAuthorities = grantedAuthorities;
	}

	public String getUserFullName() {
		return userFullName;
	}

	public void setUserFullName(String userFullName) {
		this.userFullName = userFullName;
	}

	public long getUserTypeCde() {
		return userTypeCde;
	}

	public void setUserTypeCde(long l) {
		this.userTypeCde = l;
	}

	public String getUserTypeTxt() {
		return userTypeTxt;
	}

	public void setUserTypeTxt(String userTypeTxt) {
		this.userTypeTxt = userTypeTxt;
	}

	public String getCreateBy() {
		return createBy;
	}

	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}

	public String getCreateDtm() {
		return createDtm;
	}

	public void setCreateDtm(String createDtm) {
		this.createDtm = createDtm;
	}

	public String getLastModBy() {
		return lastModBy;
	}

	public void setLastModBy(String lastModBy) {
		this.lastModBy = lastModBy;
	}

	public String getLastModDtm() {
		return lastModDtm;
	}

	public void setLastModDtm(String lastModDtm) {
		this.lastModDtm = lastModDtm;
	}

}
