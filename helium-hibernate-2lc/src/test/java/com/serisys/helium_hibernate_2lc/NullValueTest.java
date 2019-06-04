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

import java.util.Map;
import java.util.Properties;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
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

@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NullValueTest {
	protected static Cache<String, String> cache;
	
	@BeforeClass
	public static void setUp() {
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
	}
	
	@AfterClass
	public static void tearDown() {
		cache.close();
	}
	
	public NullValueTest() {
	}

	/**
	 * put single key
	 */
	@Test
	public void t0001_defaultCache_put() {
		NullPointerException expected = null; 
		try {
			cache.put("t0001_Key", null);
		} catch (NullPointerException e) {
			expected = e;
		}
		assertNotNull(expected);
	}

	protected final String t0001_value = "A valuable piece of string";

	/**
	 * getAndPut
	 */
	@Test
	public void t0004_defaultCache_getAndPut() {
		NullPointerException expected = null; 
		try {
			String old_value = cache.getAndPut("t0004_key", null);
		} catch (NullPointerException e) {
			expected = e;
		}
		assertNotNull(expected);
	}
	
	/**
	 * getAndReplace
	 */
	@Test
	public void t0006_defaultCache_getAndReplace() {
		NullPointerException expected = null; 
		try {
			cache.getAndReplace("t0006_key", null);
		} catch (NullPointerException e) {
			expected = e;
		}
		assertNotNull(expected);
	}
	
	/**
	 * putAll
	 */
	@Test
	public void t0011_defaultCache_putAll() {
		Map<String,String> check = t0011_keysValues();
		NullPointerException expected = null; 
		try {
			cache.putAll(check);
		} catch (NullPointerException e) {
			expected = e;
		}
		assertNotNull(expected);
	}
	protected final Map<String,String> t0011_keysValues() {
		Map<String,String> check = new java.util.HashMap<String,String>();
		String kroot = "t0011_";
		String vroot = "t0011_defaultCache_putAll_";
		for (int i = 0; i < 10; i++) {
			String key = new StringBuilder().append(kroot).append(i).toString(); 
			String value = new StringBuilder().append(vroot).append(i).toString(); 
			check.put(key, value);
		}
		check.put("nonsense", null);
		return check;
	}

	/**
	 * putIfAbsent
	 */
	@Test
	public void t0012_defaultCache_putIfAbsent() {
		NullPointerException expected = null; 
		try {
			cache.putIfAbsent("t0012_key", null);
		} catch (NullPointerException e) {
			expected = e;
		}
		assertNotNull(expected);
	}
	
	/**
	 * replace
	 */
	@Test
	public void t0017_defaultCache_replace() {
		NullPointerException expected = null; 
		try {
			cache.replace("t0017_key", null);
		} catch (NullPointerException e) {
			expected = e;
		}
		assertNotNull(expected);
	}
	
	/**
	 * replace(K,V,V)
	 */
	@Test
	public void t0019_defaultCache_replace() {
		NullPointerException expected = null; 
		try {
			boolean replaced = cache.replace("t0019_key", null, "t0019_new_value");
		} catch (NullPointerException e) {
			expected = e;
		}
		assertNotNull(expected);
	}
	
	/**
	 * replace(K,V,V)
	 */
	@Test
	public void t0020_defaultCache_replace() {
		NullPointerException expected = null; 
		try {
			boolean replaced = cache.replace("t0019_key", "t0019_old_value", null);
		} catch (NullPointerException e) {
			expected = e;
		}
		assertNotNull(expected);
	}
	
	/**
	 * remove(K,V)
	 */
	@Test
	public void t0022_defaultCache_removeKV() {
		NullPointerException expected = null; 
		try {
			cache.remove("t0022_key", null);
		} catch (NullPointerException e) {
			expected = e;
		}
		assertNotNull(expected);
	}

}
