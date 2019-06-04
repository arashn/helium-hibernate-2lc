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

import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.Factory;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheLoaderException;
import javax.cache.integration.CompletionListener;
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
public class CacheReadThroughTest {
	protected static Cache<String, String> cache;
	protected static final Map<String,String> read_through = new java.util.HashMap<String,String>();

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
		config.setCacheLoaderFactory((Factory<? extends CacheLoader<String, String>>) new Factory<CacheLoader<String,String>>() {
			@Override
			public CacheLoader<String, String> create() {
				return new CacheLoader<String, String>() {
					@Override
					public String load(String key) throws CacheLoaderException {
						return read_through.get(key);
					}

					@Override
					public Map<String, String> loadAll(Iterable<? extends String> keys) throws CacheLoaderException {
						Map<String,String> load = new java.util.HashMap<String,String>();
						for (String key : keys) {
							String val = read_through.get(key);
							if (val != null) {
								load.put(key, val);
							}
						}
						return load;
					}
				};
			}
		});
		config.setReadThrough(true);
		config.setTypes(String.class, String.class);
		cache = mngr.createCache("TestDatastore", config);
		
		cache.put(KEY_A, VAL_A);
		cache.put(KEY_B, VAL_B);
		cache.put(KEY_C, VAL_C);
	}
	
	@AfterClass
	public static void tearDown() {
		cache.close();
	}
	
	public CacheReadThroughTest() {
	}

	@Test
	public void t0001_get_in_cache() {
		String value = cache.get(KEY_A);
		assertEquals(VAL_A, value);
	}
	
	@Test
	public void t0002_get_read_through() {
		String xkey = "t0002";
		String xval = "t0002_get_read_through";
		read_through.put(xkey, xval);
		String value = cache.get(xkey);
		assertEquals(xval, value);
		// cache should now contain xkey,xcal. So will read-through.
		// Remove xkey, xval from read-through
		read_through.remove(xkey);
		assertFalse(read_through.containsKey(xkey));
		assertEquals(xval, cache.get(xkey));
	}
	
	@Test
	public void t0003_get_read_through_no_find() {
		String xkey = "t0003";
		String value = cache.get(xkey);
		assertNull(value);
	}

	@Test
	public void t0004_getAll_in_cache() {
		Set<String> keys = new java.util.HashSet<String>(7);
		keys.add(KEY_A);
		keys.add(KEY_B);
		Map<String,String> values = cache.getAll(keys);
		assertEquals(VAL_A, values.get(KEY_A));
		assertEquals(VAL_B, values.get(KEY_B));
	}

	@Test
	public void t0005_getAll_read_through() {
		String xkey = "t0005";
		String xval = "t0005_getAll_read_through";
		read_through.put(xkey, xval);
		Set<String> keys = new java.util.HashSet<String>(7);
		keys.add(KEY_A);
		keys.add(xkey);
		Map<String,String> values = cache.getAll(keys);
		assertEquals(VAL_A, values.get(KEY_A));
		assertEquals(xval, values.get(xkey));
		// cache should now contain xkey,xcal. So will read-through.
		// Remove xkey, xval from read-through
		read_through.remove(xkey);
		assertFalse(read_through.containsKey(xkey));
		assertEquals(xval, cache.get(xkey));
	}

	@Test
	public void t0006_getAll_read_through_no_find() {
		String xkey = "t0006";
		Set<String> keys = new java.util.HashSet<String>(7);
		keys.add(KEY_A);
		keys.add(xkey);
		Map<String,String> values = cache.getAll(keys);
		assertEquals(VAL_A, values.get(KEY_A));
		assertNull(values.get(xkey));
	}

	@Test
	public void t0007_invoke_in_cache() {
		EntryProcessor<String,String,String> processor = new EntryProcessor<String, String, String>() {
			@Override
			public String process(MutableEntry<String, String> entry, Object... arguments)
					throws EntryProcessorException {
				return entry.getKey() + " -> " + entry.getValue();
			}
		};
		
		String result = cache.invoke(KEY_A, processor, new Object[] {});
		assertEquals(KEY_A + " -> " + VAL_A, result);
	}
	
	@Test
	public void t0008_invoke_read_through() {
		String xkey = "t0008";
		String xval = "t0008_invoke_read_through";
		read_through.put(xkey, xval);
		EntryProcessor<String,String,String> processor = new EntryProcessor<String, String, String>() {
			@Override
			public String process(MutableEntry<String, String> entry, Object... arguments)
					throws EntryProcessorException {
				return entry.getKey() + " -> " + entry.getValue();
			}
		};
		
		String result = cache.invoke(xkey, processor, new Object[] {});
		assertEquals(xkey + " -> " + xval, result);
		// cache should now contain xkey,xcal. So will read-through.
		// Remove xkey, xval from read-through
		read_through.remove(xkey);
		assertFalse(read_through.containsKey(xkey));
		assertEquals(xval, cache.get(xkey));
	}
	
	@Test
	public void t0009_invoke_read_through_no_find() {
		String xkey = "t0009";
		String xval = "t0009_invoke_read_through_no_find";
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
		
		String result = cache.invoke(xkey, processor, new Object[] {});
		String expected = new StringBuilder() 
				.append(xkey)
				.append(" -> ")
				.append("null")
				.toString();
		assertEquals(expected, result);
	}
	
	@Test
	public void t0010_loadAll_in_cache() {
		Set<String> keys = new java.util.HashSet<String>();
		keys.add(KEY_A);
		keys.add(KEY_B);
		keys.add(KEY_C);
		CompletionListener completionListener = new CompletionListener() {
			@Override
			public void onException(Exception e) {}
			@Override
			public void onCompletion() {}
		};
		cache.loadAll(keys, false, completionListener);
	}
	
	@Test
	public void t0011_loadAll_read_through() {
		String xkey = "t0011";
		String xval = "t0011_loadAll_read_through";
		read_through.put(xkey, xval);
		Set<String> keys = new java.util.HashSet<String>();
		keys.add(KEY_A);
		keys.add(xkey);
		keys.add(KEY_C);
		CompletionListener completionListener = new CompletionListener() {
			@Override
			public void onException(Exception e) {}
			@Override
			public void onCompletion() {}
		};
		cache.loadAll(keys, false, completionListener);
		// cache should now contain xkey,xcal. So will read-through.
		// Remove xkey, xval from read-through
		read_through.remove(xkey);
		assertFalse(read_through.containsKey(xkey));
		assertEquals(xval, cache.get(xkey));
	}
	
	@Test
	public void t0012_loadAll_read_through_no_find() {
		String xkey = "t0012";
		Set<String> keys = new java.util.HashSet<String>();
		keys.add(KEY_A);
		keys.add(xkey);
		keys.add(KEY_C);
		CompletionListener completionListener = new CompletionListener() {
			@Override
			public void onException(Exception e) {}
			@Override
			public void onCompletion() {}
		};
		cache.loadAll(keys, false, completionListener);
		assertNull(cache.get(xkey));
	}

	@Test
	public void t0013_loadAll_read_through_replace() {
		try {
			String xkey = KEY_B;
			String xval = "t0013_loadAll_read_through_replace";
			read_through.put(xkey, xval);
			Set<String> keys = new java.util.HashSet<String>();
			keys.add(KEY_A);
			keys.add(KEY_B);
			keys.add(KEY_C);
			CompletionListener completionListener = new CompletionListener() {
				@Override
				public void onException(Exception e) {}
				@Override
				public void onCompletion() {}
			};
			cache.loadAll(keys, true, completionListener);
			// cache should now contain xkey,xcal. So will read-through.
			// Remove xkey, xval from read-through
			read_through.remove(xkey);
			assertFalse(read_through.containsKey(xkey));
			assertEquals(xval, cache.get(xkey));
		} 
		finally {
			// set the cache back the way it should be 
			cache.put(KEY_B, VAL_B);
		}
	}

	@Test
	public void t0014_loadAll_read_through_no_replace() {
		String xkey = KEY_B;
		String xval = "t0014_loadAll_read_through_no_replace";
		read_through.put(xkey, xval);
		Set<String> keys = new java.util.HashSet<String>();
		keys.add(KEY_A);
		keys.add(KEY_B);
		keys.add(KEY_C);
		CompletionListener completionListener = new CompletionListener() {
			@Override
			public void onException(Exception e) {}
			@Override
			public void onCompletion() {}
		};
		cache.loadAll(keys, false, completionListener);
		// cache should now contain xkey,xcal. So will read-through.
		// Remove xkey, xval from read-through
		read_through.remove(xkey);
		assertFalse(read_through.containsKey(xkey));
		assertEquals(VAL_B, cache.get(xkey));
	}
	
	@Test
	public void t0015_invokeAll_in_cache() {
		EntryProcessor<String,String,String> processor = new EntryProcessor<String, String, String>() {
			@Override
			public String process(MutableEntry<String, String> entry, Object... arguments)
					throws EntryProcessorException {
				return entry.getKey() + " -> " + entry.getValue();
			}
		};
		Set<String> keys = new java.util.HashSet<String>(7);
		keys.add(KEY_A);
		Map<String,EntryProcessorResult<String>> result = cache.invokeAll(keys, processor, new Object[] {});
		assertEquals(KEY_A + " -> " + VAL_A, result.get(KEY_A).get());
	}
	
	@Test
	public void t0016_invokeAll_read_through() {
		String xkey = "t0016";
		String xval = "t0016_invokeAll_read_through";
		read_through.put(xkey, xval);
		EntryProcessor<String,String,String> processor = new EntryProcessor<String, String, String>() {
			@Override
			public String process(MutableEntry<String, String> entry, Object... arguments)
					throws EntryProcessorException {
				return entry.getKey() + " -> " + entry.getValue();
			}
		};
		Set<String> keys = new java.util.HashSet<String>(7);
		keys.add(KEY_A);
		keys.add(xkey);
		Map<String,EntryProcessorResult<String>> result = cache.invokeAll(keys, processor, new Object[] {});
		assertEquals(KEY_A + " -> " + VAL_A, result.get(KEY_A).get());
		assertEquals(xkey + " -> " + xval, result.get(xkey).get());
	}
	
	@Test
	public void t0017_invokeAll_read_through_no_find() {
		String xkey = "t0017";
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
		Set<String> keys = new java.util.HashSet<String>(7);
		keys.add(KEY_A);
		keys.add(xkey);
		Map<String,EntryProcessorResult<String>> result = cache.invokeAll(keys, processor, new Object[] {});
		assertEquals(KEY_A + " -> " + VAL_A, result.get(KEY_A).get());
		String expected = new StringBuilder()
				.append(xkey)
				.append(" -> ")
				.append("null")
				.toString();
		assertEquals(expected, result.get(xkey).get());
	}
}
