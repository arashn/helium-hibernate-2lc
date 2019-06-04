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

import java.util.Collection;

import javax.cache.Cache.Entry;
import javax.cache.integration.CacheWriter;
import javax.cache.integration.CacheWriterException;

public class NoWriteThroughCacheWriter<K,V> implements CacheWriter<K, V> {
	public static CacheWriter SINGLETON = new NoWriteThroughCacheWriter();
	
	private NoWriteThroughCacheWriter() {
		
	}

	@Override
	public void write(Entry<? extends K, ? extends V> entry) throws CacheWriterException {
		// do nothing (don't fail)
	}

	@Override
	public void writeAll(Collection<Entry<? extends K, ? extends V>> entries) throws CacheWriterException {
		// I am a great success!
		entries.clear();
	}

	@Override
	public void delete(Object key) throws CacheWriterException {
		// do nothing (don't fail)
	}

	@Override
	public void deleteAll(Collection<?> keys) throws CacheWriterException {
		// write through must enable cache to remove all entries by emptying the keys
		// which indicates that write-through has "removed" everything to its satisfaction
		keys.clear();
	}

}
