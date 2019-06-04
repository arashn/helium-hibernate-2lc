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

import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

import com.levyx.helium.Helium;
import com.levyx.helium.HeliumException;
import com.levyx.helium.HeliumItem;
import com.levyx.helium.HeliumIterator;

@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HeliumTest {
	
	protected static Helium helium;
	
	@BeforeClass
	public static void setUp() {
		String DEVICE_PATH = "D:/Levyx/Helium/HE_BLOCK.DAT";
        String heliumURL = String.format("he://./%s", DEVICE_PATH);
        System.out.printf("Opening \"%s\"...\n", heliumURL);
		helium = new Helium(heliumURL, "HeliumTest", Helium.HE_O_CREATE | Helium.HE_O_TRUNCATE | Helium.HE_O_VOLUME_CREATE);
		
	}
	
	@AfterClass
	public static void tearDown() {
		helium.close();
	}
	
	public HeliumTest() {
	}

	private HeliumItem item(String key, String value) {
		byte[] byte_key = key.getBytes(Charset.forName("UTF-8"));
		byte[] byte_val = value.getBytes(Charset.forName("UTF-8"));
		HeliumItem item = new HeliumItem(byte_key.length, byte_val.length);
		item.setKeyBytes(byte_key);
		item.setValueBytes(byte_val);
		return item;
	}
	
	private HeliumItem item(String key) {
		byte[] byte_key = key.getBytes(Charset.forName("UTF-8"));
		HeliumItem item = new HeliumItem(byte_key.length, 512);
		item.setKeyBytes(byte_key);
		return item;
	}
	
	
	/**
	 * containskey
	 */
//	@Test
	public void t0000_defaultCache_containsKey() {
		final String key = "t0000";
		final String value = "t0000_defaultCache_containsKey";
		HeliumItem item = item(key, value);
		helium.insert(item);
	}
	
	/**
	 * put / get a single key
	 */
//	@Test
	public void t0001_defaultCache_put_get() {
		HeliumItem item = item(t0001_key, t0001_value);
		helium.insert(item);
//		assertEquals(t0001_value, cache.get(t0001_key));
	}

	protected final String t0001_key= "t0001";
	protected final String t0001_value = "A valuable piece of string";

	/**
	 * put / get / put / get, for same key, showing overwriting of value
	 */
//	@Test
	public void t0002_defaultCache_put_get_overwrite() {
		helium.insert(item(t0002_key, t0002_value));
//		assertEquals(t0002_value, cache.get(t0002_key));
		helium.update(item(t0002_key, t0002_value2));
//		assertEquals(t0002_value2, cache.get(t0002_key));
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
		
		helium.insert(item(key, value));
		helium.insert(item(key2, value2));
		Set<String> keys = new java.util.HashSet<String>(7);
//		keys.add(key);
//		keys.add(key2);
//		Map<String,String> results = cache.getAll(keys);
//		assertEquals(2, results.size());
//		assertEquals(value, cache.get(key));
//		assertEquals(value2, cache.get(key2));
	}
	
	/**
	 * put / get / getAndPut, for same key, showing overwriting of value
	 */
//	@Test
	public void t0004_defaultCache_getAndPut() {
		
		helium.insert(item(t0004_key, t0004_value));
//		assertEquals(t0004_value, cache.get(t0004_key));
		helium.lookup(item(t0004_key));
		helium.update(item(t0004_key, t0004_value2));
//		String old_value = cache.getAndPut(t0004_key, t0004_value2);
//		assertEquals(t0004_value, old_value);
//		assertEquals(t0004_value2, cache.get(t0004_key));
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
//	@Test
	public void t0004_defaultCache_getAndPut_2() {
		helium.insert(item(t0004_2_key, t0004_2_value));
//		assertEquals(t0004_2_value, cache.get(t0004_2_key));
		helium.lookup(item(t0004_2_key));
		helium.update(item(t0004_2_key, t0004_2_value2));
//		String old_value = cache.getAndPut(t0004_2_key, t0004_2_value2);
//		assertEquals(t0004_2_value, old_value);
//		assertEquals(t0004_2_value2, cache.get(t0004_2_key));
	}
	protected final String t0004_2_key = "z0004";
	protected final String t0004_2_value = "z0004 first value";
	protected final String t0004_2_value2 = "xyz";

	/**
	 * put / get / getAndRemove, for same key
	 */
//	@Test
	public void t0005_defaultCache_getAndRemove() {
		
		helium.insert(item(t0005_key, t0005_value));
//		assertEquals(t0005_value, cache.get(t0005_key));
		helium.deleteLookup(item(t0005_key));
//		String old_value = cache.getAndRemove(t0005_key);
//		assertEquals(t0005_value, old_value);
//		assertFalse(cache.containsKey(t0005_key));
	}
	protected final String t0005_key = "t0005";
	protected final String t0005_value = "t0005 test value";
	
	/**
	 * put / get / getAndReplace, for same key, showing overwriting of value
	 */
//	@Test
	public void t0006_defaultCache_getAndReplace() {
		
		helium.insert(item(t0006_key, t0006_value));
//		assertEquals(t0006_value, cache.get(t0006_key));
		helium.lookup(item(t0006_key));
		helium.replace(item(t0006_key, t0006_value2));
//		String old_value = cache.getAndReplace(t0006_key, t0006_value2);
//		assertEquals(t0006_value, old_value);
//		assertEquals(t0006_value2, cache.get(t0006_key));
	}
	protected final String t0006_key = "t0006";
	protected final String t0006_value = t0006_key + " first value";
	protected final String t0006_value2 = t0006_key + " second value";
	
	/**
	 * invoke using an EntryProcessor
	 */
//	@Test
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
		helium.insert(item(no_vowels_key, "cdfgxhbklz"));
		String some_vowels_key = "t0007_some_vowels";
		helium.insert(item(some_vowels_key, "llangollen"));
		String all_the_vowels_key = "t0007_all_the_vowels";
		helium.insert(item(all_the_vowels_key, "the quick brown fox jumped over the lazy dog"));
		
		MutableEntry<String,String> entry = new MutableEntry<String,String>() {
			@Override
			public String getKey() {
				return no_vowels_key;
			}
			@Override
			public String getValue() {
				HeliumItem item = item(getKey());
				helium.lookup(item);
				return new String(item.getValueBytes());
			}
			@Override
			public <T> T unwrap(Class<T> clazz) {return null;}
			@Override
			public boolean exists() {return false;}
			@Override
			public void remove() {}
			@Override
			public void setValue(String value) {}
		};
		Map<String,Integer> result = vowel_counter.process(entry, new Object[] {});
		
		//Map<String,Integer> result = cache.invoke(no_vowels_key, vowel_counter, new Object[] {});
		assertTrue(result.isEmpty());
//		result = cache.invoke(some_vowels_key, vowel_counter, new Object[] {});
		entry = new MutableEntry<String,String>() {
			@Override
			public String getKey() {
				return some_vowels_key;
			}
			@Override
			public String getValue() {
				HeliumItem item = item(getKey());
				helium.lookup(item);
				return new String(item.getValueBytes());
			}
			@Override
			public <T> T unwrap(Class<T> clazz) {return null;}
			@Override
			public boolean exists() {return false;}
			@Override
			public void remove() {}
			@Override
			public void setValue(String value) {}
		};
		result = vowel_counter.process(entry, null);
		assertEquals(3, result.size());
		assertEquals(1, result.get("a").intValue());
		assertEquals(1, result.get("e").intValue());
		assertEquals(1, result.get("o").intValue());
//		result = cache.invoke(all_the_vowels_key, vowel_counter, new Object[] {});
		entry = new MutableEntry<String,String>() {
			@Override
			public String getKey() {
				return all_the_vowels_key;
			}
			@Override
			public String getValue() {
				HeliumItem item = item(getKey());
				helium.lookup(item);
				return new String(item.getValueBytes());
			}
			@Override
			public <T> T unwrap(Class<T> clazz) {return null;}
			@Override
			public boolean exists() {return false;}
			@Override
			public void remove() {}
			@Override
			public void setValue(String value) {}
		};
		result = vowel_counter.process(entry, null);
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
//	@Test
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
		
		MutableEntry entry = new MutableEntry<String,String>() {
			@Override
			public String getKey() {
				return xkey;
			}
			@Override
			public String getValue() {
				HeliumItem item = item(getKey());
				try {
				helium.lookup(item);
				} catch (HeliumException hex) {
					assertEquals(Helium.HE_ERR_ITEM_NOT_FOUND, hex.getErrorCode());
					return null;
				}
				
				return new String(item.getValueBytes());
			}
			@Override
			public <T> T unwrap(Class<T> clazz) {return null;}
			@Override
			public boolean exists() {return false;}
			@Override
			public void remove() {}
			@Override
			public void setValue(String value) {}
		};
		String result = processor.process(entry, null);
//		String result = cache.invoke(xkey, processor, new Object[] {});
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
//	@Test
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
		helium.insert(item(no_fruits_key, "the quick brown fox jumped over the lazy dog"));
		String some_fruits_key = "t0007_some_fruits";
		helium.insert(item(some_fruits_key, "the quick brown banana jumped over the lazy grape"));
		String no_such_key = "shurely shome mishtake";
		
		Set<String> keys_to_check = new java.util.HashSet<String>(7);
		keys_to_check.add(no_fruits_key);
		keys_to_check.add(some_fruits_key);
		keys_to_check.add(no_such_key);
		
		Map<String,EntryProcessorResult<Boolean>> result = new java.util.HashMap<String, EntryProcessorResult<Boolean>>();
		for (String key : keys_to_check) {
			MutableEntry<String,String> entry = new MutableEntry<String,String>() {
				@Override
				public String getKey() {
					return key;
				}
				@Override
				public String getValue() {
					HeliumItem item = item(getKey());
					try {
					helium.lookup(item);
					} catch (HeliumException hex) {
						assertEquals(Helium.HE_ERR_ITEM_NOT_FOUND, hex.getErrorCode());
						return null;
					}
					
					return new String(item.getValueBytes());
				}
				@Override
				public <T> T unwrap(Class<T> clazz) {return null;}
				@Override
				public boolean exists() {return false;}
				@Override
				public void remove() {}
				@Override
				public void setValue(String value) {}
			};
			Boolean b = fruit_detector.process(entry, new Object[] {});
			result.put(key, new EntryProcessorResult<Boolean>() {
				private Boolean booo = b;

				@Override
				public Boolean get() throws EntryProcessorException {
					return booo;
				}
			});
		}
//		Map<String,EntryProcessorResult<Boolean>> result = cache.invokeAll(keys_to_check, fruit_detector, new Object[] {});
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
		helium.insert(item(key, value));
		//assertEquals(value, cache.get(key));
		HeliumIterator he_iterator = helium.iterator();
		List<byte[]> to_remove = new java.util.ArrayList<byte[]>();
		while (he_iterator.hasNext()) {
			HeliumItem item = he_iterator.next();
			to_remove.add(item.getKeyBytes());
		}
		for (byte[] byte_key : to_remove) {
			HeliumItem item = new HeliumItem(byte_key.length, 512);
			item.setKeyBytes(byte_key);
			helium.delete(item);
		}
		//cache.clear();
		try {
			helium.lookup(item(key));
		} catch (HeliumException hex) { 
			assertEquals(Helium.HE_ERR_ITEM_NOT_FOUND, hex.getErrorCode());
		}
//		Iterator<Cache.Entry<String,String>> cache_contents = cache.iterator();
//		boolean something = cache_contents.hasNext();
		he_iterator = helium.iterator();
		boolean something = he_iterator.hasNext();
		assertFalse(something);
	}
	
	@Test
	public void t0010_defaultCache_iterator() {
		Map<String,String> check = new java.util.HashMap<String,String>();
		String kroot = "t0010_";
		String vroot = "t0010_defaultCache_iterator_";
		for (int i = 0; i < 8; i++) {
			String key = new StringBuilder().append(kroot).append(i).toString(); 
			String value = new StringBuilder().append(vroot).append(i).toString(); 
			System.out.println("Adding " + key + " -> " + value);
			helium.update(item(key, value));
			check.put(key, value);
		}
		Iterator<HeliumItem> cache_contents = helium.iterator();
		while (cache_contents.hasNext()) {
			HeliumItem item = cache_contents.next();
			String key = new String(item.getKeyBytes());
			String value = new String(item.getValueBytes());
			System.out.printf("Iterator.next(): %s => %s\n", key, value);
			System.out.println("Removing " + key + " -> " + value);
			String match = check.remove(key);
			if (match != null) {
				assertEquals(match, value);
			}
		}
		System.out.println("check contents: " + check);
		assertTrue(check.isEmpty());
	}
	

}
