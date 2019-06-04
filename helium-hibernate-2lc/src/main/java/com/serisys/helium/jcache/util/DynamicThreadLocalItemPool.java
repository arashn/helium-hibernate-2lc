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

import java.util.Stack;

import com.levyx.helium.HeliumItem;
import com.serisys.helium.jcache.HeCachingProvider;

public class DynamicThreadLocalItemPool implements ItemManager {
	private final ThreadLocal<Stack<HeliumItem>> pools = new ThreadLocal<Stack<HeliumItem>>();

	public DynamicThreadLocalItemPool() {
    	HeCachingProvider.logger.info(">>>>>>>>>>>>>>>> USING DYNAMIC THREAD LOCAL ITEM POOL");
	}

	@Override
	public HeliumItem take() {
		Stack<HeliumItem> pool = pools.get();
		if (pool == null) {
			pool = new java.util.Stack<HeliumItem>();
			pools.set(pool);
		}
		HeliumItem item = null;
		if (pool.isEmpty()) {
			item = new HeliumItem(DEFAULT_KEY_BYTE_LENGTH, DEFAULT_VALUE_BYTE_LENGTH);
		} else {
			item = pool.pop();
		}
		return item;
	}

	@Override
	public void release(HeliumItem object) {
		Stack<HeliumItem> pool = pools.get();
		if (pool == null) { // may be a bit defensive
			throw new IllegalStateException();
		}
		pool.push(object);
	}

}
