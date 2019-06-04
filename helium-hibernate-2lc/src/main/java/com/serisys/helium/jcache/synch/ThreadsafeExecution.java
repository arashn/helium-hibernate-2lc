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

import javax.cache.Cache;
import javax.cache.processor.EntryProcessorException;

import com.levyx.helium.HeliumIterator;
import com.serisys.helium.jcache.HeCache;
import com.serisys.helium.jcache.MutableHeCacheForwardGotEntry;

public interface ThreadsafeExecution<K, V> {
	HeCache<K,V>.CacheEntryBundle bobserver(K key, Function<K, HeCache<K,V>.CacheEntryBundle> observer);
	
	HeCache<K,V>.CacheEntryBundle observer(K key, Function<K, HeCache<K,V>.CacheEntryBundle> observer);
	
	void observerUpdater(K key, Consumer<K> updater);

	void observerUpdateIterator(K key, HeliumIterator iterator, Consumer<HeliumIterator> updater);

	boolean bobserver(K key, V value, BiFunction<K, V, Boolean> observer);

	boolean bobserver(K key, byte[] old_val_bytes, BiFunction<K, byte[], Boolean> observer);

	HeCache<K,V>.CacheEntryBundle observer(K key, V value, BiFunction<K, V, HeCache<K,V>.CacheEntryBundle> observer);

	boolean bupdater(K key, Function<K, Boolean> observer);

	HeCache<K,V>.ValueHolder updater(K key, Function<K, HeCache<K,V>.ValueHolder> updater);
	
	void updater(K key, MutableHeCacheForwardGotEntry<K,V> mute, Consumer<MutableHeCacheForwardGotEntry<K,V>> updater);

	void updater(K key, Consumer<K> updater);

	void updater(HeCache<K,V>.CacheEntryBundle bundle, Consumer<HeCache<K,V>.CacheEntryBundle> updater);
	
	boolean updater(K key, byte[] old_val_bytes, BiFunction<K,byte[],Boolean> updater);
	boolean updater(HeCache<K,V>.CacheEntryBundle bundle, Function<HeCache<K,V>.CacheEntryBundle,Boolean> updater);

	HeCache<K,V>.CacheEntryBundle observer(K key, byte[] old_val_bytes, V newValue, BiFunction<Cache.Entry<K, byte[]>, V, HeCache<K,V>.CacheEntryBundle> observer);

	SynchHandling getSynchHandlingLevel();
	
	<T> T observe(K key, HeCache<K,V>.EntryProcessorInvocation<K, V, T> invoc, BiFunction<K, HeCache<K,V>.EntryProcessorInvocation<K, V, T>, T> observer) throws EntryProcessorException;
}
