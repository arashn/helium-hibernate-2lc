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
import java.util.Set;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.EntryProcessorResult;
import javax.cache.processor.MutableEntry;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;

import com.serisys.helium.jcache.EntryProcessorExceptionNoResult;

@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CacheContentTest extends CacheTestBase {
	
	@BeforeClass
	public static void setUp() {
		setUp(false);
	}
	
	@AfterClass
	public static void tearDown() {
		CacheTestBase.tearDown();
	}
	
	public CacheContentTest() {
	}

	/**
	 * containskey
	 */
	@Test
	public void t0000_defaultCache_containsKey() {
		final String key = "t0000";
		final String value = "t0000_defaultCache_containsKey";
		cache.put(key, value);				// put
		assertTrue(cache.containsKey(key)); // nothing see spec for containsKey
	}
	
	/**
	 * put / get a single key
	 */
	@Test
	public void t0001_defaultCache_put_get() {
		cache.put(t0001_key, t0001_value);					// put
		assertEquals(t0001_value, cache.get(t0001_key));	// hit
	}

	protected final String t0001_key= "t0001";
	protected final String t0001_value = "A valuable piece of string";

	/**
	 * put / get / put / get, for same key, showing overwriting of value
	 */
	@Test
	public void t0002_defaultCache_put_get_overwrite() {
		cache.put(t0002_key, t0002_value);					// put
		assertEquals(t0002_value, cache.get(t0002_key));	// hit
		cache.put(t0002_key, t0002_value2);					// put
		assertEquals(t0002_value2, cache.get(t0002_key));	// get
	}
	protected final String t0002_key = "t0002";
	protected final String t0002_value = "A valuable piece of string";
	protected final String t0002_value2 = "A new piece of string";
	
	/**
	 * put / getAll 
	 */
	@Test
	public void t0003_defaultCache_getAll() {
		final String key = "t0003_1";
		final String key2 = "t0003_2";
		final String value = "t0003_defaultCache_put_getAll";
		final String value2 = "t0003 some other string";
		
		cache.put(key, value);								// put
		cache.put(key2, value2);							// put
		Set<String> keys = new java.util.HashSet<String>(7);
		keys.add(key);
		keys.add(key2);
		Map<String,String> results = cache.getAll(keys);	// hit, hit
		assertEquals(2, results.size());
		assertEquals(value, cache.get(key));				// hit
		assertEquals(value2, cache.get(key2));				// hit
	}
	
	/**
	 * put / get / getAndPut, for same key, showing overwriting of value
	 */
	@Test
	public void t0004_defaultCache_getAndPut() {
		
		cache.put(t0004_key, t0004_value);								// put
		assertEquals(t0004_value, cache.get(t0004_key));				// hit
		String old_value = cache.getAndPut(t0004_key, t0004_value2);	// hit & put
		assertEquals(t0004_value, old_value);
		assertEquals(t0004_value2, cache.get(t0004_key));				// hit
	}
	protected final String t0004_key = "t0004";
	protected final String t0004_value = "t0004 first value";
	protected final String t0004_value2 = "t0004 second value";
	
	/**
	 * Duplication of t0004_defaultCache_getAndPut except that initial value for lookup is 
	 * shorter than actual value in cache. Can throw BufferUnderflowException if we don't 
	 * handle correctly.
	 * 
	 */
	@Test
	public void t0004_defaultCache_getAndPut_2() {
		cache.put(t0004_2_key, t0004_2_value);								// put
		assertEquals(t0004_2_value, cache.get(t0004_2_key));				// hit
		String old_value = cache.getAndPut(t0004_2_key, t0004_2_value2);	// hit & put
		assertEquals(t0004_2_value, old_value);
		assertEquals(t0004_2_value2, cache.get(t0004_2_key));				// hit
	}
	protected final String t0004_2_key = "z0004";
	protected final String t0004_2_value = "z0004 first value";
	protected final String t0004_2_value2 = "xyz";

	/**
	 * put / get / getAndRemove, for same key
	 */
	@Test
	public void t0005_defaultCache_getAndRemove() {
		
		cache.put(t0005_key, t0005_value);					// put
		assertEquals(t0005_value, cache.get(t0005_key));	// hit
		String old_value = cache.getAndRemove(t0005_key);	// hit & remove
		assertEquals(t0005_value, old_value);			
		assertFalse(cache.containsKey(t0005_key));			// nothing see spec for containsKey
	}
	protected final String t0005_key = "t0005";
	protected final String t0005_value = "t0005 test value";
	
	/**
	 * put / get / getAndReplace, for same key, showing overwriting of value
	 */
	@Test
	public void t0006_defaultCache_getAndReplace() {
		
		cache.put(t0006_key, t0006_value);									// put
		assertEquals(t0006_value, cache.get(t0006_key));					// hit
		String old_value = cache.getAndReplace(t0006_key, t0006_value2);	// hit, put
		assertEquals(t0006_value, old_value);
		assertEquals(t0006_value2, cache.get(t0006_key));					// hit
	}
	protected final String t0006_key = "t0006";
	protected final String t0006_value = t0006_key + " first value";
	protected final String t0006_value2 = t0006_key + " second value";
	
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

		String no_vowels_key = "t0007_no_vowels";
		cache.put(no_vowels_key, "cdfgxhbklz");								// put
		String some_vowels_key = "t0007_some_vowels";
		cache.put(some_vowels_key, "llangollen");							// put
		String all_the_vowels_key = "t0007_all_the_vowels";
		cache.put(all_the_vowels_key, "the quick brown fox jumped over the lazy dog");	
																			// put
		
		Map<String,Integer> result = cache.invoke(no_vowels_key, vowel_counter, new Object[] {});
																			// hit
		assertTrue(result.isEmpty());
		result = cache.invoke(some_vowels_key, vowel_counter, new Object[] {});
																			// hit
		assertEquals(3, result.size());
		assertEquals(1, result.get("a").intValue());
		assertEquals(1, result.get("e").intValue());
		assertEquals(1, result.get("o").intValue());
		result = cache.invoke(all_the_vowels_key, vowel_counter, new Object[] {});
																			// hit
		assertEquals(5, result.size());
		assertEquals(1, result.get("a").intValue());
		assertEquals(4, result.get("e").intValue());
		assertEquals(1, result.get("i").intValue());
		assertEquals(4, result.get("o").intValue());
		assertEquals(2, result.get("u").intValue());
	}
	
	/**
	 * 
	 * cf Cache.invoke() javadoc: 
	 * 
	 * "If an Cache.Entry does not exist for the specified key, an attempt is made 
	 * to load it (if a loader is configured) or a surrogate Cache.Entry, consisting 
	 * of the key with a null value is used instead." 
	 * 
	 * 
	 */
	@Test
	public void t0007_defaultCache_invoke_noSuchKey() {
		String xkey = "t0007_no_such_key";
		EntryProcessor<String,String,String> processor = new EntryProcessor<String, String, String>() {
			@Override
			public String process(MutableEntry<String, String> entry, Object... arguments)
					throws EntryProcessorException {
				return new StringBuilder()
					.append(entry.getKey())
					.append(" -> ")
					.append(entry.getValue())
					.toString();
			}
		};
		
		String result = cache.invoke(xkey, processor, new Object[] {});	// miss
		String expected = new StringBuilder() 
				.append(xkey)
				.append(" -> ")
				.append("null")
				.toString();
		assertEquals(expected, result);
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
		cache.put(no_fruits_key, "the quick brown fox jumped over the lazy dog");
																		// put
		String some_fruits_key = "t0007_some_fruits";
		cache.put(some_fruits_key, "the quick brown banana jumped over the lazy grape");
																		// put
		String no_such_key = "shurely shome mishtake";
		
		Set<String> keys_to_check = new java.util.HashSet<String>(7);
		keys_to_check.add(no_fruits_key);
		keys_to_check.add(some_fruits_key);
		keys_to_check.add(no_such_key);
		
		Map<String,EntryProcessorResult<Boolean>> result = cache.invokeAll(keys_to_check, fruit_detector, new Object[] {});
																		// hit, hit, miss
		assertFalse(result.isEmpty());
		assertEquals(3, result.size());
		assertEquals(Boolean.FALSE, result.get(no_fruits_key).get());
		assertEquals(Boolean.TRUE, result.get(some_fruits_key).get());
		assertTrue(result.containsKey(no_such_key));
		CacheException expected = null;
		try {
			result.get(no_such_key).get();
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
	}
	
	/**
	 * Cache.clear()
	 */
	@Test
	public void t0009_defaultCache_clear() {
		String key = "t0009";
		String value = "t0009_defaultCache_clear";
		cache.put(key, value);					// put
		assertEquals(value, cache.get(key));	// hit
		cache.clear();							// Does not clear stats
		assertFalse(cache.containsKey(key));	// nothing see spec for containsKey
		Iterator<Cache.Entry<String,String>> cache_contents = cache.iterator();
		boolean something = cache_contents.hasNext();
		assertFalse(something);
	}
	
	/**
	 * Cache.iterator()
	 */
	@Test
	public void t0010_defaultCache_iterator() {
		Map<String,String> check = new java.util.HashMap<String,String>();
		String kroot = "t0010_";
		String vroot = "t0010_defaultCache_iterator_";
		for (int i = 0; i < 10; i++) {												// put x 10
			String key = new StringBuilder().append(kroot).append(i).toString(); 
			String value = new StringBuilder().append(vroot).append(i).toString(); 
			System.out.println("Adding " + key + " -> " + value);
			cache.put(key, value);
			check.put(key, value);
		}
		Iterator<Cache.Entry<String,String>> cache_contents = cache.iterator();
		while (cache_contents.hasNext()) {											// hit x 10 
			Cache.Entry<String, String> entry = cache_contents.next();
			System.out.printf("Iterator.next(): %s => %s\n", entry.getKey(), entry.getValue());
			System.out.println("Removing " + entry.getKey() + " -> " + entry.getValue());
			String match = check.remove(entry.getKey());
			if (match != null) {
				assertEquals(match, entry.getValue());
			}
		}
		System.out.println("check contents: " + check);
		assertTrue(check.isEmpty());
	}
	
	/**
	 * Cache.iterator().remove()
	 */
	@Test
	public void t0010_defaultCache_iterator_remove() {
		Map<String,String> check = new java.util.HashMap<String,String>();
		String kroot = "t0010r_";
		String vroot = "t0010_defaultCache_iterator_";
		for (int i = 0; i < 10; i++) {												// put x 10
			String key = new StringBuilder().append(kroot).append(i).toString(); 
			String value = new StringBuilder().append(vroot).append(i).toString(); 
			System.out.println("Adding " + key + " -> " + value);
			cache.put(key, value);
			check.put(key, value);
		}
		Iterator<Cache.Entry<String,String>> cache_contents = cache.iterator();
		while (cache_contents.hasNext()) {											// hit x 10 
			Cache.Entry<String, String> entry = cache_contents.next();
			System.out.printf("Iterator.next(): %s => %s\n", entry.getKey(), entry.getValue());
			System.out.println("Removing " + entry.getKey() + " -> " + entry.getValue());
			String match = check.remove(entry.getKey());
			if (match != null) {
				assertEquals(match, entry.getValue());
				cache_contents.remove(); 												// remove x 10
			}
		}
		System.out.println("check contents: " + check);
		assertTrue(check.isEmpty());
	}
	
	/**
	 * putAll
	 */
	@Test
	public void t0011_defaultCache_putAll() {
		Map<String,String> check = t0011_keysValues();
		cache.putAll(check);								// put x 10
		
		for (Map.Entry<String, String> entry : check.entrySet()) {  // hit x 20
			assertEquals(entry.getValue(), cache.get(entry.getKey()));  
		}
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
		return check;
	}

	/**
	 * putIfAbsent
	 */
	@Test
	public void t0012_defaultCache_putIfAbsent() {
		boolean put = cache.putIfAbsent(t0012_key, t0012_value); 	// hit, put
		assertTrue(put);
		put = cache.putIfAbsent(t0012_key, t0012_new_value);		// miss
		assertFalse(put);
		assertEquals(t0012_value, cache.get(t0012_key));			// hit
	}
	protected final String t0012_key = "t0012";
	protected final String t0012_value = "t0012_defaultCache_putIfAbsent";
	protected final String t0012_new_value = "t0012_jibber_jabber";
	
	/**
	 * remove
	 */
	@Test
	public void t0013_defaultCache_remove() {
		cache.put(t0013_key, t0013_value);					// put
		assertEquals(t0013_value, cache.get(t0013_key));	// hit
		boolean removed = cache.remove(t0013_key);			// remove
		assertTrue(removed);
		assertNull(cache.get(t0013_key));					// miss
	}
	protected final String t0013_key = "t0013";
	protected final String t0013_value = "t0013_defaultCache_remove";
	
	/**
	 * remove
	 */
	@Test
	public void t0014_defaultCache_remove() {
		cache.put(t0014_key, t0014_value);						// put
		assertEquals(t0014_value, cache.get(t0014_key));		// hit
		boolean removed = cache.remove("t0014 no such key");	// nothing see spec
		assertFalse(removed);
		assertEquals(t0014_value, cache.get(t0014_key));		// hit
	}
	protected final String t0014_key = "t0014";
	protected final String t0014_value = "t0014_defaultCache_remove";
	
	/**
	 * Cache.removeAll()
	 */
	@Test
	public void t0015_defaultCache_removeAll() {
		cache.clear();
		cache.put(t0015_key, t0015_value);					// put
		assertEquals(t0015_value, cache.get(t0015_key));	// hit	
		cache.removeAll();									// remove
		assertFalse(cache.containsKey(t0015_key));			// nothing see spec for containsKey
		Iterator<Cache.Entry<String,String>> cache_contents = cache.iterator();
		boolean something = cache_contents.hasNext();
		assertFalse(something);
	}
	protected final String t0015_key = "t0015";
	protected final String t0015_value = "t0015_defaultCache_removeAll";
	
	/**
	 * Cache.removeAll(Set<>)
	 */
	@Test
	public void t0016_defaultCache_removeAll() {
		cache.put(t0016_key, t0016_value); 					// put
		assertEquals(t0016_value, cache.get(t0016_key));	// hit 
		cache.put(t0016_key2, t0016_value2);				// put
		assertEquals(t0016_value2, cache.get(t0016_key2));	// hit
		assertFalse(cache.containsKey(t0016_non_key));		// nothing see spec for containsKey
		Set<String> to_remove = new java.util.HashSet<String>(); 
		to_remove.add(t0016_key);
		to_remove.add(t0016_non_key);
		cache.removeAll(to_remove); 						// remove x 1 (get a remove for the element that exists, nothing for the non-existent element 
		assertFalse(cache.containsKey(t0016_key));			// nothing see spec for containsKey
		assertEquals(t0016_value2, cache.get(t0016_key2));	// hit
		assertFalse(cache.containsKey(t0016_non_key));		// nothing see spec for containsKey
	}
	protected final String t0016_key = "t0016";
	protected final String t0016_value = "t0016_defaultCache_removeAll";
	protected final String t0016_key2 = "t0016_2";
	protected final String t0016_value2 = "t0016_defaultCache_removeAll_2";
	protected final String t0016_non_key = "t0016_no_such_key";
	
	/**
	 * replace
	 */
	@Test
	public void t0017_defaultCache_replace() {
		cache.put(t0017_key, t0017_value);								// put
		assertEquals(t0017_value, cache.get(t0017_key));				// hit
		boolean replaced = cache.replace(t0017_key, t0017_new_value);	// hit, put
		assertTrue(replaced);
		assertEquals(t0017_new_value, cache.get(t0017_key));			// hit
	}
	protected final String t0017_key = "t0017";
	protected final String t0017_value = "t0017_defaultCache_replace";
	protected final String t0017_new_value = "t0017_defaultCache_replacement_value";
	
	/**
	 * replace
	 */
	@Test
	public void t0018_defaultCache_replace() {
		String key = "t0018";
		String new_value = "t0018_defaultCache_replacement_value";
		boolean replaced = cache.replace(key, new_value);			// miss
		assertFalse(replaced);
		assertFalse(cache.containsKey(key));						// nothing see spec for containsKey
	}
	
	/**
	 * replace(K,V,V)
	 */
	@Test
	public void t0019_defaultCache_replace() {
		cache.put(t0019_key, t0019_value);											// put
		assertEquals(t0019_value, cache.get(t0019_key));							// hit
		boolean replaced = cache.replace(t0019_key, t0019_value, t0019_new_value);	// hit, put
		assertTrue(replaced);
		assertEquals(t0019_new_value, cache.get(t0019_key));						// hit
	}
	protected final String t0019_key = "t0019";
	protected final String t0019_value = "t0019_defaultCache_replace";
	protected final String t0019_new_value = "t0019_defaultCache_replacement_value";
	
	/**
	 * replace(K,V,V)
	 */
	@Test
	public void t0020_defaultCache_replace() {
		cache.put(t0020_key, t0020_value);												// put
		assertEquals(t0020_value, cache.get(t0020_key));								// hit
		boolean replaced = cache.replace(t0020_key, t0020_mismatched, t0020_new_value); // does not replace but the key is present so it is a hit
		assertFalse(replaced);
		assertEquals(t0020_value, cache.get(t0020_key));								// hit
	}
	protected final String t0020_key = "t0020";
	protected final String t0020_value = "t0020_defaultCache_replace";
	protected final String t0020_mismatched = "t0020_no_such_value";
	protected final String t0020_new_value = "t0020_defaultCache_replacement_value";
	
	/**
	 * replace(K,V,V)
	 */
	@Test
	public void t0021_defaultCache_replace() {
		boolean replaced = cache.replace(t0021_key, t0021_value, t0021_new_value);	// miss
		assertFalse(replaced);
		assertFalse(cache.containsKey(t0021_key));									// nothing see spec for containsKey
	}
	protected final String t0021_key = "t0021";
	protected final String t0021_value = "t0021_defaultCache_replace";
	protected final String t0021_new_value = "t0021_defaultCache_replacement_value";
	
	/**
	 * remove(K,V)
	 */
	@Test
	public void t0022_defaultCache_removeKV() {
		cache.put(t0022_key, t0022_value);						// put
		boolean removed = cache.remove(t0022_key, t0022_value); // hit, remove
		assertTrue(removed);
		assertFalse(cache.containsKey(t0022_key));				// nothing see spec for containsKey
	}
	protected final String t0022_key = "t0022";
	protected final String t0022_value = "t0022_defaultCache_replace";

	/**
	 * remove(K,V)
	 */
	@Test
	public void t0023_defaultCache_removeKV() {
		cache.put(t0023_key, t0023_value);							// put
		boolean removed = cache.remove(t0023_key, t0023_non_value); // miss
		assertFalse(removed);
		assertTrue(cache.containsKey(t0023_key));					// nothing see spec for containsKey
	}
	protected final String t0023_key = "t0023";
	protected final String t0023_value = "t0023_defaultCache_replace";
	protected final String t0023_non_value = "t0023_defaultCache_replaceXXXXXX";

	/**
	 * remove(K,V)
	 * 
	 * Duplication of t0023_defaultCache_removeKV except that initial value for lookup is 
	 * shorter than actual value in cache. Can throw BufferUnderflowException if we don't 
	 * handle correctly.
	 * 
	 */
	@Test
	public void t0024_defaultCache_removeKV() {
		cache.put(t0024_key, t0024_value);							// put
		boolean removed = cache.remove(t0024_key, t0024_non_value);	// miss
		assertFalse(removed);
		assertTrue(cache.containsKey(t0024_key));					// nothing see spec for containsKey
	}
	protected final String t0024_key = "t0024";
	protected final String t0024_value = "t0024_defaultCache_replace";
	protected final String t0024_non_value = "t0024v";
	
	@Test
	public void t0025_iterate_variableSizes() {
		String large = getLargeString("war_and_peace.txt");
		cache.put(t0025_key, large);
		String key2 = "t0025_2";
		String val2 = "Something else";
		cache.put(key2, val2);
		Iterator<Cache.Entry<String,String>> it = cache.iterator();
		Map<String,String> contents = new java.util.HashMap<String,String>();
		while (it.hasNext()) {
			Cache.Entry<String,String> entry = it.next();
			contents.put(entry.getKey(), entry.getValue()); 
		}
		
		assertTrue(contents.containsKey(t0025_key));
		assertEquals(large, contents.get(t0025_key));
		assertTrue(contents.containsKey(key2));
		assertEquals(val2, contents.get(key2));
	}
	protected final String t0025_key = "t0025";

	@Test
	public void t0026_get_large_overflowing() {
		String large = getLargeString("war_and_peace.txt");
		cache.put(t0026_key, large);
		
		String thing = cache.get(t0026_key);
		assertEquals(large,thing);
		
	}
	protected final String t0026_key = "t0026";
	
	@Test
	public void t0027_defaultCache_invoke_setting() {
		cache.put(t0027_key, t0027_val);
		EntryProcessor<String, String, String> processor = new EntryProcessor<String, String, String>() {
			@Override
			public String process(MutableEntry<String, String> entry, Object... arguments)
					throws EntryProcessorException {
				entry.setValue(t0027_val2);
				return t0027_val2;
			}
			
		};
		
		String ret = cache.invoke(t0027_key, processor, new Object[] {});
		
		assertEquals(t0027_val2, ret);
		assertEquals(t0027_val2, cache.get(t0027_key));
	}
	protected final String t0027_key = "t0027";
	protected final String t0027_val = "t0027_defaultCache_invoke_setting";
	protected final String t0027_val2 = "t0027_UPDATED";

	@Test
	public void t0028_defaultCache_invoke_setting() {
		cache.put(t0028_key, t0028_val);
		EntryProcessor<String, String, String> processor = new EntryProcessor<String, String, String>() {
			@Override
			public String process(MutableEntry<String, String> entry, Object... arguments)
					throws EntryProcessorException {
				assertEquals(1, arguments.length);
				assertEquals(t0028_arg, arguments[0]);
				entry.setValue(new StringBuilder().append(t0028_key).append(arguments[0]).toString());
				return entry.getValue();
			}
			
		};
		
		String ret = cache.invoke(t0028_key, processor, new Object[] {t0028_arg});
		String test_val = new StringBuilder().append(t0028_key).append(t0028_arg).toString();
		assertEquals(test_val, ret);
		assertEquals(test_val, cache.get(t0028_key));
	}
	protected final String t0028_key = "t0028";
	protected final String t0028_val = "t0028_defaultCache_invoke_setting";
	protected final String t0028_arg = "_UPDATED";

	@Test
	public void t0029_defaultCache_invoke_removing() {
		cache.put(t0029_key, t0029_val);
		EntryProcessor<String, String, String> processor = new EntryProcessor<String, String, String>() {
			@Override
			public String process(MutableEntry<String, String> entry, Object... arguments)
					throws EntryProcessorException {
				entry.remove();
				return t0029_val2;
			}
			
		};
		
		String ret = cache.invoke(t0029_key, processor, new Object[] {});
		
		assertEquals(t0029_val2, ret);
		assertFalse(cache.containsKey(t0029_key));
	}
	protected final String t0029_key = "t0029";
	protected final String t0029_val = "t0029_defaultCache_invoke_removing";
	protected final String t0029_val2 = "REMOVED";

	@Test
	public void t0030_defaultCache_invoke_exists() {
		cache.put(t0030_key, t0030_val);
		EntryProcessor<String, String, Boolean> processor = new EntryProcessor<String, String, Boolean>() {
			@Override
			public Boolean process(MutableEntry<String, String> entry, Object... arguments)
					throws EntryProcessorException {
				return entry.exists();
			}
			
		};
		
		Boolean ret = cache.invoke(t0030_key, processor, new Object[] {});
		
		assertTrue(ret);
	}
	protected final String t0030_key = "t0030";
	protected final String t0030_val = "t0030_defaultCache_invoke_exists";

	@Test
	public void t0031_defaultCache_invoke_exists() {
		EntryProcessor<String, String, Boolean> processor = new EntryProcessor<String, String, Boolean>() {
			@Override
			public Boolean process(MutableEntry<String, String> entry, Object... arguments)
					throws EntryProcessorException {
				return entry.exists();
			}
			
		};
		
		Boolean ret = cache.invoke(t0031_key, processor, new Object[] {});
		
		assertFalse(ret);
	}
	protected final String t0031_key = "t0031";

	@Test
	public void t0032_defaultCache_invokeAll_setting() {
		cache.put(t0032_key, t0032_val);
		EntryProcessor<String, String, String> processor = new EntryProcessor<String, String, String>() {
			@Override
			public String process(MutableEntry<String, String> entry, Object... arguments)
					throws EntryProcessorException {
				entry.setValue(t0032_val2);
				return t0032_val2;
			}
			
		};
		Set<String> keys = new java.util.HashSet<String>();
		keys.add(t0032_key);
		Map<String,EntryProcessorResult<String>> ret = cache.invokeAll(keys, processor, new Object[] {});
		
		assertTrue(ret.containsKey(t0032_key));
		EntryProcessorResult<String> result = ret.get(t0032_key);
		assertEquals(t0032_val2, result.get());
		assertEquals(t0032_val2, cache.get(t0032_key));
	}
	protected final String t0032_key = "t0032";
	protected final String t0032_val = "t0032_defaultCache_invokeAll_setting";
	protected final String t0032_val2 = "t0032_UPDATED";

	@Test
	public void t0033_defaultCache_invokeAll_setting() {
		cache.put(t0033_key, t0033_val);
		EntryProcessor<String, String, String> processor = new EntryProcessor<String, String, String>() {
			@Override
			public String process(MutableEntry<String, String> entry, Object... arguments)
					throws EntryProcessorException {
				assertEquals(1, arguments.length);
				assertEquals(t0033_arg, arguments[0]);
				entry.setValue(new StringBuilder().append(t0033_key).append(arguments[0]).toString());
				return entry.getValue();
			}
			
		};
		
		Set<String> keys = new java.util.HashSet<String>();
		keys.add(t0033_key);
		Map<String,EntryProcessorResult<String>> ret = cache.invokeAll(keys, processor, new Object[] {t0033_arg});
		assertTrue(ret.containsKey(t0033_key));
		EntryProcessorResult<String> result = ret.get(t0033_key);
		String test_val = new StringBuilder().append(t0033_key).append(t0033_arg).toString();
		assertEquals(test_val, result.get());
		assertEquals(test_val, cache.get(t0033_key));
	}
	protected final String t0033_key = "t0033";
	protected final String t0033_val = "t0033_defaultCache_invokeAll_setting";
	protected final String t0033_arg = "_UPDATED";

	@Test
	public void t0034_defaultCache_invokeAll_removing() {
		cache.put(t0034_key, t0034_val);
		EntryProcessor<String, String, String> processor = new EntryProcessor<String, String, String>() {
			@Override
			public String process(MutableEntry<String, String> entry, Object... arguments)
					throws EntryProcessorException {
				entry.remove();
				return t0034_val2;
			}
			
		};
		
		Set<String> keys = new java.util.HashSet<String>();
		keys.add(t0034_key);
		Map<String,EntryProcessorResult<String>> ret = cache.invokeAll(keys, processor, new Object[] {});
		
		assertTrue(ret.containsKey(t0034_key));
		EntryProcessorResult<String> result = ret.get(t0034_key);
		assertEquals(t0034_val2, result.get());
		assertFalse(cache.containsKey(t0034_key));
	}
	protected final String t0034_key = "t0034";
	protected final String t0034_val = "t0034_defaultCache_invokeAll_removing";
	protected final String t0034_val2 = "REMOVED";

	@Test
	public void t0035_defaultCache_invokeAll_exists() {
		cache.put(t0035_key, t0035_val);
		EntryProcessor<String, String, Boolean> processor = new EntryProcessor<String, String, Boolean>() {
			@Override
			public Boolean process(MutableEntry<String, String> entry, Object... arguments)
					throws EntryProcessorException {
				return entry.exists();
			}
			
		};
		
		Set<String> keys = new java.util.HashSet<String>();
		keys.add(t0035_key);
		Map<String,EntryProcessorResult<Boolean>> ret = cache.invokeAll(keys, processor, new Object[] {});
		
		assertTrue(ret.containsKey(t0035_key));
		EntryProcessorResult<Boolean> result = ret.get(t0035_key);
		assertTrue(result.get());
	}
	protected final String t0035_key = "t0035";
	protected final String t0035_val = "t0035_defaultCache_invokeAll_exists";

	@Test
	public void t0036_defaultCache_invokeAll_exists() {
		EntryProcessor<String, String, Boolean> processor = new EntryProcessor<String, String, Boolean>() {
			@Override
			public Boolean process(MutableEntry<String, String> entry, Object... arguments)
					throws EntryProcessorException {
				return entry.exists();
			}
			
		};
		
		Set<String> keys = new java.util.HashSet<String>();
		keys.add(t0036_key);
		Map<String,EntryProcessorResult<Boolean>> ret = cache.invokeAll(keys, processor, new Object[] {});
		
		assertTrue(ret.containsKey(t0036_key));
		EntryProcessorResult<Boolean> result = ret.get(t0036_key);
		assertFalse(result.get());
	}
	protected final String t0036_key = "t0036";

	@Test
	public void t0037_defaultCache_invoke_getting() {
		cache.put(t0037_key, t0037_val);
		EntryProcessor<String, String, String> processor = new EntryProcessor<String, String, String>() {
			@Override
			public String process(MutableEntry<String, String> entry, Object... arguments)
					throws EntryProcessorException {
				return entry.getValue();
			}
			
		};
		
		String ret = cache.invoke(t0037_key, processor, new Object[] {});
		assertEquals(t0037_val, ret);
	}
	protected final String t0037_key = "t0037";
	protected final String t0037_val = "t0037_defaultCache_invoke_getting";

	@Test
	public void t0038_defaultCache_invokeAll_getting() {
		cache.put(t0038_key, t0038_val);
		EntryProcessor<String, String, String> processor = new EntryProcessor<String, String, String>() {
			@Override
			public String process(MutableEntry<String, String> entry, Object... arguments)
					throws EntryProcessorException {
				return entry.getValue();
			}
			
		};
		
		Set<String> keys = new java.util.HashSet<String>();
		keys.add(t0038_key);
		Map<String,EntryProcessorResult<String>> ret = cache.invokeAll(keys, processor, new Object[] {});
		assertTrue(ret.containsKey(t0038_key));
		EntryProcessorResult<String> result = ret.get(t0038_key);
		assertEquals(t0038_val, result.get());
	}
	protected final String t0038_key = "t0038";
	protected final String t0038_val = "t0038_defaultCache_invokeAll_getting";

	@Test
	public void t0039_defaultCache_invokeAll_getting() {
		cache.put(t0039_key, t0039_val);
		EntryProcessor<String, String, String> processor = new EntryProcessor<String, String, String>() {
			@Override
			public String process(MutableEntry<String, String> entry, Object... arguments)
					throws EntryProcessorException {
				return entry.getValue();
			}
			
		};
		
		Set<String> keys = new java.util.HashSet<String>();
		keys.add(t0039_key);
		keys.add("t0039_NO_KEY");
		Map<String,EntryProcessorResult<String>> ret = cache.invokeAll(keys, processor, new Object[] {});
		assertEquals(1, ret.size());
		assertTrue(ret.containsKey(t0039_key));
		EntryProcessorResult<String> result = ret.get(t0039_key);
		assertEquals(t0039_val, result.get());
	}
	protected final String t0039_key = "t0039";
	protected final String t0039_val = "t0039_defaultCache_invokeAll_getting";

	@Test
	public void t0040_defaultCache_invokeAll_setting() {
		// put + 1
		cache.put(t0040_key, t0040_val);
		EntryProcessor<String, String, String> processor = new EntryProcessor<String, String, String>() {
			@Override
			public String process(MutableEntry<String, String> entry, Object... arguments)
					throws EntryProcessorException {
				entry.setValue(t0040_val2);
				return entry.getValue();
			}
			
		};
		
		Set<String> keys = new java.util.HashSet<String>();
		keys.add(t0040_key);
		keys.add(t0040_no_key);
		// put + 2 / hit + 1 / miss + 1
		Map<String,EntryProcessorResult<String>> ret = cache.invokeAll(keys, processor, new Object[] {});
		assertEquals(2, ret.size());
		assertTrue(ret.containsKey(t0040_key));
		EntryProcessorResult<String> result = ret.get(t0040_key);
		assertEquals(t0040_val2, result.get());
		assertTrue(ret.containsKey(t0040_no_key));
		result = ret.get(t0040_no_key);
		assertEquals(t0040_val2, result.get());
		
		assertTrue(cache.containsKey(t0040_no_key));
		
		// hit + 1
		assertEquals(t0040_val2, cache.get(t0040_no_key));
	}
	protected final String t0040_key = "t0040";
	protected final String t0040_val = "t0040_defaultCache_invokeAll_setting";
	protected final String t0040_no_key = "t0040_NO_KEY";
	protected final String t0040_val2 = "t0040_UPDATED";


	@Test
	public void t0041_defaultCache_invokeAll_removing() {
		// put + 1
		cache.put(t0041_key, t0041_val);
		EntryProcessor<String, String, String> processor = new EntryProcessor<String, String, String>() {
			@Override
			public String process(MutableEntry<String, String> entry, Object... arguments)
					throws EntryProcessorException {
				String removedKey = entry.getKey();
				entry.remove();
				return removedKey;
			}
			
		};
		
		Set<String> keys = new java.util.HashSet<String>();
		keys.add(t0041_key);
		keys.add(t0041_no_key);
		// hit + 1 / miss + 1 / removal + 1
		Map<String,EntryProcessorResult<String>> ret = cache.invokeAll(keys, processor, new Object[] {});
		assertEquals(2, ret.size());
		assertTrue(ret.containsKey(t0041_key));
		EntryProcessorResult<String> result = ret.get(t0041_key);
		assertEquals(t0041_key, result.get());
		assertTrue(ret.containsKey(t0041_no_key));
		result = ret.get(t0041_no_key);
		assertEquals(t0041_no_key, result.get());
		
		assertFalse(cache.containsKey(t0041_no_key));
	}
	protected final String t0041_key = "t0041";
	protected final String t0041_val = "t0041_defaultCache_invokeAll_removing";
	protected final String t0041_no_key = "t0041_NO_KEY";

	@Test
	public void t0042_defaultCache_invokeAll_exists() {
		// put + 1
		cache.put(t0042_key, t0042_val);
		EntryProcessor<String, String, Boolean> processor = new EntryProcessor<String, String, Boolean>() {
			@Override
			public Boolean process(MutableEntry<String, String> entry, Object... arguments)
					throws EntryProcessorException {
				return entry.exists();
			}
			
		};
		
		Set<String> keys = new java.util.HashSet<String>();
		keys.add(t0042_key);
		keys.add(t0042_no_key);
		// hit + 1 / miss + 1 
		Map<String,EntryProcessorResult<Boolean>> ret = cache.invokeAll(keys, processor, new Object[] {});
		assertEquals(2, ret.size());
		assertTrue(ret.containsKey(t0042_key));
		EntryProcessorResult<Boolean> result = ret.get(t0042_key);
		assertTrue(result.get());
		assertTrue(ret.containsKey(t0042_no_key));
		result = ret.get(t0042_no_key);
		assertFalse(result.get());
	}
	protected final String t0042_key = "t0042";
	protected final String t0042_val = "t0042_defaultCache_invokeAll_exists";
	protected final String t0042_no_key = "t0042_NO_KEY";

	@Test
	public void t0043_defaultCache_invokeAll_setting_throwing() {
		// put + 11
		Set<String> keys = new java.util.HashSet<String>();
		for (int i = 0; i < 10; i ++) {
			String key = new StringBuilder().append(t0043_key).append("_").append(i).toString();
			cache.put(key, t0043_val);
			keys.add(key);
		}
		cache.put(t0043_death_key, t0043_val);
		keys.add(t0043_death_key);
		assertEquals(11, keys.size());
		EntryProcessor<String, String, String> processor = new EntryProcessor<String, String, String>() {
			@Override
			public String process(MutableEntry<String, String> entry, Object... arguments)
					throws EntryProcessorException {
				if (entry.getKey().equals(t0043_death_key)) {
					throw new ArrayIndexOutOfBoundsException(); // throw some old cock ...
				}
				entry.setValue(t0043_val2);
				return t0043_return;
			}
			
		};
		
		// hit + 11 / put + 10
		Map<String,EntryProcessorResult<String>> ret = cache.invokeAll(keys, processor, new Object[] {});
		assertEquals(11, ret.size());
		
		// hit + 11
		for (String key : keys) {
			assertTrue(ret.containsKey(key));
			EntryProcessorResult<String> result = ret.get(key);
			if (key.equals(t0043_death_key)) {
				assertTrue(result instanceof EntryProcessorExceptionNoResult);
				EntryProcessorExceptionNoResult<String> eresult = (EntryProcessorExceptionNoResult<String>) result;
				try {
					eresult.get();
				} catch (ArrayIndexOutOfBoundsException e) {
					assertNull("Unwrapped exception in entry processor result!", e);
				} catch (EntryProcessorException e) {
					ArrayIndexOutOfBoundsException expected = null;
					assertNotNull(e.getCause());
					try {
						throw e.getCause();
					} catch (ArrayIndexOutOfBoundsException a) {
						expected = a;
					} catch (Throwable t) {
						assertNull("Unexpected throwable in entry processor result!", t);
					}
					assertNotNull(expected);
				} catch (Throwable t) {
					assertNull("Unexpected throwable in entry processor result!", t);
				}
				assertEquals(t0043_val, cache.get(key));
			} else {
				assertEquals(t0043_return, result.get());
				assertEquals(t0043_val2, cache.get(key));
			}
		}
	}
	protected final String t0043_key = "t0043";
	protected final String t0043_death_key = "t0043_DEATH";
	protected final String t0043_val = "t0043_defaultCache_invokeAll_setting_throwing";
	protected final String t0043_val2 = "t0043_UPDATED";
	private final String t0043_return = "banana";

}
