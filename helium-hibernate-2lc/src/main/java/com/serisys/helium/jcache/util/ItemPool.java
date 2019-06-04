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


import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import com.levyx.helium.HeliumItem;
import com.serisys.helium.jcache.HeCachingProvider;

/*
 * Based upon FastObjectPool http://ashkrit.blogspot.com/2013/05/lock-less-java-object-pool.html
 * With thanks to Ashkrit.
 */


public class ItemPool implements ItemManager {

    private ManagedHeliumItem[] objects;

    private volatile int takePointer;
    private int releasePointer;

    private final int mask;
    private final long BASE;
    private final long INDEXSCALE;
    private final long ASHIFT;

    public ReentrantLock lock = new ReentrantLock();
    private ThreadLocal<ManagedHeliumItem> localValue = new ThreadLocal<>();

    public ItemPool(int size) {
    	HeCachingProvider.logger.info(">>>>>>>>>>>>>>>> USING POOLED ITEM MANAGER");
        int newSize = 1;
        while (newSize < size) {
            newSize = newSize << 1;
        }
        size = newSize;
        objects = new ManagedHeliumItem[size];
        for (int x = 0; x < size; x++) {
            objects[x] = new ManagedHeliumItem(DEFAULT_KEY_BYTE_LENGTH, DEFAULT_VALUE_BYTE_LENGTH);
        }
        mask = size - 1;
        releasePointer = size;
        BASE = THE_UNSAFE.arrayBaseOffset(ManagedHeliumItem[].class);
        INDEXSCALE = THE_UNSAFE.arrayIndexScale(ManagedHeliumItem[].class);
        ASHIFT = 31 - Integer.numberOfLeadingZeros((int) INDEXSCALE);
    }

    /* (non-Javadoc)
	 * @see com.serisys.helium.jcache.util.ItemManager#take()
	 */
    @Override
	public HeliumItem take() {
        int localTakePointer;

        ManagedHeliumItem localObject = localValue.get();
        if (localObject != null) {
            if (localObject.state.compareAndSet(ManagedHeliumItem.FREE, ManagedHeliumItem.USED)) {
                return localObject;
            }
        }

        while (releasePointer != (localTakePointer = takePointer)) {
            int index = localTakePointer & mask;
            ManagedHeliumItem holder = objects[index];
            if (holder != null && THE_UNSAFE.compareAndSwapObject(objects, (index << ASHIFT) + BASE, holder, null)) {
                takePointer = localTakePointer + 1;
                if (holder.state.compareAndSet(ManagedHeliumItem.FREE, ManagedHeliumItem.USED)) {
                    localValue.set(holder);
                    return holder;
                }
            }
        }
        return null;
    }

    /* (non-Javadoc)
	 * @see com.serisys.helium.jcache.util.ItemManager#release(com.serisys.helium.jcache.util.ItemPool.ManagedHeliumItem)
	 */
    @Override
	public void release(HeliumItem item) {
    	ManagedHeliumItem object = (ManagedHeliumItem) item;
        lock.lock();
        try {
            int localValue = releasePointer;
            long index = ((localValue & mask) << ASHIFT) + BASE;
            if (object.state.compareAndSet(ManagedHeliumItem.USED, ManagedHeliumItem.FREE)) {
                THE_UNSAFE.putOrderedObject(objects, index, object);
                releasePointer = localValue + 1;
            } else {
                throw new IllegalArgumentException("Invalid reference passed");
            }
        } finally {
            lock.unlock();
        }
    }

    static class ManagedHeliumItem extends HeliumItem {
        public static final int FREE = 0;
        public static final int USED = 1;

        private AtomicInteger state = new AtomicInteger(FREE);

    	public ManagedHeliumItem(HeliumItem orig) {
    		super(orig);
    	}

    	public ManagedHeliumItem(int keyLength, int valueLength) {
    		super(keyLength, valueLength);
    	}

    }

    
    public static final Unsafe THE_UNSAFE;

    static {
        try {
            final PrivilegedExceptionAction<Unsafe> action = new PrivilegedExceptionAction<Unsafe>() {
                public Unsafe run() throws Exception {
                    Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
                    theUnsafe.setAccessible(true);
                    return (Unsafe) theUnsafe.get(null);
                }
            };

            THE_UNSAFE = AccessController.doPrivileged(action);
        } catch (Exception e) {
            throw new RuntimeException("Unable to load unsafe", e);
        }
    }
}