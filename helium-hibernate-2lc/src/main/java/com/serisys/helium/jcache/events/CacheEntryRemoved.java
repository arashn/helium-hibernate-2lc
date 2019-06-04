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

import javax.cache.event.CacheEntryRemovedListener;
import javax.cache.event.CacheEntryEventFilter;

public class CacheEntryRemoved<K,V> extends CacheEntryListenerDoublet<K, V> {
	private final CacheEntryRemovedListener<K, V> listener;
	
	public CacheEntryRemoved(CacheEntryEventFilter<K, V> filter, CacheEntryRemovedListener<K, V> listener) {
		super(filter);
		this.listener = listener;
	}
	
	public CacheEntryRemovedListener<K, V> getListener() {
		return listener;
	}


}
