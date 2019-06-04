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

import java.util.Properties;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.Factory;
import javax.cache.spi.CachingProvider;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;

import com.serisys.helium.jcache.ConsolidatedCacheConfig;
import com.serisys.helium.jcache.HeCacheManager;
import com.serisys.helium.jcache.HeCachingProvider;
import com.serisys.helium.jcache.util.DynamicThreadLocalItemPool;
import com.serisys.helium.jcache.util.ItemManager;

@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DynamicThreadLocalItemPoolCacheContentTest extends CacheContentTest {
	@BeforeClass
	public static void setUp() {
		boolean enableStats = false;
		Properties properties = getDefaultProperties();
		CachingProvider provider = Caching.getCachingProvider();
		assertNotNull(provider);
		assertTrue(provider instanceof HeCachingProvider);
		provider.close();
		CacheManager mngr = provider.getCacheManager(provider.getDefaultURI(), provider.getDefaultClassLoader(), properties);
		String cacheName = "TestDatastore";
		assertNotNull(mngr);
		assertTrue(mngr instanceof HeCacheManager);
		ConsolidatedCacheConfig<String, String> config = new ConsolidatedCacheConfig<String, String>();
		config.setTypes(String.class, String.class);
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
			cache = mngr.getCache(cacheName);
		}
	}
	

	public DynamicThreadLocalItemPoolCacheContentTest() {
		
	}

}
