/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.common;

import java.util.Date;
import java.util.Map;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class TokenHelper {

	private static final Logger logger = LoggerFactory.getLogger(TokenHelper.class);

	public static String generateToken(String subject, Map<String, Object> claimsMap, String secret,
			long tokenExpiryInSecs) {

		Date currentDate = new Date(new DateTime().getMillis());
		Date expirationDate = new Date(new DateTime().getMillis() + tokenExpiryInSecs * 1000);

    	String jwt = Jwts.builder()
                .setIssuer( WorkbenchConstants.JWT_APP_NAME )
                .setClaims(claimsMap)
                .setSubject(subject)
                .setIssuedAt(currentDate)
                .setExpiration(expirationDate)
                .signWith( SignatureAlgorithm.HS512, secret )
				.compact();
		return jwt;
	}

	public static String getSubjectFromToken(String token, String secret) {
		String subject = null;
		try {
			Claims claims = getClaimsFromToken(token, secret);
			if (claims != null) {
				subject = claims.getSubject();
			}
		} catch (Exception e) {
			logger.error("Error occurred while getting subject from token", e);
		}
		return subject;
	}
	
	public static String getTenantId(String token, String secret) {
		String tenantId = null;
		try {
			Claims claims = getClaimsFromToken(token, secret);
			if (claims != null) {
				tenantId = claims.get(WorkbenchConstants.TENANT_ID,String.class);
			}
		} catch (Exception e) {
			logger.error("Error occurred while getting subject from token", e);
		}
		return tenantId;
	}

	public static boolean hasTokenExpired(String token, String secret) {
		boolean isExpired = true; // Assume token has expired
		Date currentDate = new Date();
		try {
			Claims claims = getClaimsFromToken(token, secret);
			if (claims != null) {
				if (currentDate.compareTo(claims.getExpiration()) < 0) {
					isExpired = false;
				}
			}
		} catch (Exception e) {
			logger.error("Error occurred while getting subject from token", e);
		}
		return isExpired;
	}

	public static Claims getClaimsFromToken(String token, String secret) {
		Claims claims;
		try {
			claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
		} catch (Exception e) {
			claims = null;
		}
		return claims;
		
	}

}