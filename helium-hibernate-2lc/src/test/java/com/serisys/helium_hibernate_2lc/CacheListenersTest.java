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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.cache.Cache;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.Factory;
import javax.cache.event.CacheEntryCreatedListener;
import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryEventFilter;
import javax.cache.event.CacheEntryExpiredListener;
import javax.cache.event.CacheEntryListener;
import javax.cache.event.CacheEntryListenerException;
import javax.cache.event.CacheEntryRemovedListener;
import javax.cache.event.CacheEntryUpdatedListener;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;

import com.serisys.helium.jcache.CacheEntryImpl;

@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CacheListenersTest extends CacheContentTest {
	private static final BlockingQueue<Cache.Entry<String,String>> created = new LinkedBlockingQueue<Cache.Entry<String,String>>();

	private static final BlockingQueue<Cache.Entry<String,String>> expired = new LinkedBlockingQueue<Cache.Entry<String,String>>();

	private static final BlockingQueue<Cache.Entry<String,String>> removed = new LinkedBlockingQueue<Cache.Entry<String,String>>();

	private static final BlockingQueue<Cache.Entry<String,String>> updated = new LinkedBlockingQueue<Cache.Entry<String,String>>();

	private static final int POLL_DURATION_EXPECTING = 1;
	private static final TimeUnit TIME_UNIT_EXPECTING = TimeUnit.SECONDS;
	private static final int POLL_DURATION_NOT_EXPECTING = 100;
	private static final TimeUnit TIME_UNIT_NOT_EXPECTING = TimeUnit.MILLISECONDS;

	@BeforeClass
	public static void setUp() {
		CacheContentTest.setUp();
		
		final Factory<CacheEntryEventFilter<? super String, ? super String>> pass_all = new Factory<CacheEntryEventFilter<? super String, ? super String>>() {
			@Override
			public CacheEntryEventFilter<? super String, ? super String> create() {
				return new CacheEntryEventFilter<String, String>() {

					@Override
					public boolean evaluate(CacheEntryEvent<? extends String, ? extends String> event)
							throws CacheEntryListenerException {
						return true;
					}
					
				};
			}
		};
		
		// now register listeners
		// created listener
		CacheEntryListenerConfiguration<String,String> created_config = new CacheEntryListenerConfiguration<String,String>(){
			@Override
			public Factory<CacheEntryEventFilter<? super String, ? super String>> getCacheEntryEventFilterFactory() {
				return pass_all;
			}

			@Override
			public Factory<CacheEntryListener<? super String, ? super String>> getCacheEntryListenerFactory() {
				return new Factory<CacheEntryListener<? super String, ? super String>>() {
					@Override
					public CacheEntryListener<? super String, ? super String> create() {
						return new CacheEntryCreatedListener<String, String>() {
							@Override
							public void onCreated(Iterable<CacheEntryEvent<? extends String, ? extends String>> events)
									throws CacheEntryListenerException {
								for (CacheEntryEvent<? extends String, ? extends String> event : events) {
									Cache.Entry<String,String> entry = new CacheEntryImpl(event.getKey(), event.getValue());
									created.add(entry);
								}
							}
							
						};
					}
				};
			}

			@Override
			public boolean isOldValueRequired() {
				return false;
			}

			@Override
			public boolean isSynchronous() {
				return true;
			}
		};
		cache.registerCacheEntryListener(created_config);
		
		// expired listener
		CacheEntryListenerConfiguration<String,String> expired_config = new CacheEntryListenerConfiguration<String,String>(){
			@Override
			public Factory<CacheEntryEventFilter<? super String, ? super String>> getCacheEntryEventFilterFactory() {
				return pass_all;
			}

			@Override
			public Factory<CacheEntryListener<? super String, ? super String>> getCacheEntryListenerFactory() {
				return new Factory<CacheEntryListener<? super String, ? super String>>() {
					@Override
					public CacheEntryListener<? super String, ? super String> create() {
						return new CacheEntryExpiredListener<String, String>() {
							@Override
							public void onExpired(Iterable<CacheEntryEvent<? extends String, ? extends String>> events)
									throws CacheEntryListenerException {
								for (CacheEntryEvent<? extends String, ? extends String> event : events) {
									Cache.Entry<String,String> entry = new CacheEntryImpl(event.getKey(), event.getValue());
									expired.add(entry);
								}
							}
							
						};
					}
				};
			}

			@Override
			public boolean isOldValueRequired() {
				return false;
			}

			@Override
			public boolean isSynchronous() {
				return true;
			}
		};
		cache.registerCacheEntryListener(expired_config);

		// removed listener
		CacheEntryListenerConfiguration<String,String> removed_config = new CacheEntryListenerConfiguration<String,String>(){
			@Override
			public Factory<CacheEntryEventFilter<? super String, ? super String>> getCacheEntryEventFilterFactory() {
				return pass_all;
			}

			@Override
			public Factory<CacheEntryListener<? super String, ? super String>> getCacheEntryListenerFactory() {
				return new Factory<CacheEntryListener<? super String, ? super String>>() {
					@Override
					public CacheEntryListener<? super String, ? super String> create() {
						return new CacheEntryRemovedListener<String, String>() {
							@Override
							public void onRemoved(Iterable<CacheEntryEvent<? extends String, ? extends String>> events)
									throws CacheEntryListenerException {
								for (CacheEntryEvent<? extends String, ? extends String> event : events) {
									Cache.Entry<String,String> entry = new CacheEntryImpl(event.getKey(), event.getValue());
									removed.add(entry);
								}
							}
							
						};
					}
				};
			}

			@Override
			public boolean isOldValueRequired() {
				return false;
			}

			@Override
			public boolean isSynchronous() {
				return true;
			}
		};
		cache.registerCacheEntryListener(removed_config);

		// updated listener
		CacheEntryListenerConfiguration<String,String> updated_config = new CacheEntryListenerConfiguration<String,String>(){
			@Override
			public Factory<CacheEntryEventFilter<? super String, ? super String>> getCacheEntryEventFilterFactory() {
				return pass_all;
			}

			@Override
			public Factory<CacheEntryListener<? super String, ? super String>> getCacheEntryListenerFactory() {
				return new Factory<CacheEntryListener<? super String, ? super String>>() {
					@Override
					public CacheEntryListener<? super String, ? super String> create() {
						return new CacheEntryUpdatedListener<String, String>() {
							@Override
							public void onUpdated(Iterable<CacheEntryEvent<? extends String, ? extends String>> events)
									throws CacheEntryListenerException {
								for (CacheEntryEvent<? extends String, ? extends String> event : events) {
									Cache.Entry<String,String> entry = new CacheEntryImpl(event.getKey(), event.getValue());
									updated.add(entry);
								}
							}
							
						};
					}
				};
			}

			@Override
			public boolean isOldValueRequired() {
				return true;
			}

			@Override
			public boolean isSynchronous() {
				return true;
			}
		};
		cache.registerCacheEntryListener(updated_config);
	}

	@AfterClass
	public static void tearDown() {
		CacheContentTest.tearDown();
	}

	public CacheListenersTest() {
	}
	
	private void checkNoEvents() {
		try {
			Cache.Entry<String, String> entry = created.poll(POLL_DURATION_NOT_EXPECTING, TIME_UNIT_NOT_EXPECTING);
			assertNullEntry(entry);
			entry = expired.poll(POLL_DURATION_NOT_EXPECTING, TIME_UNIT_NOT_EXPECTING);
			assertNullEntry(entry);
			entry = removed.poll(POLL_DURATION_NOT_EXPECTING, TIME_UNIT_NOT_EXPECTING);
			assertNullEntry(entry);
			entry = updated.poll(POLL_DURATION_NOT_EXPECTING, TIME_UNIT_NOT_EXPECTING);
			assertNullEntry(entry);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void assertNullEntry(Cache.Entry<String, String> entry) {
		if (entry != null) {
			StringBuilder bob = new StringBuilder()
				.append("Found unexpected entry! Entry was: ")
				.append(entry.getKey())
				.append(" -> ")
				.append(entry.getValue());
			assertNull(bob.toString(), entry);
		}
	}

	private void checkEvents(int n, BlockingQueue<Cache.Entry<String,String>> queue) {
		try {
			Cache.Entry<String, String> entry = null;
			for (int i = 0; i < n; i++) {
				entry = queue.poll(POLL_DURATION_EXPECTING, TIME_UNIT_EXPECTING);
				assertNotNull(entry);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Before
	public void clear() { 
		Collection<Cache.Entry<String,String>> cloaca = new java.util.ArrayList<Cache.Entry<String,String>>();
		created.drainTo(cloaca);
		expired.drainTo(cloaca);
		removed.drainTo(cloaca);
		updated.drainTo(cloaca);
		cloaca.clear();
	}

	@Test
	@Override
	public void t0000_defaultCache_containsKey() {
		// 1 event expected
		super.t0000_defaultCache_containsKey();
		checkEvents(1, created);
	}

	@Test
	@Override
	public void t0001_defaultCache_put_get() {
		super.t0001_defaultCache_put_get();
		try {
			Cache.Entry<String, String> entry = created.poll(POLL_DURATION_EXPECTING, TIME_UNIT_EXPECTING);
			assertNotNull(entry);
			assertEquals(t0001_key, entry.getKey());
			assertEquals(t0001_value, entry.getValue());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	} 
	@Test
	@Override
	public void t0002_defaultCache_put_get_overwrite() {
		super.t0002_defaultCache_put_get_overwrite();
		try {
			Cache.Entry<String, String> entry = created.poll(POLL_DURATION_EXPECTING, TIME_UNIT_EXPECTING);
			assertNotNull(entry);
			assertEquals(t0002_key, entry.getKey());
			assertEquals(t0002_value, entry.getValue());
			entry = updated.poll(POLL_DURATION_EXPECTING, TIME_UNIT_EXPECTING);
			assertNotNull(entry);
			assertEquals(t0002_key, entry.getKey());
			assertEquals(t0002_value2, entry.getValue());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	} 
	
	@Test
	@Override
	public void t0003_defaultCache_getAll() {
		// 2 events expected
		super.t0003_defaultCache_getAll();
		checkEvents(2, created);
	}
	
	@Test
	@Override
	public void t0004_defaultCache_getAndPut() {
		super.t0004_defaultCache_getAndPut();
		try {
			Cache.Entry<String, String> entry = created.poll(POLL_DURATION_EXPECTING, TIME_UNIT_EXPECTING);
			assertNotNull(entry);
			assertEquals(t0004_key, entry.getKey());
			assertEquals(t0004_value, entry.getValue());
			entry = updated.poll(POLL_DURATION_EXPECTING, TIME_UNIT_EXPECTING);
			assertNotNull(entry);
			assertEquals(t0004_key, entry.getKey());
			assertEquals(t0004_value2, entry.getValue());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	} 

	@Test
	@Override
	public void t0004_defaultCache_getAndPut_2() {
		super.t0004_defaultCache_getAndPut_2();
		try {
			Cache.Entry<String, String> entry = created.poll(POLL_DURATION_EXPECTING, TIME_UNIT_EXPECTING);
			assertNotNull(entry);
			assertEquals(t0004_2_key, entry.getKey());
			assertEquals(t0004_2_value, entry.getValue());
			entry = updated.poll(POLL_DURATION_EXPECTING, TIME_UNIT_EXPECTING);
			assertNotNull(entry);
			assertEquals(t0004_2_key, entry.getKey());
			assertEquals(t0004_2_value2, entry.getValue());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	@Override
	public void t0005_defaultCache_getAndRemove() {
		super.t0005_defaultCache_getAndRemove();
		try {
			Cache.Entry<String, String> entry = created.poll(POLL_DURATION_EXPECTING, TIME_UNIT_EXPECTING);
			assertNotNull(entry);
			assertEquals(t0005_key, entry.getKey());
			assertEquals(t0005_value, entry.getValue());
			entry = removed.poll(POLL_DURATION_EXPECTING, TIME_UNIT_EXPECTING);
			assertNotNull(entry);
			assertEquals(t0005_key, entry.getKey());
			assertEquals(t0005_value, entry.getValue());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	@Override
	public void t0006_defaultCache_getAndReplace() {
		super.t0006_defaultCache_getAndReplace();
		try {
			Cache.Entry<String, String> entry = created.poll(POLL_DURATION_EXPECTING, TIME_UNIT_EXPECTING);
			assertNotNull(entry);
			assertEquals(t0006_key, entry.getKey());
			assertEquals(t0006_value, entry.getValue());
			entry = updated.poll(POLL_DURATION_EXPECTING, TIME_UNIT_EXPECTING);
			assertNotNull(entry);
			assertEquals(t0006_key, entry.getKey());
			assertEquals(t0006_value2, entry.getValue());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	@Override
	public void t0007_defaultCache_invoke() {
		// 3 events expected
		super.t0007_defaultCache_invoke();
		checkEvents(3, created);
	}
	
	@Test
	@Override
	public void t0007_defaultCache_invoke_noSuchKey() {
		// No events expected
		super.t0007_defaultCache_invoke_noSuchKey();
		checkNoEvents();
	}
	
	@Test
	@Override
	public void t0008_defaultCache_invokeAll() {
		// 2 events expected
		super.t0008_defaultCache_invokeAll();
		checkEvents(2, created);
	}

	@Test
	@Override
	public void t0009_defaultCache_clear() {
		// 1 event expected
		super.t0009_defaultCache_clear();
		checkEvents(1, created);
	}

	@Test
	@Override
	public void t0010_defaultCache_iterator() {
		// 10 events expected
		super.t0010_defaultCache_iterator();
		checkEvents(10, created);
	}
	@Test
	@Override
	public void t0010_defaultCache_iterator_remove() {
		// 10 events expected
		super.t0010_defaultCache_iterator_remove();
		checkEvents(10, created);
		checkEvents(10, removed);
		checkNoEvents();
	}
	@Test
	@Override
	public void t0011_defaultCache_putAll() {
		super.t0011_defaultCache_putAll();
		Map<String,String> check = t0011_keysValues();
		for (int i = 0; i < check.size(); i++) {
			try {
				Cache.Entry<String, String> entry = created.poll(POLL_DURATION_EXPECTING, TIME_UNIT_EXPECTING);
				assertNotNull(entry);
				assertEquals(check.get(entry.getKey()), entry.getValue());
				check.remove(entry.getKey());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
	}

	@Test
	@Override
	public void t0012_defaultCache_putIfAbsent() {
		super.t0012_defaultCache_putIfAbsent();
		try {
			Cache.Entry<String, String> entry = created.poll(POLL_DURATION_EXPECTING, TIME_UNIT_EXPECTING);
			assertNotNull(entry);
			assertEquals(t0012_key, entry.getKey());
			assertEquals(t0012_value, entry.getValue());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	@Override
	public void t0013_defaultCache_remove() {
		super.t0013_defaultCache_remove();
		try {
			Cache.Entry<String, String> entry = created.poll(POLL_DURATION_EXPECTING, TIME_UNIT_EXPECTING);
			assertNotNull(entry);
			assertEquals(t0013_key, entry.getKey());
			assertEquals(t0013_value, entry.getValue());
			entry = removed.poll(POLL_DURATION_EXPECTING, TIME_UNIT_EXPECTING);
			assertNotNull(entry);
			assertEquals(t0013_key, entry.getKey());
			assertEquals(t0013_value, entry.getValue());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	@Override
	public void t0014_defaultCache_remove() {
		super.t0014_defaultCache_remove();
		try {
			Cache.Entry<String, String> entry = created.poll(POLL_DURATION_EXPECTING, TIME_UNIT_EXPECTING);
			assertNotNull(entry);
			assertEquals(t0014_key, entry.getKey());
			assertEquals(t0014_value, entry.getValue());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	@Override
	public void t0015_defaultCache_removeAll() {
		cache.clear();
		// As many events as there are entries
		int count = 0;
		Iterator<Cache.Entry<String,String>> contents = cache.iterator();
		while (contents.hasNext()) {
			contents.next();
			count++;
		}
		super.t0015_defaultCache_removeAll();
		try {
			Cache.Entry<String, String> entry = created.poll(POLL_DURATION_EXPECTING, TIME_UNIT_EXPECTING);
			assertNotNull(entry);
			assertEquals(t0015_key, entry.getKey());
			assertEquals(t0015_value, entry.getValue());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// count one extra as the test adds one before removing all ...
		checkEvents(count + 1, removed);
	}

	@Test
	@Override
	public void t0016_defaultCache_removeAll() {
		super.t0016_defaultCache_removeAll();
		try {
			Cache.Entry<String, String> entry = created.poll(POLL_DURATION_EXPECTING, TIME_UNIT_EXPECTING);
			assertNotNull(entry);
			assertEquals(t0016_key, entry.getKey());
			assertEquals(t0016_value, entry.getValue());
			entry = created.poll(POLL_DURATION_EXPECTING, TIME_UNIT_EXPECTING);
			assertNotNull(entry);
			assertEquals(t0016_key2, entry.getKey());
			assertEquals(t0016_value2, entry.getValue());
			entry = removed.poll(POLL_DURATION_EXPECTING, TIME_UNIT_EXPECTING);
			assertNotNull(entry);
			assertEquals(t0016_key, entry.getKey());
			assertEquals(t0016_value, entry.getValue());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	@Override
	public void t0017_defaultCache_replace() {
		super.t0017_defaultCache_replace();
		try {
			Cache.Entry<String, String> entry = created.poll(POLL_DURATION_EXPECTING, TIME_UNIT_EXPECTING);
			assertNotNull(entry);
			assertEquals(t0017_key, entry.getKey());
			assertEquals(t0017_value, entry.getValue());
			entry = updated.poll(POLL_DURATION_EXPECTING, TIME_UNIT_EXPECTING);
			assertNotNull(entry);
			assertEquals(t0017_key, entry.getKey());
			assertEquals(t0017_new_value, entry.getValue());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	@Override
	public void t0018_defaultCache_replace() {
		// No events expected
		super.t0018_defaultCache_replace();
		checkNoEvents();
	}

	@Test
	@Override
	public void t0019_defaultCache_replace() {
		super.t0019_defaultCache_replace();
		try {
			Cache.Entry<String, String> entry = created.poll(POLL_DURATION_EXPECTING, TIME_UNIT_EXPECTING);
			assertNotNull(entry);
			assertEquals(t0019_key, entry.getKey());
			assertEquals(t0019_value, entry.getValue());
			entry = updated.poll(POLL_DURATION_EXPECTING, TIME_UNIT_EXPECTING);
			assertNotNull(entry);
			assertEquals(t0019_key, entry.getKey());
			assertEquals(t0019_new_value, entry.getValue());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	@Override
	public void t0020_defaultCache_replace() {
		super.t0020_defaultCache_replace();
		try {
			Cache.Entry<String, String> entry = created.poll(POLL_DURATION_EXPECTING, TIME_UNIT_EXPECTING);
			assertNotNull(entry);
			assertEquals(t0020_key, entry.getKey());
			assertEquals(t0020_value, entry.getValue());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		checkNoEvents();
	}

	@Test
	@Override
	public void t0021_defaultCache_replace() {
		// No events expected
		super.t0021_defaultCache_replace();
		checkNoEvents();
	}

	@Test
	@Override
	public void t0022_defaultCache_removeKV() {
		super.t0022_defaultCache_removeKV();
		try {
			Cache.Entry<String, String> entry = created.poll(POLL_DURATION_EXPECTING, TIME_UNIT_EXPECTING);
			assertNotNull(entry);
			assertEquals(t0022_key, entry.getKey());
			assertEquals(t0022_value, entry.getValue());
			entry = removed.poll(POLL_DURATION_EXPECTING, TIME_UNIT_EXPECTING);
			assertNotNull(entry);
			assertEquals(t0022_key, entry.getKey());
			assertEquals(t0022_value, entry.getValue());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		checkNoEvents();
	}

	@Test
	@Override
	public void t0023_defaultCache_removeKV() {
		// 1 events expected
		super.t0023_defaultCache_removeKV();
		checkEvents(1, created);
	}

	@Test
	@Override
	public void t0024_defaultCache_removeKV() {
		// 1 events expected
		super.t0024_defaultCache_removeKV();
		checkEvents(1, created);
	}
	
	@Test
	@Override
	public void t0025_iterate_variableSizes() {
		super.t0025_iterate_variableSizes();
	}

	@Test
	@Override
	public void t0026_get_large_overflowing() {
		super.t0026_get_large_overflowing();
	}
	
	@Test
	@Override
	public void t0027_defaultCache_invoke_setting() {
		super.t0027_defaultCache_invoke_setting();
		checkEvents(1, created);
		checkEvents(1, updated);
	}
	
	@Test
	@Override
	public void t0028_defaultCache_invoke_setting() {
		super.t0028_defaultCache_invoke_setting();
		checkEvents(1, created);
		checkEvents(1, updated);
	}
	
	@Test
	@Override
	public void t0029_defaultCache_invoke_removing() {
		super.t0029_defaultCache_invoke_removing();
		checkEvents(1, created);
		checkEvents(1, removed);
	}
	
	@Test
	@Override
	public void t0030_defaultCache_invoke_exists() {
		super.t0030_defaultCache_invoke_exists();
		checkEvents(1, created);
	}
	
	@Test
	@Override
	public void t0031_defaultCache_invoke_exists() {
		super.t0031_defaultCache_invoke_exists();
		checkNoEvents();
	}
	
	@Test
	@Override
	public void t0032_defaultCache_invokeAll_setting() {
		super.t0032_defaultCache_invokeAll_setting();
		checkEvents(1, created);
		checkEvents(1, updated);
	}
	
	@Test
	@Override
	public void t0033_defaultCache_invokeAll_setting() {
		super.t0033_defaultCache_invokeAll_setting();
		checkEvents(1, created);
		checkEvents(1, updated);
	}
	
	@Test
	@Override
	public void t0034_defaultCache_invokeAll_removing() {
		super.t0034_defaultCache_invokeAll_removing();
		checkEvents(1, created);
		checkEvents(1, removed);
	}
	
	@Test
	@Override
	public void t0035_defaultCache_invokeAll_exists() {
		super.t0035_defaultCache_invokeAll_exists();
		checkEvents(1, created);
	}
	
	@Test
	@Override
	public void t0036_defaultCache_invokeAll_exists() {
		super.t0036_defaultCache_invokeAll_exists();
		checkNoEvents();
	}

	@Test
	@Override
	public void t0037_defaultCache_invoke_getting() {
		super.t0037_defaultCache_invoke_getting();
		checkEvents(1, created);
	}
	
	@Test
	@Override
	public void t0038_defaultCache_invokeAll_getting() {
		super.t0038_defaultCache_invokeAll_getting();
		checkEvents(1, created);
	}
	
	@Test
	@Override
	public void t0039_defaultCache_invokeAll_getting() {
		super.t0039_defaultCache_invokeAll_getting();
		checkEvents(1, created);
	}
	
	@Test
	@Override
	public void t0040_defaultCache_invokeAll_setting() {
		super.t0040_defaultCache_invokeAll_setting();
		checkEvents(2, created);
	}
	
	@Test
	@Override
	public void t0041_defaultCache_invokeAll_removing() {
		super.t0041_defaultCache_invokeAll_removing();
		checkEvents(1, created);
		checkEvents(1, removed);
	}
	
	@Test
	@Override
	public void t0042_defaultCache_invokeAll_exists() {
		super.t0042_defaultCache_invokeAll_exists();
		checkEvents(1, created);
	}
	
	@Test
	@Override
	public void t0043_defaultCache_invokeAll_setting_throwing() {
		super.t0043_defaultCache_invokeAll_setting_throwing();
		checkEvents(11, created);
	}
	
}
