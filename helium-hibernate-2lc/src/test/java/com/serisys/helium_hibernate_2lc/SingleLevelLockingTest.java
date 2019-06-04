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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ConcurrentModificationException;
import java.util.Properties;
import java.util.concurrent.Semaphore;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.Factory;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.spi.CachingProvider;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;

import com.serisys.helium.jcache.HeCache;
import com.serisys.helium.jcache.HeCacheManager;
import com.serisys.helium.jcache.HeCacheProperties;
import com.serisys.helium.jcache.HeCachingProvider;
import com.serisys.helium.jcache.synch.SingleLevelLockHandler;
import com.serisys.helium.jcache.synch.SynchHandling;
import com.serisys.helium.jcache.synch.ThreadsafeExecution;

@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SingleLevelLockingTest extends MTCacheContentTest {
	@BeforeClass
	public static void setUp() {
		Properties properties = CacheTestBase.getDefaultProperties();
		properties.setProperty(HeCacheProperties.SYNCH_HANDLER_CLASS_NAME, SingleLevelLockHandler.class.getName());
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
		return FactoryBuilder.factoryOf(SingleLevelLockHandler.class);
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
		// output is most likely to be ABCD, showing no interleaving possible
		// a less likely but possible outcome is CDAB
		assertTrue("ABCD".equals(to_check) || "CDAB".equals(to_check));
	}

	@Test
	@Override
	public void s0003_checkSafety() {
		super.s0003_checkSafety();
	}
	
	@Override
	protected void s0003_testCondition(ConcurrentModificationException e) {
		assertNull(e);
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
		assertEquals(SynchHandling.SINGLE_LEVEL, heCache.getSynchHandlingLevel());
	}

	@Test
	public void t0001_get_blocks_get() {
		final Semaphore control = new Semaphore(0);
		final Semaphore stepper = new Semaphore(0, true);
		final String key_contents = "t0001";
		final SteppingCacheKey key = new SteppingCacheKey(key_contents);
		final SteppingCacheKey stepping_key = new SteppingCacheKey(key_contents, stepper);
		final String value = "t0001_put_get";
		
		cache.put(key, value);
		assertEquals(value, cache.get(key));
		
		final String[] t1_returns = new String[1]; 
		final String[] t2_returns = new String[1]; 
		
		Thread t1 = new Thread(new Runnable() {
			public void run() {
				t1_returns[0] = cache.get(stepping_key);
			}
		});
		
		Thread t2 = new Thread(new Runnable() {
			public void run() {
				t2_returns[0] = cache.get(key);
			}
		});
		
		t1.start();
		// give t1 chance to get going ...
		sleep(50);
		t2.start();
		// ditto t2
		sleep(50);
		assertNull(t1_returns[0]);
		assertNull(t2_returns[0]);
		sleep(50);
		assertNull(t2_returns[0]);
		
		// we might not be sure how many steps the key has to go through to get out the end,
		// so do a little loop ... i don't think there are ever more than about 2 serialisations
		// of the key
		int steps = 0;
		while (steps++ < 5 && t1_returns[0] == null) {
			System.out.println("... stepping ...");
			stepper.release();
			sleep(20);
		}
		
		join(t1);
		assertEquals(value, t1_returns[0]);
		join(t2);
		assertEquals(value, t2_returns[0]);
	}
	
	@Test
	public void t0002_get_blocks_put() {
		final Semaphore control = new Semaphore(0);
		final Semaphore stepper = new Semaphore(0, true);
		final String key_contents = "t0002";
		final SteppingCacheKey key = new SteppingCacheKey(key_contents);
		final SteppingCacheKey stepping_key = new SteppingCacheKey(key_contents, stepper);
		final String value = "t0002_put_get";
		final String value_2 = "t0002_UPDATED";
		
		cache.put(key, value);
		assertEquals(value, cache.get(key));
		
		final String[] t1_returns = new String[1]; 
		final String[] t2_returns = new String[1]; 
		
		Thread t1 = new Thread(new Runnable() {
			public void run() {
				t1_returns[0] = cache.get(stepping_key);
			}
		});
		
		Thread t2 = new Thread(new Runnable() {
			public void run() {
				cache.put(key,value_2);
				t2_returns[0] = cache.get(key);
			}
		});
		
		t1.start();
		// give t1 chance to get going ...
		sleep(50);
		t2.start();
		// ditto t2
		sleep(50);
		assertNull(t1_returns[0]);
		assertNull(t2_returns[0]);
		sleep(50);
		assertNull(t2_returns[0]);
		
		// we might not be sure how many steps the key has to go through to get out the end,
		// so do a little loop ... i don't think there are ever more than about 2 serialisations
		// of the key
		int steps = 0;
		while (steps++ < 5 && t1_returns[0] == null) {
			System.out.println("... stepping ...");
			stepper.release();
			sleep(20);
		}
		
		join(t1);
		assertEquals(value, t1_returns[0]);
		join(t2);
		assertEquals(value_2, t2_returns[0]);
	}
	
	@Test
	public void t0003_put_blocks_put() {
		final Semaphore control = new Semaphore(0);
		final Semaphore stepper = new Semaphore(0, true);
		final String key_contents = "t0003";
		final SteppingCacheKey key = new SteppingCacheKey(key_contents);
		final SteppingCacheKey stepping_key = new SteppingCacheKey(key_contents, stepper);
		final String value = "t0003_put_get";
		final String value_2 = "t0003_UPDATED";
		
		cache.put(key, value);
		assertEquals(value, cache.get(key));
		
		final String[] t1_returns = new String[1]; 
		final String[] t2_returns = new String[2]; 
		
		Thread t1 = new Thread(new Runnable() {
			public void run() {
				cache.put(stepping_key, value);
				t1_returns[0] = value;
			}
		});
		
		Thread t2 = new Thread(new Runnable() {
			public void run() {
				t2_returns[0] = cache.get(key);
				cache.put(key,value_2);
				t2_returns[1] = cache.get(key);
			}
		});
		
		t1.start();
		// give t1 chance to get going ...
		sleep(50);
		t2.start();
		// ditto t2
		sleep(50);
		assertNull(t1_returns[0]);
		assertNull(t2_returns[0]);
		assertNull(t2_returns[1]);
		sleep(50);
		assertNull(t2_returns[0]);
		assertNull(t2_returns[1]);
		
		// we might not be sure how many steps the key has to go through to get out the end,
		// so do a little loop ... i don't think there are ever more than about 2 serialisations
		// of the key
		int steps = 0;
		while (steps++ < 5 && t1_returns[0] == null) {
			System.out.println("... stepping ...");
			stepper.release();
			sleep(20);
		}
		
		join(t1);
		assertEquals(value, t1_returns[0]);
		join(t2);
		assertEquals(value, t2_returns[0]);
		assertEquals(value_2, t2_returns[1]);
	}
}
