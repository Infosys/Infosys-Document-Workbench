/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.config;

import java.nio.charset.StandardCharsets;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.infosys.ainauto.commonutils.StringUtility;

@Configuration
@PropertySource("classpath:application.properties")
public class WebAppConfig {

	@Autowired
	Environment environment;

	@Bean
	public JdbcTemplate jdbcTemplate() {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(getBasicDataSource());
		jdbcTemplate.setQueryTimeout(Integer.parseInt(environment.getProperty("jdbc.queryTimeout")));
		return jdbcTemplate;
	}

	private BasicDataSource getBasicDataSource() {
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName(environment.getProperty("jdbc.driverClassName"));
		dataSource.setUrl(environment.getProperty("jdbc.url"));
		dataSource.setUsername(environment.getProperty("jdbc.username"));
		String passwrd = new String(Base64.decodeBase64(environment.getProperty("jdbc.drowssap")),
				StandardCharsets.UTF_8);
		dataSource.setPassword(passwrd);
		passwrd = "";
		String initSize = environment.getProperty("jdbc.initialSize");
		if (StringUtility.hasValue(initSize)) {
			dataSource.setInitialSize(Integer.parseInt(initSize));
		}
		dataSource.setMaxTotal(Integer.parseInt(environment.getProperty("jdbc.maxActive")));
		dataSource.setMaxIdle(Integer.parseInt(environment.getProperty("jdbc.maxIdle")));
		dataSource.setMinIdle(Integer.parseInt(environment.getProperty("jdbc.minIdle")));
		return dataSource;
	}

	@Deprecated
	private DriverManagerDataSource getDataSource() {
		// Part of Spring jdbc framework
		final DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName(environment.getProperty("jdbc.driverClassName"));
		dataSource.setUrl(environment.getProperty("jdbc.url"));
		dataSource.setUsername(environment.getProperty("jdbc.username"));
		String passwrd = new String(Base64.decodeBase64(environment.getProperty("jdbc.drowssap")),
				StandardCharsets.UTF_8);
		dataSource.setPassword(passwrd);
		passwrd = "";
		return dataSource;
	}

}