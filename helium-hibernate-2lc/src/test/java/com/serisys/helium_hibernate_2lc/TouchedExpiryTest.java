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
import java.util.Properties;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.TouchedExpiryPolicy;
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
public class TouchedExpiryTest extends CacheExpiryTest {

	@BeforeClass
	public static void setUp() {
		Properties properties = getDefaultProperties();
		CachingProvider provider = Caching.getCachingProvider();
		assertNotNull(provider);
		assertTrue(provider instanceof HeCachingProvider);
		provider.close();
		CacheManager mngr = provider.getCacheManager(provider.getDefaultURI(), provider.getDefaultClassLoader(), properties);
		String cacheName = "TestDatastore";
		assertNotNull(mngr);
		assertTrue(mngr instanceof HeCacheManager);
		Class keyType = String.class;
		Class valueType = String.class;
		MutableConfiguration<String, String> config = new MutableConfiguration<String, String>();
		config.setTypes(keyType, valueType);
		config.setStatisticsEnabled(false);
		config.setExpiryPolicyFactory(TouchedExpiryPolicy.factoryOf(EXPIRY));
		cache = mngr.createCache(cacheName, config);
	}
	
	@AfterClass
	public static void tearDown() {
		CacheTestBase.tearDown();
	}
	
	public TouchedExpiryTest() {
		
	}

	@Test
	@Override
	public void t0001_get_expiry_create() {
		String key = "t0001";
		String value = "t0001_get_expiry_create";
		assertFalse(cache.containsKey(key));
		cache.put(key, value);
		sleep(FIRST_HALF_WAIT);
		assertTrue(cache.containsKey(key));
		// no action after the create
		sleep(SECOND_HALF_WAIT);
		assertNull(cache.get(key)); // this is the method we are testing for expiry
	}

	@Test
	@Override
	public void t0002_get_expiry_update() {
		String key = "t0002";
		String value = "t0002_get_expiry_update";
		String ualue = "t0002_get_expiry_update NEW VALUE"; 
		assertFalse(cache.containsKey(key));
		cache.put(key, value);
		sleep(FIRST_HALF_WAIT);
		assertTrue(cache.containsKey(key));
		cache.put(key, ualue); // the "update" action - update DOES reset the clock
		sleep(SECOND_HALF_WAIT);
		assertTrue(cache.containsKey(key)); 
		sleep(FIRST_HALF_WAIT);
		assertNull(cache.get(key)); // this is the method we are testing for expiry
	}

	@Test
	@Override
	public void t0003_get_expiry_access() {
		String key = "t0003";
		String value = "t0003_get_expiry_access";
		assertFalse(cache.containsKey(key));
		cache.put(key, value);
		sleep(FIRST_HALF_WAIT);
		assertTrue(cache.containsKey(key));
		assertEquals(value, cache.get(key)); // the "access" action - access DOES reset the clock
		sleep(SECOND_HALF_WAIT);
		assertTrue(cache.containsKey(key));
		sleep(FIRST_HALF_WAIT);
		assertNull(cache.get(key)); // this is the method we are testing for expiry
	}

	@Test
	@Override
	public void t0005_iterate_expiry_create() {
		String key = "t0005";
		String value = "t0005_iterate_expiry_create";
		assertFalse(cache.containsKey(key));
		cache.put(key, value);
		sleep(FIRST_HALF_WAIT);
		assertTrue(cache.containsKey(key));
		// no action after the create
		sleep(SECOND_HALF_WAIT);
		
		Iterator<Cache.Entry<String,String>> iterator = cache.iterator();
		assertFalse(iterator.hasNext()); // this is the method we are testing for expiry
	}

	@Test
	@Override
	public void t0006_iterate_expiry_update() {
		String key = "t0006";
		String value = "t0006_iterate_expiry_update";
		String ualue = "t0006_iterate_expiry_update NEW VALUE"; 
		assertFalse(cache.containsKey(key));
		cache.put(key, value);
		sleep(FIRST_HALF_WAIT);
		assertTrue(cache.containsKey(key));
		cache.put(key, ualue); // the "update" action - update DOES reset the clock
		sleep(SECOND_HALF_WAIT);
		assertTrue(cache.containsKey(key)); 
		sleep(FIRST_HALF_WAIT);
		Iterator<Cache.Entry<String,String>> iterator = cache.iterator();
		assertFalse(iterator.hasNext()); // this is the method we are testing for expiry
	}

	@Test
	@Override
	public void t0007_iterate_expiry_access() {
		String key = "t0007";
		String value = "t0007_iterate_expiry_access";
		assertFalse(cache.containsKey(key));
		cache.put(key, value);
		sleep(FIRST_HALF_WAIT);
		assertTrue(cache.containsKey(key));
		assertEquals(value, cache.get(key)); // the "access" action - access DOES reset the clock
		sleep(SECOND_HALF_WAIT);
		assertTrue(cache.containsKey(key));
		sleep(FIRST_HALF_WAIT);
		Iterator<Cache.Entry<String,String>> iterator = cache.iterator();
		assertFalse(iterator.hasNext()); // this is the method we are testing for expiry
	}

	@Test
	@Override
	public void t0009_contains_expiry_create() {
		String key = "t0009";
		String value = "t0009_contains_expiry_create";
		assertFalse(cache.containsKey(key));
		cache.put(key, value);
		sleep(FIRST_HALF_WAIT);
		assertTrue(cache.containsKey(key));
		// no action after the create
		sleep(SECOND_HALF_WAIT);
		assertFalse(cache.containsKey(key)); // this is the method we are testing for expiry
	}

	@Test
	@Override
	public void t0010_contains_expiry_update() {
		String key = "t0010";
		String value = "t0010_contains_expiry_update";
		String ualue = "t0010_contains_expiry_update NEW VALUE"; 
		assertFalse(cache.containsKey(key));
		cache.put(key, value);
		sleep(FIRST_HALF_WAIT);
		assertTrue(cache.containsKey(key));
		cache.put(key, ualue); // the "update" action - update DOES reset the clock
		sleep(SECOND_HALF_WAIT);
		assertTrue(cache.containsKey(key)); 
		sleep(FIRST_HALF_WAIT);
		assertFalse(cache.containsKey(key)); // this is the method we are testing for expiry
	}

	@Test
	@Override
	public void t0011_contains_expiry_access() {
		String key = "t0011";
		String value = "t0011_contains_expiry_access";
		assertFalse(cache.containsKey(key));
		cache.put(key, value);
		sleep(FIRST_HALF_WAIT);
		assertTrue(cache.containsKey(key));
		assertEquals(value, cache.get(key)); // the "access" action - access DOES reset the clock
		sleep(SECOND_HALF_WAIT);
		assertTrue(cache.containsKey(key));
		sleep(FIRST_HALF_WAIT);
		assertFalse(cache.containsKey(key)); // this is the method we are testing for expiry
	}

	@Test
	@Override
	public void t0013_putIfAbsent_expiry_create() {
		String key = "t0013";
		String value = "t0013_putIfAbsent_expiry_create";
		String ualue = "t0013_putIfAbsent_expiry_create NEW VALUE";
		assertFalse(cache.containsKey(key));
		cache.put(key, value);
		sleep(FIRST_HALF_WAIT);
		assertTrue(cache.containsKey(key));
		// no action after the create
		sleep(SECOND_HALF_WAIT);
		assertTrue(cache.putIfAbsent(key, ualue));
		assertEquals(ualue, cache.get(key));
	}

	@Test
	@Override
	public void t0014_putIfAbsent_expiry_update() {
		String key = "t0014";
		String value = "t0014_putIfAbsent_expiry_update";
		String ualue = "t0014_putIfAbsent_expiry_update NEW VALUE";
		String walue = "t0014_putIfAbsent_expiry_update SECOND";
		assertFalse(cache.containsKey(key));
		cache.put(key, value);
		sleep(FIRST_HALF_WAIT);
		assertTrue(cache.containsKey(key));
		cache.put(key, ualue); // the "update" action - update DOES reset the clock
		sleep(SECOND_HALF_WAIT);
		assertFalse(cache.putIfAbsent(key, walue));
		sleep(FIRST_HALF_WAIT);
		assertTrue(cache.putIfAbsent(key, walue));
		assertEquals(walue, cache.get(key));
	}

	@Test
	@Override
	public void t0015_putIfAbsent_expiry_access() {
		String key = "t0015";
		String value = "t0015_putIfAbsent_expiry_access";
		String ualue = "t0015_putIfAbsent_expiry_access NEW VALUE";
		assertFalse(cache.containsKey(key));
		cache.put(key, value);
		sleep(FIRST_HALF_WAIT);
		assertTrue(cache.containsKey(key));
		assertEquals(value, cache.get(key)); // the "access" action - access DOES reset the clock
		sleep(SECOND_HALF_WAIT);
		assertTrue(cache.containsKey(key));
		sleep(FIRST_HALF_WAIT);
		assertTrue(cache.putIfAbsent(key, ualue));
		assertEquals(ualue, cache.get(key));
	}

	@Test
	@Override
	public void t0017_replace_expiry_create() {
		String key = "t0017";
		String value = "t0017_replace_expiry_create";
		String ualue = "t0017_replace_expiry_create NEW VALUE";
		assertFalse(cache.containsKey(key));
		cache.put(key, value);
		sleep(FIRST_HALF_WAIT);
		assertTrue(cache.containsKey(key));
		// no action after the create
		sleep(SECOND_HALF_WAIT);
		assertFalse(cache.replace(key, ualue));
		assertFalse(cache.containsKey(key));
	}

	@Test
	@Override
	public void t0018_replace_expiry_update() {
		String key = "t0018";
		String value = "t0018_replace_expiry_update";
		String ualue = "t0018_replace_expiry_update NEW VALUE";
		String walue = "t0018_replace_expiry_update SECOND";
		String xalue = "t0018_replace_expiry_update THIRD";
		assertFalse(cache.containsKey(key));
		cache.put(key, value);
		sleep(FIRST_HALF_WAIT);
		assertTrue(cache.containsKey(key));
		cache.put(key, ualue); // the "update" action - update DOES reset the clock
		sleep(SECOND_HALF_WAIT);
		assertTrue(cache.replace(key, walue));
		sleep(FULL_WAIT);
		assertFalse(cache.replace(key, xalue));
		assertFalse(cache.containsKey(key));
	}

	@Test
	@Override
	public void t0019_replace_expiry_access() {
		String key = "t0019";
		String value = "t0019_replace_expiry_access";
		String ualue = "t0019_replace_expiry_access NEW VALUE";
		assertFalse(cache.containsKey(key));
		cache.put(key, value);
		sleep(FIRST_HALF_WAIT);
		assertTrue(cache.containsKey(key));
		assertEquals(value, cache.get(key)); // the "access" action - access DOES reset the clock
		sleep(SECOND_HALF_WAIT);
		assertTrue(cache.containsKey(key));
		sleep(FIRST_HALF_WAIT);
		assertFalse(cache.replace(key, ualue));
		assertFalse(cache.containsKey(key));
	}

	@Test
	@Override
	public void t0021_replaceIfSame_expiry_create() {
		String key = "t0021";
		String value = "t0021_replaceIfSame_expiry_create";
		String ualue = "t0021_replaceIfSame_expiry_create NEW VALUE";
		assertFalse(cache.containsKey(key));
		cache.put(key, value);
		sleep(FIRST_HALF_WAIT);
		assertTrue(cache.containsKey(key));
		// no action after the create
		sleep(SECOND_HALF_WAIT);
		assertFalse(cache.replace(key, value, ualue));
		assertFalse(cache.containsKey(key));
	}

	@Test
	@Override
	public void t0022_replaceIfSame_expiry_update() {
		String key = "t0022";
		String value = "t0022_replaceIfSame_expiry_update";
		String ualue = "t0022_replaceIfSame_expiry_update NEW VALUE";
		String walue = "t0022_replaceIfSame_expiry_update SECOND";
		String xalue = "t0022_replaceIfSame_expiry_update THIRD";
		assertFalse(cache.containsKey(key));
		cache.put(key, value);
		sleep(FIRST_HALF_WAIT);
		assertTrue(cache.containsKey(key));
		cache.put(key, ualue); // the "update" action - update DOES reset the clock
		sleep(SECOND_HALF_WAIT);
		assertTrue(cache.replace(key, ualue, walue));
		sleep(FULL_WAIT);
		assertFalse(cache.replace(key, walue, xalue));
		assertFalse(cache.containsKey(key));
	}

	@Test
	@Override
	public void t0023_replaceIfSame_expiry_access() {
		String key = "t0022";
		String value = "t0023_replaceIfSame_expiry_access";
		String ualue = "t0023_replaceIfSame_expiry_access NEW VALUE";
		assertFalse(cache.containsKey(key));
		cache.put(key, value);
		sleep(FIRST_HALF_WAIT);
		assertTrue(cache.containsKey(key));
		assertEquals(value, cache.get(key)); // the "access" action - access DOES reset the clock
		sleep(SECOND_HALF_WAIT);
		assertTrue(cache.containsKey(key));
		sleep(FIRST_HALF_WAIT);
		assertFalse(cache.replace(key, value, ualue));
		assertFalse(cache.containsKey(key));
	}

	@Test
	@Override
	public void t0025_remove_expiry_create() {
		String key = "t0025";
		String value = "t0025_remove_expiry_create";
		assertFalse(cache.containsKey(key));
		cache.put(key, value);
		sleep(FIRST_HALF_WAIT);
		assertTrue(cache.containsKey(key));
		// no action after the create
		sleep(SECOND_HALF_WAIT);
		assertFalse(cache.remove(key));
		assertFalse(cache.containsKey(key));
	}

	@Test
	@Override
	public void t0026_remove_expiry_update() {
		String key = "t0026";
		String value = "t0026_remove_expiry_update";
		String ualue = "t0026_remove_expiry_update NEW VALUE";
		assertFalse(cache.containsKey(key));
		cache.put(key, value);
		sleep(FIRST_HALF_WAIT);
		assertTrue(cache.containsKey(key));
		cache.put(key, ualue); // the "update" action - update DOES reset the clock
		sleep(SECOND_HALF_WAIT);
		assertTrue(cache.containsKey(key));
		sleep(FIRST_HALF_WAIT);
		assertFalse(cache.remove(key));
		assertFalse(cache.containsKey(key));
	}

	@Test
	@Override
	public void t0027_remove_expiry_access() {
		String key = "t0027";
		String value = "t0027_remove_expiry_access";
		assertFalse(cache.containsKey(key));
		cache.put(key, value);
		sleep(FIRST_HALF_WAIT);
		assertTrue(cache.containsKey(key));
		assertEquals(value, cache.get(key)); // the "access" action - access DOES reset the clock
		sleep(SECOND_HALF_WAIT);
		assertTrue(cache.containsKey(key));
		sleep(FIRST_HALF_WAIT);
		assertFalse(cache.remove(key));
		assertFalse(cache.containsKey(key));
	}

	@Test
	@Override
	public void t0029_removeIfSame_expiry_create() {
		String key = "t0029";
		String value = "t0029_removeIfSame_expiry_create";
		assertFalse(cache.containsKey(key));
		cache.put(key, value);
		sleep(FIRST_HALF_WAIT);
		assertTrue(cache.containsKey(key));
		// no action after the create
		sleep(SECOND_HALF_WAIT);
		assertFalse(cache.remove(key, value));
		assertFalse(cache.containsKey(key));
	}

	@Test
	@Override
	public void t0030_removeIfSame_expiry_update() {
		String key = "t0030";
		String value = "t0030_removeIfSame_expiry_update";
		String ualue = "t0030_removeIfSame_expiry_update NEW VALUE";
		assertFalse(cache.containsKey(key));
		cache.put(key, value);
		sleep(FIRST_HALF_WAIT);
		assertTrue(cache.containsKey(key));
		cache.put(key, ualue); // the "update" action - update DOES reset the clock
		sleep(SECOND_HALF_WAIT);
		assertTrue(cache.containsKey(key));
		sleep(FIRST_HALF_WAIT);
		assertFalse(cache.remove(key, value));
		assertFalse(cache.containsKey(key));
	}

	@Test
	@Override
	public void t0031_removeIfSame_expiry_access() {
		String key = "t0031";
		String value = "t0031_removeIfSame_expiry_access";
		assertFalse(cache.containsKey(key));
		cache.put(key, value);
		sleep(FIRST_HALF_WAIT);
		assertTrue(cache.containsKey(key));
		assertEquals(value, cache.get(key)); // the "access" action - access DOES reset the clock
		sleep(SECOND_HALF_WAIT);
		assertTrue(cache.containsKey(key));
		sleep(FIRST_HALF_WAIT);
		assertFalse(cache.remove(key, value));
		assertFalse(cache.containsKey(key));
	}

}
