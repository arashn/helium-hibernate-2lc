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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.Factory;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.spi.CachingProvider;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;

import com.serisys.helium.jcache.ConsolidatedCacheConfig;
import com.serisys.helium.jcache.HeCache;
import com.serisys.helium.jcache.HeCacheManager;
import com.serisys.helium.jcache.HeCacheProperties;
import com.serisys.helium.jcache.HeCachingProvider;
import com.serisys.helium.jcache.mx.CacheMXStatsBeanImpl;
import com.serisys.helium.jcache.util.ItemManager;
import com.serisys.helium.jcache.util.UnmanagedItemManager;

@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CacheTest {
	private static Cache<String, String> cache;
	
	public CacheTest() {
	}

	@Test
	public void t0001_providerResolutionViaServiceLoader() {
		CachingProvider provider = Caching.getCachingProvider();
		assertNotNull(provider);
		assertTrue(provider instanceof HeCachingProvider);
	}

	@Test
	public void t0002_providerResolutionViaSystemProperty() {
		System.setProperty("javax.cache.CachingProvider", "com.serisys.helium.jcache.HeCachingProvider");
		CachingProvider provider = Caching.getCachingProvider();
		assertNotNull(provider);
		assertTrue(provider instanceof HeCachingProvider);
	}

	@Test
	public void t0003_providerResolutionExplicit() {
		CachingProvider provider = Caching.getCachingProvider("com.serisys.helium.jcache.HeCachingProvider");
		assertNotNull(provider);
		assertTrue(provider instanceof HeCachingProvider);
	}

	@Test
	public void t0004_defaultCacheManager() {
		CachingProvider provider = Caching.getCachingProvider();
		assertNotNull(provider);
		assertTrue(provider instanceof HeCachingProvider);
		CacheManager mngr = provider.getCacheManager();
		assertNotNull(mngr);
		assertTrue(mngr instanceof HeCacheManager);
	}

	/**
	 * create a cache with Helium HE_O_VOLUME_TRUNCATE to initialize the volume in the 
	 * case of a hitherto unused and newly initialized non-mounted, non-formatted raw SSD.
	 */
	@Test
	public void t0005_volumeTruncate() {
		Properties properties = CacheTestBase.getDefaultProperties();
		properties.setProperty(HeCacheProperties.HE_O_VOLUME_TRUNCATE_PROPERTY, "true");
		CachingProvider provider = Caching.getCachingProvider();
		assertNotNull(provider);
		assertTrue(provider instanceof HeCachingProvider);
		provider.close();
		CacheManager mngr = provider.getCacheManager(provider.getDefaultURI(), provider.getDefaultClassLoader(),
				properties);
		assertNotNull(mngr);
		assertTrue(mngr instanceof HeCacheManager);
		MutableConfiguration<String, String> config = new MutableConfiguration<String, String>();
		config.setTypes(String.class, String.class);
		cache = mngr.createCache("TestDatastore", config);
	}

	/**
	 * create a cache, check Cache.getName()
	 */
	@Test
	public void t0006_defaultCache_getName() {
		Properties properties = CacheTestBase.getDefaultProperties();
		CachingProvider provider = Caching.getCachingProvider();
		assertNotNull(provider);
		assertTrue(provider instanceof HeCachingProvider);
		provider.close();
		CacheManager mngr = provider.getCacheManager(provider.getDefaultURI(), provider.getDefaultClassLoader(),
				properties);
		assertNotNull(mngr);
		assertTrue(mngr instanceof HeCacheManager);
		MutableConfiguration<String, String> config = new MutableConfiguration<String, String>();
		config.setTypes(String.class, String.class);
		cache = mngr.createCache("TestDatastore", config);
		
		assertEquals("TestDatastore", cache.getName());
		
	}

	/**
	 * Cache.getCacheManager()
	 */
	@Test
	public void t0007_defaultCache_getCacheManager() {
		CachingProvider provider = Caching.getCachingProvider();
		assertNotNull(provider);
		assertTrue(provider instanceof HeCachingProvider);
		CacheManager mngr = provider.getCacheManager(provider.getDefaultURI(), provider.getDefaultClassLoader());
		assertNotNull(mngr);
		assertTrue(mngr instanceof HeCacheManager);
		assertEquals(mngr, cache.getCacheManager());
	}

	/**
	 * Cache.getConfiguration()
	 */
	@Test
	public void t0008_defaultCache_getConfiguration() {
		CompleteConfiguration complete = cache.getConfiguration(CompleteConfiguration.class);
		assertEquals(String.class, complete.getKeyType());
		assertEquals(String.class, complete.getValueType());
	}

	/**
	 * Cache.getConfiguration()
	 */
	@Test
	public void t0009_defaultCache_getConfiguration() {
		MutableConfiguration mute = cache.getConfiguration(MutableConfiguration.class);
		assertEquals(String.class, mute.getKeyType());
		assertEquals(String.class, mute.getValueType());
	}

	/**
	 * cache.unwrap
	 */
	@Test
	public void t0010_defaultCache_unwrap() {
		Cache<String,String> unwrapped = cache.unwrap(Cache.class);
	}
	
	/**
	 * cache.unwrap
	 */
	@Test
	public void t0011_defaultCache_unwrap() {
		Cache<String,String> unwrapped = cache.unwrap(HeCache.class);
	}
	
	/**
	 * cache.unwrap
	 */
	@Test
	public void t0012_defaultCache_unwrap() {
		IllegalArgumentException expected = null;
		try {
			Cache<String,String> unwrapped = cache.unwrap(UnimplementedCache.class);
		} catch (IllegalArgumentException e) {
			expected = e;
		}
		assertNotNull(expected);
	}
	
	/**
	 * put single key
	 * 
	 * show that when creating a new cache, WITH DEFAULT FLAGS, the contents are not persisted
	 * 
	 */
	@Test
	public void t0100_defaultCache_put_get_no_persist() {
		final String key = "A0001";
		final String value = "A valuable piece of string";
		cache.put(key, value);
		assertEquals(value, cache.get(key));

		// close cache
		cache.close();

		// open cache
		Properties properties = CacheTestBase.getDefaultProperties();
		CachingProvider provider = Caching.getCachingProvider();
		assertNotNull(provider);
		assertTrue(provider instanceof HeCachingProvider);
		provider.close();
		CacheManager mngr = provider.getCacheManager(provider.getDefaultURI(), provider.getDefaultClassLoader(),
				properties);
		assertNotNull(mngr);
		assertTrue(mngr instanceof HeCacheManager);
		ConsolidatedCacheConfig<String, String> config = new ConsolidatedCacheConfig<String, String>();
		config.setTypes(String.class, String.class);
		config.setItemManagerFactory(new Factory<ItemManager>() {
			@Override
			public ItemManager create() {
				return new UnmanagedItemManager();
			}
		});
		cache = mngr.createCache("TestDatastore", config);
		
		assertNull(cache.get(key));
	}
	
	@SuppressWarnings("rawtypes")
	@Test 
	public void t0101_unwrap() {
		Class<Cache> claz = Cache.class;
		IllegalArgumentException unexpected = null;
		try {
			cache.unwrap(claz);
		} catch (IllegalArgumentException e) {
			unexpected = e;
		}
		assertNull(unexpected);
	}
	
	@SuppressWarnings("rawtypes")
	@Test 
	public void t0102_unwrap() {
		Class<HeCache> claz = HeCache.class;
		IllegalArgumentException unexpected = null;
		try {
			cache.unwrap(claz);
		} catch (IllegalArgumentException e) {
			unexpected = e;
		}
		assertNull(unexpected);
	}
	
	@Test 
	public void t0103_unwrap() {
		Class<UnimplementedCache> claz = UnimplementedCache.class;
		IllegalArgumentException expected = null;
		try {
			UnimplementedCache unwrapped = cache.unwrap(claz);
		} catch (IllegalArgumentException e) {
			expected = e;
		}
		assertNotNull(expected);
	}
	
	@Test 
	public void t0104_unwrap() {
		Class<CacheMXStatsBeanImpl> claz = CacheMXStatsBeanImpl.class;
		IllegalArgumentException expected = null;
		try {
			CacheMXStatsBeanImpl unwrapped = cache.unwrap(claz);
		} catch (IllegalArgumentException e) {
			expected = e;
		}
		assertNotNull(expected);
	}
	
	@Test
	public void t0200_close() {
		assertFalse(cache.isClosed());
		cache.close();
		assertTrue(cache.isClosed());
	}
	
}
