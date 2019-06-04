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

import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.cache.Cache;
import javax.cache.Cache.Entry;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.Factory;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.integration.CacheWriter;
import javax.cache.integration.CacheWriterException;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.EntryProcessorResult;
import javax.cache.processor.MutableEntry;
import javax.cache.spi.CachingProvider;

import org.junit.AfterClass;
import org.junit.Before;
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
public class CacheWriteThroughTest {
	protected static Cache<String, String> cache;
	protected static final Map<String,String> write_through = new java.util.HashMap<String,String>();

	protected static final String KEY_A = "KeyA";
	protected static final String VAL_A = "First known value";
	protected static final String KEY_B = "KeyB";
	protected static final String VAL_B = "Second known value";
	protected static final String KEY_C = "KeyC";
	protected static final String VAL_C = "Third known value";

	
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
		config.setCacheWriterFactory(new Factory<CacheWriter<? super String,? super String>>() {
			@Override
			public CacheWriter<? super String, ? super String> create() {
				return new CacheWriter<String, String>() {
					@Override
					public void write(Entry<? extends String, ? extends String> entry) throws CacheWriterException {
						write_through.put(entry.getKey(), entry.getValue());
					}

					@Override
					public void writeAll(Collection<Entry<? extends String, ? extends String>> entries)
							throws CacheWriterException {
						for (Entry<? extends String, ? extends String> entry : entries) {
							write_through.put(entry.getKey(), entry.getValue());
						}
						entries.clear();
					}

					@Override
					public void delete(Object key) throws CacheWriterException {
						write_through.remove(key);
					}

					@Override
					public void deleteAll(Collection<?> keys) throws CacheWriterException {
						for (Object key : keys) {
							write_through.remove(key);
						}
						keys.clear();
						
					}
				};
			}
		});
		config.setWriteThrough(true);
		cache = mngr.createCache("TestDatastore", config);
		
		cache.put(KEY_A, VAL_A);
		cache.put(KEY_B, VAL_B);
		cache.put(KEY_C, VAL_C);
	}
	
	@AfterClass
	public static void tearDown() {
		cache.close();
	}

	@Before
	public void clean() {
		cache.put(KEY_A, VAL_A);
		cache.put(KEY_B, VAL_B);
		cache.put(KEY_C, VAL_C);
		write_through.clear();
	}
	
	public CacheWriteThroughTest() {
	}

	@Test
	public void t0001_getAndPut() {
		assertNull(write_through.get(KEY_A));
		String xval = "t0001_getAndPut";
		String value = cache.getAndPut(KEY_A, xval);
		assertEquals(VAL_A, value);
		assertEquals(xval, cache.get(KEY_A));
		assertEquals(xval, write_through.get(KEY_A));
	}
	
	@Test
	public void t0002_getAndRemove() {
		write_through.put(KEY_B, VAL_B);
		assertEquals(VAL_B, write_through.get(KEY_B));
		String value = cache.getAndRemove(KEY_B);
		assertEquals(VAL_B, value);
		assertFalse(cache.containsKey(KEY_B));
		assertFalse(write_through.containsKey(KEY_B));
	}
	
	@Test
	public void t0003_getAndReplace() {
		String xval = "t0003_getAndReplace";
		write_through.put(KEY_B, VAL_B);
		assertEquals(VAL_B, write_through.get(KEY_B));
		String value = cache.getAndReplace(KEY_B, xval);
		assertEquals(VAL_B, value);
		assertEquals(xval, cache.get(KEY_B));
		assertEquals(xval, write_through.get(KEY_B));
	}
	
	@Test
	public void t0004_getAndReplace_2() {
		String xval = "t0004_getAndReplace_no_replace";
		write_through.put(KEY_B, xval);
		assertEquals(xval, write_through.get(KEY_B));
		String value = cache.getAndReplace(KEY_B, VAL_B);
		assertEquals(VAL_B, value);
		assertEquals(VAL_B, cache.get(KEY_B));
		assertEquals(VAL_B, write_through.get(KEY_B));
	}

	@Test
	public void t0004_getAndReplace_3() {
		String xkey = "t0004_3";
		String xval = "t0004_getAndReplace_3";
		String xval2 = "t0004_getAndReplace_3_rpelace_replace";
		write_through.put(xkey, xval);
		assertEquals(xval, write_through.get(xkey));
		String value = cache.getAndReplace(xkey, xval2);
		assertEquals(null, value);
		assertFalse(cache.containsKey(xkey));
		assertEquals(null, cache.get(xkey));
		assertEquals(xval, write_through.get(xkey));
	}

	@Test
	public void t0005_invoke() {
		String xkey = KEY_A;
		String xval = "t0005_invoke";
		write_through.put(xkey, xval);
		assertEquals(xval, write_through.get(xkey));
		EntryProcessor<String,String,String> processor = new EntryProcessor<String, String, String>() {
			@Override
			public String process(MutableEntry<String, String> entry, Object... arguments)
					throws EntryProcessorException {
				String val = entry.getKey() + " -> " + entry.getValue();
				entry.setValue(val);
				return val;
			}
		};
		
		String result = cache.invoke(KEY_A, processor, new Object[] {});
		assertEquals(KEY_A + " -> " + VAL_A, result);
		assertEquals(result, cache.get(KEY_A));
		assertEquals(result, write_through.get(xkey));
	}

	@Test
	public void t0006_invokeAll() {
		String xkey = KEY_B;
		String xval = "t0006_invokeAll";
		write_through.put(xkey, xval);
		assertEquals(xval, write_through.get(xkey));
		EntryProcessor<String,String,String> processor = new EntryProcessor<String, String, String>() {
			@Override
			public String process(MutableEntry<String, String> entry, Object... arguments)
					throws EntryProcessorException {
				String val = entry.getKey() + " -> " + entry.getValue();
				entry.setValue(val);
				return val;
			}
		};
		Set<String> keys = new java.util.HashSet<String>(7);
		keys.add(KEY_A);
		keys.add(KEY_B);
		Map<String,EntryProcessorResult<String>> result = cache.invokeAll(keys, processor, new Object[] {});
		String key_a_result = result.get(KEY_A).get();
		assertEquals(KEY_A + " -> " + VAL_A, key_a_result);
		assertEquals(key_a_result, cache.get(KEY_A));
		assertEquals(key_a_result, write_through.get(KEY_A));
		String key_b_result = result.get(KEY_B).get();
		assertEquals(KEY_B + " -> " + VAL_B, key_b_result);
		assertEquals(key_b_result, cache.get(KEY_B));
		assertEquals(key_b_result, write_through.get(KEY_B));
	}
	
	@Test
	public void t0007_put() {
		String xkey = "t0007";
		String xval = "t0007_put";
		
		assertFalse(write_through.containsKey(xkey));
		
		cache.put(xkey, xval);
		
		assertEquals(xval, cache.get(xkey));
		assertEquals(xval, write_through.get(xkey));
	}
	
	@Test
	public void t0008_put() {
		String xkey = "t0007";
		String xval = "t0007_put";
		
		assertFalse(write_through.containsKey(xkey));
		
		cache.put(xkey, xval);
		
		assertEquals(xval, cache.get(xkey));
		assertEquals(xval, write_through.get(xkey));
	}

	
	@Test
	public void t0009_put() {
		String xkey = KEY_C;
		String xval = "t0009_put";
		
		assertFalse(write_through.containsKey(xkey));
		
		cache.put(xkey, xval);
		
		assertEquals(xval, cache.get(xkey));
		assertEquals(xval, write_through.get(xkey));
	}
	
	@Test
	public void t0010_put() {
		String xkey = KEY_C;
		String xval = KEY_C;
		
		assertFalse(write_through.containsKey(xkey));
		
		cache.put(xkey, xval);
		
		assertEquals(xval, cache.get(xkey));
		assertTrue(write_through.containsKey(xkey));
	}
	
	@Test
	public void t0011_putAll() {
		String xkey = "t0011";
		String xval = "t0011_putAll";
		
		assertFalse(write_through.containsKey(xkey));
		
		Map<String,String> xmap = new java.util.HashMap<String,String>();
		xmap.put(xkey, xval);
		cache.putAll(xmap);
		
		assertEquals(xval, cache.get(xkey));
		assertEquals(xval, write_through.get(xkey));
	}
	
	@Test
	public void t0012_putAll() {
		String xkey = KEY_B;
		String xval = "t0012_putAll";
		
		assertFalse(write_through.containsKey(xkey));
		
		Map<String,String> xmap = new java.util.HashMap<String,String>();
		xmap.put(xkey, xval);
		cache.putAll(xmap);
		
		assertEquals(xval, cache.get(xkey));
		assertEquals(xval, write_through.get(xkey));
	}
	
	@Test
	public void t0013_putIfAbsent() {
		String xkey = "t0013";
		String xval = "t0013_putIfAbsent";
		
		assertFalse(write_through.containsKey(xkey));
		
		boolean modified = cache.putIfAbsent(xkey, xval);
		
		assertTrue(modified);
		assertEquals(xval, cache.get(xkey));
		assertEquals(xval, write_through.get(xkey));
	}
	
	@Test
	public void t0014_putIfAbsent() {
		String xkey = KEY_A;
		String xval = "t0013_putIfAbsent";
		
		assertFalse(write_through.containsKey(xkey));
		
		boolean modified = cache.putIfAbsent(xkey, xval);
		
		assertFalse(modified);
		assertEquals(VAL_A, cache.get(xkey));
		assertFalse(write_through.containsKey(xkey));
	}

	@Test
	public void t0015_remove() {
		write_through.put(KEY_A, "t0015_remove");
		cache.remove(KEY_A);
		assertFalse(write_through.containsKey(KEY_A));
	}

	@Test
	public void t0016_remove() {
		String xkey = "t0016";
		String xval = "t0016_remove";
		write_through.put(xkey, xval);
		boolean modified = cache.remove(xkey);
		assertFalse(modified);
		assertFalse(write_through.containsKey(xkey));
	}

	@Test
	public void t0017_remove() {
		write_through.put(KEY_A, VAL_A);
		boolean modified = cache.remove(KEY_A, VAL_A);
		assertTrue(modified);
		assertFalse(write_through.containsKey(KEY_A));
	}

	@Test
	public void t0018_remove() {
		write_through.put(KEY_A, VAL_A);
		boolean modified = cache.remove(KEY_A, "t0017_remove");
		assertFalse(modified);
		assertTrue(write_through.containsKey(KEY_A));
		assertEquals(VAL_A, write_through.get(KEY_A));
	}
	
	@Test
	public void t0019_removeAll() {
		String xkey = "t0020";
		String xval = "t0020_removeAll";
		write_through.put(KEY_A, VAL_A);
		write_through.put(KEY_B, xval);
		write_through.put(KEY_C, VAL_C);
		write_through.put(xkey, xval);
		cache.removeAll();
		// not sure if this is right ...
		assertEquals(1, write_through.size());
		assertEquals(xval, write_through.get(xkey));
	}

	@Test
	public void t0020_removeAll() {
		String xval = "t0020_removeAll";
		write_through.put(KEY_A, VAL_A);
		write_through.put(KEY_B, xval);
		write_through.put(KEY_C, VAL_C);
		Set<String> keys = new java.util.HashSet<String>();
		keys.add(KEY_A);
		keys.add(KEY_B);
		cache.removeAll(keys);
		assertFalse(write_through.containsKey(KEY_A));
		assertFalse(write_through.containsKey(KEY_B));
		assertTrue(write_through.containsKey(KEY_C));
	}

	@Test
	public void t0021_removeAll() {
		String xkey = "t0021";
		String xval = "t0021_removeAll";
		write_through.put(KEY_A, VAL_A);
		write_through.put(xkey, xval);
		write_through.put(KEY_C, VAL_C);
		Set<String> keys = new java.util.HashSet<String>();
		keys.add(KEY_A);
		keys.add(xkey);
		cache.removeAll(keys);
		assertFalse(write_through.containsKey(KEY_A));
		assertFalse(write_through.containsKey(xkey));
		assertTrue(write_through.containsKey(KEY_C));
	}
	
	@Test
	public void t0022_replace() {
		String xval = "t0022_replace";
		write_through.put(KEY_A, VAL_A);
		boolean modified = cache.replace(KEY_A, xval);
		assertTrue(modified);
		assertEquals(xval, write_through.get(KEY_A));
	}
	
	@Test
	public void t0023_replace() {
		String xkey = "t0023";
		String xval = "t0023_replace";
		write_through.put(xkey, xval);
		boolean modified = cache.replace(xkey, "???");
		assertFalse(modified);
		assertEquals(xval, write_through.get(xkey));
	}
	
	@Test
	public void t0024_replace() {
		String xval = "t0024_replace";
		write_through.put(KEY_A, VAL_A);
		boolean modified = cache.replace(KEY_A, VAL_A, xval);
		assertTrue(modified);
		assertEquals(xval, write_through.get(KEY_A));
	}
	
	@Test
	public void t0025_replace() {
		String xval = "t0025_replace";
		write_through.put(KEY_A, VAL_A);
		boolean modified = cache.replace(KEY_A, "????", xval);
		assertFalse(modified);
		assertEquals(VAL_A, write_through.get(KEY_A));
	}
	
	@Test
	public void t0026_replace() {
		String xkey = "t0026";
		String xval = "t0026_replace";
		write_through.put(xkey, xval);
		boolean modified = cache.replace(xkey, "????", xval);
		assertFalse(modified);
		assertEquals(xval, write_through.get(xkey));
	}
}
