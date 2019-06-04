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

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.integration.CompletionListener;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.EntryProcessorResult;

public class UnimplementedCache<K,V> implements Cache<K, V> {

	@Override
	public V get(K key) {
		return null;
	}

	@Override
	public Map<K, V> getAll(Set<? extends K> keys) {
		return null;
	}

	@Override
	public boolean containsKey(K key) {
		return false;
	}

	@Override
	public void loadAll(Set<? extends K> keys, boolean replaceExistingValues, CompletionListener completionListener) {
		
	}

	@Override
	public void put(K key, V value) {
		
	}

	@Override
	public V getAndPut(K key, V value) {
		return null;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> map) {
		
	}

	@Override
	public boolean putIfAbsent(K key, V value) {
		return false;
	}

	@Override
	public boolean remove(K key) {
		return false;
	}

	@Override
	public boolean remove(K key, V oldValue) {
		return false;
	}

	@Override
	public V getAndRemove(K key) {
		return null;
	}

	@Override
	public boolean replace(K key, V oldValue, V newValue) {
		return false;
	}

	@Override
	public boolean replace(K key, V value) {
		return false;
	}

	@Override
	public V getAndReplace(K key, V value) {
		return null;
	}

	@Override
	public void removeAll(Set<? extends K> keys) {
		
	}

	@Override
	public void removeAll() {
		
	}

	@Override
	public void clear() {
		
	}

	@Override
	public <C extends Configuration<K, V>> C getConfiguration(Class<C> clazz) {
		return null;
	}

	@Override
	public <T> T invoke(K key, EntryProcessor<K, V, T> entryProcessor, Object... arguments)
			throws EntryProcessorException {
		return null;
	}

	@Override
	public <T> Map<K, EntryProcessorResult<T>> invokeAll(Set<? extends K> keys, EntryProcessor<K, V, T> entryProcessor,
			Object... arguments) {
		return null;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public CacheManager getCacheManager() {
		return null;
	}

	@Override
	public void close() {
		
	}

	@Override
	public boolean isClosed() {
		return false;
	}

	@Override
	public <T> T unwrap(Class<T> clazz) {
		return null;
	}

	@Override
	public void registerCacheEntryListener(CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration) {
		
	}

	@Override
	public void deregisterCacheEntryListener(CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration) {
		
	}

	@Override
	public Iterator<Entry<K, V>> iterator() {
		return null;
	}

}
