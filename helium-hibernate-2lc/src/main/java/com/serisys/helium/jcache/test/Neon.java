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

package com.serisys.helium.jcache.test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Map;

import com.levyx.helium.Helium;
import com.levyx.helium.HeliumEnvironment;
import com.levyx.helium.HeliumException;
import com.levyx.helium.HeliumItem;
import com.levyx.helium.HeliumIterator;
import com.levyx.helium.HeliumReverseIterator;
import com.levyx.helium.HeliumStats;

public class Neon extends Helium {
	private final Map<NeKey, NeVal> store = new java.util.HashMap<NeKey, NeVal>();
	
	protected Neon() {
		super();
	}

	public Neon(String url, String name, int flags) {
		super(url, name, flags);
		System.out.println("**** Using Neon dummy Helium");
	}

	public Neon(String url, String name, int flags, HeliumEnvironment env) {
		super(url, name, flags, env);
	}

	@Override
	public void close() {
		store.clear();
		super.close();
	}

	@Override
	public void commit() {
		super.commit();
	}

	@Override
	public void delete(HeliumItem item) {
		store.remove(new NeKey(item.getKeyBytes()));
	}

	@Override
	public void deleteLookup(HeliumItem item, int offset, int length) {
		deleteLookup(item);
	}

	@Override
	public void deleteLookup(HeliumItem item) {
		NeKey key = new NeKey(item.getKeyBytes());
		boolean in = store.containsKey(key);
		if (!in) {
			throw newHeliumException(HE_ERR_ITEM_NOT_FOUND);
		}
		NeVal val = store.remove(key);
		item.setValueBytes(val.value);
	}

	@Override
	public void deleteLookupDynamic(HeliumItem item, int offset, int length) {
		deleteLookup(item);
	}

	@Override
	public void deleteLookupDynamic(HeliumItem item) {
		deleteLookup(item);
	}

	@Override
	public void discard() {
		super.discard();
	}

	@Override
	public boolean exists(HeliumItem item) {
		return store.containsKey(new NeKey(item.getKeyBytes()));
	}

	@Override
	public void existsThrows(HeliumItem item) {
		if (!exists(item)) {
			throw newHeliumException(HE_ERR_ITEM_NOT_FOUND);
		}
	}

	@Override
	public void first(HeliumItem item, int offset, int length) {
		// we don't use
		super.first(item, offset, length);
	}

	@Override
	public void first(HeliumItem item) {
		// we don't use
		super.first(item);
	}

	@Override
	public HeliumStats getStats() {
		// we don't use
		return super.getStats();
	}

	@Override
	public HeliumStats getStats(HeliumStats stats) {
		// we don't use
		return super.getStats(stats);
	}

	@Override
	public void insert(HeliumItem item) {
		NeKey key = new NeKey(item.getKeyBytes());
		if (store.containsKey(key)) {
			throw newHeliumException(HE_ERR_ITEM_EXISTS);
		}
		store.put(key, new NeVal(item.getValueBytes()));
	}

	@Override
	public boolean isReadOnly() {
		// we don't use
		return super.isReadOnly();
	}

	@Override
	public boolean isTransaction() {
		// we don't use
		return super.isTransaction();
	}

	@Override
	public boolean isValid() {
		// we don't use
		return super.isValid();
	}

	@Override
	public HeliumIterator iterator() {
		return iterator(4096);
	}
	
	@Override
	public HeliumIterator iterator(int maxValueSize) {
		return new NeIterator(this, maxValueSize, store.entrySet().iterator());
	}

	@Override
	public void last(HeliumItem item, int offset, int length) {
		// we don't use
		super.last(item, offset, length);
	}

	@Override
	public void last(HeliumItem item) {
		// we don't use
		super.last(item);
	}

	@Override
	public void lookup(HeliumItem item, int offset, int length) {
		lookup(item);
	}

	@Override
	public void lookup(HeliumItem item) {
		NeKey key = new NeKey(item);
		NeVal val = store.get(key);
		if (val == null) {
			throw newHeliumException(HE_ERR_ITEM_NOT_FOUND);
		}
		item.setValueBytes(val.value);
	}

	@Override
	public void lookupDynamic(HeliumItem item, int offset, int length) {
		lookup(item);
	}

	@Override
	public void lookupDynamic(HeliumItem item) {
		lookup(item);
	}

	@Override
	public void next(HeliumItem item, int offset, int length) {
		// we don't use
		super.next(item, offset, length);
	}

	@Override
	public void next(HeliumItem item) {
		// we don't use
		super.next(item);
	}

	@Override
	public void previous(HeliumItem item, int offset, int length) {
		// we don't use
		super.previous(item, offset, length);
	}

	@Override
	public void previous(HeliumItem item) {
		// we don't use
		super.previous(item);
	}

	@Override
	public void removeDatastore() {
		// we don't use
		super.removeDatastore();
	}

	@Override
	public void renameDatastore(String name) {
		// we don't use
		super.renameDatastore(name);
	}

	@Override
	public void replace(HeliumItem item) {
		NeKey key = new NeKey(item);
		if (store.containsKey(key)) {
			store.replace(key, new NeVal(item));
			return;
		}
		throw newHeliumException(HE_ERR_ITEM_NOT_FOUND);
	}

	@Override
	public HeliumReverseIterator reverseIterator(int maxValueSize) {
		// we don't use
		return super.reverseIterator(maxValueSize);
	}

	@Override
	public String toString() {
		return super.toString();
	}

	@Override
	public void update(HeliumItem item) {
		store.put(new NeKey(item), new NeVal(item));
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
	}

	private HeliumException newHeliumException(int err_code) {
		Class hex_claz = HeliumException.class;
		try {
			Constructor<HeliumException> constr = hex_claz.getDeclaredConstructor(new Class[] {int.class} );
			constr.setAccessible(true);
			Object[] args = new Object[] {new Integer(err_code)};
			HeliumException hex = constr.newInstance(args);
			return hex;
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		throw new Error();
	}


	static class NeKey {
		final byte[] value;
		int hash;
		NeKey(HeliumItem item) {
			this(item.getKeyBytes());
		}
		NeKey(byte[] bytes) {
			value = bytes;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj == null)
				return false; 
			NeKey other = null;
			try {
				other = (NeKey) obj;
			} catch (ClassCastException e) {
				return false; 
			}
			if (value.length != other.value.length)
				return false;
			for (int i = 0; i < value.length; i++) {
				if (value[i] != other.value[i]) 
					return false;
			}
			return true;
		}

		@Override
	    public int hashCode() {
	        int h = hash;
	        int length = Math.min(32, value.length);
	        if (h == 0 && length > 0) {
	            byte val[] = value;

	            for (int i = 0; i < length; i++) {
	                h = 31 * h + val[i];
	            }
	            hash = h;
	        }
	        return h;
	    }

	}
	
	static class NeVal extends NeKey {
		NeVal(HeliumItem item) {
			this(item.getValueBytes());
		}
		NeVal(byte[] bytes) {
			super(bytes);
		}
	}

	class NeIterator extends HeliumIterator {
		private Iterator<Map.Entry<NeKey, NeVal>> maperator;
		
		public NeIterator(Helium he, int maxValueSize, Iterator<Map.Entry<NeKey, NeVal>> maperator) {
			super(he, maxValueSize);
			this.maperator = maperator;
		}
		
		@Override
		public HeliumItem next() {
			Map.Entry<NeKey, NeVal> entry = maperator.next();
			HeliumItem item = new HeliumItem(entry.getKey().value.length, entry.getValue().value.length);
			item.setKeyBytes(entry.getKey().value);
			item.setValueBytes(entry.getValue().value);
			return item;
		}
		
		@Override
		public boolean hasNext() {
			return maperator.hasNext();
		}
		
		@Override
		public void remove() {
			maperator.remove();
		}

	}
}
