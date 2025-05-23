/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.datasource.impl.filesystem.basic.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.infosys.ainauto.commonutils.FileUtility;
import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.datasource.api.IDataSourceReader;
import com.infosys.ainauto.datasource.common.DataSourceException;
import com.infosys.ainauto.datasource.config.DataSourceConfig;
import com.infosys.ainauto.datasource.config.file.FileSystemDataSourceReaderConfig;
import com.infosys.ainauto.datasource.impl.filesystem.basic.common.FileSystemDataSourceConstants;
import com.infosys.ainauto.datasource.model.DataSourceFolder;
import com.infosys.ainauto.datasource.model.DataSourceRecord;
import com.infosys.ainauto.datasource.model.file.FileSystemDataSourceRecord;

public class FileSystemDataSourceReader implements IDataSourceReader {

	private final static Logger logger = LoggerFactory.getLogger(FileSystemDataSourceReader.class);
	private String name;
	private FileSystemDataSourceReaderConfig fileSystemDataSourceReaderConfig;
	private File sourceDirectory;
	private File archivalDirectory;
	private File tempDirectory;
	private Path batchFolderPath = null;
	private Map<String, FileSystemDataSourceRecord> fileIdToFileDsrMap = null;
	private List<String> zipExtensionList;

	public FileSystemDataSourceReader(String name, DataSourceConfig dataSourceReaderConfig) {
		this.name = name;
		this.fileSystemDataSourceReaderConfig = (FileSystemDataSourceReaderConfig) dataSourceReaderConfig;
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * @Description this method is used to connect to source dir
	 */
	@Override
	public boolean connect() throws DataSourceException {
		boolean operationResult = false; // Assume operation will fail
		try {

			String sourceDirectoryPath = fileSystemDataSourceReaderConfig.getFileSourceDir();
			sourceDirectory = new File(sourceDirectoryPath);

			String archivalDirectoryPath = fileSystemDataSourceReaderConfig.getFileArchivalDir();
			archivalDirectory = new File(archivalDirectoryPath);

			String tempDirectoryPath = fileSystemDataSourceReaderConfig.getFileTempDir();
			tempDirectory = new File(tempDirectoryPath);

			zipExtensionList = new ArrayList<>();
			zipExtensionList.add("zip");
			zipExtensionList.add("7z");
			zipExtensionList.add("rar");

		} catch (Exception e) {
			logger.error("Error occurred in connect method", e);
			if (fileSystemDataSourceReaderConfig.isOnConnectionErrorIgnore()) {
				return operationResult;
			}
			throw new DataSourceException("Error occurred in connect method", e);
		}

		if (sourceDirectory.exists()) {
			logger.info("Connected to source directory: " + sourceDirectory.getAbsolutePath());
		} else {
			logger.error("Source directory does not exist: " + sourceDirectory.getAbsolutePath());
			if (fileSystemDataSourceReaderConfig.isOnConnectionErrorIgnore()) {
				return operationResult;
			}
			throw new DataSourceException("Source directory does not exist: " + sourceDirectory.getAbsolutePath());

		}

		if (archivalDirectory.exists()) {
			logger.info("Connected to archival directory: " + archivalDirectory.getAbsolutePath());
		} else {
			logger.error("Archival directory does not exist: " + archivalDirectory.getAbsolutePath());
			if (fileSystemDataSourceReaderConfig.isOnConnectionErrorIgnore()) {
				return operationResult;
			}
			throw new DataSourceException("Archival directory does not exist: " + archivalDirectory.getAbsolutePath());

		}

		if (tempDirectory.exists()) {
			logger.info("Connected to temp directory: " + tempDirectory.getAbsolutePath());
		} else {
			logger.error("Temp directory does not exist: " + tempDirectory.getAbsolutePath());
			if (fileSystemDataSourceReaderConfig.isOnConnectionErrorIgnore()) {
				return operationResult;
			}
			throw new DataSourceException("Temp directory does not exist: " + tempDirectory.getAbsolutePath());

		}

		operationResult = true;
		return operationResult;
	}

	@Override
	public List<DataSourceRecord> getNewItems() throws DataSourceException {
		List<DataSourceRecord> dataSourceRecordList = new ArrayList<>();

		if (sourceDirectory == null || archivalDirectory == null || tempDirectory == null) {
			if (fileSystemDataSourceReaderConfig.isOnConnectionErrorIgnore()) {
				return dataSourceRecordList;
			}
			throw new DataSourceException("A folder needs to be opened first by calling connect() method.");
		}

		List<DataSourceRecord> tempDataSourceRecordList = new ArrayList<>();

		// Create new Map every time for new files call
		fileIdToFileDsrMap = new HashMap<String, FileSystemDataSourceRecord>();

		try (Stream<Path> paths = Files.walk(Paths.get(sourceDirectory.getAbsolutePath()))
				.filter(Files::isRegularFile)) {

			for (Path path : paths.collect(Collectors.toList())) {
				FileSystemDataSourceRecord fileSystemDataSourceRecord = new FileSystemDataSourceRecord();
				String uniqueKey = generateFileId();
				fileSystemDataSourceRecord.setDataSourceRecordId(uniqueKey);
				String fileName = path.getFileName().toString();
				String fileAbsolutePath = path.toString();
				String fileSubPath = getSubPath(fileAbsolutePath, sourceDirectory.getAbsolutePath(), fileName);
				String subFolder = fileSubPath;
				if (fileSubPath != null && fileSubPath.length() > 0) {
					int subFolderIndex = fileSubPath.indexOf(File.separator);
					if (subFolderIndex != -1) {
						subFolder = fileSubPath.substring(0, subFolderIndex);
						String remainingFolder = fileSubPath.substring(subFolderIndex + 1);
						if (remainingFolder != null && remainingFolder.length() > 0) {
							fileName = remainingFolder + FileSystemDataSourceConstants.FILE_SEPARATOR + fileName;
						}
					}
				}

				fileSystemDataSourceRecord.setFileName(fileName);
				fileSystemDataSourceRecord.setFileAbsolutePath(fileAbsolutePath);
				fileSystemDataSourceRecord.setFileSubPath(subFolder);

				BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
				fileSystemDataSourceRecord.setBasicFileAttributes(attr);

				String fileExtension = getFileExtension(fileName).toLowerCase();
				if (zipExtensionList.contains(fileExtension)) {
					fileSystemDataSourceRecord.setHasChildren(true);
					fileSystemDataSourceRecord.setFileSystemDataSourceRecordList(
							recursiveUnzip(fileSystemDataSourceRecord, tempDirectory.getAbsolutePath()));
				}

				tempDataSourceRecordList.add(fileSystemDataSourceRecord);
			}
			if (tempDataSourceRecordList.size() > 0) {

				Map<String, List<FileSystemDataSourceRecord>> childFilesMap = new HashMap<>();
				Map<String, DataSourceRecord> childFolderMap = new HashMap<>();
				Map<String, List<String>> subFolderChildFolderMap = new HashMap<>();

				for (DataSourceRecord dataSourceRecord : tempDataSourceRecordList) {
					if (dataSourceRecord instanceof FileSystemDataSourceRecord) {
						FileSystemDataSourceRecord fileSystemDataSourceRecord = (FileSystemDataSourceRecord) dataSourceRecord;
						String subPath = fileSystemDataSourceRecord.getFileSubPath();
						if (subPath != null && subPath.length() > 0) {
							String fileName = fileSystemDataSourceRecord.getFileName();
							// Child Folders inside subFolder.
							if (fileName.contains(FileSystemDataSourceConstants.FILE_SEPARATOR)) {
								String childFolder = fileName
										.split(Pattern.quote(FileSystemDataSourceConstants.FILE_SEPARATOR))[0];
								String key = subPath + FileSystemDataSourceConstants.FILE_SEPARATOR + childFolder;

								// Grouping files inside childFolder
								{
									List<FileSystemDataSourceRecord> existingSourceRecordList = childFilesMap.get(key);
									if (existingSourceRecordList == null) {
										existingSourceRecordList = new ArrayList<>();
									}
									if (zipExtensionList.contains(getFileExtension(fileName).toLowerCase())) {
										for (FileSystemDataSourceRecord subFileSystemDataSourceRecord : fileSystemDataSourceRecord
												.getFileSystemDataSourceRecordList()) {
											existingSourceRecordList.add(subFileSystemDataSourceRecord);
										}
									} else {
										existingSourceRecordList.add(fileSystemDataSourceRecord);
									}
									childFilesMap.put(key, existingSourceRecordList);
								}

								// To map unique child folders
								{
									FileSystemDataSourceRecord existingFileSystemDataSourceRecord = (FileSystemDataSourceRecord) childFolderMap
											.get(key);
									if (existingFileSystemDataSourceRecord == null) {
										existingFileSystemDataSourceRecord = new FileSystemDataSourceRecord();
										existingFileSystemDataSourceRecord.setBasicFileAttributes(
												fileSystemDataSourceRecord.getBasicFileAttributes());
										existingFileSystemDataSourceRecord.setDataSourceRecordId(
												fileSystemDataSourceRecord.getDataSourceRecordId());
										String fileAbsolutePath = fileSystemDataSourceRecord.getFileAbsolutePath()
												.substring(0, fileSystemDataSourceRecord.getFileAbsolutePath()
														.indexOf(childFolder) + childFolder.length());
										existingFileSystemDataSourceRecord.setFileAbsolutePath(fileAbsolutePath);
										existingFileSystemDataSourceRecord.setFileName(childFolder);
										existingFileSystemDataSourceRecord.setFileSubPath(subPath);
										childFolderMap.put(key, existingFileSystemDataSourceRecord);
									}
								}

								// Mapping subFolder & childFolder logic
								{
									List<String> childFolders = subFolderChildFolderMap.get(subPath);
									if (childFolders == null) {
										childFolders = new ArrayList<>();
									}
									if (!childFolders.contains(childFolder)) {
										childFolders.add(childFolder);
									}
									subFolderChildFolderMap.put(subPath, childFolders);
								}

							} else {
								// No Child folders inside subFolder.
								dataSourceRecordList.add(fileSystemDataSourceRecord);
								fileIdToFileDsrMap.put(fileSystemDataSourceRecord.getDataSourceRecordId(),
										fileSystemDataSourceRecord);
							}
						} else {
							// No subFolder inside root folder.
							dataSourceRecordList.add(fileSystemDataSourceRecord);
							fileIdToFileDsrMap.put(fileSystemDataSourceRecord.getDataSourceRecordId(),
									fileSystemDataSourceRecord);
						}
					}
				}

				for (String subPath : subFolderChildFolderMap.keySet()) {
					List<String> childFolders = subFolderChildFolderMap.get(subPath);
					for (String childFolder : childFolders) {
						String key = subPath + FileSystemDataSourceConstants.FILE_SEPARATOR + childFolder;
						FileSystemDataSourceRecord existingFileSystemDataSourceRecord = (FileSystemDataSourceRecord) childFolderMap
								.get(key);
						existingFileSystemDataSourceRecord.setHasChildren(true);
						existingFileSystemDataSourceRecord.setFileSystemDataSourceRecordList(childFilesMap.get(key));
						dataSourceRecordList.add(existingFileSystemDataSourceRecord);
						fileIdToFileDsrMap.put(existingFileSystemDataSourceRecord.getDataSourceRecordId(),
								existingFileSystemDataSourceRecord);
					}
				}

				// Get batch folder name
				batchFolderPath = getBatchFolderName(archivalDirectory.toString());
			}
			logger.debug("File list size is :" + dataSourceRecordList.size());
		} catch (IOException e) {
			logger.error("Error occurred in getNewFiles method", e);
			throw new DataSourceException("Error occurred in getNewFiles method", e);
		}
		return dataSourceRecordList;
	}

	/**
	 * @Description this method is use to update particular ticket as read
	 * @param uId
	 */
	@Override
	public boolean updateItemAsRead(String dataSourceRecordId) throws DataSourceException {
		boolean operationResult = false; // Assume operation will fail
		Path sourceFilePath = null;
		Path destinationFilePath = null;

		try {
			String archivalDir = Paths
					.get(archivalDirectory.getAbsolutePath(), batchFolderPath.getFileName().toString()).toString();

			FileSystemDataSourceRecord fileSystemDataSourceRecord = fileIdToFileDsrMap.get(dataSourceRecordId);
			if (fileSystemDataSourceRecord != null) {
				sourceFilePath = Paths.get(fileSystemDataSourceRecord.getFileAbsolutePath());

				if (fileSystemDataSourceRecord.getFileSubPath().length() > 0) {
					destinationFilePath = Paths.get(archivalDir, fileSystemDataSourceRecord.getFileSubPath(),
							fileSystemDataSourceRecord.getFileName());
					Path destinationSubFolderPath = Paths.get(archivalDir, fileSystemDataSourceRecord.getFileSubPath());
					Files.createDirectories(destinationSubFolderPath);
					Files.move(sourceFilePath, destinationFilePath);

					// If sub-folder in source is empty, then delete it
					List<String> permanentSubFolders = Arrays
							.asList(fileSystemDataSourceReaderConfig.getFileSourcePermanentSubDir().split(","));

					String subfolderName = fileSystemDataSourceRecord.getFileSubPath();
					String[] innerFolders = subfolderName.split(Pattern.quote(File.separator));
					boolean skipDelete = true;
					String srcDir = sourceDirectory.getAbsolutePath();
					String subDir = fileSystemDataSourceRecord.getFileSubPath();
					if (innerFolders.length == 2 && !permanentSubFolders.contains(innerFolders[1])) {
						skipDelete = false;
					} else if (innerFolders.length > 2) {
						skipDelete = false;
						if (permanentSubFolders.contains(innerFolders[1])) {
							srcDir = srcDir + "/" + innerFolders[1];
							subDir = subDir.substring(("\\" + innerFolders[1]).length());
						}
					}
					if (!skipDelete) {
						Path sourceSubFolderPath = Paths.get(srcDir, subDir);
						if (sourceSubFolderPath.toFile().list().length == 0) {
							if (!sourceSubFolderPath.toFile().delete()) {
								logger.error("Not able to delete file");
							}
						}
					}

				} else {
					destinationFilePath = Paths.get(archivalDir, fileSystemDataSourceRecord.getFileName());
					Files.move(sourceFilePath, destinationFilePath);
				}

				operationResult = true;
				// Remove from map
				fileIdToFileDsrMap.remove(dataSourceRecordId);
			}
		} catch (Exception ex) {
			logger.error("Error occurred while updating file as read", ex);
		}

		return operationResult;
	}

	/**
	 * @Description this method to disconnect or close the connection
	 */
	@Override
	public boolean disconnect() {
		sourceDirectory = null;
		archivalDirectory = null;
		tempDirectory = null;
		return true;
	}

	private String generateFileId() {
		return UUID.randomUUID().toString();
	}

	private String getSubPath(String filePath, String parentPath, String fileName) {
		String subPath = "";
		try {
			// Convert to Path objects to handle slashes as per OS
			Path filePathPath = Paths.get(filePath);
			Path parentPathPath = Paths.get(parentPath);

			Path relativePath = parentPathPath.relativize(filePathPath);
			if (relativePath.getParent() != null) {
				subPath = relativePath.getParent().toString();
			}
		} catch (Exception ex) {
			logger.error("Error occurred in getSubPath method", ex);
		}

		return subPath;
	}

	private Path getBatchFolderName(String directoryPath) throws DataSourceException {
		Path batchFolderPath = null;
		long startNum = 10000;
		try {

			while (true) {
				Path tempBatchFolderPath = Paths.get(directoryPath, String.valueOf(startNum));
				if (!tempBatchFolderPath.toFile().exists()) {
					batchFolderPath = Files.createDirectories(tempBatchFolderPath);
					break;
				} else {
					startNum++;
				}
			}
		} catch (Exception ex) {
			logger.error("Error occurred while creating bacth folder name", ex);
			throw new DataSourceException("Error occurred while creating bacth folder name", ex);
		}

		return batchFolderPath;
	}

	@Override
	public boolean updateItemAsUnread(String dataSourceRecordId) throws DataSourceException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean openFolder(DataSourceFolder dataSourceFolder) throws DataSourceException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean closeFolder() throws DataSourceException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean createFolder(DataSourceFolder dataSourceFolder) throws DataSourceException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteFolder(DataSourceFolder dataSourceFolder) throws DataSourceException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int moveItemToFolder(List<String> dataSourceRecordIdList, DataSourceFolder sourceDataSourceFolder,
			DataSourceFolder targetDataSourceFolder) throws DataSourceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int copyItemToFolder(List<String> dataSourceRecordIdList, DataSourceFolder sourceDataSourceFolder,
			DataSourceFolder targetDataSourceFolder) throws DataSourceException {
		// TODO Auto-generated method stub
		return 0;
	}

	private String getFileExtension(String fileName) {
		String fileExtension = "";
		if (fileName != null && fileName.length() > 0) {
			String[] tokens = fileName.split("\\.(?=[^\\.]+$)");
			if (tokens.length > 1) {
				fileExtension = tokens[tokens.length - 1];
			}
		}
		return fileExtension;
	}

	// unzip files from zip file.
	private List<FileSystemDataSourceRecord> unzip(FileSystemDataSourceRecord fileSystemDataSourceRecord,
			String destinationDirectory) {
		File directory = new File(destinationDirectory);
		// create output directory if it doesn't exist
		if (!directory.exists())
			directory.mkdirs();
		FileInputStream fileInputStream = null;
		ZipInputStream zipInputStream = null;
		List<FileSystemDataSourceRecord> fileSystemDataSourceRecordList = new ArrayList<>();
		// buffer for read and write data to file
		byte[] buffer = new byte[1024];
		try {
			String zipName = fileSystemDataSourceRecord.getFileName();
			fileInputStream = new FileInputStream(fileSystemDataSourceRecord.getFileAbsolutePath());
			zipInputStream = new ZipInputStream(fileInputStream);
			ZipEntry zipEntry = zipInputStream.getNextEntry();
			while (zipEntry != null) {
				String zipEntryName = FileUtility.cleanPath(zipEntry.getName());
				FileSystemDataSourceRecord subFileSystemDataSourceRecord = new FileSystemDataSourceRecord();
				String logicalName = zipName + File.separator + zipEntryName;
				subFileSystemDataSourceRecord.setFileName(zipName + FileSystemDataSourceConstants.FILE_SEPARATOR
						+ zipEntryName.replace("/", FileSystemDataSourceConstants.FILE_SEPARATOR));
				String fileName = generateFileId() + "." + getFileExtension(logicalName);
				File newFile = new File(destinationDirectory + File.separator + fileName);
				// create directories for sub directories in zip
				new File(newFile.getParent()).mkdirs();
				FileOutputStream fileOutputStream = null;
				try {
					fileOutputStream = new FileOutputStream(newFile);
					int length;
					while ((length = zipInputStream.read(buffer)) > 0) {
						fileOutputStream.write(buffer, 0, length);
					}
				} catch (IOException e) {
				} finally {
					FileUtility.safeCloseOutputStream(fileOutputStream);
				}
				if (StringUtility.hasValue(FileUtility.getFileExtension(zipEntryName))) {
					subFileSystemDataSourceRecord.setFileAbsolutePath(newFile.getAbsolutePath());
					subFileSystemDataSourceRecord.setTempFile(true);
					subFileSystemDataSourceRecord
							.setBasicFileAttributes(Files.readAttributes(newFile.toPath(), BasicFileAttributes.class));
					fileSystemDataSourceRecordList.add(subFileSystemDataSourceRecord);
				}
				// close this ZipEntry
				zipInputStream.closeEntry();
				zipEntry = zipInputStream.getNextEntry();
			}
			// close last ZipEntry
			zipInputStream.closeEntry();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			FileUtility.safeCloseInputStream(fileInputStream);
			FileUtility.safeCloseInputStream(zipInputStream);
		}
		return fileSystemDataSourceRecordList;
	}

	// To process n levels & n number of zip files inside single zip file.
	private List<FileSystemDataSourceRecord> recursiveUnzip(FileSystemDataSourceRecord fileSystemDataSourceRecord,
			String destDir) {
		List<FileSystemDataSourceRecord> fileSystemDataSourceRecordList = unzip(fileSystemDataSourceRecord, destDir);
		for (FileSystemDataSourceRecord subFileSystemDataSourceRecord : fileSystemDataSourceRecordList) {
			String fileExtension = getFileExtension(subFileSystemDataSourceRecord.getFileName()).toLowerCase();
			if (zipExtensionList.contains(fileExtension)) {
				List<FileSystemDataSourceRecord> subFileSystemDataSourceRecordList = recursiveUnzip(
						subFileSystemDataSourceRecord, destDir);
				fileSystemDataSourceRecordList = fileSystemDataSourceRecordList.stream()
						.filter(data -> data != subFileSystemDataSourceRecord).collect(Collectors.toList());
				fileSystemDataSourceRecordList.addAll(subFileSystemDataSourceRecordList);
			}
		}
		return fileSystemDataSourceRecordList;
	}
}
