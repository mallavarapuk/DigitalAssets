package com.mss.sample;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;

@Service
@RestController
public class DigitalAssetsDao {

	public String bucketName = "miracle-automation-portal";

	@GetMapping("/getDetailsOfFoldersAndFiles")
	public Map getDetailsOfFoldersAndFiles(String objectKey) {
		Map response = new HashMap();
		response.put("success", false);
		response.put("message", "Unable to connect s3 bucket");
		response.put("data", "");
		long totalSize = 0;
		Date date1 = new Date();
		Date date2;
		Date lastModifiesDate = new Date();

		Map foldersMap = new HashMap();
		try {
			if (objectKey != null && !"".equalsIgnoreCase(objectKey)) {
				AmazonS3 s3client = AmazonS3ClientBuilder.defaultClient();
				ListObjectsV2Request listObjectsRequest = new ListObjectsV2Request().withBucketName(bucketName)
						.withPrefix(objectKey + "/");
				ListObjectsV2Result objects = s3client.listObjectsV2(listObjectsRequest);
				List<S3ObjectSummary> summaries = objects.getObjectSummaries();
				File file = new File(objectKey);
				String key = file.getName();
				SimpleDateFormat sdfo = new SimpleDateFormat("yyyy-MM-dd");

				if (key.contains(".")) {

					for (S3ObjectSummary objectSummary : summaries) {

						System.out.println(objectSummary.getSize());

						System.out.println(objectSummary.getKey());
					}
				} else {
					int i = 0;
					for (S3ObjectSummary objectSummary : summaries) {
						totalSize = totalSize += objectSummary.getSize();
						System.out.println(objectSummary.getLastModified());
						if (i == 0) {
							date1 = objectSummary.getLastModified();
//							lastModifiesDate = date1;
						} else {
							date2 = objectSummary.getLastModified();

							if (date2.compareTo(date1) > 0) {
								date1 = date2;
//								/System.out.println(date1);
							}
//							if (date1.after(date2)) {
//								date2 = date1;
//								lastModifiesDate = date1;
//							}
						}

					}

					System.out.println("lastModifiesDate===>" + date1);

					foldersMap.put("size", totalSize);
					foldersMap.put("items", summaries.size());
					foldersMap.put("location", objectKey);
					foldersMap.put("modified", "");
//					System.out.println(totalSize);
//					System.out.println(summaries.size());
				}
			} else {
				response.put("message", "Please give object key!");
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.put("message", "Sorry! Unable to process this request");
		}

		return response;
	}

	public Map getFoldersAndFiles(String folderKey) {
		Map response = new HashMap();
		List<String> listOfObjectKeys = new ArrayList();
		Map objectMap = new HashMap();
		response.put("success", false);
		response.put("message", "Unable to connect s3 bucket");
		response.put("data", objectMap);
		List mainFoldersList = new ArrayList();
		List duplicateFolders = new ArrayList();
		List mainFilesList = new ArrayList();

		Map mainFilesMap = new HashMap();
		Map mainFolderMap = null;

		long totalSize = 0;
		long totalItems = 0;
		try {
			AmazonS3 s3client = AmazonS3ClientBuilder.defaultClient();
			ListObjectsV2Request listObjectsRequest;
			if (folderKey.length() > 0 && folderKey != null) {
				listObjectsRequest = new ListObjectsV2Request().withBucketName(bucketName).withPrefix(folderKey + "/");
			} else {
				listObjectsRequest = new ListObjectsV2Request().withBucketName(bucketName).withPrefix(folderKey);
			}

			ListObjectsV2Result objects = s3client.listObjectsV2(listObjectsRequest);
			List<S3ObjectSummary> summaries = objects.getObjectSummaries();

			if (summaries.size() > 0) {
				for (S3ObjectSummary objectSummary : summaries) {
					mainFolderMap = new HashMap();
					mainFilesMap = new HashMap();
					String originalKey = objectSummary.getKey();
					if (originalKey.startsWith("/")) {
						if (folderKey.length() > 0) {
							String originalName = originalKey.substring(folderKey.length() + 1, originalKey.length());
							String[] folders = originalName.split("/");
							if (!duplicateFolders.contains(folders[0])) {
								duplicateFolders.add(folders[0]);
								Map result = getSubFoldersAndFiles(folderKey + "/" + folders[0]);
								mainFolderMap.put("path", folderKey + "/" + folders[0]);
								mainFolderMap.put("folderName", folders[0]);
								mainFolderMap.put("size", calculateSize((long) result.get("totalSize")));
								mainFolderMap.put("items", result.get("totalItems"));
								mainFolderMap.put("subFolders", result.get("subFolders"));
								mainFolderMap.put("subFiles", result.get("subFiles"));
								mainFoldersList.add(mainFolderMap);
							}
						} else {
							// with out folder key
							String formatObjectKeys = originalKey.substring(1, originalKey.length() - 1);
							String[] folders = formatObjectKeys.split("/");
							if (!duplicateFolders.contains(folders[0])) {
								duplicateFolders.add(folders[0]);
								Map result = getSubFoldersAndFiles("/" + folders[0]);
								mainFolderMap.put("path", "/" + folders[0]);
								mainFolderMap.put("folderName", folders[0]);
								mainFolderMap.put("size", calculateSize((long) result.get("totalSize")));
								mainFolderMap.put("items", result.get("totalItems"));
								mainFolderMap.put("subFolders", result.get("subFolders"));
								mainFolderMap.put("subFiles", result.get("subFiles"));
								mainFoldersList.add(mainFolderMap);
							}
						}
					} else if (originalKey.indexOf("/") < 0) {
						if (originalKey.contains(".")) {
							System.out.println(objectSummary.getKey());
							mainFilesMap.put("filePath", objectSummary.getKey());
							mainFilesMap.put("fileSize", calculateSize((long) objectSummary.getSize()));
							mainFilesMap.put("fileName", originalKey);
							mainFilesMap.put("fileType", FilenameUtils.getExtension(originalKey));
							mainFilesList.add(mainFilesMap);
							System.out.println();
						} else {
							if (!duplicateFolders.contains(originalKey)) {
								duplicateFolders.add(originalKey);
								Map result = getSubFoldersAndFiles(originalKey);
								mainFolderMap.put("path", originalKey);
								mainFolderMap.put("folderName", originalKey);
								mainFolderMap.put("size", calculateSize((long) result.get("totalSize")));
								mainFolderMap.put("items", result.get("totalItems"));
								mainFolderMap.put("subFolders", result.get("subFolders"));
								mainFolderMap.put("subFiles", result.get("subFiles"));
								mainFoldersList.add(mainFolderMap);
							}
						}
					} else {
						if (folderKey.length() > 0) {
							String originalName = originalKey.substring(folderKey.length() + 1, originalKey.length());
							if (originalName.indexOf("/") < 0) {
								if (originalName.contains(".")) {
									mainFilesMap.put("filePath", objectSummary.getKey());
									mainFilesMap.put("fileSize", calculateSize((long) objectSummary.getSize()));
									mainFilesMap.put("fileName", originalName);
									mainFilesMap.put("fileType", FilenameUtils.getExtension(originalName));
									mainFilesList.add(mainFilesMap);
								} else {
									if (!duplicateFolders.contains(originalName)) {
										duplicateFolders.add(originalName);
										Map result = getSubFoldersAndFiles(originalKey);
										mainFolderMap.put("path", originalKey);
										mainFolderMap.put("folderName", originalName);
										mainFolderMap.put("size", calculateSize((long) result.get("totalSize")));
										mainFolderMap.put("items", result.get("totalItems"));
										mainFolderMap.put("subFolders", result.get("subFolders"));
										mainFolderMap.put("subFiles", result.get("subFiles"));
										mainFoldersList.add(mainFolderMap);
									}
								}
							} else {
								String[] folders = originalName.split("/");
								if (!duplicateFolders.contains(folders[0])) {
									duplicateFolders.add(folders[0]);
									Map result = getSubFoldersAndFiles(folderKey + "/" + folders[0]);
									mainFolderMap.put("folderName", folders[0]);
									mainFolderMap.put("path", folderKey + "/" + folders[0]);
									mainFolderMap.put("size", calculateSize((long) result.get("totalSize")));
									mainFolderMap.put("items", result.get("totalItems"));
									mainFolderMap.put("subFolders", result.get("subFolders"));
									mainFolderMap.put("subFiles", result.get("subFiles"));
									mainFoldersList.add(mainFolderMap);
								}
							}

						} else {
							String[] folders = originalKey.split("/");
							if (!duplicateFolders.contains(folders[0])) {
								duplicateFolders.add(folders[0]);
								Map result = getSubFoldersAndFiles(folders[0]);
								mainFolderMap.put("path", folders[0]);
								mainFolderMap.put("folderName", folders[0]);
								mainFolderMap.put("size", calculateSize((long) result.get("totalSize")));
								mainFolderMap.put("items", result.get("totalItems"));
								mainFolderMap.put("subFolders", result.get("subFolders"));
								mainFolderMap.put("subFiles", result.get("subFiles"));
								mainFoldersList.add(mainFolderMap);
							}
						}
					}

					totalSize += objectSummary.getSize();
					totalItems += 1;

				}

				objectMap.put("MainFolders", mainFoldersList);
				objectMap.put("MainFiles", mainFilesList);
				objectMap.put("TotalSize", calculateSize(totalSize));
				objectMap.put("TotalItems", totalItems);

				response.put("success", true);
				response.put("message", "Successfully retrieved the data");
				response.put("data", objectMap);
			} else {
				response.put("message", "No files in the Bucket");
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.put("message", "Sorry! Unable to process this request right now");
		}
		return response;
	}

	public String calculateSize(long size) {
		float totalSize = 0;
		String result = "0.0 KB";

		if (size >= 1024 && size < 1048576) {
			totalSize = size / 1024;
			result = String.valueOf(totalSize) + " KB";
		} else if (size >= 1048576) {
			totalSize = size / 1048576;
			result = String.valueOf(totalSize) + " MB";
		}
		return result;
	}

	private Map getSubFoldersAndFiles(String objectKey) {
		// TODO Auto-generated method stub
		Map result = new HashMap();
		long totalSize = 0;
		int totalItems = 0;
		List subFoldersList = new ArrayList();
		List subFilesList = new ArrayList();
		List duplicateList = new ArrayList();

		Map subFilesMap = new HashMap();
		AmazonS3 s3client = AmazonS3ClientBuilder.defaultClient();
		ListObjectsV2Request listObjectsRequest;
		if (objectKey.length() > 0 && objectKey != null) {
			listObjectsRequest = new ListObjectsV2Request().withBucketName(bucketName).withPrefix(objectKey + "/");
		} else {
			listObjectsRequest = new ListObjectsV2Request().withBucketName(bucketName).withPrefix(objectKey);
		}

		ListObjectsV2Result objects = s3client.listObjectsV2(listObjectsRequest);
		List<S3ObjectSummary> summaries = objects.getObjectSummaries();

		for (S3ObjectSummary objectSummer : summaries) {
			subFilesMap = new HashMap();
			String originalName = objectSummer.getKey();
			String subFolders = originalName.substring(objectKey.length() + 1, originalName.length());
			String[] subfolder = subFolders.split("/");
			if (subfolder[0].contains(".")) {
				subFilesMap.put("filePath", objectSummer.getKey());
				subFilesMap.put("fileSize", calculateSize(objectSummer.getSize()));
				subFilesMap.put("fileName", subfolder[0]);
				subFilesMap.put("fileType", FilenameUtils.getExtension(originalName));
				subFilesList.add(subFilesMap);
			} else if (!duplicateList.contains(subfolder[0])) {
				duplicateList.add(subfolder[0]);
				subFoldersList.add(subfolder[0]);
			}
			totalSize += objectSummer.getSize();
		}
		result.put("totalSize", totalSize);
		result.put("totalItems", summaries.size());
		result.put("subFolders", subFoldersList);
		result.put("subFiles", subFilesList);

		return result;
	}

}
