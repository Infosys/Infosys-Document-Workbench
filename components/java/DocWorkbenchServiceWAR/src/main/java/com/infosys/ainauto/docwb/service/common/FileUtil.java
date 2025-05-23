/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.common;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import com.infosys.ainauto.commonutils.FileUtility;

public class FileUtil {

	private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

	public static boolean saveMultipartFile(MultipartFile file, String destinationPath) {
		boolean status = false;
		try {
			File destinationFile = new File(FileUtility.cleanPath(destinationPath));
			file.transferTo(destinationFile);
			status = true;
		} catch (Exception e) {
			logger.error("Error occurred in saveFile()", e);
		}
		return status;
	}

	public static boolean createImageFile(String path, String content) {
		byte[] data = DatatypeConverter.parseBase64Binary(content);
		File file = new File(FileUtility.cleanPath(path));
		boolean isSuccess = true;
		try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file))) {
			outputStream.write(data);
			outputStream.close();
		} catch (IOException e) {
			isSuccess = false;
			logger.error("Error occurred while creating Image", e);
		}
		return isSuccess;
	}

}