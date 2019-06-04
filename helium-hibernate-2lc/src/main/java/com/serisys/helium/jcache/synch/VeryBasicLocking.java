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

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.cache.Cache.Entry;
import javax.cache.processor.EntryProcessorException;

import com.levyx.helium.HeliumIterator;
import com.serisys.helium.jcache.CacheEntryImpl;
import com.serisys.helium.jcache.HeCache;
import com.serisys.helium.jcache.HeCachingProvider;
import com.serisys.helium.jcache.MutableHeCacheForwardGotEntry;

public class VeryBasicLocking<K,V> implements ThreadsafeExecution<K, V> {

	public VeryBasicLocking() {
		HeCachingProvider.logger.info(">>>>>>>>>>>>>>>> USING BASIC SYNCH LOCKING");		
	}


	@Override
	public HeCache<K,V>.CacheEntryBundle observer(K key, Function<K, HeCache<K,V>.CacheEntryBundle> observer) {
		return observer.apply(key);
	}
	
	@Override
	public synchronized void observerUpdater(K key, Consumer<K> updater) {
		updater.accept(key);
	}
	
	@Override
	public synchronized void observerUpdateIterator(K key, HeliumIterator iterator, Consumer<HeliumIterator> updater) {
		updater.accept(iterator);
	}
	
	@Override
	public synchronized void updater(HeCache<K,V>.CacheEntryBundle bundle, Consumer<HeCache<K,V>.CacheEntryBundle> updater) {
		updater.accept(bundle);
	}

	@Override
	public synchronized HeCache<K,V>.CacheEntryBundle bobserver(K key, Function<K, HeCache<K,V>.CacheEntryBundle> observer) {
		return observer.apply(key);
	}

	@Override
	public synchronized boolean bobserver(K key, V value, BiFunction<K, V, Boolean> observer) {
		return observer.apply(key, value);
	}
	
	@Override
	public synchronized boolean bobserver(K key, byte[] old_val_bytes, BiFunction<K, byte[], Boolean> observer) {
		return observer.apply(key, old_val_bytes);
	}

	@Override
	public synchronized HeCache<K,V>.CacheEntryBundle observer(K key, V value, BiFunction<K, V, HeCache<K,V>.CacheEntryBundle> observer) {
		return observer.apply(key, value);
	}

	@Override
	public synchronized boolean bupdater(K key, Function<K, Boolean> updater) {
		return updater.apply(key);
	}

	@Override
	public synchronized void updater(K key, Consumer<K> updater) {
		updater.accept(key);
	}

	@Override
	public synchronized boolean updater(K key, byte[] old_val_bytes, BiFunction<K,byte[],Boolean> updater) {
		return updater.apply(key, old_val_bytes);
	}
	
	@Override
	public synchronized boolean updater(HeCache<K, V>.CacheEntryBundle bundle,
			Function<HeCache<K, V>.CacheEntryBundle, Boolean> updater) {
		return updater.apply(bundle);
	}
	
	@Override
	public HeCache<K, V>.CacheEntryBundle observer(K key, byte[] old_val_bytes, V newValue, BiFunction<Entry<K, byte[]>, V, HeCache<K, V>.CacheEntryBundle> observer) {
		Entry<K,byte[]> keyToOldValue = new CacheEntryImpl<K, byte[]>(key, old_val_bytes);
		return observer.apply(keyToOldValue, newValue);
	}

	@Override
	public SynchHandling getSynchHandlingLevel() {
		return SynchHandling.SINGLE_LEVEL;
	}


	@Override
	public synchronized HeCache<K, V>.ValueHolder updater(K key, Function<K, HeCache<K, V>.ValueHolder> updater) {
		return updater.apply(key);
	}
	
	@Override
	public synchronized <T> T observe(K key, com.serisys.helium.jcache.HeCache<K,V>.EntryProcessorInvocation<K,V,T> invoc, java.util.function.BiFunction<K,com.serisys.helium.jcache.HeCache<K,V>.EntryProcessorInvocation<K,V,T>,T> observer) throws EntryProcessorException {
		return observer.apply(key, invoc);
	}
	
	@Override
	public synchronized void updater(K key, MutableHeCacheForwardGotEntry<K, V> mute,
			Consumer<MutableHeCacheForwardGotEntry<K, V>> updater) {
		updater.accept(mute);
	}
}
