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

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;

import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheLoaderException;

public class ReadThroughDisabledCacheLoader<K,V> implements CacheLoader<K, V>, Closeable {
	private  final CacheLoader<K, V> delegate;

	public V load(K key) throws CacheLoaderException {
		return null;
	}

	public Map<K, V> loadAll(Iterable<? extends K> keys) throws CacheLoaderException {
		return delegate.loadAll(keys);
	}

	public ReadThroughDisabledCacheLoader(CacheLoader<K, V> delegate) {
		this.delegate = delegate;
	}
	
	@Override
	public void close() throws IOException {
		if (delegate instanceof Closeable) {
	    	 ((Closeable) delegate).close();
	     }
	}

}
