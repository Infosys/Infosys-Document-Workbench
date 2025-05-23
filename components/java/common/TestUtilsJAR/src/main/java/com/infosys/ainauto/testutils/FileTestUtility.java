/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.testutils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Properties;

public class FileTestUtility {
	public static Properties readPropertiesFileFromClassPath(String propertiesFile) throws Exception {
		Properties properties = new Properties();
		InputStream input = null;
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			input = classLoader.getResourceAsStream(propertiesFile);

			properties.load(input);
		} catch (Throwable th) {
			throw new Exception("Error while loading properties file", th);
		} finally {
			safeCloseInputStream(input);
		}
		return properties;
	}

	public static String getAbsolutePathOfResourceFile(String resourceFileName) {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		URL resource = classLoader.getResource(resourceFileName);
		File file = new File(resource.getPath());
		return file.getAbsolutePath();
	}

	public static boolean copyFile(String src, String dst) throws Exception {
		FileInputStream fs = null;
		int b;

		FileOutputStream os = null;
		try {
			os = new FileOutputStream(dst);
			fs = new FileInputStream(src);

			while ((b = fs.read()) != -1) {
				os.write(b);
			}
		} catch (IOException e) {
			throw e;
		} finally {
			safeCloseInputStream(fs);
			safeCloseOutputStream(os);
		}
		return true;
	}

	public static boolean saveFile(String fileName, String content) {
		byte data[] = content.getBytes();
		boolean isSuccess = true;
		try (FileOutputStream out = new FileOutputStream(fileName)) {
			out.write(data);
		} catch (IOException e) {
			isSuccess = false;
		}
		return isSuccess;
	}

	public static boolean createDirsRecursively(String path) {
		File file = new File(path);

		if (file.exists()) {
			return true;
		}
		return file.mkdirs();
	}

	public static void safeCloseOutputStream(OutputStream outputStream) {
		if (outputStream != null) {
			try {
				outputStream.close();
			} catch (IOException e) {
			}
		}
	}

	public static void safeCloseInputStream(InputStream inputStream) {
		if (inputStream != null) {
			try {
				inputStream.close();
			} catch (IOException e) {
			}
		}
	}

}
