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
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.cache.Cache.Entry;

import com.levyx.helium.HeliumIterator;
import com.serisys.helium.jcache.CacheEntryImpl;
import com.serisys.helium.jcache.HeCache;
import com.serisys.helium.jcache.HeCachingProvider;
import com.serisys.helium.jcache.MutableHeCacheForwardGotEntry;
import com.serisys.helium.jcache.HeCache.ValueHolder;

public class DualLevelLockHandler<K, V> implements ThreadsafeExecution<K, V> {
	private final Map<K,ReentrantReadWriteLock> locksByKey = new ConcurrentHashMap<K,ReentrantReadWriteLock>();
	private final ThreadLocal<Map<K,ReentrantReadWriteLock>> locksHeld = new ThreadLocal<Map<K,ReentrantReadWriteLock>>();

	public DualLevelLockHandler() {
		HeCachingProvider.logger.info(">>>>>>>>>>>>>>>> USING DUAL LEVEL LOCKING");		
	}

	private ReentrantReadWriteLock heldLock(K key) {
		Map<K,ReentrantReadWriteLock> locks_held = locksHeld.get();
		if (locks_held == null) {
			locks_held = new java.util.HashMap<K, ReentrantReadWriteLock>();
			locksHeld.set(locks_held);
			return null;
		} else {
			ReentrantReadWriteLock lock = locks_held.get(key);
			return lock;
		}
	}
	
	private ReentrantReadWriteLock newReadLock(K key) {
		ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
		lock.readLock().lock();
		// heldLock always gets checked just before we come into this method.
		// it sets up the map for the current thread. so we don't have to. 
		locksHeld.get().put(key, lock);
		return lock;
	}

	private ReentrantReadWriteLock newWriteLock(K key) {
		ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
		lock.writeLock().lock();
		// heldLock always gets checked just before we come into this method.
		// it sets up the map for the current thread. so we don't have to. 
		locksHeld.get().put(key, lock);
		return lock;
	}

	private ReentrantReadWriteLock readLock(K key) {
		boolean outermost = true;
		ReentrantReadWriteLock lock = heldLock(key);		
		if (lock == null) {
			lock = newReadLock(key);
			while (true) {
				ReentrantReadWriteLock existing = locksByKey.putIfAbsent(key, lock);
				if (existing == null) {
					return lock;
				} else {
					// wait on the old lock
					existing.readLock().lock();
					// now it is done, no other thread will have it, so we can release. 
					// And we now have the lock on the key. 
					existing.readLock().unlock();
				}
			}
		} else {
			outermost = false; 
			return null;
		}
	
	}

	private ReentrantReadWriteLock writeLock(K key) {
		boolean outermost = true;
		ReentrantReadWriteLock lock = heldLock(key);		
		if (lock == null) {
			lock = newWriteLock(key);
			while (true) {
				ReentrantReadWriteLock existing = locksByKey.putIfAbsent(key, lock);
				if (existing == null) {
					return lock;
				} else {
					// wait on the old lock
					existing.readLock().lock();
					existing.writeLock().lock();
					// now it is done, no other thread will have it, so we can release. 
					// And we now have the lock on the key. 
					existing.writeLock().unlock();
					existing.readLock().unlock();
				}
			}
		} else {
			outermost = false; 
			return null;
		}
	
	}

	private void readUnlock(K key, ReentrantReadWriteLock lock) {
		if (lock == null) {
			// not the outermost locking context of a thread re-entrantly locking this resource
			return;
		}
		if (!lock.isWriteLockedByCurrentThread()) {
			locksByKey.remove(key);
			locksHeld.get().remove(key);
		}
		lock.readLock().unlock();
	}

	private void writeUnlock(K key, ReentrantReadWriteLock lock) {
		if (lock == null) {
			// not the outermost locking context of a thread re-entrantly locking this resource
			return;
		}
		if (lock.getReadHoldCount() == 0) {
			locksByKey.remove(key);
			locksHeld.get().remove(key);
		}
		lock.writeLock().unlock();
	}

	@Override
	public HeCache<K,V>.CacheEntryBundle observer(K key, Function<K, HeCache<K,V>.CacheEntryBundle> observer) {
		HeCache<K,V>.CacheEntryBundle value = null;
		ReentrantReadWriteLock lock = readLock(key);
		try {
			value = observer.apply(key);
		} finally {
			readUnlock(key, lock);
		}
		return value;
	}

	@Override
	public <T> T observe(K key, com.serisys.helium.jcache.HeCache<K,V>.EntryProcessorInvocation<K,V,T> invoc, java.util.function.BiFunction<K,com.serisys.helium.jcache.HeCache<K,V>.EntryProcessorInvocation<K,V,T>,T> observer) throws javax.cache.processor.EntryProcessorException {
		T result = null;
		ReentrantReadWriteLock lock = readLock(key);
		try {
			result = observer.apply(key, invoc);
		} finally {
			readUnlock(key, lock);
		}
		return result;
	}
	
	@Override
	public void observerUpdater(K key, Consumer<K> updater) {
		ReentrantReadWriteLock rlock = readLock(key);
		ReentrantReadWriteLock wlock = writeLock(key); // will actually be the same instance
		try {
			updater.accept(key);
		} finally {
			writeUnlock(key, wlock);
			readUnlock(key, rlock);
		}
	}
	
	@Override
	public void observerUpdateIterator(K key, HeliumIterator iterator, Consumer<HeliumIterator> updater) {
		ReentrantReadWriteLock rlock = readLock(key);
		ReentrantReadWriteLock wlock = writeLock(key); // will actually be the same instance
		try {
			updater.accept(iterator);
		} finally {
			writeUnlock(key, wlock);
			readUnlock(key, rlock);
		}
	}

	@Override
	public void updater(HeCache<K, V>.CacheEntryBundle bundle, Consumer<HeCache<K, V>.CacheEntryBundle> updater) {
		ReentrantReadWriteLock lock = writeLock(bundle.getKey());
		try {
			updater.accept(bundle);
		} finally {
			writeUnlock(bundle.getKey(), lock);
		}
	}

	@Override
	public HeCache<K,V>.CacheEntryBundle bobserver(K key, Function<K, HeCache<K,V>.CacheEntryBundle> observer) {
		ReentrantReadWriteLock lock = readLock(key);
		try {
			return observer.apply(key);
		} finally {
			readUnlock(key, lock);
		}
	}

	@Override
	public boolean bobserver(K key, V value, BiFunction<K, V, Boolean> observer) {
		ReentrantReadWriteLock lock = readLock(key);
		try {
			return observer.apply(key, value);
		} finally {
			readUnlock(key, lock);
		}
	}
	
	@Override
	public synchronized boolean bobserver(K key, byte[] old_val_bytes, BiFunction<K, byte[], Boolean> observer) {
		ReentrantReadWriteLock lock = readLock(key);
		try {
			return observer.apply(key, old_val_bytes);
		} finally {
			readUnlock(key, lock);
		}
	}

	@Override
	public HeCache<K,V>.CacheEntryBundle observer(K key, V value, BiFunction<K, V, HeCache<K,V>.CacheEntryBundle> observer) {
		ReentrantReadWriteLock lock = readLock(key);
		try {
			return observer.apply(key, value);
		} finally {
			readUnlock(key, lock);
		}
	}

	@Override
	public boolean bupdater(K key, Function<K, Boolean> observer) {
		ReentrantReadWriteLock lock = writeLock(key);
		try {
			return observer.apply(key);
		} finally {
			writeUnlock(key, lock);
		}
	}

	@Override
	public HeCache<K,V>.ValueHolder updater(K key, Function<K, HeCache<K,V>.ValueHolder> updater) {
		ReentrantReadWriteLock lock = writeLock(key);
		try {
			return updater.apply(key);
		} finally {
			writeUnlock(key, lock);
		}
	}

	@Override
	public void updater(K key, Consumer<K> updater) {
		ReentrantReadWriteLock lock = writeLock(key);
		try {
			updater.accept(key);
		} finally {
			writeUnlock(key, lock);
		}
	}
	
	@Override
	public void updater(K key, MutableHeCacheForwardGotEntry<K, V> mute,
			Consumer<MutableHeCacheForwardGotEntry<K, V>> updater) {
		ReentrantReadWriteLock lock = writeLock(key);
		try {
			updater.accept(mute);
		} finally {
			writeUnlock(key, lock);
		}
	}

	@Override
	public boolean updater(K key, byte[] old_val_bytes, BiFunction<K,byte[],Boolean> updater) {
		ReentrantReadWriteLock lock = writeLock(key);
		try {
			return updater.apply(key, old_val_bytes);
		} finally {
			writeUnlock(key, lock);
		}
	}
	
	@Override
	public boolean updater(HeCache<K, V>.CacheEntryBundle bundle,
			Function<HeCache<K, V>.CacheEntryBundle, Boolean> updater) {
		ReentrantReadWriteLock lock = writeLock(bundle.getKey());
		try {
			return updater.apply(bundle);
		} finally {
			writeUnlock(bundle.getKey(), lock);
		}
	}

	@Override
	public HeCache<K, V>.CacheEntryBundle observer(K key, byte[] old_val_bytes, V newValue, BiFunction<Entry<K, byte[]>, V, HeCache<K, V>.CacheEntryBundle> observer) {
		ReentrantReadWriteLock lock = readLock(key);
		try {
			Entry<K,byte[]> keyToOldValue = new CacheEntryImpl<K,byte[]>(key, old_val_bytes);
			return observer.apply(keyToOldValue, newValue);
		} finally {
			readUnlock(key, lock);
		}
	}
	
	@Override
	public SynchHandling getSynchHandlingLevel() {
		return SynchHandling.OBSERVER_UPDATER;
	}

}
