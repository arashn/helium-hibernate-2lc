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

package com.serisys.helium.jcache.synch;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.cache.Cache.Entry;

import com.levyx.helium.HeliumIterator;
import com.serisys.helium.jcache.CacheEntryImpl;
import com.serisys.helium.jcache.HeCache;
import com.serisys.helium.jcache.HeCachingProvider;
import com.serisys.helium.jcache.MutableHeCacheForwardGotEntry;

public class SingleLevelLockHandler<K, V> implements ThreadsafeExecution<K, V> {
	private final Map<K,ReentrantLock> locksByKey = new ConcurrentHashMap<K,ReentrantLock>();
	private final ThreadLocal<Map<K,ReentrantLock>> locksHeld = new ThreadLocal<Map<K,ReentrantLock>>();
	
	public SingleLevelLockHandler() {
		HeCachingProvider.logger.info(">>>>>>>>>>>>>>>> USING SINGLE LEVEL LOCKING");
	}
	
	private ReentrantLock heldLock(K key) {
		Map<K,ReentrantLock> locks_held = locksHeld.get();
		if (locks_held == null) {
			locks_held = new java.util.HashMap<K, ReentrantLock>();
			locksHeld.set(locks_held);
			return null;
		} else {
			ReentrantLock lock = locks_held.get(key);
			return lock;
		}
	}
	
	private ReentrantLock newLock(K key) {
		ReentrantLock lock = new ReentrantLock(true);
		lock.lock();
		// heldLock always gets checked just before we come into this method.
		// it sets up the map for the current thread. so we don't have to. 
		locksHeld.get().put(key, lock);
		return lock;
	}

	private ReentrantLock lock(K key) {
		boolean outermost = true;
		ReentrantLock lock = heldLock(key);		
		if (lock == null) {
			lock = newLock(key);
			while (true) {
				ReentrantLock existing = locksByKey.putIfAbsent(key, lock);
				if (existing == null) {
					return lock;
				} else {
					// wait on the old lock
					existing.lock();
					// now it is done, no other thread will have it, so we can release. 
					// And we now have the lock on the key. 
					existing.unlock();
				}
			}
		} else {
			outermost = false; 
			return null;
		}
	}
	
	private void unlock(K key, ReentrantLock lock) {
		if (lock == null) {
			// not the outermost locking context of a thread re-entrantly locking this resource
			return;
		}
		locksByKey.remove(key);
		locksHeld.get().remove(key);
		lock.unlock();
	}
	
	@Override
	public HeCache<K,V>.CacheEntryBundle observer(K key, Function<K, HeCache<K,V>.CacheEntryBundle> observer) {
		HeCache<K,V>.CacheEntryBundle value = null;
		ReentrantLock lock = lock(key);
		try {
			value = observer.apply(key);
		} finally {
			unlock(key, lock);
		}
		return value;
	}
	
	@Override
	public void observerUpdater(K key, Consumer<K> updater) {
		updater(key, updater);
	}
	
	@Override
	public void observerUpdateIterator(K key, HeliumIterator iterator, Consumer<HeliumIterator> updater) {
		ReentrantLock lock = lock(key);
		try {
			updater.accept(iterator);
		} finally {
			unlock(key, lock);
		}
	}
	
	@Override
	public void updater(HeCache<K,V>.CacheEntryBundle bundle, Consumer<HeCache<K,V>.CacheEntryBundle> updater) {
		ReentrantLock lock = lock(bundle.getKey());
		try {
			updater.accept(bundle);
		} finally {
			unlock(bundle.getKey(), lock);
		}
	}

	@Override
	public HeCache<K,V>.CacheEntryBundle bobserver(K key, Function<K, HeCache<K,V>.CacheEntryBundle> observer) {
		ReentrantLock lock = lock(key);
		try {
			return observer.apply(key);
		} finally {
			unlock(key, lock);
		}
	}

	@Override
	public boolean bobserver(K key, V value, BiFunction<K, V, Boolean> observer) {
		ReentrantLock lock = lock(key);
		try {
			return observer.apply(key, value);
		} finally {
			unlock(key, lock);
		}
	}
	
	@Override
	public synchronized boolean bobserver(K key, byte[] old_val_bytes, BiFunction<K, byte[], Boolean> observer) {
		ReentrantLock lock = lock(key);
		try {
			return observer.apply(key, old_val_bytes);
		} finally {
			unlock(key, lock);
		}
	}

	@Override
	public HeCache<K,V>.CacheEntryBundle observer(K key, V value, BiFunction<K, V, HeCache<K,V>.CacheEntryBundle> observer) {
		ReentrantLock lock = lock(key);
		try {
			return observer.apply(key, value);
		} finally {
			unlock(key, lock);
		}
	}

	@Override
	public boolean bupdater(K key, Function<K, Boolean> observer) {
		ReentrantLock lock = lock(key);
		try {
			return observer.apply(key);
		} finally {
			unlock(key, lock);
		}
	}

	@Override
	public HeCache<K,V>.ValueHolder updater(K key, Function<K, HeCache<K,V>.ValueHolder> updater) {
		ReentrantLock lock = lock(key);
		try {
			return updater.apply(key);
		} finally {
			unlock(key, lock);
		}
	}

	@Override
	public void updater(K key, Consumer<K> updater) {
		ReentrantLock lock = lock(key);
		try {
			updater.accept(key);
		} finally {
			unlock(key, lock);
		}
	}
	
	@Override
	public void updater(K key, MutableHeCacheForwardGotEntry<K, V> mute,
			Consumer<MutableHeCacheForwardGotEntry<K, V>> updater) {
		ReentrantLock lock = lock(key);
		try {
			updater.accept(mute);
		} finally {
			unlock(key, lock);
		}
	}

	@Override
	public boolean updater(K key, byte[] old_val_bytes, BiFunction<K,byte[],Boolean> updater) {
		ReentrantLock lock = lock(key);
		try {
			return updater.apply(key, old_val_bytes);
		} finally {
			unlock(key, lock);
		}
	}
	
	@Override
	public boolean updater(HeCache<K, V>.CacheEntryBundle bundle,
			Function<HeCache<K, V>.CacheEntryBundle, Boolean> updater) {
		ReentrantLock lock = lock(bundle.getKey());
		try {
			return updater.apply(bundle);
		} finally {
			unlock(bundle.getKey(), lock);
		}
	}

	@Override
	public HeCache<K, V>.CacheEntryBundle observer(K key, byte[] old_val_bytes, V newValue, BiFunction<Entry<K, byte[]>, V, HeCache<K, V>.CacheEntryBundle> observer) {
		ReentrantLock lock = lock(key);
		try {
			Entry<K,byte[]> keyToOldValue = new CacheEntryImpl<K,byte[]>(key, old_val_bytes);
			return observer.apply(keyToOldValue, newValue);
		} finally {
			unlock(key, lock);
		}
	}
	
	@Override
	public SynchHandling getSynchHandlingLevel() {
		return SynchHandling.SINGLE_LEVEL;
	}
	
	@Override
	public <T> T observe(K key, com.serisys.helium.jcache.HeCache<K,V>.EntryProcessorInvocation<K,V,T> invoc, java.util.function.BiFunction<K,com.serisys.helium.jcache.HeCache<K,V>.EntryProcessorInvocation<K,V,T>,T> observer) throws javax.cache.processor.EntryProcessorException {
		ReentrantLock lock = lock(key);
		try {
			return observer.apply(key, invoc);
		} finally {
			unlock(key, lock);
		}
	}
}
