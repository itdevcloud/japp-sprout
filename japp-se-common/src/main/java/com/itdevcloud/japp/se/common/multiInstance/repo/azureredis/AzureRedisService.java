package com.itdevcloud.japp.se.common.multiInstance.repo.azureredis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import com.itdevcloud.japp.se.common.multiInstance.repo.EventManagerConstant;
import com.itdevcloud.japp.se.common.util.StringUtil;

import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

public class AzureRedisService {

	private static final Logger logger = Logger.getLogger(AzureRedisService.class.getName());

	public static final String KEY_CONTENT_SEPARATOR = "§§";

	public static final String REDIS_HOST_NAME = "MI-Msg-Event.redis.cache.windows.net";
	public static final int REDIS_PORT = 6380;
	public static final int REDIS_TIMEOUT_MILLIS = 20000;
	public static final String REDIS_ACCESS_KEY = "Fw0A3yovtW7DoUcjXRgKkXIXPDeMPAIy3AzCaElsgVM=";
	
	public static final Boolean REDIS_ENABLE_SSL = true;
	public static final int REDIS_MAX_VALUE_LENGTH_PER_KEY = 300;
	public static final int REDIS_CHECK_INTERVAL_MILLIS = 5000;
	public static final int REDIS_CONNECTION_RETRY_INTERVAL_MINUTES = 1;

	private static AzureRedisService instance = null;

	private Jedis jedis = null;

	public static AzureRedisService getInstance(String appName) {
		if (instance != null) {
			return instance;
		}
		instance = new AzureRedisService(appName);
		return instance;
	}

	private AzureRedisService(String appName) {
		super();
		reset(appName);
	}

	protected void closeRedis() {
		try {
			if (this.jedis != null) {
				this.jedis.close();
				logger.info("exisitng redis connection is closed...");
			}
		} catch (Throwable t) {
			logger.warning("Close redis conenction error: " + t);
		} finally {
			this.jedis = null;
		}

	}

	private void connectRedis() {
		// Connect to the Azure Cache for Redis over the TLS/SSL port using the key.
		try {
			logger.info("connectRedis() start.....");
			Long startTS = System.currentTimeMillis();
			closeRedis();
			this.jedis = new Jedis(REDIS_HOST_NAME, REDIS_PORT,
					DefaultJedisClientConfig.builder().password(REDIS_ACCESS_KEY).ssl(REDIS_ENABLE_SSL)
							.timeoutMillis(REDIS_TIMEOUT_MILLIS).socketTimeoutMillis(REDIS_TIMEOUT_MILLIS).build());
			Long endTS = System.currentTimeMillis();
			logger.info("Redis connection is established.....total millis = " + (endTS - startTS));
		} catch (Throwable t) {
			this.jedis = null;
			logger.severe("Redis connection can not be established.....error: " + t);
		}
	}

	public void reset(String appName) {
		if (appName == null || (appName = appName.trim()).isEmpty()) {
			throw new RuntimeException("Must provide APP Name when creating JwtBlacklistService instance!");
		}
		connectRedis();
	}

	protected boolean isConnected() {
		boolean isConnected = this.jedis == null ? false : true;
		return isConnected;
	}

	private boolean cleanContentInRedis(String keyPrefix, int startKeyCount) {
		if (this.jedis == null) {
			logger.fine("There is no Redis connection established, do nothing!");
			return false;
		}
		if (StringUtil.isEmptyOrNull(keyPrefix)) {
			logger.fine("There is no key prefix provided, do nothing!");
			return false;
		}
		logger.fine("cleanContentInRedis() start.....keyPrefix = " + keyPrefix);
		Long startTS = System.currentTimeMillis();
		
		keyPrefix = keyPrefix.trim().toUpperCase();
		String key = null;
		int prefixCount = 0;
		try {
			if (startKeyCount <= 0) {
				key = keyPrefix;
				if (this.jedis.exists(key)) {
					this.jedis.del(key);
					prefixCount = 1;
				}
				startKeyCount = 1;
			}
			// clean up other extra keys
			while (true) {
				key = keyPrefix + "-" + startKeyCount;
				if (!this.jedis.exists(key)) {
					break;
				} else {
					this.jedis.del(key);
					startKeyCount++;
				}
			}
			Long endTS = System.currentTimeMillis();
			logger.fine("cleanContentInRedis()  end.....total millis = " + (endTS - startTS)
					+ ", total cleaned key count = " + (startKeyCount - 1 + prefixCount));
			return true;
		} catch (Throwable t) {
			t.printStackTrace();
			logger.warning("cleanContentInRedis got error: " + t);
			if (t instanceof JedisException) {
				closeRedis();
			}
			return false;
		}
	}

	protected boolean saveContentToRedis(String keyPrefix, List<String> contentList, boolean startFromKeyPrefix, boolean append) {

		if (this.jedis == null) {
			logger.fine("There is no redis connectin established, do nothing!");
			return false;
		}
		if (StringUtil.isEmptyOrNull(keyPrefix)) {
			logger.fine("There is no key prefix provided, do nothing!");
			return false;
		}
		logger.fine("save content to Redis cache start.....keyPrefix = " + keyPrefix + ", content (to be saved) List size = " + (contentList==null? 0: contentList.size()));

		Long startTS = System.currentTimeMillis();

		keyPrefix = keyPrefix.trim().toUpperCase();

		if (contentList == null || contentList.isEmpty()) {
			// clean all contents
			if (append) {
				//keep exisitng data
				Long endTS = System.currentTimeMillis();
				logger.fine("saveContentToRedis()  end, <Append Mode> - contentList is null or empty, do nothing......total millis = " + (endTS - startTS) );
				return true;
			}else {
				boolean result =  cleanContentInRedis(keyPrefix, 0);
				Long endTS = System.currentTimeMillis();
				logger.fine("saveContentToRedis()  end, <Refresh Mode> contentList is null or empty, clean all keys.....total millis = " + (endTS - startTS) );
				return result;
			}
		}
		try {
			String key = null;
			String value = null;
			String tmpStr = null;
			int keyCount = startFromKeyPrefix ? 0 : 1;
			
			if(append) {
				int tmpCount = keyCount;
				if (tmpCount == 0) {
					key = keyPrefix;
				} else {
					key = keyPrefix + "-" + tmpCount;
				}
				while (true) {
					if (this.jedis.exists(key)) {
						tmpCount ++ ;
					} else {
						break;
					}
					key = keyPrefix + "-" + tmpCount;
				}
				keyCount = tmpCount;
			}
			for (int i = 0; i < contentList.size(); i++) {
				tmpStr = contentList.get(i);
				if (value == null) {
					value = (StringUtil.isEmptyOrNull(tmpStr) ? EventManagerConstant.EVENT_INFO_EMPTY_CONTENT : tmpStr);
				} else {
					value = value + KEY_CONTENT_SEPARATOR + (StringUtil.isEmptyOrNull(tmpStr) ? EventManagerConstant.EVENT_INFO_EMPTY_CONTENT : tmpStr);
				}
				if (value.length() >= REDIS_MAX_VALUE_LENGTH_PER_KEY || i ==  (contentList.size() -1)) {
					if (keyCount == 0) {
						key = keyPrefix;
						this.jedis.set(key, value);
						value = null;
						keyCount++;
					} else {
						key = keyPrefix + "-" + keyCount;
						this.jedis.set(key, value);
						keyCount++;
						value = null;
					}
					continue;
				}
			}
			cleanContentInRedis(keyPrefix, keyCount);
			Long endTS = System.currentTimeMillis();
			logger.fine("saveContentToRedis()  end.....keyPrefix = " + keyPrefix + ", total millis = " + (endTS - startTS) + ", total key count = "
					+ keyCount + ", saved contentList size = " + contentList.size());
			return true;
		} catch (Throwable t) {
			t.printStackTrace();
			logger.warning("saveContentToRedis got error: " + t);
			if (t instanceof JedisException) {
				closeRedis();
			}
			return false;
		}
	}

	protected List<String> getContentFromRedis(String keyPrefix) {

		if (this.jedis == null) {
			logger.fine("There is no redis connectin established, do nothing!");
			return null;
		}
		if (StringUtil.isEmptyOrNull(keyPrefix)) {
			logger.fine("There is no key prefix provided, do nothing!");
			return null;
		}
		logger.fine("get content from Redis cache start.....keyPrefix = " + keyPrefix);
		try {
			Long startTS = System.currentTimeMillis();

			keyPrefix = keyPrefix.trim().toUpperCase();
			List<String> contentList = new ArrayList<String>();

			int prefixCount = 0;
			String value = null;
			if (jedis.exists(keyPrefix)) {
				value = jedis.get(keyPrefix);
				if (!StringUtil.isEmptyOrNull(value)) {
					String[] vArray = value.split(KEY_CONTENT_SEPARATOR);
					List<String> vList = Arrays.asList(vArray);
					contentList.addAll(vList);
				}
				prefixCount = 1;
			}
			int keyCount = 1;
			String key = null;
			while (true) {
				key = keyPrefix + "-" + keyCount;
				if (jedis.exists(key)) {
					value = jedis.get(key);
					if (!StringUtil.isEmptyOrNull(value)) {
						String[] vArray = value.split(KEY_CONTENT_SEPARATOR);
						List<String> vList = Arrays.asList(vArray);
						contentList.addAll(vList);
					}
				} else {
					break;
				}
				keyCount++;
			}
			Long endTS = System.currentTimeMillis();
			logger.fine("getContentFromRedis()  end.....keyPrefix = " + keyPrefix + ", total millis = " + (endTS - startTS) + ", total key count = "
					+ (keyCount - 1 + prefixCount) + ", total content lines (remove null or empty value) = "
					+ contentList.size());
			return contentList;
		} catch (Throwable t) {
			t.printStackTrace();
			logger.warning("saveContentToRedis got error: " + t);
			if (t instanceof JedisException) {
				closeRedis();
			}
			return null;
		}
	}

	protected String getValueFromRedis(String key) {

		if (this.jedis == null) {
			logger.fine("There is no redis connectin established, do nothing!");
			return null;
		}
		if (StringUtil.isEmptyOrNull(key)) {
			logger.fine("There is no key provided, do nothing!");
			return null;
		}
		logger.fine("getValueFromRedis start.....key = " + key);
		try {
			Long startTS = System.currentTimeMillis();

			String value = jedis.get(key);

			Long endTS = System.currentTimeMillis();
			logger.fine("getValueFromRedis()  end.....key = " + key + ", total millis = " + (endTS - startTS));
			return value;
		} catch (Throwable t) {
			t.printStackTrace();
			logger.warning("saveContentToRedis got error: " + t);
			if (t instanceof JedisException) {
				closeRedis();
			}
			return null;
		}
	}

	protected boolean setValueToRedis(String key, String value) {

		if (this.jedis == null) {
			logger.fine("There is no redis connectin established, do nothing!");
			return false;
		}
		if (StringUtil.isEmptyOrNull(key)) {
			logger.fine("There is no key provided, do nothing!");
			return false;
		}
		logger.fine("setValueToRedis start.....");
		try {
			Long startTS = System.currentTimeMillis();
			if (value == null) {
				jedis.del(key);
			} else {
				if(value.isEmpty()) {
					value = EventManagerConstant.EVENT_INFO_EMPTY_CONTENT;
				}
				jedis.set(key, value);
			}
			Long endTS = System.currentTimeMillis();
			logger.fine("setValueToRedis()  end.....key = " + key + ", total millis = " + (endTS - startTS));
			return true;
		} catch (Throwable t) {
			t.printStackTrace();
			logger.warning("saveContentToRedis got error: " + t);
			if (t instanceof JedisException) {
				closeRedis();
			}
			return false;
		}
	}

	public static void main(String[] args) {

	}

}
