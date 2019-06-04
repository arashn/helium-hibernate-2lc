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

import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.function.Function;

import javax.cache.Cache;
import javax.cache.configuration.Factory;

import org.junit.AfterClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;

import com.serisys.helium.jcache.HeCache;
import com.serisys.helium.jcache.synch.ThreadsafeExecution;

@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class MTCacheContentTest {
	protected static Cache<CustomCacheKey, String> cache;

	public MTCacheContentTest() {
	}
	
	@AfterClass
	public static void tearDown() {
		cache.close();
	}
	
	
	protected abstract Factory<? extends ThreadsafeExecution> getSynchHandlerFactory();
	/**
	 * snnnn_ tests check out synchronisation in the abstract. 
	 */
	@Test
	public void s0001_testFactory() {
		Factory<? extends ThreadsafeExecution> fack = getSynchHandlerFactory();
		assertNotNull(fack);
		ThreadsafeExecution<String,String> texe = fack.create();
		assertNotNull(texe);
	}
	
	@Test
	public void s0002_checkSafety() {
		Factory<? extends ThreadsafeExecution> fack = getSynchHandlerFactory();
		assertNotNull(fack);
		final ThreadsafeExecution<String,String> texe = fack.create();
		assertNotNull(texe);
		
		final Semaphore control = new Semaphore(0, true);
		final StringBuilder bob = new StringBuilder();

		final Function<String,HeCache<String,String>.CacheEntryBundle> func_a = key -> {
			acquire(control);
			bob.append("A");
			control.release();
			sleep(30);
			acquire(control);
			bob.append("B");
			control.release();
			return null;
		};
		final Function<String,HeCache<String,String>.CacheEntryBundle> func_b = key -> {
			acquire(control);
			bob.append("C");
			control.release();
			sleep(50);
			acquire(control);
			bob.append("D");
			control.release();
			return null;
		};
		
		final String key = "S0002";
		
		Thread t_a = new Thread(new Runnable() {
			public void run() {
				texe.observer(key, func_a);
			}
		});
		
		t_a.start();
		sleep(30);

		Thread t_b = new Thread(new Runnable() {
			public void run() {
				texe.observer(key, func_b);
			}
		});
		
		t_b.start();

		sleep(30);
		
		control.release();
		
		try {
			t_a.join();
			t_b.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println(bob.toString());
		s0002_testCondition(bob.toString());
	}
	
	protected abstract void s0002_testCondition(String to_check);


	@Test
	public void s0003_checkSafety() {
		Factory<? extends ThreadsafeExecution> fack = getSynchHandlerFactory();
		assertNotNull(fack);
		final ThreadsafeExecution<String,String> texe = fack.create();
		assertNotNull(texe);
		
		final Map<String,String> map = new java.util.HashMap<String,String>();
		for (int i = 0; i < 10; i++) {
			final StringBuilder bob = new StringBuilder();
			bob.append("Key_").append(i);
			map.put(bob.toString(), bob.append("_value").toString());
		}
		
		
		final Semaphore control = new Semaphore(0, true);

		final ConcurrentModificationException[] expected = new ConcurrentModificationException[1];
		
		final Function<String,HeCache<String,String>.CacheEntryBundle> func_a = key -> {
			acquire(control);
			control.release();
			Set<Map.Entry<String,String>> entries = map.entrySet();
			try {
				for (Map.Entry<String,String> entry : entries) {
					sleep(20);
					System.out.println(entry.getKey() + " -> " + entry.getValue());
				}
			} catch (ConcurrentModificationException cme) {
				expected[0] = cme;
			}
			return null;
		};
		
		final Function<String,HeCache<String,String>.CacheEntryBundle> func_b = key -> {
			acquire(control);
				for (int i = 0; i < 10; i++) {
					sleep(20);
					final StringBuilder bob = new StringBuilder();
					bob.append("Xxx_").append(i);
					map.put(bob.toString(), bob.append("_value").toString());
				}
			return null;
		};
		
		final String key = "S0002";
		
		Thread t_a = new Thread(new Runnable() {
			public void run() {
				texe.observer(key, func_a);
			}
		});
		
		t_a.start();

		Thread t_b = new Thread(new Runnable() {
			public void run() {
				texe.observer(key, func_b);
			}
		});
		
		sleep(30);
		t_b.start();

		sleep(30);
		
		control.release();
		
		try {
			t_a.join();
			t_b.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		s0003_testCondition(expected[0]);
	}
	
	protected abstract void s0003_testCondition(ConcurrentModificationException e);

	@Test
	public void s0004_checkSafety() {
		Factory<? extends ThreadsafeExecution> fack = getSynchHandlerFactory();
		assertNotNull(fack);
		final ThreadsafeExecution<String,String> texe = fack.create();
		assertNotNull(texe);
		
		final Semaphore control = new Semaphore(0, true);
		final StringBuilder bob = new StringBuilder();

		final Function<String,HeCache<String,String>.CacheEntryBundle> func_a = key -> {
			acquire(control);
			bob.append("A");
			control.release();
			sleep(30);
			acquire(control);
			bob.append("B");
			control.release();
			return null;
		};
		final Function<String,HeCache<String,String>.CacheEntryBundle> func_b = key -> {
			acquire(control);
			bob.append("C");
			control.release();
			sleep(50);
			acquire(control);
			bob.append("D");
			control.release();
			return null;
		};
		
		final String key_a = "A0002";
		final String key_b = "B0002";
		
		Thread t_a = new Thread(new Runnable() {
			public void run() {
				texe.observer(key_a, func_a);
			}
		});
		
		t_a.start();

		Thread t_b = new Thread(new Runnable() {
			public void run() {
				texe.observer(key_b, func_b);
			}
		});
		
		t_b.start();

		sleep(30);
		
		control.release();
		
		try {
			t_a.join();
			t_b.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println(bob.toString());
		s0004_testCondition(bob.toString());
	}
	
	protected abstract void s0004_testCondition(String to_check);

	static void acquire(Semaphore control) {
		try {
			control.acquire();
		} catch (InterruptedException e) {
			
		}
	}
	
	static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			
		}
	}
	
	static void join(Thread t) {
		try {
			t.join();
		} catch (InterruptedException e) {
			
		}
	}
	
	public abstract void t0000_checkLevel();
	
}
