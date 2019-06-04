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
import java.util.Set;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.EntryProcessorResult;
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
public class NullKeyTest {
	private boolean record = false; 
	
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
	
	public NullKeyTest() {
	}
	
	public Map<String,String> recordCacheState() {
		System.out.println("Recording cache state ... ");
		Map<String,String> state = new java.util.HashMap<String,String>();
		if (record) {
			Iterator<Cache.Entry<String,String>> contents = cache.iterator();
			while (contents.hasNext()) {
				Cache.Entry<String,String> entry = contents.next();
				state.put(entry.getKey(), entry.getValue());
			}
		}
		System.out.println(" ... done recording cache state");
		return state;
	}
	
	public void compareCacheState(Map<String,String> pre_state) {
		Map<String,String> post_state = recordCacheState();
		assertEquals(pre_state,post_state);
	}

	/**
	 * containskey
	 */
	@Test
	public void t0000_defaultCache_containsKey() {
		Map pre_state = recordCacheState();
		final String key = null;
		final String value = "t0000_defaultCache_containsKey";
		NullPointerException expected = null; 
		try {
			cache.containsKey(key);
		} catch (NullPointerException e) {
			expected = e;
		}
		assertNotNull(expected);
		compareCacheState(pre_state);
	}
	
	/**
	 * put single key
	 */
	@Test
	public void t0001_defaultCache_put() {
		Map pre_state = recordCacheState();
		NullPointerException expected = null; 
		try {
			cache.put(null, "t0001_value");
		} catch (NullPointerException e) {
			expected = e;
		}
		assertNotNull(expected);
		compareCacheState(pre_state);
	}

	/**
	 * get a single key
	 */
	@Test
	public void t0002_defaultCache_get() {
		Map pre_state = recordCacheState();
		NullPointerException expected = null; 
		try {
			cache.get(null);
		} catch (NullPointerException e) {
			expected = e;
		}
		assertNotNull(expected);
		compareCacheState(pre_state);
	}

	/**
	 * put / getAll 
	 */
	@Test
	public void t0003_defaultCache_getAll() {
		Map pre_state = recordCacheState();
		final String key = null;
		final String key2 = "t0003_2";
		final String value = "t0003_defaultCache_put_getAll";
		final String value2 = "t0003 some other string";
		
		Set<String> keys = new java.util.HashSet<String>(7);
		keys.add(key);
		keys.add(key2);
		NullPointerException expected = null; 
		try {
			Map<String,String> results = cache.getAll(keys);
		} catch (NullPointerException e) {
			expected = e;
		}
		assertNotNull(expected);
		compareCacheState(pre_state);
	}
	
	/**
	 * put / get / getAndPut, for same key, showing overwriting of value
	 */
	@Test
	public void t0004_defaultCache_getAndPut() {
		Map pre_state = recordCacheState();
		NullPointerException expected = null; 
		try {
			cache.getAndPut(null, "t0004_value");
		} catch (NullPointerException e) {
			expected = e;
		}
		assertNotNull(expected);
		compareCacheState(pre_state);
	}
	
	/**
	 * put / get / getAndRemove, for same key
	 */
	@Test
	public void t0005_defaultCache_getAndRemove() {
		Map pre_state = recordCacheState();
		NullPointerException expected = null; 
		try {
			cache.getAndRemove(null);
		} catch (NullPointerException e) {
			expected = e;
		}
		assertNotNull(expected);
		compareCacheState(pre_state);
	}
	
	/**
	 * put / get / getAndReplace, for same key, showing overwriting of value
	 */
	@Test
	public void t0006_defaultCache_getAndReplace() {
		Map pre_state = recordCacheState();
		NullPointerException expected = null; 
		try {
			cache.getAndReplace(null, "t0006_value");
		} catch (NullPointerException e) {
			expected = e;
		}
		assertNotNull(expected);
		compareCacheState(pre_state);
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
				java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("a+|e+|i+|o+|u+");
				java.util.regex.Matcher matcher = pattern.matcher(entry.getValue());
				Map<String,Integer> results = new java.util.HashMap<String,Integer>();
				while (matcher.find()) {
					String match = matcher.group();
					Integer count = results.get(match);
					if (count == null) {
						count = new Integer(0);
					}
					results.put(match, new Integer(count.intValue()+1));
				}
				return results;
			}
		};

		Map pre_state = recordCacheState();
		
		NullPointerException expected = null; 
		try {
			Map<String,Integer> result = cache.invoke(null, vowel_counter, new Object[] {});
		} catch (NullPointerException e) {
			expected = e;
		}
		assertNotNull(expected);
		compareCacheState(pre_state);
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
				java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(banana)+|(mango)+|(kumquat)+|(grape)+");
				java.util.regex.Matcher matcher = pattern.matcher(entry.getValue());
				return matcher.find();
			}
		};

		String no_fruits_key = "t0008_fruitless";
//		cache.put(no_fruits_key, "the quick brown fox jumped over the lazy dog");
		cache.put(no_fruits_key, "fox");
		String some_fruits_key = "t0007_some_fruits";
//		cache.put(some_fruits_key, "the quick brown banana jumped over the lazy grape");
		cache.put(some_fruits_key, "banana");
		String no_such_key = "shurely shome mishtake";

		Map pre_state = recordCacheState();
		
		Set<String> keys_to_check = new java.util.HashSet<String>(7);
		keys_to_check.add(no_fruits_key);
		keys_to_check.add(some_fruits_key);
		keys_to_check.add(null);
		
		Map<String,EntryProcessorResult<Boolean>> result = cache.invokeAll(keys_to_check, fruit_detector, new Object[] {});
		assertFalse(result.isEmpty());
		assertEquals(3, result.size());
		for (Map.Entry<String,EntryProcessorResult<Boolean>> entry : result.entrySet()) {
			if (entry.getKey() == null) {
				CacheException expected = null;
				try {
					entry.getValue().get();
				} catch (CacheException e) {
					expected = e;
				}
				assertNotNull(expected);
				assertNotNull(expected.getCause());
				NullPointerException expected_npe = null;
				Throwable unexcepted = null;
				
				try {
					throw expected.getCause();
				} catch (NullPointerException npe) {
					expected_npe = npe;
				} catch (Throwable t) {
					unexcepted = t;
				}
				assertNull(unexcepted);
				assertNotNull(expected_npe);
			} else if (entry.getKey().equals(no_fruits_key)) {
				assertEquals(Boolean.FALSE, entry.getValue().get());
			} else if (entry.getKey().equals(some_fruits_key)) {
				assertEquals(Boolean.TRUE, entry.getValue().get());
			}  
		}
		compareCacheState(pre_state);
	}
	
	/**
	 * putAll
	 */
	@Test
	public void t0011_defaultCache_putAll() {
		Map pre_state = recordCacheState();
		Map<String,String> check = t0011_keysValues();
		NullPointerException expected = null; 
		try {
			cache.putAll(check);
		} catch (NullPointerException e) {
			expected = e;
		}
		assertNotNull(expected);
		compareCacheState(pre_state);
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
		check.put(null, "nonsense");
		return check;
	}

	/**
	 * putIfAbsent
	 */
	@Test
	public void t0012_defaultCache_putIfAbsent() {
		Map pre_state = recordCacheState();
		NullPointerException expected = null; 
		try {
			cache.putIfAbsent(null, "t0012_value");
		} catch (NullPointerException e) {
			expected = e;
		}
		assertNotNull(expected);
		compareCacheState(pre_state);
	}
	
	/**
	 * remove
	 */
	@Test
	public void t0013_defaultCache_remove() {
		Map pre_state = recordCacheState();
		NullPointerException expected = null; 
		try {
			cache.remove(null);
		} catch (NullPointerException e) {
			expected = e;
		}
		assertNotNull(expected);
		compareCacheState(pre_state);
	}
	
	/**
	 * Cache.removeAll(Set<>)
	 */
	@Test
	public void t0016_defaultCache_removeAll() {
		Map pre_state = recordCacheState();
		Set<String> to_remove = new java.util.HashSet<String>();
		to_remove.add("t0016_key");
		to_remove.add(null);
		NullPointerException expected = null; 
		try {
			cache.removeAll(to_remove);
		} catch (NullPointerException e) {
			expected = e;
		}
		assertNotNull(expected);
		compareCacheState(pre_state);
	}
	
	/**
	 * replace
	 */
	@Test
	public void t0017_defaultCache_replace() {
		Map pre_state = recordCacheState();
		NullPointerException expected = null; 
		try {
			cache.replace(null, "t0017_new_value");
		} catch (NullPointerException e) {
			expected = e;
		}
		assertNotNull(expected);
		compareCacheState(pre_state);
	}
	
	/**
	 * replace(K,V,V)
	 */
	@Test
	public void t0019_defaultCache_replace() {
		Map pre_state = recordCacheState();
		NullPointerException expected = null; 
		try {
			boolean replaced = cache.replace(null, "t0019_value", "t0019_new_value");
		} catch (NullPointerException e) {
			expected = e;
		}
		assertNotNull(expected);
		compareCacheState(pre_state);
	}
	
	/**
	 * remove(K,V)
	 */
	@Test
	public void t0022_defaultCache_removeKV() {
		Map pre_state = recordCacheState();
		NullPointerException expected = null; 
		try {
			cache.remove(null, "t0022_value");
		} catch (NullPointerException e) {
			expected = e;
		}
		assertNotNull(expected);
		compareCacheState(pre_state);
	}

}
