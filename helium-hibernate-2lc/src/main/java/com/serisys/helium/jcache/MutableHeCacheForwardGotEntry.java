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

import java.util.List;
import java.util.Map;

import javax.cache.Cache;
import javax.cache.integration.CacheLoader;
import javax.cache.processor.MutableEntry;

import com.serisys.helium.jcache.HeCache.ValueHolder;

public class MutableHeCacheForwardGotEntry<K,V> implements MutableEntry<K, V> {
	private final K key;
	private final Cache<K,V> cache;
	private final V init_value;
	private final byte[] init_bytes;
	private final Map<K,V> local = new java.util.HashMap<K,V>(7);
	private List<Action> acts = new java.util.ArrayList<Action>(); 
	private Action last_act = Action.NONE;
	private final CacheLoader<K, V> loader;
	private final HeCache<K,V> helperCache;
	
	public MutableHeCacheForwardGotEntry(K key, ValueHolder value_holder, Cache<K, V> cache, HeCache<K,V> helperCache, CacheLoader loader) {
		this.key = key;
		this.cache = cache;
		if (value_holder != null) {
			this.init_value = (V) value_holder.getValue();
			this.init_bytes = value_holder.getValueBytes();
		} else {
			init_value = null;
			init_bytes = new byte[0];
		}
		//this.init_bytes = init_bytes;
		acts.add(Action.NONE);
		this.loader = loader;
		this.helperCache = helperCache;
	}

	@Override
	public final K getKey() {
		return key;
	}

	@Override
	public V getValue() {
		if (last_act == Action.NONE) {
			// this is a "fetch"
//			last_act = Action.ACCESS;
			if (init_value == null) {
				if (loader != null) {
					V value = loader.load(key);
					if (value != null) {
						local.put(key, value);
						last_act = Action.LOAD;
					}
				}
			} else {
				last_act = Action.ACCESS;
				if (initValueIsExpired()) {
					local.remove(key);
				} else {
					local.put(key, init_value);
				}
			}
		}
		return local.get(key);
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
	public boolean exists() {
		// Cache.containsKey does not invoke the loader
		if (last_act == Action.NONE) {
			last_act = Action.ACCESS;
			if (init_value != null && initValueIsExpired()) {
				return false;
			} else {
				if (init_value != null) {
					local.put(key, init_value);
					return true;
				} else {
					return false;
				}
			}
		}
		return local.containsKey(key);
	}

	@Override
	public void remove() {
		last_act = last_act == Action.CREATE ? Action.NULL : Action.REMOVE;
		local.remove(key);
	}

	@Override
	public void setValue(V value) {
		// I don't think we should check for null unless this is the end net result 
		// But if we don't I think we might get in a bit of a state
//		if (value == null) {
//			throw new NullPointerException();
//		}
		if (init_value == null || initValueIsExpired()) {
			last_act = Action.CREATE;
		} else {
			last_act = Action.UPDATE;
		}
		local.put(key, value);
	}
	
	protected void apply() {
		switch (last_act) {
		case ACCESS:
			helperCache.update_exp_for_access(key, getValue());
			break;
		case CREATE:
			cache.put(key, getValue());
			break;
		case LOAD:
			helperCache._put(key, getValue());
			break;
		case NONE:
			break;
		case NULL:
			break;
		case REMOVE:
			cache.remove(key);
			break;
		case UPDATE:
			cache.put(key, getValue());
			break;
		default:
			break;
		}
	}
	
	private boolean initValueIsExpired() {
		return helperCache.isExpired(init_bytes); 
	}

	enum Action {
		ACCESS, CREATE, LOAD, NONE, NULL, REMOVE, UPDATE;
	}
}
