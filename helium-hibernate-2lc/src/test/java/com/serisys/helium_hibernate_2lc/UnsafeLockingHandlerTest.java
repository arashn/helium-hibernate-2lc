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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ConcurrentModificationException;
import java.util.Properties;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.Factory;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.spi.CachingProvider;

import org.junit.BeforeClass;
import org.junit.Test;

import com.serisys.helium.jcache.HeCache;
import com.serisys.helium.jcache.HeCacheManager;
import com.serisys.helium.jcache.HeCacheProperties;
import com.serisys.helium.jcache.HeCachingProvider;
import com.serisys.helium.jcache.synch.SynchHandling;
import com.serisys.helium.jcache.synch.ThreadsafeExecution;
import com.serisys.helium.jcache.synch.UnsafeNoSynchHandler;

public class UnsafeLockingHandlerTest extends MTCacheContentTest {
	@BeforeClass
	public static void setUp() {
		Properties properties = CacheTestBase.getDefaultProperties();
		properties.setProperty(HeCacheProperties.SYNCH_HANDLER_CLASS_NAME, UnsafeNoSynchHandler.class.getName());
		CachingProvider provider = Caching.getCachingProvider();
		assertNotNull(provider);
		assertTrue(provider instanceof HeCachingProvider);
		provider.close();
		CacheManager mngr = provider.getCacheManager(provider.getDefaultURI(), provider.getDefaultClassLoader(), properties);
		String cacheName = "TestDatastore";
		assertNotNull(mngr);
		assertTrue(mngr instanceof HeCacheManager);
		MutableConfiguration<CustomCacheKey, String> config = new MutableConfiguration<CustomCacheKey, String>();
		config.setTypes(CustomCacheKey.class, String.class);
		config.setStatisticsEnabled(false);
		cache = mngr.createCache(cacheName, config);
	}

	@Override
	protected Factory<? extends ThreadsafeExecution> getSynchHandlerFactory() {
		return FactoryBuilder.factoryOf(UnsafeNoSynchHandler.class);
	}

	@Test
	@Override
	public void s0001_testFactory() {
		super.s0001_testFactory();
	}
	
	@Test
	@Override
	public void s0002_checkSafety() {
		super.s0002_checkSafety();
	}
	
	@Override
	protected void s0002_testCondition(String to_check) {
		// output will probably be ACBD, showing interleaving. But it shouldn't be ABCD. 
		assertNotEquals("ABCD", to_check);
	}
	
	@Test
	@Override
	public void s0003_checkSafety() {
		super.s0003_checkSafety();
	}
	
	@Override
	protected void s0003_testCondition(ConcurrentModificationException e) {
		assertNotNull(e);
	}

	@Test
	@Override
	public void s0004_checkSafety() {
		super.s0004_checkSafety();
	}
	
	@Override
	protected void s0004_testCondition(String to_check) {
		// output will probably be ACBD, showing interleaving. But it shouldn't be ABCD. 
		assertNotEquals("ABCD", to_check);
	}
	
	@Test
	@Override
	public void t0000_checkLevel() {
		HeCache heCache = cache.unwrap(HeCache.class);
		assertEquals(SynchHandling.UNSAFE, heCache.getSynchHandlingLevel());
	}

}
