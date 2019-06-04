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

package com.serisys.helium.jcache.events;

import javax.cache.Cache;
import javax.cache.event.CacheEntryEvent;
import javax.cache.event.EventType;

public class KeyValueEvent<K,V> extends CacheEntryEvent<K, V> {
	private static final long serialVersionUID = 4276964455449155067L;

	private final K key;
	private final V value;
	
	public KeyValueEvent(Cache source, EventType eventType, K key, V value) {
		super(source, eventType);
		this.key = key;
		this.value = value;
	}


	@Override
	public K getKey() {
		return key;
	}

	@Override
	public V getValue() {
		return value;
	}

	@Override
	public <T> T unwrap(Class<T> clazz) {
		if (clazz.isAssignableFrom(getClass())) {
			return clazz.cast(this);
		} else {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public V getOldValue() {
		return null;
	}

	@Override
	public boolean isOldValueAvailable() {
		return false;
	}

}
