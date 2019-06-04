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

package com.serisys.helium.jcache.mx;

import java.lang.management.ManagementFactory;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.integration.CompletionListener;
import javax.cache.management.CacheStatisticsMXBean;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.EntryProcessorResult;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import com.serisys.helium.jcache.CacheNotModified;
import com.serisys.helium.jcache.HeCache;
import com.serisys.helium.jcache.KeysNotModified;
import com.serisys.helium.jcache.WrappableCache;

public class CacheMXStatsBeanImpl<K, V> extends CacheMXBeanRoot implements WrappableCache<K,V>, CacheStatisticsMXBean {
	
	private WrappableCache<K, V> innerCache;
	
	private CacheStats statistics;
	
	public CacheMXStatsBeanImpl(ObjectName objectName, WrappableCache<K, V> innerCache) {
		super(objectName);
		this.innerCache = innerCache;
		innerCache.unwrap(HeCache.class).beWrapped(this);
		this.statistics = new CacheStats(System.currentTimeMillis());
	}

	public V get(K key) {		
		long start = System.nanoTime();
		V ret = innerCache.get(key);
		long duration = System.nanoTime() - start;
		if (ret == null) {
			statistics.incrementCacheMisses(duration);
		}
		else {
			statistics.incrementCacheHits(duration);
		}
		return ret;
	}
	
	@Override
	public HeCache<K, V>.ValueHolder getNoExpiryCheck(K key) {
		long start = System.nanoTime();
		HeCache<K, V>.ValueHolder ret = innerCache.getNoExpiryCheck(key);
		long duration = System.nanoTime() - start;
		if (ret == null) {
			statistics.incrementCacheMisses(duration);
		}
		else {
			statistics.incrementCacheHits(duration);
		}
		return ret;
	}

	public WrappableCache<K,V> getInnerCache() {
		return innerCache;
	}
	public Map<K, V> getAll(Set<? extends K> keys) {
		long start = System.nanoTime();
		Map<K,V> ret = innerCache.getAll(keys);
		long duration = System.nanoTime() - start;
		int hit = ret.size();
		int missed = keys.size() - hit;
		statistics.increaseCacheHitsAndMisses(hit, missed, duration);
		return ret;
	}

	public boolean containsKey(K key) {
		// Spec says containsKey does not update the stats
		return innerCache.containsKey(key);
	}

	public void loadAll(Set<? extends K> keys, boolean replaceExistingValues, CompletionListener completionListener) {
		innerCache.loadAll(keys, replaceExistingValues, completionListener);
	}

	public void put(K key, V value) {
		try {
			wrappedPut(key, value);
		} catch (CacheNotModified e) {
			// nothing to do here
		}
	}

	@Override
	public V getAndPut(K key, V value) {
		V ret = null;
		try {
			ret = wrappedGetAndPut(key, value);
		} catch (CacheNotModified e) {
			// nothing to do here
		}
		return ret;
	}
	
	@Override
	public V wrappedGetAndPut(K key, V value) throws CacheNotModified {
		V ret = null;
		long start = System.nanoTime();
		try {
			ret = innerCache.wrappedGetAndPut(key, value);
			long duration = System.nanoTime() - start;
			statistics.incrementCachePuts(duration);
			if (ret == null) {
				statistics.incrementCacheMisses(duration);
			}
			else {
				statistics.incrementCacheHits(duration);
			}
		} catch (CacheNotModified e) {
			// the cache was not modified because of an expiry, so we don't increment the stats
		}
		return ret;
	}

	public void putAll(Map<? extends K, ? extends V> map) {
		try {
			wrappedPutAll(map);
		} catch (KeysNotModified e) {
			// nothing to do here
		}
	}

	public boolean putIfAbsent(K key, V value) {
		long start = System.nanoTime();
		boolean ret = innerCache.putIfAbsent(key, value);
		long duration = System.nanoTime() - start;
		if (ret) {
			 //this means that there was no key in the Cache and the put succeeded
			statistics.incrementCachePuts(duration);
			statistics.incrementCacheMisses();
		}
		else {
			 //this means that there was a key in the Cache and the put did not succeed
			statistics.incrementCacheHits();
		}
		return ret;
	}

	public boolean remove(K key) {
		long start = System.nanoTime();
		boolean ret = innerCache.remove(key);
		long duration = System.nanoTime() - start;
		if (ret) {
			statistics.incrementCacheRemovals(duration);
		}
		return ret;
	}

	public boolean remove(K key, V oldValue) {
		long start = System.nanoTime();
		boolean ret = innerCache.remove(key, oldValue);
		long duration = System.nanoTime() - start;
		if (ret) {
			statistics.incrementCacheHits(duration);
			statistics.incrementCacheRemovals(duration);
		}
		else {
			statistics.incrementCacheMisses(duration);
		}
		return ret;
	}

	public V getAndRemove(K key) {
		long start = System.nanoTime();
		V ret = innerCache.getAndRemove(key);
		long duration = System.nanoTime() - start;
		if (ret == null) {
			statistics.incrementCacheMisses(duration);
		}
		else {
			statistics.incrementCacheHits(duration);
			statistics.incrementCacheRemovals(duration);
		}
		return ret;
	}

	public boolean replace(K key, V oldValue, V newValue) {
		long start = System.nanoTime();
		// A bit ugly that we perform a containsKey then a replace
		// But the stats want a hit if the key is there & a miss if it isn't whether the replace is successful or not.
		// But at least we do not try the replace if the containsKey fails
		boolean contained = innerCache.containsKey(key);
		if (contained) {
			boolean ret = innerCache.replace(key, oldValue, newValue);
			long duration = System.nanoTime() - start;
			if (ret) {
				statistics.incrementCachePuts(duration);
			}
			statistics.incrementCacheHits(duration);
			return ret;
		}
		else {
			long duration = System.nanoTime() - start;
			statistics.incrementCacheMisses(duration);
			return false;
		}
	}

	public boolean replace(K key, V value) {
		long start = System.nanoTime();
		boolean ret = innerCache.replace(key, value);
		long duration = System.nanoTime() - start;
		if (ret) {
			statistics.incrementCachePuts(duration);
			statistics.incrementCacheHits(duration);
		}
		else {
			statistics.incrementCacheMisses(duration);
		}
		return ret;
	}

	public V getAndReplace(K key, V value) {
		long start = System.nanoTime();
		V ret = innerCache.getAndReplace(key, value);
		long duration = System.nanoTime() - start;
		if (ret == null) {
			statistics.incrementCacheMisses(duration);
		}
		else {
			statistics.incrementCachePuts(duration);
			statistics.incrementCacheHits(duration);
		}
		return ret;
	}

	public void removeAll(Set<? extends K> keys) {
		Iterator<? extends K> itty = keys.iterator();
		while (itty.hasNext()) {
			remove(itty.next());
		}
	}

	public void removeAll() {
		long start = System.nanoTime();
		Iterator<Entry<K, V>> innerIter = innerCache.iterator();
		int rems = 0;
		while (innerIter.hasNext()) {
			innerIter.next();
			innerIter.remove();
			rems++;
		}
		// oddly the RICache removeAll does not add any "remove time" to the stats - doesn't seem right to me
		statistics.increaseCacheRemovals(rems, System.nanoTime() - start);	
	}

	public void clear() {
		// A bit odd?
		// We should not be passing the stats-cache object to the outside world 
		// It should always be wrapped in the public face - the HoHoCache
		// Calling clear on the HoHoCache clears the inner HeCache only not the stats
		// Clear on the stats cache - will clear the stats only
		// This clear is generally invoked by the MBean action 	
		statistics.clear();
	}

	public <C extends Configuration<K, V>> C getConfiguration(Class<C> clazz) {
		return innerCache.getConfiguration(clazz);
	}

	public <T> T invoke(K key, EntryProcessor<K, V, T> entryProcessor, Object... arguments) throws EntryProcessorException {
		return innerCache.invoke(key, entryProcessor, arguments);
	}

	public <T> Map<K, EntryProcessorResult<T>> invokeAll(Set<? extends K> keys, EntryProcessor<K, V, T> entryProcessor, Object... arguments) {
		return innerCache.invokeAll(keys, entryProcessor, arguments);
	}

	public String getName() {
		return innerCache.getName();
	}

	public CacheManager getCacheManager() {
		return innerCache.getCacheManager();
	}

	public void close() {
	   //disable statistics and management
	   setStatisticsEnabled(false);
	   innerCache.close();
	}

	private void setStatisticsEnabled(boolean enabled) {
		MBeanServer mcBeans = ManagementFactory.getPlatformMBeanServer();
		String on = new StringBuilder()
			.append("javax.cache:type=CacheStatistics,CacheManager=")
			.append(getCacheManager().getURI().toASCIIString())
			.append(",Cache=")
			.append(getName())
			.toString();
		ObjectName name;
		try {
			name = new ObjectName(on);
			if (enabled) {
				mcBeans.registerMBean(this, name);
			}
			else {
				mcBeans.unregisterMBean(name);
			}
			
		} catch (InstanceAlreadyExistsException e) {
			// ignore this - we are already being managed
		} catch (MalformedObjectNameException | MBeanRegistrationException | NotCompliantMBeanException | InstanceNotFoundException e) {
			throw new CacheException(e);
		}
	}

	public boolean isClosed() {
		return innerCache.isClosed();
	}

	public <T> T unwrap(Class<T> clazz) {
		if (clazz.isAssignableFrom(CacheMXStatsBeanImpl.class)) { 
			return clazz.cast(this);
		} else {
			return innerCache.unwrap(clazz);
		}
	}

	public void registerCacheEntryListener(CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration) {
		innerCache.registerCacheEntryListener(cacheEntryListenerConfiguration);
	}

	public void deregisterCacheEntryListener(CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration) {
		innerCache.deregisterCacheEntryListener(cacheEntryListenerConfiguration);
	}

	public Iterator<Entry<K, V>> iterator() {
		return new Iterator<Entry<K, V>>() {
			Iterator<Entry<K, V>> innerIter = innerCache.iterator();
			Entry<K, V> nextEntry = null;
			
			@Override
			public boolean hasNext() {
				if (nextEntry == null) {
					long start = System.nanoTime();
					if (innerIter.hasNext()) {
						statistics.incrementCacheHits(System.nanoTime() - start);
						// is this correct hit/getTime it all in the innerIter.hasNext call - nothing in the innerIter.next() call (Jim)
						// - yes this is correct (Rich)
						nextEntry = innerIter.next();
					}
				}
				return nextEntry != null;
			}
			@Override
			public Entry<K, V> next() {
			      if (hasNext()) {
			    	  Entry<K, V> lastEntry = nextEntry;
			    	  //reset nextEntry to force fetching the next available entry
			    	  nextEntry = null;
			        return lastEntry;
			      } else {
			        throw new NoSuchElementException();
			      }
			}
			@Override
			public void remove() {
				long start = System.nanoTime();
				innerIter.remove();
				statistics.incrementCacheRemovals(System.nanoTime() - start);	
			}
		};
	}

	// Access methods for the statistics 
	@Override
	public long getCacheHits() {
		return statistics.getCacheHits();
	}

	@Override
	public float getCacheHitPercentage() {
		return statistics.getCacheHitPercentage();
	}

	@Override
	public long getCacheMisses() {
		return statistics.getCacheMisses();
	}

	@Override
	public float getCacheMissPercentage() {
		return statistics.getCacheMissPercentage();
	}

	@Override
	public long getCacheGets() {
		return statistics.getCacheGets();
	}

	@Override
	public long getCachePuts() {
		return statistics.getCachePuts();
	}

	@Override
	public long getCacheRemovals() {
		return statistics.getCacheRemovals();
	}

	@Override
	public long getCacheEvictions() {
		return statistics.getCacheEvictions();
	}

	@Override
	public float getAverageGetTime() {
		return statistics.getAverageGetTime();
	}

	@Override
	public float getAveragePutTime() {
		return statistics.getAveragePutTime();
	}

	@Override
	public float getAverageRemoveTime() {
		return statistics.getAverageRemoveTime();
	}
	
	@Override
	public String toString() {
		return statistics.toString();
	}
	
	@Override
	public void wrappedPut(K key, V value) throws CacheNotModified {
		long start = System.nanoTime();
		try {
			innerCache.wrappedPut(key, value);
			statistics.incrementCachePuts(System.nanoTime() - start);
		} catch (CacheNotModified cnm) {
			// the cache was not modified because of an expiry, so we don't increment the stats
		}
	}
	
	@Override
	public void wrappedPutAll(Map<? extends K, ? extends V> map) throws KeysNotModified {
		long start = System.nanoTime();
		int nputs = map.size();
		try {
			innerCache.wrappedPutAll(map);
		} catch (KeysNotModified knm) {
			nputs = nputs - knm.getUnwrittenKeys().size();
		}
		long duration = System.nanoTime() - start;
		statistics.increaseCachePuts(nputs, duration);
	}

	public CacheStats getStats() {
		return statistics;
	}
}
