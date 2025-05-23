/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.common;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TestHelper {

	/**
	 * @param relativePath
	 *            e.g. "src/main/java"
	 * @param extension
	 * @return
	 * @throws Exception
	 */
	public static List<String> getAllMatchingFiles(String relativePath, String extension) throws Exception {
		File dir = new File(relativePath);
		List<String> fileList = listFilesForFolder(dir, extension);

		return fileList;
	}

	private static List<String> listFilesForFolder(final File folder, String extension) {
		List<String> fileList = new ArrayList<>();
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				fileList.addAll(listFilesForFolder(fileEntry, extension));
			} else if (fileEntry.getName().endsWith(extension)) {
				fileList.add(fileEntry.getAbsolutePath());
			}
		}
		return fileList;
	}
}
