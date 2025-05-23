/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.infosys.ainauto.docwb.service.security.AuthenticationFailureHandler;
import com.infosys.ainauto.docwb.service.security.AuthenticationSuccessHandler;
import com.infosys.ainauto.docwb.service.security.LogoutSuccessHandler;
import com.infosys.ainauto.docwb.service.security.TokenAuthenticationFilter;

@Configuration
@EnableWebSecurity
//@EnableGlobalMethodSecurity(prePostEnabled = true) //-> Note: This setting has been moved to patterns-routing-servlet.xml  
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	//This bean is needed else it throws error on startup
	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public TokenAuthenticationFilter getTokenAuthenticationFilter() throws Exception {
		return new TokenAuthenticationFilter();
	}
	
	@Autowired
	private AuthenticationSuccessHandler authenticationSuccessHandler;

	@Autowired
	private AuthenticationFailureHandler authenticationFailureHandler;
	
	@Autowired
	private LogoutSuccessHandler logoutSuccessHandler;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// @formatter:off		
		http.addFilterBefore(getTokenAuthenticationFilter(), BasicAuthenticationFilter.class)
			.authorizeRequests()
//	        .antMatchers("/swagger/**").hasAnyRole("ADMIN")
	        .antMatchers("/login*").permitAll()
			.antMatchers("/webjars/**").permitAll()
			.antMatchers("/schema/**").permitAll()
	        .antMatchers("/registration*").permitAll()
	        .antMatchers("/api/v1/auth*").permitAll()
	        .antMatchers("/api/v1/user/register").permitAll()
	        .anyRequest().authenticated()
	        .and()
	        .formLogin()
	        .loginPage("/login.html") //For custom login page
	        .loginProcessingUrl("/dologin")
	        .successHandler(authenticationSuccessHandler)
	        .failureHandler(authenticationFailureHandler)
	        .and()
	        .logout()
	        .logoutUrl("/logout")
	        .logoutSuccessHandler(logoutSuccessHandler)
	        .invalidateHttpSession(true)
	        .deleteCookies("JSESSIONID")
	        .and()
	        .csrf().disable()
	        .headers()
	        .contentSecurityPolicy("script-src 'self';");
		// @formatter:on
	}
}
