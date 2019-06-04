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

package com.serisys.helium.jcache.mx;

import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.LongAdder;

public class CacheStats {

	private static final long ONE_L = 1L;
	private static final float ONE_F = 1f;
	protected static final float FLOAT_HUNDRED = 100.0f;
    protected static final long NANOSECONDS_IN_A_MICROSECOND = 1000L;

    public static float getAverageGetTime(long no_gets, long time_gets_nanos) {
        if (time_gets_nanos == 0 || no_gets == 0) {
            return 0;
        }
        return ((ONE_F * time_gets_nanos) / no_gets) / NANOSECONDS_IN_A_MICROSECOND;
    }

    public static float getAveragePutTime(long no_puts, long time_put_nanos) {
        if (time_put_nanos == 0 || no_puts == 0) {
            return 0;
        }
        return ((ONE_F * time_put_nanos) / no_puts) / NANOSECONDS_IN_A_MICROSECOND;
    }
    public static float getAverageRemoveTime(long no_removals, long time_removal_nanos) {
        if (time_removal_nanos == 0 || no_removals == 0) {
            return 0;
        }
        return ((ONE_F * time_removal_nanos) / no_removals) / NANOSECONDS_IN_A_MICROSECOND;
    }
    public static float getCacheHitPercentage(long no_hits, long no_gets) {
        if (no_hits == 0 || no_gets == 0) {
            return 0;
        }
        return (float) no_hits / no_gets * FLOAT_HUNDRED;
    }
    public static float getCacheMissPercentage(long no_misses, long no_gets) {
        if (no_misses == 0 || no_gets == 0) {
            return 0;
        }
        return (float) no_misses / no_gets * FLOAT_HUNDRED;
    }
    public static <E> void setMax(E obj, AtomicLongFieldUpdater<E> updater, long value) {
	    long current = updater.get(obj);
	    if (current >= value) {
	        return;
	    }
	
	    if (updater.compareAndSet(obj, current, value)) {
	        return;
	    }
    }
    

    protected LongAdder cacheEvictions = new LongAdder();
    protected LongAdder cacheExpiries = new LongAdder();
    protected LongAdder cacheGetTimeTakenNanos = new LongAdder();
    protected LongAdder cacheHits = new LongAdder();
    protected LongAdder cacheMisses = new LongAdder();
    protected LongAdder cachePuts = new LongAdder();
    protected LongAdder cachePutTimeTakenNanos = new LongAdder();
    protected LongAdder cacheRemovals = new LongAdder();
    protected LongAdder cacheRemoveTimeTakenNanos = new LongAdder();
    protected long creationTime;
    
    public CacheStats(long creationTime) {
    	this.creationTime = creationTime;
    }

    private void addGetTimeNanos(long duration) {
        if (cacheGetTimeTakenNanos.longValue() + duration >= Long.MAX_VALUE) {
        	//counter full. Just reset.
        	clear();
        }
        cacheGetTimeTakenNanos.add(duration);
    }

    private void addPutTimeNanos(long duration) {
    	if (cachePutTimeTakenNanos.longValue() + duration >= Long.MAX_VALUE) {
    		//counter full. Just reset.
    		clear();
    	}
    	cachePutTimeTakenNanos.add(duration);
    }

    protected void addRemoveTimeNanos(long duration) {
        if (cacheRemoveTimeTakenNanos.longValue() + duration >= Long.MAX_VALUE) {
        	//counter full. Just reset.
            clear();
          }
        cacheRemoveTimeTakenNanos.add(duration);
    }

    public void clear() {
        cachePuts.reset();
        cacheMisses.reset();
        cacheRemovals.reset();
        cacheExpiries.reset();
        cacheHits.reset();
        cacheEvictions.reset();
        cacheGetTimeTakenNanos.reset();
        cachePutTimeTakenNanos.reset();
        cacheRemoveTimeTakenNanos.reset();
    }
    
    public float getAverageGetTime() {
    	return getAverageGetTime(getCacheGets(), getCacheGetTimeTakenNanos());
    }

    public float getAveragePutTime() {
    	return getAverageGetTime(getCachePuts(), getCachePutTimeTakenNanos());
    }

    public float getAverageRemoveTime() {
    	return getAverageRemoveTime(getCacheRemovals(), getCacheRemoveTimeTakenNanos());
    }
    
    public long getCacheEvictions() {
        return cacheEvictions.longValue();
    }
    
    public long getCacheExpiries() {
        return cacheExpiries.longValue();
    }

    public long getCacheGets() {
        return getCacheHits() + getCacheMisses();
    }

    public long getCacheGetTimeTakenNanos() {
        return cacheGetTimeTakenNanos.longValue();
    }

    public float getCacheHitPercentage() {
    	return getCacheHitPercentage(getCacheHits(), getCacheGets());
    }

    public long getCacheHits() {
        return cacheHits.longValue();
    }
    
    public long getCacheMisses() {
        return cacheMisses.longValue();
    }
    
    public float getCacheMissPercentage() {
        return getCacheMissPercentage(getCacheMisses(), getCacheGets());
    }
    
    public long getCachePuts() {
        return cachePuts.longValue();
    }
    
    public long getCachePutTimeTakenNanos() {
        return cachePutTimeTakenNanos.longValue();
    }

    public long getCacheRemovals() {
        return cacheRemovals.longValue();
    }

    public long getCacheRemoveTimeTakenNanos() {
        return cacheRemoveTimeTakenNanos.longValue();
    }

    public long getCreationTime() {
        return creationTime;
    }

    public CacheStats_Snapshot getSnapshot() {
		return new CacheStats_Snapshot(this);
	}

    public void increaseCacheEvictions(long number) {
        cacheEvictions.add(number);
    }

    public void increaseCacheExpiries(long number) {
        cacheExpiries.add(number);
    }

    public void increaseCacheHits(long number, long duration) {
        cacheHits.add(number);
        addGetTimeNanos(duration);
    }

    public void increaseCacheHitsAndMisses(long no_hits, long no_mises, long duration) {
        cacheHits.add(no_hits);
        cacheMisses.add(no_mises);
        addGetTimeNanos(duration);
    }

    public void increaseCacheMisses(long number, long duration) {
        cacheMisses.add(number);
        addGetTimeNanos(duration);
    }

    public void increaseCachePuts(long number, long duration) {
        cachePuts.add(number);
        addPutTimeNanos(duration);
    }

    public void increaseCacheRemovals(long number, long duration) {
        cacheRemovals.add(number);
        addRemoveTimeNanos(duration);
    }

    public void incrementCacheEvictions() {
    	// could call cacheEvictions.increment() but save one method call by invoking add(1L)
        cacheEvictions.add(ONE_L);
    }

    public void incrementCacheExpiries() {
        cacheExpiries.add(ONE_L);
    }

    public void incrementCacheHits() {
        cacheHits.add(ONE_L);
    }
    public void incrementCacheHits(long duration) {
        cacheHits.add(ONE_L);
        addGetTimeNanos(duration);
    }

    public void incrementCacheMisses() {
        cacheMisses.add(ONE_L);
    }
    public void incrementCacheMisses(long duration) {
        cacheMisses.add(ONE_L);
        addGetTimeNanos(duration);
    }

    public void incrementCachePuts(long duration) {
        cachePuts.add(ONE_L);
        addPutTimeNanos(duration);
    }

    public void incrementCacheRemovals(long duration) {
        cacheRemovals.add(ONE_L);
        addRemoveTimeNanos(duration);
    }
    

	public String toString() {
    	StringBuilder bob = new StringBuilder();
    	toString(bob);
    	bob.append('}');
    	return bob.toString();
    }
	protected void toString(StringBuilder bob) {
		bob.append("CacheStats{creationTime=")
			.append(creationTime)
			.append(", removals=")
			.append(cacheRemovals)
			.append(", expiries=")
			.append(cacheExpiries)
			.append(", puts=")
			.append(cachePuts)
			.append(", hits=")
			.append(cacheHits)
			.append(", misses=")
			.append(cacheMisses)
			.append(", evictions=")
			.append(cacheEvictions)
			.append(", putTimeTakenNanos=")
			.append(cachePutTimeTakenNanos)
			.append(", getCacheTimeTakenNanos=")
			.append(cacheGetTimeTakenNanos)
			.append(", removeTimeTakenNanos=")
			.append(cacheRemoveTimeTakenNanos);
	}
}
