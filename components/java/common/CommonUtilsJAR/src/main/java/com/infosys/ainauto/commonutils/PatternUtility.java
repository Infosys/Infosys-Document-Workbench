/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.commonutils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PatternUtility {

	private static final Pattern userNamePattern = Pattern.compile("[a-zA-Z0-9]*");
	private static final Pattern passwordPattern = Pattern
			.compile("(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{6,}$");
	private static final Pattern emailIdPattern = Pattern
			.compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
	private static final Pattern htmlImgTagPattern = Pattern.compile("<img[^>]*src=[\"']([^\"^']*)",
			Pattern.CASE_INSENSITIVE);

	private PatternUtility() {
		// private constructor to avoid instantiation
	}

	public static final boolean isValidUserName(String userName) {
		Matcher matcher = userNamePattern.matcher(userName);
		return matcher.matches();
	}

	public static final boolean isValidPassword(String password) {
		Matcher matcher = passwordPattern.matcher(password);
		return matcher.matches();
	}

	public static boolean isValidEmailId(String emailId) {
		Matcher matcher = emailIdPattern.matcher(emailId);
		return matcher.matches();
	}

	public static final List<String> getHtmlImgSrcValues(String html) {
		List<String> matchesList = new ArrayList<String>();
		Matcher matcher = htmlImgTagPattern.matcher(html);
		while (matcher.find()) {
			matchesList.add(matcher.group(1));
		}
		return matchesList;
	}

	public static String formatEmailId(String emailId) {
		String id = emailId.trim();
		id = id.replaceAll(" ", ";");
		String[] idList = id.split(";");
		List<String> list = new ArrayList<String>();
		for (String s : idList) {
			if (s != null && s.length() > 0) {
				list.add(s);
			}
		}
		idList = list.toArray(new String[list.size()]);
		String formattedId = "";
		for (String verifyId : idList) {
			boolean isEmailId = PatternUtility.isValidEmailId(verifyId);
			if (!isEmailId) {
				String errorMessage = "Invalid E-mail Id.";
				return errorMessage;
			}
			formattedId += verifyId + ";";
		}
		formattedId = formattedId.substring(0, formattedId.length() - 1);
		return formattedId;

	}

	public static String formatThreadName(String name) {
		return name != null ? (name.length() > 14 ? name.substring(0, 14) : name).concat("-Thread-") : name;
	}
}
