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
import java.util.Collection;

import javax.cache.Cache.Entry;
import javax.cache.integration.CacheWriter;
import javax.cache.integration.CacheWriterException;

public class CacheWriterHandler<K,V> implements CacheWriter<K, V>, Closeable {
	private final CacheWriter<K,V> delegate;
	public void write(Entry<? extends K, ? extends V> entry) throws CacheWriterException {
		try {
			delegate.write(entry);
		} catch (CacheWriterException e) {
			throw e;
		} catch (Throwable t) {
			throw new CacheWriterException(t);
		}
	}
	public void writeAll(Collection<Entry<? extends K, ? extends V>> entries) throws CacheWriterException {
		try {
			delegate.writeAll(entries);
		} catch (CacheWriterException e) {
			throw e;
		} catch (Throwable t) {
			throw new CacheWriterException(t);
		}
	}
	public void delete(Object key) throws CacheWriterException {
		try {
			delegate.delete(key);
		} catch (CacheWriterException e) {
			throw e;
		} catch (Throwable t) {
			throw new CacheWriterException(t);
		}
	}
	public void deleteAll(Collection<?> keys) throws CacheWriterException {
		try { 
			delegate.deleteAll(keys);
		} catch (CacheWriterException e) {
			throw e;
		} catch (Throwable t) {
			throw new CacheWriterException(t);
		}
	}
	public CacheWriterHandler(CacheWriter<K,V> delegate) {
		this.delegate = delegate;
	}
	
	@Override
	public void close() throws IOException {
	      if (delegate instanceof Closeable) {
	    	  ((Closeable) delegate).close();
	      }
	}

}
