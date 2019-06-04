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

package com.serisys.helium.jcache.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class ImmutableEmptyMap<K, V> implements Map<K, V> {
	public static Map SINGLETON = new ImmutableEmptyMap(); 

	private ImmutableEmptyMap() {
		
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public boolean isEmpty() {
		return true;
	}

	@Override
	public boolean containsKey(Object key) {
		return false;
	}

	@Override
	public boolean containsValue(Object value) {
		return false;
	}

	@Override
	public V get(Object key) {
		return null;
	}

	@Override
	public V put(K key, V value) {
		return null;
	}

	@Override
	public V remove(Object key) {
		return null;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		return;
	}

	@Override
	public void clear() {
		return;
	}

	@Override
	public Set<K> keySet() {
		return ImmutableEmptySet.SINGLETON;
	}

	@Override
	public Collection<V> values() {
		return ImmutableEmptySet.SINGLETON;
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		return ImmutableEmptySet.SINGLETON;
	}

}
