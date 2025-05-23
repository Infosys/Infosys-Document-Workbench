/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.infosys.ainauto.docwb.service.model.db.AppUserDbData;

public class UserDetailsData implements UserDetails {
	private static final long serialVersionUID = 1L;
	private AppUserDbData appUserDbData;

	public UserDetailsData(AppUserDbData appUserDbData) {
		this.appUserDbData = appUserDbData;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return appUserDbData.getGrantedAuthorities();
	}

	@Override
	public String getPassword() {
		return appUserDbData.getUserPassword();
	}

	public long getUserId() {
		return appUserDbData.getAppUserId();
	}

	@Override
	public String getUsername() {
		return appUserDbData.getUserLoginId();
	}

	@Override
	public boolean isAccountNonExpired() {
		return appUserDbData.isAccountNonExpired();
	}

	@Override
	public boolean isAccountNonLocked() {
		return appUserDbData.isAccountNonLocked();
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return appUserDbData.isCredentialsNonExpired();
	}

	@Override
	public boolean isEnabled() {
		return appUserDbData.isAccountEnabled();
	}

	public String getUserFullName() {
		return appUserDbData.getUserFullName();
	}

	public String getUserEmail() {
		return appUserDbData.getUserEmail();
	}

	public long getUserTypeCde() {
		return appUserDbData.getUserTypeCde();
	}

	public String getUserTypeTxt() {
		return appUserDbData.getUserTypeTxt();
	}

	public String getRoleTypeTxt() {
		return appUserDbData.getRoleTypeTxt();
	}

	public long getRoleTypeCde() {
		return appUserDbData.getRoleTypeCde();
	}
	
	public String getTenantId() {
		return appUserDbData.getTenantId();
	}

}
