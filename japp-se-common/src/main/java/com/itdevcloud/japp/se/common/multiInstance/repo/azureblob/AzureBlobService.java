package com.itdevcloud.japp.se.common.multiInstance.repo.azureblob;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.AppendBlobRequestConditions;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.specialized.AppendBlobClient;
import com.azure.storage.blob.specialized.BlobLeaseClient;
import com.azure.storage.blob.specialized.BlobLeaseClientBuilder;
import com.itdevcloud.japp.se.common.multiInstance.repo.EventManagerConstant;
import com.itdevcloud.japp.se.common.util.DateUtils;
import com.itdevcloud.japp.se.common.util.StringUtil;

public class AzureBlobService {

	private static final Logger logger = Logger.getLogger(AzureBlobService.class.getName());
	protected static DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

	// protected static final String STORAGE_URL =
	// "https://fcsdevappsa04.blob.core.windows.net";
	// protected static final String SAS_TOKEN =
	// "sv=2022-11-02&ss=bfqt&srt=sco&sp=rwdlacupiytfx&se=2025-04-30T23:05:54Z&st=2025-02-20T16:05:54Z&sip=165.85.214.1-165.85.214.198&spr=https&sig=wbgUObn32L9QcA%2BMqyuupi1aDOeADXKd9HEBgQBtNE4%3D";
	protected static final String STORAGE_URL = "https://msunpocblobstorage.blob.core.windows.net/";
	protected static final String SAS_TOKEN = "sv=2022-11-02&ss=bfqt&srt=sco&sp=rwdlacupiytfx&se=2025-05-02T04:40:25Z&st=2025-03-01T21:40:25Z&spr=https&sig=wkLp%2BcOTNkqu5%2F9qvL8dMjiYiCFdpdhfvl7w8o4Hvqs%3D";

	protected static final String DEFAULT_BLOB_CONTAINER_NAME = "mi-event-management";
	protected static final int DEFAULT_BLOB_LEASE_TIMEOUT_SECOND = 30;
	protected static final int DEFAULT_BLOB_LEASE_RETRY_MILLIS = 3000;
	protected static final int DEFAULT_BLOB_LEASE_DURATION_SECOND = 20;

	protected static final String BLOB_TAG_LAST_UPDATED_DATE = "LAST_UPDATED_DATE";

	private String blobContainerName = null;
	private BlobServiceClient blobServiceClient = null;
	private BlobContainerClient blobContainerClient = null;
	
	private boolean initSuccess = false;
	
	private static AzureBlobService instance = null;

	public static AzureBlobService getInstance() {
		if (instance != null) {
			return instance;
		}
		instance = new AzureBlobService(null);
		return instance;
	}

	public static AzureBlobService getInstance(String containerName) {
		if (instance != null) {
			return instance;
		}
		instance = new AzureBlobService(containerName);
		return instance;
	}

	private AzureBlobService(String containerName) {
		init(containerName);
	}

	public boolean init(String containerName) {
		try {
			containerName = StringUtil.isEmptyOrNull(containerName) ? DEFAULT_BLOB_CONTAINER_NAME
					: containerName.trim().toLowerCase();
			logger.info("init() Connecting to Storage Container start...containerName = " + containerName);
			Long startTS = System.currentTimeMillis();

			this.blobContainerName = containerName;
			this.blobServiceClient = new BlobServiceClientBuilder().endpoint(STORAGE_URL).sasToken(SAS_TOKEN)
					.buildClient();

			this.blobContainerClient = this.blobServiceClient.getBlobContainerClient(this.blobContainerName);

			this.blobContainerClient.createIfNotExists();

			Long endTS = System.currentTimeMillis();
			logger.info("init() Connect to Storage Container end.....total millis = " + (endTS - startTS)
					+ ", containerName = " + containerName);
			
			this.initSuccess = true;
			return true;
			
		} catch (Throwable t) {
			logger.severe("Can not init blob client, error: " + t);
			t.printStackTrace();
			this.initSuccess = false;
			return false;
		}

	}

	public boolean isInitSuccess() {
		return initSuccess;
	}

	public String getBlobContainerName() {
		return blobContainerName;
	}

	public List<BlobItem> listAllBlobs(boolean print) {

		logger.fine("listAllBlobs().....Blob Container Name = " + this.blobContainerName);
		Long startTS = System.currentTimeMillis();
		List<BlobItem> blobItemList = new ArrayList<BlobItem>();
		String blobStr = null;
		String tmpStr = null;
		for (BlobItem blobItem : this.blobContainerClient.listBlobs()) {
			blobItemList.add(blobItem);
			long ts = blobItem.getProperties().getLastModified().toInstant().toEpochMilli();
			tmpStr = "Blob name: [" + blobItem.getName() + "], tags: [" + blobItem.getTags()
					+ "], Last Updated Timestamp: " + DateUtils.timestampToDateString(ts, "yyyy-MM-dd HH:mm:ss.SSS");
			blobStr = (blobStr == null ? tmpStr : blobStr + "\n" + tmpStr);
		}
		if (print) {
			logger.info(blobStr);
		}
		int count = blobItemList.size();
		if (count == 0) {
			logger.info("Blob Container is empty. Blob Container Name: " + this.blobContainerName);
		} else {
			logger.info("Total Blob Count in the container: " + count + ", Blob Container Name = "
					+ this.blobContainerName);
		}

		Long endTS = System.currentTimeMillis();
		logger.fine("listAllBlobs()r end.....total millis = " + (endTS - startTS) + ", containerName = "
				+ this.blobContainerName);

		return blobItemList;
	}

	public boolean saveContentToBlob(String blobName, String content, boolean append) {

		if (this.blobContainerClient == null) {
			logger.fine("There is no blobContainerClient established, do nothing!");
			return false;
		}
		if (StringUtil.isEmptyOrNull(blobName)) {
			logger.fine("There is no blob name provided, do nothing!");
			return false;
		}
		blobName = blobName.trim();
		content = StringUtil.isEmptyOrNull(content) ? EventManagerConstant.EVENT_INFO_EMPTY_CONTENT : content;

		logger.fine("save content to Blob start.....blobName = " + blobName + ", content (to be saved) size = "
				+ content.length());

		Long startTS = System.currentTimeMillis();

		BlobClient blobClient = null;
		AppendBlobClient appendBlobClient = null;
		BlobLeaseClient blobLeaseClient = null;
		try {
			// get the blob client
			blobClient = blobContainerClient.getBlobClient(blobName);
			appendBlobClient = blobClient.getAppendBlobClient();
			if (!append) {
				appendBlobClient.deleteIfExists();
				appendBlobClient.create();
			} else {
				appendBlobClient.createIfNotExists();
			}
			if (appendBlobClient.getProperties().getBlobSize() > 0) {
				content = "\n" + content;
			}
			blobLeaseClient = getBlobLease(blobClient);

			if (blobLeaseClient == null || blobLeaseClient.getLeaseId() == null) {
				logger.warning("can not get lease for the blob - " + blobName + ", do nothing...");
				Long endTS = System.currentTimeMillis();
				logger.fine("saveContentToBlob()  end.....total millis = " + (endTS - startTS));
				return false;
			} else {
				// set the lease ID
				AppendBlobRequestConditions requestConditions = new AppendBlobRequestConditions()
						.setLeaseId(blobLeaseClient.getLeaseId());
				byte[] bytes = content.getBytes();
				appendBlobClient.appendBlockWithResponse(new ByteArrayInputStream(bytes), bytes.length, null,
						requestConditions, null, null);
			}
			Long endTS = System.currentTimeMillis();
			logger.fine("saveContentToBlob()  end.....total millis = " + (endTS - startTS));
			return true;
		} catch (Throwable t) {
			logger.severe("saveContentToBlob() failed: " + t.getMessage());
			Long endTS = System.currentTimeMillis();
			logger.fine("saveContentToBlob()  end.....total millis = " + (endTS - startTS));
			return false;
		} finally {
			if (blobLeaseClient != null) {
				try {
					blobLeaseClient.releaseLease();
				} catch (BlobStorageException e) {
				}
			}
		}
	}

	private BlobLeaseClient getBlobLease(BlobClient blobClient) {
		if (blobClient == null) {
			return null;
		}
		BlobLeaseClient blobLeaseClient = new BlobLeaseClientBuilder().blobClient(blobClient).buildClient();
		int wait = DEFAULT_BLOB_LEASE_RETRY_MILLIS;
		int retryCount = (DEFAULT_BLOB_LEASE_TIMEOUT_SECOND * 1000) / wait;
		retryCount = (retryCount <= 0 ? 1 : retryCount);
		for (int i = 0; i < retryCount; i++) {
			if (i != 0) {
				logger.fine("getLeadId()..... wait for " + wait + " millis to retry, retry count = " + i);
				try {
					Thread.sleep(wait);
				} catch (Throwable t) {
				}
			}
			try {
				blobLeaseClient.acquireLease(DEFAULT_BLOB_LEASE_DURATION_SECOND);
			} catch (Throwable t) {
				logger.warning("Failed to acquire lease for the blob: " + blobClient.getBlobName() + ", error = "
						+ t.getMessage());
			}
			if (blobLeaseClient.getLeaseId() != null) {
				return blobLeaseClient;
			}
		}
		return blobLeaseClient.getLeaseId() == null ? null : blobLeaseClient;
	}

	public String getContentFromBlob(String blobName) {

		if (this.blobContainerClient == null) {
			logger.fine("There is no blobContainerClient established, do nothing!");
			return null;
		}
		if (StringUtil.isEmptyOrNull(blobName)) {
			logger.fine("There is no blob name provided, do nothing!");
			return null;
		}
		blobName = blobName.trim();
		String content = null;

		logger.fine("get content from Blob start.....blobName = '" + blobName + "'");

		Long startTS = System.currentTimeMillis();

		BlobClient blobClient = null;
		ByteArrayOutputStream outputStream = null;
		try {
			// get the blob client
			blobClient = blobContainerClient.getBlobClient(blobName);
			outputStream = new ByteArrayOutputStream();

			blobClient.downloadStream(outputStream);

			content = outputStream.toString();

			Long endTS = System.currentTimeMillis();
			logger.fine("getContentFromBlob()  end.....total millis = " + (endTS - startTS));
			return content;
		} catch (Throwable t) {
			logger.warning("getContentFromBlob() failed: " + t.getMessage());
			Long endTS = System.currentTimeMillis();
			logger.fine("getContentFromBlob()  end.....total millis = " + (endTS - startTS));
			return null;
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
					outputStream = null;
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		}
	}

	public long getBlobLastUpdatedTimestamp(String blobName) {

		if (this.blobContainerClient == null) {
			logger.fine("There is no blobContainerClient established, do nothing!");
			return -1;
		}
		if (StringUtil.isEmptyOrNull(blobName)) {
			logger.fine("There is no blob name provided, do nothing!");
			return -1;
		}
		blobName = blobName.trim();

		logger.fine("getBlobLastUpdatedTimestamp start.....blobName = " + blobName);

		Long startTS = System.currentTimeMillis();
		long ts = -1;
		try {
			// get the blob client
			BlobClient blobClient = blobContainerClient.getBlobClient(blobName);
			BlobProperties blobProperties = blobClient.getProperties();
			OffsetDateTime offsetDateTime = blobProperties.getLastModified();
			ts = offsetDateTime.toInstant().toEpochMilli();
			Long endTS = System.currentTimeMillis();
			logger.fine("getBlobLastUpdatedTimestamp()  end.....total millis = " + (endTS - startTS));
			return ts;
		} catch (Throwable t) {
			logger.warning("getBlobLastUpdatedTimestamp .....blobName = " + blobName + ", error = " + t);
			Long endTS = System.currentTimeMillis();
			logger.fine("getBlobLastUpdatedTimestamp()  end.....total millis = " + (endTS - startTS));
			return -1;
		}
	}
}
