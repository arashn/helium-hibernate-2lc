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

package com.serisys.helium.jcache;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.integration.CompletionListener;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.EntryProcessorResult;
import javax.management.ObjectName;

import com.serisys.helium.jcache.mx.CacheMXStatsBeanImpl;
import com.serisys.helium.jcache.util.MBeanServerRegistrationUtil;
import com.serisys.helium.jcache.util.MBeanServerRegistrationUtil.MBeanType;

public class WrappableCacheWrapper<K, V>  implements CacheWrapper<K, V> {

	private WrappableCache<K,V> innerCache;
	
	public V get(K key) {
		return innerCache.get(key);
	}
	
	@Override
	public HeCache<K, V>.ValueHolder getNoExpiryCheck(K key) {
		return innerCache.getNoExpiryCheck(key);
	}

	public Map<K, V> getAll(Set<? extends K> keys) {
		return innerCache.getAll(keys);
	}

	public boolean containsKey(K key) {
		return innerCache.containsKey(key);
	}

	public void loadAll(Set<? extends K> keys, boolean replaceExistingValues, CompletionListener completionListener) {
		innerCache.loadAll(keys, replaceExistingValues, completionListener);
	}

	public void put(K key, V value) {
		try {
			innerCache.wrappedPut(key, value);
		} catch (CacheNotModified cnm) {
			// nothing to do here
		}
	}

	public V getAndPut(K key, V value) {
		return innerCache.getAndPut(key, value);
	}

	public void putAll(Map<? extends K, ? extends V> map) {
		try {
			innerCache.wrappedPutAll(map);
		} catch (KeysNotModified e) {
			// nothing to do here
		}
	}

	public boolean putIfAbsent(K key, V value) {
		return innerCache.putIfAbsent(key, value);
	}

	public boolean remove(K key) {
		return innerCache.remove(key);
	}

	public boolean remove(K key, V oldValue) {
		return innerCache.remove(key, oldValue);
	}

	public V getAndRemove(K key) {
		return innerCache.getAndRemove(key);
	}

	public boolean replace(K key, V oldValue, V newValue) {
		return innerCache.replace(key, oldValue, newValue);
	}

	public boolean replace(K key, V value) {
		return innerCache.replace(key, value);
	}

	public V getAndReplace(K key, V value) {
		return innerCache.getAndReplace(key, value);
	}

	public void removeAll(Set<? extends K> keys) {
		innerCache.removeAll(keys);
	}

	public void removeAll() {
		innerCache.removeAll();
	}

	public void clear() {
		// Clear the actual cache - miss out the stats cache if it is present
		getHeCache().clear();
	}

	public <C extends Configuration<K, V>> C getConfiguration(Class<C> clazz) {
		return innerCache.getConfiguration(clazz);
	}

	public <T> T invoke(K key, EntryProcessor<K, V, T> entryProcessor, Object... arguments)
			throws EntryProcessorException {
		return innerCache.invoke(key, entryProcessor, arguments);
	}

	public <T> Map<K, EntryProcessorResult<T>> invokeAll(Set<? extends K> keys, EntryProcessor<K, V, T> entryProcessor,
			Object... arguments) {
		return innerCache.invokeAll(keys, entryProcessor, arguments);
	}

	public String getName() {
		return innerCache.getName();
	}

	public CacheManager getCacheManager() {
		return innerCache.getCacheManager();
	}

	public void close() {
		innerCache.close();
	}

	public boolean isClosed() {
		return innerCache.isClosed();
	}

	public <T> T unwrap(Class<T> clazz) {
		return innerCache.unwrap(clazz);
	}

	public void registerCacheEntryListener(CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration) {
		innerCache.registerCacheEntryListener(cacheEntryListenerConfiguration);
	}

	public void deregisterCacheEntryListener(CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration) {
		innerCache.deregisterCacheEntryListener(cacheEntryListenerConfiguration);
	}

	public Iterator<Entry<K, V>> iterator() {
		return innerCache.iterator();
	}

	public WrappableCacheWrapper(WrappableCache<K,V> inner) {
		this.innerCache = inner;
	}

	public WrappableCache<K,V> getInnerCache() {
		return innerCache;
	}
	public void setInnerCache(WrappableCache<K,V> inner) {
		this.innerCache = inner;
	}

	public void setManagementEnabled(boolean enabled) {
		getHeCache().setManagementEnabled(enabled);
		
	}
	private HeCache<K, V> getHeCache() {
		Cache<K, V> inner = getInnerCache();
		if (inner instanceof HeCache) {
			return (HeCache<K, V>) inner;
		}
		else {
			return (HeCache<K,V>) ((CacheMXStatsBeanImpl<K, V>) inner).getInnerCache();
		}
	}

	public void setStatisticsEnabled(boolean enabled) {
		ObjectName objectName = MBeanServerRegistrationUtil.objectNameFor(this, MBeanType.CacheStatistics);
		if (enabled) {
			WrappableCache<K, V> inner = getInnerCache();
			CacheMXStatsBeanImpl<K, V> stat_cache = null;
			if (inner instanceof HeCache) {
				stat_cache = new CacheMXStatsBeanImpl<K, V>(objectName, inner);
				setInnerCache(stat_cache);					
			}
			else {
				stat_cache = (CacheMXStatsBeanImpl<K, V>) inner;
			}
			// register MBean
			MBeanServerRegistrationUtil.registerMBean(stat_cache, objectName);
		}
		else {
			Cache<K, V> inner = getInnerCache();
			if (inner instanceof CacheMXStatsBeanImpl) {
				// remove the inner replace with the HeCache
				CacheMXStatsBeanImpl<K, V> b = (CacheMXStatsBeanImpl<K, V>) inner;				
				setInnerCache(b.getInnerCache());
			}
			// Unregister MBean
			MBeanServerRegistrationUtil.unregisterMBean(objectName);
		}
		getConfiguration(MutableConfiguration.class).setStatisticsEnabled(enabled);
	}
}
