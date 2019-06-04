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

import javax.cache.Cache;

public interface WrappableCache<K,V> extends CacheWrapper<K, V> {
	/*
	 * These are the methods for which the stats wrapper assumes the cache 
	 * will be updated but this can not occur in the event that the 
	 * expiry policy specifies "immediate expiry upon creation".  
	 */
	
	V wrappedGetAndPut(K key, V value) throws CacheNotModified;
	
//	V wrappedGetAndReplace(K key, V value) throws CacheNotModified;
	
	void wrappedPut(K key, V value) throws CacheNotModified;
	
	void wrappedPutAll(java.util.Map<? extends K, ? extends V> map) throws KeysNotModified;
	
//	boolean wrappedPutIfAbsent(K key, V value) throws CacheNotModified;
	
//	boolean wrappedReplace(K key, V value) throws CacheNotModified;
	
//	boolean wrappedReplace(K key, V oldValue, V newValue) throws CacheNotModified;

}
