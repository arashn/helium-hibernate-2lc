/*
 * 
 * Copyright © Serisys Solutions (Europe) Limited 2018-2019
 *
 *
    This file is part of helium-hibernate-2lc.

    helium-hibernate-2lc is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published 
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    helium-hibernate-2lc is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with helium-hibernate-2lc.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.serisys.helium_hibernate_2lc;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.Factory;
import javax.cache.spi.CachingProvider;

import com.serisys.helium.jcache.ConsolidatedCacheConfig;
import com.serisys.helium.jcache.HeCacheManager;
import com.serisys.helium.jcache.HeCacheProperties;
import com.serisys.helium.jcache.HeCachingProvider;
import com.serisys.helium.jcache.synch.DualLevelLockHandler;
import com.serisys.helium.jcache.util.DynamicThreadLocalItemPool;
import com.serisys.helium.jcache.util.ItemManager;

public abstract class CacheTestBase {
	protected static Cache<String, String> cache;
	
	public static Properties getDefaultProperties() {
		Properties properties = new Properties();
		properties.setProperty(HeCacheProperties.SYNCH_HANDLER_CLASS_NAME, DualLevelLockHandler.class.getName());
		if (System.getProperty(HeCacheProperties.HE_CACHE_URL_PROPERTY) == null) {
			properties.setProperty(HeCacheProperties.HE_CACHE_URL_PROPERTY, AllCacheTests.HE_DEV_URL);
		}
		properties.setProperty(HeCacheProperties.HE_O_CREATE_PROPERTY, "true");
		properties.setProperty(HeCacheProperties.HE_O_TRUNCATE_PROPERTY, "true");
		properties.setProperty(HeCacheProperties.HE_O_VOLUME_CREATE_PROPERTY, "true");
		return properties;
	}

	public static void setUp(boolean enableStats) {
		Properties properties = getDefaultProperties();
		CachingProvider provider = Caching.getCachingProvider();
		assertNotNull(provider);
		assertTrue(provider instanceof HeCachingProvider);
		provider.close();
		CacheManager mngr = provider.getCacheManager(provider.getDefaultURI(), provider.getDefaultClassLoader(), properties);
		String cacheName = "TestDatastore";
		assertNotNull(mngr);
		assertTrue(mngr instanceof HeCacheManager);
		Class keyType = String.class;
		Class valueType = String.class;
		ConsolidatedCacheConfig<String, String> config = new ConsolidatedCacheConfig<String, String>();
		config.setTypes(keyType, valueType);
		config.setStatisticsEnabled(enableStats);
		config.setItemManagerFactory(new Factory<ItemManager>() {
			@Override
			public ItemManager create() {
				return new DynamicThreadLocalItemPool();
			}
		});
		cache = mngr.createCache(cacheName, config);
		if (enableStats) {
			mngr.enableStatistics(cacheName, true);		
			cache = mngr.getCache(cacheName, keyType, valueType);
		}
	}

	public static void tearDown() {
		cache.close();
	}
	
	public String getLargeString(String path) {
		byte[] encoded;
		try {
			URL url = getClass().getClassLoader().getResource(path);
			encoded = Files.readAllBytes(Paths.get(url.toURI()));
		} catch (IOException | URISyntaxException e) {
			throw new Error("Could not read file", e);
		}
		return new String(encoded, Charset.forName("UTF-8"));

	}
}
