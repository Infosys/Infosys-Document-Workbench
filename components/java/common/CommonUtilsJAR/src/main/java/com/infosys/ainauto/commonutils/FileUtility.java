/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.commonutils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FileUtility {

	private static final Logger logger = LoggerFactory.getLogger(FileUtility.class);

	private static final String FILE_MIME_TYPE = "docwbmimetypes.properties";

	private static final Properties mimeTypeProperties = new Properties(FileUtility.readProperties(FILE_MIME_TYPE));

	private FileUtility() {
		// private constructor to avoid instantiation
	}

	public static boolean doesFileExist(String fileName) {
		if (fileName == null || fileName.length() == 0) {
			return false;
		}
		return new File(cleanPath(fileName)).exists();
	}

	public static String getCurrentDirectory() {
		return System.getProperty("user.dir");
	}

	public static boolean deleteFile(String fileName) {
		return new File(cleanPath(fileName)).delete();
	}

	public static boolean saveFile(String fileName, String content) {
		byte data[] = content.getBytes();
		boolean isSuccess = true;
		try (FileOutputStream out = new FileOutputStream(cleanPath(fileName))) {
			out.write(data);
		} catch (IOException e) {
			isSuccess = false;
			logger.error("Error occurred in saveFile()", e);
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

	public static String getAbsolutePath(String location) {
		// Check if not windows path and not starting with "/"
		if (location != null && location.length() > 0 && location.indexOf(":") < 0 && !location.startsWith("/")) {
			// String fileSeparator = (location.startsWith("\\") ? "" : "\\");
			// location = getCurrentDirectory() + fileSeparator + location;
			location = getConcatenatedName(getCurrentDirectory(), location);
		}
		return location;
	}

	public static String getFileNameNoExtension(String fileName) {
		if (fileName.indexOf(".") > 0) {
			return fileName.replaceFirst("[.][^.]+$", "");
		}
		return fileName;
	}

	public static String getOrginalFileName(String fileName) {
		int indexOfDot = fileName.indexOf(".", fileName.indexOf(".") + 1);
		return fileName.substring(0, indexOfDot);
	}

	public static void mergeFiles(String sourceFile, String destinationFile) {
		FileWriter fw = null;
		BufferedWriter outFileWriter = null;
		BufferedReader inFileReader = null;
		FileInputStream fis = null;
		try {
			fw = new FileWriter(destinationFile, true);
			outFileWriter = new BufferedWriter(fw);

			fis = new FileInputStream(sourceFile);
			inFileReader = new BufferedReader(new InputStreamReader(fis));

			String aLine;
			while ((aLine = inFileReader.readLine()) != null) {
				outFileWriter.write(aLine);
				outFileWriter.newLine();
			}

			inFileReader.close();
			outFileWriter.close();

		} catch (IOException e1) {
			logger.error("Error occurred in mergeFiles()", e1);
		} finally {
			safeCloseInputStream(fis);
			safeCloseFileWriter(fw);
		}

	}

	/**
	 * Check if a resource exists (resource = file or folder)
	 * 
	 * @param fileName
	 * @return
	 */
	public static boolean doesResourceExist(String fileName) {
		if (fileName == null || fileName.length() == 0) {
			return false;
		}
		if (!fileName.startsWith("/")) {
			fileName = "/" + fileName;
		}

		InputStream is = null;
		try {
			is = FileUtility.class.getResourceAsStream(fileName);
			return (null != is);
		} finally {
			safeCloseInputStream(is);
		}
	}

	public static String readResourceFile(String fileName) {
		if (fileName == null || fileName.length() == 0) {
			return "";
		}
		StringBuilder out = new StringBuilder();
		InputStream in = null;
		try {
			if (!fileName.startsWith("/")) {
				fileName = "/" + fileName;
			}
			in = FileUtility.class.getResourceAsStream(fileName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

			String line;
			while ((line = reader.readLine()) != null) {
				out.append(line).append("\n");
			}
			reader.close();
		} catch (IOException e) {
			logger.error("Error occurred in readResourceFile()", e);
		} finally {
			safeCloseInputStream(in);
		}
		return out.toString();
	}

	public static String createBackup(String fileName) {
		int index = 0;
		String backupFileName = fileName + ".bkup." + index;
		while (doesFileExist(backupFileName)) {
			backupFileName = fileName + ".bkup." + index++;
		}
		if (renameFile(fileName, backupFileName)) {
			return backupFileName;
		}
		return "";
	}

	public static String getConcatenatedName(String part1, String part2) {
		return part1.concat(File.separator).concat(part2);
	}

	public static String getConcatenatedPath(String part1, String part2) {
		return getConcatenatedName(getFileSeparatorTokenizedStr(part1), getFileSeparatorTokenizedStr(part2));
	}

	public static String getFileSeparatorTokenizedStr(String str) {
		String tokenized = "";
		String[] tokens = str.split("/");
		for (int i = 0; i < tokens.length - 1; i++) {
			tokenized += tokens[i] + File.separator;
		}
		if (tokenized.equals("")) {
			return str;
		}
		tokenized += tokens[tokens.length - 1];
		return tokenized;
	}

	public static String generateUniqueFileName(String fileName) {
		// Split string into name and extension
		String[] tokens = fileName.split("\\.(?=[^\\.]+$)");
		// Fix for defect #1 logged on issue #131
		String fileExtension = "";
		if (tokens.length == 2) {
			fileExtension = "." + tokens[1];
		}
		return UUID.randomUUID().toString() + fileExtension;
	}

	public static boolean renameFile(String from, String to) {
		File oldFile = new File(from);
		File newFile = new File(to);
		return oldFile.renameTo(newFile);
	}

	public static String[] getContent(String filePath) {
		StringBuilder sb = new StringBuilder();
		try (BufferedReader inFileReader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)))) {
			String aLine;
			while ((aLine = inFileReader.readLine()) != null) {
				sb.append(aLine).append("\n");
			}
		} catch (IOException e) {
			logger.error("Error occurred in getContent()", e);
		}
		return sb.toString().split("\n");
	}

	// public static boolean saveMultipartFile(MultipartFile file, String
	// destinationPath) {
	// boolean status = false;
	// try {
	// File destinationFile = new File(destinationPath);
	// file.transferTo(destinationFile);
	// status = true;
	// } catch (Exception e) {
	// logger.error("Error occurred in saveFile()", e);
	// }
	// return status;
	// }

	public static boolean moveFile(String fromLocation, String toLocation) {
		File fileFrom = new File(fromLocation);
		File fileTo = new File(toLocation);
		return fileFrom.renameTo(fileTo);
	}

	public static byte[] readFile(String filePath) {
		File file = new File(cleanPath(filePath));
		byte[] data = {};
		try (FileInputStream fis = new FileInputStream(file)) {
			data = new byte[(int) file.length()];
			fis.read(data);
		} catch (IOException e) {
			logger.error("File read operation failed", e);
		}
		return data;
	}

	@Deprecated
	public static byte[] readFileByHash(String filePath, String lookupFilePath)
			throws FileNotFoundException, IOException {
		Map<String, String> hashByFileName = new HashMap<>();
		try (BufferedReader br = new BufferedReader(new FileReader(lookupFilePath))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] values = line.split(",");
				hashByFileName.put(values[0], values[1]);
			}
		}
		String fileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1);
		String tempfilePath = filePath.replace(fileName, "");
		tempfilePath = tempfilePath + hashByFileName.get(fileName);
		List<File> tempFileName = new ArrayList<>();
		FileUtility.getAllFiles(tempfilePath, tempFileName);
		File file = tempFileName.get(0);
		logger.info("File by its hash value : {}", file.getAbsolutePath());
		byte[] data = {};
		try (FileInputStream fis = new FileInputStream(file)) {
			data = new byte[(int) file.length()];
			fis.read(data);
		} catch (IOException e) {
			logger.error("File read operation failed", e);
		}
		return data;
	}

	public static void getAllFiles(String dirName, List<File> fileList) {
		File currDir = new File(cleanPath(dirName));

		File[] currDirFileArray = currDir.listFiles();
		for (File file : currDirFileArray) {
			if (file.isFile()) {
				fileList.add(file);
			} else if (file.isDirectory()) {
				getAllFiles(file.getAbsolutePath(), fileList);
			}
		}
	}

	/**
	 * @param fileFullPath
	 * @param level        0 = same, 1 = parent, 2 = grandparent, 3 =
	 *                     great-grandparent, ...
	 * @return
	 */
	public static String getParent(String fileFullPath, int level) {
		String parent = fileFullPath;
		try {
			int i = 0;
			while (i < level) {
				File file = new File(parent);
				parent = file.getParent();
				i++;
			}
		} catch (Exception ex) {
			logger.error("Error occurred while getting parent of " + fileFullPath, ex);
		}
		return parent;
	}

	public static boolean copyFile(String src, String dst) {
		FileInputStream fs = null;
		FileOutputStream os = null;
		try {
			fs = new FileInputStream(src);
			int b;
			os = new FileOutputStream(dst);
			while ((b = fs.read()) != -1) {
				os.write(b);
			}
			return true;
		} catch (Exception e) {
			logger.error("File copy operation failed", e);
			return false;
		} finally {
			safeCloseInputStream(fs);
			safeCloseOutputStream(os);
		}
	}

	public static String getFileExtension(String fileName) {
		String fileExtension = "";
		if (fileName != null && fileName.length() > 0) {
			String[] tokens = fileName.split("\\.(?=[^\\.]+$)");
			if (tokens.length > 1) {
				fileExtension = tokens[tokens.length - 1];
			}
		}
		return fileExtension;
	}

	public static JsonObject readJsonAsObject(String jsonFile) {
		InputStream input = null;
		try {
			input = FileUtility.class.getClassLoader().getResourceAsStream(jsonFile);
			if (input == null) {
				logger.info("Sorry, unable to find " + jsonFile);
				return null;
			}

			JsonReader jsonReader = Json.createReader(input);
			JsonObject jsonObject = jsonReader.readObject();

			jsonReader.close();
			return jsonObject;
		} finally {
			safeCloseInputStream(input);
		}
	}

	public static JsonArray readJsonAsArray(String jsonFile) {
		InputStream input = null;
		try {
			input = FileUtility.class.getClassLoader().getResourceAsStream(jsonFile);
			if (input == null) {
				logger.info("Sorry, unable to find " + jsonFile);
				return null;
			}

			JsonReader jsonReader = Json.createReader(input);
			JsonArray jsonArray = jsonReader.readArray();

			jsonReader.close();
			return jsonArray;
		} finally {
			safeCloseInputStream(input);
		}
	}

	public static Properties readProperties(String propertiesFile) {
		InputStream input = null;
		try {
			input = FileUtility.class.getClassLoader().getResourceAsStream(propertiesFile);
			if (input == null) {
				logger.info("Sorry, unable to find " + propertiesFile);
				return null;
			}

			// load a properties file from class path, inside static method
			Properties properties = new Properties();
			try {
				properties.load(input);
			} catch (IOException e) {
				logger.error("Sorry, unable to find " + e);
			}
			return properties;
		} finally {
			safeCloseInputStream(input);
		}
	}

	public static Date getLastModifiedDtm(String fileName) {
		if (fileName == null || fileName.length() == 0) {
			return null;
		}
		if (!fileName.startsWith("/")) {
			fileName = "/" + fileName;
		}
		URL url = FileUtility.class.getResource(cleanPath(fileName));
		Date lastModifiedDtm = null;
		try {
			lastModifiedDtm = new Date(url.openConnection().getLastModified());
		} catch (IOException e) {
			logger.error("Error occurred in getLastModifiedDtm()", e);
		}
		return lastModifiedDtm;
	}

	/**
	 * This method recursively finds all files present in provided <b>path</b> and
	 * matching provided <b>extension</b>. Use <b>prefixPath=True</b> to return
	 * <b>relative path from classpath root</b>.
	 * 
	 * @param pathLocal
	 * @param extension
	 * @return
	 */
	public static List<String> getResourceFilesInPath(String path, String extension, boolean prefixPath) {
		List<String> fileList = new ArrayList<String>();
		String pathLocal = path;
		if (pathLocal == null || pathLocal.length() == 0) {
			return fileList;
		}
		if (!pathLocal.startsWith("/")) {
			pathLocal = "/" + pathLocal;
		}
		String extensionLowerCase = extension.toLowerCase(Locale.ENGLISH);
		InputStream in = null;
		try {
			in = FileUtility.class.getResourceAsStream(pathLocal);
			if (in == null) {
				return fileList;
			}
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));

			String resource;
			while ((resource = reader.readLine()) != null) {
				File file = new File(FileUtility.class.getResource(cleanPath(pathLocal + "/" + resource)).getFile());
				if (file.isDirectory()) {
					fileList.addAll(getResourceFilesInPath(path + "/" + resource, extension, prefixPath));
				} else {
					if (resource.toLowerCase(Locale.ENGLISH).endsWith(extensionLowerCase)) {
						if (prefixPath) {
							fileList.add(path + "/" + resource);
						} else {
							fileList.add(resource);
						}

					}
				}
			}
			reader.close();
		} catch (IOException e) {
			logger.error("Error occurred in readResourceFile()", e);
		} finally {
			safeCloseInputStream(in);
		}
		return fileList;

	}

	public static byte[] readResourceFileAsByte(String fileName) {
		byte[] data = {};
		if (fileName == null || fileName.length() == 0) {
			return data;
		}
		if (!fileName.startsWith("/")) {
			fileName = "/" + fileName;
		}

		FileInputStream fis = null;
		try {
			File file = new File(FileUtility.class.getResource(cleanPath(fileName)).getFile());
			fis = new FileInputStream(file);
			data = new byte[(int) file.length()];
			if (fis.read(data) == -1) {
				logger.error("No data present in file");
			}
			fis.close();
		} catch (IOException e) {
			logger.error("Error occurred while reading file {}", StringUtility.sanitizeReqData(fileName), e);
		} finally {
			try {
				if (fis != null)
					fis.close();
			} catch (IOException ex) {
				logger.error("File close operation failed", ex);
			}
		}
		return data;
	}

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

	public static String getFileMIMEType(String filePath) {
		Path path = new File(cleanPath(filePath)).toPath();
		String mimeType = mimeTypeProperties.getProperty("bin");
		try {
			mimeType = Files.probeContentType(path);
		} catch (IOException e) {
			logger.error("Unable to detect type of file " + path + " - " + e);
		}
		return mimeType;
	}

	public static String getContentType(String filePath, String fileExtension) {
		String contentType = mimeTypeProperties.getProperty(fileExtension);
		if (!StringUtility.hasTrimmedValue(contentType)) {
			contentType = getFileMIMEType(filePath);
		}
		return contentType;
	}

	public static String cleanPath(String path) {
		if (path == null)
			return null;
		String cleanString = "";
		for (int i = 0; i < path.length(); ++i) {
			cleanString += cleanChar(path.charAt(i));
		}
		return cleanString;
	}

	private static char cleanChar(char aChar) {
		// 0 - 9
		for (int i = 48; i < 58; ++i) {
			if (aChar == i)
				return (char) i;
		}
		// 'A' - 'Z'
		for (int i = 65; i < 91; ++i) {
			if (aChar == i)
				return (char) i;
		}
		// 'a' - 'z'
		for (int i = 97; i < 123; ++i) {
			if (aChar == i)
				return (char) i;
		}
		// other valid characters
		switch (aChar) {
		case '/':
			return '/';
		case '.':
			return '.';
		case '-':
			return '-';
		case '_':
			return '_';
		case ':':
			return ':';
		case '\\':
			return '\\';
		case ' ':
			return ' ';
		case '(':
			return '(';
		case ')':
			return ')';
		case '$':
			return '$';
		}
		return '%';
	}

	public static boolean createZipFile(String zipFilePath, List<String> fileList) {
		boolean isZipCreated = false;
		FileOutputStream fileOutputStream = null;
		ZipOutputStream zipOutputStream = null;
		try {
			fileOutputStream = new FileOutputStream(cleanPath(zipFilePath));
			zipOutputStream = new ZipOutputStream(fileOutputStream);
			for (String filePath : fileList) {
				File fileToZip = new File(cleanPath(filePath));
				FileInputStream fileInputStream = null;
				try {
					fileInputStream = new FileInputStream(fileToZip);
					ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
					zipOutputStream.putNextEntry(zipEntry);
					byte[] bytes = new byte[1024];
					int length;
					while ((length = fileInputStream.read(bytes)) >= 0) {
						zipOutputStream.write(bytes, 0, length);
					}
				} catch (IOException e) {
					logger.error("Zip creation failed - " + e);
				} finally {
					safeCloseInputStream(fileInputStream);
				}
			}
			isZipCreated = true;
		} catch (Exception e) {
			logger.error("Zip creation failed - " + e);
		} finally {
			safeCloseOutputStream(zipOutputStream);
			safeCloseOutputStream(fileOutputStream);
		}
		return isZipCreated;
	}

	public static void safeCloseOutputStream(OutputStream outputStream) {
		if (outputStream != null) {
			try {
				outputStream.close();
			} catch (IOException e) {
				logger.error("Error occurred in safeCloseOutputStream method", e);
			}
		}
	}

	public static void safeCloseFileWriter(FileWriter fileWriter) {
		if (fileWriter != null) {
			try {
				fileWriter.close();
			} catch (IOException e) {
				logger.error("Error occurred in safeCloseFileWriter method", e);
			}
		}
	}

	public static void safeCloseInputStream(InputStream inputStream) {
		if (inputStream != null) {
			try {
				inputStream.close();
			} catch (IOException e) {
				logger.error("Error occurred in safeCloseInputStream method", e);
			}
		}
	}
}