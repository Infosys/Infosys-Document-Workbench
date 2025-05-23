/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.commonutils;

import java.io.StringReader;
import java.net.URI;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonException;
import javax.json.JsonReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class StringUtility {

	public static final String NON_ASCI_REGEX = "[^\\x00-\\x7F]";
	private final static Logger LOGGER = LoggerFactory.getLogger(StringUtility.class);

	private StringUtility() {
		// private constructor to avoid instantiation
	}

	public static boolean hasValue(String str) {
		if (str == null || str.length() == 0) {
			return false;
		}
		return true;
	}

	public static boolean hasTrimmedValue(String str) {
		if (str == null || str.trim().length() == 0) {
			return false;
		}
		return true;
	}

	public static String generateTransactionId() {
		return String.valueOf(getRangeOfRandomNumberInInt(100000000, 999999999));
	}

	public static String findAndReplace(String str, String findRegex, String replaceWithString) {
		String obj = str.replaceAll(findRegex, replaceWithString);
		return obj;
	}

	public static String findAndReplace1(String str, String findRegex, String replaceWithString) {
		if (!StringUtility.hasValue(str))
			return "";
		String obj = str.replaceAll(findRegex, replaceWithString);
		return obj;
	}

	// TODO - Not fully tested - Use at your own risk!
	public static String removeNonRecognizedCharacters(String textToClean) {
		if (textToClean == null || textToClean.length() == 0) {
			return textToClean;
		}
		StringBuffer sb = new StringBuffer();
		List<Integer> restrictedAsciiCharacters = Arrays.asList(160);
		for (char c : textToClean.toCharArray()) {
			LOGGER.debug("char=" + c + " AND Decimal=" + (int) c);
			if (restrictedAsciiCharacters.contains((int) c)) {

				continue;
			}
			sb.append(c);
		}
		return sb.toString();
	}

	public static List<String> getTrigramList(String textToProcess) {

		textToProcess = textToProcess.replaceAll("[^A-Za-z0-9]", " ");
		LOGGER.debug(textToProcess);
		// Step 2 - Convert all words to token
		textToProcess = textToProcess.replaceAll("   ", " ");
		textToProcess = textToProcess.replaceAll("  ", " ");
		String[] unigram = textToProcess.split(" ");
		List<String> trigram = new ArrayList<String>();
		int i = 0, j = 1, k = 2;
		while (i < unigram.length) {
			while (j < unigram.length - 1) {
				while (k < unigram.length - 2) {
					String trigramText = unigram[i] + " " + unigram[j] + " " + unigram[k];
					trigram.add(trigramText);
					i += 3;
					j += 3;
					k += 3;
					// System.out.println(trigramText+i+j+k);
				}
				j += 1;
				i += 1;
			}
			if (i == unigram.length - 2 && j == unigram.length - 1) {
				String trigramText = unigram[i] + " " + unigram[j];
				trigram.add(trigramText);
				i += 2;
				j += 1;
				// System.out.println(trigramText+i+j+k);
			}
		}
		return trigram;
	}

	public static List<String> getDigramList(String textToProcess) {

		textToProcess = textToProcess.replaceAll("[^A-Za-z0-9]", " ");

		// Step 2 - Convert all words to token
		textToProcess = textToProcess.replaceAll("\\s{2,4}", " ");
		textToProcess = textToProcess.replaceAll("  ", " ");
		LOGGER.debug(textToProcess);
		String[] unigram = textToProcess.split(" ");
		List<String> digram = new ArrayList<String>();
		int i = 0, j = 1;
		while (i < unigram.length) {
			while (j < unigram.length - 1) {
				String digramText = unigram[i] + " " + unigram[j];
				digram.add(digramText);
				i += 2;
				j += 2;
			}
			if (i == unigram.length - 1) {
				String digramText = unigram[i];
				digram.add(digramText);
				i += 1;
			}
		}
		return digram;
	}

	public static String getCapitalizedName(String textToProcess1) {
		String textToProcess = "";
		textToProcess = textToProcess1.toLowerCase().replaceAll("[^A-Za-z0-9]", " ").trim();
		String[] textArray = textToProcess.split(" ");
		if (textArray.length > 0) {
			textToProcess = textArray[0].substring(0, 1).toUpperCase() + textArray[0].substring(1);
		} else {
			textToProcess = textToProcess.substring(0, 1).toLowerCase() + textToProcess.substring(1);
		}
		return textToProcess;
	}

	public static boolean isNumber(String data) {
		if (data == null || data.length() == 0) {
			return false;
		}
		String regex = "\\d+";
		return data.matches(regex);
	}

	public static String getStringExtension(String content) {
		String extension;
		switch (content) {
		case "data:image/jpeg;base64":
			extension = "jpeg";
			break;
		case "data:image/png;base64":
			extension = "png";
			break;
		default:
			extension = "jpg";
			break;
		}
		return extension;
	}

	public static String getStringExtension1(String content) {
		String extension = content.split(";")[0].split("/")[1];
		return extension;
	}

	public static String getFileExtension(String fileName) {
		String fileExtension = "";
		if (StringUtility.hasValue(fileName)) {
			String[] tokens = fileName.split("\\.(?=[^\\.]+$)");
			if (tokens.length > 1) {
				fileExtension = tokens[tokens.length - 1];
			}
		}
		return fileExtension;
	}

	public static String getBase64Extension(String content) {
		String extension;
		switch (content) {
		case "jpeg":
			extension = "data:image/jpeg;base64";
			break;
		case "png":
			extension = "data:image/png;base64";
			break;
		default:
			extension = "data:image/jpg;base64";
			break;
		}
		return extension;
	}

	public static String getBase64Extension1(String content) {
		String extension = "";
		if (content.isEmpty()) {
			extension = "data:image" + content + ";base64";
		} else {
			extension = "data:image/" + content + ";base64";
		}

		return extension;
	}

	public static List<String> getBetweenStrings(String text, String textFrom, String textTo) {

		Pattern p = Pattern.compile(Pattern.quote(textFrom) + "(.*?)" + Pattern.quote(textTo));
		Matcher m = p.matcher(text);
		List<String> findReplaceList = new ArrayList<String>();
		while (m.find()) {
			String match = m.group(1);
			findReplaceList.add(match);
		}
		return findReplaceList;
	}

	public static String replaceNewLineCharacters(String content) {
		String[] textArray = content.split("\\r?\\n");
		String text = "";
		for (int j = 0; j < textArray.length; j++) {
			if (textArray[j].equals(""))
				text += "<br>";
			else
				text += textArray[j] + "<br>";
		}
		return text;
	}

	public static String wrapInQuotes(String str) {
		return "\"" + str + "\"";
	}

	public static String getHostNameFromUrl(String url) {
		String hostNameWithPort = "";
		try {
			URI uri = new URI(url);
			hostNameWithPort = uri.getHost() + ":" + uri.getPort();
		} catch (Exception ex) {
			// Do nothing
		}
		return hostNameWithPort;
	}

	public static boolean isJsonValid(String textToProcess) {
		boolean value = false;
		try {
			if (textToProcess == null)
				return false;
			JsonReader reader = Json.createReader(new StringReader(textToProcess));
			JsonArray jsonArray = reader.readArray();
			if (jsonArray.size() > 0) {
				value = true;
			}
		} catch (JsonException ex) {
			value = false;
		} catch (Exception e) {
			value = false;
		}
		return value;
	}

	public static String getRandomAlphaString(int n) {
		String alphabets = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "abcdefghijklmnopqrstuvxyz";
		StringBuilder sb = new StringBuilder(n);
		for (int i = 0; i < n; i++) {
			int index = (int) (alphabets.length() * getRandomNumberInDouble());
			sb.append(alphabets.charAt(index));
		}
		return sb.toString();
	}

	/**
	 * Returns the next pseudo random, uniformly distributed double value from
	 * 0.00000000000000000 to 0.99999999999999999.
	 * 
	 * @return double pseudo random number.
	 */
	public static double getRandomNumberInDouble() {
		return new SecureRandom().nextDouble();
	}

	/**
	 * Returns the next pseudo random, uniformly distributed integer value.
	 * 
	 * @return int pseudo random number.
	 */
	public static int getRandomNumberInInt() {
		return new SecureRandom().nextInt();
	}

	/**
	 * Returns the next pseudo random, uniformly distributed integer value between
	 * two limits. Start, End indexes are inclusive. Must be positive.
	 * 
	 * @return int pseudo random in given limit.
	 */
	public static int getRangeOfRandomNumberInInt(int start, int end) {
		return Math.abs(new SecureRandom().nextInt(end - start + 1)) + start;
	}

	/**
	 * Method prevents attacker to forge log entries or inject malicious content
	 * into the log
	 * 
	 * @return sanitized str
	 */
	public static String sanitizeReqData(String str) {
		String cleanString = "";
		if (hasValue(str)) {
			for (int i = 0; i < str.length(); ++i) {
				cleanString += StringUtility.cleanReqParamChar(str.charAt(i));
			}
		} else {
			cleanString = null;
		}
		return cleanString;
	}

	/**
	 * @return unique string
	 */
	public static String getUniqueString() {
		return UUID.randomUUID().toString();
	}

	public static String sanitizeSql(String query) {
		String cleanString = "";
		if (query == null)
			cleanString = null;
		else {
			for (int i = 0; i < query.length(); ++i) {
				cleanString += StringUtility.cleanQueryChar(query.charAt(i));
			}
		}
		return cleanString;
	}
	
	public static String[] splitWithEscape(String str, String delimiter, int max) {
		String regex = "(?<!\\\\)" + Pattern.quote(delimiter);
		return str.split(regex, max);
	}
	
	public static String[] splitWithEscape(String str, String delimiter) {
		return splitWithEscape(str,delimiter,0);
	}

	private static char cleanQueryChar(char queryChar) {
		char charCleaned = '%';
		// 0 - 9
		for (int i = 48; i < 58; ++i) {
			if (queryChar == i)
				charCleaned = (char) i;
		}
		// 'A' - 'Z'
		for (int i = 65; i < 91; ++i) {
			if (queryChar == i)
				charCleaned = (char) i;
		}
		// 'a' - 'z'
		for (int i = 97; i < 123; ++i) {
			if (queryChar == i)
				charCleaned = (char) i;
		}
		// other valid characters
		switch (queryChar) {
		case '.':
			charCleaned = '.';
			break;
		case '_':
			charCleaned = '_';
			break;
		case ' ':
			charCleaned = ' ';
			break;
		case '(':
			charCleaned = '(';
			break;
		case ')':
			charCleaned = ')';
			break;
		case '?':
			charCleaned = '?';
			break;
		case ',':
			charCleaned = ',';
			break;
		case '=':
			charCleaned = '=';
			break;
		case '<':
			charCleaned = '<';
			break;
		case '>':
			charCleaned = '>';
			break;
		case '\'':
			charCleaned = '\'';
			break;
		case '*':
			charCleaned = '*';
			break;
		case '!':
			charCleaned = '!';
			break;
		case '{':
			charCleaned = '{';
			break;
		case '}':
			charCleaned = '}';
			break;
		case ':':
			charCleaned = ':';
			break;
		case '\t':
			charCleaned = '\t';
			break;
		case '-':
			charCleaned = '-';
			break;
		}
		return charCleaned;
	}

	private static char cleanReqParamChar(char reqParamChar) {
		char charCleaned = '_';
		// 0 - 9
		for (int i = 48; i < 58; ++i) {
			if (reqParamChar == i)
				charCleaned = (char) i;
		}
		// 'A' - 'Z'
		for (int i = 65; i < 91; ++i) {
			if (reqParamChar == i)
				charCleaned = (char) i;
		}
		// 'a' - 'z'
		for (int i = 97; i < 123; ++i) {
			if (reqParamChar == i)
				charCleaned = (char) i;
		}
		// other valid characters
		switch (reqParamChar) {
		case '.':
			charCleaned = '.';
			break;
		case '-':
			charCleaned = '-';
			break;
		case ' ':
			charCleaned = ' ';
			break;
		case '(':
			charCleaned = '(';
			break;
		case ')':
			charCleaned = ')';
			break;
		case '?':
			charCleaned = '?';
			break;
		case ',':
			charCleaned = ',';
			break;
		case '=':
			charCleaned = '=';
			break;
		case '\'':
			charCleaned = '\'';
			break;
		case '*':
			charCleaned = '*';
			break;
		case '!':
			charCleaned = '!';
			break;
		case ':':
			charCleaned = ':';
			break;
		case '+':
			charCleaned = '+';
			break;
		case '&':
			charCleaned = '&';
			break;
		case '~':
			charCleaned = '~';
			break;
		case '/':
			charCleaned = '/';
			break;
		case '#':
			charCleaned = '#';
			break;
		case '[':
			charCleaned = '[';
			break;
		case ']':
			charCleaned = ']';
			break;
		case '@':
			charCleaned = '@';
			break;
		case '$':
			charCleaned = '$';
			break;
		case ';':
			charCleaned = ';';
			break;
		case '%':
			charCleaned = '%';
			break;
		} 
		return charCleaned;
	}
}