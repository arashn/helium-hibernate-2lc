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
import java.util.Set;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.MutableEntry;
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
public class ClosedCacheTest {
	private static Cache<String, String> cache;
	
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
		// cache already closed
	}
	
	public ClosedCacheTest() {
	}

	/**
	 * close
	 */
	@Test
	public void t0001_close() {
		cache.close();
		assertTrue(cache.isClosed());
	}
	
	@Test
	public void t0002_defaultCache_get() {
		final String key = "t0002";
		IllegalStateException expected = null;
		try {
			cache.get(key);
		} catch (IllegalStateException e) {
			expected = e;
		}
		assertNotNull(expected);
	}
	
	/**
	 * put / getAll 
	 */
	@Test
	public void t0003_defaultCache_getAll() {
		final String key = "t0003_1";
		final String key2 = "t0003_2";
		
		Set<String> keys = new java.util.HashSet<String>(7);
		keys.add(key);
		keys.add(key2);
		IllegalStateException expected = null;
		try {
			cache.getAll(keys);
		} catch (IllegalStateException e) {
			expected = e;
		}
		assertNotNull(expected);
	}
	
	/**
	 * put / get / getAndPut, for same key, showing overwriting of value
	 */
	@Test
	public void t0004_defaultCache_getAndPut() {
		final String key = "t0004";
		final String value2 = "t0004 second value";
		
		IllegalStateException expected = null;
		try {
			cache.getAndPut(key, value2);
		} catch (IllegalStateException e) {
			expected = e;
		}
		assertNotNull(expected);
	}
	
	/**
	 * put / get / getAndRemove, for same key
	 */
	@Test
	public void t0005_defaultCache_getAndRemove() {
		final String key = "t0005";
		
		IllegalStateException expected = null;
		try {
			cache.getAndRemove(key);
		} catch (IllegalStateException e) {
			expected = e;
		}
		assertNotNull(expected);
	}
	
	/**
	 * put / get / getAndReplace, for same key, showing overwriting of value
	 */
	@Test
	public void t0006_defaultCache_getAndReplace() {
		final String key = "t0006";
		final String value2 = key + " second value";
		
		IllegalStateException expected = null;
		try {
			cache.getAndReplace(key, value2);
		} catch (IllegalStateException e) {
			expected = e;
		}
		assertNotNull(expected);
	}
	
	/**
	 * invoke using an EntryProcessor
	 */
	@Test
	public void t0007_defaultCache_invoke() {
		EntryProcessor<String,String,Map<String,Integer>> vowel_counter = new EntryProcessor<String,String,Map<String,Integer>>() {
			@Override
			public Map<String,Integer> process(MutableEntry<String, String> entry, Object... arguments)
					throws EntryProcessorException {
				Map<String,Integer> results = new java.util.HashMap<String,Integer>();
				return results;
			}
		};

		IllegalStateException expected = null;
		try {
			cache.invoke("no_vowels_key", vowel_counter, new Object[] {});
		} catch (IllegalStateException e) {
			expected = e;
		}
		assertNotNull(expected);
	}
	
	/**
	 * invokeAll using an EntryProcessor
	 */
	@Test
	public void t0008_defaultCache_invokeAll() {
		EntryProcessor<String,String,Boolean> fruit_detector = new EntryProcessor<String,String,Boolean>() {
			@Override
			public Boolean process(MutableEntry<String, String> entry, Object... arguments)
					throws EntryProcessorException {
				return false;
			}
		};

		Set<String> keys_to_check = new java.util.HashSet<String>(7);
		IllegalStateException expected = null;
		try {
			cache.invokeAll(keys_to_check, fruit_detector, new Object[] {});
		} catch (IllegalStateException e) {
			expected = e;
		}
		assertNotNull(expected);
	}
	
	/**
	 * Cache.clear()
	 */
	@Test
	public void t0009_defaultCache_clear() {
		IllegalStateException expected = null;
		try {
			cache.clear();
		} catch (IllegalStateException e) {
			expected = e;
		}
		assertNotNull(expected);
	}
	
	/**
	 * Cache.iterator()
	 */
	@Test
	public void t0010_defaultCache_iterator() {
		IllegalStateException expected = null;
		try {
			cache.iterator();
		} catch (IllegalStateException e) {
			expected = e;
		}
		assertNotNull(expected);
	}
	
	/**
	 * putAll
	 */
	@Test
	public void t0011_defaultCache_putAll() {
		Map<String,String> check = new java.util.HashMap<String,String>();
		IllegalStateException expected = null;
		try {
			cache.putAll(check);
		} catch (IllegalStateException e) {
			expected = e;
		}
		assertNotNull(expected);
	}
	
	/**
	 * putIfAbsent
	 */
	@Test
	public void t0012_defaultCache_putIfAbsent() {
		String key = "t0012";
		String value = "t0012_defaultCache_putIfAbsent";
		IllegalStateException expected = null;
		try {
			cache.putIfAbsent(key, value);
		} catch (IllegalStateException e) {
			expected = e;
		}
		assertNotNull(expected);
	}
	
	/**
	 * remove
	 */
	@Test
	public void t0013_defaultCache_remove() {
		String key = "t0013";
		IllegalStateException expected = null;
		try {
			cache.remove(key);
		} catch (IllegalStateException e) {
			expected = e;
		}
		assertNotNull(expected);
	}
	
	/**
	 * Cache.removeAll()
	 */
	@Test
	public void t0015_defaultCache_removeAll() {
		IllegalStateException expected = null;
		try {
			cache.removeAll();
		} catch (IllegalStateException e) {
			expected = e;
		}
		assertNotNull(expected);
	}
	
	/**
	 * Cache.removeAll(Set<>)
	 */
	@Test
	public void t0016_defaultCache_removeAll() {
		Set<String> to_remove = new java.util.HashSet<String>();
		IllegalStateException expected = null;
		try {
			cache.removeAll(to_remove);
		} catch (IllegalStateException e) {
			expected = e;
		}
		assertNotNull(expected);
	}
	
	/**
	 * replace
	 */
	@Test
	public void t0017_defaultCache_replace() {
		String key = "t0017";
		String new_value = "t0017_defaultCache_replacement_value";
		IllegalStateException expected = null;
		try {
			cache.replace(key, new_value);
		} catch (IllegalStateException e) {
			expected = e;
		}
		assertNotNull(expected);
	}
	
	/**
	 * replace(K,V,V)
	 */
	@Test
	public void t0019_defaultCache_replace() {
		String key = "t0019";
		String value = "t0019_defaultCache_replace";
		String new_value = "t0019_defaultCache_replacement_value";
		IllegalStateException expected = null;
		try {
			cache.replace(key, value, new_value);
		} catch (IllegalStateException e) {
			expected = e;
		}
		assertNotNull(expected);
	}
	
}
