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

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.Factory;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.expiry.ExpiryPolicy;
import javax.cache.spi.CachingProvider;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;

import com.serisys.helium.jcache.HeCacheManager;
import com.serisys.helium.jcache.HeCachingProvider;
import com.serisys.helium.jcache.WrappableCacheWrapper;
import com.serisys.helium.jcache.mx.CacheMXStatsBeanImpl;
import com.serisys.helium.jcache.mx.CacheStats_Snapshot;


@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ExpireOnCreationStatsTest extends CacheTestBase {

	@BeforeClass
	public static void setUp() {
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
		MutableConfiguration<String, String> config = new MutableConfiguration<String, String>();
		config.setTypes(keyType, valueType);
		config.setStatisticsEnabled(true);
		config.setExpiryPolicyFactory(new Factory<ExpiryPolicy>() {
			@Override
			public ExpiryPolicy create() {
				return new ExpiryPolicy() {

					@Override
					public Duration getExpiryForCreation() {
						return Duration.ZERO;
					}

					@Override
					public Duration getExpiryForAccess() {
						return null;
					}

					@Override
					public Duration getExpiryForUpdate() {
						return null;
					}
					
				};
			}
			
		});
		cache = mngr.createCache(cacheName, config);
	}
	
	@AfterClass
	public static void tearDown() {
		CacheTestBase.tearDown();
	}
	
	public ExpireOnCreationStatsTest() {
		
	}
	
	@Test
	public void test001_getAndPut() {
		String key = "test001";
		String value = "test001_getAndPut";
		CacheStats_Snapshot snap0 = getStats_Snapshot();
		cache.getAndPut(key, value);
		CacheStats_Snapshot snap1 = getStats_Snapshot();
		assertEquals(snap0.getCachePuts(), snap1.getCachePuts());
		assertEquals(snap0.getCacheHits(), snap1.getCacheHits());
		assertEquals(snap0.getCacheMisses(), snap1.getCacheMisses());
		assertEquals(snap0.getCacheRemovals(), snap1.getCacheRemovals());
	}

	@Test
	public void test002_put() {
		String key = "test002";
		String value = "test002_put";
		CacheStats_Snapshot snap0 = getStats_Snapshot();
		cache.put(key, value);
		CacheStats_Snapshot snap1 = getStats_Snapshot();
		assertEquals(snap0.getCachePuts(), snap1.getCachePuts());
		assertEquals(snap0.getCacheHits(), snap1.getCacheHits());
		assertEquals(snap0.getCacheMisses(), snap1.getCacheMisses());
		assertEquals(snap0.getCacheRemovals(), snap1.getCacheRemovals());
	}

	@Test
	public void test003_putAll() {
		String key = "test003";
		String value = "test003_putAll";
		String key2 = "test003/2";
		String value2 = "test003/2_putAll";
		Map<String,String> vals = new java.util.HashMap<String, String>();
		vals.put(key, value);
		vals.put(key2, value2);
		CacheStats_Snapshot snap0 = getStats_Snapshot();
		cache.putAll(vals);
		CacheStats_Snapshot snap1 = getStats_Snapshot();
		assertEquals(snap0.getCachePuts(), snap1.getCachePuts());
		assertEquals(snap0.getCacheHits(), snap1.getCacheHits());
		assertEquals(snap0.getCacheMisses(), snap1.getCacheMisses());
		assertEquals(snap0.getCacheRemovals(), snap1.getCacheRemovals());
	}

	private CacheStats_Snapshot getStats_Snapshot() {
		return ((CacheMXStatsBeanImpl<String, String>)((WrappableCacheWrapper<String, String>) cache).getInnerCache()).getStats().getSnapshot();
	}
	
}
