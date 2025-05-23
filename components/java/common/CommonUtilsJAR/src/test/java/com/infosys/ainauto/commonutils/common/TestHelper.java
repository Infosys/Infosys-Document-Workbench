/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.commonutils.common;

import java.io.File;

public class TestHelper {

	public static String getFileFullPathFromClassPath(String fileName) throws Exception {
		String fileFullPath = "";
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

			fileFullPath = classLoader.getResource(fileName).getPath();
		} catch (Throwable th) {
			throw new Exception("Error while loading properties file", th);
		}
		return fileFullPath;
	}
	
	public static boolean doesFileExist(String fileName) {
		if (fileName == null || fileName.length() == 0) {
			return false;
		}
		return new File(fileName).exists();
	}
	
	public static boolean deleteFile(String fileName) {
		return new File(fileName).delete();
	}
}
